"""Tests for the two-call screenshot-escalation design:

  - Pass 1 is always text-only, regardless of `config.llm.send_screenshots`.
  - Pass 2 (screenshot attached) only happens when pass 1 explicitly signals
    `confidence == "low"` AND a screenshot is actually available (which itself
    requires `config.llm.send_screenshots` to be on) — never inferred from
    category/analysis presence alone.
  - A failed or unusable pass-2 result falls back to pass 1's result rather
    than losing an already-usable answer.
"""

from __future__ import annotations

import sys
import unittest
from dataclasses import dataclass
from pathlib import Path
from types import SimpleNamespace

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.main import analyze_failure_with_llm
from failure_analyzer.parsers.bug_report_parser import parse_single_failure_analysis


def make_response(category="Script Issue", confidence="High", analysis="a", fix="b"):
    return (
        f"**Category:** {category}\n\n"
        f"**Confidence:** {confidence}\n\n"
        f"**Analysis:** {analysis}\n\n"
        f"**Fix:** {fix}"
    )


class ParseConfidenceTests(unittest.TestCase):
    def test_parses_high_confidence(self):
        parsed = parse_single_failure_analysis(make_response(confidence="High"))
        self.assertEqual(parsed["confidence"], "high")

    def test_parses_low_confidence(self):
        parsed = parse_single_failure_analysis(make_response(confidence="Low"))
        self.assertEqual(parsed["confidence"], "low")

    def test_missing_confidence_field_is_empty_not_low(self):
        text = "**Category:** Script Issue\n\n**Analysis:** a\n\n**Fix:** b"
        parsed = parse_single_failure_analysis(text)
        self.assertEqual(parsed["confidence"], "")

    def test_empty_text_yields_empty_confidence(self):
        parsed = parse_single_failure_analysis("")
        self.assertEqual(parsed["confidence"], "")


@dataclass
class FakeFailure:
    scenario_name: str = "Some Scenario"
    trace_path: str = "/tmp/fake-trace.zip"
    screenshot_name: str = "test-failed-1.jpeg"


class FakeLLM:
    """Stub connector: returns queued responses in order, one per .generate() call."""

    def __init__(self, responses):
        self._responses = list(responses)
        self.calls = []

    async def generate(self, prompt, images=None):
        self.calls.append({"prompt": prompt, "images": images or []})
        response = self._responses.pop(0)
        if isinstance(response, Exception):
            raise response
        return response


def make_config(send_screenshots: bool):
    return SimpleNamespace(llm=SimpleNamespace(send_screenshots=send_screenshots))


class AnalyzeFailureWithLlmTests(unittest.IsolatedAsyncioTestCase):
    async def test_high_confidence_makes_only_one_call(self):
        llm = FakeLLM([make_response(confidence="High")])
        config = make_config(send_screenshots=True)

        name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)

        self.assertEqual(len(llm.calls), 1)
        self.assertEqual(llm.calls[0]["images"], [])
        self.assertEqual(name, "Some Scenario")
        self.assertEqual(parsed["confidence"], "high")

    async def test_low_confidence_with_screenshot_available_escalates(self):
        llm = FakeLLM(
            [
                make_response(confidence="Low", analysis="uncertain text-only take"),
                make_response(confidence="High", analysis="confirmed from screenshot"),
            ]
        )
        config = make_config(send_screenshots=True)
        # collect_screenshot_for_failure will fail to extract a real screenshot from a
        # fake trace path, so patch it to simulate one being available.
        import failure_analyzer.main as main_mod
        original = main_mod.collect_screenshot_for_failure
        main_mod.collect_screenshot_for_failure = lambda failure, cfg: ["fake-base64-jpeg"]
        try:
            name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)
        finally:
            main_mod.collect_screenshot_for_failure = original

        self.assertEqual(len(llm.calls), 2)
        self.assertEqual(llm.calls[0]["images"], [])
        self.assertEqual(llm.calls[1]["images"], ["fake-base64-jpeg"])
        self.assertEqual(parsed["analysis"], "confirmed from screenshot")

    async def test_low_confidence_without_available_screenshot_makes_only_one_call(self):
        llm = FakeLLM([make_response(confidence="Low", analysis="uncertain")])
        config = make_config(send_screenshots=True)
        import failure_analyzer.main as main_mod
        original = main_mod.collect_screenshot_for_failure
        main_mod.collect_screenshot_for_failure = lambda failure, cfg: []  # nothing to attach
        try:
            name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)
        finally:
            main_mod.collect_screenshot_for_failure = original

        self.assertEqual(len(llm.calls), 1)
        self.assertEqual(parsed["analysis"], "uncertain")

    async def test_low_confidence_with_send_screenshots_disabled_never_escalates(self):
        # Uses the REAL collect_screenshot_for_failure (not a stub) to prove the
        # config flag is honored end-to-end, not just assumed.
        llm = FakeLLM([make_response(confidence="Low", analysis="uncertain")])
        config = make_config(send_screenshots=False)

        name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)

        self.assertEqual(len(llm.calls), 1)
        self.assertEqual(parsed["analysis"], "uncertain")

    async def test_escalation_call_error_falls_back_to_pass_one_result(self):
        llm = FakeLLM(
            [
                make_response(confidence="Low", analysis="text-only take, still usable"),
                RuntimeError("Claude CLI exited with code 1"),
            ]
        )
        config = make_config(send_screenshots=True)
        import failure_analyzer.main as main_mod
        original = main_mod.collect_screenshot_for_failure
        main_mod.collect_screenshot_for_failure = lambda failure, cfg: ["fake-base64-jpeg"]
        try:
            name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)
        finally:
            main_mod.collect_screenshot_for_failure = original

        self.assertEqual(len(llm.calls), 2)
        self.assertEqual(parsed["analysis"], "text-only take, still usable")

    async def test_escalation_call_unusable_output_falls_back_to_pass_one_result(self):
        llm = FakeLLM(
            [
                make_response(confidence="Low", analysis="text-only take, still usable"),
                "garbage output with no recognizable fields",
            ]
        )
        config = make_config(send_screenshots=True)
        import failure_analyzer.main as main_mod
        original = main_mod.collect_screenshot_for_failure
        main_mod.collect_screenshot_for_failure = lambda failure, cfg: ["fake-base64-jpeg"]
        try:
            name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)
        finally:
            main_mod.collect_screenshot_for_failure = original

        self.assertEqual(len(llm.calls), 2)
        self.assertEqual(parsed["analysis"], "text-only take, still usable")

    async def test_pass_one_error_returns_none(self):
        llm = FakeLLM([RuntimeError("Claude CLI timed out")])
        config = make_config(send_screenshots=True)

        name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)

        self.assertEqual(len(llm.calls), 1)
        self.assertIsNone(parsed)
        self.assertEqual(name, "Some Scenario")

    async def test_pass_one_unusable_output_returns_none_without_escalating(self):
        llm = FakeLLM(["garbage output with no recognizable fields"])
        config = make_config(send_screenshots=True)

        name, parsed = await analyze_failure_with_llm(FakeFailure(), "prompt", llm, config)

        self.assertEqual(len(llm.calls), 1)  # unusable + no confidence signal => no escalation
        self.assertIsNone(parsed)


if __name__ == "__main__":
    unittest.main()
