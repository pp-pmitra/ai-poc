#!/usr/bin/env python3
"""Human-driven calibration loop for the failure-analyzer's verdicts.

Nothing here changes any verdict — it's a separate, human-in-the-loop step
for spot-checking whether the analyzer actually got it right, so "80%
correct" and "product bugs are really product bugs" are measured claims
instead of assumed ones.

Usage:
    # After reviewing a verdict yourself (checked the code, or asked a dev):
    python3 review_verdicts.py mark "<exact scenarioName>" agreed
    python3 review_verdicts.py mark "<exact scenarioName>" disagreed --note "stale locator, not a bug"

    # See where things stand:
    python3 review_verdicts.py report
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Optional

DEFAULT_FIX_HISTORY = Path(__file__).resolve().parent / "history/fix-history.json"

_PRODUCT_BUG_VERDICTS = {"Product Bug — Confirmed", "Product Bug — Suspected"}
_CONFIRMED_PRODUCT_BUG_VERDICT = "Product Bug — Confirmed"
_VALID_STATUSES = {"agreed", "disagreed"}


def find_latest_entry(fix_history: list[dict[str, Any]], scenario_name: str) -> Optional[dict[str, Any]]:
    """Most recent fix-history entry for this exact scenario name, or None."""
    candidates = [e for e in fix_history if e.get("scenarioName") == scenario_name]
    if not candidates:
        return None
    return max(candidates, key=lambda e: e.get("timestamp") or "")


def mark_review(entry: dict[str, Any], status: str, note: Optional[str]) -> dict[str, Any]:
    if status not in _VALID_STATUSES:
        raise ValueError(f"status must be one of {sorted(_VALID_STATUSES)}, got {status!r}")
    entry["review"] = {
        "status": status,
        "reviewedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "note": note,
    }
    return entry


def compute_report(fix_history: list[dict[str, Any]]) -> dict[str, Any]:
    def review_status(entry: dict[str, Any]) -> Optional[str]:
        status = (entry.get("review") or {}).get("status")
        return status if status in _VALID_STATUSES else None

    reviewed = [e for e in fix_history if review_status(e) is not None]

    def rate(entries: list[dict[str, Any]]) -> Optional[float]:
        if not entries:
            return None
        agreed = sum(1 for e in entries if review_status(e) == "agreed")
        return agreed / len(entries)

    product_bug_reviewed = [e for e in reviewed if e.get("verdict") in _PRODUCT_BUG_VERDICTS]
    confirmed_reviewed = [e for e in reviewed if e.get("verdict") == _CONFIRMED_PRODUCT_BUG_VERDICT]

    disagreed_entries = [
        {
            "scenarioName": e.get("scenarioName"),
            "verdict": e.get("verdict"),
            "timestamp": e.get("timestamp"),
            "note": (e.get("review") or {}).get("note"),
        }
        for e in reviewed
        if review_status(e) == "disagreed"
    ]

    return {
        "totalEntries": len(fix_history),
        "reviewedCount": len(reviewed),
        "unreviewedCount": len(fix_history) - len(reviewed),
        "overallAgreementRate": rate(reviewed),
        "productBugReviewedCount": len(product_bug_reviewed),
        "productBugAgreementRate": rate(product_bug_reviewed),
        "confirmedProductBugReviewedCount": len(confirmed_reviewed),
        "confirmedProductBugAgreementRate": rate(confirmed_reviewed),
        "disagreedEntries": disagreed_entries,
    }


def _load(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        return []
    return json.loads(path.read_text(encoding="utf-8"))


def _save(path: Path, fix_history: list[dict[str, Any]]) -> None:
    path.write_text(json.dumps(fix_history, indent=2) + "\n", encoding="utf-8")


def _format_rate(rate: Optional[float]) -> str:
    return "n/a (none reviewed yet)" if rate is None else f"{rate:.0%}"


def cmd_mark(args: argparse.Namespace) -> None:
    fix_history = _load(args.fix_history)
    entry = find_latest_entry(fix_history, args.scenario_name)
    if entry is None:
        print(f"No fix-history entry found for scenario: {args.scenario_name!r}")
        raise SystemExit(1)
    mark_review(entry, args.status, args.note)
    _save(args.fix_history, fix_history)
    print(f"Marked {args.scenario_name!r} (verdict: {entry.get('verdict')}) as {args.status!r}.")


def cmd_report(args: argparse.Namespace) -> None:
    fix_history = _load(args.fix_history)
    report = compute_report(fix_history)

    print(f"Total fix-history entries: {report['totalEntries']}")
    print(f"Reviewed: {report['reviewedCount']} ({report['unreviewedCount']} unreviewed)")
    print(f"Overall agreement rate: {_format_rate(report['overallAgreementRate'])}")
    print(
        f"Product-bug (Confirmed + Suspected) agreement rate: "
        f"{_format_rate(report['productBugAgreementRate'])} "
        f"({report['productBugReviewedCount']} reviewed)"
    )
    print(
        f"Product Bug — Confirmed only, agreement rate: "
        f"{_format_rate(report['confirmedProductBugAgreementRate'])} "
        f"({report['confirmedProductBugReviewedCount']} reviewed)"
    )
    if report["disagreedEntries"]:
        print("\nDisagreed entries:")
        for e in report["disagreedEntries"]:
            note = f" — {e['note']}" if e["note"] else ""
            print(f"  [{e['verdict']}] {e['scenarioName']}{note}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--fix-history", type=Path, default=DEFAULT_FIX_HISTORY)
    sub = parser.add_subparsers(dest="command", required=True)

    mark_parser = sub.add_parser("mark", help="mark the latest fix-history entry for a scenario as agreed/disagreed")
    mark_parser.add_argument("scenario_name", help="exact scenarioName to mark")
    mark_parser.add_argument("status", choices=sorted(_VALID_STATUSES))
    mark_parser.add_argument("--note", default=None)
    mark_parser.set_defaults(func=cmd_mark)

    report_parser = sub.add_parser("report", help="print calibration stats from reviewed entries")
    report_parser.set_defaults(func=cmd_report)

    return parser.parse_args()


def main() -> None:
    args = parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
