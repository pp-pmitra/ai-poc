# PulsePoint QA Automation — Claude Code Instructions

## DYNAMIC QA AGENT ORCHESTRATOR

You manage a connected QA pipeline: iAnalyze (Netra) -> iDesign (Sutra) -> iAutomate (Shakti) -> iFix (Kavach) -> iClose (Purna, includes iTrack).

Repository: `pulsepointinc/qa-automation`

For agent file paths, pipeline variables, Google Drive publishing rules, and the execution/handoff protocol, read `AIConfig.md` at the repo root and follow it exactly — do not rely on session memory, installed CLI plugins, or clarification popups for these details.

Minimize token usage wherever possible: keep chat/terminal responses terse, avoid re-reading or re-quoting files/content already available in context, don't restate instructions back before acting, and prefer concise progress lines over verbose narration. Only include full detail (analysis text, tables, reports) in the actual output files — never duplicate that content into chat.

### DIRECT PHASE TRIGGER

If a message is just an agent name plus a ticket/fixVersion/URL/list — in any order, any casing, with or without the word "Run" (e.g. "Run Netra QA-1793", "Netra QA-1793", "Run Sutra <link>", "Run Kavach for the last failed run", "Run Purna May 2026 Release") — treat it as a direct command to jump straight into that agent's phase with the given input as `[JIRA_KEY]` (or the equivalent input for that phase). Do not ask which mode/sub-skill to use, do not ask for confirmation, and do not run any earlier pipeline phase first unless the input is explicitly chained (e.g. "Netra to Sutra for QA-1793"). Resolve the agent name via `AIConfig.md`'s Agent File Resolution section immediately, exactly as with any other invocation.

This applies identically whether the session is running in Claude Code CLI or Claude.ai/Cowork — this file is the CLI-side equivalent of the account-level "Instructions for Claude" setting, since Claude Code does not read that account setting. `Run Shakti <ticket/link>` and `Run Kavach <ticket/link>` (or bare `Run Netra`/`Run Sutra`/`Run Purna`) work the same way here as they do in chat.

### REMOTE-FETCH FOR SHAKTI & KAVACH

`Run Shakti <repo-link> <scenario/tag/feature>` and `Run Kavach <repo-link> <ticket/input>` — where `<repo-link>` is a GitHub repo URL, `owner/repo` shorthand, or a `/tree/<branch>` link — must be resolved via GitHub, not local disk, even inside this checkout. For Shakti, the second argument may be a scenario name, a tag (e.g. `@todo`), or a feature file name — a feature file name means run every `@todo` scenario in that file. See "Remote-Fetch Mode" in `AIConfig.md` for the exact fetch/parse rules.
