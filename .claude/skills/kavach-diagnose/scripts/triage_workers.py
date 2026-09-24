#!/usr/bin/env python3
"""Cheap, no-browser triage tier — runs between `list_failures.py`'s
mechanical grouping and `replay_workers.py`'s live-replay packet building.

For every group NOT already flagged `allFailuresLikelyIntermittent`, tries,
in order:
  1. Fix-pattern cache: a prior run's still-present, verified fix.
  2. Deterministic classifier (`failure_analyzer.analyzers.assertion_analyzer`).
  3. A batched, text-only LLM fallback for whatever's left with cause "unknown";
     any item that still comes back `confidence: "low"` gets one more single-item
     retry with its failure-moment screenshot attached (if one exists) before
     giving up and escalating — still no live browser access at any point.

Writes the same receipt/manifest shape `replay_workers.py`'s live-replay run
produces (so `validate_replay_receipts.py` and the verdict-report aggregation
work unmodified), plus `escalate-groups.json` listing the groupIds Tier 2
(live Playwright-MCP replay) still needs to handle.

No live browser is ever touched by this script. Run in SHADOW MODE first —
i.e. just inspect this script's output against known-good live-replay runs —
before trusting the `failure-triage` skill's Phase 2.5 to actually skip live
replay for the groups this script resolves.
"""

from __future__ import annotations

import argparse
import asyncio
import json
from datetime import datetime
from pathlib import Path
from typing import Any

from failure_analyzer.config import load_config
from failure_analyzer.llm.connector_factory import create_connector
from failure_analyzer.packet_utils import all_likely_intermittent, select_representative
from failure_analyzer.triage.fix_pattern_cache import find_cached_fix, verify_fix_still_present
from failure_analyzer.triage.llm_static_triage import (
    build_tier1_prompt,
    enforce_tier1_verdict_constraints,
    triage_with_llm,
)
from failure_analyzer.analyzers.offline_dom_analyzer import analyze_offline
from failure_analyzer.triage.static_classifier import classify, disposition
from replay_workers import group_failures

_ANALYZER_DIR = Path(__file__).resolve().parent
_HISTORY_DIR = _ANALYZER_DIR.parents[3] / "kavach-data" / "history"
_REPO_ROOT = _ANALYZER_DIR.parent

DEFAULT_INPUT = _HISTORY_DIR / "failures-for-replay.json"
DEFAULT_FIX_HISTORY = _HISTORY_DIR / "fix-history.json"
DEFAULT_OUT_ROOT = _HISTORY_DIR / "triage-results"
DEFAULT_BATCH_SIZE = 6

REPORT_VERDICT = {
    "script_issue_fix_proposed": "Script Issue — Fix Proposed",
    "not_reproduced_intermittent": "Not Reproduced — Intermittent",
    "needs_investigation": "Needs Investigation",
}


def _estimated_tokens(text: str) -> int:
    # Rough model-agnostic accounting for run metrics; exact provider billing is
    # intentionally not coupled to this local analyser.
    return max(1, (len(text) + 3) // 4)


def _empty_gate() -> dict[str, bool]:
    return {
        "userLevelBehaviorReproduced": False,
        "targetAffordanceMissingOrBroken": False,
        "domStructureChecked": False,
        "staleLocatorRuledOut": False,
        "testDataOrEnvironmentRuledOut": False,
    }


def _build_receipt(
    group: dict[str, Any],
    representative: dict[str, Any],
    analysis_tier: str,
    verdict: str,
    confidence: str,
    evidence: list[str],
    recommended_action: str,
    proposed_change: dict[str, Any] | None,
    used_screenshot: bool = False,
) -> dict[str, Any]:
    return {
        "groupId": group["groupId"],
        "representativeScenario": representative.get("scenarioName"),
        "affectedScenarios": [f.get("scenarioName") for f in group["failures"]],
        "distinctIssueCount": 1,
        "verdict": verdict,
        "confidence": confidence,
        "backgroundMatched": False,
        "liveReplayPerformed": False,
        "alternateValidPathFound": False,
        "productBugGate": _empty_gate(),
        "evidence": evidence,
        "recommendedAction": recommended_action,
        "analysisTier": analysis_tier,
        "needsLiveReplay": False,
        "proposedChange": proposed_change,
        "usedScreenshot": used_screenshot,
    }


def _cache_lookup(
    representative: dict[str, Any],
    group: dict[str, Any],
    fix_history: list[dict[str, Any]],
    repo_root: Path,
) -> dict[str, Any] | None:
    cached = find_cached_fix(representative.get("scenarioName"), group["groupId"], fix_history)
    if cached is None:
        return None
    if not verify_fix_still_present(cached["change"], repo_root):
        return None
    return _build_receipt(
        group,
        representative,
        analysis_tier="fix_pattern_cache",
        verdict="script_issue_fix_proposed",
        confidence=str(cached.get("confidence") or "medium").lower(),
        evidence=[
            f"Reusing prior fix from {cached.get('timestamp')}: {cached['change'].get('description', '')}",
            f"Verified `{cached['change'].get('after')}` is still present at {cached['change'].get('file')}:{cached['change'].get('line')}.",
        ],
        recommended_action="Fix already known from a prior run and still present in source — no re-diagnosis needed.",
        proposed_change=cached["change"],
    )


def _frequency_by_scenario(
    fix_history: list[dict[str, Any]],
    failures: list[dict[str, Any]],
) -> dict[str, dict[str, Any]]:
    """Compact per-scenario frequency signal for `analyze_failure`'s history
    context (pattern/count/total/lastPassed), derived from fix-history.json —
    NOT `failure_analyzer.reporters.history_writer.get_scenario_run_pattern`,
    which is richer but reads from `failure-history.jsonl`, a separate
    rolling log this pipeline's live Phase 5 never writes to (confirmed: no
    caller of `record_history.py`/`append_to_history` exists in the current
    kavach-diagnose pipeline) — reusing it here would silently regress to
    zero signal for every scenario instead of the real data fix-history.json
    already has.

    Scoped by BOTH scenarioName AND groupId (the same exceptionType|step
    signature `failure_grouper.py` already computes) so an unrelated past
    failure on a same-named scenario isn't miscounted as "recurring" —
    mirroring `get_scenario_run_pattern`'s signature-scoping using data this
    pipeline actually populates.

    `fix-history.json` only ever logs scenarios that FAILED in Cucumber that
    run (Phase 5 never records a pass), so "regression"/"intermittent"
    (which need real pass data) can't be derived honestly from this source —
    only "recurring" (repeat failures under the same signature) is set;
    anything with fewer than 2 signature-scoped priors is left unset so
    `analyze_failure()` falls through to its own "first failure" framing
    instead of a fabricated pattern.
    """
    group_id_by_scenario = {f.get("scenarioName"): f.get("groupId") for f in failures}

    by_scenario: dict[str, list[dict[str, Any]]] = {}
    for entry in fix_history:
        name = entry.get("scenarioName")
        if name:
            by_scenario.setdefault(name, []).append(entry)

    frequency: dict[str, dict[str, Any]] = {}
    for name, entries in by_scenario.items():
        current_group_id = group_id_by_scenario.get(name)
        scoped = [e for e in entries if e.get("groupId") == current_group_id] if current_group_id else entries
        if not scoped:
            scoped = entries
        scoped = sorted(scoped, key=lambda e: e.get("timestamp") or "")
        total = len(scoped)
        failed = sum(1 for e in scoped if not str(e.get("verdict", "")).startswith("Not Reproduced"))
        passed = total - failed
        frequency[name] = {
            "totalRuns": total,
            "failedRuns": failed,
            "passedRuns": passed,
            "lastPassed": None,
            "pattern": "recurring" if total >= 2 else None,
            "confidence": "low" if total <= 2 else "high",
        }
    return frequency


def _prior_history_by_scenario(fix_history: list[dict[str, Any]], scenario_names: set[str]) -> dict[str, list[dict[str, Any]]]:
    by_scenario: dict[str, list[dict[str, Any]]] = {}
    for entry in fix_history:
        name = entry.get("scenarioName")
        if name in scenario_names:
            by_scenario.setdefault(name, []).append({
                "verdict": entry.get("verdict"),
                "confidence": entry.get("confidence"),
                "timestamp": entry.get("timestamp"),
                "change": entry.get("change"),
                "notes": entry.get("notes"),
            })
    for name, entries in by_scenario.items():
        entries.sort(key=lambda e: e.get("timestamp") or "", reverse=True)
        by_scenario[name] = entries[:3]
    return by_scenario


async def run_triage(
    input_path: Path,
    fix_history_path: Path,
    out_root: Path,
    repo_root: Path,
    batch_size: int,
) -> Path:
    data = json.loads(input_path.read_text(encoding="utf-8"))
    fix_history = json.loads(fix_history_path.read_text(encoding="utf-8")) if fix_history_path.exists() else []
    frequency = _frequency_by_scenario(fix_history, data.get("failures", []))

    groups = group_failures(data)
    receipts: dict[str, dict[str, Any]] = {}
    escalate_group_ids: list[str] = []
    llm_pending: list[tuple[dict[str, Any], dict[str, Any], dict[str, Any]]] = []  # (group, representative, det_result)
    tier1_prompt_chars = 0
    tier1_text_call_count = 0

    config = load_config()

    for group in groups:
        failures = group["failures"]
        representative = select_representative(failures)

        if all_likely_intermittent(failures):
            receipts[group["groupId"]] = _build_receipt(
                group, representative, "tier0_intermittent",
                "not_reproduced_intermittent", "high",
                ["All failures in this group are mechanically flagged likely-intermittent (same-run step reliability)."],
                "No action — same-run reliability signal.", None,
            )
            continue

        cache_hit = _cache_lookup(representative, group, fix_history, repo_root)
        if cache_hit is not None:
            receipts[group["groupId"]] = cache_hit
            continue

        det = classify(representative, frequency.get(representative.get("scenarioName"), {}))
        cause = det.get("cause")

        if cause == "unknown":
            # Try offline DOM analysis first — if the failure has saved page
            # source and the selector can be diagnosed deterministically,
            # resolve it here instead of burning an LLM call.
            dom_result = analyze_offline(representative)
            if dom_result and dom_result.get("verdict") != "needs_investigation":
                receipts[group["groupId"]] = _build_receipt(
                    group, representative, "tier2_offline_dom",
                    dom_result["verdict"], dom_result["confidence"],
                    dom_result["evidence"],
                    dom_result.get("recommendedAction", "Fix proposed by offline DOM analysis."),
                    None,
                )
                continue
            llm_pending.append((group, representative, det))
            continue

        disp = disposition(cause, representative, repo_root)
        if disp["resolved"]:
            # Route through the same allowlist/fix-verification clamp the LLM
            # path uses — disposition() only returns safe verdicts today, but
            # this makes that a structural guarantee rather than a property
            # that depends on disposition() staying hand-written correctly.
            disp = enforce_tier1_verdict_constraints(disp, repo_root)
        if disp["resolved"] and not disp.get("needsLiveReplay"):
            receipts[group["groupId"]] = _build_receipt(
                group, representative, "tier1_deterministic",
                disp["verdict"], disp["confidence"], disp["evidence"],
                disp["recommendedAction"], disp["proposedChange"],
            )
        else:
            escalate_group_ids.append(group["groupId"])

    if llm_pending:
        llm = create_connector(config.llm)
        scenario_names = {rep.get("scenarioName") for _, rep, _ in llm_pending}
        prior_history = _prior_history_by_scenario(fix_history, scenario_names)

        for start in range(0, len(llm_pending), batch_size):
            chunk = llm_pending[start:start + batch_size]
            batch_failures = [rep for _, rep, _ in chunk]
            tier1_prompt_chars += len(build_tier1_prompt(batch_failures, prior_history, repo_root))
            tier1_text_call_count += 1
            results = await triage_with_llm(
                batch_failures, prior_history, llm, repo_root,
                send_screenshots=config.llm.send_screenshots,
            )
            for group, representative, _det in chunk:
                # groupId, not scenarioName -- two different groups can share
                # a scenarioName (see llm_static_triage.py's correlation contract).
                result = results.get(group["groupId"])
                if result is None or result.get("needsLiveReplay"):
                    escalate_group_ids.append(group["groupId"])
                    continue
                receipts[group["groupId"]] = _build_receipt(
                    group, representative, "tier1_llm_static",
                    result["verdict"], str(result.get("confidence") or "low").lower(),
                    result.get("evidence") or [], result.get("recommendedAction") or "",
                    result.get("proposedChange"),
                    used_screenshot=bool(result.get("usedScreenshot")),
                )

    # --- Tier 2: Offline DOM analysis for escalated groups with artifacts ---
    groups_by_id = {g["groupId"]: g for g in groups}
    still_escalated: list[str] = []
    for gid in escalate_group_ids:
        if gid in receipts:
            continue
        group = groups_by_id.get(gid)
        if not group:
            still_escalated.append(gid)
            continue
        representative = select_representative(group["failures"])
        dom_result = analyze_offline(representative)
        if dom_result and dom_result.get("verdict") != "needs_investigation":
            receipts[gid] = _build_receipt(
                group, representative, "tier2_offline_dom",
                dom_result["verdict"], dom_result["confidence"],
                dom_result["evidence"],
                dom_result.get("recommendedAction", "Fix proposed by offline DOM analysis."),
                None,
            )
        else:
            still_escalated.append(gid)
    escalate_group_ids = still_escalated

    timestamp = datetime.now().strftime("%Y-%m-%d-%H%M%S")
    out_dir = out_root / timestamp
    receipts_dir = out_dir / "receipts"
    receipts_dir.mkdir(parents=True, exist_ok=True)

    manifest_groups = []
    for group in groups:
        group_id = group["groupId"]
        receipt = receipts.get(group_id)
        receipt_path = receipts_dir / f"{group['index']:03d}.receipt.json" if receipt is not None else None
        manifest_groups.append({
            "groupId": group_id,
            "scenarioCount": len(group["failures"]),
            "representativeScenario": select_representative(group["failures"]).get("scenarioName"),
            "resolved": receipt is not None,
            # So a downstream aggregator (validate_replay_receipts.py --triage-manifest)
            # can locate each receipt directly instead of reconstructing the filename.
            "receiptPath": str(receipt_path) if receipt_path else None,
        })
        if receipt is not None:
            receipt_path.write_text(json.dumps(receipt, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    verdict_counts: dict[str, int] = {}
    tier_counts: dict[str, int] = {}
    for receipt in receipts.values():
        verdict = str(receipt.get("verdict") or "needs_investigation")
        tier = str(receipt.get("analysisTier") or "unknown")
        verdict_counts[verdict] = verdict_counts.get(verdict, 0) + 1
        tier_counts[tier] = tier_counts.get(tier, 0) + 1

    unique_escalations = sorted(set(escalate_group_ids))
    manifest = {
        "generatedAt": timestamp,
        "source": str(input_path),
        "totalFailures": len(data.get("failures", [])),
        "groupCount": len(groups),
        "resolvedCount": len(receipts),
        "escalatedCount": len(unique_escalations),
        "metrics": {
            "totalGroups": len(groups),
            "staticResolvedGroups": len(receipts),
            "replayEscalatedGroups": len(unique_escalations),
            "tier1TextCallCount": tier1_text_call_count,
            "estimatedTier1PromptChars": tier1_prompt_chars,
            "estimatedTier1PromptTokens": _estimated_tokens("x" * tier1_prompt_chars) if tier1_prompt_chars else 0,
            "resolvedByVerdict": verdict_counts,
            "resolvedByTier": tier_counts,
        },
        "groups": manifest_groups,
    }
    (out_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    (out_dir / "escalate-groups.json").write_text(
        json.dumps({"groupIds": unique_escalations}, indent=2) + "\n", encoding="utf-8"
    )
    return out_dir


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-i", "--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("--fix-history", type=Path, default=DEFAULT_FIX_HISTORY)
    parser.add_argument("-o", "--out-root", type=Path, default=DEFAULT_OUT_ROOT)
    parser.add_argument("--repo-root", type=Path, default=_REPO_ROOT)
    parser.add_argument("--tier1-batch-size", type=int, default=DEFAULT_BATCH_SIZE)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    out_dir = asyncio.run(run_triage(
        input_path=args.input,
        fix_history_path=args.fix_history,
        out_root=args.out_root,
        repo_root=args.repo_root.resolve(),
        batch_size=max(1, args.tier1_batch_size),
    ))
    print(f"Wrote triage results: {out_dir}")


if __name__ == "__main__":
    main()
