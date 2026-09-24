"""Failure history — persists one JSONL record per failure.

Each record contains everything needed for both the existing use cases
(frequency counting, historical context in prompts) and future RAG retrieval
(embedding_text, analysis, fix, category).

Schema
------
failure record:
  record_type   : "failure"
  run_timestamp : ISO-8601
  run_date      : YYYY-MM-DD
  feature       : stem of feature file (e.g. "Life_Line_Item_Creation")
  feature_file  : relative path
  scenario      : scenario name
  error_type    : e.g. "TimeoutError"
  failed_step   : keyword + step name
  failed_locator: XPath or selector (may be empty)
  line_item_type: e.g. "Search Extension" (may be null)
  category      : "Script Issue" | "Potential Functional Issue" | "Environment Issue" | null
  root_cause    : "a"–"e" code (may be null)
  embedding_text: compact failure description optimised for semantic similarity
  analysis      : LLM analysis text (may be null if recorded without LLM)
  fix           : LLM fix text (may be null)

passed record:
  record_type   : "passed"
  run_timestamp, run_date, feature, feature_file, scenario
"""

from __future__ import annotations

import json
import re
from datetime import datetime
from pathlib import Path

from failure_analyzer.grouping.failure_grouper import extract_exception_type, normalize_step

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

HISTORY_DIR = Path(__file__).parent.parent.parent / "history"
HISTORY_FILE = HISTORY_DIR / "failure-history.jsonl"
_LEGACY_FILE = HISTORY_DIR / "failure-history.txt"
MAX_RUNS = 50  # keep up to this many distinct run_dates
MAX_HISTORY_CHARS = 4000  # cap the rendered history block sent into LLM prompts


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _attr(obj, key, default=""):
    if isinstance(obj, dict):
        return obj.get(key, default)
    return getattr(obj, key, default)


def _read_records() -> list[dict]:
    """Load all JSONL records, newest first."""
    if not HISTORY_FILE.exists():
        return []
    records = []
    for line in HISTORY_FILE.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            records.append(json.loads(line))
        except json.JSONDecodeError:
            continue
    # File is written newest-first; return as-is
    return records


def _write_records(records: list[dict]) -> None:
    HISTORY_DIR.mkdir(parents=True, exist_ok=True)
    HISTORY_FILE.write_text(
        "\n".join(json.dumps(r, ensure_ascii=False) for r in records) + "\n",
        encoding="utf-8",
    )


def _extract_locator(failure) -> str:
    """Best-effort: pull a locator from action_timeline or stack trace."""
    timeline = _attr(failure, "action_timeline", None) or []
    for action in timeline:
        failed = action.get("failed", False) if isinstance(action, dict) else getattr(action, "failed", False)
        if failed:
            sel = action.get("selector", "") if isinstance(action, dict) else getattr(action, "selector", "")
            if sel:
                return sel
    return ""


def _build_embedding_text(failure, category: str | None, analysis: str | None) -> str:
    """Compact, semantically rich description of a failure for embedding."""
    error_type = extract_exception_type(_attr(failure, "error_message", "")) or "Error"
    feature = Path(_attr(failure, "feature_file", "")).stem or "Unknown"
    failed_step = _attr(failure, "failed_step", "") or ""
    locator = _extract_locator(failure)
    line_item_type = _attr(failure, "line_item_type", None)

    parts = [f"[{error_type}]"]
    if category:
        parts.append(f"[{category}]")
    parts.append(f"Feature:{feature}")
    if failed_step:
        parts.append(f"Step:{failed_step}")
    if locator:
        parts.append(f"Locator:{locator}")
    if line_item_type:
        parts.append(f"Entity:{line_item_type}")
    if analysis:
        # First sentence captures the core finding
        first = re.split(r"(?<=[.!?])\s", analysis.strip())[0][:300]
        parts.append(f"Finding:{first}")
    return " | ".join(parts)


def _run_fingerprint(failures) -> str:
    items = sorted(
        f"{Path(_attr(f,'feature_file','')).stem}|{_attr(f,'scenario_name','')}"
        for f in failures
    )
    return "::".join(items)


# ---------------------------------------------------------------------------
# Public API  (same signatures as before + optional `analyses` param)
# ---------------------------------------------------------------------------

def append_to_history(
    failures,
    passed_scenarios=None,
    analyses: dict | None = None,
) -> None:
    """Append one run's failures and passed scenarios to the JSONL history.

    ``analyses`` is a dict keyed by scenario_name → {"category", "analysis", "fix"}.
    Pass it when calling from main.py after LLM analysis to store the full record.
    """
    if not failures:
        return

    existing = _read_records()

    # Duplicate detection — skip if identical to the last run
    new_fp = _run_fingerprint(failures)
    last_fp_parts: list[str] = []
    last_ts = None
    for r in existing:
        if r.get("record_type") == "failure":
            ts = r.get("run_timestamp", "")
            if last_ts is None:
                last_ts = ts
            if ts != last_ts:
                break
            last_fp_parts.append(f"{r.get('feature','')}|{r.get('scenario','')}")
    if new_fp and "::".join(sorted(last_fp_parts)) == new_fp:
        print("  [history] Identical to last run — skipping duplicate write.")
        return

    now = datetime.now()
    run_timestamp = now.isoformat(timespec="seconds")
    run_date = now.strftime("%Y-%m-%d")

    new_records: list[dict] = []

    for f in failures:
        feature_file = _attr(f, "feature_file", "")
        feature = Path(feature_file).stem if feature_file else "Unknown"
        scenario = _attr(f, "scenario_name", "")
        error_type = extract_exception_type(_attr(f, "error_message", "")) or "Error"

        ai = (analyses or {}).get(scenario, {})
        category = ai.get("category") or _attr(f, "issue_category", None)
        analysis_text = ai.get("analysis") or None
        fix_text = ai.get("fix") or None

        # root_cause code: extract from category
        root_cause = None
        if category == "Script Issue":
            root_cause = "c/e"
        elif category == "Potential Functional Issue":
            root_cause = "a/b"
        elif category == "Environment Issue":
            root_cause = "d"

        new_records.append({
            "record_type": "failure",
            "run_timestamp": run_timestamp,
            "run_date": run_date,
            "feature": feature,
            "feature_file": feature_file,
            "scenario": scenario,
            "error_type": error_type,
            "failed_step": _attr(f, "failed_step", ""),
            "failed_locator": _extract_locator(f),
            "line_item_type": _attr(f, "line_item_type", None),
            "category": category,
            "root_cause": root_cause,
            "embedding_text": _build_embedding_text(f, category, analysis_text),
            "analysis": analysis_text,
            "fix": fix_text,
        })

    for p in (passed_scenarios or []):
        feature_file = _attr(p, "feature_file", "")
        new_records.append({
            "record_type": "passed",
            "run_timestamp": run_timestamp,
            "run_date": run_date,
            "feature": Path(feature_file).stem if feature_file else "Unknown",
            "feature_file": feature_file,
            "scenario": _attr(p, "scenario_name", ""),
        })

    # Prepend new records and trim to MAX_RUNS distinct dates
    all_records = new_records + existing
    seen_dates: list[str] = []
    for r in all_records:
        d = r.get("run_date", "")
        if d and d not in seen_dates:
            seen_dates.append(d)
    cutoff_date = seen_dates[MAX_RUNS - 1] if len(seen_dates) >= MAX_RUNS else None
    if cutoff_date:
        all_records = [r for r in all_records if r.get("run_date", "") >= cutoff_date]

    _write_records(all_records)


def get_failure_frequency(feature_file_name: str, scenario_name: str) -> dict:
    """Return ``{"count": int, "total": int}`` for a scenario within its feature."""
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]

    records = _read_records()
    feature_run_dates: set[str] = set()
    scenario_run_dates: set[str] = set()

    for r in records:
        if r.get("record_type") != "failure":
            continue
        if r.get("feature", "").lower() != feature_key.lower():
            continue
        d = r.get("run_date", "")
        feature_run_dates.add(d)
        if r.get("scenario", "").strip() == scenario_name.strip():
            scenario_run_dates.add(d)

    # Fall back to legacy txt if JSONL has no data yet
    if not feature_run_dates and _LEGACY_FILE.exists():
        return _legacy_get_failure_frequency(feature_file_name, scenario_name)

    return {"count": len(scenario_run_dates), "total": len(feature_run_dates)}


def get_last_passed_date(feature_file_name: str, scenario_name: str) -> str | None:
    """Return the date of the most recent run where the scenario passed."""
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]

    for r in _read_records():
        if r.get("record_type") != "passed":
            continue
        if r.get("feature", "").lower() != feature_key.lower():
            continue
        if r.get("scenario", "").strip() == scenario_name.strip():
            return r.get("run_date")

    if _LEGACY_FILE.exists():
        return _legacy_get_last_passed_date(feature_file_name, scenario_name)
    return None


# Number of most-recent runs to include in the compact pass/fail sequence.
_RECENT_SEQUENCE_LEN = 5

# Below this many prior runs, a pattern verdict is statistically thin and is
# flagged "low" confidence so the report is honest about limited history.
_MIN_CONFIDENT_RUNS = 3


def get_scenario_run_pattern(
    feature_file_name: str,
    scenario_name: str,
    current_error_type: str | None = None,
    current_failed_step: str | None = None,
) -> dict:
    """Classify a scenario's pass/fail behaviour across recent recorded runs.

    Runs are counted per distinct ``run_timestamp`` (not per ``run_date``), so
    multiple runs on the same day are distinguished — this is what makes an
    intermittent (flaky) result visible instead of collapsing into one day.

    Called for a scenario that is failing in the *current* run. The current run
    is not yet in history when this runs, so the counts describe prior runs only.

    ``current_error_type``/``current_failed_step`` (when both given) scope the
    pattern to the SAME underlying problem: a historical failure record is only
    counted as "FAIL" if its ``error_type`` + normalized ``failed_step`` matches
    the current failure's signature. A scenario that failed twice on one bug and
    is now failing on an unrelated one should read as a fresh issue, not
    "recurring" — the shared scenario name is coincidental, not evidence of the
    same problem. Historical "passed" records always count (a pass is a pass).

    Returns (keys ``count``/``total``/``lastPassed`` kept for back-compat)::

        {
          "count": failedRuns, "total": totalRuns,
          "failedRuns": int, "passedRuns": int, "totalRuns": int,
          "lastPassed": "YYYY-MM-DD" | None, "lastFailed": "YYYY-MM-DD" | None,
          "recentSequence": ["FAIL", "PASS", ...],   # newest -> oldest
          "pattern": "recurring" | "intermittent" | "regression" | "first-failure",
        }

    Pattern (given the current run is a failure):
      - no prior runs        -> "first-failure"
      - prior runs all failed -> "recurring"    (consistent, not flaky)
      - prior runs all passed -> "regression"   (was passing, now broken)
      - prior runs mixed      -> "intermittent" (flaky / transient)
    """
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]

    target = scenario_name.strip()

    current_signature: str | None = None
    if current_error_type and current_failed_step:
        current_signature = f"{current_error_type}|{normalize_step(current_failed_step)}"

    # Status per distinct run, newest first (records are stored newest-first).
    run_status: dict[str, str] = {}   # run_timestamp -> "FAIL" | "PASS"
    run_date_of: dict[str, str] = {}
    order: list[str] = []             # run_timestamps in first-seen (newest) order

    for r in _read_records():
        rtype = r.get("record_type")
        if rtype not in ("failure", "passed"):
            continue
        if r.get("feature", "").lower() != feature_key.lower():
            continue
        if r.get("scenario", "").strip() != target:
            continue
        if rtype == "failure" and current_signature:
            hist_signature = (
                f"{r.get('error_type', '')}|{normalize_step(r.get('failed_step', '') or '')}"
            )
            if hist_signature != current_signature:
                # Different underlying problem — not evidence for or against
                # THIS failure's pattern; exclude this run's failure record.
                continue
        ts = r.get("run_timestamp", "") or r.get("run_date", "")
        if not ts:
            continue
        if ts not in run_status:
            order.append(ts)
            run_status[ts] = "FAIL" if rtype == "failure" else "PASS"
            run_date_of[ts] = r.get("run_date", "")
        elif rtype == "failure":
            # A failure record for a run outweighs a passed one (shouldn't co-occur).
            run_status[ts] = "FAIL"

    statuses = [run_status[ts] for ts in order]   # newest -> oldest
    total_runs = len(order)
    failed_runs = statuses.count("FAIL")
    passed_runs = statuses.count("PASS")
    last_passed = next((run_date_of[ts] for ts in order if run_status[ts] == "PASS"), None)
    last_failed = next((run_date_of[ts] for ts in order if run_status[ts] == "FAIL"), None)

    # Fall back to legacy .txt only when the JSONL has nothing for this scenario.
    if total_runs == 0 and _LEGACY_FILE.exists():
        legacy = _legacy_get_failure_frequency(feature_file_name, scenario_name)
        legacy["lastPassed"] = _legacy_get_last_passed_date(feature_file_name, scenario_name)
        legacy.update(
            failedRuns=legacy.get("count", 0),
            passedRuns=0,
            totalRuns=legacy.get("total", 0),
            lastFailed=None,
            recentSequence=[],
            pattern="first-failure",
            confidence="none",
        )
        return legacy

    if total_runs == 0:
        pattern = "first-failure"
    elif passed_runs == 0:
        pattern = "recurring"
    elif failed_runs == 0:
        pattern = "regression"
    else:
        pattern = "intermittent"

    if total_runs == 0:
        confidence = "none"
    elif total_runs < _MIN_CONFIDENT_RUNS:
        confidence = "low"
    else:
        confidence = "high"

    return {
        "count": failed_runs,
        "total": total_runs,
        "failedRuns": failed_runs,
        "passedRuns": passed_runs,
        "totalRuns": total_runs,
        "lastPassed": last_passed,
        "lastFailed": last_failed,
        "recentSequence": statuses[:_RECENT_SEQUENCE_LEN],
        "pattern": pattern,
        "confidence": confidence,
    }


def get_history_for_feature(feature_file_name: str) -> str:
    """Return recent failure history for a feature as a text block for the LLM prompt.

    Includes category and fix when available (recorded via main.py).
    """
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]

    records = _read_records()
    failures = [
        r for r in records
        if r.get("record_type") == "failure"
        and r.get("feature", "").lower() == feature_key.lower()
    ]

    if not failures:
        if _LEGACY_FILE.exists():
            return _legacy_get_history_for_feature(feature_file_name)
        return ""

    # Group by run_date, most recent first
    by_date: dict[str, list[dict]] = {}
    for r in failures:
        d = r.get("run_date", "unknown")
        by_date.setdefault(d, []).append(r)

    lines: list[str] = []
    for date in sorted(by_date.keys(), reverse=True)[:10]:
        lines.append(f"=== {date} ===")
        for r in by_date[date]:
            lines.append(f"  [{r.get('error_type','Error')}] {r.get('scenario','')}")
            lines.append(f"    Step   : {r.get('failed_step','')}")
            if r.get("failed_locator"):
                lines.append(f"    Locator: {r['failed_locator']}")
            if r.get("line_item_type"):
                lines.append(f"    Entity : {r['line_item_type']}")
            if r.get("category"):
                lines.append(f"    Result : {r['category']}")
            if r.get("fix"):
                lines.append(f"    Prior fix: {r['fix'][:150]}")
            lines.append("")

    text = "\n".join(lines)
    if len(text) > MAX_HISTORY_CHARS:
        text = (
            text[:MAX_HISTORY_CHARS]
            + "\n... (truncated — older runs omitted, see failure-history.jsonl for full history)"
        )
    return text


def get_total_runs() -> int:
    """Total number of distinct run dates recorded."""
    dates = {r.get("run_date") for r in _read_records() if r.get("run_date")}
    if not dates and _LEGACY_FILE.exists():
        return _legacy_get_total_runs()
    return len(dates)


# ---------------------------------------------------------------------------
# Legacy .txt reader (fallback until JSONL has enough data)
# ---------------------------------------------------------------------------

def _legacy_read_runs() -> list[str]:
    if not _LEGACY_FILE.exists():
        return []
    content = _LEGACY_FILE.read_text(encoding="utf-8")
    blocks = re.split(r"(?====\s*RUN:)", content)
    return [b for b in blocks if b.strip()]


def _legacy_get_failure_frequency(feature_file_name: str, scenario_name: str) -> dict:
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]
    pattern = re.compile(
        rf"\[{re.escape(feature_key)}]([\s\S]*?)(?=\n\[|$)", re.IGNORECASE
    )
    feature_runs = 0
    scenario_runs = 0
    target = f"Scenario: {scenario_name.strip()}"
    for run in _legacy_read_runs():
        m = pattern.search(run)
        if not m:
            continue
        feature_runs += 1
        if any(line.strip() == target for line in m.group(1).split("\n")):
            scenario_runs += 1
    return {"count": scenario_runs, "total": feature_runs}


def _legacy_get_last_passed_date(feature_file_name: str, scenario_name: str) -> str | None:
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]
    target = f"{feature_key} :: {scenario_name.strip()}"
    for run in _legacy_read_runs():
        passed_idx = run.find("[PASSED]")
        if passed_idx == -1:
            continue
        if any(line.strip() == target for line in run[passed_idx:].split("\n")):
            m = re.match(r"^=== RUN:\s*(\d{4}-\d{2}-\d{2})", run)
            return m.group(1) if m else None
    return None


def _legacy_get_history_for_feature(feature_file_name: str) -> str:
    feature_key = Path(feature_file_name).stem
    if feature_key.endswith(".feature"):
        feature_key = feature_key[: -len(".feature")]
    pattern = re.compile(
        rf"\[{re.escape(feature_key)}]([\s\S]*?)(?=\n\[|$)", re.IGNORECASE
    )
    blocks: list[str] = []
    for run in _legacy_read_runs():
        header_m = re.match(r"^(=== RUN:.*===)", run)
        header = header_m.group(1) if header_m else ""
        m = pattern.search(run)
        if m:
            blocks.append(f"{header}\n[{feature_key}]{m.group(1)}")
    return "\n".join(blocks)


def _legacy_get_total_runs() -> int:
    return len(_legacy_read_runs())


# ---------------------------------------------------------------------------
# run_fingerprint kept for record_history.py compatibility
# ---------------------------------------------------------------------------

def run_fingerprint(failures) -> str:
    return _run_fingerprint(failures)
