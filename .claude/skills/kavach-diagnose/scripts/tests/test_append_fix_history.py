"""Tests for append_fix_history.py — the validated, lock-safe replacement
for verdict-reporting's old hand-written fix-history.json append."""

from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from append_fix_history import append_entries, validate_entry


def _valid_entry(**overrides):
    entry = {
        "timestamp": "2026-09-24T10:00:00Z",
        "runDate": "2026-09-24",
        "scenarioName": "Some scenario",
        "featureFile": "src/test/resources/features/life/Life_Tactic.feature",
        "verdict": "script_issue_fix_proposed",
        "confidence": "high",
        "priority": "high_confidence_script_fix",
        "groupId": None,
        "isGroupRepresentative": True,
        "attempts": 1,
        "change": {"file": "x.java", "line": 1, "description": "d", "before": "a", "after": "b"},
        "liveVerification": None,
        "notes": "notes",
        "analysisTier": "tier1_deterministic",
        "review": {"status": "unreviewed", "reviewedAt": None, "note": None},
    }
    entry.update(overrides)
    return entry


class ValidateEntryTests(unittest.TestCase):
    def test_valid_entry_has_no_problems(self):
        self.assertEqual(validate_entry(_valid_entry(), 0), [])

    def test_missing_required_key_rejected(self):
        entry = _valid_entry()
        del entry["scenarioName"]
        problems = validate_entry(entry, 0)
        self.assertTrue(any("scenarioName" in p for p in problems))

    def test_unknown_verdict_rejected(self):
        problems = validate_entry(_valid_entry(verdict="totally_made_up"), 0)
        self.assertTrue(any("verdict" in p for p in problems))

    def test_script_issue_fix_applied_accepted(self):
        # Only kavach-repair writes this one, never a combined-receipts.json
        # row — must still be a valid fix-history.json verdict.
        self.assertEqual(validate_entry(_valid_entry(verdict="script_issue_fix_applied"), 0), [])

    def test_unknown_confidence_rejected(self):
        problems = validate_entry(_valid_entry(confidence="super-sure"), 0)
        self.assertTrue(any("confidence" in p for p in problems))

    def test_unknown_analysis_tier_rejected(self):
        problems = validate_entry(_valid_entry(analysisTier="tier9_made_up"), 0)
        self.assertTrue(any("analysisTier" in p for p in problems))

    def test_non_integer_attempts_rejected(self):
        problems = validate_entry(_valid_entry(attempts=0), 0)
        self.assertTrue(any("attempts" in p for p in problems))

    def test_bad_review_shape_rejected(self):
        problems = validate_entry(_valid_entry(review={"status": "agreed_i_guess"}), 0)
        self.assertTrue(any("review" in p for p in problems))

    def test_fabricated_live_verification_on_static_tier_rejected(self):
        problems = validate_entry(
            _valid_entry(analysisTier="tier1_deterministic", liveVerification={"elementFound": True}), 0
        )
        self.assertTrue(any("liveVerification" in p for p in problems))

    def test_real_live_verification_on_live_tier_accepted(self):
        entry = _valid_entry(
            analysisTier="tier2_live_replay",
            liveVerification={"elementFound": True, "scenarioContinuedPastFixPoint": True},
        )
        self.assertEqual(validate_entry(entry, 0), [])

    def test_non_dict_entry_rejected(self):
        problems = validate_entry("not a dict", 0)
        self.assertTrue(any("expected an object" in p for p in problems))


class AppendEntriesTests(unittest.TestCase):
    def test_append_to_missing_file_creates_it(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "history" / "fix-history.json"
            total = append_entries(path, [_valid_entry()])
            self.assertEqual(total, 1)
            self.assertEqual(len(json.loads(path.read_text())), 1)

    def test_sequential_appends_accumulate(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "fix-history.json"
            append_entries(path, [_valid_entry(scenarioName="A")])
            total = append_entries(path, [_valid_entry(scenarioName="B"), _valid_entry(scenarioName="C")])
            self.assertEqual(total, 3)
            names = [e["scenarioName"] for e in json.loads(path.read_text())]
            self.assertEqual(names, ["A", "B", "C"])

    def test_existing_entries_are_preserved(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "fix-history.json"
            path.write_text(json.dumps([_valid_entry(scenarioName="Pre-existing")]))
            append_entries(path, [_valid_entry(scenarioName="New")])
            names = [e["scenarioName"] for e in json.loads(path.read_text())]
            self.assertEqual(names, ["Pre-existing", "New"])

    def test_identical_batch_resubmitted_is_not_double_appended(self):
        """A retried CI step or a rerun after an interruption can pipe the
        exact same batch through this script twice -- it must not double the
        file's length."""
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "fix-history.json"
            batch = [_valid_entry(scenarioName="A"), _valid_entry(scenarioName="B")]

            first_total = append_entries(path, batch)
            second_total = append_entries(path, batch)

            self.assertEqual(first_total, 2)
            self.assertEqual(second_total, 2)
            self.assertEqual(len(json.loads(path.read_text())), 2)

    def test_a_genuinely_new_entry_for_the_same_scenario_still_appends(self):
        """Dedup must key on more than scenarioName alone -- a later run
        re-diagnosing the same scenario (different timestamp) is new
        information, not a duplicate to be silently dropped."""
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "fix-history.json"
            append_entries(path, [_valid_entry(scenarioName="A", timestamp="2026-09-24T10:00:00Z")])
            total = append_entries(path, [_valid_entry(scenarioName="A", timestamp="2026-09-25T10:00:00Z")])

            self.assertEqual(total, 2)


if __name__ == "__main__":
    raise SystemExit(unittest.main())
