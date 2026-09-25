"""Deterministic (zero-LLM) disposition of a failure, reusing the existing
rule-based `analyze_failure()` classifier rather than reimplementing it.

`disposition()` is the one function callers need: given a failure dict and
its deterministic `cause` (from `analyze_failure`), it decides whether Tier 1
can safely finalize a verdict on its own, or must set `needsLiveReplay: True`
so Tier 2 (live Playwright-MCP replay) picks it up instead.

Hard rule, enforced here and re-checked by `validate_replay_receipts.py`:
this module must never return `confirmed_product_bug` / `suspected_product_bug`
/ `not_reproduced_passed_live` — none of those are provable without a live
replay having actually happened.
"""

from __future__ import annotations

import re
from pathlib import Path
from typing import Any, Optional

from failure_analyzer.analyzers.assertion_analyzer import analyze_failure
from failure_analyzer.packet_utils import resolve_repo_path, strip_file_scheme
from failure_analyzer.triage.failure_adapter import to_failure_like

__all__ = ["classify", "is_probable_copy_diff", "build_copy_diff_fix", "disposition", "extract_locator_text"]


def classify(failure: dict[str, Any], frequency: Optional[dict] = None) -> dict:
    """Thin wrapper around the existing `analyze_failure()` — no new logic."""
    return analyze_failure(to_failure_like(failure), frequency)


def is_probable_copy_diff(expected: Optional[str], actual: Optional[str]) -> bool:
    """True only when expected/actual are the "same" text modulo case/whitespace —
    the one case narrow enough that auto-proposing a feature-file literal update
    can't plausibly be masking a real missing-feature product bug.
    """
    if not expected or not actual:
        return False
    e, a = expected.strip(), actual.strip()
    if not e or not a or e == a:
        return False
    if e.lower() == a.lower():
        return True
    norm = lambda s: re.sub(r"\s+", " ", s).lower()
    return norm(e) == norm(a)


_LINE_SEARCH_RADIUS = 5

# Playwright call-log patterns for locator text extraction.
# Covers the most common Playwright locator APIs used in this repo.
_LOCATOR_TEXT_RE = re.compile(
    r"(?:"
    r"locator\(['\"]text=(?P<a>[^'\"]{2,})['\"]"          # locator('text=Submit')
    r"|getByText\(['\"](?P<b>[^'\"]{2,})['\"]"             # getByText('Submit')
    r"|getByRole\([^,)]+,\s*\{[^}]*name:\s*['\"](?P<c>[^'\"]{2,})['\"]"  # getByRole('button', {name:'Save'})
    r"|getByLabel\(['\"](?P<d>[^'\"]{2,})['\"]"            # getByLabel('Email')
    r"|getByPlaceholder\(['\"](?P<e>[^'\"]{2,})['\"]"      # getByPlaceholder('Enter name')
    r")"
)


def extract_locator_text(playwright_call_log: Optional[str]) -> Optional[str]:
    """Extract the human-readable text argument from the last ~3 Playwright
    locator calls in the call log.  Returns None when nothing parseable is
    found — callers must treat None as "unknown" and escalate rather than
    assuming absence.
    """
    if not playwright_call_log:
        return None
    # Check the last 3 lines; the failing locator call is almost always last.
    lines = [l.strip() for l in playwright_call_log.splitlines() if l.strip()]
    for line in reversed(lines[-3:]):
        m = _LOCATOR_TEXT_RE.search(line)
        if m:
            return next(v for v in m.groupdict().values() if v)
    return None


def build_copy_diff_fix(failure: dict[str, Any], repo_root: Path) -> Optional[dict]:
    """Locate the literal `expectedValue` in the feature file and return a
    concrete, verbatim-appliable `{file, line, before, after}` diff — the one
    fix shape Tier 1 may propose without any live verification, since
    `auto-fix.md` re-runs Maven before ever committing it.

    Requires the recorded `errorLocation.feature.line` and only searches
    within `_LINE_SEARCH_RADIUS` lines of it (a multi-line Examples row can
    shift the literal text slightly, but not far) — never scans the whole
    file. The same expected text can legitimately appear in an earlier,
    unrelated scenario; matching anywhere in the file risks patching that
    scenario instead of the one that actually failed.
    """
    expected = failure.get("expectedValue")
    actual = failure.get("actualValue")
    if not expected or not actual:
        return None
    feature_loc = (failure.get("errorLocation") or {}).get("feature") or {}
    feature_path = feature_loc.get("file") or strip_file_scheme(failure.get("featureFile"))
    recorded_line = feature_loc.get("line")
    if not recorded_line:
        return None
    file_path = resolve_repo_path(repo_root, feature_path)
    if file_path is None or not file_path.exists():
        return None
    try:
        lines = file_path.read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError:
        return None
    start = max(1, int(recorded_line) - _LINE_SEARCH_RADIUS)
    end = min(len(lines), int(recorded_line) + _LINE_SEARCH_RADIUS)
    for idx in range(start, end + 1):
        line = lines[idx - 1]
        if expected in line:
            return {
                "file": strip_file_scheme(feature_path),
                "line": idx,
                "before": line,
                "after": line.replace(expected, actual),
            }
    return None


def disposition(cause: str, failure: dict[str, Any], repo_root: Path) -> dict:
    """Decide whether Tier 1 can finalize this failure, or must escalate.

    Returns a dict with: resolved (bool), verdict, confidence, evidence
    (list[str]), recommendedAction, proposedChange (dict|None), needsLiveReplay.
    `cause == "unknown"` is intentionally not handled here — the caller routes
    those to the batched LLM fallback tier instead.
    """
    scenario = failure.get("scenarioName")

    if cause == "same-run-intermittent":
        reliability = failure.get("backgroundReliability") or {}
        return {
            "resolved": True,
            "verdict": "not_reproduced_intermittent",
            "confidence": "high",
            "evidence": [
                f"Background passed {reliability.get('passed')}/{reliability.get('total')} "
                f"times elsewhere in this same run (rate {reliability.get('rate', 0):.0%})."
            ],
            "recommendedAction": "No action — same-run reliability signal indicates a flaky timing race, not a consistent break.",
            "proposedChange": None,
            "needsLiveReplay": False,
        }

    error_message = failure.get("errorMessage") or ""
    page_text = failure.get("pageText") or ""

    if "strict mode violation" in error_message.lower():
        # Escalates rather than resolves: enforce_tier1_verdict_constraints()
        # (llm_static_triage.py) already requires any script_issue_fix_proposed
        # verdict to carry a proposedChange whose `before` text is mechanically
        # re-verified against the real source file -- this branch has no
        # proposedChange to offer, so triage_workers.py always downgrades a
        # "resolved" result here to needs_investigation anyway. Matching that
        # outcome here (instead of asserting a "resolved" shape the pipeline
        # can never actually honor) keeps this module's own reasoning aligned
        # with offline_dom_analyzer.py's ambiguous_selector handling, which
        # treats the identical underlying symptom (a locator matching more
        # than one element) the same way: a locator matching >1 element is
        # likely too broad, but a product bug rendering duplicate elements
        # cannot be ruled out from static evidence alone -- see
        # offline_dom_analyzer's ambiguous_selector case for the same
        # reasoning applied to a DOM snapshot instead of a Playwright error.
        return {
            "resolved": False,
            "verdict": None,
            "confidence": None,
            "evidence": [
                "Playwright strict-mode violation: the locator matched more than one element.",
                "Likely a too-broad locator, but a product bug rendering duplicate elements "
                "cannot be ruled out without live replay.",
            ],
            "recommendedAction": None,
            "proposedChange": None,
            "needsLiveReplay": True,
        }

    # NOTE: `analyze_failure()`'s real cause vocabulary is
    # environment-or-runner-failure | network-aborted | api-error |
    # ui-value-missing | ui-value-present-timing | console-error | unknown |
    # same-run-intermittent (see CAUSE_TO_CATEGORY in assertion_analyzer.py) —
    # it never produces "timeout" or "assertion-error". The two branches
    # below used to check for those non-existent cause values and were
    # therefore permanently dead code; they're reconciled here with the
    # real cause values that carry the same semantic intent (a locator/
    # assertion timed out on a value that IS present in the DOM — a timing
    # race, not a missing element).
    if cause == "ui-value-present-timing":
        locator_text = extract_locator_text(failure.get("playwrightCallLog"))
        detail = f"locator text {locator_text!r}" if locator_text else "the expected value"
        # ui-value-present-timing is only assigned when analyze_failure()
        # already confirmed the expected text IS present in the captured page
        # snapshot, so there is no "text NOT in DOM" sub-case to branch on
        # here — unlike the dead code this replaces.
        return {
            "resolved": False,
            "verdict": None,
            "confidence": None,
            "evidence": [
                f"Timed out or failed waiting for {detail}, but it IS present in the "
                "captured page snapshot — suggests a timing/visibility race rather than "
                "a missing element.",
            ],
            "recommendedAction": None,
            "proposedChange": None,
            "needsLiveReplay": True,
        }

    # The former dead "assertion-error" branch's logic (escalate when
    # expectedValue is/isn't present in the DOM) is fully superseded by the
    # ui-value-missing handling directly below — that block's own final
    # fallback already returns the equivalent "expected text not found in
    # final page DOM — needs live confirmation" evidence for the not-present
    # case, and analyze_failure() never leaves an ui-value-missing failure's
    # expected text present (that combination is classified as
    # ui-value-present-timing instead, handled above).
    if cause == "ui-value-missing":
        expected = failure.get("expectedValue")
        actual = failure.get("actualValue")
        if is_probable_copy_diff(expected, actual):
            fix = build_copy_diff_fix(failure, repo_root)
            if fix is not None:
                return {
                    "resolved": True,
                    "verdict": "script_issue_fix_proposed",
                    "confidence": "medium",
                    "evidence": [
                        f"Expected `{expected}` vs actual `{actual}` differ only by case/whitespace — "
                        f"a wording/formatting mismatch, not a missing feature.",
                        f"Verified `{expected}` is still present verbatim at {fix['file']}:{fix['line']}.",
                    ],
                    "recommendedAction": f"Update the literal expected text in {fix['file']}:{fix['line']} to match the app.",
                    "proposedChange": fix,
                    "needsLiveReplay": False,
                }
        return {
            "resolved": False,
            "verdict": None,
            "confidence": None,
            "evidence": [f"Expected text `{expected}` not found in final page DOM — needs live confirmation before ruling on product-bug vs. stale locator."],
            "recommendedAction": None,
            "proposedChange": None,
            "needsLiveReplay": True,
        }

    # api-error, ui-value-present-timing, console-error, and anything else:
    # deterministic evidence exists but no diff can be derived without a live
    # session, so always escalate.
    return {
        "resolved": False,
        "verdict": None,
        "confidence": None,
        "evidence": [],
        "recommendedAction": None,
        "proposedChange": None,
        "needsLiveReplay": True,
    }
