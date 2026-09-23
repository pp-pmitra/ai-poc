"""Port of failureGrouper.js — groups test failures by a failure signature.

The original key was only ``exception_type|normalized_step``. That was cheap,
but too coarse: one "click X" timeout caused by a stale locator could be
merged with another "click X" timeout caused by an API outage or a browser
crash. Keep the old two fields as the backbone, then append stronger dimensions
only when the evidence exists.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from urllib.parse import urlparse

from failure_analyzer.packet_utils import filter_noise_network_errors

# JUnit assertion classes that are semantically identical to AssertionError
# and should be grouped/processed as one.
ASSERTION_ALIASES = {"ComparisonFailure", "AssertionFailedError"}

_EXCEPTION_RE = re.compile(r"\w+Error|\w+Exception|\w+Failure")
_QUOTED_SELECTOR_RE = re.compile(r"""locator\((['"])(.*?)\1\)|waiting for (?:selector|locator)\s+(['"])(.*?)\3""", re.IGNORECASE | re.DOTALL)
_STATUS_RE = re.compile(r"^(-?\d+)\s+([A-Z]+)\s+(\S+)")
_HEXISH_RE = re.compile(r"\b[0-9a-f]{8,}\b", re.IGNORECASE)
_NUMBER_RE = re.compile(r"\d+")


def _get(obj, attr: str, json_key: str | None = None):
    if isinstance(obj, dict):
        return obj.get(json_key or attr)
    return getattr(obj, attr, None)


@dataclass
class FailureGroup:
    group_id: str
    root_cause_label: str
    normalized_step: str
    failures: list = field(default_factory=list)
    affected_features: list[str] = field(default_factory=list)


def extract_exception_type(error_message: str | None) -> str:
    """Regex-match an Error/Exception/Failure class name from the error message.

    Normalises JUnit assertion variants (ComparisonFailure, AssertionFailedError)
    to ``AssertionError`` so they are caught by the existing workItem.errorTypes
    config without requiring extra entries.

    Returns ``"UnknownError"`` when no recognisable class name is found.
    """
    if not error_message:
        return "UnknownError"
    match = _EXCEPTION_RE.search(error_message)
    if not match:
        return "UnknownError"
    return "AssertionError" if match.group(0) in ASSERTION_ALIASES else match.group(0)


def normalize_step(step_name: str) -> str:
    """Normalise a step name for grouping — replaces quoted strings with ``"..."``,
    digits with ``N``, lowercases, and strips whitespace.
    """
    result = re.sub(r'"[^"]*"', '"..."', step_name)
    result = re.sub(r"\d+", "N", result)
    return result.lower().strip()


def _normalize_value(value: str | None, cap: int = 48) -> str | None:
    if not value:
        return None
    text = re.sub(r"\s+", " ", str(value)).strip().lower()
    if not text:
        return None
    return text[:cap]


def _page_object_signature(failure) -> str | None:
    loc = _get(failure, "error_location", "errorLocation")
    if not loc:
        return None
    page_obj = loc.get("pageObject") if isinstance(loc, dict) else getattr(loc, "page_object", None)
    if not page_obj:
        return None
    class_name = page_obj.get("className") if isinstance(page_obj, dict) else getattr(page_obj, "class_name", None)
    method = page_obj.get("method") if isinstance(page_obj, dict) else getattr(page_obj, "method", None)
    if not class_name and not method:
        return None
    return ".".join(p for p in (class_name, method) if p)


def _locator_signature(failure) -> str | None:
    call_log = _get(failure, "playwright_call_log", "playwrightCallLog") or ""
    match = _QUOTED_SELECTOR_RE.search(call_log)
    if match:
        selector = match.group(2) or match.group(4)
        return _normalize_value(selector, 80)
    # Fall back to the last browser action when a selector is unavailable.
    return _normalize_value(_get(failure, "last_browser_action", "lastBrowserAction"), 40)


def _route_signature(failure) -> str | None:
    url = _get(failure, "page_url", "pageUrl")
    if not url:
        return None
    try:
        parsed = urlparse(url)
    except Exception:
        return _normalize_value(url, 80)
    if parsed.scheme in {"about", "chrome-error"}:
        return f"{parsed.scheme}:{parsed.path or 'blank'}"
    path = parsed.path or "/"
    path = _HEXISH_RE.sub("<id>", path)
    path = _NUMBER_RE.sub("N", path)
    # Query values are often cache-busters or entity ids; the path family is
    # enough to avoid merging failures from unrelated screens.
    return _normalize_value(path, 80)


def _network_signature(failure) -> str | None:
    entries = _get(failure, "network_errors", "networkErrors") or []
    if not entries:
        return None
    real_entries = filter_noise_network_errors(entries)
    if not real_entries:
        return None
    first = str(real_entries[0])
    match = _STATUS_RE.match(first)
    if not match:
        return _normalize_value(first, 80)
    status, method, path = match.groups()
    path = _HEXISH_RE.sub("<id>", path)
    path = _NUMBER_RE.sub("N", path)
    return _normalize_value(f"{status} {method} {path}", 100)


def _crash_signature(failure) -> str | None:
    text = " ".join(
        str(part or "")
        for part in (
            _get(failure, "error_message", "errorMessage"),
            _get(failure, "playwright_call_log", "playwrightCallLog"),
            _get(failure, "page_url", "pageUrl"),
        )
    ).lower()
    if "targetclosederror" in text or "target page, context or browser has been closed" in text:
        return "target-closed"
    if "page crashed" in text or "renderer process crashed" in text:
        return "page-crash"
    if "browser has been closed" in text or "browser disconnected" in text or "cdp" in text and "disconnect" in text:
        return "browser-disconnected"
    if "net::err_aborted" in text or "navigation aborted" in text:
        return "navigation-aborted"
    if "about:blank" in text or "chrome-error://" in text:
        return "blank-or-error-page"
    return None


def build_group_key(failure) -> tuple[str, str, str]:
    """Return ``(group_key, exception_type, normalized_step)`` for *failure*.

    The key is stable and human-readable because it is written into
    `failures-for-replay.json` and later used for history lookups.
    """
    exception_type = extract_exception_type(_get(failure, "error_message", "errorMessage"))
    n_step = normalize_step(_get(failure, "failed_step", "failedStep") or "")
    parts = [exception_type, n_step]
    optional_parts = {
        "locator": _locator_signature(failure),
        "pageObject": _page_object_signature(failure),
        "route": _route_signature(failure),
        "expected": _normalize_value(_get(failure, "expected_value", "expectedValue")),
        "actual": _normalize_value(_get(failure, "actual_value", "actualValue")),
        "network": _network_signature(failure),
        "crash": _crash_signature(failure),
    }
    for label, value in optional_parts.items():
        if value:
            parts.append(f"{label}={value}")
    return "|".join(parts), exception_type, n_step


def group_failures(failures: list) -> list[FailureGroup]:
    """Group failures by a stable, evidence-backed signature key.

    Each failure is expected to expose ``error_message``, ``failed_step``, and
    ``feature_file`` as snake_case attributes (i.e. the ``Failure`` dataclass from
    ``failure_analyzer.parsers.cucumber_parser``).

    Returns a list of :class:`FailureGroup` with ``affected_features`` as a
    sorted list of unique feature files per group.
    """
    groups: dict[str, FailureGroup] = {}
    feature_sets: dict[str, set[str]] = {}

    for failure in failures:
        group_key, exception_type, n_step = build_group_key(failure)

        if group_key not in groups:
            groups[group_key] = FailureGroup(
                group_id=group_key,
                root_cause_label=exception_type,
                normalized_step=n_step,
            )
            feature_sets[group_key] = set()

        groups[group_key].failures.append(failure)
        if failure.feature_file:
            feature_sets[group_key].add(failure.feature_file)

    result: list[FailureGroup] = []
    for key, group in groups.items():
        group.affected_features = sorted(feature_sets[key])
        result.append(group)

    return result
