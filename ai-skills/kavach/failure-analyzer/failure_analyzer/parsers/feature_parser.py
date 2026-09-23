"""
feature_parser.py

Port of featureParser.js — extracts Gherkin text from .feature files.
"""

from __future__ import annotations

import re

MAX_EXAMPLES_ROWS = 2


def normalize_scenario_name(name: str) -> str:
    """Strip whitespace and collapse internal whitespace."""
    return re.sub(r"\s+", " ", name.strip()).lower()


def is_scenario_header(line: str) -> bool:
    """Returns True if the line matches a Scenario or Scenario Outline header."""
    return bool(re.match(r"^\s*Scenario(\s+Outline)?:", line, re.IGNORECASE))


def get_background_gherkin(feature_file_path: str) -> str:
    """Extract the Background section from a feature file, or empty string if none."""
    try:
        with open(feature_file_path, "r", encoding="utf-8") as f:
            lines = f.read().split("\n")
    except OSError:
        return ""

    capturing = False
    result = []

    for line in lines:
        trimmed = line.strip()

        if not capturing and re.match(r"^Background:", trimmed, re.IGNORECASE):
            capturing = True

        if capturing:
            # Stop when we hit a tag or a new Scenario/Feature
            if len(result) > 1 and (
                trimmed.startswith("@")
                or is_scenario_header(trimmed)
                or re.match(r"^Feature:", trimmed, re.IGNORECASE)
            ):
                break
            result.append(line)

    return "\n".join(result).strip()


def get_gherkin_for_scenario(feature_file_path: str, scenario_name: str) -> str:
    """
    Reads the feature file and finds the scenario header matching scenario_name
    (case-insensitive after normalizing whitespace). Extracts from that header
    through all subsequent lines until the next Scenario, tag, or Feature header
    or end of file. Prepends any Background section. Calls truncate_examples_table.

    Returns the extracted Gherkin text string.
    """
    with open(feature_file_path, "r", encoding="utf-8") as f:
        lines = f.read().split("\n")

    normalized_target = normalize_scenario_name(scenario_name)

    capturing = False
    result = []

    for line in lines:
        trimmed = line.strip()

        if not capturing and is_scenario_header(trimmed):
            header_name = re.sub(
                r"^Scenario(\s+Outline)?:\s*", "", trimmed, flags=re.IGNORECASE
            )
            if normalize_scenario_name(header_name) == normalized_target:
                capturing = True

        if capturing:
            result.append(line)
            if len(result) > 2:
                if trimmed.startswith("@") or (
                    is_scenario_header(trimmed) and len(result) > 3
                ):
                    result.pop()
                    break

    scenario_text = "\n".join(result).strip()
    background = get_background_gherkin(feature_file_path)

    if background:
        full = background + "\n\n" + scenario_text
    else:
        full = scenario_text

    return truncate_examples_table(full)


def truncate_examples_table(gherkin: str) -> str:
    """
    Finds Examples: sections in the gherkin text. Keeps the header row and up
    to MAX_EXAMPLES_ROWS data rows. If truncated, appends a note indicating
    how many more rows were omitted.
    """
    lines = gherkin.split("\n")
    output = []
    in_examples = False
    table_rows_seen = 0
    total_data_rows = 0

    # First pass: count total data rows per Examples block so we can report
    # the truncated count accurately. We do this inline during the main pass.

    # We need a two-pass approach to know total rows for the "N more rows" note.
    # Pre-scan to count rows per Examples block.
    examples_row_counts = []
    current_count = 0
    scanning_examples = False
    for line in lines:
        trimmed = line.strip()
        if re.match(r"^Examples:", trimmed, re.IGNORECASE):
            if scanning_examples:
                examples_row_counts.append(current_count)
            scanning_examples = True
            current_count = 0
        elif scanning_examples:
            if trimmed.startswith("|"):
                current_count += 1
            else:
                examples_row_counts.append(current_count)
                scanning_examples = False
    if scanning_examples:
        examples_row_counts.append(current_count)

    # Main pass: build output with truncation.
    examples_block_index = -1

    for line in lines:
        trimmed = line.strip()

        if re.match(r"^Examples:", trimmed, re.IGNORECASE):
            in_examples = True
            table_rows_seen = 0
            examples_block_index += 1
            output.append(line)
            continue

        if in_examples:
            if trimmed.startswith("|"):
                if table_rows_seen == 0:
                    # Header row -- always include
                    output.append(line)
                    table_rows_seen += 1
                elif table_rows_seen <= MAX_EXAMPLES_ROWS:
                    output.append(line)
                    table_rows_seen += 1
                else:
                    # First skipped row: add truncation note
                    if table_rows_seen == MAX_EXAMPLES_ROWS + 1:
                        # total rows in this block minus 1 for header = data rows
                        total_in_block = examples_row_counts[examples_block_index] - 1
                        remaining = total_in_block - MAX_EXAMPLES_ROWS
                        if remaining > 0:
                            output.append(
                                f"    | ... ({remaining} more rows) |"
                            )
                        table_rows_seen += 1
            else:
                # Non-table line ends the examples block
                in_examples = False
                output.append(line)
            continue

        output.append(line)

    return "\n".join(output).strip()


def get_feature_description(feature_file_path: str) -> str:
    """
    Returns everything before the first @tag or Scenario line.
    """
    with open(feature_file_path, "r", encoding="utf-8") as f:
        lines = f.read().split("\n")

    desc_lines = []
    for line in lines:
        trimmed = line.strip()
        if trimmed.startswith("@") or trimmed.startswith("Scenario"):
            break
        desc_lines.append(line)

    return "\n".join(desc_lines).strip()
