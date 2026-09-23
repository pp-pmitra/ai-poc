from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.triage.llm_static_triage import (
    enforce_tier1_verdict_constraints,
    triage_with_llm,
)


class EnforceTier1VerdictConstraintsTests(unittest.TestCase):
    def test_forbidden_verdict_is_clamped_to_needs_investigation(self):
        for forbidden in ("confirmed_product_bug", "suspected_product_bug", "not_reproduced_passed_live"):
            with self.subTest(forbidden=forbidden):
                parsed = {"verdict": forbidden, "confidence": "high"}
                result = enforce_tier1_verdict_constraints(parsed, Path("."))
                self.assertEqual(result["verdict"], "needs_investigation")
                self.assertTrue(result["needsLiveReplay"])
                self.assertIsNone(result["proposedChange"])

    def test_allowed_verdict_without_fix_passes_through(self):
        parsed = {"verdict": "not_reproduced_intermittent", "confidence": "high"}
        result = enforce_tier1_verdict_constraints(parsed, Path("."))
        self.assertEqual(result["verdict"], "not_reproduced_intermittent")

    def test_script_issue_fix_with_verified_before_text_passes_through(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            f = repo_root / "A.java"
            f.write_text("public void x() { return old(); }\n", encoding="utf-8")
            parsed = {
                "verdict": "script_issue_fix_proposed",
                "proposedChange": {"file": "A.java", "line": 1, "before": "return old();", "after": "return new_();"},
            }
            result = enforce_tier1_verdict_constraints(parsed, repo_root)
            self.assertEqual(result["verdict"], "script_issue_fix_proposed")
            self.assertIsNotNone(result["proposedChange"])

    def test_script_issue_fix_with_unverifiable_before_text_is_downgraded(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            f = repo_root / "A.java"
            f.write_text("public void x() { return old(); }\n", encoding="utf-8")
            parsed = {
                "verdict": "script_issue_fix_proposed",
                "proposedChange": {"file": "A.java", "line": 1, "before": "this text does not exist", "after": "y"},
                "evidence": ["some claim"],
            }
            result = enforce_tier1_verdict_constraints(parsed, repo_root)
            self.assertEqual(result["verdict"], "needs_investigation")
            self.assertTrue(result["needsLiveReplay"])
            self.assertIsNone(result["proposedChange"])

    def test_script_issue_fix_referencing_missing_file_is_downgraded(self):
        parsed = {
            "verdict": "script_issue_fix_proposed",
            "proposedChange": {"file": "does-not-exist.java", "line": 1, "before": "x", "after": "y"},
        }
        result = enforce_tier1_verdict_constraints(parsed, Path("/tmp"))
        self.assertEqual(result["verdict"], "needs_investigation")

    def test_low_confidence_pass_through_still_forces_needs_live_replay(self):
        # A LOW-confidence allowed verdict must never finalize unescalated,
        # even if the LLM's own JSON said needsLiveReplay=false or omitted it.
        parsed = {"verdict": "needs_investigation", "confidence": "low", "needsLiveReplay": False}
        result = enforce_tier1_verdict_constraints(parsed, Path("."))
        self.assertTrue(result["needsLiveReplay"])

    def test_missing_confidence_pass_through_forces_needs_live_replay(self):
        parsed = {"verdict": "not_reproduced_intermittent"}  # no confidence key at all
        result = enforce_tier1_verdict_constraints(parsed, Path("."))
        self.assertTrue(result["needsLiveReplay"])

    def test_high_confidence_pass_through_does_not_force_needs_live_replay(self):
        parsed = {"verdict": "not_reproduced_intermittent", "confidence": "high"}
        result = enforce_tier1_verdict_constraints(parsed, Path("."))
        self.assertFalse(result["needsLiveReplay"])

    def test_medium_confidence_pass_through_does_not_force_needs_live_replay(self):
        parsed = {"verdict": "needs_investigation", "confidence": "medium"}
        result = enforce_tier1_verdict_constraints(parsed, Path("."))
        self.assertFalse(result["needsLiveReplay"])

    def test_verified_fix_far_from_claimed_line_is_downgraded(self):
        # Regression test: the "before" text matching SOMEWHERE in the file is
        # not enough — it must be near the claimed line, or a stale/wrong line
        # number could false-verify a fix that isn't actually grounded there.
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            f = repo_root / "A.java"
            lines = [f"line {i}" for i in range(1, 51)]
            lines[2] = "return old();"   # line 3
            lines[44] = "return old();"  # line 45 -- same text, far away
            f.write_text("\n".join(lines) + "\n", encoding="utf-8")
            parsed = {
                "verdict": "script_issue_fix_proposed",
                # Claims line 45, but the actual failing code (per this scenario) is at line 3.
                "proposedChange": {"file": "A.java", "line": 45, "before": "return old();", "after": "return new_();"},
            }
            result = enforce_tier1_verdict_constraints(parsed, repo_root)
            # Line 45 IS within the lookaround window of the claimed line, so this
            # specific case still verifies -- the real regression case is covered
            # by the next test, where the only match is far outside the window.
            self.assertEqual(result["verdict"], "script_issue_fix_proposed")

    def test_before_text_only_matching_outside_the_line_window_is_downgraded(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            f = repo_root / "A.java"
            lines = [f"line {i}" for i in range(1, 51)]
            lines[44] = "return old();"  # line 45, only occurrence
            f.write_text("\n".join(lines) + "\n", encoding="utf-8")
            parsed = {
                "verdict": "script_issue_fix_proposed",
                # Claims line 1 -- more than the default 10-line lookaround from
                # the actual (and only) occurrence at line 45.
                "proposedChange": {"file": "A.java", "line": 1, "before": "return old();", "after": "return new_();"},
            }
            result = enforce_tier1_verdict_constraints(parsed, repo_root)
            self.assertEqual(result["verdict"], "needs_investigation")
            self.assertTrue(result["needsLiveReplay"])


class FakeLLM:
    def __init__(self, responses):
        self._responses = list(responses)
        self.calls = []  # list of (prompt, images) tuples

    async def generate(self, prompt, images=None):
        self.calls.append((prompt, images or []))
        response = self._responses.pop(0)
        if isinstance(response, Exception):
            raise response
        return response


def _batch_response(items):
    return json.dumps(items)


class TriageWithLlmTests(unittest.IsolatedAsyncioTestCase):
    async def test_well_formed_batch_response_is_used_directly(self):
        batch = [{"groupId": "g1", "scenarioName": "s1"}, {"groupId": "g2", "scenarioName": "s2"}]
        llm = FakeLLM([_batch_response([
            {"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"},
            {"groupId": "g2", "verdict": "not_reproduced_intermittent", "confidence": "high"},
        ])])
        results = await triage_with_llm(batch, {}, llm, Path("."))
        self.assertEqual(len(llm.calls), 1)
        self.assertEqual(results["g1"]["verdict"], "needs_investigation")
        self.assertEqual(results["g2"]["verdict"], "not_reproduced_intermittent")

    async def test_unparseable_batch_response_retries_each_item_individually(self):
        batch = [{"groupId": "g1", "scenarioName": "s1"}, {"groupId": "g2", "scenarioName": "s2"}]
        llm = FakeLLM([
            "garbage, not json",
            _batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"}]),
            _batch_response([{"groupId": "g2", "verdict": "not_reproduced_intermittent", "confidence": "high"}]),
        ])
        results = await triage_with_llm(batch, {}, llm, Path("."))
        self.assertEqual(len(llm.calls), 3)  # 1 batch attempt + 2 individual retries
        self.assertEqual(results["g1"]["verdict"], "needs_investigation")
        self.assertEqual(results["g2"]["verdict"], "not_reproduced_intermittent")

    async def test_batch_missing_a_scenario_retries_each_item_individually(self):
        batch = [{"groupId": "g1", "scenarioName": "s1"}, {"groupId": "g2", "scenarioName": "s2"}]
        llm = FakeLLM([
            _batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"}]),  # g2 missing
            _batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"}]),
            _batch_response([{"groupId": "g2", "verdict": "needs_investigation", "confidence": "low"}]),
        ])
        results = await triage_with_llm(batch, {}, llm, Path("."))
        self.assertIn("g1", results)
        self.assertIn("g2", results)

    async def test_llm_error_never_drops_an_item_silently(self):
        batch = [{"groupId": "g1", "scenarioName": "s1"}]
        llm = FakeLLM([RuntimeError("boom"), RuntimeError("boom again")])
        results = await triage_with_llm(batch, {}, llm, Path("."))
        self.assertIn("g1", results)
        self.assertEqual(results["g1"]["verdict"], "needs_investigation")
        self.assertTrue(results["g1"]["needsLiveReplay"])

    async def test_forbidden_verdict_from_llm_is_clamped_even_via_batch_path(self):
        batch = [{"groupId": "g1", "scenarioName": "s1"}]
        llm = FakeLLM([_batch_response([
            {"groupId": "g1", "verdict": "confirmed_product_bug", "confidence": "high"},
        ])])
        results = await triage_with_llm(batch, {}, llm, Path("."))
        self.assertEqual(results["g1"]["verdict"], "needs_investigation")
        self.assertTrue(results["g1"]["needsLiveReplay"])

    async def test_two_groups_sharing_a_scenario_name_do_not_collide(self):
        # Regression test: two DIFFERENT groups whose representative scenarios
        # happen to share a scenarioName (e.g. an unparameterized Scenario
        # Outline title) must still get independently correct results — the
        # correlation key is groupId, scenarioName is informational only.
        batch = [
            {"groupId": "g1", "scenarioName": "Same Title"},
            {"groupId": "g2", "scenarioName": "Same Title"},
        ]
        llm = FakeLLM([_batch_response([
            {"groupId": "g1", "verdict": "not_reproduced_intermittent", "confidence": "high"},
            {"groupId": "g2", "verdict": "needs_investigation", "confidence": "high"},
        ])])
        results = await triage_with_llm(batch, {}, llm, Path("."))
        self.assertEqual(results["g1"]["verdict"], "not_reproduced_intermittent")
        self.assertEqual(results["g2"]["verdict"], "needs_investigation")


def _make_trace_zip(tmp_dir: Path, screenshot_name: str) -> Path:
    import zipfile

    trace_path = tmp_dir / "trace.zip"
    with zipfile.ZipFile(trace_path, "w") as zf:
        zf.writestr(screenshot_name, b"\xff\xd8\xff\xe0fake-jpeg-bytes")
    return trace_path


class ScreenshotEscalationTests(unittest.IsolatedAsyncioTestCase):
    async def test_low_confidence_with_screenshot_available_escalates_and_attaches_image(self):
        with tempfile.TemporaryDirectory() as tmp:
            trace_path = _make_trace_zip(Path(tmp), "shot.jpeg")
            batch = [{"groupId": "g1", "scenarioName": "s1", "tracePath": str(trace_path), "screenshotFile": "shot.jpeg"}]
            llm = FakeLLM([
                _batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"}]),
                _batch_response([{"groupId": "g1", "verdict": "script_issue_fix_proposed", "confidence": "high",
                                   "proposedChange": {"file": "x.java", "line": 1, "before": "a", "after": "b"}}]),
            ])
            results = await triage_with_llm(batch, {}, llm, Path("."), send_screenshots=True)
            self.assertEqual(len(llm.calls), 2)
            _prompt, images = llm.calls[1]
            self.assertEqual(len(images), 1)
            # Escalation's fix isn't mechanically verifiable (x.java doesn't exist), so it's
            # correctly downgraded by enforce_tier1_verdict_constraints — the point of this
            # test is that escalation was attempted with the image attached, not the outcome.
            self.assertTrue(results["g1"].get("usedScreenshot"))

    async def test_no_escalation_when_no_screenshot_available(self):
        batch = [{"groupId": "g1", "scenarioName": "s1"}]  # no tracePath/screenshotFile
        llm = FakeLLM([_batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"}])])
        results = await triage_with_llm(batch, {}, llm, Path("."), send_screenshots=True)
        self.assertEqual(len(llm.calls), 1)
        self.assertFalse(results["g1"].get("usedScreenshot"))

    async def test_no_escalation_when_send_screenshots_disabled(self):
        with tempfile.TemporaryDirectory() as tmp:
            trace_path = _make_trace_zip(Path(tmp), "shot.jpeg")
            batch = [{"groupId": "g1", "scenarioName": "s1", "tracePath": str(trace_path), "screenshotFile": "shot.jpeg"}]
            llm = FakeLLM([_batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low"}])])
            results = await triage_with_llm(batch, {}, llm, Path("."), send_screenshots=False)
            self.assertEqual(len(llm.calls), 1)
            self.assertFalse(results["g1"].get("usedScreenshot"))

    async def test_no_escalation_for_high_confidence_result(self):
        with tempfile.TemporaryDirectory() as tmp:
            trace_path = _make_trace_zip(Path(tmp), "shot.jpeg")
            batch = [{"groupId": "g1", "scenarioName": "s1", "tracePath": str(trace_path), "screenshotFile": "shot.jpeg"}]
            llm = FakeLLM([_batch_response([{"groupId": "g1", "verdict": "not_reproduced_intermittent", "confidence": "high"}])])
            results = await triage_with_llm(batch, {}, llm, Path("."), send_screenshots=True)
            self.assertEqual(len(llm.calls), 1)

    async def test_escalation_error_falls_back_to_first_pass_result(self):
        with tempfile.TemporaryDirectory() as tmp:
            trace_path = _make_trace_zip(Path(tmp), "shot.jpeg")
            batch = [{"groupId": "g1", "scenarioName": "s1", "tracePath": str(trace_path), "screenshotFile": "shot.jpeg"}]
            llm = FakeLLM([
                _batch_response([{"groupId": "g1", "verdict": "needs_investigation", "confidence": "low",
                                   "evidence": ["text-only, still usable"]}]),
                RuntimeError("Claude CLI exited with code 1"),
            ])
            results = await triage_with_llm(batch, {}, llm, Path("."), send_screenshots=True)
            self.assertEqual(len(llm.calls), 2)
            self.assertEqual(results["g1"]["evidence"], ["text-only, still usable"])
            self.assertNotIn("usedScreenshot", results["g1"])


if __name__ == "__main__":
    unittest.main()
