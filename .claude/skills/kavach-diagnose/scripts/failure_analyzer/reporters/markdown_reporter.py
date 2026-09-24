"""Port of markdownReporter.js — writes markdown bug report files."""

from __future__ import annotations

import os
import re
from datetime import datetime
from pathlib import Path


def write_bug_report(bug_report_text: str, group, output_dir: str) -> str:
    """Write a markdown bug report for a :class:`FailureGroup`.

    Parameters
    ----------
    bug_report_text:
        The AI-generated analysis text to embed.
    group:
        A ``FailureGroup`` dataclass with ``root_cause_label``, ``failures``,
        and ``affected_features``.
    output_dir:
        Directory to write the report into (created if needed).

    Returns
    -------
    str
        Absolute path to the written file.
    """
    os.makedirs(output_dir, exist_ok=True)

    timestamp = datetime.now().strftime("%Y-%m-%dT%H-%M-%S")
    safe_name = re.sub(r"[^a-zA-Z0-9]", "_", group.root_cause_label)
    file_name = f"{safe_name}_{timestamp}.md"
    file_path = os.path.join(output_dir, file_name)

    affected = ", ".join(Path(f).name for f in group.affected_features)

    header = "\n".join([
        f"# Bug Report: {group.root_cause_label}",
        "",
        "| Field | Value |",
        "|-------|-------|",
        f"| Generated | {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} |",
        f"| Failed Scenarios | {len(group.failures)} |",
        f"| Affected Features | {affected} |",
        f"| Root Cause Pattern | {group.root_cause_label} |",
        f"| Failed Step | {group.failures[0].failed_step} |",
        "",
        "---",
        "",
    ])

    scenario_lines = [
        "## Affected Scenarios",
        "",
    ]
    for i, f in enumerate(group.failures, 1):
        scenario_lines.append(f"{i}. **{f.scenario_name}**")
    scenario_lines.extend(["", "---", ""])
    scenario_list = "\n".join(scenario_lines)

    trace_section = ""
    if group.failures[0].trace_path:
        trace_section = "\n".join([
            "## Playwright Trace",
            "",
            "```bash",
            f'npx playwright show-trace "{group.failures[0].trace_path}"',
            "```",
            "",
            "---",
            "",
        ])

    ai_section = "\n".join([
        "## AI Analysis",
        "",
        bug_report_text,
        "",
    ])

    with open(file_path, "w", encoding="utf-8") as fh:
        fh.write(header + scenario_list + trace_section + ai_section)

    return os.path.abspath(file_path)
