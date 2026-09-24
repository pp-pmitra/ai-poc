"""Port of traceParser.js — parses Playwright trace zip files and enriches
failure objects with browser context."""

from __future__ import annotations

import base64
import json
import re
import zipfile
from pathlib import Path
from typing import Any, Optional, Union

from .trace_correlator import correlate_failure_context, format_action_timeline, is_telemetry


# ---------------------------------------------------------------------------
# Snapshot rendering
# ---------------------------------------------------------------------------

_SKIP_TAGS: set[str] = {"SCRIPT", "STYLE", "NOSCRIPT", "svg", "SVG", "HEAD"}

# Attributes whose values carry visible or semantically meaningful text.
# Included so the UI Check can match locators that use aria-label, title, or id.
_TEXT_ATTRS: set[str] = {"aria-label", "title", "id", "placeholder", "alt"}


def render_snapshot_text(node: Any, out: list[str], _depth: int = 0) -> None:
    """Recursively render a Playwright frame-snapshot node tree to plain text.

    Playwright stores snapshots as `[tagName, attrsDict, ...children]` lists.
    Numeric pairs like `[44, 0]` are compressed resource references (CSS/JS) —
    skip them. Tag names at index-0 are also skipped; only inline string children
    (text nodes) and selected attribute values are emitted.

    *out* accumulates text fragments.
    """
    if node is None or _depth > 80:
        return

    # Pure text node — bare string that is NOT the tag-name of a parent
    if isinstance(node, str):
        stripped = node.strip()
        if stripped:
            out.append(stripped)
        return

    if isinstance(node, list):
        if not node:
            return
        # Numeric pair [int, int] = compressed resource reference — skip entirely
        if len(node) == 2 and isinstance(node[0], int) and isinstance(node[1], int):
            return
        # Element node: [tagName, attrsDict?, ...children]
        tag = node[0] if isinstance(node[0], str) else ""
        if tag.upper() in _SKIP_TAGS:
            return
        # Emit selected attribute values so the UI Check sees aria-label, id, etc.
        start = 1
        if len(node) > 1 and isinstance(node[1], dict):
            attrs = node[1]
            for attr in _TEXT_ATTRS:
                val = attrs.get(attr, "").strip()
                if val:
                    out.append(val)
            start = 2
        for child in node[start:]:
            render_snapshot_text(child, out, _depth + 1)
        return

    # Dict node — may wrap children list
    if isinstance(node, dict):
        for child in node.get("children", []):
            render_snapshot_text(child, out, _depth + 1)


# ---------------------------------------------------------------------------
# Network event parsing
# ---------------------------------------------------------------------------

def parse_network_events(zip_obj: zipfile.ZipFile) -> list[dict]:
    """Read ``trace.network`` from *zip_obj* (newline-delimited JSON) and
    return dicts for failed/errored requests.

    Each dict has: status, method, path (pathname+search), response_body (4xx/5xx only), timestamp.
    """
    from urllib.parse import urlparse as _urlparse

    events: list[dict] = []
    try:
        raw = zip_obj.read("trace.network").decode("utf-8", errors="replace")
    except KeyError:
        return events

    for line in raw.splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            entry = json.loads(line)
        except json.JSONDecodeError:
            continue

        if entry.get("type") != "resource-snapshot":
            continue

        snapshot = entry.get("snapshot", {})
        response = snapshot.get("response", {})
        request = snapshot.get("request", {})

        status = response.get("status")
        if status is None:
            continue
        if status != -1 and status < 400:
            continue

        raw_url = request.get("url", "")
        try:
            from urllib.parse import parse_qs as _parse_qs, unquote as _unquote
            parsed = _urlparse(raw_url)
            url_path = parsed.path + (("?" + parsed.query) if parsed.query else "")
            # Normalize BuyerProxy.ashx URLs: extract the `u=` sub-endpoint so
            # cache-busting `_=` timestamps don't prevent deduplication.
            if ("BuyerProxy" in parsed.path or "BuyerProxySSI" in parsed.path) and parsed.query:
                qs = _parse_qs(parsed.query)
                u_vals = qs.get("u", [])
                if u_vals:
                    proxy_base = "BuyerProxySSI.ashx" if "BuyerProxySSI" in parsed.path else "BuyerProxy.ashx"
                    url_path = f"{proxy_base}?{_unquote(u_vals[0])}"
        except Exception:
            url_path = raw_url

        if is_telemetry(url_path):
            continue

        method = request.get("method", "GET")

        response_body: str | None = None
        if status >= 400:
            body_raw = response.get("body") or response.get("_body") or ""
            if body_raw:
                text = body_raw.decode("utf-8", errors="replace") if isinstance(body_raw, bytes) else str(body_raw)
                response_body = text[:300] + ("…" if len(text) > 300 else "")

        events.append({
            "status": status,
            "method": method,
            "path": url_path,
            "response_body": response_body,
            "timestamp": snapshot.get("_monotonicTime", 0),
        })

    return events


# ---------------------------------------------------------------------------
# Trace parsing
# ---------------------------------------------------------------------------

def parse_trace(trace_zip_path: str | Path) -> dict:
    """Open a Playwright trace zip and extract structured data.

    Returns a dict with keys:
        - ``actions``
        - ``consoleEvents``
        - ``networkEvents``
        - ``pageUrl``
        - ``pageText``
        - ``lastScreenshotName``
        - ``lastBrowserAction``
    """
    trace_zip_path = Path(trace_zip_path)

    actions: list[dict] = []
    console_events: list[dict] = []
    network_events: list[dict] = []
    page_url: Optional[str] = None
    page_text: Optional[str] = None
    snapshot_age_ms: Optional[float] = None  # ms between best snapshot and failing action
    last_screenshot_name: Optional[str] = None
    last_browser_action: Optional[str] = None
    # All frame snapshots (main + iframes) for page-text extraction at failure point
    all_frame_snapshots: list[tuple[float, Any]] = []
    # (timestamp, frameUrl) pairs from main-frame snapshots — for URL-at-failure lookup
    url_timeline: list[tuple[float, str]] = []

    with zipfile.ZipFile(trace_zip_path, "r") as zf:
        # Collect screenshot names from the zip index
        screenshot_candidates: list[str] = []
        for name in zf.namelist():
            if re.match(r"resources/page@.*\.jpeg", name):
                screenshot_candidates.append(name)
        if screenshot_candidates:
            # Sort by name — timestamp is embedded, so lexicographic order = chronological
            last_screenshot_name = sorted(screenshot_candidates)[-1]

        # --- Parse trace.trace (newline-delimited JSON) ---
        try:
            trace_raw = zf.read("trace.trace").decode("utf-8", errors="replace")
        except KeyError:
            trace_raw = ""

        # before/after pairs: callId → pending before data
        calls_by_id: dict[str, dict] = {}
        # tracingGroup boundaries: each step becomes {callId, name, startTime, endTime}
        step_groups: list[dict] = []
        # Track the last known monotonic time to approximate console event timestamps
        last_known_time: float = 0.0

        for raw_line in trace_raw.splitlines():
            raw_line = raw_line.strip()
            if not raw_line:
                continue
            try:
                event = json.loads(raw_line)
            except json.JSONDecodeError:
                continue

            event_type = event.get("type", "")

            if event_type == "before":
                call_id = event.get("callId", "")
                api_name = event.get("apiName", "")
                method = event.get("method", "")
                params = event.get("params", {})
                t = event.get("startTime", 0)
                if t:
                    last_known_time = t

                if method == "tracingGroup":
                    # Gherkin step boundary marker
                    step_groups.append({
                        "callId": call_id,
                        "name": api_name,
                        "startTime": t,
                        "endTime": None,
                    })
                elif api_name:
                    calls_by_id[call_id] = {
                        "apiName": api_name,
                        "startTime": t,
                        "params": params,
                    }
                    if "navigate" in api_name.lower() and params.get("url"):
                        page_url = params["url"]

            elif event_type == "after":
                call_id = event.get("callId", "")
                end_time = event.get("endTime", 0)
                if end_time:
                    last_known_time = end_time

                # Close matching step group
                for sg in step_groups:
                    if sg["callId"] == call_id and sg["endTime"] is None:
                        sg["endTime"] = end_time
                        break

                # Complete pending action
                if call_id in calls_by_id:
                    before = calls_by_id.pop(call_id)
                    err = event.get("error")
                    error_msg = ""
                    if err:
                        if isinstance(err, dict):
                            error_msg = err.get("message", "")
                        else:
                            error_msg = str(err)

                    api_name = before["apiName"]
                    params = before["params"]
                    action_entry: dict = {
                        "apiName": api_name,
                        "selector": params.get("selector", ""),
                        "startTime": before["startTime"],
                        "endTime": end_time or before["startTime"],
                        "error": error_msg,
                        "stepName": "",
                        "params": params,
                    }
                    actions.append(action_entry)
                    last_browser_action = api_name

            elif event_type == "console":
                msg_type = event.get("messageType", "")
                if msg_type in ("error", "warning"):
                    # Use explicit timestamp if present, otherwise infer from stream position
                    ts = event.get("timestamp", event.get("time", 0)) or last_known_time
                    console_events.append({
                        "text": event.get("text", ""),
                        "messageType": msg_type,
                        "timestamp": ts,
                    })

            # Collect frame snapshots for page-text extraction and URL timeline
            if event_type == "frame-snapshot":
                snap = event.get("snapshot", {})
                snap_ts = float(snap.get("timestamp", 0) or snap.get("wallTime", 0))
                if snap.get("html"):
                    all_frame_snapshots.append((snap_ts, snap["html"]))
                if snap.get("isMainFrame"):
                    frame_url = snap.get("frameUrl", "")
                    if frame_url and snap_ts:
                        url_timeline.append((snap_ts, frame_url))

        # Assign Gherkin step names to actions based on tracingGroup time windows
        for action in actions:
            act_start = action["startTime"]
            for sg in step_groups:
                sg_start = sg["startTime"]
                sg_end = sg["endTime"]
                if act_start >= sg_start and (sg_end is None or act_start <= sg_end):
                    action["stepName"] = sg["name"]
                    break

        # --- Network events from trace.network (canonical source) ---
        for evt in parse_network_events(zf):
            network_events.append({
                "status": evt["status"],
                "method": evt["method"],
                "url": evt["path"],
                "path": evt["path"],
                "response_body": evt.get("response_body"),
                "timestamp": evt.get("timestamp", 0),
            })

        # --- Render page text from the last non-empty snapshot before failure ---
        # Walk candidates from most-recent to oldest; use the first snapshot that
        # renders to non-empty text, then also fold in any other frames whose
        # timestamps are within 100 ms (catches simultaneous iframe snapshots).
        if all_frame_snapshots:
            from .trace_correlator import find_failing_action_index
            failing_idx, _ = find_failing_action_index(actions)
            fail_time = actions[failing_idx].get("startTime", 0) if failing_idx >= 0 and actions else 0

            # All snapshots at or before the failing action (fall back to all if none match)
            candidates = [
                (ts, html) for ts, html in all_frame_snapshots
                if not fail_time or ts <= fail_time
            ] or all_frame_snapshots

            # Walk newest → oldest to find the most recent snapshot with actual content.
            # Snapshots nearest failure are often delta-compressed (render to empty).
            anchor_ts: Optional[float] = None
            anchor_parts: list[str] = []
            for ts, html in sorted(candidates, key=lambda x: x[0], reverse=True):
                out: list[str] = []
                render_snapshot_text(html, out)
                if " ".join(out).strip():
                    anchor_ts = ts
                    anchor_parts = out
                    break

            if anchor_ts is not None:
                # Collect all other frames within 100 ms of the anchor
                # (covers simultaneous iframe snapshots when they exist).
                for ts, html in candidates:
                    if ts == anchor_ts:
                        continue
                    if abs(ts - anchor_ts) <= 100:
                        out = []
                        render_snapshot_text(html, out)
                        anchor_parts.extend(out)
                combined = " ".join(anchor_parts)
                if combined.strip():
                    page_text = combined[:6000]
                    if len(combined) > 6000:
                        page_text += "\n…(truncated)"
                    # How stale is this snapshot relative to the failing action?
                    # Large values mean the page may have changed since the snapshot.
                    snapshot_age_ms = (fail_time - anchor_ts) if fail_time else 0

    # Resolve page_url to the URL active at the failing action's time.
    # frame-snapshot frameUrl entries are more accurate than navigate params
    # for SPA apps where Angular routing changes the URL without a navigate event.
    if url_timeline and actions:
        from .trace_correlator import find_failing_action_index
        failing_idx, _ = find_failing_action_index(actions)
        if failing_idx >= 0:
            fail_time = actions[failing_idx].get("startTime", 0)
            # Sort ascending by timestamp before scanning — trace events may not
            # be monotonically ordered (async snapshot flushes).
            url_timeline.sort(key=lambda x: x[0])
            # Pick the most recent snapshot URL taken at or before the failing action
            best_url = None
            for ts, url in url_timeline:
                if ts <= fail_time:
                    best_url = url
                else:
                    break
            if best_url:
                page_url = best_url
        elif url_timeline:
            # fallback: last known URL
            page_url = url_timeline[-1][1]

    return {
        "actions": actions,
        "consoleEvents": console_events,
        "networkEvents": network_events,
        "pageUrl": page_url,
        "pageText": page_text,
        "snapshotAgeMs": snapshot_age_ms,
        "lastScreenshotName": last_screenshot_name,
        "lastBrowserAction": last_browser_action,
    }


# ---------------------------------------------------------------------------
# Screenshot helpers
# ---------------------------------------------------------------------------

def extract_screenshot_base64(
    trace_zip_path: str | Path,
    screenshot_name: str,
) -> Optional[str]:
    """Extract a screenshot from the trace zip and return it as a
    base64-encoded string."""
    try:
        with zipfile.ZipFile(trace_zip_path, "r") as zf:
            data = zf.read(screenshot_name)
            return base64.b64encode(data).decode("ascii")
    except (KeyError, zipfile.BadZipFile, FileNotFoundError):
        return None


def save_screenshot(
    trace_zip_path: str | Path,
    screenshot_name: str,
    dest_path: str | Path,
) -> bool:
    """Save a screenshot from the trace zip to *dest_path*.

    Returns ``True`` on success.
    """
    try:
        with zipfile.ZipFile(trace_zip_path, "r") as zf:
            data = zf.read(screenshot_name)
        dest = Path(dest_path)
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)
        return True
    except (KeyError, zipfile.BadZipFile, FileNotFoundError):
        return False


# ---------------------------------------------------------------------------
# Fuzzy matching helpers
# ---------------------------------------------------------------------------

_NON_ALNUM_RE = re.compile(r"[^a-z0-9]+")


def slugify(s: str) -> str:
    """Lowercase, replace non-alphanumeric runs with hyphens, strip."""
    return _NON_ALNUM_RE.sub("-", s.lower()).strip("-")


def token_set(slug: str) -> set[str]:
    """Split a slug on hyphens into a set of tokens."""
    return {t for t in slug.split("-") if t}


def similarity_score(a: str, b: str) -> float:
    """Jaccard similarity between token sets of two strings."""
    set_a = token_set(slugify(a))
    set_b = token_set(slugify(b))
    if not set_a or not set_b:
        return 0.0
    intersection = set_a & set_b
    union = set_a | set_b
    return len(intersection) / len(union)


# ---------------------------------------------------------------------------
# Setter helper for dataclass / dict interop
# ---------------------------------------------------------------------------

def _set(obj: Any, key: str, value: Any) -> None:
    """Set *key* on *obj* using ``setattr`` for dataclasses or item-assignment
    for dicts."""
    if isinstance(obj, dict):
        obj[key] = value
    else:
        setattr(obj, key, value)


# ---------------------------------------------------------------------------
# Main enrichment
# ---------------------------------------------------------------------------

def enrich_failures_with_traces(
    failures: list,
    traces_dir: str | Path,
    min_match_score: float = 0.4,
    correlation_buffer_ms: int = 2000,
) -> None:
    """Match each failure to its best-matching trace zip file and enrich it
    with browser context.

    Modifies the *failures* list in place.
    """
    traces_path = Path(traces_dir)
    if not traces_path.is_dir():
        return

    # Collect all trace zip files
    trace_files: list[Path] = sorted(traces_path.rglob("*.zip"))
    if not trace_files:
        return

    # Pre-compute slugs for trace file stems
    trace_slugs: list[str] = [slugify(tf.stem) for tf in trace_files]

    for failure in failures:
        scenario_name = (
            failure.scenario_name
            if hasattr(failure, "scenario_name")
            else failure.get("scenario_name", "")
        )
        if not scenario_name:
            continue

        # Find best matching trace by Jaccard similarity
        best_score = 0.0
        best_idx = -1
        for i, t_slug in enumerate(trace_slugs):
            score = similarity_score(scenario_name, trace_files[i].stem)
            if score > best_score:
                best_score = score
                best_idx = i

        if best_score < min_match_score or best_idx < 0:
            continue

        best_trace = trace_files[best_idx]

        # Parse the trace. A malformed/unreadable zip must not abort the whole
        # run, but it also must not be silent — a dropped trace means all browser
        # evidence for this failure is missing, which skews later classification.
        try:
            trace_data = parse_trace(best_trace)
        except Exception as exc:
            print(
                f'  [trace] WARNING: could not parse trace for "{scenario_name}" '
                f"({best_trace.name}): {exc} — continuing without trace evidence"
            )
            continue

        actions = trace_data.get("actions", [])
        console_events = trace_data.get("consoleEvents", [])
        network_events = trace_data.get("networkEvents", [])

        # Correlate
        correlation = correlate_failure_context(
            actions,
            console_events,
            network_events,
            buffer_ms=correlation_buffer_ms,
        )

        # Action timeline
        timeline = format_action_timeline(
            actions,
            correlation["failingActionIndex"],
            correlation["failureInferred"],
        )

        # Enrich the failure object
        _set(failure, "trace_path", str(best_trace))
        _set(failure, "page_url", trace_data.get("pageUrl"))
        _set(failure, "screenshot_name", trace_data.get("lastScreenshotName"))
        _set(failure, "page_text", trace_data.get("pageText"))
        _set(failure, "snapshot_age_ms", trace_data.get("snapshotAgeMs"))
        _set(failure, "console_errors", correlation["relevantConsoleErrors"])
        _set(failure, "network_errors", correlation["relevantNetworkErrors"])
        _set(failure, "network_error_bodies", correlation.get("relevantNetworkErrorBodies", []))
        _set(failure, "action_timeline", timeline)
        _set(failure, "last_browser_action", trace_data.get("lastBrowserAction"))

        # Page-text fallback for line_item_type: if not resolved at parse time,
        # check which candidate types appear in the page text (the page was on
        # that entity's detail view when it failed).
        def _get(obj, key):
            return obj.get(key) if isinstance(obj, dict) else getattr(obj, key, None)

        if not _get(failure, "line_item_type"):
            candidates = _get(failure, "candidate_line_item_types") or []
            page_text = trace_data.get("pageText") or ""
            if candidates and page_text:
                matches = [c for c in candidates if c.lower() in page_text.lower()]
                unique = list(dict.fromkeys(matches))  # preserve order, deduplicate
                if len(unique) == 1:
                    _set(failure, "line_item_type", unique[0])
