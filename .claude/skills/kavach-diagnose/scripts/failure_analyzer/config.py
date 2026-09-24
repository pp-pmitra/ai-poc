from __future__ import annotations

import json
from pathlib import Path
from dataclasses import dataclass, field


@dataclass
class LLMConfig:
    provider: str = "claudeCode"
    model: str = "sonnet"
    ollama_base_url: str = "http://localhost:11434"
    api_key_env: str = "ANTHROPIC_API_KEY"
    # A screenshot is only ever sent as a *second*, escalation call — the first
    # LLM call per failure is always text-only. See main.py:analyze_failure_with_llm.
    send_screenshots: bool = True
    timeout_ms: int = 180_000
    # Extended thinking: "none" | "low" (1024) | "medium" (8000) | "high" (16000)
    # Only supported by provider="claude" (Anthropic API direct)
    thinking: str = "none"
    # Max concurrent LLM calls during per-failure analysis (hybrid/ai strategy).
    # Each ClaudeCodeConnector call spawns a `claude` CLI subprocess, so this also
    # bounds concurrent OS processes — keep modest unless using a lighter connector.
    concurrency: int = 5


@dataclass
class PathsConfig:
    cucumber_json: str = "../../../../target/cucumber-reports/cucumber.json"
    traces_dir: str = "../../../../target"
    features_root: str = "../../../../src/test/resources/features"
    output_dir: str = "../../../../target/bug-reports"


@dataclass
class WorkItemConfig:
    default_type: str = "maintenanceStory"
    error_types: list[str] = field(default_factory=lambda: ["AssertionError"])


@dataclass
class AnalysisConfig:
    assertion_strategy: str = "hybrid"


@dataclass
class TracesConfig:
    min_match_score: float = 0.4
    correlation_buffer_ms: int = 2000


@dataclass
class JiraConfig:
    enabled: bool = False
    base_url: str = ""
    project: str = "QA"
    issue_type: str = "Story"
    issue_type_map: dict[str, str] = field(default_factory=dict)
    email_env: str = "JIRA_EMAIL"
    api_token_env: str = "JIRA_API_TOKEN"
    labels: list[str] = field(default_factory=list)
    priority_names: dict[str, str] = field(default_factory=dict)
    default_priority: str = "Normal"
    component_map: dict[str, str] = field(default_factory=dict)


@dataclass
class Config:
    environment: str = "Demo"
    llm: LLMConfig = field(default_factory=LLMConfig)
    paths: PathsConfig = field(default_factory=PathsConfig)
    work_item: WorkItemConfig = field(default_factory=WorkItemConfig)
    analysis: AnalysisConfig = field(default_factory=AnalysisConfig)
    traces: TracesConfig = field(default_factory=TracesConfig)
    jira: JiraConfig = field(default_factory=JiraConfig)
    environment_info: dict = field(default_factory=dict)


def load_config(config_path: Path | None = None) -> Config:
    if config_path is None:
        config_path = Path(__file__).parent.parent / "config.json"

    if not config_path.exists():
        raise FileNotFoundError(
            f"Config file not found: {config_path}\n"
            "Create a config.json next to the failure-analyzer package (see config.json.example)."
        )
    try:
        raw = json.loads(config_path.read_text())
    except json.JSONDecodeError as exc:
        raise ValueError(
            f"config.json is not valid JSON: {config_path}\n"
            f"Parse error at line {exc.lineno}, column {exc.colno}: {exc.msg}"
        ) from exc

    llm_raw = raw.get("llm", {})
    llm = LLMConfig(
        provider=llm_raw.get("provider", "claudeCode"),
        model=llm_raw.get("model", "sonnet"),
        ollama_base_url=llm_raw.get("ollamaBaseUrl", "http://localhost:11434"),
        api_key_env=llm_raw.get("apiKeyEnv", "ANTHROPIC_API_KEY"),
        send_screenshots=llm_raw.get("sendScreenshots", True),
        timeout_ms=llm_raw.get("timeoutMs", 180_000),
        thinking=llm_raw.get("thinking", "none"),
        concurrency=llm_raw.get("concurrency", 5),
    )

    paths_raw = raw.get("paths", {})
    paths = PathsConfig(
        cucumber_json=paths_raw.get("cucumberJson", "../../../../target/cucumber-reports/cucumber.json"),
        traces_dir=paths_raw.get("tracesDir", "../../../../target"),
        features_root=paths_raw.get("featuresRoot", "../../../../src/test/resources/features"),
        output_dir=paths_raw.get("outputDir", "../../../../target/bug-reports"),
    )

    wi_raw = raw.get("workItem", {})
    work_item = WorkItemConfig(
        default_type=wi_raw.get("defaultType", "maintenanceStory"),
        error_types=wi_raw.get("errorTypes", ["AssertionError"]),
    )

    analysis = AnalysisConfig(
        assertion_strategy=raw.get("analysis", {}).get("assertionStrategy", "hybrid"),
    )

    tr_raw = raw.get("traces", {})
    traces = TracesConfig(
        min_match_score=tr_raw.get("minMatchScore", 0.4),
        correlation_buffer_ms=tr_raw.get("correlationBufferMs", 2000),
    )

    jira_raw = raw.get("jira", {})
    jira = JiraConfig(
        enabled=jira_raw.get("enabled", False),
        base_url=jira_raw.get("baseUrl", ""),
        project=jira_raw.get("project", "QA"),
        issue_type=jira_raw.get("issueType", "Story"),
        issue_type_map=jira_raw.get("issueTypeMap", {}),
        email_env=jira_raw.get("emailEnv", "JIRA_EMAIL"),
        api_token_env=jira_raw.get("apiTokenEnv", "JIRA_API_TOKEN"),
        labels=jira_raw.get("labels", []),
        priority_names=jira_raw.get("priorityNames", {}),
        default_priority=jira_raw.get("defaultPriority", "Normal"),
        component_map=jira_raw.get("componentMap", {}),
    )

    return Config(
        environment=raw.get("environment", "Demo"),
        llm=llm,
        paths=paths,
        work_item=work_item,
        analysis=analysis,
        traces=traces,
        jira=jira,
        environment_info=raw.get("environmentInfo", {}),
    )
