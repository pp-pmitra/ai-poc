"""Port of promptBuilder.js — builds analysis prompts and renders maintenance reports."""

from __future__ import annotations

import os
import re
from pathlib import Path
from typing import Any, Optional

from failure_analyzer.grouping.failure_grouper import FailureGroup
from failure_analyzer.packet_utils import filter_noise_network_errors
from failure_analyzer.parsers.step_def_extractor import (
    get_step_def_snippets_for_group,
    get_all_step_def_snippets_for_scenario,
    get_step_def_snippet,
)

# ---------------------------------------------------------------------------
# Template loading
# ---------------------------------------------------------------------------

_PROMPTS_DIR = Path(__file__).parent

_BUG_TEMPLATE_PATH = _PROMPTS_DIR / "bugReport.txt"
_SINGLE_TEMPLATE_PATH = _PROMPTS_DIR / "singleFailureAnalysis.txt"
_GLOSSARY_PATH = _PROMPTS_DIR / "app-glossary.txt"

BUG_TEMPLATE = _BUG_TEMPLATE_PATH.read_text(encoding="utf-8") if _BUG_TEMPLATE_PATH.exists() else ""
SINGLE_FAILURE_TEMPLATE = _SINGLE_TEMPLATE_PATH.read_text(encoding="utf-8") if _SINGLE_TEMPLATE_PATH.exists() else ""
APP_GLOSSARY = _GLOSSARY_PATH.read_text(encoding="utf-8") if _GLOSSARY_PATH.exists() else ""

# ---------------------------------------------------------------------------
# Context routing
# ---------------------------------------------------------------------------

CONTEXT_DIR = _PROMPTS_DIR / "context"

CONTEXT_ROUTE_RULES: list[tuple[list[str], str]] = [
    (["campaign", "lineitem", "line_item", "tactic"], "campaign-lineitem-tactic.txt"),
    (["targeting"],                                    "targeting.txt"),
    (["creative"],                                     "creatives.txt"),
    (["pmp", "deal"],                                  "pmp-and-deals.txt"),
    (["npi", "list", "upload"],                        "lists-and-uploads.txt"),
    (["report", "export"],                             "reports-and-export.txt"),
    (["studio"],                                       "studio.txt"),
    (["hcp"],                                          "hcp.txt"),
    (["e2e"],                                          "e2e-general.txt"),
    (["life"],                                         "life-and-npi.txt"),
    (["regression"],                                   "regression-general.txt"),
]

FALLBACK_CONTEXT_FILE = "maintenance-and-analysis.txt"

CATEGORY_ICON: dict[str, str] = {
    "Potential Functional Issue": "🔴",
    "Script Issue": "🟡",
    "Environment Issue": "⚪",
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def load_context_for_features(feature_files: list[str]) -> str:
    """Load the most relevant context file for the given feature files.

    Falls through to the next matching rule — and finally to the fallback
    file — whenever a route's file doesn't exist on disk yet, instead of
    returning a "not found" placeholder that silently wastes a prompt section.
    """
    combined = " ".join(
        Path(f).stem.lower() for f in feature_files
    )

    for keywords, filename in CONTEXT_ROUTE_RULES:
        if not any(k in combined for k in keywords):
            continue
        full_path = CONTEXT_DIR / filename
        if full_path.exists():
            return full_path.read_text(encoding="utf-8")

    fallback_path = CONTEXT_DIR / FALLBACK_CONTEXT_FILE
    if fallback_path.exists():
        return fallback_path.read_text(encoding="utf-8")
    return ""


def extract_search_term(expected_value: str | None) -> str | None:
    """Pull the first quoted token from an expected-value string."""
    if not expected_value:
        return None
    m = re.search(r"""['"]([^'"]+)['"]""", expected_value)
    return m.group(1) if m else expected_value.strip()


def render_action_timeline(action_timeline) -> str | None:
    """Render the action timeline as a markdown table. Returns ``None`` when empty."""
    if not action_timeline:
        return None

    def _ga(a, key, default=None):
        if isinstance(a, dict):
            return a.get(key, default)
        return getattr(a, key, default)

    has_steps = any(_ga(a, "stepName") for a in action_timeline)
    last_step = None

    rows: list[str] = []
    for a in action_timeline:
        api_name = _ga(a, "apiName", "")
        selector = _ga(a, "selector", "")
        action = f"{api_name} `{selector}`" if selector else api_name
        failed = _ga(a, "failed", False)
        inferred = _ga(a, "inferred", False)
        error = _ga(a, "error", "")
        duration_ms = _ga(a, "durationMs", None)

        if failed:
            result = f"❌ {error or 'FAILED/TIMED OUT'}"
        elif inferred:
            result = "🔎 last action before assertion"
        else:
            result = "✅"

        duration = f"{round(duration_ms)}ms" if isinstance(duration_ms, (int, float)) else "n/a"
        index = _ga(a, "index", "")

        if has_steps:
            step_name = _ga(a, "stepName", "") or ""
            # Only suppress the step header when the name is non-empty AND matches
            # the previous non-empty step — empty stepName entries must not update
            # last_step, otherwise the next real step is incorrectly suppressed.
            if step_name:
                step_cell = step_name if step_name != last_step else ""
                last_step = step_name
            else:
                step_cell = ""
            rows.append(f"| {index} | {step_cell} | {action} | {duration} | {result} |")
        else:
            rows.append(f"| {index} | {action} | {duration} | {result} |")

    if has_steps:
        header = [
            "| # | Step | Action | Duration | Result |",
            "|---|------|--------|----------|--------|",
        ]
    else:
        header = [
            "| # | Action | Duration | Result |",
            "|---|--------|----------|--------|",
        ]
    return "\n".join(header + rows)


def render_step_execution_trace(failure) -> str:
    """Render the full step execution trace as a compact text block for the LLM.

    Each line: [STATUS] Keyword step text → step_def_location
    """
    trace = getattr(failure, "step_execution_trace", None) or []
    if not trace:
        return "Not available"

    lines: list[str] = []
    for s in trace:
        status = s.get("status", "unknown").upper()
        keyword = s.get("keyword", "").strip()
        name = s.get("name", "").strip()
        step_def = s.get("step_def", "")
        # Shorten to just the method name: strip params first, then drop package prefix
        if step_def:
            qualified = step_def.split("(")[0]  # "stepdefinitions.LifeSteps.methodName"
            short_def = qualified.split(".")[-1]  # "methodName"
        else:
            short_def = ""
        step_text = f"{keyword} {name}".strip()
        suffix = f" → {short_def}" if short_def else ""
        lines.append(f"[{status:7}] {step_text}{suffix}")

    return "\n".join(lines)


# Extracts literal text from XPath expressions like contains(text(),'X') or text()='X'
_XPATH_TEXT_RE = re.compile(
    r"""(?:contains\s*\(\s*text\s*\(\s*\)\s*,\s*|text\s*\(\s*\)\s*=\s*)['"]([^'"]{3,})['"]""",
    re.IGNORECASE,
)


def extract_text_from_locator(selector: str) -> str | None:
    """Extract human-readable literal text from an XPath locator string."""
    m = _XPATH_TEXT_RE.search(selector)
    return m.group(1) if m else None


def _get_attr(obj, key):
    if isinstance(obj, dict):
        return obj.get(key)
    return getattr(obj, key, None)


def ui_text_check(failure) -> str | None:
    """Check whether the expected/locator text appears in the page text.

    Works for both AssertionErrors (expected_value set) and TimeoutErrors
    (text extracted from the failing action's XPath locator).
    """
    page_text = _get_attr(failure, "page_text")
    if not page_text:
        return None

    # If the DOM snapshot is more than 5 seconds older than the failing action,
    # the page likely changed in between (navigation, redirect, SPA route change).
    # Returning a stale result here causes hallucinated UI Check conclusions.
    snapshot_age_ms = _get_attr(failure, "snapshot_age_ms")
    if snapshot_age_ms is not None and snapshot_age_ms > 5000:
        return None

    # --- AssertionError path: use expected_value ---
    expected_value = _get_attr(failure, "expected_value")
    if expected_value:
        term = extract_search_term(expected_value)
        if term:
            present = term.lower() in page_text.lower()
            if present:
                return f"Expected text '{term}' IS present in the final page DOM (may be a timing/locator issue)"
            return f"Expected text '{term}' NOT found in the final page DOM (likely removed or renamed in the app)"

    # --- TimeoutError path: extract text from the failing action's XPath ---
    action_timeline = _get_attr(failure, "action_timeline") or []
    for action in action_timeline:
        failed = action.get("failed", False) if isinstance(action, dict) else getattr(action, "failed", False)
        if not failed:
            continue
        selector = action.get("selector", "") if isinstance(action, dict) else getattr(action, "selector", "")
        if not selector:
            continue
        term = extract_text_from_locator(selector)
        if term:
            present = term.lower() in page_text.lower()
            if present:
                return f"Locator text '{term}' IS present in the final page DOM — element exists but XPath/tag may not match"
            return f"Locator text '{term}' NOT found in the final page DOM — element may have been removed or not yet rendered"

    return None


def category_icon(category: str) -> str:
    """Map a category string to its emoji icon."""
    return CATEGORY_ICON.get(category, "")


def format_history_verdict(freq) -> str:
    """Render a scenario's cross-run pass/fail history as an explicit verdict.

    Uses the fields from ``history_writer.get_scenario_run_pattern`` when present
    (recurring / intermittent / regression), falling back to a plain count when
    only the legacy ``count``/``total`` keys are available.
    """
    def _g(key, default=None):
        return freq.get(key, default) if isinstance(freq, dict) else getattr(freq, key, default)

    pattern = _g("pattern")
    total = _g("totalRuns", _g("total", 0)) or 0
    failed = _g("failedRuns", _g("count", 0)) or 0
    passed = _g("passedRuns", 0) or 0
    last_passed = _g("lastPassed")
    seq = _g("recentSequence") or []
    confidence = _g("confidence")

    if not total or pattern in (None, "first-failure"):
        return "First recorded occurrence for this scenario (no prior run history)"

    if pattern == "recurring":
        msg = (
            f"FAILED {failed}/{total} recent run(s) — recurring/consistent failure "
            f"(not flaky); likely a real issue or a stale test"
        )
    elif pattern == "intermittent":
        msg = (
            f"FAILED {failed}/{total} and PASSED {passed}/{total} recent run(s) — "
            f"intermittent/flaky (usually a timing or transient state issue)"
        )
    elif pattern == "regression":
        msg = (
            f"PASSED {passed}/{total} recent run(s) then started failing — "
            f"likely a regression after a recent change"
        )
    else:
        msg = f"Failed in {failed}/{total} recorded run(s)"

    if seq:
        msg += f". Recent (newest→oldest): {', '.join(seq)}"
    if last_passed:
        msg += f". Last passed {last_passed}"
    if confidence == "low":
        msg += (
            f" ⚠️ Low confidence — only {total} prior run(s) recorded; "
            f"treat this pattern as tentative until more runs accumulate"
        )
    return msg


# Keep in sync with the enforcement threshold in main.py / assertion_analyzer.py.
_INTERMITTENT_PASS_RATE_THRESHOLD = 0.5


def format_background_reliability(failure) -> Optional[str]:
    """Render the same-run Background reliability fact, or ``None`` when not
    applicable (not a Background failure, or no other executions this run).

    Each scenario gets its OWN independent Background execution (see
    cucumber_parser.py), so this reports the real pass/fail ratio across this
    feature's Background runs this run — not a binary "did any sibling pass".
    """
    if not getattr(failure, "is_background_failure", False):
        return None
    reliability = getattr(failure, "background_reliability", None)
    if not reliability or not reliability.get("total"):
        return None

    passed_bg = reliability["passed"]
    total_bg = reliability["total"]
    rate = reliability["rate"]

    if rate >= _INTERMITTENT_PASS_RATE_THRESHOLD:
        return (
            f"⚠️ The identical Background PASSED for {passed_bg}/{total_bg} "
            f"({rate:.0%}) executions in this same run — intermittent/transient "
            f"(flaky) setup failure, NOT a functional break"
        )
    if rate > 0:
        return (
            f"⚠️ The identical Background only PASSED {passed_bg}/{total_bg} "
            f"({rate:.0%}) executions in this same run — a FREQUENTLY reproducing "
            f"failure, not a rare blip. Likely a real environment/performance "
            f"issue on this step, not just a timing race — manual investigation "
            f"recommended"
        )
    return (
        f"⚠️ The identical Background FAILED for all {total_bg} execution(s) in "
        f"this same run — a consistent, fully-reproducing failure with no "
        f"passing-sibling evidence"
    )


def find_analysis_for_scenario(analyses: dict | None, scenario_name: str):
    """Tolerant scenario-name lookup in an analyses dict.

    Tries exact match first, then fuzzy substring with a 0.6 length-ratio guard.
    """
    if not analyses:
        return None
    if scenario_name in analyses:
        return analyses[scenario_name]
    target = scenario_name.lower().strip()
    for name, value in analyses.items():
        candidate = name.lower().strip()
        if target in candidate or candidate in target:
            longer = max(len(target), len(candidate))
            shorter = min(len(target), len(candidate))
            if longer > 0 and shorter / longer >= 0.6:
                return value
    return None


def group_by_root_cause(failures) -> tuple[list[dict], list]:
    """Group failures sharing the same category + step + locator.

    Returns ``(grouped, singletons)`` where grouped are clusters of 2+.
    """
    map_: dict[str, dict] = {}
    singletons: list = []

    for f in failures:
        issue_category = getattr(f, "issue_category", None)
        failed_step = getattr(f, "failed_step", None)
        if not issue_category or not failed_step:
            singletons.append(f)
            continue

        action_timeline = getattr(f, "action_timeline", []) or []
        failing_action = None
        for a in action_timeline:
            a_failed = a.get("failed", False) if isinstance(a, dict) else getattr(a, "failed", False)
            a_inferred = a.get("inferred", False) if isinstance(a, dict) else getattr(a, "inferred", False)
            if a_failed or a_inferred:
                failing_action = a
                break
        selector = (failing_action.get("selector") if isinstance(failing_action, dict) else getattr(failing_action, "selector", None)) if failing_action else None

        key = f"{issue_category}|||{failed_step}|||{selector or ''}"
        if key not in map_:
            map_[key] = {"category": issue_category, "step": failed_step, "selector": selector, "failures": []}
        map_[key]["failures"].append(f)

    grouped: list[dict] = []
    for g in map_.values():
        if len(g["failures"]) >= 2:
            grouped.append(g)
        else:
            singletons.extend(g["failures"])

    return grouped, singletons


def render_grouped_block(group: dict, frequency_by_scenario: dict, analyses: dict | None) -> str:
    """Render one combined block for N scenarios sharing the same root cause."""
    icon = category_icon(group["category"])
    count = len(group["failures"])
    noun = "stale locator" if group.get("selector") else "failing step"

    lines = [
        f"### {icon} {group['category']} — {count} scenarios fail on the same {noun}",
        "",
        "| Field | Value |",
        "|-------|-------|",
        f"| **Failed Step** | `{group['step']}` |",
    ]
    if group.get("selector"):
        lines.append(f"| **Locator** | `{group['selector']}` |")
    lines.extend(["", "**Affected scenarios:**"])

    for f in group["failures"]:
        sn = getattr(f, "scenario_name", "")
        freq = frequency_by_scenario.get(sn, {"count": 0, "total": 0})
        freq_total = freq.get("total", 0) if isinstance(freq, dict) else getattr(freq, "total", 0)
        freq_count = freq.get("count", 0) if isinstance(freq, dict) else getattr(freq, "count", 0)
        note = f" — failed {freq_count}/{freq_total} run(s)" if freq_total > 0 else ""
        lines.append(f"- {sn}{note}")

    # Use the first failure as representative — all failures in the group share
    # the same root cause so one timeline/trace covers the full picture.
    rep = group["failures"][0]
    rep_name = getattr(rep, "scenario_name", "")

    error_message = getattr(rep, "error_message", None)
    if error_message:
        lines.extend(["", f"> **Error:** `{error_message.split(chr(10))[0]}`"])

    action_timeline = getattr(rep, "action_timeline", None)
    timeline = render_action_timeline(action_timeline)
    if timeline:
        lines.extend(["", "**Action Timeline:**", "", timeline])

    # Step definitions (collapsible) from representative failure
    step_trace = getattr(rep, "step_execution_trace", None) or []
    rep_feature_file = getattr(rep, "feature_file", "") or ""
    if step_trace:
        step_code_blocks: list[str] = []
        for s in step_trace:
            s_status = s.get("status", "unknown").upper()
            s_keyword = s.get("keyword", "").strip()
            s_name = s.get("name", "").strip()
            full_step = f"{s_keyword} {s_name}".strip()
            code = get_step_def_snippet(full_step, rep_feature_file)
            icon_s = "❌" if s_status == "FAILED" else ("⏭" if s_status == "SKIPPED" else "✅")
            header_line = f"**{icon_s} [{s_status}] {full_step}**"
            if code:
                step_code_blocks.append(f"{header_line}\n```java\n{code}\n```")
            else:
                step_code_blocks.append(f"{header_line}\n_Step definition not found_")
        lines.extend([
            "",
            "<details>",
            "<summary>📄 Step Definitions</summary>",
            "",
            "\n\n".join(step_code_blocks),
            "",
            "</details>",
        ])

    stack_trace = getattr(rep, "stack_trace", None)
    if stack_trace:
        lines.extend(["", "```java", stack_trace, "```"])

    screenshot_file = getattr(rep, "screenshot_file", None)
    if screenshot_file:
        lines.extend(["", f"![Failure screenshot]({screenshot_file})"])

    ai = find_analysis_for_scenario(analyses, rep_name)
    if ai:
        ai_analysis = ai.get("analysis", "") if isinstance(ai, dict) else getattr(ai, "analysis", "")
        ai_fix = ai.get("fix", "") if isinstance(ai, dict) else getattr(ai, "fix", "")
        if ai_analysis or ai_fix:
            lines.extend(["", f"**Analysis:** {ai_analysis or '_Not available_'}"])
            lines.extend(["", f"**Fix:** {ai_fix or '_Not available_'}"])

    return "\n".join(lines)


# ---------------------------------------------------------------------------
# Feature sections renderer
# ---------------------------------------------------------------------------

def render_feature_sections(
    assertion_groups,
    frequency_by_scenario: dict,
    for_prompt: bool = False,
    analyses: dict | None = None,
) -> str:
    """Render the per-feature scenario blocks (the heart of the report).

    Parameters
    ----------
    assertion_groups:
        List of ``FailureGroup`` instances.
    frequency_by_scenario:
        ``{scenario_name: {"count": int, "total": int, "lastPassed": str | None}}``.
    for_prompt:
        When ``True``, emits facts only (no icons, no grouped blocks, no screenshots).
    analyses:
        ``{scenario_name: {"analysis": str, "fix": str}}``.
    """
    if analyses is None:
        analyses = {}

    global_idx = 0

    # Group failures by feature file for visual grouping
    by_feature: dict[str, list] = {}
    for g in assertion_groups:
        failures = getattr(g, "failures", []) if not isinstance(g, dict) else g.get("failures", [])
        for f in failures:
            feature_file = getattr(f, "feature_file", "") if not isinstance(f, dict) else f.get("feature_file", "")
            feature_key = Path(feature_file).stem if feature_file else "Unknown"
            if feature_key not in by_feature:
                by_feature[feature_key] = []
            by_feature[feature_key].append(f)

    feature_sections: list[str] = []

    for feature_key, feature_failures in by_feature.items():
        # In report mode: detect same-cause groups and render them first
        grouped_scenarios: set[str] = set()
        grouped_blocks: list[str] = []
        if not for_prompt:
            grouped, _ = group_by_root_cause(feature_failures)
            for g in grouped:
                for f in g["failures"]:
                    grouped_scenarios.add(getattr(f, "scenario_name", ""))
                grouped_blocks.append(render_grouped_block(g, frequency_by_scenario, analyses))

        individual_failures = (
            feature_failures
            if for_prompt
            else [f for f in feature_failures if getattr(f, "scenario_name", "") not in grouped_scenarios]
        )

        failure_blocks: list[str] = []
        for f in individual_failures:
            global_idx += 1
            scenario_name = getattr(f, "scenario_name", "")
            freq = frequency_by_scenario.get(scenario_name, {"count": 0, "total": 0})
            history = format_history_verdict(freq)

            lines: list[str] = [
                f"### {global_idx}. {scenario_name}",
                "",
                "| Field    | Value |",
                "|----------|-------|",
            ]

            # Category (report mode only)
            issue_category = getattr(f, "issue_category", None)
            if not for_prompt and issue_category:
                lines.append(f"| **Category** | {category_icon(issue_category)} {issue_category} |")

            lines.append(f"| **Feature** | `{feature_key}.feature` |")
            failed_step = getattr(f, "failed_step", "N/A") or "N/A"
            lines.append(f"| **Step** | `{failed_step}` |")

            # Error location
            error_location = getattr(f, "error_location", None)
            if error_location:
                sd = getattr(error_location, "step_def", None)
                if sd:
                    cn = getattr(sd, "class_name", "")
                    mt = getattr(sd, "method", "")
                    fl = getattr(sd, "file", "")
                    ln = getattr(sd, "line", 0)
                    base = Path(fl).name if fl else ""
                    lines.append(f"| **Error At** | `{cn}.{mt}()` — [{base}:{ln}]({fl}:{ln}) |")
                po = getattr(error_location, "page_object", None)
                if po:
                    cn = getattr(po, "class_name", "")
                    mt = getattr(po, "method", "")
                    fl = getattr(po, "file", "")
                    ln = getattr(po, "line", 0)
                    base = Path(fl).name if fl else ""
                    lines.append(f"| **Page Object** | `{cn}.{mt}()` — [{base}:{ln}]({fl}:{ln}) |")
                ut = getattr(error_location, "utility", None)
                if ut:
                    cn = getattr(ut, "class_name", "")
                    mt = getattr(ut, "method", "")
                    fl = getattr(ut, "file", "")
                    ln = getattr(ut, "line", 0)
                    base = Path(fl).name if fl else ""
                    lines.append(f"| **Utility** | `{cn}.{mt}()` — [{base}:{ln}]({fl}:{ln}) |")
                feat = getattr(error_location, "feature", None)
                if feat:
                    feat_file = getattr(feat, "file", "")
                    feat_line = getattr(feat, "line", 0)
                    feat_base = Path(feat_file).name if feat_file else ""
                    lines.append(f"| **Feature Line** | [{feat_base}:{feat_line}]({feat_file}:{feat_line}) |")

            workspace_type = getattr(f, "workspace_type", None)
            if workspace_type:
                lines.append(f"| **Workspace Type** | `{workspace_type}` |")
            line_item_type = getattr(f, "line_item_type", None)
            if line_item_type:
                lines.append(f"| **Line Item Type** | `{line_item_type}` |")

            expected_value = getattr(f, "expected_value", None)
            if expected_value:
                actual_value = getattr(f, "actual_value", "")
                lines.append(f"| **Expected** | `{expected_value}` |")
                lines.append(f"| **Actual** | `{actual_value}` |")

            page_url = getattr(f, "page_url", None)
            if page_url:
                lines.append(f"| **Page URL** | {page_url} |")

            # DOM evidence
            ui_check_result = ui_text_check(f)
            if ui_check_result:
                lines.append(f"| **UI Check** | {ui_check_result} |")

            if not for_prompt:
                network_errors = filter_noise_network_errors(getattr(f, "network_errors", None) or [])
                if network_errors:
                    net_joined = " \\| ".join(network_errors[:8])
                    if len(network_errors) > 8:
                        net_joined += f" (+{len(network_errors) - 8} more)"
                    lines.append(f"| **Network** | `{net_joined}` |")
                console_errors = getattr(f, "console_errors", None) or []
                if console_errors:
                    raw_console = console_errors[0]
                    # strip JS stack frames — keep only the first error line
                    console_first_line = raw_console.split("\n    at ")[0].split("\n")[0].strip()
                    lines.append(f"| **Console** | `{console_first_line}` |")

            lines.append(f"| **History** | {history} |")

            # Same-run reliability: how often THIS feature's Background succeeded
            # elsewhere in this same run (each scenario gets its own independent
            # Background execution — see cucumber_parser.py).
            bg_verdict = format_background_reliability(f)
            if bg_verdict:
                lines.append(f"| **Same-Run Reliability** | {bg_verdict} |")
            lines.append("")

            # Error message quote
            error_message = getattr(f, "error_message", "N/A") or "N/A"
            first_err_line = error_message.split("\n")[0]
            lines.append(f"> **Error:** `{first_err_line}`")

            # Action timeline
            action_timeline = getattr(f, "action_timeline", None)
            timeline = render_action_timeline(action_timeline)
            if timeline:
                lines.extend(["", "**Action Timeline:**", "", timeline])

            # Step definition code (collapsible — report mode only)
            if not for_prompt:
                step_trace = getattr(f, "step_execution_trace", None) or []
                f_feature_file = getattr(f, "feature_file", "") or ""
                if step_trace:
                    step_code_blocks: list[str] = []
                    for s in step_trace:
                        s_status = s.get("status", "unknown").upper()
                        s_keyword = s.get("keyword", "").strip()
                        s_name = s.get("name", "").strip()
                        full_step = f"{s_keyword} {s_name}".strip()
                        code = get_step_def_snippet(full_step, f_feature_file)
                        icon = "❌" if s_status == "FAILED" else ("⏭" if s_status == "SKIPPED" else "✅")
                        header_line = f"**{icon} [{s_status}] {full_step}**"
                        if code:
                            step_code_blocks.append(f"{header_line}\n```java\n{code}\n```")
                        else:
                            step_code_blocks.append(f"{header_line}\n_Step definition not found_")
                    lines.extend([
                        "",
                        "<details>",
                        "<summary>📄 Step Definitions</summary>",
                        "",
                        "\n\n".join(step_code_blocks),
                        "",
                        "</details>",
                    ])

            # Stack trace
            stack_trace = getattr(f, "stack_trace", None)
            if stack_trace:
                lines.extend(["", "```java", stack_trace, "```"])

            # Page DOM text — sent to the LLM when a text locator or assertion failed.
            # Lets the LLM see what IS on the page (not just "found/not found").
            if for_prompt:
                page_text = getattr(f, "page_text", None) or ""
                # Include when there is a UI check (text-locator or assertion), or
                # when there is an expected_value to compare against.
                has_text_failure = bool(ui_check_result or getattr(f, "expected_value", None))
                if page_text and has_text_failure:
                    truncated = page_text[:2000]
                    suffix = "…(truncated)" if len(page_text) > 2000 else ""
                    lines.extend(["", f"**Page DOM Text (at time of failure):**", "```", f"{truncated}{suffix}", "```"])

            # API error response bodies (report only — not sent to LLM for analysis)
            if not for_prompt:
                network_error_bodies = getattr(f, "network_error_bodies", None) or []
                if network_error_bodies:
                    lines.extend(["", "**API Error Response(s):**"])
                    for body_item in network_error_bodies:
                        endpoint = body_item.get("endpoint", "") if isinstance(body_item, dict) else getattr(body_item, "endpoint", "")
                        body = body_item.get("body", "") if isinstance(body_item, dict) else getattr(body_item, "body", "")
                        lines.extend(["", f"`{endpoint}`", "```", body, "```"])

            if not for_prompt:
                # Screenshot
                screenshot_file = getattr(f, "screenshot_file", None)
                if screenshot_file:
                    lines.extend(["", f"![Failure screenshot]({screenshot_file})"])

                ai = find_analysis_for_scenario(analyses, scenario_name)
                ai_analysis = ""
                ai_fix = ""
                if ai:
                    ai_analysis = ai.get("analysis", "") if isinstance(ai, dict) else getattr(ai, "analysis", "")
                    ai_fix = ai.get("fix", "") if isinstance(ai, dict) else getattr(ai, "fix", "")
                lines.extend([
                    "",
                    f"**Analysis:** {ai_analysis or '_[AI analysis unavailable for this scenario — see raw output below]_'}",
                    "",
                    f"**Fix:** {ai_fix or '_[AI fix unavailable for this scenario — see raw output below]_'}",
                ])

            failure_blocks.append("\n".join(lines))

        all_blocks = grouped_blocks + failure_blocks

        feature_sections.append("\n".join([
            "---",
            f"## 📁 {feature_key}.feature",
            "",
            "\n\n".join(all_blocks),
        ]))

    return "\n\n".join(feature_sections)


# ---------------------------------------------------------------------------
# Run-level facts
# ---------------------------------------------------------------------------

def _compute_run_facts(
    assertion_groups,
    frequency_by_scenario: dict,
    config,
    run_meta,
) -> dict:
    """Compute run-level facts shared by the prompt and the final report."""
    from datetime import datetime as _dt

    run_date = _dt.now().strftime("%Y-%m-%d")
    environment = getattr(config, "environment", "Demo") if not isinstance(config, dict) else config.get("environment", "Demo")

    feature_areas_set: set[str] = set()
    all_failures: list = []
    for g in assertion_groups:
        affected = getattr(g, "affected_features", [])
        for ff in affected:
            feature_areas_set.add(Path(ff).stem)
        all_failures.extend(getattr(g, "failures", []))
    feature_areas = ", ".join(sorted(feature_areas_set))

    # Run type from tags
    all_tags: list[str] = []
    for f in all_failures:
        all_tags.extend(getattr(f, "tags", []) or [])

    if any("sanity" in t.lower() for t in all_tags):
        run_type = "Sanity"
    elif any("regression" in t.lower() for t in all_tags):
        run_type = "Regression"
    else:
        run_type = "Automation"

    assertion_count = len(all_failures)
    if run_meta:
        total_scenarios = run_meta.get("totalScenarios", 0) if isinstance(run_meta, dict) else getattr(run_meta, "total_scenarios", getattr(run_meta, "totalScenarios", 0))
        failed_count = run_meta.get("failedCount", 0) if isinstance(run_meta, dict) else getattr(run_meta, "failed_count", getattr(run_meta, "failedCount", 0))
        run_stats = (
            f"{assertion_count} assertion failure(s) out of {total_scenarios} total scenario(s) "
            f"({failed_count} total failure(s))"
        )
    else:
        run_stats = f"{assertion_count} assertion failure(s)"

    # Recurring failures (>= 50% of runs where the feature appeared)
    recurring_items: list[str] = []
    for f in all_failures:
        sn = getattr(f, "scenario_name", "")
        freq = frequency_by_scenario.get(sn)
        if freq:
            ft = freq.get("total", 0) if isinstance(freq, dict) else getattr(freq, "total", 0)
            fc = freq.get("count", 0) if isinstance(freq, dict) else getattr(freq, "count", 0)
            if ft > 1 and fc / ft >= 0.5:
                recurring_items.append(f"{sn} (failed {fc}/{ft} runs)")

    recurring = "; ".join(recurring_items)

    return {
        "runDate": run_date,
        "environment": environment,
        "featureAreas": feature_areas,
        "runType": run_type,
        "runStats": run_stats,
        "recurring": recurring,
    }


# ---------------------------------------------------------------------------
# Maintenance report builder
# ---------------------------------------------------------------------------

def build_maintenance_report(
    assertion_groups,
    frequency_by_scenario: dict,
    config,
    run_meta,
    analyses: dict | None = None,
) -> str:
    """Assemble the final maintenance report markdown deterministically.

    All facts come from parsed test data; only Analysis/Fix text comes from the LLM.
    """
    if analyses is None:
        analyses = {}

    facts = _compute_run_facts(assertion_groups, frequency_by_scenario, config, run_meta)

    sections = render_feature_sections(assertion_groups, frequency_by_scenario, analyses=analyses)

    parts: list[str] = [
        f"# [AUTOMATION] [MAINTENANCE] {facts['runType']} Analysis — {facts['runDate']}",
        "",
        "| | |",
        "|---|---|",
        f"| **Date** | {facts['runDate']} |",
        f"| **Environment** | {facts['environment']} |",
        f"| **Run Type** | {facts['runType']} |",
        f"| **Feature Area** | {facts['featureAreas']} |",
        f"| **Run Stats** | {facts['runStats']} |",
    ]

    if run_meta:
        app_version = run_meta.get("appVersion") if isinstance(run_meta, dict) else getattr(run_meta, "app_version", getattr(run_meta, "appVersion", None))
        if app_version:
            parts.append(f"| **App Version** | {app_version} |")

    # Category summary table
    all_failures: list = []
    for g in assertion_groups:
        all_failures.extend(getattr(g, "failures", []))
    by_category: dict[str, list[str]] = {}
    for f in all_failures:
        cat = getattr(f, "issue_category", None) or "Uncategorised"
        if cat not in by_category:
            by_category[cat] = []
        by_category[cat].append(getattr(f, "scenario_name", ""))

    summary_order = ["Potential Functional Issue", "Script Issue", "Environment Issue"]
    summary_rows: list[str] = []
    for cat in summary_order:
        scenarios = by_category.get(cat, [])
        scenario_list = "; ".join(scenarios) if scenarios else "—"
        summary_rows.append(f"| {category_icon(cat)} {cat} | {len(scenarios)} | {scenario_list} |")

    uncategorised = by_category.get("Uncategorised", [])
    if uncategorised:
        summary_rows.append(f"| ❓ Uncategorised | {len(uncategorised)} | {'; '.join(uncategorised)} |")

    parts.extend([
        "",
        "## 📊 Failure Summary",
        "",
        "| Category | Count | Scenarios |",
        "|----------|-------|-----------|",
        *summary_rows,
    ])

    parts.extend(["", sections, ""])
    parts.extend(["---", "", "## ⚠️ Recurring Failures (needs priority attention)", ""])
    parts.append(facts["recurring"] or "None identified.")

    # Safety net: surface unmatched AI output
    all_scenarios = [getattr(f, "scenario_name", "") for f in all_failures]
    missing = [name for name in all_scenarios if not find_analysis_for_scenario(analyses, name)]

    if missing:
        missing_list = "\n".join(f"{i + 1}. {n}" for i, n in enumerate(missing))
        parts.extend(["", "---", "", "## 🤖 Raw AI Output (some analyses could not be matched)", ""])
        parts.extend(["_The following scenario(s) received no AI analysis:_", "", missing_list])

    return "\n".join(parts)


# ---------------------------------------------------------------------------
# Work-item prompt builder (bug / task / story)
# ---------------------------------------------------------------------------

def build_work_item_prompt(
    groups,
    work_item_type: str,
    gherkin_by_scenario: dict,
    history_by_feature: dict,
    frequency_by_scenario: dict,
) -> list[str]:
    """Build one prompt per group using the bug report template.

    Returns a list of prompt strings.
    """
    prompts: list[str] = []

    for group in groups:
        failures = getattr(group, "failures", [])
        if not failures:
            continue

        root_cause_label = getattr(group, "root_cause_label", "")
        affected_features = getattr(group, "affected_features", [])

        sample = failures[0]
        scenario_names = "\n".join(f"  - {getattr(f, 'scenario_name', '')}" for f in failures)

        # Failure details via feature sections
        failure_details = render_feature_sections([group], frequency_by_scenario, for_prompt=True)

        # Gherkin for the first scenario
        gherkin = gherkin_by_scenario.get(getattr(sample, "scenario_name", ""), "")

        # Step definition code
        step_defs = get_step_def_snippets_for_group(failures) or "Not available"

        # History (de-duplicated across affected features)
        history_texts = list({
            history_by_feature.get(Path(ff).stem, "")
            for ff in affected_features
        })
        history = "\n".join(t for t in history_texts if t) or "No prior history recorded."

        # Context
        context = load_context_for_features(affected_features)

        prompt = (
            BUG_TEMPLATE
            .replace("{{rootCauseLabel}}", root_cause_label)
            .replace("{{affectedFeatures}}", ", ".join(affected_features))
            .replace("{{count}}", str(len(failures)))
            .replace("{{failureDetails}}", failure_details)
            .replace("{{gherkin}}", gherkin or "Not available")
            .replace("{{stepDefinitions}}", step_defs)
            .replace("{{history}}", history)
            .replace("{{context}}", context or "Not available")
            .replace("{{glossary}}", APP_GLOSSARY or "Not available")
        )

        prompts.append(prompt)

    return prompts


# ---------------------------------------------------------------------------
# Single-failure prompt builder
# ---------------------------------------------------------------------------

def build_single_failure_prompt(
    failure,
    frequency: dict | None,
    gherkin_text: str,
    history_text: str,
) -> str:
    """Build a prompt scoped to exactly one failure.

    Keeps prompt size constant regardless of how many failures occurred in the run.
    """
    scenario_name = getattr(failure, "scenario_name", "")
    feature_file = getattr(failure, "feature_file", "")

    fake_group = FailureGroup(
        group_id="single",
        root_cause_label="",
        normalized_step="",
        failures=[failure],
        affected_features=[feature_file] if feature_file else [],
    )

    failure_block = render_feature_sections(
        [fake_group],
        {scenario_name: frequency or {}},
        for_prompt=True,
    )

    # Cap gherkin to avoid sending huge data tables (raised to include Background)
    GHERKIN_CAP = 2500
    if gherkin_text and len(gherkin_text) > GHERKIN_CAP:
        capped_gherkin = (
            gherkin_text[:GHERKIN_CAP]
            + "\n    ... (truncated — see full feature file for complete data tables)"
        )
    else:
        capped_gherkin = gherkin_text or "Not available"

    step_execution_trace = getattr(failure, "step_execution_trace", []) or []
    step_def_code = get_all_step_def_snippets_for_scenario(
        step_execution_trace, feature_file or ""
    ) if step_execution_trace else (get_step_def_snippets_for_group([failure]) or "Not available")
    feature_context = load_context_for_features([feature_file or ""])
    step_exec_trace = render_step_execution_trace(failure)

    return (
        SINGLE_FAILURE_TEMPLATE
        .replace("{{appGlossary}}", APP_GLOSSARY or "Not available")
        .replace("{{featureContext}}", feature_context or "Not available")
        .replace("{{stepExecutionTrace}}", step_exec_trace)
        .replace("{{stepDefCode}}", step_def_code)
        .replace("{{gherkinSteps}}", capped_gherkin)
        .replace("{{historicalContext}}", history_text or "No prior history recorded.")
        .replace("{{failureBlock}}", failure_block)
    )
