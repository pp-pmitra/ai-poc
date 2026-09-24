#!/usr/bin/env python3
"""Validated, concurrency-safe append to kavach-data/history/fix-history.json.

Before this script existed, the verdict-reporting skill hand-wrote entries
into fix-history.json directly (read whole file, append in memory, write
whole file back) with no schema check and no locking — a malformed entry
(wrong enum, missing key) was a silent contract break that fix_pattern_cache.py
and review_verdicts.py would only discover later, and two runs finishing close
together could lose one's writes to a read-modify-write race. This script is
the single place that validates an entry against the same machine-format
enums validate_kavach_contract.py enforces on combined-receipts.json, and
serializes the actual file write with an exclusive flock so concurrent CI
runs can't clobber each other.

All-or-nothing: if any entry in the batch fails validation, nothing is
written — a bad entry for one scenario must not corrupt the file for the
correctly-diagnosed scenarios in the same run.

Usage:
    python3 append_fix_history.py < entries.json
    echo '[{...}, {...}]' | python3 append_fix_history.py
    python3 append_fix_history.py --fix-history <path> < entries.json

Exit 0 and prints "OK: appended N entr(y/ies), M total in <path>" on success.
Exit 1 and prints one line per problem, appending nothing, otherwise.
"""

from __future__ import annotations

import argparse
import fcntl
import json
import sys
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).resolve().parent))

from validate_kavach_contract import VALID_CONFIDENCE, VALID_TIERS, VALID_VERDICTS

_SCRIPTS_DIR = Path(__file__).resolve().parent
DEFAULT_FIX_HISTORY = _SCRIPTS_DIR.parents[3] / "kavach-data" / "history" / "fix-history.json"

# fix-history.json also records script_issue_fix_applied once kavach-repair
# has actually applied a fix — a verdict combined-receipts.json never carries
# (it's assigned after repair, not by kavach-diagnose), so it's not part of
# validate_kavach_contract.py's VALID_VERDICTS.
FIX_HISTORY_VALID_VERDICTS = VALID_VERDICTS | {"script_issue_fix_applied"}

REQUIRED_ENTRY_KEYS = (
    "timestamp",
    "runDate",
    "scenarioName",
    "featureFile",
    "verdict",
    "confidence",
    "priority",
    "groupId",
    "isGroupRepresentative",
    "attempts",
    "analysisTier",
    "review",
)

VALID_REVIEW_STATUSES = {"unreviewed", "agreed", "disagreed"}


def validate_entry(entry: Any, index: int) -> list[str]:
    problems: list[str] = []
    if not isinstance(entry, dict):
        return [f"entries[{index}]: expected an object, got {type(entry).__name__}"]

    prefix = f"entries[{index}] (scenarioName={entry.get('scenarioName')!r})"

    for key in REQUIRED_ENTRY_KEYS:
        if key not in entry:
            problems.append(f"{prefix}: missing required key {key!r}")

    verdict = entry.get("verdict")
    if verdict is not None and verdict not in FIX_HISTORY_VALID_VERDICTS:
        problems.append(f"{prefix}: verdict {verdict!r} not one of {sorted(FIX_HISTORY_VALID_VERDICTS)}")

    confidence = entry.get("confidence")
    if confidence is not None and confidence not in VALID_CONFIDENCE:
        problems.append(f"{prefix}: confidence {confidence!r} not one of {sorted(VALID_CONFIDENCE)}")

    tier = entry.get("analysisTier")
    if tier is not None and tier not in VALID_TIERS:
        problems.append(f"{prefix}: analysisTier {tier!r} not one of {sorted(VALID_TIERS)}")

    attempts = entry.get("attempts")
    if attempts is not None and (not isinstance(attempts, int) or isinstance(attempts, bool) or attempts < 1):
        problems.append(f"{prefix}: attempts must be a positive integer, got {attempts!r}")

    is_representative = entry.get("isGroupRepresentative")
    if is_representative is not None and not isinstance(is_representative, bool):
        problems.append(f"{prefix}: isGroupRepresentative must be a boolean, got {is_representative!r}")

    review = entry.get("review")
    if review is not None:
        if not isinstance(review, dict) or review.get("status") not in VALID_REVIEW_STATUSES:
            problems.append(
                f"{prefix}: review must be an object with status in {sorted(VALID_REVIEW_STATUSES)}, got {review!r}"
            )

    # tier2_live_replay is the only tier a real browser backs; every other
    # tier's liveVerification must be explicitly null, never fabricated
    # {"elementFound": true, ...} — this is the one gate that can't be
    # re-derived from combined-receipts.json alone, so it's worth checking here.
    if tier is not None and tier != "tier2_live_replay" and entry.get("liveVerification") not in (None,):
        problems.append(f"{prefix}: liveVerification must be null for analysisTier {tier!r}, got a non-null value")

    return problems


def _load_locked(f) -> list[dict[str, Any]]:
    text = f.read()
    return json.loads(text) if text.strip() else []


def _dedup_key(entry: dict[str, Any]) -> tuple[Any, Any, Any]:
    return (entry.get("scenarioName"), entry.get("timestamp"), entry.get("groupId"))


def append_entries(path: Path, new_entries: list[dict[str, Any]]) -> int:
    """Appends under an exclusive lock held for the whole read-modify-write,
    so two runs finishing close together serialize instead of racing.

    Also idempotent against an identical retry: a batch resubmitted verbatim
    (a retried CI step, a rerun after an interruption) must not double-append
    entries already present. Dedup is keyed on
    (scenarioName, timestamp, groupId) -- the same triple that uniquely
    identifies "this run's diagnosis of this scenario" -- so a genuinely new
    entry for the same scenario (a later run, a different timestamp) is never
    mistaken for a duplicate."""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.touch(exist_ok=True)
    with open(path, "r+", encoding="utf-8") as f:
        fcntl.flock(f.fileno(), fcntl.LOCK_EX)
        try:
            current = _load_locked(f)
            existing_keys = {_dedup_key(e) for e in current}
            to_add = [e for e in new_entries if _dedup_key(e) not in existing_keys]
            current.extend(to_add)
            f.seek(0)
            f.truncate()
            f.write(json.dumps(current, indent=2) + "\n")
            f.flush()
            return len(current)
        finally:
            fcntl.flock(f.fileno(), fcntl.LOCK_UN)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--fix-history", type=Path, default=DEFAULT_FIX_HISTORY)
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    raw = sys.stdin.read()
    try:
        entries = json.loads(raw)
    except json.JSONDecodeError as e:
        print(f"Invalid JSON on stdin: {e}")
        raise SystemExit(1)

    if not isinstance(entries, list) or not entries:
        print("Input must be a non-empty JSON array of fix-history entries.")
        raise SystemExit(1)

    problems: list[str] = []
    for i, entry in enumerate(entries):
        problems.extend(validate_entry(entry, i))
    if problems:
        for p in problems:
            print(p)
        raise SystemExit(1)

    total = append_entries(args.fix_history, entries)
    suffix = "y" if len(entries) == 1 else "ies"
    print(f"OK: appended {len(entries)} entr{suffix}, {total} total in {args.fix_history}")


if __name__ == "__main__":
    main()
