"""Port of traceCorrelator.js — correlates console/network events to failing
Playwright actions."""

from __future__ import annotations

import re
from typing import Optional


# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

TELEMETRY_PATH_PATTERNS: list[re.Pattern] = [
    re.compile(r"/collect", re.IGNORECASE),
    re.compile(r"/events", re.IGNORECASE),
    re.compile(r"/beacon", re.IGNORECASE),
    re.compile(r"/analytics", re.IGNORECASE),
    re.compile(r"/tracking", re.IGNORECASE),
    re.compile(r"/telemetry", re.IGNORECASE),
    re.compile(r"/pixel", re.IGNORECASE),
    re.compile(r"/impression", re.IGNORECASE),
    re.compile(r"/log", re.IGNORECASE),
    re.compile(r"/heartbeat", re.IGNORECASE),
    re.compile(r"\.(js|css|woff2?|png|gif|ico|svg)(\?|$)", re.IGNORECASE),
    re.compile(r"sentry_key=", re.IGNORECASE),
    re.compile(r"/envelope/", re.IGNORECASE),
]

NON_SUBSTANTIVE_ACTIONS: set[str] = {
    "Page.screenshot",
    "Tracing.stop",
    "Tracing.stopChunk",
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def is_telemetry(url_path: str) -> bool:
    """Return ``True`` if *url_path* matches a known telemetry / static-asset
    pattern."""
    for pattern in TELEMETRY_PATH_PATTERNS:
        if pattern.search(url_path):
            return True
    return False


def find_failing_action_index(
    actions: list[dict],
) -> tuple[int, bool]:
    """Find the index of the failing action.

    Returns:
        ``(index, inferred)`` where *inferred* is ``True`` when no explicit
        error was found and the last substantive action is used instead.
    """
    # First pass: look for an action that carries an explicit error
    for i, action in enumerate(actions):
        if action.get("error"):
            return i, False

    # Second pass: last substantive (non-screenshot / non-tracing) action
    for i in range(len(actions) - 1, -1, -1):
        api_name = actions[i].get("apiName", "")
        if api_name not in NON_SUBSTANTIVE_ACTIONS:
            return i, True

    # Fallback: last action
    if actions:
        return len(actions) - 1, True
    return -1, True


# ---------------------------------------------------------------------------
# Correlation
# ---------------------------------------------------------------------------

def correlate_failure_context(
    actions: list[dict],
    console_events: list[dict],
    network_events: list[dict],
    buffer_ms: int = 2000,
) -> dict:
    """Correlate console/network events to the time window around the failing
    action.

    Returns a dict with keys:
        - ``failingActionIndex``
        - ``failureInferred``
        - ``relevantConsoleErrors``  (list of strings)
        - ``relevantNetworkErrors``  (list of formatted strings)
    """
    if not actions:
        return {
            "failingActionIndex": -1,
            "failureInferred": True,
            "relevantConsoleErrors": [],
            "relevantNetworkErrors": [],
        }

    failing_idx, inferred = find_failing_action_index(actions)

    if failing_idx < 0:
        return {
            "failingActionIndex": -1,
            "failureInferred": inferred,
            "relevantConsoleErrors": [],
            "relevantNetworkErrors": [],
        }

    failing_action = actions[failing_idx]
    start_time = failing_action.get("startTime", 0)
    window_start = start_time - buffer_ms
    window_end = start_time + buffer_ms

    # --- Console errors in window ---
    windowed_console: list[str] = []
    for evt in console_events:
        ts = evt.get("timestamp", evt.get("time", 0))
        if window_start <= ts <= window_end:
            text = evt.get("text", evt.get("message", ""))
            if text:
                windowed_console.append(str(text))

    # --- Network errors in window ---
    windowed_network: list[str] = []
    seen_net_paths: set[str] = set()
    for evt in network_events:
        ts = evt.get("timestamp", evt.get("time", 0))
        status = evt.get("status", 0)
        if window_start <= ts <= window_end:
            if status == -1 or status == 0 or status >= 400:
                url_path = evt.get("url", evt.get("path", ""))
                if not is_telemetry(url_path) and url_path not in seen_net_paths:
                    seen_net_paths.add(url_path)
                    method = evt.get("method", "GET")
                    windowed_network.append(f"{status} {method} {url_path}")
                    if len(windowed_network) >= 10:
                        break

    # Fallback: if no in-window console errors, surface the first unique one
    # with a marker so the LLM knows to treat it as weak/circumstantial evidence.
    if not windowed_console:
        seen_text: set[str] = set()
        for evt in console_events:
            text = evt.get("text", evt.get("message", ""))
            if text and text not in seen_text:
                seen_text.add(text)
                windowed_console.append(f"{text} (outside failure window)")
                break  # only the first unique one

    if not windowed_network:
        seen_fallback_paths: set[str] = set()
        for evt in network_events:
            status = evt.get("status", 0)
            if status == -1 or status == 0 or status >= 400:
                url_path = evt.get("url", evt.get("path", ""))
                if not is_telemetry(url_path) and url_path not in seen_fallback_paths:
                    seen_fallback_paths.add(url_path)
                    method = evt.get("method", "GET")
                    windowed_network.append(f"{status} {method} {url_path}")
                    if len(windowed_network) >= 8:
                        break

    # Collect response bodies for 4xx/5xx errors (LLM context only)
    network_error_bodies: list[dict] = []
    all_network_formatted = windowed_network
    for evt in network_events:
        status = evt.get("status", 0)
        body = evt.get("response_body")
        if (status == -1 or status == 0 or status >= 400) and body:
            url_path = evt.get("url", evt.get("path", ""))
            method = evt.get("method", "GET")
            network_error_bodies.append({"endpoint": f"{status} {method} {url_path}", "body": body})
        if len(network_error_bodies) >= 3:
            break

    return {
        "failingActionIndex": failing_idx,
        "failureInferred": inferred,
        "relevantConsoleErrors": windowed_console,
        "relevantNetworkErrors": windowed_network,
        "relevantNetworkErrorBodies": network_error_bodies,
    }


# ---------------------------------------------------------------------------
# Action timeline
# ---------------------------------------------------------------------------

def format_action_timeline(
    actions: list[dict],
    failing_action_index: int,
    failure_inferred: bool,
    max_actions: int = 15,
) -> list[dict]:
    """Return the last *max_actions* actions around the failure as a list of
    descriptive dicts."""
    if not actions:
        return []

    # Determine the slice of actions to show — centred around the failure
    start = max(0, failing_action_index - max_actions + 1)
    end = min(len(actions), start + max_actions)
    # Adjust start in case end was clamped
    start = max(0, end - max_actions)

    timeline: list[dict] = []
    for i in range(start, end):
        action = actions[i]
        api_name = action.get("apiName", "")
        start_time = action.get("startTime", 0)
        end_time = action.get("endTime", start_time)
        duration_ms = end_time - start_time

        entry: dict = {
            "index": i,
            "apiName": api_name,
            "selector": action.get("selector", action.get("params", {}).get("selector", "")),
            "stepName": action.get("stepName", ""),
            "durationMs": duration_ms,
            "failed": i == failing_action_index and not failure_inferred,
            "inferred": i == failing_action_index and failure_inferred,
            "error": action.get("error", ""),
        }
        timeline.append(entry)

    return timeline
