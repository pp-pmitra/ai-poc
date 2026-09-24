"""Batched, text-only LLM fallback for the `cause == "unknown"` remainder that
`static_classifier.py`'s deterministic rules can't resolve.

This reuses the same two-call escalation *shape* already proven in
`failure_analyzer.main.analyze_failure_with_llm` twice over:
  - Pass 1 (always): a batched, TEXT-ONLY call — no MCP/tool access, so it
    cannot drive a browser by construction.
  - Pass 2 (only when pass 1 comes back `confidence: "low"` AND a failure
    screenshot actually exists): a single-item call with the failure-moment
    JPEG (already captured in the Playwright trace) attached, to see if a
    picture resolves what text alone couldn't — still no live browser access,
    just a static image from the original failed run.
  - Pass 3 (only if pass 2 still isn't confident, or no screenshot exists):
    escalate to Tier 2's live Playwright-MCP replay — the actually-expensive
    step this whole tier exists to avoid paying for by default.

Hard constraint, enforced in `enforce_tier1_verdict_constraints` at every
pass: this tier may only ever finalize `not_reproduced_intermittent` /
`script_issue_fix_proposed` (with a mechanically re-verified diff) /
`needs_investigation`. Anything else is clamped to `needs_investigation` +
`needsLiveReplay: true` — Tier 2's live replay and
`validate_replay_receipts.py`'s gate are the only places a product bug can
ever be confirmed.
"""

from __future__ import annotations

import base64
import json
import re
from pathlib import Path
from typing import Any, Optional

from failure_analyzer.packet_utils import (
    code_context_for_failure,
    filter_noise_network_errors,
    redact_text,
    text_present_near_line,
    truncate,
)
from failure_analyzer.parsers.trace_parser import extract_screenshot_base64

__all__ = [
    "build_tier1_prompt",
    "collect_screenshot",
    "triage_with_llm",
    "enforce_tier1_verdict_constraints",
]

_ALLOWED_VERDICTS = {
    "not_reproduced_intermittent",
    "script_issue_fix_proposed",
    "needs_investigation",
}

_ITEM_CAP = 500
_PAGE_TEXT_CAP = 800


def _compact_for_llm(failure: dict[str, Any], repo_root: Path) -> dict[str, Any]:
    record = {
        # groupId, not scenarioName, is the correlation key the LLM must echo
        # back — scenarioName can collide across two different failure groups
        # (an unparameterized Scenario Outline title, or the same name reused
        # across two .feature files), which would otherwise silently merge
        # two unrelated groups' verdicts/fixes into one.
        "groupId": failure.get("groupId"),
        "scenarioName": failure.get("scenarioName"),
        "featureFile": failure.get("featureFile"),
        "failedStep": failure.get("failedStep"),
        "errorMessage": truncate(failure.get("errorMessage"), _ITEM_CAP),
        "expectedValue": failure.get("expectedValue"),
        "actualValue": failure.get("actualValue"),
        "playwrightCallLog": truncate(failure.get("playwrightCallLog"), _ITEM_CAP),
        "pageText": truncate(failure.get("pageText"), _PAGE_TEXT_CAP),
        "consoleErrors": (failure.get("consoleErrors") or [])[:3],
        "networkErrors": filter_noise_network_errors(failure.get("networkErrors") or [])[:3],
        "codeContext": code_context_for_failure(repo_root, failure, roles=("stepDef", "pageObject")),
    }
    return redact_text(record)


def build_tier1_prompt(
    batch: list[dict[str, Any]],
    prior_history_by_scenario: dict[str, list[dict[str, Any]]],
    repo_root: Path,
) -> str:
    items = []
    for failure in batch:
        record = _compact_for_llm(failure, repo_root)
        prior = prior_history_by_scenario.get(failure.get("scenarioName"), [])
        if prior:
            record["priorHistory"] = prior
        items.append(record)

    return "\n".join([
        "You are a cheap, TEXT-ONLY triage pass for test failures. You have NO browser access "
        "and cannot verify anything live — you are working from static evidence only "
        "(error text, DOM snapshot text, console/network errors, code context).",
        "",
        "For EACH item in the batch below, decide one of exactly three outcomes:",
        '  - "not_reproduced_intermittent": only if the evidence itself shows a same-run flaky signal you can point to.',
        '  - "script_issue_fix_proposed": ONLY if you can identify a concrete, verbatim source change (exact before/after text, '
        "with file+line) that would fix it — e.g. a stale locator visible in the provided code context. Never propose a fix you "
        "haven't grounded in the literal code shown to you.",
        '  - "needs_investigation": the default for anything else, INCLUDING anything that looks like it might be a real product '
        "bug — you cannot confirm a product bug without live replay, so never claim one here.",
        "",
        "Set needsLiveReplay=true whenever you are not highly confident, whenever you suspect a real app/product issue, or "
        "whenever the fix you'd propose isn't grounded in code you were actually shown.",
        "",
        "Return ONLY a JSON array, one object per item, in the same order as the input, each shaped exactly as:",
        json.dumps({
            "groupId": "string, must match the input item's groupId EXACTLY — this is how your answer gets matched back "
                       "to the right item, not scenarioName (two different items can share a scenarioName)",
            "scenarioName": "string, for your own reference only",
            "verdict": "not_reproduced_intermittent | script_issue_fix_proposed | needs_investigation",
            "confidence": "high | medium | low",
            "needsLiveReplay": True,
            "evidence": ["2-4 concrete facts drawn only from the provided evidence"],
            "recommendedAction": "short action",
            "proposedChange": {"file": "string", "line": 1, "before": "string", "after": "string"},
        }, indent=2),
        '(omit "proposedChange" entirely, or set it to null, unless verdict is "script_issue_fix_proposed")',
        "",
        "Batch:",
        json.dumps(items, indent=2, sort_keys=True),
    ])


def collect_screenshot(failure: dict[str, Any], send_screenshots: bool) -> list[str]:
    """Base64 image for this failure's failure-moment screenshot, or `[]`
    when disabled/unavailable. Tries the Playwright trace-embedded screenshot
    first (mirrors `main.py`'s `collect_screenshot_for_failure`), then falls
    back to the separately-saved `failureArtifacts.screenshot` PNG on disk —
    `screenshotFile`/`tracePath` are sometimes unset even though a usable
    screenshot was still captured to `failureArtifacts` at failure time.
    """
    if not send_screenshots:
        return []
    trace_path = failure.get("tracePath")
    screenshot_name = failure.get("screenshotFile")
    if trace_path and screenshot_name:
        b64 = extract_screenshot_base64(trace_path, screenshot_name)
        if b64:
            return [b64]
    artifact_path = (failure.get("failureArtifacts") or {}).get("screenshot")
    if artifact_path and Path(artifact_path).exists():
        return [base64.b64encode(Path(artifact_path).read_bytes()).decode("ascii")]
    return []


def build_screenshot_escalation_prompt(
    failure: dict[str, Any],
    first_pass: dict[str, Any],
    repo_root: Path,
) -> str:
    record = _compact_for_llm(failure, repo_root)
    return "\n".join([
        "You already analyzed this failure TEXT-ONLY and came back with LOW confidence. A screenshot taken at "
        "the moment of failure (from the original test run, not a live session) is now attached — read it and "
        "use it to firm up or revise your answer. You still have NO live browser access.",
        "",
        "Your first-pass answer was:",
        json.dumps(first_pass, indent=2, sort_keys=True),
        "",
        "Same rules as before: verdict must be one of "
        '"not_reproduced_intermittent" | "script_issue_fix_proposed" | "needs_investigation" — never claim a '
        "product bug, and only propose a fix grounded in the code shown below. Set needsLiveReplay=true whenever "
        "you remain unsure or suspect a real app issue, even after seeing the screenshot.",
        "",
        "Return ONLY a JSON array with exactly one object, shaped exactly as your first pass was:",
        json.dumps([{
            "groupId": failure.get("groupId"),
            "scenarioName": failure.get("scenarioName"),
            "verdict": "not_reproduced_intermittent | script_issue_fix_proposed | needs_investigation",
            "confidence": "high | medium | low",
            "needsLiveReplay": True,
            "evidence": ["2-4 concrete facts, including what the screenshot shows"],
            "recommendedAction": "short action",
            "proposedChange": {"file": "string", "line": 1, "before": "string", "after": "string"},
        }], indent=2),
        "",
        "Evidence record:",
        json.dumps(record, indent=2, sort_keys=True),
    ])


async def _escalate_with_screenshot(
    failure: dict[str, Any],
    first_pass: dict[str, Any],
    llm,
    repo_root: Path,
    images: list[str],
) -> dict[str, Any]:
    """Pass 2: retry a low-confidence text-only result with the failure
    screenshot attached. Falls back to `first_pass` unchanged on any error or
    unusable output — an escalation attempt must never lose an already-usable
    (if low-confidence) answer.
    """
    group_id = failure.get("groupId")
    prompt = build_screenshot_escalation_prompt(failure, first_pass, repo_root)
    try:
        raw = await llm.generate(prompt, images=images)
        parsed = _parse_batch_response(raw, [group_id])
    except Exception:  # noqa: BLE001 - any connector failure keeps the pass-1 result
        parsed = None
    if parsed is None:
        return first_pass
    result = enforce_tier1_verdict_constraints(parsed[group_id], repo_root)
    return {**result, "usedScreenshot": True}


def _parse_batch_response(text: str, expected_group_ids: list[str]) -> Optional[dict[str, dict]]:
    """Parse the LLM's JSON array response into {groupId: item}. `groupId`
    (not scenarioName) is the correlation key — two different failure groups
    can legitimately share a scenarioName, so keying on it would silently
    merge their results.
    """
    match = re.search(r"\[[\s\S]*\]", text)
    if not match:
        return None
    try:
        parsed = json.loads(match.group(0))
    except json.JSONDecodeError:
        return None
    if not isinstance(parsed, list):
        return None
    by_group_id = {item.get("groupId"): item for item in parsed if isinstance(item, dict)}
    if not all(gid in by_group_id for gid in expected_group_ids):
        return None
    return by_group_id


def enforce_tier1_verdict_constraints(parsed: dict[str, Any], repo_root: Path) -> dict[str, Any]:
    """Clamp a single item's parsed LLM output to the tier's allowed verdicts,
    and mechanically re-verify any claimed fix diff before accepting it —
    mirrors `validate_replay_receipts.py`'s own "downgrade if unproven"
    philosophy, one layer earlier and independent of it.

    This is the ONE place any Tier-1-produced result (deterministic or LLM)
    must pass through before a receipt is written — callers should route
    every exit path here rather than writing a verdict directly.
    """
    verdict = str(parsed.get("verdict") or "needs_investigation").strip()
    if verdict not in _ALLOWED_VERDICTS:
        return {**parsed, "verdict": "needs_investigation", "needsLiveReplay": True, "proposedChange": None}

    if verdict == "script_issue_fix_proposed":
        change = parsed.get("proposedChange") or {}
        verified = text_present_near_line(repo_root, change.get("file"), change.get("line"), change.get("before"))
        if not verified:
            return {
                **parsed,
                "verdict": "needs_investigation",
                "needsLiveReplay": True,
                "proposedChange": None,
                "evidence": (parsed.get("evidence") or []) + ["LLM-proposed fix's \"before\" text could not be verified near the claimed line in the actual source file — downgraded."],
            }

    # Pass-through case: an allowed verdict, and (if applicable) a verified
    # fix. Still never trust a low-confidence answer's own needsLiveReplay
    # claim — force escalation unless confidence is explicitly high/medium,
    # so a response that simply omits the field can't slip past unescalated.
    confidence = str(parsed.get("confidence") or "").strip().lower()
    needs_live_replay = bool(parsed.get("needsLiveReplay")) or confidence not in ("high", "medium")
    return {**parsed, "needsLiveReplay": needs_live_replay}


async def triage_with_llm(
    batch: list[dict[str, Any]],
    prior_history_by_scenario: dict[str, list[dict[str, Any]]],
    llm,
    repo_root: Path,
    send_screenshots: bool = True,
) -> dict[str, dict[str, Any]]:
    """Run one batched text-only call for `batch`; on unparseable/mismatched
    output, retry each item individually rather than dropping any of them.
    Then, for any item still at `confidence: "low"`, try one screenshot
    escalation pass (pass 2) before returning — a low-confidence result with
    an available screenshot never ships as-is without that attempt.
    Returns {groupId: clamped-result-dict} — keyed by groupId, not
    scenarioName, since two different groups can share a scenarioName.
    """
    group_ids = [f.get("groupId") for f in batch]
    failures_by_group_id = {f.get("groupId"): f for f in batch}
    prompt = build_tier1_prompt(batch, prior_history_by_scenario, repo_root)
    try:
        raw = await llm.generate(prompt, images=[])
    except Exception:  # noqa: BLE001 - any connector failure falls back to per-item retry below
        raw = None

    parsed_by_group_id = _parse_batch_response(raw, group_ids) if raw else None
    if parsed_by_group_id is not None:
        results = {
            gid: enforce_tier1_verdict_constraints(parsed_by_group_id[gid], repo_root)
            for gid in group_ids
        }
    else:
        # Batch call failed or didn't parse cleanly — retry each item alone so
        # one bad response never silently drops the whole batch.
        results = {}
        for failure in batch:
            gid = failure.get("groupId")
            single_prompt = build_tier1_prompt([failure], prior_history_by_scenario, repo_root)
            try:
                single_raw = await llm.generate(single_prompt, images=[])
                single_parsed = _parse_batch_response(single_raw, [gid])
            except Exception:  # noqa: BLE001
                single_parsed = None
            if single_parsed is None:
                results[gid] = {
                    "groupId": gid,
                    "scenarioName": failure.get("scenarioName"),
                    "verdict": "needs_investigation",
                    "confidence": "low",
                    "needsLiveReplay": True,
                    "evidence": ["Tier 1 LLM call failed or returned unparseable output for this item."],
                    "recommendedAction": "Escalate to live replay.",
                    "proposedChange": None,
                }
            else:
                results[gid] = enforce_tier1_verdict_constraints(single_parsed[gid], repo_root)

    for gid, result in list(results.items()):
        if str(result.get("confidence") or "").lower() != "low":
            continue
        # If the text-only pass was *confident* that live replay is needed (high/medium
        # confidence + needsLiveReplay), a screenshot won't change that outcome — skip it.
        # Low-confidence results have needsLiveReplay forced True by enforce_tier1_verdict_constraints
        # purely because confidence is not high/medium; those still get screenshot escalation.
        confidence_level = str(result.get("confidence") or "").lower()
        if result.get("needsLiveReplay") and confidence_level in ("high", "medium"):
            continue
        failure = failures_by_group_id.get(gid)
        if failure is None:
            continue
        images = collect_screenshot(failure, send_screenshots)
        if not images:
            continue
        results[gid] = await _escalate_with_screenshot(failure, result, llm, repo_root, images)

    return results
