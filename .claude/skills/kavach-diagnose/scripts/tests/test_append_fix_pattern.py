"""Tests for append_fix_pattern.py — the lock-safe writer for the shared
kavach-data/fix-patterns/<feature-slug>.md files (the same dual-writer
topology fix-history.json has, closed here the same way for prose content)."""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from append_fix_pattern import append_block


class AppendBlockTests(unittest.TestCase):
    def test_bootstraps_a_new_feature_file_with_header(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "life-campaign-dashboard.md"
            append_block(path, "### 2026-09-25\n\n- fixed a stale locator", "Life Campaign Dashboard")

            text = path.read_text(encoding="utf-8")
            self.assertTrue(text.startswith("# Life Campaign Dashboard fix patterns\n\n## Run log\n"))
            self.assertIn("### 2026-09-25", text)
            self.assertIn("fixed a stale locator", text)

    def test_appends_to_existing_file_without_touching_prior_content(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "_default.md"
            path.write_text("# Default fix patterns (all features)\n\nSome existing content.\n", encoding="utf-8")

            append_block(path, "### New pattern\n\n- new content", None)

            text = path.read_text(encoding="utf-8")
            self.assertIn("Some existing content.", text)
            self.assertIn("### New pattern", text)
            # Existing content must still appear before the new block, unmodified.
            self.assertLess(text.index("Some existing content."), text.index("### New pattern"))

    def test_no_feature_name_on_empty_file_does_not_fabricate_a_header(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "no-header.md"
            append_block(path, "- just a block", None)

            text = path.read_text(encoding="utf-8")
            self.assertFalse(text.startswith("#"))
            self.assertIn("just a block", text)

    def test_two_sequential_appends_both_survive(self):
        # Simulates kavach-diagnose and kavach-repair each appending once in
        # the same run -- the scenario the missing lock used to lose one of.
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "life-pmp.md"
            append_block(path, "- first writer's entry", "Life PMP")
            append_block(path, "- second writer's entry", None)

            text = path.read_text(encoding="utf-8")
            self.assertIn("first writer's entry", text)
            self.assertIn("second writer's entry", text)


if __name__ == "__main__":
    unittest.main()
