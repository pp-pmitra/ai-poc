"""Port of cucumberParser.js — parses Cucumber JSON test reports."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional


# ---------------------------------------------------------------------------
# Dataclasses
# ---------------------------------------------------------------------------

@dataclass
class FileLocation:
    file: str
    line: int


@dataclass
class MethodLocation:
    class_name: str
    method: str
    file: str
    line: int


@dataclass
class ErrorLocation:
    step_def: Optional[MethodLocation] = None
    page_object: Optional[MethodLocation] = None
    utility: Optional[MethodLocation] = None
    feature: Optional[FileLocation] = None


@dataclass
class PassedScenario:
    scenario_name: str
    feature_file: str
    # Full ordered step list: [{status, keyword, name, step_def}] — same shape
    # as Failure.step_execution_trace, captured so step-level pass counts can
    # be computed across the whole run, not just within failed scenarios.
    step_execution_trace: list = field(default_factory=list)


@dataclass
class Failure:
    scenario_name: str
    feature_name: str
    feature_file: str
    tags: list[str]
    failed_step: str
    error_message: str
    stack_trace: str
    expected_value: Optional[str] = None
    actual_value: Optional[str] = None
    error_location: Optional[ErrorLocation] = None
    workspace_type: Optional[str] = None
    line_item_type: Optional[str] = None
    # All line types found in step data tables — used for page-text fallback matching
    candidate_line_item_types: list = field(default_factory=list)
    # Playwright "Call log:" section extracted from the error (locator poll results)
    playwright_call_log: Optional[str] = None
    # Full ordered step list: [{status, keyword, name, step_def}]
    step_execution_trace: list = field(default_factory=list)
    # This scenario's own Background steps (regardless of pass/fail), kept
    # separately from step_execution_trace for non-Background failures so
    # step-reliability tallying sees Background outcomes too. Empty when
    # is_background_failure is True (already covered by step_execution_trace).
    background_step_trace: list = field(default_factory=list)
    # Enriched by trace_parser
    trace_path: Optional[str] = None
    last_browser_action: Optional[str] = None
    page_url: Optional[str] = None
    page_text: Optional[str] = None
    snapshot_age_ms: Optional[float] = None  # ms between best DOM snapshot and failing action
    console_errors: list[str] = field(default_factory=list)
    network_errors: list[str] = field(default_factory=list)
    network_error_bodies: list = field(default_factory=list)
    screenshot_name: Optional[str] = None
    action_timeline: list = field(default_factory=list)
    # Set during analysis
    issue_category: Optional[str] = None
    screenshot_file: Optional[str] = None
    screenshot_abs_path: Optional[str] = None
    # True when this scenario's OWN Background execution failed (its steps never
    # ran). Each scenario gets an independent Background run in the Cucumber JSON
    # (elements alternate background/scenario 1:1), so this is attributed per
    # scenario rather than as one shared feature-level event.
    is_background_failure: bool = False
    # Set in main.py for is_background_failure=True failures: how many of this
    # feature's OTHER Background executions passed this run, out of how many ran.
    # e.g. {"passed": 5, "total": 30, "rate": 0.166...}. None when not applicable.
    background_reliability: Optional[dict] = None
    # Set in list_failures.py for EVERY failure (Background or regular scenario
    # step alike): how many times this exact step (normalized) passed vs failed
    # anywhere else in this same run. e.g. {"passed": 12, "failed": 1, "rate":
    # 0.923, "likelyIntermittent": true}. Unlike background_reliability this
    # isn't restricted to Background steps — it's the general same-run signal.
    step_reliability: Optional[dict] = None
    # Paths to on-disk failure artifacts saved by the Java hook
    failure_artifacts: Optional[dict] = None


# ---------------------------------------------------------------------------
# Workspace / line-item helpers
# ---------------------------------------------------------------------------

_WORKSPACE_QUOTED_RE = re.compile(r'workspace type as "([^"]+)"', re.IGNORECASE)
_WORKSPACE_HCP_RE = re.compile(r'clicks on (HCP Explorer) workspace', re.IGNORECASE)


def _map_steps(steps: list[dict]) -> list[dict]:
    """Map raw Cucumber-JSON step dicts to the {status, keyword, name, step_def}
    shape shared by Failure.step_execution_trace and PassedScenario.step_execution_trace.
    """
    mapped: list[dict] = []
    for step in steps:
        s_result = step.get("result", {})
        mapped.append({
            "status": s_result.get("status", "unknown"),
            "keyword": (step.get("keyword") or "").strip(),
            "name": (step.get("name") or "").strip(),
            "step_def": (step.get("match") or {}).get("location", ""),
        })
    return mapped


def extract_workspace_type(steps: list[dict]) -> Optional[str]:
    """Scan scenario steps for workspace type references (Studio features).

    Patterns recognised:
      - workspace type as "DTC Workspace"
      - clicks on HCP Explorer workspace
    """
    for step in steps:
        name = step.get("name", "")
        m = _WORKSPACE_QUOTED_RE.search(name)
        if m:
            return m.group(1)
        m = _WORKSPACE_HCP_RE.search(name)
        if m:
            return m.group(1)
    return None


def extract_line_item_type(
    steps: list[dict],
    error_message: str,
    stack_trace: str,
    scenario_name: str,
) -> tuple[Optional[str], list[str]]:
    """Look for a data table with a ``line_type`` or ``LINE_TYPE`` column header.

    Returns ``(matched_type, all_candidate_types)``.
    - matched_type: the single type that appears in the error/stack/scenario text,
      or None if ambiguous.
    - all_candidate_types: every type found in the data table (for page-text fallback).
    """
    line_types: list[str] = []

    for step in steps:
        rows = step.get("rows") or step.get("arguments", [])
        if not rows:
            continue

        actual_rows = rows
        if isinstance(rows, list) and rows and isinstance(rows[0], dict) and "rows" in rows[0]:
            actual_rows = rows[0]["rows"]

        if not actual_rows:
            continue

        header_row = actual_rows[0]
        cells = header_row.get("cells", [])
        col_idx: Optional[int] = None
        for idx, cell in enumerate(cells):
            val = cell.get("value", cell) if isinstance(cell, dict) else cell
            if str(val).strip().lower() in ("line_type", "line_item_type"):
                col_idx = idx
                break

        if col_idx is None:
            continue

        for row in actual_rows[1:]:
            row_cells = row.get("cells", [])
            if col_idx < len(row_cells):
                cell = row_cells[col_idx]
                val = cell.get("value", cell) if isinstance(cell, dict) else cell
                val = str(val).strip()
                if val:
                    line_types.append(val)

    if not line_types:
        return None, []

    combined_text = f"{error_message} {stack_trace} {scenario_name}"
    # Use word-boundary regex to avoid "Display" matching "Native Display" or
    # a scenario name like "Verify Display Budget Settings".
    def _matches_word(term: str, text: str) -> bool:
        return bool(re.search(r"(?<!\w)" + re.escape(term) + r"(?!\w)", text))

    matches = [lt for lt in line_types if _matches_word(lt, combined_text)]
    if len(matches) == 1:
        return matches[0], line_types
    if len(set(line_types)) == 1:
        return line_types[0], line_types
    return None, line_types


# ---------------------------------------------------------------------------
# Expected / Actual extraction
# ---------------------------------------------------------------------------

# Ordered list of (pattern, expected_group, actual_group) tuples.
_EA_PATTERNS: list[tuple[re.Pattern, int, int]] = [
    # 1. JUnit 5: expected: <X> but was: <Y>
    (re.compile(r"expected:\s*<(.+?)>\s*but was:\s*<(.+?)>", re.DOTALL), 1, 2),
    # 2. Quoted (possibly multi-line): expected: "X" but was: "Y"
    (re.compile(r'expected:\s*"(.*?)"\s*but was:\s*"(.*?)"', re.DOTALL), 1, 2),
    # 3. Hamcrest: Expected: "X" but: was "Y"
    (re.compile(r'Expected:\s*"(.*?)"\s*but:\s*was\s*"(.*?)"', re.DOTALL), 1, 2),
    # 4. expected [X] but found [Y]
    (re.compile(r"expected\s*\[(.+?)]\s*but found\s*\[(.+?)]", re.DOTALL), 1, 2),
    # 5. AssertJ: expected: "X"\nactual: "Y"
    (re.compile(r'expected:\s*"(.*?)"\s*\n\s*actual:\s*"(.*?)"', re.DOTALL), 1, 2),
    # 6. Expected value 'X' not found for category 'Y'. Found: [...]
    (
        re.compile(
            r"Expected value\s+'([^']+)'\s+not found for category\s+'([^']+)'\.\s*Found:\s*\[([^\]]*)]",
            re.DOTALL,
        ),
        1,
        3,
    ),
    # 7. Simpler: Expected value 'X' not found
    (re.compile(r"Expected value\s+'([^']+)'\s+not found", re.DOTALL), 1, -1),
]


def extract_expected_actual(error_message: str) -> Optional[dict[str, Optional[str]]]:
    """Try 7 regex patterns to extract expected / actual values from an assertion
    error message.  Returns ``{"expected": ..., "actual": ...}`` or ``None``.
    """
    for pattern, exp_grp, act_grp in _EA_PATTERNS:
        m = pattern.search(error_message)
        if m:
            expected = m.group(exp_grp).strip()
            actual = m.group(act_grp).strip() if act_grp > 0 else None
            return {"expected": expected, "actual": actual}
    return None


# ---------------------------------------------------------------------------
# Error-location parsing
# ---------------------------------------------------------------------------

_STEP_DEF_RE = re.compile(
    r"at\s+stepdefinitions\.(\w+)\.(\w+)\(\w+\.java:(\d+)\)"
)
_PAGE_OBJ_RE = re.compile(
    r"at\s+pages\.([.\w]+)\.(\w+)\(\w+\.java:(\d+)\)"
)
_UTILITY_RE = re.compile(
    r"at\s+utils\.(\w+)\.(\w+)\(\w+\.java:(\d+)\)"
)
# U+273D ✽
_FEATURE_RE = re.compile(
    r"✽\..*?\(file:///(.+?\.feature):(\d+)\)"
)


def parse_error_location(full_error: str) -> Optional[ErrorLocation]:
    """Parse stack trace frames into an :class:`ErrorLocation`."""
    loc = ErrorLocation()
    found = False

    m = _STEP_DEF_RE.search(full_error)
    if m:
        class_name = m.group(1)
        method = m.group(2)
        line = int(m.group(3))
        loc.step_def = MethodLocation(
            class_name=class_name,
            method=method,
            file=f"src/test/java/stepdefinitions/{class_name}.java",
            line=line,
        )
        found = True

    m = _PAGE_OBJ_RE.search(full_error)
    if m:
        pkg_class = m.group(1)  # e.g. "life.LineItemDetails"
        method = m.group(2)
        line = int(m.group(3))
        parts = pkg_class.split(".")
        class_name = parts[-1]
        path_segments = "/".join(parts)
        loc.page_object = MethodLocation(
            class_name=class_name,
            method=method,
            file=f"src/main/java/pages/{path_segments}.java",
            line=line,
        )
        found = True

    m = _UTILITY_RE.search(full_error)
    if m:
        class_name = m.group(1)
        method = m.group(2)
        line = int(m.group(3))
        loc.utility = MethodLocation(
            class_name=class_name,
            method=method,
            file=f"src/main/java/utils/{class_name}.java",
            line=line,
        )
        found = True

    m = _FEATURE_RE.search(full_error)
    if m:
        abs_path = m.group(1)
        line = int(m.group(2))
        marker = "src/test/resources/features/"
        idx = abs_path.find(marker)
        if idx != -1:
            rel_path = abs_path[idx:]
        else:
            rel_path = abs_path
        loc.feature = FileLocation(file=rel_path, line=line)
        found = True

    return loc if found else None


# ---------------------------------------------------------------------------
# Error log helpers
# ---------------------------------------------------------------------------

_TEST_FRAME_RE = re.compile(
    r"at (stepdefinitions|pages|hooks|utils|factory)\.",
    re.IGNORECASE,
)


def _extract_call_log(error_lines: list[str]) -> Optional[str]:
    """Extract the Playwright 'Call log:' block from split error lines.

    Returns the call log text, or None if not present.
    """
    collecting = False
    log_lines: list[str] = []
    for line in error_lines:
        if not collecting:
            if line.strip() == "Call log:":
                collecting = True
            continue
        stripped = line.strip()
        # Stop at Java stack frames — marks end of the first call log section.
        # (Caused by: chained exceptions repeat the Call log; we only want the first.)
        if stripped.startswith("at "):
            break
        if not stripped:
            # Allow one blank line inside the block
            if log_lines and log_lines[-1].strip():
                log_lines.append(line)
        else:
            log_lines.append(line)

    result = "\n".join(log_lines).strip()
    return result if result else None


def _build_stack_trace(error_lines: list[str]) -> str:
    """Build a focused stack trace for the LLM.

    Includes:
    - Error header (first ~10 lines up to the closing `}`)
    - Full Playwright Call log section
    - Only the test-relevant Java frames (stepdefinitions, pages, hooks, utils)

    Skips Playwright/JUnit internals to reduce noise.
    """
    header: list[str] = []
    call_log: list[str] = []
    java_test_frames: list[str] = []

    # Collect header (up to closing '}' of the Playwright error object, or until
    # the Call log section starts — whichever comes first).  Stopping at
    # "Call log:" prevents the call log from appearing in BOTH the header and
    # the dedicated call_log list.
    in_header = True
    in_call_log = False
    call_log_done = False  # True after the first call log section ends; prevents re-entry
    for line in error_lines:
        stripped = line.strip()
        if in_header:
            if stripped == "Call log:":
                # Don't include "Call log:" in the header; fall through to
                # the call-log collection below.
                in_header = False
            else:
                header.append(line)
                if stripped == "}":
                    in_header = False
                continue
        # Only start collecting call log for the FIRST occurrence (not Caused-by repeats)
        if not in_call_log and not call_log_done and stripped == "Call log:":
            in_call_log = True
            call_log.append(line)
            continue
        if in_call_log:
            # Stop at Java stack frames (marks end of Playwright call log)
            if stripped.startswith("at "):
                in_call_log = False
                call_log_done = True
                # Fall through to Java stack frame check below
            elif not stripped:
                if call_log and call_log[-1].strip():
                    call_log.append(line)
            else:
                call_log.append(line)
        if not in_call_log and stripped.startswith("at "):
            if _TEST_FRAME_RE.search(stripped):
                java_test_frames.append(line)

    parts = ["\n".join(header)]
    if call_log:
        parts.append("\n".join(call_log))
    if java_test_frames:
        parts.append("\n".join(java_test_frames))

    return "\n".join(parts).strip()


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------

def parse_cucumber_json(
    json_path: str | Path,
) -> tuple[list[Failure], list[PassedScenario], int]:
    """Parse a Cucumber JSON report.

    Returns:
        ``(failures, passed_scenarios, total_scenario_count)``
    """
    path = Path(json_path)
    if not path.exists():
        raise FileNotFoundError(
            f"Cucumber report not found: {path}\n"
            "Run your tests first to generate a cucumber.json report."
        )
    raw = path.read_text(encoding="utf-8").strip()
    if not raw:
        raise ValueError(
            f"Cucumber report is empty: {path}\n"
            "The file exists but has no content — make sure the test run completed."
        )
    try:
        data = json.loads(raw)
    except json.JSONDecodeError as exc:
        raise ValueError(
            f"Cucumber report is not valid JSON: {path}\n"
            f"Parse error at line {exc.lineno}, column {exc.colno}: {exc.msg}"
        ) from exc

    failures: list[Failure] = []
    passed: list[PassedScenario] = []
    total = 0

    for feature in data:
        feature_name = feature.get("name", "")
        feature_file = feature.get("uri", "")

        # Cucumber-JVM's JSON alternates elements 1:1 as
        # [background, scenario, background, scenario, ...] — each scenario gets
        # its OWN independent Background execution immediately before it. Track
        # the immediately-preceding background's failure (if any) and attribute
        # it to exactly the one scenario that follows, rather than assuming a
        # single shared Background failure blocks every later scenario.
        pending_bg_failure: Optional[tuple[dict, list[dict]]] = None
        # Every background's own step list, regardless of pass/fail, kept
        # separately from pending_bg_failure so step-reliability tallying
        # (list_failures.py) can see Background step outcomes for EVERY
        # scenario, not just the ones where the Background itself failed.
        current_bg_steps: list[dict] = []

        for element in feature.get("elements", []):
            if element.get("type") == "background":
                bg_steps = element.get("steps", [])
                current_bg_steps = bg_steps
                bg_failed_obj = next(
                    (s for s in bg_steps if s.get("result", {}).get("status") == "failed"),
                    None,
                )
                pending_bg_failure = (bg_failed_obj, bg_steps) if bg_failed_obj else None
                continue

            total += 1
            scenario_name = element.get("name", "")
            # Scenario Outline: all example rows share the same name. Append the
            # example row index (from the id's trailing ";;N" segment) so failures
            # from different rows are distinguishable throughout the system.
            if element.get("keyword") == "Scenario Outline":
                elem_id = element.get("id", "")
                m = re.search(r";;(\d+)$", elem_id)
                if m:
                    # Cucumber-JVM numbers rows from 2 (header = 1), so subtract 1.
                    row_num = int(m.group(1)) - 1
                    scenario_name = f"{scenario_name} (Example #{row_num})"
            tags = [t.get("name", "") for t in element.get("tags", [])]
            steps = element.get("steps", [])

            is_background_failure = False
            bg_trace_steps: list[dict] = []

            if pending_bg_failure is not None:
                # This scenario's own Background failed — its own steps never
                # ran (all "skipped"), so the failure IS the background step.
                failed_step_obj, bg_trace_steps = pending_bg_failure
                is_background_failure = True
                pending_bg_failure = None  # consumed; applies to this scenario only
            else:
                # Look for the first failed step in the scenario's own steps
                failed_step_obj = None
                for step in steps:
                    result = step.get("result", {})
                    if result.get("status") == "failed":
                        failed_step_obj = step
                        break

                # @Before/@After hook failures: all steps get status "skipped" and
                # the error lives in element["before"] / element["after"] arrays.
                if failed_step_obj is None:
                    for hook_list_key in ("before", "after"):
                        for hook in element.get(hook_list_key, []):
                            if hook.get("result", {}).get("status") == "failed":
                                # Synthesise a minimal step-like object so the rest
                                # of the parsing pipeline can continue unchanged.
                                failed_step_obj = {
                                    "keyword": hook_list_key.capitalize(),
                                    "name": f"hook ({hook.get('match', {}).get('location', 'unknown')})",
                                    "result": hook["result"],
                                }
                                break
                        if failed_step_obj:
                            break

            if failed_step_obj is None:
                # Prepend this scenario's own (passed) Background steps so
                # step-reliability tallying sees every Background execution,
                # not just the ones attached to a failure.
                passed_step_trace = _map_steps(current_bg_steps) + _map_steps(steps)
                passed.append(PassedScenario(
                    scenario_name=scenario_name,
                    feature_file=feature_file,
                    step_execution_trace=passed_step_trace,
                ))
                continue

            # Step keyword + name
            keyword = (failed_step_obj.get("keyword") or "").strip()
            step_name = (failed_step_obj.get("name") or "").strip()
            failed_step = f"{keyword} {step_name}".strip()

            # Full error for location parsing
            full_error = failed_step_obj.get("result", {}).get("error_message", "")

            error_lines = full_error.splitlines()
            error_message = "\n".join(error_lines[:3])

            # Extract Playwright call log (lines after "Call log:" up to blank)
            playwright_call_log = _extract_call_log(error_lines)

            # Stack trace: error header + call log + test-relevant Java frames
            stack_trace = _build_stack_trace(error_lines)

            # Expected / actual
            combined = f"{error_message}\n{stack_trace}"
            ea = extract_expected_actual(combined)
            expected_value = ea["expected"] if ea else None
            actual_value = ea["actual"] if ea else None

            # Error location — use the FULL error, not the truncated version
            error_location = parse_error_location(full_error)

            # Workspace & line-item type — read from the scenario's OWN steps
            # (present in the JSON with their Gherkin text/data tables even when
            # "skipped" because the Background blocked them).
            workspace_type = extract_workspace_type(steps)
            line_item_type, candidate_line_item_types = extract_line_item_type(
                steps, error_message, stack_trace, scenario_name,
            )

            # Build full step execution trace with status + step def location.
            # When the Background failed, show its steps (what actually ran);
            # the scenario's own steps never started.
            trace_source = bg_trace_steps if is_background_failure else steps
            step_trace = _map_steps(trace_source)
            # Separately, this scenario's own Background steps regardless of
            # pass/fail — kept apart from step_trace (which stays the existing
            # "what actually ran for this failure" shape) so step-reliability
            # tallying can see Background outcomes even for non-Background
            # failures, without changing step_trace's established meaning.
            background_step_trace = [] if is_background_failure else _map_steps(current_bg_steps)

            failures.append(Failure(
                scenario_name=scenario_name,
                feature_name=feature_name,
                feature_file=feature_file,
                tags=tags,
                failed_step=failed_step,
                error_message=error_message,
                stack_trace=stack_trace,
                playwright_call_log=playwright_call_log,
                expected_value=expected_value,
                actual_value=actual_value,
                error_location=error_location,
                workspace_type=workspace_type,
                line_item_type=line_item_type,
                candidate_line_item_types=candidate_line_item_types,
                step_execution_trace=step_trace,
                background_step_trace=background_step_trace,
                is_background_failure=is_background_failure,
            ))

    return failures, passed, total
