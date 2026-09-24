#!/usr/bin/env python3
"""Validate a combined-receipts.json file against the kavach-verdict contract.

This is the deterministic gate kavach-orchestrator runs after kavach-diagnose
and before ever considering a handoff to kavach-imaintain — the same role
Netra's validate-analysis.py plays for its own contract. It re-checks the
*shape* of the already-written combined-receipts.json (produced by
validate_replay_receipts.py); it does not re-derive verdicts or re-run any
analysis. Deliberately dependency-free (no jsonschema package) so it can run
anywhere Python 3 runs, matching the style of the other failure-analyzer
scripts in this directory.

Usage: python3 validate_kavach_contract.py <combined-receipts.json>
Exit 0 and prints "OK: N rows valid" if every row conforms; exit 1 and prints
one line per problem otherwise.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

VALID_VERDICTS = {
    "script_issue_fix_proposed",
    "confirmed_product_bug",
    "suspected_product_bug",
    "not_reproduced_intermittent",
    "not_reproduced_passed_live",
    "needs_investigation",
}

VALID_TIERS = {
    "tier0_intermittent",
    "fix_pattern_cache",
    "tier1_deterministic",
    "tier1_llm_static",
    "tier2_offline_dom",
    "tier2_live_replay",
}

REQUIRED_KEYS = ("groupId", "verdict", "reportVerdict", "analysisTier", "evidence")

CONFIRMED_BUG_GATE_KEYS = (
    "userLevelBehaviorReproduced",
    "targetAffordanceMissingOrBroken",
    "domStructureChecked",
    "staleLocatorRuledOut",
    "testDataOrEnvironmentRuledOut",
)

ARTIFACT_KEYS = ("domStructureChecked", "staleLocatorRuledOut")
MIN_ARTIFACT_LEN = 20


def validate_row(row: dict, index: int) -> list[str]:
    problems = []
    prefix = f"row[{index}] (groupId={row.get('groupId')!r})"

    for key in REQUIRED_KEYS:
        if key not in row:
            problems.append(f"{prefix}: missing required key {key!r}")

    verdict = row.get("verdict")
    if verdict is not None and verdict not in VALID_VERDICTS:
        problems.append(f"{prefix}: verdict {verdict!r} not one of {sorted(VALID_VERDICTS)}")

    tier = row.get("analysisTier")
    if tier is not None and tier not in VALID_TIERS:
        problems.append(f"{prefix}: analysisTier {tier!r} not one of {sorted(VALID_TIERS)}")

    evidence = row.get("evidence")
    if evidence is not None and (not isinstance(evidence, list) or len(evidence) == 0):
        if verdict in ("confirmed_product_bug", "suspected_product_bug", "not_reproduced_passed_live"):
            problems.append(f"{prefix}: evidence must be a non-empty list for verdict {verdict!r}")

    if verdict == "confirmed_product_bug":
        for key in CONFIRMED_BUG_GATE_KEYS:
            if key not in row:
                problems.append(f"{prefix}: confirmed_product_bug requires gate key {key!r}")
        for key in ARTIFACT_KEYS:
            value = row.get(key)
            if isinstance(value, str) and len(value.strip()) < MIN_ARTIFACT_LEN:
                problems.append(
                    f"{prefix}: {key!r} looks like a placeholder ({value!r}), "
                    f"needs a real DOM excerpt or count()/evaluate() output"
                )

    return problems


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: python3 validate_kavach_contract.py <combined-receipts.json>")
        return 1

    path = Path(sys.argv[1])
    if not path.exists():
        print(f"FAIL: {path} does not exist")
        return 1

    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        print(f"FAIL: {path} is not valid JSON: {e}")
        return 1

    rows = data if isinstance(data, list) else data.get("rows", data.get("receipts"))
    if not isinstance(rows, list):
        print(f"FAIL: expected a JSON array of receipt rows (or a {{'rows': [...]}} object), got {type(data).__name__}")
        return 1

    problems: list[str] = []
    for i, row in enumerate(rows):
        if not isinstance(row, dict):
            problems.append(f"row[{i}]: expected an object, got {type(row).__name__}")
            continue
        problems.extend(validate_row(row, i))

    if problems:
        print(f"FAIL: {len(problems)} problem(s) in {path}")
        for p in problems:
            print(f"  - {p}")
        return 1

    print(f"OK: {len(rows)} row(s) valid in {path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
