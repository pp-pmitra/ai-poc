from __future__ import annotations

import asyncio
import base64
import tempfile
from pathlib import Path

from failure_analyzer.llm.base_connector import BaseLLMConnector

_IMAGE_NOTE = (
    "\n\n---\n**Failure screenshot(s):** the following JPEG file(s) were captured "
    "at the moment of failure — read them with your file-reading tool before "
    "writing the analysis:\n{paths}\n---\n"
)


class ClaudeCodeConnector(BaseLLMConnector):
    def __init__(self, model: str | None = None, timeout_ms: int = 300_000):
        self.model = model
        self.timeout_ms = timeout_ms

    async def generate(self, prompt: str, images: list[str] | None = None) -> str:
        # The Claude CLI has no --image/--attachment flag for headless mode, and
        # embedding base64 image data directly in the prompt text risks exceeding
        # the OS argv-length limit (MAX_ARG_STRLEN, 128KB on Linux) for realistic
        # screenshot sizes. Instead, decode each image to a temp JPEG file and
        # reference it by path — Claude reads it with its own file tool — and
        # pipe the whole prompt through stdin rather than argv (stdin is capped
        # at 10MB, far above anything this prompt reaches).
        #
        # Headless `-p` mode only allows Read within the CLI's working directory
        # (or directories added via --add-dir); a system temp path is outside
        # that by default and the read is silently denied (confirmed against a
        # real run: the model reported "file read was not authorized"). Each
        # call's screenshots get their own private temp directory, granted via
        # --add-dir — Read-only in effect, and scoped to just this call's files
        # rather than the whole shared OS temp directory.
        full_prompt = prompt
        temp_dir: str | None = None
        temp_paths: list[str] = []
        try:
            if images:
                temp_dir = tempfile.mkdtemp(prefix="failure-screenshots-")
                for i, img_b64 in enumerate(images):
                    try:
                        jpeg_bytes = base64.b64decode(img_b64)
                    except (ValueError, TypeError):
                        continue
                    path = str(Path(temp_dir) / f"screenshot-{i}.jpeg")
                    try:
                        Path(path).write_bytes(jpeg_bytes)
                    except OSError:
                        continue
                    temp_paths.append(path)

                if temp_paths:
                    full_prompt = prompt + _IMAGE_NOTE.format(
                        paths="\n".join(f"  - {p}" for p in temp_paths)
                    )

            cmd = ["claude", "-p", "--output-format", "text"]
            if self.model:
                cmd.extend(["--model", self.model])
            if temp_paths:
                cmd.extend(["--add-dir", temp_dir])

            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdin=asyncio.subprocess.PIPE,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
            )

            try:
                stdout, stderr = await asyncio.wait_for(
                    process.communicate(input=full_prompt.encode("utf-8")),
                    timeout=self.timeout_ms / 1000,
                )
            except asyncio.TimeoutError:
                process.kill()
                await process.communicate()
                raise TimeoutError(
                    f"Claude CLI timed out after {self.timeout_ms}ms"
                )

            if process.returncode != 0:
                raise RuntimeError(
                    f"Claude CLI exited with code {process.returncode}: "
                    f"{stderr.decode().strip()}"
                )

            return stdout.decode().strip()
        finally:
            for path in temp_paths:
                Path(path).unlink(missing_ok=True)
            if temp_dir:
                try:
                    Path(temp_dir).rmdir()
                except OSError:
                    pass
