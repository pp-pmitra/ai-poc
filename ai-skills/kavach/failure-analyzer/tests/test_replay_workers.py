from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from replay_workers import build_packet, build_packets, build_prompt, redact_text


class ReplayWorkerRedactionTests(unittest.TestCase):
    def test_redacts_secret_like_keys_and_values(self):
        raw = {
            "password": "plain",
            "nested": {
                "clientSecret": "secret-value",
                "message": "apiKey = abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456",
            },
            "tokenish": "eyJ" + "a" * 30 + "." + "b" * 30 + "." + "c" * 20,
        }

        redacted = redact_text(raw)

        serialized = json.dumps(redacted)
        self.assertNotIn("plain", serialized)
        self.assertNotIn("secret-value", serialized)
        self.assertNotIn("abcdefghijklmnopqrstuvwxyz", serialized)
        self.assertIn("<redacted", serialized)


class ReplayWorkerPacketTests(unittest.TestCase):
    def test_packet_is_group_scoped_and_page_text_is_capped(self):
        failure = {
            "scenarioName": "Scenario A",
            "featureFile": "file:src/test/resources/features/life/Life_LineItem.feature",
            "tags": ["@regression"],
            "failedStep": "When user clicks something",
            "errorMessage": "TimeoutError: password=should-not-leak",
            "errorLocation": {
                "feature": {
                    "file": "src/test/resources/features/life/Life_LineItem.feature",
                    "line": 115,
                }
            },
            "pageText": "x" * 500,
            "stepReliability": {"passed": 0, "failed": 1, "likelyIntermittent": False},
            "groupId": "TimeoutError|when user clicks something",
        }
        group = {
            "groupId": failure["groupId"],
            "rootCauseLabel": "TimeoutError",
            "normalizedStep": "when user clicks something",
            "affectedFeatures": [failure["featureFile"]],
            "failures": [failure],
        }

        packet = build_packet(group, Path(__file__).resolve().parents[2], page_text_cap=100)
        prompt = build_prompt(packet)

        self.assertEqual(packet["representativeScenario"], "Scenario A")
        self.assertIn("...(truncated)", packet["failures"][0]["pageText"])
        self.assertNotIn("should-not-leak", json.dumps(packet))
        self.assertIn("Return JSON only", prompt)
        self.assertIn("productBugGate", prompt)
        self.assertNotIn("Live-replay verdict", prompt)
        self.assertNotIn("Confirmed Product Bugs", prompt)

    def test_all_intermittent_group_marks_skip_flag(self):
        failure = {
            "scenarioName": "Flaky Scenario",
            "featureFile": "file:features/Foo.feature",
            "failedStep": "Then flaky step",
            "stepReliability": {"passed": 9, "failed": 1, "likelyIntermittent": True},
            "groupId": "TimeoutError|then flaky step",
        }
        group = {"groupId": failure["groupId"], "failures": [failure]}

        packet = build_packet(group, Path.cwd(), page_text_cap=100)

        self.assertTrue(packet["allFailuresLikelyIntermittent"])
        self.assertEqual(packet["representativeScenario"], "Flaky Scenario")


class BuildPacketsManifestTests(unittest.TestCase):
    def _write_input(self, tmp_dir: Path) -> Path:
        data = {
            "totalScenarios": 3,
            "failedCount": 3,
            "failures": [
                {"scenarioName": "A", "groupId": "g1", "featureFile": "f.feature", "failedStep": "s"},
                {"scenarioName": "B", "groupId": "g1", "featureFile": "f.feature", "failedStep": "s"},
                {"scenarioName": "C", "groupId": "g2", "featureFile": "f.feature", "failedStep": "s2"},
            ],
            "groups": [
                {"groupId": "g1", "rootCauseLabel": "TimeoutError", "normalizedStep": "s", "affectedFeatures": ["f.feature"]},
                {"groupId": "g2", "rootCauseLabel": "TimeoutError", "normalizedStep": "s2", "affectedFeatures": ["f.feature"]},
            ],
        }
        input_path = tmp_dir / "failures-for-replay.json"
        input_path.write_text(json.dumps(data), encoding="utf-8")
        return input_path

    def test_total_failures_matches_full_scope_when_unfiltered(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            input_path = self._write_input(tmp_dir)
            out_dir = build_packets(
                input_path=input_path, out_root=tmp_dir / "out", repo_root=tmp_dir,
                page_text_cap=100, write_runner_script=False, claude_args=[],
            )
            manifest = json.loads((out_dir / "manifest.json").read_text())
            self.assertEqual(manifest["groupCount"], 2)
            self.assertEqual(manifest["totalFailures"], 3)

    def test_total_failures_matches_filtered_scope_with_only_groups(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            input_path = self._write_input(tmp_dir)
            out_dir = build_packets(
                input_path=input_path, out_root=tmp_dir / "out", repo_root=tmp_dir,
                page_text_cap=100, write_runner_script=False, claude_args=[],
                only_group_ids={"g2"},
            )
            manifest = json.loads((out_dir / "manifest.json").read_text())
            self.assertEqual(manifest["groupCount"], 1)
            # Must reflect ONLY the 1 failure in g2, not all 3 in the input file.
            self.assertEqual(manifest["totalFailures"], 1)


if __name__ == "__main__":
    unittest.main()
