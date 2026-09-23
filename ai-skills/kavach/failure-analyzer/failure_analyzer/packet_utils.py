"""Shared packet-building helpers used by both the live-replay worker packets
(`replay_workers.py`) and the cheap no-browser triage tier (`triage/`).

Extracted from `replay_workers.py` so the two pipelines share one
redaction/truncation/code-window implementation instead of drifting apart.
"""

from __future__ import annotations

import re
from pathlib import Path
from typing import Any

CODE_RADIUS = 18

NOISE_URL_RE = re.compile(
    r"/sr\b|/beacon|/collect|/analytics|/tracking|/pixel|/log\b|"
    r"doubleclick|googlesyndication|facebook\.com/tr|"
    r"/timeline/|/cp\?p=|pusher",
    re.IGNORECASE,
)


def filter_noise_network_errors(entries: list) -> list:
    """Strip tracking beacons, analytics pixels, and websocket keep-alives
    from a list of network error strings — their -1 aborts are infra noise,
    not evidence of the app breaking."""
    return [e for e in entries if not NOISE_URL_RE.search(str(e))]


SECRET_VALUE_RE = re.compile(
    r"(?i)\b(password|passwd|pwd|secret|token|api[-_ ]?key|client[-_ ]?secret)\b"
    r"\s*[:=]\s*['\"]?[^'\"\s,;}]+"
)
JWT_RE = re.compile(r"\beyJ[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{10,}\b")
LONG_KEY_RE = re.compile(r"\b[A-Za-z0-9_/-]{48,}={0,2}\b")


def redact_text(value: Any) -> Any:
    """Redact secret-looking substrings from strings, recursively for JSON-ish data."""
    if isinstance(value, str):
        text = SECRET_VALUE_RE.sub(lambda m: f"{m.group(1)}=<redacted>", value)
        text = JWT_RE.sub("<redacted-jwt>", text)
        return LONG_KEY_RE.sub("<redacted-long-value>", text)
    if isinstance(value, list):
        return [redact_text(v) for v in value]
    if isinstance(value, dict):
        redacted: dict[str, Any] = {}
        for key, item in value.items():
            if re.search(r"(?i)(password|passwd|pwd|secret|token|apiKey|api_key|clientSecret|client_secret)", key):
                redacted[key] = "<redacted>"
            else:
                redacted[key] = redact_text(item)
        return redacted
    return value


def truncate(text: str | None, cap: int) -> str | None:
    if not text:
        return text
    return text if len(text) <= cap else text[:cap] + "...(truncated)"


def strip_file_scheme(path: str | None) -> str:
    if not path:
        return ""
    return re.sub(r"^file:", "", path).lstrip("/")


def safe_slug(value: str, fallback: str) -> str:
    slug = re.sub(r"[^a-zA-Z0-9]+", "-", value).strip("-").lower()
    return (slug or fallback)[:90]


def resolve_repo_path(repo_root: Path, path: str | None) -> Path | None:
    cleaned = strip_file_scheme(path)
    if not cleaned:
        return None
    candidate = Path(cleaned)
    if not candidate.is_absolute():
        candidate = repo_root / candidate
    try:
        candidate.relative_to(repo_root)
    except ValueError:
        return None
    return candidate


def read_code_window(
    repo_root: Path,
    path: str | None,
    line: int | None,
    radius: int = CODE_RADIUS,
) -> dict[str, Any] | None:
    if not path or not line:
        return None
    file_path = resolve_repo_path(repo_root, path)
    if file_path is None or not file_path.exists():
        return None
    try:
        lines = file_path.read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError:
        return None
    start = max(1, int(line) - radius)
    end = min(len(lines), int(line) + radius)
    # If the window clipped at the file boundary (Background + scenario
    # within a few lines of each other), expand back to the default radius
    # so Background steps aren't dropped.
    if (start == 1 or end == len(lines)) and radius < CODE_RADIUS:
        start = max(1, int(line) - CODE_RADIUS)
        end = min(len(lines), int(line) + CODE_RADIUS)
    width = len(str(end))
    snippet = "\n".join(f"{idx:>{width}} | {lines[idx - 1]}" for idx in range(start, end + 1))
    return {
        "file": strip_file_scheme(path),
        "line": line,
        "startLine": start,
        "endLine": end,
        "snippet": snippet,
    }


DEFAULT_TEXT_LOOKAROUND = 10


def text_present_near_line(
    repo_root: Path,
    file_ref: str | None,
    line: int | None,
    text: str | None,
    lookaround: int = DEFAULT_TEXT_LOOKAROUND,
) -> bool:
    """Is `text` present in `file_ref`, within `lookaround` lines of `line`
    (or anywhere in the file if `line` isn't given)? Lightweight grep, not an
    AST diff — used to verify a claimed diff/fix is grounded in the actual
    recorded location, not a coincidental match of the same text elsewhere in
    the file (a locator string reused across multiple methods, for example).
    """
    if not text or not file_ref:
        return False
    file_path = resolve_repo_path(repo_root, file_ref)
    if file_path is None or not file_path.exists():
        return False
    try:
        lines = file_path.read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError:
        return False
    if line:
        start = max(0, int(line) - 1 - lookaround)
        end = min(len(lines), int(line) - 1 + lookaround + 1)
        window = lines[start:end]
    else:
        window = lines
    return any(text in text_line for text_line in window)


def select_representative(failures: list[dict[str, Any]]) -> dict[str, Any]:
    """Prefer a non-(likely-intermittent) failure as the group's representative;
    fall back to the first failure if every one in the group looks intermittent.
    """
    return next(
        (f for f in failures if not ((f.get("stepReliability") or {}).get("likelyIntermittent"))),
        failures[0],
    )


def all_likely_intermittent(failures: list[dict[str, Any]]) -> bool:
    return all((f.get("stepReliability") or {}).get("likelyIntermittent") for f in failures)


_FEATURE_FILE_RADIUS = 8


def code_context_for_failure(
    repo_root: Path, failure: dict[str, Any], roles: tuple[str, ...] = ("feature", "stepDef", "pageObject", "utility")
) -> list[dict[str, Any]]:
    contexts: list[dict[str, Any]] = []
    seen: set[tuple[str, int]] = set()
    error_location = failure.get("errorLocation") or {}
    for role in roles:
        loc = error_location.get(role)
        if not loc:
            continue
        key = (loc.get("file") or "", int(loc.get("line") or 0))
        if key in seen:
            continue
        seen.add(key)
        # Feature files contain short Gherkin step lists; a tighter window
        # (±8 lines) captures the scenario without bloating the packet with
        # surrounding scenarios. The fallback-to-default logic in
        # read_code_window handles files where Background + scenario are very
        # close together.
        radius = _FEATURE_FILE_RADIUS if role == "feature" else CODE_RADIUS
        window = read_code_window(repo_root, loc.get("file"), loc.get("line"), radius=radius)
        if window:
            window["role"] = role
            if loc.get("className"):
                window["className"] = loc.get("className")
            if loc.get("method"):
                window["method"] = loc.get("method")
            contexts.append(window)
    return contexts
