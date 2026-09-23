"""
bug_report_parser.py

Port of bugReportParser.js — parses LLM output text into structured fields.
"""

from __future__ import annotations

import re

SEVERITY_TO_JIRA_PRIORITY = {
    "critical": "Highest",
    "high": "High",
    "medium": "Normal",
    "low": "Low",
}


def extract_field(text: str, field_name: str) -> str:
    """
    Extracts a **Field:** value block from text. Uses regex to find
    **field_name:** and captures everything until the next ** bold header,
    horizontal rule ---, or heading #. Strips whitespace.
    """
    escaped = re.escape(field_name)
    pattern = re.compile(
        rf"\*\*{escaped}:\*\*\s*([\s\S]*?)(?=\*\*[A-Za-z]|\n---|\n#|$)",
        re.IGNORECASE,
    )
    match = pattern.search(text)
    return match.group(1).strip() if match else ""


def parse_bug_report(text: str) -> dict:
    """
    Returns dict with keys: title, component, severity (lowercased), priority
    (mapped via SEVERITY_TO_JIRA_PRIORITY), steps_to_reproduce, expected_result,
    actual_result, root_cause, investigation_hints, raw_text. All extracted via
    extract_field.
    """
    severity_raw = extract_field(text, "Severity").lower().split()[0] if extract_field(text, "Severity") else ""
    # Also handle parenthetical suffixes like "high (P2)"
    severity_raw = re.split(r"[\s(]", severity_raw)[0] if severity_raw else ""

    title = extract_field(text, "Title")
    if not title:
        # Try extracting from a markdown heading: # Title text
        heading_match = re.search(r"^#\s+(.+)", text, re.MULTILINE)
        title = heading_match.group(1).strip() if heading_match else "Automated Bug Report (title extraction failed)"

    return {
        "title": title,
        "component": extract_field(text, "Component"),
        "severity": severity_raw,
        "priority": SEVERITY_TO_JIRA_PRIORITY.get(severity_raw, "Normal"),
        "steps_to_reproduce": extract_field(text, "Steps to Reproduce"),
        "expected_result": extract_field(text, "Expected Result"),
        "actual_result": extract_field(text, "Actual Result"),
        "root_cause": extract_field(text, "Possible Root Cause"),
        "investigation_hints": extract_field(text, "Investigation Hints"),
        "raw_text": text,
    }


def parse_single_failure_analysis(text: str) -> dict:
    """
    Extracts category, confidence, analysis, fix from single-failure AI responses.
    Category comes from **Category:** field. Calls normalize_category.
    Confidence from **Confidence:** field, lowercased ("high"/"low"), empty
    string if absent (older responses / malformed output) — callers must treat
    a missing confidence as "not low" (i.e. don't escalate), never as "low".
    Analysis from **Analysis:**. Fix from **Fix:** or **Suggested Fix:**.
    """
    if not text:
        return {"category": None, "confidence": "", "analysis": "", "fix": ""}

    category_match = re.search(
        r"\*{0,2}Category\*{0,2}\s*[-:–]\s*(.+)", text, re.IGNORECASE
    )
    confidence_match = re.search(
        r"\*{0,2}Confidence\*{0,2}\s*[-:–]\s*\*{0,2}\s*(\w+)", text, re.IGNORECASE
    )
    # Use the LAST **Fix:** in the text — the LLM sometimes quotes prior history
    # that contains "Fix: ..." mid-analysis, which trips up re.search (first match).
    # The real fix is always the final one.
    fix_positions = [m for m in re.finditer(
        r"\*{0,2}(?:Suggested\s+)?Fix\*{0,2}\s*[-:–]\s*",
        text,
        re.IGNORECASE,
    )]
    last_fix_pos = fix_positions[-1].end() if fix_positions else None

    analysis_match = re.search(
        r"\*{0,2}Analysis\*{0,2}\s*[-:–]\s*([\s\S]*?)(?=\n\s*\*{0,2}(?:Suggested\s+)?Fix\*{0,2}\s*[-:–]|$)",
        text[:last_fix_pos] if last_fix_pos else text,
        re.IGNORECASE,
    )
    fix_match = re.search(
        r"([\s\S]*)$",
        text[last_fix_pos:] if last_fix_pos is not None else "",
        re.IGNORECASE,
    )

    def clean_capture(m):
        return re.sub(r"^[*\s]+", "", m.group(1)).strip() if m else ""

    return {
        "category": normalize_category(category_match.group(1).strip() if category_match else ""),
        "confidence": confidence_match.group(1).strip().lower() if confidence_match else "",
        "analysis": clean_capture(analysis_match),
        "fix": clean_capture(fix_match),
    }


def normalize_category(raw: str) -> str | None:
    """
    Maps raw category text to one of: 'Script Issue', 'Potential Functional Issue',
    'Environment Issue', or None. Matches case-insensitively by checking if key
    phrases appear in the raw text.
    """
    s = (raw or "").lower().strip()
    if not s:
        return None
    if "script" in s:
        return "Script Issue"
    if "functional" in s or "app" in s or "product" in s:
        return "Potential Functional Issue"
    if "environment" in s or "infra" in s:
        return "Environment Issue"
    return None
