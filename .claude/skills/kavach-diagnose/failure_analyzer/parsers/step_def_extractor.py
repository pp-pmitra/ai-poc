"""
step_def_extractor.py

Port of stepDefExtractor.js — finds matching Java step definitions for Gherkin steps.
"""

from __future__ import annotations

import os
import re
from pathlib import Path

STEP_DEFS_ROOT = Path(__file__).parent.parent.parent.parent / "src/test/java/stepdefinitions"

MODULE_FILE_MAP = {
    "life": "LifeSteps.java",
    "hcp": "HcpSteps.java",
    "studio": "StudioSteps.java",
    "api": "ApiSteps.java",
}

# Cache: module key -> list of parsed step defs
_cache: dict = {}

# Emit the "step defs root missing" warning at most once per process.
_root_warning_emitted = False


def _warn_missing_root() -> None:
    """Warn (once) that the step-definitions root is absent, so callers can tell
    'no step def matched' apart from 'no step defs could be loaded at all'."""
    global _root_warning_emitted
    if not _root_warning_emitted:
        _root_warning_emitted = True
        print(
            f"  [stepdefs] WARNING: step definitions root not found: "
            f"{STEP_DEFS_ROOT} — step-definition snippets will be unavailable"
        )


def _scan_braces(line: str, depth: int, in_block_comment: bool) -> tuple[int, bool]:
    """Update the running brace *depth* for one line of Java source.

    Braces inside string literals, char literals, ``//`` line comments and
    ``/* */`` block comments are ignored, so constructs like SLF4J ``"{}"``
    placeholders or a lone ``"}"`` in a string don't skew the count. Returns the
    new ``(depth, in_block_comment)`` state. (Java text blocks are not handled;
    the caller's line cap is the safety net.)
    """
    i = 0
    n = len(line)
    in_string = False
    in_char = False
    while i < n:
        ch = line[i]
        nxt = line[i + 1] if i + 1 < n else ""
        if in_block_comment:
            if ch == "*" and nxt == "/":
                in_block_comment = False
                i += 2
                continue
            i += 1
        elif in_string:
            if ch == "\\":
                i += 2
                continue
            if ch == '"':
                in_string = False
            i += 1
        elif in_char:
            if ch == "\\":
                i += 2
                continue
            if ch == "'":
                in_char = False
            i += 1
        elif ch == "/" and nxt == "/":
            break  # line comment — ignore the rest of the line
        elif ch == "/" and nxt == "*":
            in_block_comment = True
            i += 2
        elif ch == '"':
            in_string = True
            i += 1
        elif ch == "'":
            in_char = True
            i += 1
        else:
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
            i += 1
    return depth, in_block_comment


def _escape_for_regex(text: str) -> str:
    """Escape special regex characters in a string."""
    return re.escape(text)


def cucumber_expr_to_regex(annotation_text: str) -> "re.Pattern | None":
    """
    Converts Cucumber expression annotations to compiled Python regexes.

    Replacements:
      {string}  -> "([^"]*)"
      {int}     -> (-?\\d+)
      {double}  -> (-?\\d+\\.?\\d*)
      {word}    -> (\\w+)
      (.*)      -> (.*)

    Wraps in ^...$ and compiles case-insensitively.
    """
    pattern = _escape_for_regex(annotation_text.strip())

    # Replace escaped Cucumber expression placeholders with regex equivalents.
    # re.escape turns {string} into \{string\}, so we match those escaped forms.
    pattern = pattern.replace(r"\{string\}", '"([^"]*)"')
    pattern = pattern.replace(r"\{int\}", r"(-?\d+)")
    pattern = pattern.replace(r"\{double\}", r"(-?\d+\.?\d*)")
    pattern = pattern.replace(r"\{word\}", r"(\w+)")

    # Fallback for unknown Cucumber types like {float}, {bigdecimal}, etc.
    pattern = re.sub(r"\\\{[^}]+\\\}", ".*", pattern)

    # Handle raw regex groups in annotations: (.*) style.
    # re.escape turns (.*) into \(\.\*\), so unescape them.
    pattern = pattern.replace(r"\(\.\*\)", "(.*)")
    # General unescaping of regex groups: \(content\) -> (content)
    pattern = re.sub(r"\\\(([^)]+)\\\)", r"(\1)", pattern)

    try:
        return re.compile("^" + pattern + "$", re.IGNORECASE)
    except re.error:
        return None


def parse_step_defs_from_file(file_path: str | Path) -> list[dict]:
    """
    Reads a Java file, finds @Given/@When/@Then/@And/@But annotations.
    For each match, extracts the annotation text and the method body
    (using brace counting to find the matching closing brace).

    Returns list of {"annotation": str, "regex": compiled pattern, "body": str}.
    """
    file_path = Path(file_path)
    if not file_path.exists():
        return []

    content = file_path.read_text(encoding="utf-8")
    lines = content.split("\n")
    defs = []

    annotation_re = re.compile(
        r'^\s*@(?:Given|When|Then|And|But)\s*\(\s*"((?:[^"\\]|\\.)*)"\s*\)\s*$'
    )

    for i, line in enumerate(lines):
        match = annotation_re.match(line)
        if not match:
            continue

        annotation_text = match.group(1)

        # Find the opening brace of the method (may be on the next line)
        brace_start = -1
        for j in range(i + 1, min(i + 5, len(lines))):
            if "{" in lines[j]:
                brace_start = j
                break

        if brace_start == -1:
            continue

        # Walk forward counting braces to find the matching closing brace,
        # ignoring braces inside strings/char literals/comments.
        depth = 0
        body_lines = []
        in_block_comment = False
        for j in range(brace_start, len(lines)):
            current_line = lines[j]
            depth, in_block_comment = _scan_braces(current_line, depth, in_block_comment)
            body_lines.append(current_line)
            if depth == 0:
                break
            if len(body_lines) > 60:
                break

        # Include the method signature line (one line above brace_start)
        signature_line = lines[brace_start - 1] if brace_start > 0 else ""
        method_body = "\n".join([signature_line] + body_lines)

        defs.append(
            {
                "annotation": annotation_text,
                "regex": cucumber_expr_to_regex(annotation_text),
                "body": method_body,
            }
        )

    return defs


def detect_module(feature_file_path: str) -> str:
    """
    Detects module from path segments. Looks for 'life', 'hcp', 'studio', 'api'
    in the path. Returns the module key or 'life' as default.
    """
    lower = (feature_file_path or "").lower()
    if "/hcp/" in lower or "hcp_" in lower:
        return "hcp"
    if "/studio/" in lower or "studio_" in lower:
        return "studio"
    if "/api/" in lower or "api_" in lower:
        return "api"
    return "life"


def _get_step_defs(module: str) -> list[dict]:
    """Load and cache step defs for a specific module."""
    if module in _cache:
        return _cache[module]
    if not STEP_DEFS_ROOT.exists():
        _warn_missing_root()
    file_name = MODULE_FILE_MAP.get(module, MODULE_FILE_MAP["life"])
    file_path = STEP_DEFS_ROOT / file_name
    _cache[module] = parse_step_defs_from_file(file_path)
    return _cache[module]


def _get_all_step_defs() -> list[dict]:
    """
    Load and cache step defs from ALL .java files in the stepdefinitions folder.
    Used as a fallback when the module-specific file has no match.
    """
    if "__all" in _cache:
        return _cache["__all"]
    defs = []
    if STEP_DEFS_ROOT.exists():
        for entry in sorted(STEP_DEFS_ROOT.iterdir()):
            if entry.suffix == ".java":
                defs.extend(parse_step_defs_from_file(entry))
    else:
        _warn_missing_root()
    _cache["__all"] = defs
    return defs


def get_step_def_snippet(step_text: str, feature_file: str) -> str:
    """
    Strips the Gherkin keyword (Given/When/Then/And/But) from step_text, tries
    matching against the module-specific step def file first, then falls back to
    all files. Returns the matching method body capped at 30 lines, or empty string.
    """
    if not step_text:
        return ""

    bare = re.sub(r"^\s*(?:Given|When|Then|And|But)\s+", "", step_text, flags=re.IGNORECASE).strip()

    module = detect_module(feature_file)
    def_sets = [_get_step_defs(module), _get_all_step_defs()]

    for defs in def_sets:
        for d in defs:
            if d["regex"] is None:
                continue
            if d["regex"].search(bare):
                trimmed = "\n".join(d["body"].split("\n")[:30]).strip()
                return f'// Step: @... ("{d["annotation"]}")\n{trimmed}'

    return ""


def get_step_def_snippets_for_group(failures: list) -> str:
    """
    Deduplicates by feature_file||failed_step, collects snippets for each
    unique step, returns them joined by '---' separators. Access failure
    attributes as failure.feature_file and failure.failed_step (snake_case).
    """
    seen = set()
    snippets = []

    for failure in failures:
        key = f"{failure.feature_file}||{failure.failed_step}"
        if key in seen:
            continue
        seen.add(key)

        snippet = get_step_def_snippet(failure.failed_step, failure.feature_file)
        if snippet:
            snippets.append(snippet)

    if snippets:
        return "\n\n---\n\n".join(snippets)
    return "Step definition not found."


def get_all_step_def_snippets_for_scenario(
    step_execution_trace: list,
    feature_file: str,
    char_cap: int = 6000,
) -> str:
    """Return step definition code for every step in the scenario.

    Steps are ordered as executed (Background first, then scenario steps).
    For each step the full method body is included, capped at 30 lines.
    The combined output is capped at *char_cap* characters total; steps after
    the failing one are dropped first if space is needed, since the LLM cares
    most about what ran before the failure.
    """
    if not step_execution_trace:
        return "Not available"

    # Split into pre-failure (including failed) and post-failure (skipped)
    pre: list[dict] = []
    post: list[dict] = []
    found_failed = False
    for s in step_execution_trace:
        if found_failed:
            post.append(s)
        else:
            pre.append(s)
            if s.get("status") == "failed":
                found_failed = True

    def _snippet(step: dict) -> str:
        keyword = step.get("keyword", "").strip()
        name = step.get("name", "").strip()
        full_step = f"{keyword} {name}".strip()
        status = step.get("status", "unknown").upper()
        code = get_step_def_snippet(full_step, feature_file)
        if not code:
            return f"// [{status}] {full_step}\n// (step definition not found)"
        return f"// [{status}] {full_step}\n{code}"

    # Build from pre-failure steps (most important); add post only if space allows
    parts: list[str] = [_snippet(s) for s in pre]
    combined = "\n\n---\n\n".join(parts)

    if len(combined) <= char_cap:
        # Try to include post-failure steps too (context for skipped assertions)
        for s in post:
            extra = "\n\n---\n\n" + _snippet(s)
            if len(combined) + len(extra) > char_cap:
                combined += f"\n\n// ... ({len(post)} step(s) after failure omitted)"
                break
            combined += extra

    if len(combined) > char_cap:
        combined = combined[:char_cap] + "\n// ... (truncated)"

    return combined or "Not available"


def resolve_step_def_file(feature_file: str) -> str:
    """Returns the relative path string to the step def file for the given feature file's module."""
    module = detect_module(feature_file)
    file_name = MODULE_FILE_MAP.get(module, MODULE_FILE_MAP["life"])
    return f"src/test/java/stepdefinitions/{file_name}"
