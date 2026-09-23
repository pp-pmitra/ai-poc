import os
import sys

from failure_analyzer.config import LLMConfig
from failure_analyzer.llm.base_connector import BaseLLMConnector


def create_connector(llm_config: LLMConfig) -> BaseLLMConnector:
    # Normalise: strip hyphens/underscores and lowercase so "Claude-Code",
    # "claude_code", "ClaudeCode" all resolve the same way.
    provider = llm_config.provider.lower().replace("-", "").replace("_", "")

    if provider == "ollama":
        from failure_analyzer.llm.ollama_connector import OllamaConnector

        print(f"Using Ollama provider with model: {llm_config.model}")
        return OllamaConnector(
            base_url=llm_config.ollama_base_url,
            model=llm_config.model,
            timeout_ms=llm_config.timeout_ms,
        )

    if provider in ("claude", "anthropic"):
        from failure_analyzer.llm.claude_connector import ClaudeConnector

        api_key = os.environ.get(llm_config.api_key_env, "")
        if not api_key:
            print(
                f"Error: {llm_config.api_key_env} environment variable is not set."
            )
            sys.exit(1)

        thinking = getattr(llm_config, "thinking", "none")
        label = f"model: {llm_config.model}" + (f", thinking: {thinking}" if thinking != "none" else "")
        print(f"Using Claude provider with {label}")
        return ClaudeConnector(
            api_key=api_key,
            model=llm_config.model,
            timeout_ms=llm_config.timeout_ms,
            thinking=thinking,
        )

    if provider in ("claudecode",):
        from failure_analyzer.llm.claude_code_connector import ClaudeCodeConnector

        print(f"Using Claude Code provider with model: {llm_config.model}")
        return ClaudeCodeConnector(
            model=llm_config.model,
            timeout_ms=llm_config.timeout_ms,
        )

    raise ValueError(f"Unknown LLM provider: {llm_config.provider}")
