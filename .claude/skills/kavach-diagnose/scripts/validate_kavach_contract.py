#!/usr/bin/env python3
"""Validate a combined-receipts.json file against the kavach-verdict contract.

This is the deterministic gate kavach.yml runs after kavach-diagnose and
before ever considering a handoff to kavach-repair — the same role Netra's
validate-analysis.py plays for its own contract. It re-checks the
*shape* of the already-written combined-receipts.json document (produced by
validate_replay_receipts.py's combined_summary()/validate_manifest()); it
does not re-derive verdicts or re-run any analysis. Deliberately
dependency-free (no jsonschema package) so it can run anywhere Python 3 runs,
matching the style of the other failure-analyzer scripts in this directory.

The document shape validated here was traced directly from
validate_replay_receipts.py's actual return values, not from iFix.md's
Phase 3 worker-receipt spec — those are different shapes. A worker receipt
has productBugGate/productBugArtifacts booleans; this merged document
reduces that down to a plain-English gateProblems list per group, which is
what's actually checked here.

Usage: python3 validate_kavach_contract.py <combined-receipts.json>
Exit 0 and prints "OK: N group(s) valid" if the whole document conforms;
exit 1 and prints one line per problem otherwise.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Any

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

VALID_CONFIDENCE = {"high", "medium", "low"}

DOC_REQUIRED_KEYS = (
    "totalFailures",
    "groupCount",
    "summaryByGroup",
    "affectedScenariosByVerdict",
    "distinctIssuesByVerdict",
    "metrics",
    "groups",
)

GROUP_REQUIRED_KEYS = (
    "groupId",
    "scenarioCount",
    "verdict",
    "reportVerdict",
    "confidence",
    "gateProblems",
    "evidence",
    "analysisTier",
)

EVIDENCE_REQUIRED_VERDICTS = {
    "confirmed_product_bug",
    "suspected_product_bug",
    "not_reproduced_passed_live",
}


def _is_str_list(value: Any) -> bool:
    return isinstance(value, list) and all(isinstance(v, str) for v in value)


def validate_group(group: Any, index: int) -> list[str]:
    problems: list[str] = []
    if not isinstance(group, dict):
        return [f"groups[{index}]: expected an object, got {type(group).__name__}"]

    prefix = f"groups[{index}] (groupId={group.get('groupId')!r})"

    for key in GROUP_REQUIRED_KEYS:
        if key not in group:
            problems.append(f"{prefix}: missing required key {key!r}")

    verdict = group.get("verdict")
    if verdict is not None and verdict not in VALID_VERDICTS:
        problems.append(f"{prefix}: verdict {verdict!r} not one of {sorted(VALID_VERDICTS)}")

    tier = group.get("analysisTier")
    if tier is not None and tier not in VALID_TIERS:
        problems.append(f"{prefix}: analysisTier {tier!r} not one of {sorted(VALID_TIERS)}")

    confidence = group.get("confidence")
    if confidence is not None and confidence not in VALID_CONFIDENCE:
        problems.append(f"{prefix}: confidence {confidence!r} not one of {sorted(VALID_CONFIDENCE)}")

    scenario_count = group.get("scenarioCount")
    if scenario_count is not None and (not isinstance(scenario_count, int) or isinstance(scenario_count, bool) or scenario_count < 0):
        problems.append(f"{prefix}: scenarioCount must be a non-negative integer, got {scenario_count!r}")

    distinct_count = group.get("distinctIssueCount")
    if distinct_count is not None and (not isinstance(distinct_count, int) or isinstance(distinct_count, bool) or distinct_count < 1):
        problems.append(f"{prefix}: distinctIssueCount must be a positive integer, got {distinct_count!r}")

    gate_problems = group.get("gateProblems")
    if gate_problems is not None and not _is_str_list(gate_problems):
        problems.append(f"{prefix}: gateProblems must be a list of strings")

    evidence = group.get("evidence")
    if evidence is not None and not _is_str_list(evidence):
        problems.append(f"{prefix}: evidence must be a list of strings")
    elif verdict in EVIDENCE_REQUIRED_VERDICTS and not evidence:
        problems.append(f"{prefix}: evidence must be non-empty for verdict {verdict!r}")

    if verdict == "confirmed_product_bug" and gate_problems:
        problems.append(
            f"{prefix}: verdict is confirmed_product_bug but gateProblems is non-empty "
            f"({gate_problems!r}) — validate_receipt() can only produce this verdict with "
            f"zero gate problems; a non-empty list here means this document was hand-edited "
            f"or the producer's own invariant was violated"
        )

    return problems


def validate_document(data: Any) -> list[str]:
    if not isinstance(data, dict):
        return [f"expected a JSON object (document), got {type(data).__name__}"]

    problems: list[str] = []
    for key in DOC_REQUIRED_KEYS:
        if key not in data:
            problems.append(f"missing required top-level key {key!r}")

    groups = data.get("groups")
    if not isinstance(groups, list):
        problems.append(f"'groups' must be a JSON array, got {type(groups).__name__}")
        return problems
    if len(groups) == 0:
        problems.append("'groups' must be non-empty — a document with zero groups means nothing was diagnosed")
        return problems

    for i, group in enumerate(groups):
        problems.extend(validate_group(group, i))

    group_count = data.get("groupCount")
    if isinstance(group_count, int) and group_count != len(groups):
        problems.append(f"groupCount ({group_count}) does not match len(groups) ({len(groups)})")

    # Recompute summaryByGroup / affectedScenariosByVerdict / distinctIssuesByVerdict
    # from the groups themselves and compare — catches a document where the
    # top-level totals were hand-edited independently of the groups array.
    valid_groups = [g for g in groups if isinstance(g, dict)]
    recomputed_summary: dict[str, int] = {}
    recomputed_affected: dict[str, int] = {}
    recomputed_distinct: dict[str, int] = {}
    for g in valid_groups:
        v = g.get("verdict")
        if v not in VALID_VERDICTS:
            continue
        recomputed_summary[v] = recomputed_summary.get(v, 0) + 1
        recomputed_affected[v] = recomputed_affected.get(v, 0) + int(g.get("scenarioCount") or 0)
        recomputed_distinct[v] = recomputed_distinct.get(v, 0) + int(g.get("distinctIssueCount") or 1)

    for label, recomputed, actual_key in (
        ("summaryByGroup", recomputed_summary, "summaryByGroup"),
        ("affectedScenariosByVerdict", recomputed_affected, "affectedScenariosByVerdict"),
        ("distinctIssuesByVerdict", recomputed_distinct, "distinctIssuesByVerdict"),
    ):
        actual = data.get(actual_key)
        if isinstance(actual, dict) and actual != recomputed:
            problems.append(
                f"{label} ({actual}) does not match what the groups array actually contains ({recomputed})"
            )

    metrics = data.get("metrics")
    if isinstance(metrics, dict):
        total_groups = metrics.get("totalGroups")
        if isinstance(total_groups, int) and total_groups != len(groups):
            problems.append(f"metrics.totalGroups ({total_groups}) does not match len(groups) ({len(groups)})")

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

    problems = validate_document(data)

    if problems:
        print(f"FAIL: {len(problems)} problem(s) in {path}")
        for p in problems:
            print(f"  - {p}")
        return 1

    group_count = len(data.get("groups", []))
    print(f"OK: {group_count} group(s) valid in {path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
