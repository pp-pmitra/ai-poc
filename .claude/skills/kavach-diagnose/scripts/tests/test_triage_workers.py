from __future__ import annotations

import argparse
import sys
import unittest
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import triage_workers
from triage_workers import _frequency_by_scenario
from failure_analyzer.triage.static_classifier import disposition
from failure_analyzer.triage.llm_static_triage import enforce_tier1_verdict_constraints

_SCRIPTS_DIR = Path(__file__).resolve().parent.parent
_REPO_ROOT = _SCRIPTS_DIR.parents[3]


class DocumentedCliInvocationTests(unittest.TestCase):
    """Guards the specific regression class the review found: failure-triage/
    SKILL.md's Phase 2.5 previously documented `-i`/`--fix-history`/`-o`
    overrides that were relative paths resolved against the wrong cwd,
    crashing with FileNotFoundError before any real triage logic ran. The
    fixed SKILL.md now runs `triage_workers.py` with no path arguments at all
    and relies entirely on these defaults -- so this test asserts the
    defaults themselves resolve under the real repo-root `kavach-data/`
    directory, not a path relative to the scripts/ directory."""

    def test_no_arg_invocation_uses_repo_root_relative_defaults(self):
        with mock.patch.object(sys, "argv", ["triage_workers.py"]):
            args = triage_workers.parse_args()

        self.assertEqual(args.input, _REPO_ROOT / "kavach-data" / "history" / "failures-for-replay.json")
        self.assertEqual(args.fix_history, _REPO_ROOT / "kavach-data" / "history" / "fix-history.json")
        self.assertEqual(args.out_root, _REPO_ROOT / "kavach-data" / "history" / "triage-results")
        # None of these should ever be a bare 'history/...' path -- that shape
        # only resolves correctly if the caller happens to already be sitting
        # in kavach-data/, which the documented `cd .../scripts && ...`
        # invocation never is.
        for path in (args.input, args.fix_history, args.out_root):
            self.assertTrue(path.is_absolute(), f"{path} must be an absolute, repo-root-anchored default")


class StrictModeViolationPipelineTests(unittest.TestCase):
    """Regression coverage for a real pipeline gap the review found:
    disposition()'s strict-mode-violation branch used to claim
    resolved=True/script_issue_fix_proposed with no proposedChange, which
    enforce_tier1_verdict_constraints() below (the gate run_triage() always
    applies before accepting a Tier-1 result) would silently downgrade
    anyway -- but the unit tests for disposition() in isolation asserted the
    pre-downgrade shape, certifying behavior the real pipeline could never
    produce. This exercises both functions in the same sequence run_triage()
    does, so a future change to either one can't silently reopen that gap
    without a failing test here."""

    def test_strict_mode_violation_is_escalated_not_finalized(self):
        failure = {
            "scenarioName": "s",
            "errorMessage": "Error: strict mode violation: locator('button') resolved to 3 elements",
        }
        disp = disposition("any-cause", failure, Path("."))
        if disp["resolved"]:
            disp = enforce_tier1_verdict_constraints(disp, Path("."))

        # This is the exact condition triage_workers.run_triage() gates
        # receipt-finalization on -- it must be False here, i.e. the group
        # gets escalated to live replay rather than finalized as a
        # script-issue fix with no verifiable proposedChange behind it.
        finalized = disp["resolved"] and not disp.get("needsLiveReplay")
        self.assertFalse(finalized)


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
