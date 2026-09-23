from __future__ import annotations

import anthropic

from failure_analyzer.llm.base_connector import BaseLLMConnector

_MODEL_ALIASES: dict[str, str] = {
    "sonnet": "claude-sonnet-4-6",
    "opus": "claude-opus-4-8",
    "haiku": "claude-haiku-4-5-20251001",
    "sonnet-4-6": "claude-sonnet-4-6",
    "opus-4-8": "claude-opus-4-8",
}

_THINKING_BUDGETS: dict[str, int] = {
    "low": 1_024,
    "medium": 8_000,
    "high": 16_000,
}


def _resolve_model(name: str) -> str:
    if name.startswith("claude-"):
        return name
    return _MODEL_ALIASES.get(name.lower(), name)


class ClaudeConnector(BaseLLMConnector):
    def __init__(
        self,
        api_key: str,
        model: str = "claude-sonnet-4-6",
        timeout_ms: int = 180_000,
        thinking: str = "none",
    ):
        self.client = anthropic.AsyncAnthropic(
            api_key=api_key,
            timeout=timeout_ms / 1000,
        )
        self.model = _resolve_model(model)
        self.thinking = thinking.lower()

    async def generate(self, prompt: str, images: list[str] | None = None) -> str:
        content: list[dict] = []

        if images:
            for img_b64 in images:
                content.append(
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": "image/jpeg",
                            "data": img_b64,
                        },
                    }
                )

        content.append({"type": "text", "text": prompt})

        messages = [{"role": "user", "content": content}]

        budget = _THINKING_BUDGETS.get(self.thinking, 0)
        kwargs: dict = {
            "model": self.model,
            # Reserve 8192 tokens for text output on top of the thinking budget.
            # Without this, thinking consumes almost all of max_tokens and the
            # actual analysis gets truncated at the same ceiling as no-thinking.
            "max_tokens": max(4096, budget + 8192),
            "messages": messages,
        }
        if budget:
            kwargs["thinking"] = {"type": "enabled", "budget_tokens": budget}

        # Typed, consistent failures at the connector boundary — mirrors
        # ClaudeCodeConnector's TimeoutError/RuntimeError contract so callers
        # (llm_static_triage.py, main.py) can rely on the same exception
        # shape regardless of which connector is configured.
        try:
            response = await self.client.messages.create(**kwargs)
        except anthropic.APITimeoutError as exc:
            raise TimeoutError(f"Claude API timed out: {exc}") from exc
        except anthropic.APIStatusError as exc:
            raise RuntimeError(f"Claude API returned {exc.status_code}: {exc.message}") from exc
        except anthropic.APIError as exc:
            raise RuntimeError(f"Claude API request failed: {exc}") from exc

        # Extended thinking responses have multiple content blocks;
        # return only the text block(s).
        text_parts = [b.text for b in response.content if hasattr(b, "text")]
        return "\n".join(text_parts).strip()
