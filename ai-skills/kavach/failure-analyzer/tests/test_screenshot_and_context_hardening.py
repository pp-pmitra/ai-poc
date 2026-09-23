"""Tests for:
  1. sendScreenshots defaulting to on (config.json + config.py fallback) — safe
     to default on because a screenshot is only ever sent on a second,
     escalation-only pass (see test_two_call_escalation.py), never the first.
  2. ClaudeCodeConnector piping the prompt via stdin and referencing screenshots
     by temp-file path instead of inlining base64 into the argv, so a realistic
     screenshot can never trigger `OSError: Argument list too long`.
  3. History block capped to a bounded size.
  4. Context routing falling through to a working file instead of returning a
     "(context file not found: ...)" placeholder.
"""

from __future__ import annotations

import asyncio
import base64
import json
import sys
import unittest
from pathlib import Path
from unittest.mock import AsyncMock, patch

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.config import LLMConfig, load_config
from failure_analyzer.llm.claude_code_connector import ClaudeCodeConnector
from failure_analyzer.reporters import history_writer
from failure_analyzer.prompts.prompt_builder import (
    load_context_for_features,
    CONTEXT_ROUTE_RULES,
    CONTEXT_DIR,
    FALLBACK_CONTEXT_FILE,
)


class ScreenshotEscalationDefaultTests(unittest.TestCase):
    """sendScreenshots defaults to True — safe now that it only gates a
    *second*, escalation-only LLM pass (see analyze_failure_with_llm), not a
    screenshot on every call the way it used to.
    """

    def test_shipped_config_json_enables_screenshots(self):
        config_path = Path(__file__).resolve().parent.parent / "config.json"
        raw = json.loads(config_path.read_text())
        self.assertTrue(raw["llm"]["sendScreenshots"])

    def test_dataclass_default_is_on(self):
        self.assertTrue(LLMConfig().send_screenshots)

    def test_load_config_defaults_to_on_when_key_absent(self, tmp_name="tmp_config_no_screenshots.json"):
        tmp_path = Path(__file__).resolve().parent / tmp_name
        tmp_path.write_text(json.dumps({"llm": {"provider": "claudeCode"}}))
        try:
            config = load_config(tmp_path)
            self.assertTrue(config.llm.send_screenshots)
        finally:
            try:
                tmp_path.unlink(missing_ok=True)
            except OSError:
                pass


def _make_fake_process(stdout=b"analysis text", stderr=b"", returncode=0, communicate_side_effect=None):
    process = AsyncMock()
    process.returncode = returncode
    if communicate_side_effect is not None:
        process.communicate = AsyncMock(side_effect=communicate_side_effect)
    else:
        process.communicate = AsyncMock(return_value=(stdout, stderr))
    process.kill = lambda: None
    return process


class ClaudeCodeConnectorStdinTests(unittest.IsolatedAsyncioTestCase):
    async def test_prompt_is_piped_via_stdin_not_argv(self):
        connector = ClaudeCodeConnector(model="sonnet", timeout_ms=5000)
        prompt = "x" * 500_000  # far bigger than any single argv element could safely hold

        fake_process = _make_fake_process()
        with patch(
            "failure_analyzer.llm.claude_code_connector.asyncio.create_subprocess_exec",
            new=AsyncMock(return_value=fake_process),
        ) as create_exec:
            result = await connector.generate(prompt, images=None)

        self.assertEqual(result, "analysis text")
        args, kwargs = create_exec.call_args
        # The prompt text must never appear as one of the argv elements.
        self.assertNotIn(prompt, args)
        self.assertEqual(kwargs.get("stdin"), asyncio.subprocess.PIPE)
        # It must have been sent through communicate(input=...) instead.
        sent_input = fake_process.communicate.call_args.kwargs.get("input")
        self.assertEqual(sent_input, prompt.encode("utf-8"))

    async def test_screenshot_written_to_temp_dir_referenced_by_path_granted_via_add_dir_and_cleaned_up(self):
        connector = ClaudeCodeConnector(model=None, timeout_ms=5000)
        jpeg_bytes = b"\xff\xd8\xff" + b"fake-jpeg-body" * 1000
        img_b64 = base64.b64encode(jpeg_bytes).decode()

        captured_paths = []
        fake_process = _make_fake_process()

        with patch(
            "failure_analyzer.llm.claude_code_connector.asyncio.create_subprocess_exec",
            new=AsyncMock(return_value=fake_process),
        ) as create_exec:
            await connector.generate("analyze this failure", images=[img_b64])

        sent_input = fake_process.communicate.call_args.kwargs.get("input").decode("utf-8")
        # The raw base64 blob must never be inlined into the prompt text.
        self.assertNotIn(img_b64, sent_input)
        # A path to a real, existing-at-call-time temp jpeg must be referenced instead.
        for line in sent_input.splitlines():
            line = line.strip()
            if line.startswith("- ") and line.endswith(".jpeg"):
                captured_paths.append(line[2:])
        self.assertEqual(len(captured_paths), 1)
        temp_dir = str(Path(captured_paths[0]).parent)

        # Headless -p mode only allows Read inside the CLI's working directory or
        # directories granted via --add-dir — a system temp path needs this flag,
        # confirmed against a real run where the read was otherwise denied.
        args, kwargs = create_exec.call_args
        self.assertIn("--add-dir", args)
        self.assertEqual(args[args.index("--add-dir") + 1], temp_dir)

        # Both the file and its private temp directory must be cleaned up.
        self.assertFalse(Path(captured_paths[0]).exists())
        self.assertFalse(Path(temp_dir).exists())

    async def test_timeout_still_cleans_up_temp_screenshot_dir(self):
        connector = ClaudeCodeConnector(model=None, timeout_ms=50)
        img_b64 = base64.b64encode(b"\xff\xd8\xff" + b"x" * 100).decode()

        written_dirs = []
        import tempfile as _tempfile

        real_mkdtemp = _tempfile.mkdtemp

        def spy_mkdtemp(*args, **kwargs):
            path = real_mkdtemp(*args, **kwargs)
            written_dirs.append(path)
            return path

        call_count = {"n": 0}

        async def hang_once_then_return(*args, **kwargs):
            # First call is the timed one wait_for cancels; the drain call made
            # after process.kill() must return promptly, as a real killed
            # process's communicate() would.
            call_count["n"] += 1
            if call_count["n"] == 1:
                await asyncio.sleep(10)
            return (b"", b"")

        fake_process = _make_fake_process(communicate_side_effect=hang_once_then_return)

        with patch(
            "failure_analyzer.llm.claude_code_connector.asyncio.create_subprocess_exec",
            new=AsyncMock(return_value=fake_process),
        ), patch(
            "failure_analyzer.llm.claude_code_connector.tempfile.mkdtemp", side_effect=spy_mkdtemp
        ):
            with self.assertRaises(TimeoutError):
                await connector.generate("analyze this failure", images=[img_b64])

        self.assertEqual(len(written_dirs), 1)
        self.assertFalse(Path(written_dirs[0]).exists())

    async def test_no_images_means_no_temp_files_no_add_dir_and_unchanged_prompt(self):
        connector = ClaudeCodeConnector(model=None, timeout_ms=5000)
        fake_process = _make_fake_process()

        with patch(
            "failure_analyzer.llm.claude_code_connector.asyncio.create_subprocess_exec",
            new=AsyncMock(return_value=fake_process),
        ) as create_exec:
            await connector.generate("plain prompt, no screenshots", images=None)

        sent_input = fake_process.communicate.call_args.kwargs.get("input").decode("utf-8")
        self.assertEqual(sent_input, "plain prompt, no screenshots")
        args, kwargs = create_exec.call_args
        self.assertNotIn("--add-dir", args)


class HistoryCapTests(unittest.TestCase):
    def setUp(self):
        self._orig_history_file = history_writer.HISTORY_FILE
        self._orig_legacy_file = history_writer._LEGACY_FILE
        self.tmp_dir = Path(__file__).resolve().parent / "_tmp_history"
        self.tmp_dir.mkdir(exist_ok=True)
        history_writer.HISTORY_FILE = self.tmp_dir / "failure-history.jsonl"
        history_writer._LEGACY_FILE = self.tmp_dir / "failure-history.txt"  # force-absent

    def tearDown(self):
        try:
            history_writer.HISTORY_FILE.unlink(missing_ok=True)
        except OSError:
            pass
        try:
            self.tmp_dir.rmdir()
        except OSError:
            pass
        history_writer.HISTORY_FILE = self._orig_history_file
        history_writer._LEGACY_FILE = self._orig_legacy_file

    def _write_records(self, records):
        history_writer.HISTORY_FILE.write_text(
            "\n".join(json.dumps(r) for r in records) + "\n"
        )

    def test_large_history_is_capped(self):
        records = []
        for day in range(20):
            for i in range(10):
                records.append(
                    {
                        "record_type": "failure",
                        "run_date": f"2026-01-{day + 1:02d}",
                        "feature": "Life_PMP",
                        "scenario": f"Scenario {day}-{i}",
                        "error_type": "TimeoutError",
                        "failed_step": "some step " * 10,
                        "fix": "a fairly long prior fix description " * 5,
                    }
                )
        self._write_records(records)

        result = history_writer.get_history_for_feature("file:src/test/.../Life_PMP.feature")
        self.assertLessEqual(len(result), history_writer.MAX_HISTORY_CHARS + 200)
        self.assertIn("truncated", result)

    def test_small_history_is_not_truncated(self):
        self._write_records(
            [
                {
                    "record_type": "failure",
                    "run_date": "2026-01-01",
                    "feature": "Life_PMP",
                    "scenario": "Scenario A",
                    "error_type": "AssertionError",
                    "failed_step": "step",
                }
            ]
        )
        result = history_writer.get_history_for_feature("Life_PMP.feature")
        self.assertNotIn("truncated", result)
        self.assertIn("Scenario A", result)


class ContextRoutingTests(unittest.TestCase):
    def test_existing_route_file_is_still_returned_verbatim(self):
        result = load_context_for_features(["file:src/test/resources/features/life/Life_PMP.feature"])
        expected = (CONTEXT_DIR / "life-and-npi.txt").read_text(encoding="utf-8")
        self.assertEqual(result, expected)

    def test_missing_route_file_falls_back_instead_of_placeholder(self):
        # "studio" routes to studio.txt, which does not exist in this repo.
        result = load_context_for_features(["file:src/test/resources/features/studio/Studio_Explorer.feature"])
        self.assertNotIn("context file not found", result)
        fallback = (CONTEXT_DIR / FALLBACK_CONTEXT_FILE).read_text(encoding="utf-8")
        self.assertEqual(result, fallback)

    def test_no_keyword_match_falls_back_cleanly(self):
        result = load_context_for_features(["totally_unmatched_feature_name"])
        self.assertNotIn("context file not found", result)

    def test_never_returns_not_found_placeholder_for_any_configured_route(self):
        for keywords, _filename in CONTEXT_ROUTE_RULES:
            result = load_context_for_features([f"file:features/{keywords[0]}/Something.feature"])
            self.assertNotIn("context file not found", result)


if __name__ == "__main__":
    unittest.main()
