#!/usr/bin/env python3

import glob
import json
import os
import re
import subprocess

from anthropic import Anthropic


# ==============================================================================
# CONFIGURATION & CONSTANTS
# ==============================================================================

FEATURE_ROOT = "src/test/resources/features"
STEP_DEF_ROOT = "src/test/java"
PAGE_OBJ_ROOT = "src/main/java/pages"
NAV_TREE_PATH = "navigation-map/navigation-tree.html"

anthropic_client = Anthropic(
    api_key=os.getenv("ANTHROPIC_API_KEY")
)


# ==============================================================================
# HELPER FUNCTIONS: REPO CALIBRATION & UTILITIES
# ==============================================================================

def run_command(cmd, check=True):
    """Executes a bash command and returns stdout."""
    res = subprocess.run(
        cmd,
        shell=True,
        capture_output=True,
        text=True,
    )

    if check and res.returncode != 0:
        raise RuntimeError(
            f"Command failed: {cmd}\nError: {res.stderr}"
        )

    return res.stdout.strip()


def extract_nav_graph():
    """
    Extracts and parses the GRAPH object from
    navigation-map/navigation-tree.html.
    """
    if not os.path.exists(NAV_TREE_PATH):
        return {}

    with open(NAV_TREE_PATH, "r", encoding="utf-8") as f:
        content = f.read()

    match = re.search(
        r"const\s+GRAPH\s*=\s*(\{.*?\});",
        content,
        re.DOTALL,
    )

    if not match:
        return {}

    raw_json = match.group(1)

    try:
        return json.loads(raw_json)
    except Exception:
        return {"raw_graph_string": raw_json}


def scan_existing_feature_files():
    """
    Recursively scans all .feature files under
    src/test/resources/features/.
    """
    feature_files = {}

    for filepath in glob.glob(
        f"{FEATURE_ROOT}/**/*.feature",
        recursive=True,
    ):
        with open(filepath, "r", encoding="utf-8") as f:
            feature_files[filepath] = f.read()

    return feature_files


def run_column_width_formatter(file_path):
    """
    Applies the strict Sutra Column Width Algorithm to align
    Gherkin tables.

    Calculates max width per column across header + all data rows.
    """
    if not os.path.exists(file_path):
        return

    with open(file_path, "r", encoding="utf-8") as f:
        lines = f.read().split("\n")

    out = []
    i = 0

    row_re = re.compile(r"^(\s*)\|(.*)\|\s*$")

    while i < len(lines):
        match = row_re.match(lines[i])

        if match:
            indent = match.group(1)
            block = []

            while i < len(lines):
                row_match = row_re.match(lines[i])

                if not row_match:
                    break

                block.append(
                    [
                        cell.strip()
                        for cell in row_match.group(2).split("|")
                    ]
                )

                i += 1

            widths = [
                max(len(row[column]) for row in block)
                for column in range(len(block[0]))
            ]

            for row in block:
                formatted_row = (
                    indent
                    + "| "
                    + " | ".join(
                        cell.ljust(width)
                        for cell, width in zip(row, widths)
                    )
                    + " |"
                )

                out.append(formatted_row)

        else:
            out.append(lines[i])
            i += 1

    with open(file_path, "w", encoding="utf-8") as f:
        f.write("\n".join(out))


# ==============================================================================
# ANTHROPIC CLAUDE INTEGRATION
# ==============================================================================

SYSTEM_PROMPT = """
You are Sutra, an expert BDD Scenario Generation AI.

Your task is to analyze test requirements, cross-reference codebase step
definitions, navigation graphs, and existing feature files, and produce
complete, review-ready, workflow-consolidated Gherkin test coverage.

STRICT CONVENTIONS:

1. 2-space indentation throughout.
2. Tag placement: @todo tag ONLY above scenarios.
3. Uppercase-First-Letter after every keyword
   (Given/When/Then/And/But).
4. NO blank line before Examples:.
5. Step Atomicity: One assertion per line.
   Split chained assertions into separate And steps.
6. NO meta/abstract steps or rationale prose inside step text.
7. Use <UPPERCASE_ANGLED_BRACKETS> for Examples table parameters.
8. Feature files MUST land under a module subdirectory:
   src/test/resources/features/<module>/<Domain>_<Module>.feature.
9. If appending to an existing file, keep existing content
   byte-for-byte untouched and append at the end.

OUTPUT FORMAT:

Return ONLY a valid JSON object matching this schema.
Do not include markdown code fences or commentary outside the JSON.

{
  "target_file":
    "src/test/resources/features/<module>/<Domain>_<Module>.feature",
  "is_append": true,
  "gherkin_content":
    "Full feature file content or appended scenarios",
  "triage_chat_table":
    "Markdown formatted STEP 3 Automation Triage Table",
  "pr_body":
    "Full Markdown formatted PR body matching exact Sutra template"
}
"""


def generate_sutra_output(
    ticket_id,
    requirements_text,
    existing_features,
    nav_graph,
):
    """
    Sends prompt and codebase context to Claude via Anthropic API.
    """

    existing_feature_context = {
        path: content[:500] + "..."
        for path, content in existing_features.items()
    }

    user_prompt = f"""
TARGET TICKET: {ticket_id}

=== INGESTED REQUIREMENTS / CONTEXT ===
{requirements_text}

=== CODEBASE NAVIGATION GRAPH ===
{json.dumps(nav_graph, indent=2)}

=== EXISTING FEATURE FILES MAP ===
{json.dumps(existing_feature_context, indent=2)}

Synthesize the Gherkin coverage following all Sutra rules
and return the JSON payload.
"""

    response = anthropic_client.messages.create(
        model="claude-sonnet-5",
        max_tokens=8192,
        # Sonnet 5 enables adaptive thinking by default. This request needs
        # machine-readable JSON, so reserve the output budget for the answer
        # instead of allowing thinking blocks to consume all max_tokens.
        thinking={"type": "disabled"},
        system=SYSTEM_PROMPT,
        messages=[
            {
                "role": "user",
                "content": user_prompt,
            }
        ],
    )

    # Claude Sonnet 5 responses may contain multiple content blocks,
    # including ThinkingBlock and TextBlock.
    # Only TextBlock objects expose the .text attribute.
    text_blocks = [
        block.text
        for block in response.content
        if block.type == "text"
    ]

    if not text_blocks:
        block_types = [
            getattr(block, "type", type(block).__name__)
            for block in response.content
        ]

        raise ValueError(
            "Claude response contained no text block. "
            f"Received block types: {block_types}. "
            f"Stop reason: {response.stop_reason}"
        )

    raw_text = "\n".join(text_blocks).strip()

    # Claude has been explicitly instructed to return only JSON,
    # so parse the complete response instead of extracting JSON
    # with a regular expression.
    try:
        return json.loads(raw_text)

    except json.JSONDecodeError as exc:
        raise ValueError(
            "Failed to parse Claude response as JSON.\n\n"
            f"Raw response:\n{raw_text}"
        ) from exc


# ==============================================================================
# MAIN EXECUTION FLOW
# ==============================================================================

def main():
    ticket_id = os.getenv(
        "JIRA_TICKET_ID",
        "UNKNOWN_TICKET",
    )

    requirements_doc = os.getenv(
        "REQUIREMENTS_TEXT",
        "No requirements payload provided.",
    )

    print(
        f"--- [STEP 0] Ingesting Input for Ticket: "
        f"{ticket_id} ---"
    )

    print(
        "--- [STEP 2] Calibrating Codebase & "
        "Step Definitions ---"
    )

    existing_features = scan_existing_feature_files()
    nav_graph = extract_nav_graph()

    print(
        "--- [STEP 3-4] Generating Gherkin Coverage "
        "via Claude API ---"
    )

    result = generate_sutra_output(
        ticket_id,
        requirements_doc,
        existing_features,
        nav_graph,
    )

    target_file = result["target_file"]
    gherkin_content = result["gherkin_content"]
    is_append = result["is_append"]
    pr_body = result["pr_body"]

    print(
        f"--- [STEP 4] Writing changes to "
        f"{target_file} ---"
    )

    os.makedirs(
        os.path.dirname(target_file),
        exist_ok=True,
    )

    if is_append and os.path.exists(target_file):
        with open(
            target_file,
            "a",
            encoding="utf-8",
        ) as f:
            f.write(
                "\n\n" + gherkin_content.strip()
            )

    else:
        with open(
            target_file,
            "w",
            encoding="utf-8",
        ) as f:
            f.write(
                gherkin_content.strip()
            )

    print(
        "--- [STEP 5] Running Column Width "
        "Alignment Script ---"
    )

    run_column_width_formatter(target_file)

    print(
        "--- [STEP 6] Preparing Git Branch Index ---"
    )

    branches = run_command("git branch -r")

    sutra_indices = [
        int(match.group(1))
        for match in re.finditer(
            r"Sutra_(\d+)",
            branches,
        )
    ]

    next_index = (
        max(sutra_indices) + 1
        if sutra_indices
        else 101
    )

    branch_name = f"Sutra_{next_index}"

    # Export variables to GitHub Actions output.
    if "GITHUB_OUTPUT" in os.environ:
        with open(
            os.environ["GITHUB_OUTPUT"],
            "a",
            encoding="utf-8",
        ) as gh_out:
            gh_out.write(
                f"branch_name={branch_name}\n"
            )
            gh_out.write(
                f"target_file={target_file}\n"
            )

    # Write PR body markdown file for GitHub CLI step.
    with open(
        "pr_body.md",
        "w",
        encoding="utf-8",
    ) as f:
        f.write(pr_body)

    print(
        "Sutra run complete. "
        f"Resolved branch name: {branch_name}"
    )


if __name__ == "__main__":
    main()
