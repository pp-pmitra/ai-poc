"""Cheap, no-browser triage tier inserted between mechanical grouping
(`list_failures.py`) and live Playwright-MCP replay (`replay_workers.py`,
driven by the `live-replay-diagnosis` skill's Phase 3).

Every function here is either pure/deterministic or a single text-only LLM
call with no tool access — nothing in this package can drive a browser. Its
job is to resolve as many failure groups as it safely can (intermittency,
known network noise, exact copy-diffs, a still-present cached fix) and to
flag everything else with `needsLiveReplay: true` for Tier 2 to pick up.
"""
