"""Integration test: the real producer's output must pass the real validator.

This is the test the kavach-remaining-work review specifically asked for —
proof that validate_kavach_contract.py actually accepts what
validate_replay_receipts.py actually writes, not a hand-rolled fixture that
merely looks plausible. It reuses combined_summary() (the real producer
function) to build the document, then feeds that exact object straight into
validate_document() (the real validator function) — no serialization
round-trip needed since both operate on plain dicts.
"""

from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from validate_replay_receipts import combined_summary
from validate_kavach_contract import validate_document, validate_group


_VALID_ARTIFACTS = {
    "domStructureChecked": "evaluate(el => el.outerHTML) on the nearby container returned '<div class=\"empty-state\">No campaigns found</div>' — target row absent",
    "staleLocatorRuledOut": "page.locator(\"//button[normalize-space(text())='Lifetime']\").count() -> 0 after trying the corrected normalize-space variant too",
}


def _write(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data), encoding="utf-8")


class ProducerConsumerIntegrationTests(unittest.TestCase):
    """Builds a realistic combined-receipts.json via the actual producer
    (mixing a Tier-1-resolved group and a Tier-2 live-replay-escalated
    confirmed_product_bug group, mirroring a real mixed run), then validates
    it with the actual consumer."""

    def test_producer_output_passes_the_real_validator(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"
            replay_dir = tmp_dir / "replay-packets" / "ts2"

            resolved_receipt = triage_dir / "receipts" / "001.receipt.json"
            _write(resolved_receipt, {
                "groupId": "g1", "representativeScenario": "A", "affectedScenarios": ["A"],
                "verdict": "not_reproduced_intermittent", "confidence": "high",
                "evidence": ["passed elsewhere"], "analysisTier": "tier0_intermittent",
            })
            _write(triage_dir / "manifest.json", {
                "groups": [
                    {"groupId": "g1", "scenarioCount": 1, "representativeScenario": "A",
                     "resolved": True, "receiptPath": str(resolved_receipt)},
                    {"groupId": "g2", "scenarioCount": 2, "representativeScenario": "B",
                     "resolved": False, "receiptPath": None},
                ],
            })

            _write(replay_dir / "receipts" / "g2.receipt.json", {
                "groupId": "g2", "representativeScenario": "B", "affectedScenarios": ["B", "B2"],
                "distinctIssueCount": 1,
                "verdict": "confirmed_product_bug", "confidence": "high",
                "backgroundMatched": True, "liveReplayPerformed": True, "alternateValidPathFound": False,
                "productBugGate": {k: True for k in (
                    "userLevelBehaviorReproduced", "targetAffordanceMissingOrBroken",
                    "domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut",
                )},
                "productBugArtifacts": _VALID_ARTIFACTS,
                "evidence": ["clicked X, nothing changed"],
                "recommendedAction": "Flag for product team — not a script issue",
            })
            _write(replay_dir / "manifest.json", {
                "totalFailures": 2,
                "groups": [{"groupId": "g2", "prompt": "g2.prompt.md", "scenarioCount": 2, "representativeScenario": "B"}],
            })

            document = combined_summary(triage_dir / "manifest.json", replay_dir / "manifest.json")

        problems = validate_document(document)

        self.assertEqual(problems, [], f"real producer output failed the real validator: {problems}")

    def test_producer_output_round_trips_through_json_and_still_passes(self):
        """Same as above, but also proves the document survives a real
        json.dumps/json.loads round trip unchanged — the exact path CI takes
        (write combined-receipts.json to disk, read it back for validation)."""
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            triage_dir = tmp_dir / "triage-results" / "ts1"

            _write(triage_dir / "manifest.json", {
                "groups": [
                    {"groupId": "g1", "scenarioCount": 1, "representativeScenario": "A",
                     "resolved": False, "receiptPath": None},
                ],
            })

            document = combined_summary(triage_dir / "manifest.json", None)
            out_path = tmp_dir / "combined-receipts.json"
            out_path.write_text(json.dumps(document, indent=2, sort_keys=True), encoding="utf-8")
            reloaded = json.loads(out_path.read_text(encoding="utf-8"))

        problems = validate_document(reloaded)
        self.assertEqual(problems, [])


class ValidatorRejectsRealisticBreakageTests(unittest.TestCase):
    """Confirms the validator actually rejects the malformed shapes the
    review specifically called out, using the same document shape the
    producer emits (not the old, wrong bare-array/rows/receipts shape)."""

    def test_missing_groups_key_rejected(self):
        problems = validate_document({"totalFailures": 0, "groupCount": 0, "summaryByGroup": {},
                                       "affectedScenariosByVerdict": {}, "distinctIssuesByVerdict": {},
                                       "metrics": {"totalGroups": 0, "confirmedProductBugGroups": 0,
                                                    "suspectedProductBugGroups": 0, "falseProductBugDowngrades": 0}})
        self.assertTrue(any("groups" in p for p in problems))

    def test_empty_groups_rejected(self):
        problems = validate_document({"totalFailures": 0, "groupCount": 0, "summaryByGroup": {},
                                       "affectedScenariosByVerdict": {}, "distinctIssuesByVerdict": {},
                                       "metrics": {"totalGroups": 0, "confirmedProductBugGroups": 0,
                                                    "suspectedProductBugGroups": 0, "falseProductBugDowngrades": 0},
                                       "groups": []})
        self.assertTrue(any("non-empty" in p for p in problems))

    def test_old_rows_key_document_is_rejected_not_silently_accepted(self):
        """The exact bug the review found: the old validator looked for
        data.get('rows', data.get('receipts')) instead of 'groups'. Confirms
        that shape is now correctly rejected rather than silently passing
        with 0 rows checked."""
        old_shape = {"rows": [{"groupId": "g1", "verdict": "needs_investigation",
                                "reportVerdict": "Needs Investigation", "confidence": "low",
                                "gateProblems": [], "evidence": [], "analysisTier": "tier1_deterministic",
                                "scenarioCount": 1}]}
        problems = validate_document(old_shape)
        self.assertTrue(any("groups" in p for p in problems))

    def test_unknown_verdict_rejected(self):
        problems = validate_group({
            "groupId": "g1", "scenarioCount": 1, "verdict": "totally_made_up",
            "reportVerdict": "?", "confidence": "high", "gateProblems": [],
            "evidence": [], "analysisTier": "tier2_live_replay",
        }, 0)
        self.assertTrue(any("not one of" in p for p in problems))

    def test_unknown_analysis_tier_rejected(self):
        problems = validate_group({
            "groupId": "g1", "scenarioCount": 1, "verdict": "needs_investigation",
            "reportVerdict": "Needs Investigation", "confidence": "high", "gateProblems": [],
            "evidence": [], "analysisTier": "made_up_tier",
        }, 0)
        self.assertTrue(any("analysisTier" in p for p in problems))

    def test_confirmed_product_bug_with_nonempty_gate_problems_rejected(self):
        """This shape can never come from the real producer (validate_receipt
        downgrades on any gate problem), but a hand-edited or corrupted
        document could still claim it — the validator must catch it."""
        problems = validate_group({
            "groupId": "g1", "scenarioCount": 1, "verdict": "confirmed_product_bug",
            "reportVerdict": "Product Bug — Confirmed", "confidence": "high",
            "gateProblems": ["productBugGate.domStructureChecked was not true"],
            "evidence": ["something"], "analysisTier": "tier2_live_replay",
        }, 0)
        self.assertTrue(any("gateProblems is non-empty" in p for p in problems))

    def test_evidence_with_non_string_items_rejected(self):
        problems = validate_group({
            "groupId": "g1", "scenarioCount": 1, "verdict": "needs_investigation",
            "reportVerdict": "Needs Investigation", "confidence": "low", "gateProblems": [],
            "evidence": [123, None], "analysisTier": "tier1_deterministic",
        }, 0)
        self.assertTrue(any("evidence must be a list of strings" in p for p in problems))

    def test_empty_evidence_rejected_for_verdicts_that_require_it(self):
        problems = validate_group({
            "groupId": "g1", "scenarioCount": 1, "verdict": "suspected_product_bug",
            "reportVerdict": "Product Bug — Suspected", "confidence": "medium", "gateProblems": [],
            "evidence": [], "analysisTier": "tier2_live_replay",
        }, 0)
        self.assertTrue(any("evidence must be non-empty" in p for p in problems))

    def test_negative_scenario_count_rejected(self):
        problems = validate_group({
            "groupId": "g1", "scenarioCount": -1, "verdict": "needs_investigation",
            "reportVerdict": "Needs Investigation", "confidence": "low", "gateProblems": [],
            "evidence": [], "analysisTier": "tier1_deterministic",
        }, 0)
        self.assertTrue(any("scenarioCount" in p for p in problems))

    def test_zero_distinct_issue_count_rejected(self):
        problems = validate_group({
            "groupId": "g1", "scenarioCount": 1, "distinctIssueCount": 0,
            "verdict": "needs_investigation", "reportVerdict": "Needs Investigation",
            "confidence": "low", "gateProblems": [], "evidence": [],
            "analysisTier": "tier1_deterministic",
        }, 0)
        self.assertTrue(any("distinctIssueCount" in p for p in problems))

    def test_group_count_mismatch_rejected(self):
        problems = validate_document({
            "totalFailures": 1, "groupCount": 5, "summaryByGroup": {"needs_investigation": 1},
            "affectedScenariosByVerdict": {"needs_investigation": 1},
            "distinctIssuesByVerdict": {"needs_investigation": 1},
            "metrics": {"totalGroups": 1, "confirmedProductBugGroups": 0,
                        "suspectedProductBugGroups": 0, "falseProductBugDowngrades": 0},
            "groups": [{"groupId": "g1", "scenarioCount": 1, "verdict": "needs_investigation",
                        "reportVerdict": "Needs Investigation", "confidence": "low",
                        "gateProblems": [], "evidence": [], "analysisTier": "tier1_deterministic"}],
        })
        self.assertTrue(any("groupCount" in p for p in problems))

    def test_summary_totals_disagreeing_with_groups_rejected(self):
        problems = validate_document({
            "totalFailures": 1, "groupCount": 1,
            "summaryByGroup": {"confirmed_product_bug": 99},
            "affectedScenariosByVerdict": {"needs_investigation": 1},
            "distinctIssuesByVerdict": {"needs_investigation": 1},
            "metrics": {"totalGroups": 1, "confirmedProductBugGroups": 0,
                        "suspectedProductBugGroups": 0, "falseProductBugDowngrades": 0},
            "groups": [{"groupId": "g1", "scenarioCount": 1, "verdict": "needs_investigation",
                        "reportVerdict": "Needs Investigation", "confidence": "low",
                        "gateProblems": [], "evidence": [], "analysisTier": "tier1_deterministic"}],
        })
        self.assertTrue(any("summaryByGroup" in p for p in problems))


if __name__ == "__main__":
    unittest.main()
