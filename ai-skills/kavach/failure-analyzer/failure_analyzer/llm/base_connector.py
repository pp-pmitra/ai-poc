from __future__ import annotations

from abc import ABC, abstractmethod


class BaseLLMConnector(ABC):
    @abstractmethod
    async def generate(self, prompt: str, images: list[str] | None = None) -> str:
        """Send a prompt to the LLM and return the response text.

        Args:
            prompt: The text prompt to send.
            images: Optional list of base64-encoded image strings.
        """
        ...
