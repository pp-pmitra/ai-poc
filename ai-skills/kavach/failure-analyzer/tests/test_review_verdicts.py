from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from review_verdicts import compute_report, find_latest_entry, mark_review


class FindLatestEntryTests(unittest.TestCase):
    def test_returns_most_recent_by_timestamp(self):
        history = [
            {"scenarioName": "s1", "timestamp": "2026-08-01T00:00:00Z", "verdict": "Needs Investigation"},
            {"scenarioName": "s1", "timestamp": "2026-08-10T00:00:00Z", "verdict": "Script Issue — Fix Proposed"},
        ]
        entry = find_latest_entry(history, "s1")
        self.assertEqual(entry["timestamp"], "2026-08-10T00:00:00Z")

    def test_returns_none_when_no_match(self):
        self.assertIsNone(find_latest_entry([], "nope"))
        self.assertIsNone(find_latest_entry([{"scenarioName": "other"}], "s1"))


class MarkReviewTests(unittest.TestCase):
    def test_marks_agreed_with_timestamp_and_note(self):
        entry = {"scenarioName": "s1"}
        mark_review(entry, "agreed", "confirmed via dev")
        self.assertEqual(entry["review"]["status"], "agreed")
        self.assertEqual(entry["review"]["note"], "confirmed via dev")
        self.assertIsNotNone(entry["review"]["reviewedAt"])

    def test_marks_disagreed_without_note(self):
        entry = {"scenarioName": "s1"}
        mark_review(entry, "disagreed", None)
        self.assertEqual(entry["review"]["status"], "disagreed")
        self.assertIsNone(entry["review"]["note"])

    def test_rejects_invalid_status(self):
        with self.assertRaises(ValueError):
            mark_review({}, "unreviewed", None)
        with self.assertRaises(ValueError):
            mark_review({}, "maybe", None)


class ComputeReportTests(unittest.TestCase):
    def test_empty_history_reports_none_rates(self):
        report = compute_report([])
        self.assertEqual(report["totalEntries"], 0)
        self.assertEqual(report["reviewedCount"], 0)
        self.assertIsNone(report["overallAgreementRate"])
        self.assertIsNone(report["productBugAgreementRate"])
        self.assertIsNone(report["confirmedProductBugAgreementRate"])

    def test_unreviewed_entries_are_excluded_from_rates(self):
        history = [
            {"scenarioName": "s1", "verdict": "Needs Investigation", "review": {"status": "unreviewed"}},
            {"scenarioName": "s2", "verdict": "Needs Investigation"},  # no review field at all
        ]
        report = compute_report(history)
        self.assertEqual(report["totalEntries"], 2)
        self.assertEqual(report["reviewedCount"], 0)
        self.assertEqual(report["unreviewedCount"], 2)

    def test_overall_agreement_rate_across_all_reviewed(self):
        history = [
            {"scenarioName": "s1", "verdict": "Needs Investigation", "review": {"status": "agreed"}},
            {"scenarioName": "s2", "verdict": "Needs Investigation", "review": {"status": "agreed"}},
            {"scenarioName": "s3", "verdict": "Needs Investigation", "review": {"status": "disagreed"}},
        ]
        report = compute_report(history)
        self.assertEqual(report["reviewedCount"], 3)
        self.assertAlmostEqual(report["overallAgreementRate"], 2 / 3)

    def test_product_bug_rate_only_counts_product_bug_verdicts(self):
        history = [
            {"scenarioName": "s1", "verdict": "Product Bug — Confirmed", "review": {"status": "agreed"}},
            {"scenarioName": "s2", "verdict": "Product Bug — Suspected", "review": {"status": "disagreed"}},
            {"scenarioName": "s3", "verdict": "Script Issue — Fix Proposed", "review": {"status": "agreed"}},
        ]
        report = compute_report(history)
        self.assertEqual(report["productBugReviewedCount"], 2)
        self.assertAlmostEqual(report["productBugAgreementRate"], 0.5)
        # Overall rate still includes the script-issue entry too.
        self.assertAlmostEqual(report["overallAgreementRate"], 2 / 3)

    def test_confirmed_only_rate_excludes_suspected(self):
        history = [
            {"scenarioName": "s1", "verdict": "Product Bug — Confirmed", "review": {"status": "agreed"}},
            {"scenarioName": "s2", "verdict": "Product Bug — Confirmed", "review": {"status": "disagreed"}},
            {"scenarioName": "s3", "verdict": "Product Bug — Suspected", "review": {"status": "agreed"}},
        ]
        report = compute_report(history)
        self.assertEqual(report["confirmedProductBugReviewedCount"], 2)
        self.assertAlmostEqual(report["confirmedProductBugAgreementRate"], 0.5)

    def test_disagreed_entries_are_listed_with_notes(self):
        history = [
            {
                "scenarioName": "s1",
                "verdict": "Product Bug — Confirmed",
                "timestamp": "2026-08-20T10:00:00Z",
                "review": {"status": "disagreed", "note": "actually a stale locator"},
            },
        ]
        report = compute_report(history)
        self.assertEqual(len(report["disagreedEntries"]), 1)
        self.assertEqual(report["disagreedEntries"][0]["note"], "actually a stale locator")
        self.assertEqual(report["disagreedEntries"][0]["scenarioName"], "s1")


if __name__ == "__main__":
    unittest.main()
