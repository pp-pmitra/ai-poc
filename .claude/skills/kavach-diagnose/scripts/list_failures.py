"""Mechanical-only failure extraction — no LLM calls, no report generation.

Parses cucumber.json and enriches with Playwright trace data (page URL, DOM
text, call log, screenshot), then writes a plain JSON list of failures. This
is the input for live-replay diagnosis (done by Claude directly against the
running app) instead of `failure_analyzer.main`'s LLM-driven report pipeline.

Usage: python3 list_failures.py [-o OUTPUT_PATH]
"""

from __future__ import annotations

import argparse
import dataclasses
import json
import re
import sys
from pathlib import Path

from failure_analyzer.config import load_config
from failure_analyzer.parsers.cucumber_parser import parse_cucumber_json
from failure_analyzer.parsers.trace_parser import enrich_failures_with_traces
from failure_analyzer.grouping.failure_grouper import group_failures, normalize_step

DEFAULT_OUTPUT = "history/failures-for-replay.json"
DEFAULT_ARTIFACT_DIR = Path("../../../../target/failure-artifacts")

# A Background failure is only worth calling "likely flaky timing" when a
# MAJORITY of this feature's other Background executions succeeded this run.
# Below this rate the step is failing too often to be a rare blip — leave it
# to normal live-replay diagnosis instead of pre-labeling it intermittent.
_INTERMITTENT_PASS_RATE_THRESHOLD = 0.5

# Same threshold, applied to the general (Background-or-not) step-reliability
# signal below: if this exact step passed at least this fraction of the time
# elsewhere in the run, a single failure here reads as a same-run flaky race
# rather than a consistently broken locator.
_STEP_INTERMITTENT_PASS_RATE_THRESHOLD = 0.5


def compute_background_reliability(failures: list, passed_scenarios: list) -> None:
    """Mechanical signal (no LLM): for each Background failure, how often did
    this feature's OTHER Background executions succeed in this same run?
    Each scenario gets its own independent Background run in the Cucumber
    JSON, so this is a real same-run reliability rate, not a guess. Sets
    `background_reliability` = {"passed", "total", "rate"} in place.
    """
    bg_total_by_feature: dict[str, int] = {}
    bg_failed_by_feature: dict[str, int] = {}
    for p in passed_scenarios:
        ff = getattr(p, "feature_file", "") or ""
        bg_total_by_feature[ff] = bg_total_by_feature.get(ff, 0) + 1
    for f in failures:
        ff = f.feature_file or ""
        bg_total_by_feature[ff] = bg_total_by_feature.get(ff, 0) + 1
        if f.is_background_failure:
            bg_failed_by_feature[ff] = bg_failed_by_feature.get(ff, 0) + 1
    for f in failures:
        f.background_reliability = None
        if not f.is_background_failure:
            continue
        ff = f.feature_file or ""
        total_bg = bg_total_by_feature.get(ff, 0)
        failed_bg = bg_failed_by_feature.get(ff, 0)
        passed_bg = total_bg - failed_bg
        f.background_reliability = {
            "passed": passed_bg,
            "total": total_bg,
            "rate": (passed_bg / total_bg) if total_bg else 0.0,
            "likelyIntermittent": total_bg > 0
            and (passed_bg / total_bg) >= _INTERMITTENT_PASS_RATE_THRESHOLD,
        }


def compute_step_reliability(failures: list, passed_scenarios: list) -> None:
    """Mechanical signal (no LLM), generalizing compute_background_reliability
    to EVERY failed step — Background or a regular scenario step alike.

    For each failure, counts how many times this exact step (normalized the
    same way as failure grouping — quoted strings and digits collapsed) shows
    up as PASSED anywhere else in this same run: in any other scenario that
    passed outright, in another failing scenario's own earlier passed steps,
    or in another scenario's Background execution. That's compared against
    how many times it shows up as FAILED (across all failures this run).

    A step that mostly passes elsewhere and fails here is a strong same-run
    signal of an intermittent race, not a consistently broken locator —
    regardless of whether it's a Background step or not. Sets
    `step_reliability` = {"passed", "failed", "rate", "likelyIntermittent"}
    on every failure (None is never set — every failure gets this signal).
    """
    passed_by_step: dict[str, int] = {}

    def _tally_passed(step_trace: list[dict]) -> None:
        for step in step_trace:
            if step.get("status") != "passed":
                continue
            name = f"{step.get('keyword', '').strip()} {step.get('name', '').strip()}".strip()
            key = normalize_step(name)
            passed_by_step[key] = passed_by_step.get(key, 0) + 1

    for p in passed_scenarios:
        _tally_passed(getattr(p, "step_execution_trace", None) or [])
    for f in failures:
        _tally_passed(f.step_execution_trace)
        _tally_passed(getattr(f, "background_step_trace", None) or [])

    failed_by_step: dict[str, int] = {}
    for f in failures:
        key = normalize_step(f.failed_step)
        failed_by_step[key] = failed_by_step.get(key, 0) + 1

    for f in failures:
        key = normalize_step(f.failed_step)
        passed_n = passed_by_step.get(key, 0)
        failed_n = failed_by_step.get(key, 0)
        total = passed_n + failed_n
        f.step_reliability = {
            "passed": passed_n,
            "failed": failed_n,
            "rate": (passed_n / total) if total else 0.0,
            "likelyIntermittent": total > 0
            and (passed_n / total) >= _STEP_INTERMITTENT_PASS_RATE_THRESHOLD,
        }


# Nothing ever cleans or regenerates target/failure-artifacts/ between runs, so
# a directory matching this run's scenario slug may be leftover from an earlier,
# unrelated run. A genuine same-run artifact is written by the same @After hook
# invocation that produces the trace zip, so its mtime should be within seconds
# of the trace's — not hours or days older. Anything older than this buffer is
# treated as stale and dropped rather than silently attached to today's failure.
_STALE_ARTIFACT_BUFFER_SECONDS = 300


def enrich_failures_with_artifacts(failures: list, artifact_base: Path) -> None:
    """Link each failure to its on-disk failure artifacts (page-source.html,
    screenshot.png, failure-context.json) saved by the Java hook.
    Sets `failure_artifacts` dict on each failure where a matching directory
    exists AND the artifact is not stale relative to this run's own trace zip
    (see `_STALE_ARTIFACT_BUFFER_SECONDS`).
    """
    if not artifact_base.exists():
        return
    for f in failures:
        # Strip Cucumber outline suffix "(Example #N)" — the Java hook saves
        # the artifact directory using the base scenario name without it.
        # Also normalize whitespace so double spaces don't produce extra underscores.
        name = re.sub(r"\s*\(Example\s+#\d+\)\s*$", "", f.scenario_name or "", flags=re.IGNORECASE)
        name = re.sub(r"\s+", " ", name).strip()
        slug = name.replace(" ", "_")
        slug = "".join(c if c.isalnum() or c in "._-" else "_" for c in slug)
        artifact_dir = artifact_base / slug
        if not artifact_dir.is_dir():
            continue
        artifacts = {}
        for artifact_name, key in [
            ("failure-context.json", "context"),
            ("page-source.html", "pageSource"),
            ("screenshot.png", "screenshot"),
        ]:
            path = artifact_dir / artifact_name
            if path.exists():
                artifacts[key] = str(path)
        if not artifacts:
            continue

        trace_path = getattr(f, "trace_path", None)
        if trace_path and Path(trace_path).exists():
            trace_mtime = Path(trace_path).stat().st_mtime
            artifact_mtime = max(Path(p).stat().st_mtime for p in artifacts.values())
            if artifact_mtime < trace_mtime - _STALE_ARTIFACT_BUFFER_SECONDS:
                age_hours = (trace_mtime - artifact_mtime) / 3600
                print(
                    f"Warning: failure-artifacts for '{f.scenario_name}' are "
                    f"{age_hours:.1f}h older than this run's trace zip — likely "
                    f"leftover from a previous run. Skipping (not attaching stale "
                    f"page-source.html/screenshot to this failure).",
                    file=sys.stderr,
                )
                continue

        f.failure_artifacts = artifacts


def _error_location_dict(loc) -> dict | None:
    if loc is None:
        return None

    def _method(m):
        return None if m is None else dataclasses.asdict(m)

    def _file(f):
        return None if f is None else dataclasses.asdict(f)

    return {
        "stepDef": _method(loc.step_def),
        "pageObject": _method(loc.page_object),
        "utility": _method(loc.utility),
        "feature": _file(loc.feature),
    }


def failure_to_dict(f) -> dict:
    return {
        "scenarioName": f.scenario_name,
        "featureFile": f.feature_file,
        "tags": f.tags,
        "failedStep": f.failed_step,
        "errorMessage": f.error_message,
        "expectedValue": f.expected_value,
        "actualValue": f.actual_value,
        "errorLocation": _error_location_dict(f.error_location),
        "playwrightCallLog": f.playwright_call_log,
        "isBackgroundFailure": f.is_background_failure,
        "backgroundReliability": f.background_reliability,
        "stepReliability": f.step_reliability,
        "groupId": getattr(f, "group_id", None),
        "tracePath": f.trace_path,
        "lastBrowserAction": f.last_browser_action,
        "pageUrl": f.page_url,
        "pageText": f.page_text,
        "screenshotFile": f.screenshot_file,
        "snapshotAgeMs": getattr(f, "snapshot_age_ms", None),
        "consoleErrors": getattr(f, "console_errors", None),
        "networkErrors": getattr(f, "network_errors", None),
        "networkErrorBodies": getattr(f, "network_error_bodies", None),
        "actionTimeline": getattr(f, "action_timeline", None),
        "failureArtifacts": f.failure_artifacts,
    }


def group_to_dict(g) -> dict:
    return {
        "groupId": g.group_id,
        "rootCauseLabel": g.root_cause_label,
        "normalizedStep": g.normalized_step,
        "affectedFeatures": g.affected_features,
        "scenarioNames": [f.scenario_name for f in g.failures],
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-o", "--output", default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    config = load_config()
    base_dir = Path(__file__).parent
    cucumber_json_path = (base_dir / config.paths.cucumber_json).resolve()
    traces_dir = (base_dir / config.paths.traces_dir).resolve()

    if not cucumber_json_path.exists():
        print(f"Error: cucumber.json not found at:\n  {cucumber_json_path}")
        print("Run your tests first (`mvn test`), then re-run this script.")
        sys.exit(1)

    failures, passed_scenarios, total_scenarios = parse_cucumber_json(str(cucumber_json_path))
    if not failures:
        print("No failures found — all tests passed!")
        return

    enrich_failures_with_traces(
        failures,
        str(traces_dir),
        config.traces.min_match_score,
        config.traces.correlation_buffer_ms,
    )

    artifact_base = (base_dir / DEFAULT_ARTIFACT_DIR).resolve()
    enrich_failures_with_artifacts(failures, artifact_base)

    # Mechanical signal 1: same-run Background reliability (flaky-timing hint,
    # Background steps only)
    compute_background_reliability(failures, passed_scenarios)

    # Mechanical signal 1b: same-run step reliability, generalized to EVERY
    # failed step (Background or a regular scenario step alike) — did this
    # exact step pass elsewhere in this run?
    compute_step_reliability(failures, passed_scenarios)

    # Mechanical signal 2: group near-duplicate failures (same exception,
    # normalized step, locator/action, page object, route, data, network, and
    # crash signatures when available) so live replay can spot-check one per group instead of
    # replaying every near-identical failure individually.
    groups = group_failures(failures)
    for g in groups:
        for f in g.failures:
            f.group_id = g.group_id

    output_path = (base_dir / args.output).resolve()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(
            {
                "totalScenarios": total_scenarios,
                "failedCount": len(failures),
                "failures": [failure_to_dict(f) for f in failures],
                "groups": [group_to_dict(g) for g in groups],
            },
            indent=2,
        )
    )

    print(f"{len(failures)} failure(s) out of {total_scenarios} scenario(s) -> {len(groups)} group(s)")
    intermittent = [
        f.scenario_name
        for f in failures
        if f.background_reliability and f.background_reliability["likelyIntermittent"]
    ]
    if intermittent:
        print(f"  Likely-intermittent Background failures ({len(intermittent)}): {', '.join(intermittent)}")
    step_intermittent = [
        f.scenario_name
        for f in failures
        if f.step_reliability and f.step_reliability["likelyIntermittent"]
    ]
    if step_intermittent:
        print(f"  Likely-intermittent failures, any step ({len(step_intermittent)}): {', '.join(step_intermittent)}")
    multi = [g for g in groups if len(g.failures) > 1]
    if multi:
        print(f"  {len(multi)} group(s) have >1 scenario sharing the same failure signature — consider spot-checking one per group")
    print(f"Written: {output_path}")
    print("No LLM calls made, no report generated — feed this into live-replay diagnosis.")


if __name__ == "__main__":
    main()
