#!/usr/bin/env python3
"""Validate live-replay worker receipts and enforce the product-bug gate."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any

_ANALYZER_DIR = Path(__file__).resolve().parent
_HISTORY_DIR = _ANALYZER_DIR.parents[3] / "kavach-data" / "history"

REQUIRED_GATE_KEYS = (
    "userLevelBehaviorReproduced",
    "targetAffordanceMissingOrBroken",
    "domStructureChecked",
    "staleLocatorRuledOut",
    "testDataOrEnvironmentRuledOut",
)

# For `confirmed_product_bug` specifically, a boolean gate claim alone is
# self-reported by the same worker session that did the replay — nothing
# independently re-checks it was actually done rather than rushed/eyeballed.
# These three keys are the most concrete/checkable ones (a real DOM excerpt, a
# real count()/evaluate() output, or a named test-data value actually searched
# for), so require the *raw* observed text behind the claim, not just `true`,
# before letting "Critical" stand unchallenged. The other two gate keys
# (userLevelBehaviorReproduced, targetAffordanceMissingOrBroken) are covered
# indirectly by the receipt-level `evidence` non-empty check below, since a
# real user-level reproduction is expected to show up there too.
REQUIRED_ARTIFACT_KEYS = ("domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut")
MIN_ARTIFACT_LEN = 20
_PLACEHOLDER_ARTIFACT_RE = re.compile(r"(?i)^(true|false|yes|no|checked|verified|done|n/?a|none|ok)\.?$")
# A real DOM excerpt or count()/evaluate() output has at least one of these —
# a fabricated prose sentence describing the check ("the element was missing
# after a thorough check") passes the length/placeholder checks above but has
# none of these, so this closes that gap without claiming to prove authenticity.
_ARTIFACT_SIGNAL_RE = re.compile(r"[<>]|\d|outerHTML|\.count\(|\.evaluate\(|locator\(")
# testDataOrEnvironmentRuledOut isn't a DOM/locator check — a real one names
# the specific value searched for and what was found (e.g. "searched
# 'AutoSegment747695', 0 rows returned" or "environment=Demo confirmed via
# GET /api/config -> {\"env\":\"demo\"}"), so a digit or a quoted literal is
# the corresponding real-artifact signal for this key, not the DOM/tool-call
# syntax the other two keys look for.
_ENV_ARTIFACT_SIGNAL_RE = re.compile(r"\d|['\"][^'\"]+['\"]")
_ARTIFACT_SIGNAL_RE_BY_KEY = {
    "domStructureChecked": _ARTIFACT_SIGNAL_RE,
    "staleLocatorRuledOut": _ARTIFACT_SIGNAL_RE,
    "testDataOrEnvironmentRuledOut": _ENV_ARTIFACT_SIGNAL_RE,
}

REPORT_VERDICT = {
    "script_issue_fix_proposed": "Script Issue — Fix Proposed",
    "confirmed_product_bug": "Product Bug — Confirmed",
    "suspected_product_bug": "Product Bug — Suspected",
    "not_reproduced_intermittent": "Not Reproduced — Intermittent",
    "not_reproduced_passed_live": "Not Reproduced — Passed Live Replay",
    "needs_investigation": "Needs Investigation",
}


def latest_manifest(root: Path) -> Path:
    manifests = sorted(root.glob("*/manifest.json"), key=lambda p: p.stat().st_mtime, reverse=True)
    if not manifests:
        raise FileNotFoundError(f"No replay-packets manifest found under {root}")
    return manifests[0]


def parse_json_receipt(path: Path) -> dict[str, Any]:
    text = path.read_text(encoding="utf-8", errors="replace").strip()
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        match = re.search(r"\{[\s\S]*\}", text)
        if not match:
            raise
        return json.loads(match.group(0))


def _artifact_problems(receipt: dict[str, Any]) -> list[str]:
    artifacts = receipt.get("productBugArtifacts")
    if not isinstance(artifacts, dict):
        artifacts = {}
    problems: list[str] = []
    for key in REQUIRED_ARTIFACT_KEYS:
        value = artifacts.get(key)
        if not isinstance(value, str):
            problems.append(f"productBugArtifacts.{key} missing or not a string")
            continue
        stripped = value.strip()
        signal_re = _ARTIFACT_SIGNAL_RE_BY_KEY[key]
        if len(stripped) < MIN_ARTIFACT_LEN or _PLACEHOLDER_ARTIFACT_RE.match(stripped):
            problems.append(
                f"productBugArtifacts.{key} missing or not a real artifact — must be the actual observed "
                f"DOM excerpt / count() output / searched value, not a boolean restated as text"
            )
        elif not signal_re.search(stripped):
            problems.append(
                f"productBugArtifacts.{key} doesn't look like a real observed artifact "
                f"(no tag, digit, quoted value, or tool-call signal found) — looks like restated prose, not observed output"
            )
    return problems


def validate_receipt(receipt: dict[str, Any]) -> tuple[str, list[str]]:
    verdict = str(receipt.get("verdict") or "needs_investigation").strip().lower()
    problems: list[str] = []

    if verdict == "confirmed_product_bug":
        if receipt.get("backgroundMatched") is not True:
            problems.append("backgroundMatched was not true")
        if receipt.get("liveReplayPerformed") is not True:
            problems.append("liveReplayPerformed was not true")
        if receipt.get("alternateValidPathFound") is True:
            problems.append("alternateValidPathFound was true")
        gate = receipt.get("productBugGate") or {}
        for key in REQUIRED_GATE_KEYS:
            if gate.get(key) is not True:
                problems.append(f"productBugGate.{key} was not true")
        if not receipt.get("evidence"):
            problems.append("evidence was empty")
        problems.extend(_artifact_problems(receipt))
        if problems:
            return "suspected_product_bug", problems

    # Lighter bar than confirmed_product_bug (partial verification is exactly
    # what these two verdicts already mean), but still requires proof a live
    # replay actually happened — without this, nothing previously stopped a
    # `suspected_product_bug`/`not_reproduced_passed_live` receipt with zero
    # supporting fields from passing through unchecked.
    elif verdict in ("suspected_product_bug", "not_reproduced_passed_live"):
        if receipt.get("liveReplayPerformed") is not True:
            problems.append("liveReplayPerformed was not true")
        if not receipt.get("evidence"):
            problems.append("evidence was empty")
        if problems:
            return "needs_investigation", problems

    # script_issue_fix_proposed is the verdict that most directly leads to a
    # real code change via kavach-repair, yet previously had no gate at all.
    # Unlike the product-bug verdicts above, it can legitimately come from a
    # no-browser Tier-1 pass (deterministic classifier / text-only LLM), so
    # this deliberately does NOT require liveReplayPerformed the way
    # suspected_product_bug does — every real producer (disposition(),
    # enforce_tier1_verdict_constraints(), and the live-replay worker's own
    # RECEIPT_SCHEMA) always populates `evidence` regardless of tier, so
    # requiring it here catches a hand-edited or bare-minimum fabricated
    # receipt without rejecting genuine static-tier resolutions.
    elif verdict == "script_issue_fix_proposed":
        if not receipt.get("evidence"):
            problems.append("evidence was empty")
        if problems:
            return "needs_investigation", problems

    if verdict not in REPORT_VERDICT:
        return "needs_investigation", [f"unknown verdict {verdict!r}"]
    return verdict, []


def validate_manifest(manifest_path: Path) -> dict[str, Any]:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    base_dir = manifest_path.parent
    receipts_dir = base_dir / "receipts"
    rows: list[dict[str, Any]] = []

    for group in manifest.get("groups", []):
        prompt = Path(group["prompt"])
        receipt_path = receipts_dir / (prompt.stem.replace(".prompt", "") + ".receipt.json")
        if not receipt_path.exists():
            rows.append({
                "groupId": group.get("groupId"),
                "representativeScenario": group.get("representativeScenario"),
                "scenarioCount": group.get("scenarioCount", 0),
                "receiptPath": str(receipt_path),
                "verdict": "needs_investigation",
                "reportVerdict": REPORT_VERDICT["needs_investigation"],
                "confidence": "low",
                "gateProblems": ["receipt file missing"],
                "evidence": [],
            })
            continue

        receipt = parse_json_receipt(receipt_path)
        original_verdict = str(receipt.get("verdict") or "needs_investigation").strip().lower()
        verdict, problems = validate_receipt(receipt)
        rows.append({
            "groupId": group.get("groupId"),
            "representativeScenario": receipt.get("representativeScenario") or group.get("representativeScenario"),
            "affectedScenarios": receipt.get("affectedScenarios") or [],
            "scenarioCount": group.get("scenarioCount", 0),
            "distinctIssueCount": receipt.get("distinctIssueCount", 1),
            "receiptPath": str(receipt_path),
            "verdict": verdict,
            "originalVerdict": original_verdict,
            "reportVerdict": REPORT_VERDICT[verdict],
            "confidence": str(receipt.get("confidence") or "low").lower(),
            "gateProblems": problems,
            "evidence": receipt.get("evidence") or [],
            "recommendedAction": receipt.get("recommendedAction") or "",
            "blockedReason": receipt.get("blockedReason"),
        })

    summary: dict[str, int] = {}
    affected_by_verdict: dict[str, int] = {}
    distinct_by_verdict: dict[str, int] = {}
    false_product_bug_downgrades = 0
    for row in rows:
        verdict = row["verdict"]
        summary[verdict] = summary.get(verdict, 0) + 1
        affected_by_verdict[verdict] = affected_by_verdict.get(verdict, 0) + int(row.get("scenarioCount") or 0)
        distinct_by_verdict[verdict] = distinct_by_verdict.get(verdict, 0) + int(row.get("distinctIssueCount") or 1)
        original = row.get("originalVerdict")
        if original in ("confirmed_product_bug", "suspected_product_bug") and verdict != original:
            false_product_bug_downgrades += 1

    return {
        "manifest": str(manifest_path),
        "totalFailures": manifest.get("totalFailures", 0),
        "groupCount": len(rows),
        "summaryByGroup": summary,
        "affectedScenariosByVerdict": affected_by_verdict,
        "distinctIssuesByVerdict": distinct_by_verdict,
        "metrics": {
            "totalGroups": len(rows),
            "confirmedProductBugGroups": summary.get("confirmed_product_bug", 0),
            "suspectedProductBugGroups": summary.get("suspected_product_bug", 0),
            "falseProductBugDowngrades": false_product_bug_downgrades,
        },
        "groups": rows,
    }


def combined_summary(
    triage_manifest_path: Path,
    replay_manifest_path: Path | None,
    blocked_reason: str | None = None,
) -> dict[str, Any]:
    """Merge Phase 2.5's already-resolved receipts with Phase 3's validated
    live-replay receipts into ONE consolidated, completeness-checked row set
    — the mechanical backing for Phase 4's "union both sources" step, so that
    isn't manual LLM file-merging every run.

    Every Tier-1 (Phase 2.5) receipt is re-validated through the same
    `validate_receipt()` gate as Tier-2 receipts here — Tier 1 already can't
    produce a forbidden verdict (`enforce_tier1_verdict_constraints`), but
    this makes the "independently re-checked by validate_replay_receipts.py"
    guarantee literally true for Phase 2.5 output too, not just Phase 3's.

    Completeness is driven by the triage manifest's own `groups` list, which
    already records every group this run saw (resolved or escalated) — no
    separate reconciliation against `failures-for-replay.json` is needed.
    """
    triage_manifest = json.loads(triage_manifest_path.read_text(encoding="utf-8"))

    replay_rows_by_group: dict[str, dict[str, Any]] = {}
    if replay_manifest_path and replay_manifest_path.exists():
        replay_result = validate_manifest(replay_manifest_path)
        replay_rows_by_group = {
            row["groupId"]: {**row, "analysisTier": "tier2_live_replay"}
            for row in replay_result["groups"]
        }

    rows: list[dict[str, Any]] = []
    for group in triage_manifest.get("groups", []):
        group_id = group.get("groupId")

        if group.get("resolved") and group.get("receiptPath"):
            receipt = parse_json_receipt(Path(group["receiptPath"]))
            original_verdict = str(receipt.get("verdict") or "needs_investigation").strip().lower()
            verdict, problems = validate_receipt(receipt)
            rows.append({
                "groupId": group_id,
                "representativeScenario": receipt.get("representativeScenario") or group.get("representativeScenario"),
                "affectedScenarios": receipt.get("affectedScenarios") or [],
                "scenarioCount": group.get("scenarioCount", 0),
                "distinctIssueCount": receipt.get("distinctIssueCount", 1),
                "receiptPath": group["receiptPath"],
                "verdict": verdict,
                "originalVerdict": original_verdict,
                "reportVerdict": REPORT_VERDICT[verdict],
                "confidence": str(receipt.get("confidence") or "low").lower(),
                "gateProblems": problems,
                "evidence": receipt.get("evidence") or [],
                "recommendedAction": receipt.get("recommendedAction") or "",
                "analysisTier": receipt.get("analysisTier"),
            })
            continue

        row = replay_rows_by_group.get(group_id)
        if row is None:
            # Escalated to Phase 3 but no matching receipt found there (e.g. a
            # worker crashed before writing one) -- never drop the group.
            row = {
                "groupId": group_id,
                "representativeScenario": group.get("representativeScenario"),
                "scenarioCount": group.get("scenarioCount", 0),
                "receiptPath": None,
                "verdict": "needs_investigation",
                "reportVerdict": REPORT_VERDICT["needs_investigation"],
                "confidence": "low",
                "gateProblems": ["escalated to Phase 3 but no matching replay-packets receipt found"],
                "evidence": [],
                "analysisTier": "tier2_live_replay",
            }
        rows.append(row)

    if blocked_reason:
        for row in rows:
            missing = any(
                "receipt" in str(problem) and ("missing" in str(problem) or "no matching" in str(problem))
                for problem in row.get("gateProblems") or []
            )
            if row.get("analysisTier") == "tier2_live_replay" and missing:
                row["blockedReason"] = blocked_reason

    summary: dict[str, int] = {}
    affected_by_verdict: dict[str, int] = {}
    distinct_by_verdict: dict[str, int] = {}
    false_product_bug_downgrades = 0
    for row in rows:
        verdict = row["verdict"]
        summary[verdict] = summary.get(verdict, 0) + 1
        affected_by_verdict[verdict] = affected_by_verdict.get(verdict, 0) + int(row.get("scenarioCount") or 0)
        distinct_by_verdict[verdict] = distinct_by_verdict.get(verdict, 0) + int(row.get("distinctIssueCount") or 1)
        original = row.get("originalVerdict")
        if original in ("confirmed_product_bug", "suspected_product_bug") and verdict != original:
            false_product_bug_downgrades += 1

    return {
        "triageManifest": str(triage_manifest_path),
        "replayManifest": str(replay_manifest_path) if replay_manifest_path else None,
        "totalFailures": sum(int(row.get("scenarioCount") or 0) for row in rows),
        "groupCount": len(rows),
        "summaryByGroup": summary,
        "affectedScenariosByVerdict": affected_by_verdict,
        "distinctIssuesByVerdict": distinct_by_verdict,
        "metrics": {
            "totalGroups": len(rows),
            "staticResolvedGroups": sum(1 for row in rows if row.get("analysisTier") != "tier2_live_replay"),
            "replayEscalatedGroups": sum(1 for row in rows if row.get("analysisTier") == "tier2_live_replay"),
            "confirmedProductBugGroups": summary.get("confirmed_product_bug", 0),
            "suspectedProductBugGroups": summary.get("suspected_product_bug", 0),
            "falseProductBugDowngrades": false_product_bug_downgrades,
        },
        "groups": rows,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", nargs="?", type=Path, help="Phase 3 replay-packets manifest.json (omit if every group resolved in Phase 2.5)")
    parser.add_argument("--root", type=Path, default=_HISTORY_DIR / "replay-packets")
    parser.add_argument(
        "--triage-manifest", type=Path, default=None,
        help="Phase 2.5's triage-results manifest.json -- when given, produces ONE combined, "
             "completeness-checked summary spanning both phases instead of validating Phase 3 alone",
    )
    parser.add_argument(
        "--blocked-reason", default=None,
        help="Why Phase 3 live replay could not complete (e.g. 'CDP_ENDPOINT_DEAD: 9223 -- ...'). "
             "Recorded as blockedReason on every escalated group that has no receipt, so the verdict "
             "report can list it instead of the run ending without a report",
    )
    parser.add_argument("-o", "--out", type=Path, default=None, help="output path (default: alongside --triage-manifest as combined-receipts.json)")
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    if args.triage_manifest:
        replay_manifest = args.manifest
        if replay_manifest is None and args.root.exists():
            try:
                replay_manifest = latest_manifest(args.root)
            except FileNotFoundError:
                replay_manifest = None
        result = combined_summary(args.triage_manifest, replay_manifest, args.blocked_reason)
        out_path = args.out or (args.triage_manifest.parent / "combined-receipts.json")
        out_path.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"Wrote combined receipt summary: {out_path}")
        print(json.dumps({
            "groups": result["summaryByGroup"],
            "affectedScenarios": result["affectedScenariosByVerdict"],
            "distinctIssues": result["distinctIssuesByVerdict"],
        }, indent=2, sort_keys=True))
        return

    manifest = args.manifest or latest_manifest(args.root)
    result = validate_manifest(manifest)
    out_path = manifest.parent / "receipt-summary.json"
    out_path.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Wrote receipt summary: {out_path}")
    print(json.dumps({
        "groups": result["summaryByGroup"],
        "affectedScenarios": result["affectedScenariosByVerdict"],
        "distinctIssues": result["distinctIssuesByVerdict"],
    }, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
