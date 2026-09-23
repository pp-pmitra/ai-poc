"""Port of assertionAnalyzer.js — deterministic, rule-based analysis for
AssertionError failures.

For AssertionError, the test run already gives us everything we need to
explain the failure: expected vs actual, whether the expected text is in the
final DOM, failing network calls, console errors, and the failure history.
An LLM only paraphrases these facts (and sometimes hallucinates), so this
module derives the Analysis/Fix text directly from the parsed facts -- no
model call, fully reproducible, zero hallucination.

Public API:
    analyze_failure(failure, frequency)               -> dict
    build_deterministic_analyses(groups, freq_map)    -> dict
"""

from __future__ import annotations

import os
import re
from typing import Optional

from failure_analyzer.packet_utils import filter_noise_network_errors

# A Background failure is only force-classified as intermittent/Script Issue when
# a MAJORITY of this feature's other Background executions succeeded this run.
# Keep in sync with main.py's constant of the same name/value.
_INTERMITTENT_PASS_RATE_THRESHOLD = 0.5


# ---------------------------------------------------------------------------
# Network-error classification
# ---------------------------------------------------------------------------

def classify_network_error(failure) -> Optional[dict]:
    """Classify the first *real* network error, if any.

    Distinguishes a real HTTP error (4xx/5xx, a backend/API problem) from an
    aborted request with no response at all (status -1, formatted as
    ``"-1 METHOD /path"`` by trace_correlator).  Tracking beacons, analytics
    pixels, and websocket keep-alives are filtered out before classification —
    their aborts are noise, not a sign the test runner's network dropped.
    """
    entries = getattr(failure, "network_errors", None) or []
    if not entries:
        return None
    real_entries = filter_noise_network_errors(entries)
    if not real_entries:
        return None
    entry = real_entries[0]
    parts = entry.split(" ", 1)
    if not parts:
        return None
    try:
        status = int(parts[0])
    except (ValueError, IndexError):
        return None
    if status == -1:
        return {"entry": entry, "kind": "aborted"}
    if status >= 400:
        return {"entry": entry, "kind": "http_error"}
    return None


_CRASH_PATTERNS: list[tuple[re.Pattern, str]] = [
    (re.compile(r"TargetClosedError|target page, context or browser has been closed", re.IGNORECASE), "target-closed"),
    (re.compile(r"page crashed|renderer process crashed", re.IGNORECASE), "page-crash"),
    (re.compile(r"browser (?:has been )?closed|browser disconnected|cdp.*disconnect", re.IGNORECASE), "browser-disconnected"),
    (re.compile(r"net::ERR_ABORTED|navigation aborted|frame was detached", re.IGNORECASE), "navigation-aborted"),
    (re.compile(r"about:blank|chrome-error://", re.IGNORECASE), "blank-or-error-page"),
]


def classify_environment_or_runner_failure(failure) -> Optional[dict]:
    """Detect browser/app-session/navigation failures that static analysis must
    not turn into product bugs.

    These are failures of the run reaching a trustworthy app state. Without a
    clean rerun or live replay reproducing the same crash at the same state,
    they should stay `needs_investigation`/infra-flavored.
    """
    combined = "\n".join(
        str(part or "")
        for part in (
            getattr(failure, "error_message", None),
            getattr(failure, "playwright_call_log", None),
            getattr(failure, "page_url", None),
            getattr(failure, "last_browser_action", None),
        )
    )
    for pattern, kind in _CRASH_PATTERNS:
        if pattern.search(combined):
            return {"kind": kind, "evidence": pattern.pattern}

    page_url = (getattr(failure, "page_url", None) or "").strip().lower()
    page_text = (getattr(failure, "page_text", None) or "").strip()
    last_action = (getattr(failure, "last_browser_action", None) or "").lower()
    if page_url in {"about:blank", ""} and "navigate" in last_action and len(page_text) < 20:
        return {"kind": "blank-navigation-state", "evidence": page_url or "missing page URL"}

    return None


# ---------------------------------------------------------------------------
# UI verdict — is the expected value actually on the final page?
# ---------------------------------------------------------------------------

def ui_verdict(failure) -> Optional[dict]:
    """Check whether the expected value text is present in the final page DOM.

    Returns ``{"term": str, "present": bool}`` or ``None`` when there is
    nothing to check.
    """
    page_text = getattr(failure, "page_text", None)
    expected_value = getattr(failure, "expected_value", None)
    if not page_text or not expected_value:
        return None
    # Stale snapshot — page likely changed since the snapshot was taken
    snapshot_age_ms = getattr(failure, "snapshot_age_ms", None)
    if snapshot_age_ms is not None and snapshot_age_ms > 5000:
        return None
    m = re.search(r"""['"]([^'"]+)['"]""", expected_value)
    term = m.group(1) if m else expected_value.strip()
    if not term:
        return None
    return {"term": term, "present": term.lower() in page_text.lower()}


# ---------------------------------------------------------------------------
# Cause -> category mapping
# ---------------------------------------------------------------------------

CAUSE_TO_CATEGORY: dict[str, Optional[str]] = {
    "environment-or-runner-failure": "Environment Issue",
    "network-aborted": "Environment Issue",
    "api-error":       "Potential Functional Issue",
    "ui-value-missing": "Potential Functional Issue",
    "ui-value-present-timing": "Script Issue",
    "console-error":   "Potential Functional Issue",
    "unknown":         "Script Issue",
}

_FOOTER = (
    "Deterministic analysis — no LLM call. "
    "⚠️ Confirm with developer whether this is an expected change. "
    "If yes — update the test. If no — raise a bug ticket. "
    "Manual review recommended before actioning."
)


# ---------------------------------------------------------------------------
# Fix builder
# ---------------------------------------------------------------------------

def build_fix(
    failure,
    cause: str,
    exp: Optional[str],
    act: Optional[str],
    net_error: Optional[str],
    ui: Optional[dict],
) -> str:
    """Derive a fix recommendation from the dominant root-cause signal."""
    feature_file = getattr(failure, "feature_file", "") or ""
    feature = os.path.basename(feature_file).removesuffix(".feature") + ".feature"

    if cause == "network-aborted":
        return (
            f"Re-run once connectivity is stable — multiple requests "
            f"(e.g. `{net_error}`) got no response at all, which points to "
            f"the test runner's network dropping mid-run rather than an app "
            f"bug. If it recurs consistently in this environment, escalate as "
            f"an infra issue."
        )

    if cause == "api-error":
        return (
            f"Investigate the failing API call (`{net_error}`). If the "
            f"backend regressed, raise a bug ticket; if the endpoint was moved "
            f"or renamed, update the page object/test to use the new URL."
        )

    if cause == "ui-value-missing":
        if exp:
            return (
                f"If the change is intentional, update the expected value "
                f"`{exp}` in `{feature}` to match the app (`{act}`); "
                f"if unintended, raise a bug ticket."
            )
        return (
            f"Update the expected value in `{feature}` to match the current "
            f"app, or raise a bug ticket if the element was removed "
            f"unintentionally."
        )

    if cause == "ui-value-present-timing":
        return (
            f"The expected value is present in the DOM, so this is most "
            f"likely a synchronization/locator issue — add or increase an "
            f"explicit wait before the assertion and verify the locator in "
            f"`{feature}`."
        )

    if cause == "console-error":
        return (
            f"Investigate the console error captured during `{feature}`. "
            f"If it is a frontend bug, raise a ticket; otherwise stabilise "
            f"the step and re-run."
        )

    # unknown / fallback
    if exp:
        return (
            f"Compare expected `{exp}` vs actual `{act}` in `{feature}` "
            f"— update the test if the change is intended, otherwise "
            f"raise a bug ticket."
        )
    return (
        f"Review the assertion in `{feature}` against current app behaviour "
        f"— update the test if the change is intended, otherwise raise a "
        f"bug ticket."
    )


# ---------------------------------------------------------------------------
# Core analysis
# ---------------------------------------------------------------------------

def analyze_failure(failure, frequency: Optional[dict] = None) -> dict:
    """Produce ``{"category", "cause", "analysis", "fix"}`` for a single
    failure from its parsed facts.

    Parameters
    ----------
    failure
        Enriched failure object from the parsers (snake_case attributes).
    frequency
        ``{"count": N, "total": M, "lastPassed": "YYYY-MM-DD"}`` for this
        scenario, or an empty dict.
    """
    if frequency is None:
        frequency = {}

    exp = getattr(failure, "expected_value", None)
    act = getattr(failure, "actual_value", None)
    err_msg = getattr(failure, "error_message", "") or ""
    err_line = err_msg.split("\n")[0].strip()

    # Same-run reliability: each scenario gets its own independent Background
    # execution, so `background_reliability` (set in main.py) tells us exactly how
    # often this feature's Background succeeded elsewhere this run. Only force
    # Script Issue when a MAJORITY succeeded — decisive evidence of a flaky timing
    # race. A minority pass rate is a frequently-reproducing problem, not a rare
    # blip, so it falls through to normal analysis with a caveat instead.
    is_background = bool(getattr(failure, "is_background_failure", False))
    reliability = getattr(failure, "background_reliability", None)
    background_note: Optional[str] = None
    if is_background and reliability and reliability.get("total"):
        passed_bg = reliability["passed"]
        total_bg = reliability["total"]
        rate = reliability["rate"]
        failed_step = getattr(failure, "failed_step", "") or err_line
        if rate >= _INTERMITTENT_PASS_RATE_THRESHOLD:
            return {
                "category": "Script Issue",
                "cause": "same-run-intermittent",
                "analysis": (
                    f"The Background/setup step `{failed_step}` failed, but the identical "
                    f"Background PASSED for {passed_bg}/{total_bg} ({rate:.0%}) executions in "
                    f"this same run. Because the same setup steps succeeded elsewhere this run, "
                    f"they are not consistently broken — this is an intermittent/transient "
                    f"(flaky) failure, most likely a timing/wait race, not a functional "
                    f"regression."
                ),
                "fix": (
                    f"Stabilise the Background step `{failed_step}` — add or increase an "
                    f"explicit wait before it (the element/page does appear, since the same "
                    f"step passed {passed_bg}/{total_bg} times this run), then re-run the "
                    f"scenario."
                ),
            }
        elif rate > 0:
            background_note = (
                f"This Background step only passed {passed_bg}/{total_bg} ({rate:.0%}) "
                f"executions in this same run — a FREQUENTLY reproducing failure, not a rare "
                f"blip. Treat with caution: this points to a real environment/performance issue "
                f"on this step rather than a simple timing race. Manual investigation "
                f"recommended before assuming a wait fix will resolve it."
            )
        else:
            background_note = (
                f"This Background step failed in all {total_bg} execution(s) in this same "
                f"run — a consistent, fully-reproducing failure with no passing-sibling "
                f"evidence this run."
            )

    net_error = classify_network_error(failure)
    ui = ui_verdict(failure)
    console_errors = getattr(failure, "console_errors", None) or []
    console_err = console_errors[0] if console_errors else None

    parts: list[str] = []

    # 1. What the test expected vs what it found.
    if exp:
        parts.append(f"The test expected `{exp}` but found `{act}`.")
    elif err_line:
        parts.append(f"The assertion failed with: {err_line}.")

    # 2. Strongest available root-cause signal (ordered by reliability).
    cause: str
    env_failure = classify_environment_or_runner_failure(failure)
    if env_failure:
        parts.append(
            f"The failure signature looks like a runner/navigation/browser-state problem "
            f"(`{env_failure['kind']}`), not a trustworthy reproduction of broken product "
            f"behavior. Static analysis must not classify this as a product bug without a "
            f"clean rerun or live replay reaching the same app state and reproducing it."
        )
        cause = "environment-or-runner-failure"
    elif net_error and net_error["kind"] == "aborted":
        parts.append(
            f"Multiple requests got no response at all (e.g. "
            f"`{net_error['entry']}`), which usually means the test machine's "
            f"network connection dropped mid-run rather than the app actually "
            f"breaking."
        )
        cause = "network-aborted"
    elif net_error and net_error["kind"] == "http_error":
        parts.append(
            f"A failing API call (`{net_error['entry']}`) occurred during the "
            f"scenario, so this is most likely a backend/API or test-data "
            f"problem rather than a UI change."
        )
        cause = "api-error"
    elif ui is not None and ui["present"] is False:
        parts.append(
            f"The expected text `{ui['term']}` is NOT present in the final "
            f"page DOM, so it was most likely removed or renamed in the app."
        )
        cause = "ui-value-missing"
    elif ui is not None and ui["present"] is True:
        parts.append(
            f"The expected text `{ui['term']}` IS present in the final page "
            f"DOM, so this is most likely a timing or locator issue rather "
            f"than a real app change."
        )
        cause = "ui-value-present-timing"
    elif console_err:
        parts.append(
            f"A console error (`{console_err}`) was captured, which may point "
            f"to a frontend/runtime problem affecting the page."
        )
        cause = "console-error"
    else:
        cause = "unknown"

    if background_note:
        parts.append(background_note)

    # 3. History context — recurring vs intermittent vs regression.
    pattern = frequency.get("pattern")
    total = frequency.get("totalRuns", frequency.get("total", 0)) or 0
    count = frequency.get("failedRuns", frequency.get("count", 0)) or 0
    passed = frequency.get("passedRuns", 0) or 0
    last_passed = frequency.get("lastPassed")

    if pattern == "recurring" or (pattern is None and total > 1 and count / total >= 0.5):
        parts.append(
            f"This scenario has failed in {count}/{total} recent runs "
            f"— a recurring, consistent failure (not flaky) that needs priority attention."
        )
    elif pattern == "intermittent":
        parts.append(
            f"This scenario both passed and failed across recent runs "
            f"(failed {count}/{total}, passed {passed}/{total}) — an intermittent/flaky "
            f"result, usually a timing or transient state issue rather than a hard break."
        )
    elif pattern == "regression":
        parts.append(
            f"This scenario passed in {passed}/{total} recent runs and only recently "
            f"started failing"
            + (f" (last passed {last_passed})" if last_passed else "")
            + " — likely a regression introduced by a recent change."
        )
    elif last_passed:
        parts.append(
            f"It last passed on {last_passed}, so the app or test data "
            f"likely changed after that date."
        )
    elif total > 0:
        parts.append("This is the first recorded failure for this scenario.")

    # Thin-history caveat — a pattern verdict on 1–2 prior runs is tentative.
    if pattern in ("recurring", "intermittent", "regression") and frequency.get("confidence") == "low":
        parts.append(
            f"(Low confidence: only {total} prior run(s) recorded — treat this "
            f"pattern as tentative until more runs accumulate.)"
        )

    # 4. Standard reviewer guidance.
    parts.append(_FOOTER)

    category = CAUSE_TO_CATEGORY.get(cause)

    return {
        "category": category,
        "cause": cause,
        "analysis": " ".join(parts),
        "fix": build_fix(failure, cause, exp, act, net_error["entry"] if net_error else None, ui),
    }


# ---------------------------------------------------------------------------
# Batch builder
# ---------------------------------------------------------------------------

def build_deterministic_analyses(
    groups: list,
    frequency_by_scenario: Optional[dict] = None,
) -> dict[str, dict]:
    """Build the ``{scenario_name: {"category", "cause", "analysis", "fix"}}``
    map for all failures in the given groups.

    This is the same shape ``main.py`` builds from parsed LLM output, so it is
    a drop-in replacement for the AI analyses map.
    """
    if frequency_by_scenario is None:
        frequency_by_scenario = {}

    analyses: dict[str, dict] = {}
    for group in groups:
        for failure in group.failures:
            scenario = getattr(failure, "scenario_name", None)
            if scenario is None:
                continue
            freq = frequency_by_scenario.get(scenario, {})
            analyses[scenario] = analyze_failure(failure, freq)
    return analyses
