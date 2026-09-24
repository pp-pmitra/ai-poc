from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from triage_workers import _frequency_by_scenario


class FrequencyByScenarioTests(unittest.TestCase):
    def test_scopes_by_matching_group_id_not_just_scenario_name(self):
        # Two prior entries for "S": one under the CURRENT run's groupId (g1),
        # one under an unrelated groupId (g2, a different underlying problem).
        # Only the g1 entry should count toward "S"'s frequency.
        fix_history = [
            {"scenarioName": "S", "groupId": "g1", "verdict": "Script Issue — Fix Proposed", "timestamp": "2026-08-01"},
            {"scenarioName": "S", "groupId": "g2", "verdict": "Product Bug — Confirmed", "timestamp": "2026-08-05"},
        ]
        failures = [{"scenarioName": "S", "groupId": "g1"}]

        freq = _frequency_by_scenario(fix_history, failures)

        self.assertEqual(freq["S"]["totalRuns"], 1)
        self.assertIsNone(freq["S"]["pattern"])  # only 1 signature-scoped prior -> no pattern

    def test_recurring_pattern_set_when_two_or_more_signature_scoped_priors(self):
        fix_history = [
            {"scenarioName": "S", "groupId": "g1", "verdict": "Script Issue — Fix Proposed", "timestamp": "2026-08-01"},
            {"scenarioName": "S", "groupId": "g1", "verdict": "Needs Investigation", "timestamp": "2026-08-05"},
        ]
        failures = [{"scenarioName": "S", "groupId": "g1"}]

        freq = _frequency_by_scenario(fix_history, failures)

        self.assertEqual(freq["S"]["totalRuns"], 2)
        self.assertEqual(freq["S"]["pattern"], "recurring")

    def test_falls_back_to_all_scenario_entries_when_current_group_id_unknown(self):
        # The current run's failures list doesn't have this scenario (edge
        # case) -- fall back to counting by scenarioName alone rather than
        # silently reporting zero history.
        fix_history = [
            {"scenarioName": "S", "groupId": "g1", "verdict": "Needs Investigation", "timestamp": "2026-08-01"},
        ]
        freq = _frequency_by_scenario(fix_history, failures=[])

        self.assertEqual(freq["S"]["totalRuns"], 1)

    def test_not_reproduced_verdicts_count_as_passed_not_failed(self):
        fix_history = [
            {"scenarioName": "S", "groupId": "g1", "verdict": "Not Reproduced — Intermittent", "timestamp": "2026-08-01"},
            {"scenarioName": "S", "groupId": "g1", "verdict": "Script Issue — Fix Proposed", "timestamp": "2026-08-05"},
        ]
        failures = [{"scenarioName": "S", "groupId": "g1"}]

        freq = _frequency_by_scenario(fix_history, failures)

        self.assertEqual(freq["S"]["passedRuns"], 1)
        self.assertEqual(freq["S"]["failedRuns"], 1)

    def test_scenario_with_no_prior_history_is_absent(self):
        freq = _frequency_by_scenario([], failures=[{"scenarioName": "New", "groupId": "g1"}])
        self.assertEqual(freq, {})


if __name__ == "__main__":
    unittest.main()
