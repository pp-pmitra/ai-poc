from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.triage.fix_pattern_cache import find_cached_fix, verify_fix_still_present


class FindCachedFixTests(unittest.TestCase):
    def test_matches_by_scenario_name_and_picks_most_recent(self):
        history = [
            {
                "scenarioName": "s1",
                "verdict": "script_issue_fix_proposed",
                "timestamp": "2026-08-01T00:00:00Z",
                "change": {"file": "a.java", "line": 1, "after": "old"},
            },
            {
                "scenarioName": "s1",
                "verdict": "script_issue_fix_proposed",
                "timestamp": "2026-08-10T00:00:00Z",
                "change": {"file": "a.java", "line": 1, "after": "new"},
            },
        ]
        found = find_cached_fix("s1", None, history)
        self.assertEqual(found["change"]["after"], "new")

    def test_falls_back_to_group_id_when_no_scenario_match(self):
        history = [{
            "scenarioName": "other-scenario",
            "groupId": "g1",
            "verdict": "script_issue_fix_applied",
            "timestamp": "2026-08-01T00:00:00Z",
            "change": {"file": "a.java", "line": 1, "after": "x"},
        }]
        found = find_cached_fix("s1", "g1", history)
        self.assertIsNotNone(found)

    def test_product_bug_history_is_never_a_cache_hit(self):
        history = [{
            "scenarioName": "s1",
            "verdict": "confirmed_product_bug",
            "timestamp": "2026-08-01T00:00:00Z",
            "change": None,
        }]
        self.assertIsNone(find_cached_fix("s1", None, history))

    def test_entry_without_change_is_ignored(self):
        history = [{
            "scenarioName": "s1",
            "verdict": "script_issue_fix_proposed",
            "timestamp": "2026-08-01T00:00:00Z",
            "change": None,
        }]
        self.assertIsNone(find_cached_fix("s1", None, history))

    def test_no_match_returns_none(self):
        self.assertIsNone(find_cached_fix("nope", "nope", []))


class VerifyFixStillPresentTests(unittest.TestCase):
    def test_returns_true_when_after_text_present_near_line(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            f = repo_root / "A.java"
            f.write_text("\n".join(f"line {i}" for i in range(1, 21)) + "\n", encoding="utf-8")
            change = {"file": "A.java", "line": 10, "after": "line 10"}
            self.assertTrue(verify_fix_still_present(change, repo_root))

    def test_returns_false_when_after_text_absent(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            f = repo_root / "A.java"
            f.write_text("nothing relevant here\n", encoding="utf-8")
            change = {"file": "A.java", "line": 1, "after": "totally different code"}
            self.assertFalse(verify_fix_still_present(change, repo_root))

    def test_returns_false_when_file_missing(self):
        change = {"file": "does-not-exist.java", "line": 1, "after": "x"}
        self.assertFalse(verify_fix_still_present(change, Path("/tmp")))

    def test_returns_false_without_after_or_file(self):
        self.assertFalse(verify_fix_still_present({}, Path("/tmp")))


if __name__ == "__main__":
    unittest.main()
