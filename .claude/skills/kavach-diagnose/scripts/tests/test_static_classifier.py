from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.triage.static_classifier import (
    build_copy_diff_fix,
    classify,
    disposition,
    extract_locator_text,
    is_probable_copy_diff,
)


class IsProbableCopyDiffTests(unittest.TestCase):
    def test_identical_strings_are_not_a_diff(self):
        self.assertFalse(is_probable_copy_diff("Foo", "Foo"))

    def test_case_only_difference_is_a_copy_diff(self):
        self.assertTrue(is_probable_copy_diff("Foo Bar", "foo bar"))

    def test_whitespace_only_difference_is_a_copy_diff(self):
        self.assertTrue(is_probable_copy_diff("Foo   Bar", "Foo Bar"))

    def test_substantively_different_strings_are_not_a_copy_diff(self):
        self.assertFalse(is_probable_copy_diff("Advertiser Name", "Campaign Name"))

    def test_missing_values_are_not_a_copy_diff(self):
        self.assertFalse(is_probable_copy_diff(None, "x"))
        self.assertFalse(is_probable_copy_diff("x", None))


class BuildCopyDiffFixTests(unittest.TestCase):
    def test_locates_exact_line_and_builds_verbatim_diff(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            feature_dir = repo_root / "src" / "test" / "resources" / "features"
            feature_dir.mkdir(parents=True)
            feature_file = feature_dir / "Life_Campaign.feature"
            feature_file.write_text(
                "Feature: Campaign\n"
                '  Scenario: x\n'
                '    Then user sees "Advertiser" label\n',
                encoding="utf-8",
            )
            failure = {
                "expectedValue": "Advertiser",
                "actualValue": "advertiser",
                "featureFile": "file:src/test/resources/features/Life_Campaign.feature",
                "errorLocation": {
                    "feature": {"file": "src/test/resources/features/Life_Campaign.feature", "line": 3}
                },
            }
            fix = build_copy_diff_fix(failure, repo_root)
            self.assertIsNotNone(fix)
            self.assertEqual(fix["line"], 3)
            self.assertIn("Advertiser", fix["before"])
            self.assertIn("advertiser", fix["after"])

    def test_returns_none_when_expected_value_not_found_in_file(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            feature_dir = repo_root / "src"
            feature_dir.mkdir(parents=True)
            feature_file = feature_dir / "x.feature"
            feature_file.write_text("Feature: x\n", encoding="utf-8")
            failure = {
                "expectedValue": "Nowhere",
                "actualValue": "nowhere",
                "errorLocation": {"feature": {"file": "src/x.feature", "line": 1}},
            }
            self.assertIsNone(build_copy_diff_fix(failure, repo_root))

    def test_returns_none_without_expected_or_actual(self):
        self.assertIsNone(build_copy_diff_fix({}, Path(".")))

    def test_returns_none_without_a_recorded_line(self):
        # No errorLocation.feature.line at all -- never guess a location by
        # scanning the whole file; escalate instead.
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            (repo_root / "src").mkdir()
            feature_file = repo_root / "src" / "x.feature"
            feature_file.write_text('Then user sees "Advertiser"\n', encoding="utf-8")
            failure = {
                "expectedValue": "Advertiser",
                "actualValue": "advertiser",
                "errorLocation": {"feature": {"file": "src/x.feature"}},  # no "line"
            }
            self.assertIsNone(build_copy_diff_fix(failure, repo_root))

    def test_ignores_a_match_in_an_earlier_unrelated_scenario(self):
        # Regression test: the same expected text appears in an EARLIER
        # scenario (line 2, well outside the search radius) and in the
        # actually-failing one (line 20, the recorded line). Only the
        # near-line match must be used -- picking line 2 would patch the
        # wrong scenario.
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            (repo_root / "src").mkdir()
            feature_file = repo_root / "src" / "x.feature"
            lines = ["Feature: x"]
            lines.append('    Then user sees "Advertiser" label')  # line 2, unrelated
            lines.extend(f"    # filler {i}" for i in range(3, 20))  # lines 3-19
            lines.append('    Then user sees "Advertiser" label')  # line 20, the real match
            feature_file.write_text("\n".join(lines) + "\n", encoding="utf-8")
            failure = {
                "expectedValue": "Advertiser",
                "actualValue": "advertiser",
                "errorLocation": {"feature": {"file": "src/x.feature", "line": 20}},
            }
            fix = build_copy_diff_fix(failure, repo_root)
            self.assertIsNotNone(fix)
            self.assertEqual(fix["line"], 20)

    def test_returns_none_when_only_match_is_far_from_recorded_line(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            (repo_root / "src").mkdir()
            feature_file = repo_root / "src" / "x.feature"
            lines = [f"# line {i}" for i in range(1, 21)]
            lines[1] = '    Then user sees "Advertiser" label'  # line 2, only occurrence
            feature_file.write_text("\n".join(lines) + "\n", encoding="utf-8")
            failure = {
                "expectedValue": "Advertiser",
                "actualValue": "advertiser",
                # Recorded line 18 -- more than the 5-line search radius from line 2.
                "errorLocation": {"feature": {"file": "src/x.feature", "line": 18}},
            }
            self.assertIsNone(build_copy_diff_fix(failure, repo_root))


class DispositionTests(unittest.TestCase):
    def test_same_run_intermittent_is_finalized_without_live_replay(self):
        failure = {"backgroundReliability": {"passed": 8, "total": 9, "rate": 0.89}}
        result = disposition("same-run-intermittent", failure, Path("."))
        self.assertTrue(result["resolved"])
        self.assertEqual(result["verdict"], "not_reproduced_intermittent")
        self.assertFalse(result["needsLiveReplay"])

    def test_network_aborted_escalates_to_live_replay(self):
        result = disposition("network-aborted", {"scenarioName": "s"}, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])

    def test_environment_or_runner_failure_escalates_to_live_replay(self):
        result = disposition("environment-or-runner-failure", {"scenarioName": "s"}, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])

    def test_ui_value_present_timing_always_escalates(self):
        result = disposition("ui-value-present-timing", {"scenarioName": "s"}, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])

    def test_console_error_always_escalates(self):
        result = disposition("console-error", {"scenarioName": "s"}, Path("."))
        self.assertTrue(result["needsLiveReplay"])

    def test_api_error_always_escalates(self):
        result = disposition("api-error", {"scenarioName": "s"}, Path("."))
        self.assertTrue(result["needsLiveReplay"])

    def test_ui_value_missing_non_trivial_diff_escalates(self):
        failure = {"expectedValue": "Advertiser Name", "actualValue": "Campaign Name"}
        result = disposition("ui-value-missing", failure, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])

    def test_ui_value_missing_trivial_diff_with_verifiable_fix_resolves(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo_root = Path(tmp)
            (repo_root / "src").mkdir()
            feature_file = repo_root / "src" / "x.feature"
            feature_file.write_text('Then user sees "Advertiser"\n', encoding="utf-8")
            failure = {
                "expectedValue": "Advertiser",
                "actualValue": "advertiser",
                "errorLocation": {"feature": {"file": "src/x.feature", "line": 1}},
            }
            result = disposition("ui-value-missing", failure, repo_root)
            self.assertTrue(result["resolved"])
            self.assertEqual(result["verdict"], "script_issue_fix_proposed")
            self.assertIsNotNone(result["proposedChange"])
            self.assertFalse(result["needsLiveReplay"])

    def test_ui_value_missing_trivial_diff_without_locatable_fix_escalates(self):
        # Copy-diff heuristic says "trivial" but the literal text isn't found in
        # the feature file (e.g. stale line info) — must not fabricate a diff.
        failure = {
            "expectedValue": "Advertiser",
            "actualValue": "advertiser",
            "errorLocation": {"feature": {"file": "src/does-not-exist.feature", "line": 1}},
        }
        result = disposition("ui-value-missing", failure, Path("/tmp"))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])


class ExtractLocatorTextTests(unittest.TestCase):
    def test_extracts_from_locator_call(self):
        log = "waiting for locator('text=Submit')\n  at ..."
        self.assertEqual(extract_locator_text(log), "Submit")

    def test_extracts_from_get_by_text(self):
        log = "waiting for getByText('Campaign Name')"
        self.assertEqual(extract_locator_text(log), "Campaign Name")

    def test_extracts_from_get_by_role_name(self):
        log = "waiting for getByRole('button', {name: 'Save'})"
        self.assertEqual(extract_locator_text(log), "Save")

    def test_extracts_from_get_by_label(self):
        log = "waiting for getByLabel('Email')"
        self.assertEqual(extract_locator_text(log), "Email")

    def test_returns_none_for_non_text_locator(self):
        log = "waiting for locator('#some-id')"
        self.assertIsNone(extract_locator_text(log))

    def test_returns_none_for_empty_log(self):
        self.assertIsNone(extract_locator_text(None))
        self.assertIsNone(extract_locator_text(""))

    def test_uses_last_few_lines_not_first(self):
        log = "waiting for locator('text=Old')\n...\nwaiting for locator('text=Current')"
        self.assertEqual(extract_locator_text(log), "Current")


class DispositionStrictModeTests(unittest.TestCase):
    def test_strict_mode_violation_escalates_to_live_replay(self):
        # A locator matching >1 element cannot be finalized without a
        # proposedChange (enforce_tier1_verdict_constraints requires one for
        # script_issue_fix_proposed, and this branch has none to offer), and
        # a product bug rendering duplicate elements can't be ruled out from
        # static evidence alone — matching offline_dom_analyzer.py's
        # ambiguous_selector handling of the identical underlying symptom.
        failure = {
            "scenarioName": "s",
            "errorMessage": "Error: strict mode violation: locator('button') resolved to 3 elements",
        }
        result = disposition("any-cause", failure, Path("."))
        self.assertFalse(result["resolved"])
        self.assertIsNone(result["verdict"])
        self.assertTrue(result["needsLiveReplay"])
        self.assertIsNone(result["proposedChange"])

    def test_strict_mode_check_is_case_insensitive(self):
        failure = {"errorMessage": "Strict Mode Violation: ..."}
        result = disposition("ui-value-present-timing", failure, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])


class DispositionUiValuePresentTimingTests(unittest.TestCase):
    """`ui-value-present-timing` is the real cause analyze_failure() produces
    when the expected value IS present in the captured page snapshot but the
    step still failed (a timing/visibility race). Regression coverage for a
    prior bug where this branch checked for a `cause` value ("timeout") that
    analyze_failure() never actually emits, making the branch permanently
    dead code — see static_classifier.py's disposition() for the reconciled
    version."""

    def test_extracts_locator_text_into_the_evidence_message(self):
        failure = {
            "scenarioName": "s",
            "errorMessage": "TimeoutError: waiting for locator",
            "playwrightCallLog": "waiting for locator('text=Save Campaign')",
            "pageText": "Here is the Save Campaign button on the page.",
        }
        result = disposition("ui-value-present-timing", failure, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])
        self.assertTrue(any("Save Campaign" in e for e in result["evidence"]))

    def test_non_extractable_locator_still_escalates_with_generic_evidence(self):
        failure = {
            "scenarioName": "s",
            "errorMessage": "TimeoutError",
            "playwrightCallLog": "waiting for locator('#some-css-id')",
            "pageText": "irrelevant",
        }
        result = disposition("ui-value-present-timing", failure, Path("."))
        self.assertFalse(result["resolved"])
        self.assertTrue(result["needsLiveReplay"])
        self.assertTrue(result["evidence"])

    def test_no_call_log_still_escalates(self):
        failure = {"scenarioName": "s", "errorMessage": "TimeoutError", "playwrightCallLog": None, "pageText": "x"}
        result = disposition("ui-value-present-timing", failure, Path("."))
        self.assertTrue(result["needsLiveReplay"])


class ClassifyDelegatesToAnalyzeFailureTests(unittest.TestCase):
    def test_classify_wraps_analyze_failure_unmodified(self):
        failure = {
            "scenarioName": "s",
            "expectedValue": None,
            "actualValue": None,
            "errorMessage": "TimeoutError: x",
            "pageText": None,
            "isBackgroundFailure": False,
            "backgroundReliability": None,
            "networkErrors": [],
            "consoleErrors": [],
        }
        result = classify(failure)
        self.assertEqual(result["cause"], "unknown")
        self.assertIn("category", result)

    def test_target_closed_error_is_environment_or_runner_failure(self):
        failure = {
            "scenarioName": "s",
            "errorMessage": "TargetClosedError: Target page, context or browser has been closed",
            "playwrightCallLog": "",
            "lastBrowserAction": "page.navigate",
            "pageUrl": "about:blank",
            "pageText": "",
            "networkErrors": [],
            "consoleErrors": [],
        }

        result = classify(failure)

        self.assertEqual(result["cause"], "environment-or-runner-failure")
        self.assertEqual(result["category"], "Environment Issue")

    def test_bulk_status_minus_one_network_errors_no_longer_force_environment_failure(self):
        failure = {
            "scenarioName": "s",
            "errorMessage": "TimeoutError: waiting for page",
            "pageText": "some page text",
            "networkErrors": [
                "-1 GET /BuyerProxy.ashx?u=/api/a",
                "-1 POST /BuyerProxy.ashx?u=/api/b",
                "-1 GET /BuyerProxy.ashx?u=/api/c",
            ],
            "consoleErrors": [],
        }

        result = classify(failure)

        self.assertEqual(result["cause"], "network-aborted")

    def test_single_status_minus_one_remains_network_aborted(self):
        failure = {
            "scenarioName": "s",
            "errorMessage": "TimeoutError: waiting for page",
            "pageText": "some page text",
            "networkErrors": ["-1 GET /BuyerProxy.ashx?u=/api/a"],
            "consoleErrors": [],
        }

        result = classify(failure)

        self.assertEqual(result["cause"], "network-aborted")


if __name__ == "__main__":
    unittest.main()
