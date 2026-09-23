# Failure Analyzer

Diagnoses failed Cucumber/Playwright scenarios by replaying them live in the
browser, classifies each as a **script issue** (stale locator/timing — fix
proposed) or a **potential bug** (real app behavior — flagged only), and
writes a verdict report. It never edits or commits anything on its own.

## How it works

Diagnosis is driven live by Claude, not by a separate LLM report-generation
step — the old `failure_analyzer.main` pipeline (LLM-authored bug reports,
`script-fixes.json`) is retired because live replay against the real app is
the actual source of truth. The flow is two commands:

### 1. `/analyze-failure` — diagnose

1. **Mechanical extraction** (`list_failures.py`, no LLM, no tokens) parses
   `target/cucumber-reports/cucumber.json` + Playwright trace data into
   `failure-analyzer/history/failures-for-replay.json`. Each failure is
   enriched with two same-run reliability signals — `stepReliability` and
   `backgroundReliability` — that flag a step as "likely intermittent" if it
   passed elsewhere in the same run most of the time, and a `groupId` that
   clusters failures sharing the same exception + step pattern so only one
   representative per group needs a full replay.
2. **History/pattern lookup** — reads `.claude/fix-patterns/*.md` and
   `failure-analyzer/history/fix-history.json` so it never repeats an
   approach already known to fail, and reuses one already known to work.
3. **Per-group worker diagnosis loop** — `replay_workers.py` turns each
   `groupId` into a compact, redacted worker packet and prompt. Each packet is
   diagnosed in a fresh Claude CLI session, which performs the faithful live
   replay via Playwright MCP and returns a JSON receipt. Likely-intermittent
   groups skip replay entirely.
4. Each failure is classified **script issue**, **potential bug**, or **not
   reproduced**, and every proposed fix is presented (file, line, before →
   after, live-verification evidence) — nothing is edited yet.
5. Writes a timestamped verdict table to
   `failure-analyzer/history/replay-verdict-<YYYY-MM-DD-HHmm>.md` and appends
   one entry per scenario to `failure-analyzer/history/fix-history.json`.
6. After the verdict report and history writes are complete, the command
   touches the matching CDP done marker (`.claude/auth/life-cdp-done` or
   `.claude/auth/studio-cdp-done`) so the held browser closes cleanly.

### 2. `/auto-fix` — apply, verify, PR

Reads the latest verdict file, applies each `script-issue` fix, verifies it
with `mvn test -Dcucumber.filter.name="<scenario>"`, commits only the fixes
that pass, reverts anything that doesn't, and opens a PR. Only
`.feature`, `src/test/java/stepdefinitions/`, and `src/main/java/pages/`
files are ever touched — never application source.

Full phase-by-phase rules live in [.claude/commands/analyze-failure.md](../.claude/commands/analyze-failure.md)
and [.claude/commands/auto-fix.md](../.claude/commands/auto-fix.md).

## Prerequisites

- A Cucumber run has already produced `target/cucumber-reports/cucumber.json`
  and Playwright traces under `target/` (run `mvn test`, or
  `mvn test -Dtest=FailedTestRunner` to re-run only previous failures).
- The `playwright` MCP server is enabled (already configured in
  `.claude/settings.json`).

## Running from Claude Code (interactive)

```
/analyze-failure
```

Review the printed verdict table, then approve fixes:

```
/auto-fix
```

Pass a substring to scope `/auto-fix` to matching scenarios only, e.g.
`/auto-fix "Rename, Duplication"`.

## Running from the terminal with `claude -p`

The diagnosis loop drives real Playwright MCP tool calls and reads/writes
files without a human approving each one, so it needs a non-interactive
permission mode plus an explicit allowlist of the tools it's allowed to call
on its own:

```bash
claude -p "/analyze-failure" \
  --mcp-config .claude/mcp-ci-life.json \
  --strict-mcp-config \
  --permission-mode auto \
  --allowedTools "Bash(*),Read,Write(*),Edit(*),Grep,Glob,mcp__playwright__browser_navigate,mcp__playwright__browser_tabs,mcp__playwright__browser_snapshot,mcp__playwright__browser_evaluate,mcp__playwright__browser_click,mcp__playwright__browser_fill_form,mcp__playwright__browser_type,mcp__playwright__browser_press_key,mcp__playwright__browser_wait_for,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_close,mcp__playwright__browser_run_code_unsafe" \
  --verbose \
  --output-format text
```

- Use `.claude/mcp-ci-life.json` for Life batches; use
  `.claude/mcp-ci-studio.json` for Studio batches. Those configs attach the
  Playwright MCP server to the browser held by the matching auth bootstrap via
  `--cdp-endpoint`.
- `--strict-mcp-config` is intentional: it prevents Claude Code from also
  loading the default `.mcp.json`, which would start a separate unauthenticated
  browser instead of attaching to the held CI session.
- `--permission-mode auto` still honors the `deny` list in
  `.claude/settings.json` (no `git push`, no `git reset --hard`), unlike
  `bypassPermissions`.
- `--allowedTools` is required in `-p` mode regardless — without it there's
  no one at the keyboard to approve the `mcp__playwright__browser_*`/`Bash`
  calls the replay loop makes, so it would stall waiting for approval.
- `--verbose --output-format text` streams the live-replay steps as they
  happen instead of waiting for one final blob.
- In CI CDP mode, browser headless/headed behavior is controlled by the auth
  bootstrap (`-Dauth.headless=true|false`). The Playwright MCP server attaches
  to that browser; it should not launch its own separate browser.
- After the final verdict report is written, `/analyze-failure` closes the held
  browser by touching `.claude/auth/life-cdp-done` for Life or
  `.claude/auth/studio-cdp-done` for Studio.

Applying fixes afterwards (still requires human review of the verdict output
first — `/analyze-failure` never auto-applies):

```bash
claude -p "/auto-fix" \
  --permission-mode auto \
  --allowedTools "Bash(*),Read,Write(*),Edit(*),Grep,Glob" \
  --verbose \
  --output-format text
```

To scope to specific scenarios:

```bash
claude -p '/auto-fix "Studio Explorer Workspace"' \
  --permission-mode auto \
  --allowedTools "Bash(*),Read,Write(*),Edit(*),Grep,Glob" \
  --verbose \
  --output-format text
```

Both commands are safe to run unattended this way: `/analyze-failure` only
proposes changes (nothing is written outside `failure-analyzer/history/` and
`.claude/fix-patterns/`), and `/auto-fix` only commits a fix after its own
`mvn test` run for that scenario passes, reverting otherwise. `/auto-fix`
doesn't touch a browser, so its `--allowedTools` doesn't need the Playwright
entries.

## Standalone scripts

These are the mechanical pieces the commands above call; they can also be
run directly for debugging:

```bash
cd failure-analyzer

# Mechanical failure extraction only (no LLM) — same thing /analyze-failure
# runs in Phase 1.
python3 list_failures.py [-o OUTPUT_PATH]

# Build compact per-group worker prompts/packets for fresh Claude CLI sessions.
python3 replay_workers.py [--write-runner]

# Validate worker receipts and enforce the confirmed-product-bug gate.
python3 validate_replay_receipts.py [path/to/manifest.json]

# Append every failure from the last cucumber.json run to the rolling
# history file (failure-history.jsonl), independent of the replay workflow.
python3 record_history.py [path/to/cucumber.json]

# Regenerate app-glossary.txt descriptions for new/changed .feature files
# (used to keep LLM prompt context current; --all reprocesses everything).
python3 refresh_glossary.py [--all]
```

## Key files

| Path | Purpose |
|---|---|
| `list_failures.py` | Mechanical Cucumber/trace parsing → `history/failures-for-replay.json` |
| `history/failures-for-replay.json` | Input to the live-replay diagnosis loop |
| `replay_workers.py` | Builds compact per-`groupId` prompts/packets and an optional worker runner |
| `validate_replay_receipts.py` | Validates JSON receipts and downgrades unproven confirmed-product-bug claims |
| `history/replay-packets/` | Generated redacted worker packets, prompts, receipts, and manifests |
| `history/fix-history.json` | Every fix ever attempted, one entry per scenario |
| `history/replay-verdict-*.md` | One timestamped verdict table per `/analyze-failure` run |
| `../.claude/fix-patterns/*.md` | Known-good fix patterns, one file per feature |
| `config.json` | LLM provider/model, paths, Jira settings (Jira reporting currently disabled) |
| `failure_analyzer/` | Python package: parsers, trace correlation, grouping, LLM connectors — mostly infra for the retired report-generation pipeline; `parsers`/`grouping`/`config` are still used by `list_failures.py` |
