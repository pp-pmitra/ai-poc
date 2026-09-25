#!/usr/bin/env python3
"""Concurrency-safe append to a kavach-data/fix-patterns/<feature-slug>.md file.

Both kavach-diagnose (via the verdict-reporting skill's Phase 5) and
kavach-repair (Phase 6) append to the same per-feature pattern file — the
identical dual-writer topology `kavach-data/history/fix-history.json` has,
which was fixed by giving it a single, `flock`-guarded writer
(`append_fix_history.py`). The `.md` pattern files never got the same
treatment: a plain read-whole-file/append-in-memory/write-whole-file-back
edit on both sides can silently lose one writer's entry if both append close
together, with no error on either side. This script closes that gap the
same way, for prose content instead of JSON: it holds an exclusive flock for
the whole read-modify-write, and does not modify or delete any existing
line, whether or not the file exists yet.

Usage:
    echo '### 2026-09-25\n\n- some new fix pattern' | \
        python3 append_fix_pattern.py life-campaign-dashboard --feature-name "Life_CampaignDashboard"

    # _default.md (no --feature-name -- it already has content and its own header)
    cat block.md | python3 append_fix_pattern.py _default

Pass --feature-name whenever the target file has no content yet, whether it's
brand new or a tracked-but-still-empty placeholder (a 0-byte file that
already exists on disk but has no header) -- path-existence alone does not
mean a header was ever written. Omit it only once the file actually has
content (a header plus at least one entry).

Exit 0 and prints the path appended to on success. Exit 1 with a message if
the block text on stdin is empty (nothing to append) or the target
directory can't be created.
"""

from __future__ import annotations

import argparse
import fcntl
import sys
from pathlib import Path

_SCRIPTS_DIR = Path(__file__).resolve().parent
DEFAULT_FIX_PATTERNS_DIR = _SCRIPTS_DIR.parents[3] / "kavach-data" / "fix-patterns"


def append_block(path: Path, block: str, feature_name: str | None) -> None:
    """Appends `block` under an exclusive lock held for the whole
    read-modify-write, so two writers (kavach-diagnose and kavach-repair,
    or two concurrent runs of either) finishing close together serialize
    instead of one silently overwriting the other's just-written entry.
    Bootstraps a new feature file with the standard header if it doesn't
    exist yet and `feature_name` is given; never touches any existing line.
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    path.touch(exist_ok=True)
    with open(path, "r+", encoding="utf-8") as f:
        fcntl.flock(f.fileno(), fcntl.LOCK_EX)
        try:
            current = f.read()
            if not current.strip():
                if feature_name:
                    current = f"# {feature_name} fix patterns\n\n## Run log\n"
                else:
                    current = ""
            separator = "" if not current or current.endswith("\n\n") else ("\n" if current.endswith("\n") else "\n\n")
            f.seek(0)
            f.truncate()
            f.write(current + separator + block.rstrip("\n") + "\n")
            f.flush()
        finally:
            fcntl.flock(f.fileno(), fcntl.LOCK_UN)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("feature_slug", help="e.g. life-campaign-dashboard, or _default")
    parser.add_argument("--dir", type=Path, default=DEFAULT_FIX_PATTERNS_DIR)
    parser.add_argument(
        "--feature-name", default=None,
        help="Feature name (use the .feature file's own stem, e.g. \"Life_CampaignDashboard\", "
             "not a humanized form) used only to bootstrap a header when the target file has no "
             "content yet -- either brand new or a tracked-but-still-empty placeholder. "
             "Omit only for _default.md or a feature file that already has a header and entries.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    block = sys.stdin.read()
    if not block.strip():
        print("Nothing to append: stdin was empty.")
        return 1

    path = args.dir / f"{args.feature_slug}.md"
    append_block(path, block, args.feature_name)
    print(f"Appended to {path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
