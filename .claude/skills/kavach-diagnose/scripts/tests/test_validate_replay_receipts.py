from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from validate_replay_receipts import combined_summary, validate_receipt


_VALID_ARTIFACTS = {
    "domStructureChecked": "evaluate(el => el.outerHTML) on the nearby container returned '<div class=\"empty-state\">No campaigns found</div>' — target row absent",
    "staleLocatorRuledOut": "page.locator(\"//button[normalize-space(text())='Lifetime']\").count() -> 0 after trying the corrected normalize-space variant too",
    "testDataOrEnvironmentRuledOut": "searched for 'AutoSegment747695' in the Demo account — 0 rows returned, confirmed environment is Demo via account switcher",
}


class ValidateReplayReceiptsTests(unittest.TestCase):
    def test_confirmed_product_bug_requires_every_gate(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {
                "userLevelBehaviorReproduced": True,
                "targetAffordanceMissingOrBroken": True,
                "domStructureChecked": False,
                "staleLocatorRuledOut": True,
                "testDataOrEnvironmentRuledOut": True,
            },
            "productBugArtifacts": _VALID_ARTIFACTS,
            "evidence": ["count=0"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")
        self.assertIn("productBugGate.domStructureChecked was not true", problems)

    def test_valid_confirmed_product_bug_passes(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {
                "userLevelBehaviorReproduced": True,
                "targetAffordanceMissingOrBroken": True,
                "domStructureChecked": True,
                "staleLocatorRuledOut": True,
                "testDataOrEnvironmentRuledOut": True,
            },
            "productBugArtifacts": _VALID_ARTIFACTS,
            "evidence": ["url unchanged after click"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "confirmed_product_bug")
        self.assertEqual(problems, [])

    def test_unknown_verdict_becomes_needs_investigation(self):
        verdict, problems = validate_receipt({"verdict": "probably_buggy"})

        self.assertEqual(verdict, "needs_investigation")
        self.assertEqual(problems, ["unknown verdict 'probably_buggy'"])

    def test_confirmed_product_bug_with_no_artifacts_is_downgraded(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {k: True for k in (
                "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
            )},
            "evidence": ["something"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")
        self.assertTrue(any("productBugArtifacts.domStructureChecked" in p for p in problems))
        self.assertTrue(any("productBugArtifacts.staleLocatorRuledOut" in p for p in problems))
        self.assertTrue(any("productBugArtifacts.testDataOrEnvironmentRuledOut" in p for p in problems))

    def test_confirmed_product_bug_with_placeholder_testdata_artifact_is_downgraded(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {k: True for k in (
                "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
            )},
            "productBugArtifacts": {
                "domStructureChecked": _VALID_ARTIFACTS["domStructureChecked"],
                "staleLocatorRuledOut": _VALID_ARTIFACTS["staleLocatorRuledOut"],
                "testDataOrEnvironmentRuledOut": "confirmed test data and environment were both fine after checking",
            },
            "evidence": ["something"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")
        self.assertTrue(any("productBugArtifacts.testDataOrEnvironmentRuledOut" in p for p in problems))

    def test_confirmed_product_bug_with_placeholder_artifacts_is_downgraded(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {k: True for k in (
                "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
            )},
            "productBugArtifacts": {"domStructureChecked": "verified", "staleLocatorRuledOut": "true"},
            "evidence": ["something"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")

    def test_confirmed_product_bug_with_too_short_artifact_is_downgraded(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {k: True for k in (
                "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
            )},
            "productBugArtifacts": {"domStructureChecked": _VALID_ARTIFACTS["domStructureChecked"], "staleLocatorRuledOut": "0"},
            "evidence": ["something"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")

    def test_suspected_product_bug_is_not_held_to_the_full_artifact_requirement(self):
        # Partial verification is exactly what "suspected" already means — the
        # productBugArtifacts requirement only applies to the stronger "confirmed"
        # claim — but it must still prove SOME live replay happened (see below).
        receipt = {"verdict": "suspected_product_bug", "liveReplayPerformed": True, "evidence": ["partial check only"]}

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")
        self.assertEqual(problems, [])

    def test_suspected_product_bug_without_live_replay_is_downgraded(self):
        receipt = {"verdict": "suspected_product_bug", "evidence": ["something"]}

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "needs_investigation")
        self.assertIn("liveReplayPerformed was not true", problems)

    def test_suspected_product_bug_with_no_evidence_is_downgraded(self):
        receipt = {"verdict": "suspected_product_bug", "liveReplayPerformed": True}

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "needs_investigation")
        self.assertIn("evidence was empty", problems)

    def test_script_issue_fix_proposed_with_no_evidence_is_downgraded(self):
        """Regression test: this verdict most directly leads to a real code
        change via kavach-repair, but previously had zero mechanical gate at
        all — a bare-minimum {"verdict": "script_issue_fix_proposed"} used to
        pass straight through unchanged."""
        receipt = {"verdict": "script_issue_fix_proposed", "recommendedAction": "fix it"}

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "needs_investigation")
        self.assertIn("evidence was empty", problems)

    def test_script_issue_fix_proposed_with_evidence_passes_without_live_replay(self):
        # Deliberately NOT held to suspected_product_bug's liveReplayPerformed
        # bar -- a genuine Tier-1 (no-browser) static resolution is a valid
        # source for this verdict and must not be rejected for lacking a
        # field that tier never sets.
        receipt = {
            "verdict": "script_issue_fix_proposed",
            "evidence": ["Expected `Save` vs actual `save` differ only by case."],
            "recommendedAction": "Update the literal expected text.",
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "script_issue_fix_proposed")
        self.assertEqual(problems, [])

    def test_not_reproduced_passed_live_without_live_replay_is_downgraded(self):
        receipt = {"verdict": "not_reproduced_passed_live", "evidence": ["scenario completed"]}

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "needs_investigation")
        self.assertIn("liveReplayPerformed was not true", problems)

    def test_not_reproduced_passed_live_with_live_replay_and_evidence_passes(self):
        receipt = {"verdict": "not_reproduced_passed_live", "liveReplayPerformed": True, "evidence": ["scenario completed past the fix point"]}

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "not_reproduced_passed_live")
        self.assertEqual(problems, [])

    def test_non_dict_product_bug_artifacts_does_not_crash(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {k: True for k in (
                "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
            )},
            "productBugArtifacts": "see evidence above",  # malformed: a string, not a dict
            "evidence": ["something"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")
        self.assertTrue(any("productBugArtifacts.domStructureChecked" in p for p in problems))

    def test_fabricated_prose_artifact_without_dom_or_count_signal_is_downgraded(self):
        receipt = {
            "verdict": "confirmed_product_bug",
            "backgroundMatched": True,
            "liveReplayPerformed": True,
            "alternateValidPathFound": False,
            "productBugGate": {k: True for k in (
                "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
            )},
            "productBugArtifacts": {
                "domStructureChecked": "the target element was definitely missing from the DOM after a thorough check",
                "staleLocatorRuledOut": _VALID_ARTIFACTS["staleLocatorRuledOut"],
            },
            "evidence": ["something"],
        }

        verdict, problems = validate_receipt(receipt)

        self.assertEqual(verdict, "suspected_product_bug")
        self.assertTrue(any("doesn't look like a real observed artifact" in p for p in problems))


class CombinedSummaryTests(unittest.TestCase):
    def _write(self, path: Path, data) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data), encoding="utf-8")

    def test_merges_resolved_triage_group_and_escalated_replay_group(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"
            replay_dir = tmp_dir / "replay-packets" / "ts2"

            receipt_path = triage_dir / "receipts" / "001.receipt.json"
            self._write(receipt_path, {
                "groupId": "g1", "representativeScenario": "A", "affectedScenarios": ["A"],
                "verdict": "not_reproduced_intermittent", "confidence": "high",
                "evidence": ["passed elsewhere"], "analysisTier": "tier0_intermittent",
            })
            self._write(triage_dir / "manifest.json", {
                "groups": [
                    {"groupId": "g1", "scenarioCount": 1, "representativeScenario": "A",
                     "resolved": True, "receiptPath": str(receipt_path)},
                    {"groupId": "g2", "scenarioCount": 1, "representativeScenario": "B",
                     "resolved": False, "receiptPath": None},
                ],
            })

            self._write(replay_dir / "receipts" / "g2.receipt.json", {
                "groupId": "g2", "representativeScenario": "B", "affectedScenarios": ["B"],
                "verdict": "confirmed_product_bug", "confidence": "high",
                "backgroundMatched": True, "liveReplayPerformed": True, "alternateValidPathFound": False,
                "productBugGate": {k: True for k in (
                    "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                    "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
                )},
                "productBugArtifacts": _VALID_ARTIFACTS,
                "evidence": ["clicked X, nothing changed"],
            })
            self._write(replay_dir / "manifest.json", {
                "totalFailures": 1,
                "groups": [{"groupId": "g2", "prompt": "g2.prompt.md", "scenarioCount": 1, "representativeScenario": "B"}],
            })

            result = combined_summary(triage_dir / "manifest.json", replay_dir / "manifest.json")

            self.assertEqual(result["groupCount"], 2)
            rows_by_group = {r["groupId"]: r for r in result["groups"]}
            self.assertEqual(rows_by_group["g1"]["verdict"], "not_reproduced_intermittent")
            self.assertEqual(rows_by_group["g1"]["analysisTier"], "tier0_intermittent")
            self.assertEqual(rows_by_group["g2"]["verdict"], "confirmed_product_bug")
            self.assertEqual(rows_by_group["g2"]["analysisTier"], "tier2_live_replay")

    def test_tier1_receipt_is_independently_revalidated_not_trusted_blindly(self):
        # Even though enforce_tier1_verdict_constraints already prevents this
        # upstream, combined_summary must still run every receipt (Tier 1
        # included) through validate_receipt() -- defense in depth.
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"
            receipt_path = triage_dir / "receipts" / "001.receipt.json"
            # A malformed/tampered receipt that shouldn't exist from the real
            # pipeline, but combined_summary must not trust it blindly.
            self._write(receipt_path, {"groupId": "g1", "verdict": "confirmed_product_bug"})
            self._write(triage_dir / "manifest.json", {
                "groups": [{"groupId": "g1", "scenarioCount": 1, "representativeScenario": "A",
                            "resolved": True, "receiptPath": str(receipt_path)}],
            })

            result = combined_summary(triage_dir / "manifest.json", None)

            self.assertEqual(result["groups"][0]["verdict"], "suspected_product_bug")
            self.assertTrue(result["groups"][0]["gateProblems"])
            self.assertEqual(result["metrics"]["suspectedProductBugGroups"], 1)
            # This was downgraded from confirmed to suspected, so it is tracked
            # as a false product-bug-strength claim.
            self.assertEqual(result["metrics"]["falseProductBugDowngrades"], 1)

    def test_escalated_group_missing_from_replay_receipts_is_not_dropped(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"
            self._write(triage_dir / "manifest.json", {
                "groups": [{"groupId": "g1", "scenarioCount": 1, "representativeScenario": "A",
                            "resolved": False, "receiptPath": None}],
            })

            result = combined_summary(triage_dir / "manifest.json", None)

            self.assertEqual(result["groupCount"], 1)
            self.assertEqual(result["groups"][0]["verdict"], "needs_investigation")
            self.assertEqual(result["groups"][0]["groupId"], "g1")

    def test_blocked_reason_is_recorded_on_receiptless_escalated_groups_only(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"
            self._write(triage_dir / "manifest.json", {
                "groups": [{"groupId": "g1", "scenarioCount": 1, "representativeScenario": "A",
                            "resolved": False, "receiptPath": None}],
            })

            result = combined_summary(triage_dir / "manifest.json", None, blocked_reason="CDP_ENDPOINT_DEAD: 9223")
            unblocked = combined_summary(triage_dir / "manifest.json", None)

            self.assertEqual(result["groups"][0]["verdict"], "needs_investigation")
            self.assertEqual(result["groups"][0]["blockedReason"], "CDP_ENDPOINT_DEAD: 9223")
            self.assertNotIn("blockedReason", unblocked["groups"][0])

    def test_total_failures_summed_from_rows_not_a_stale_source_count(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"
            self._write(triage_dir / "manifest.json", {
                "groups": [
                    {"groupId": "g1", "scenarioCount": 3, "representativeScenario": "A", "resolved": False, "receiptPath": None},
                    {"groupId": "g2", "scenarioCount": 2, "representativeScenario": "B", "resolved": False, "receiptPath": None},
                ],
            })

            result = combined_summary(triage_dir / "manifest.json", None)

            self.assertEqual(result["totalFailures"], 5)


if __name__ == "__main__":
    unittest.main()
