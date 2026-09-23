from __future__ import annotations

import httpx

from failure_analyzer.llm.base_connector import BaseLLMConnector


class OllamaConnector(BaseLLMConnector):
    def __init__(
        self,
        base_url: str = "http://localhost:11434",
        model: str = "llama3",
        timeout_ms: int = 300_000,
    ):
        self.base_url = base_url.rstrip("/")
        self.model = model
        self.timeout_s = timeout_ms / 1000

    async def generate(self, prompt: str, images: list[str] | None = None) -> str:
        payload: dict = {
            "model": self.model,
            "prompt": prompt,
            "stream": False,
        }

        if images:
            payload["images"] = images

        # Typed, consistent failures at the connector boundary — mirrors
        # ClaudeCodeConnector's TimeoutError/RuntimeError contract so callers
        # (llm_static_triage.py, main.py) can rely on the same exception
        # shape regardless of which connector is configured.
        try:
            async with httpx.AsyncClient(timeout=self.timeout_s) as client:
                response = await client.post(
                    f"{self.base_url}/api/generate",
                    json=payload,
                )
                response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise TimeoutError(f"Ollama request timed out after {self.timeout_s}s") from exc
        except httpx.HTTPStatusError as exc:
            raise RuntimeError(f"Ollama returned {exc.response.status_code}: {exc.response.text}") from exc
        except httpx.HTTPError as exc:
            raise RuntimeError(f"Ollama request failed: {exc}") from exc

        return response.json()["response"]
