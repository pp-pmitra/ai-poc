"""
Standalone script — reads cucumber.json from the last test run and appends
all failures to the rolling history file.

Run independently after any Maven/test run:
  python failure-analyzer/record_history.py
  python failure-analyzer/record_history.py path/to/custom/cucumber.json
"""

import sys
from pathlib import Path

from failure_analyzer.config import load_config
from failure_analyzer.parsers.cucumber_parser import parse_cucumber_json
from failure_analyzer.reporters.history_writer import append_to_history


def main():
    config = load_config()

    if len(sys.argv) > 1:
        cucumber_json_path = Path(sys.argv[1]).resolve()
    else:
        base_dir = Path(__file__).parent
        cucumber_json_path = (base_dir / config.paths.cucumber_json).resolve()

    print("\n======================================")
    print("  Failure History Recorder")
    print("======================================\n")

    if not cucumber_json_path.exists():
        print(f"Error: cucumber.json not found at:\n  {cucumber_json_path}")
        print("Run your tests first to generate this file.\n")
        sys.exit(1)

    print(f"Reading: {cucumber_json_path}")

    failures, passed_scenarios, _total = parse_cucumber_json(str(cucumber_json_path))

    if not failures:
        print("No failures found in this run — nothing to record.\n")
        sys.exit(0)

    print(
        f"Found {len(failures)} failure(s), {len(passed_scenarios)} passed — appending to history...\n"
    )

    append_to_history(failures, passed_scenarios)

    by_feature: dict[str, int] = {}
    for f in failures:
        key = Path(f.feature_file).stem if f.feature_file else "Unknown"
        by_feature[key] = by_feature.get(key, 0) + 1

    for feature, count in by_feature.items():
        print(f"  {feature}: {count} failure(s)")

    print("\nHistory updated: failure-analyzer/history/failure-history.jsonl")
    print("======================================\n")


if __name__ == "__main__":
    main()
