from __future__ import annotations

import sys
import unittest
from pathlib import Path
from types import SimpleNamespace

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.grouping.failure_grouper import build_group_key, group_failures


def failure(**kwargs):
    defaults = {
        "error_message": "TimeoutError: waiting for locator",
        "failed_step": "When user clicks menu option",
        "feature_file": "file:features/Foo.feature",
        "playwright_call_log": None,
        "last_browser_action": None,
        "page_url": None,
        "expected_value": None,
        "actual_value": None,
        "network_errors": [],
        "error_location": None,
    }
    defaults.update(kwargs)
    return SimpleNamespace(**defaults)


class FailureGrouperSignatureTests(unittest.TestCase):
    def test_minimal_failure_keeps_legacy_key_shape(self):
        key, exception, step = build_group_key(failure())

        self.assertEqual(exception, "TimeoutError")
        self.assertEqual(step, "when user clicks menu option")
        self.assertEqual(key, "TimeoutError|when user clicks menu option")

    def test_same_step_different_locator_splits_groups(self):
        failures = [
            failure(playwright_call_log='Call log:\n- waiting for locator("//button[@id=\'save\']")'),
            failure(playwright_call_log='Call log:\n- waiting for locator("//button[@id=\'cancel\']")'),
        ]

        groups = group_failures(failures)

        self.assertEqual(len(groups), 2)

    def test_same_step_same_locator_collapses_group(self):
        failures = [
            failure(playwright_call_log='Call log:\n- waiting for locator("//button[@id=\'save\']")'),
            failure(playwright_call_log='Call log:\n- waiting for locator("//button[@id=\'save\']")'),
        ]

        groups = group_failures(failures)

        self.assertEqual(len(groups), 1)
        self.assertEqual(len(groups[0].failures), 2)

    def test_different_api_signature_splits_groups(self):
        failures = [
            failure(network_errors=["500 GET /api/campaigns/123"]),
            failure(network_errors=["404 GET /api/advertisers/456"]),
        ]

        groups = group_failures(failures)

        self.assertEqual(len(groups), 2)

    def test_crash_signature_is_part_of_group_key(self):
        key, _exception, _step = build_group_key(
            failure(error_message="TargetClosedError: Target page, context or browser has been closed")
        )

        self.assertIn("crash=target-closed", key)


if __name__ == "__main__":
    unittest.main()
