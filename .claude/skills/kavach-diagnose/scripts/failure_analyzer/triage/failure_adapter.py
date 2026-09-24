"""Adapts a `failures-for-replay.json` failure dict (camelCase JSON keys) into
the snake_case attribute shape `failure_analyzer.analyzers.assertion_analyzer`
and `failure_analyzer.main`'s hybrid-strategy helpers already expect, so those
modules can be reused unmodified against the triage pipeline's JSON input
instead of the live `Failure` dataclass they were originally written for.
"""

from __future__ import annotations

from types import SimpleNamespace
from typing import Any


def to_failure_like(failure: dict[str, Any]) -> SimpleNamespace:
    step_reliability = failure.get("stepReliability") or {}
    return SimpleNamespace(
        scenario_name=failure.get("scenarioName"),
        feature_file=failure.get("featureFile"),
        failed_step=failure.get("failedStep"),
        error_message=failure.get("errorMessage"),
        expected_value=failure.get("expectedValue"),
        actual_value=failure.get("actualValue"),
        playwright_call_log=failure.get("playwrightCallLog"),
        last_browser_action=failure.get("lastBrowserAction"),
        page_url=failure.get("pageUrl"),
        page_text=failure.get("pageText"),
        snapshot_age_ms=failure.get("snapshotAgeMs"),
        is_background_failure=bool(failure.get("isBackgroundFailure")),
        background_reliability=failure.get("backgroundReliability"),
        step_reliability=step_reliability,
        network_errors=failure.get("networkErrors") or [],
        console_errors=failure.get("consoleErrors") or [],
        trace_path=failure.get("tracePath"),
        screenshot_name=failure.get("screenshotFile"),
        group_id=failure.get("groupId"),
    )
