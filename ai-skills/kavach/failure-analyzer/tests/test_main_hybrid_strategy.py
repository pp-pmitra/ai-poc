"""Tests for the assertion-analysis strategy selection in main.py.

Covers the `hybrid` strategy inversion fix: deterministic analysis must be
used immediately whenever it is confident (``cause != "unknown"``), and the
LLM must only be called for genuinely ambiguous failures. `deterministic`
and `llm` strategies must keep their existing all-or-nothing behavior.
"""

from __future__ import annotations

import asyncio
import sys
import unittest
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.main import partition_for_strategy, merge_llm_results


@dataclass
class FakeFailure:
    scenario_name: str


def det_result(cause: str) -> dict:
    """A deterministic analysis result shaped like analyze_failure()'s output.

    `analyze_failure()` always supplies a `category`, even for `cause ==
    "unknown"` — so tests must never use `category` presence as a stand-in
    for confidence.
    """
    return {"category": "Script Issue", "cause": cause, "analysis": "x", "fix": "y"}


class PartitionForStrategyTests(unittest.TestCase):
    def setUp(self):
        self.failures = [
            FakeFailure("resolved-network"),
            FakeFailure("resolved-ui-missing"),
            FakeFailure("ambiguous-1"),
            FakeFailure("ambiguous-2"),
        ]
        self.deterministic = {
            "resolved-network": det_result("network-aborted"),
            "resolved-ui-missing": det_result("ui-value-missing"),
            "ambiguous-1": det_result("unknown"),
            "ambiguous-2": det_result("unknown"),
        }

    def test_deterministic_strategy_uses_only_deterministic_never_llm(self):
        analyses, llm_targets = partition_for_strategy(
            "deterministic", self.failures, self.deterministic
        )
        self.assertEqual(llm_targets, [])
        self.assertEqual(analyses, self.deterministic)
        # Must be a copy, not the same object, so callers can't mutate the
        # deterministic map out from under other strategies.
        self.assertIsNot(analyses, self.deterministic)

    def test_llm_strategy_targets_every_failure_regardless_of_cause(self):
        analyses, llm_targets = partition_for_strategy(
            "llm", self.failures, self.deterministic
        )
        self.assertEqual(analyses, {})
        self.assertEqual(llm_targets, self.failures)

    def test_hybrid_only_targets_unknown_cause_failures(self):
        analyses, llm_targets = partition_for_strategy(
            "hybrid", self.failures, self.deterministic
        )
        self.assertEqual(
            {f.scenario_name for f in llm_targets},
            {"ambiguous-1", "ambiguous-2"},
        )
        # Resolved failures are already present with their deterministic result —
        # zero LLM calls will ever be made for them.
        self.assertEqual(analyses["resolved-network"], self.deterministic["resolved-network"])
        self.assertEqual(analyses["resolved-ui-missing"], self.deterministic["resolved-ui-missing"])

    def test_hybrid_targets_failures_missing_from_deterministic_map(self):
        failures = [FakeFailure("no-det-entry")]
        analyses, llm_targets = partition_for_strategy("hybrid", failures, {})
        self.assertEqual(llm_targets, failures)
        self.assertEqual(analyses, {})

    def test_category_presence_is_not_used_as_a_confidence_signal(self):
        # Every deterministic result (including "unknown") carries a category;
        # only "unknown" cause should route to the LLM.
        for det in self.deterministic.values():
            self.assertIn("category", det)
        _, llm_targets = partition_for_strategy(
            "hybrid", self.failures, self.deterministic
        )
        self.assertEqual(len(llm_targets), 2)


class MergeLlmResultsTests(unittest.TestCase):
    def test_successful_result_overrides_deterministic_fallback(self):
        analyses = {"s1": det_result("unknown")}
        ai_count = merge_llm_results(analyses, [("s1", {"category": "Potential Functional Issue"})])
        self.assertEqual(ai_count, 1)
        self.assertEqual(analyses["s1"], {"category": "Potential Functional Issue"})

    def test_unusable_result_retains_deterministic_fallback(self):
        fallback = det_result("unknown")
        analyses = {"s1": fallback}
        ai_count = merge_llm_results(analyses, [("s1", None)])
        self.assertEqual(ai_count, 0)
        self.assertEqual(analyses["s1"], fallback)

    def test_unusable_result_with_no_prior_entry_leaves_scenario_absent(self):
        analyses: dict = {}
        ai_count = merge_llm_results(analyses, [("s1", None)])
        self.assertEqual(ai_count, 0)
        self.assertNotIn("s1", analyses)


class HybridEndToEndCallCountTests(unittest.TestCase):
    """Drives partition_for_strategy + merge_llm_results through the same
    gather-one-call-per-target pattern main.py uses, with a stub LLM, to prove
    actual call counts end-to-end rather than just the partitioning logic.
    """

    def _run(self, strategy: str, failures, deterministic, generate):
        calls = {"count": 0}

        async def _analyze_one(failure):
            calls["count"] += 1
            try:
                parsed = await generate(failure.scenario_name)
                return failure.scenario_name, parsed
            except Exception:
                return failure.scenario_name, None

        async def _go():
            analyses, llm_targets = partition_for_strategy(strategy, failures, deterministic)
            if llm_targets:
                results = await asyncio.gather(*(_analyze_one(f) for f in llm_targets))
                merge_llm_results(analyses, results)
            return analyses

        return asyncio.run(_go()), calls["count"]

    def test_zero_llm_calls_when_all_failures_resolved_deterministically(self):
        failures = [FakeFailure("a"), FakeFailure("b")]
        deterministic = {
            "a": det_result("network-aborted"),
            "b": det_result("ui-value-present-timing"),
        }

        async def generate(_name):
            raise AssertionError("LLM must not be called for resolved failures")

        analyses, call_count = self._run("hybrid", failures, deterministic, generate)
        self.assertEqual(call_count, 0)
        self.assertEqual(analyses["a"], deterministic["a"])
        self.assertEqual(analyses["b"], deterministic["b"])

    def test_llm_called_only_for_unknown_cause_failures(self):
        failures = [FakeFailure("resolved"), FakeFailure("unknown-1"), FakeFailure("unknown-2")]
        deterministic = {
            "resolved": det_result("api-error"),
            "unknown-1": det_result("unknown"),
            "unknown-2": det_result("unknown"),
        }
        seen = []

        async def generate(name):
            seen.append(name)
            return {"category": "Potential Functional Issue", "cause": "ai"}

        analyses, call_count = self._run("hybrid", failures, deterministic, generate)
        self.assertEqual(call_count, 2)
        self.assertEqual(set(seen), {"unknown-1", "unknown-2"})
        self.assertEqual(analyses["resolved"], deterministic["resolved"])
        self.assertEqual(analyses["unknown-1"]["cause"], "ai")
        self.assertEqual(analyses["unknown-2"]["cause"], "ai")

    def test_llm_error_on_ambiguous_failure_falls_back_to_deterministic(self):
        failures = [FakeFailure("unknown-1")]
        deterministic = {"unknown-1": det_result("unknown")}

        async def generate(_name):
            raise RuntimeError("Claude CLI exited with code 1")

        analyses, call_count = self._run("hybrid", failures, deterministic, generate)
        self.assertEqual(call_count, 1)
        self.assertEqual(analyses["unknown-1"], deterministic["unknown-1"])

    def test_llm_strategy_calls_every_failure_even_when_deterministic_is_confident(self):
        failures = [FakeFailure("a"), FakeFailure("b")]
        deterministic = {
            "a": det_result("network-aborted"),
            "b": det_result("ui-value-present-timing"),
        }
        seen = []

        async def generate(name):
            seen.append(name)
            return {"category": "Potential Functional Issue", "cause": "ai"}

        analyses, call_count = self._run("llm", failures, deterministic, generate)
        self.assertEqual(call_count, 2)
        self.assertEqual(set(seen), {"a", "b"})

    def test_deterministic_strategy_never_calls_llm(self):
        failures = [FakeFailure("a")]
        deterministic = {"a": det_result("unknown")}

        async def generate(_name):
            raise AssertionError("LLM must not be called under deterministic strategy")

        analyses, call_count = self._run("deterministic", failures, deterministic, generate)
        self.assertEqual(call_count, 0)
        self.assertEqual(analyses["a"], deterministic["a"])


if __name__ == "__main__":
    unittest.main()
