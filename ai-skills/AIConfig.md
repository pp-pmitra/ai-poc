# PulsePoint QA Automation — Claude Code Instructions

## DYNAMIC QA AGENT ORCHESTRATOR

You manage a connected QA pipeline: iAnalyze (Netra) -> iDesign (Sutra) -> iAutomate (Shakti) -> iFix (Kavach) -> iClose (Purna, includes iTrack).

Repository: `pulsepointinc/qa-automation`  Branch: `main`

This file holds both the account-level orchestrator behavior and the detailed mechanics for the pipeline (agent file paths, pipeline variables, Google Drive publishing rules, and the execution/handoff protocol). Read it in full whenever any pipeline stage is invoked — do not rely on session memory, installed CLI plugins, or clarification popups for these details.

Minimize token usage wherever possible: keep chat/terminal responses terse, avoid re-reading or re-quoting files/content already available in context, don't restate instructions back before acting, and prefer concise progress lines over verbose narration. Only include full detail (analysis text, tables, reports) in the actual output files — never duplicate that content into chat.

## DIRECT PHASE TRIGGER

If a message is just an agent name plus a ticket/fixVersion/URL/list — in any order, any casing, with or without the word "Run" (e.g. "Run Netra main", "Netra main", "Run Sutra ", "Run Kavach for the last failed run", "Run Purna May 2026 Release") — treat it as a direct command to jump straight into that agent's phase with the given input as `[JIRA_KEY]` (or the equivalent input for that phase). Do not ask which mode/sub-skill to use, do not ask for confirmation, and do not run any earlier pipeline phase first unless the input is explicitly chained (e.g. "Netra to Sutra for main"). Resolve the agent name via the Agent File Resolution section below immediately, exactly as with any other invocation.

This applies identically whether the session is running in Claude Code CLI or Claude.ai/Cowork — this file is the CLI-side equivalent of the account-level "Instructions for Claude" setting, since Claude Code does not read that account setting. Run Shakti \<ticket/link> and Run Kavach \<ticket/link> (or bare Run Netra/Run Sutra/Run Purna) work the same way here as they do in chat.

## Agent File Resolution (do not search local memory)

When "Netra", "Sutra", "Shakti", "Kavach", or "Purna" is mentioned, do NOT check internal session memory, installed CLI plugins, or ask clarification popups. IMMEDIATELY read the skill definition file from workspace disk or the remote GitHub branch above:

1. **iAnalyze (Netra):**
   * Path: `ai-skills/netra/iAnalyze.md`
   * Action: Read the file immediately. Execute its 14-step requirement analysis workflow.
   * Assets to Inspect:
     * `ai-skills/netra/Test Design Template.xlsx`
     * `ai-skills/netra/Confluence.md` (if Confluence docs are in scope)
   * Outputs Generated: Update `[CACHE_FILE]`, `[XLSX_PATH]`, `[DOCX_PATH]`, and `[HTML_PATH]`.
2. **iDesign (Sutra):**
   * Path: `ai-skills/sutra/iDesign.md`
   * Action: Read the file immediately. Consume `[XLSX_PATH]` / `[DOCX_PATH]` from Netra and parse `navigation-map.html`.
   * Outputs Generated: Generate Gherkin scenarios in `src/test/resources/features/`, commit, open PR, and update `[GHERKIN_FEATURES]`, `[BRANCH_NAME]`, `[PR_LINK]`.
3. **iAutomate (Shakti):**
   * Path: `ai-skills/shakti/iAutomate.md`
   * Action: Read the file immediately. Process `@todo` scenarios in `[GHERKIN_FEATURES]` to build glue code and step definitions.
   * Accepted input (`[GHERKIN_FEATURES]`) — one of:
     * Scenario name — run that single `@todo` scenario only.
     * Tag name (e.g. `@todo`, or any other tag present in the suite) — run every scenario carrying that tag.
     * Feature file name (e.g. `Life_Deal_Platform.feature`) — scan that file for every `@todo`-tagged scenario and run all of them, in file order.
   * Resolve which of the three the input is by matching it against the feature files first (exact `.feature` filename match), then tags (leading `@`), then falling back to a scenario-title match. If ambiguous or no match is found, ask which scenario/tag/feature was meant rather than guessing.
4. **iFix (Kavach):**
   * Path: `.claude/agents/kavach-diagnose.md`, which runs its three preloaded skills in order — `.claude/skills/failure-triage/SKILL.md`, `.claude/skills/live-replay-diagnosis/SKILL.md`, `.claude/skills/verdict-reporting/SKILL.md`
   * Action: Read the file immediately. Replay failing Cucumber/Playwright scenarios live in the browser; classify each as a script issue (fix proposed) or a product bug (flagged only); write a timestamped verdict report.
   * Outputs Generated: Update `[VERDICT_REPORT]`.
   * Note: applying a proposed fix is a separate stage, `.claude/skills/kavach-repair/SKILL.md` (agent: `.claude/agents/kavach-repair.md`) — always interactive, never invoked automatically from here.
5. **iClose (Purna):**
   * Path (closure/compliance audit): `ai-skills/purna/iClose.md`
   * Action: Read the file immediately. Audit the given QA ticket or fix version for comment/test-evidence closure compliance, applying the Global Exclusion Filter, and produce the downloadable Excel/Sheet report (compliance-gap tables, Ready for Release table, Scope Notes sheet).
   * Path (status/tracking — iTrack, folded into iClose): `ai-skills/purna/iTrack.md`
   * Action: Read the file immediately. Query the Jira "QA" project for the release; run staleness detection, comment analysis, and/or Slack notification drafting per the request's detected intent.
   * Outputs Generated: Update `[COMPLIANCE_REPORT_PATH]` and, when iTrack is invoked, `[SLACK_DRAFTS]`.

Note: iMaintenance (Trishul) now has a skill file — `.claude/skills/kavach-repair/SKILL.md` — but it is always interactive and never invoked automatically from this orchestrator; a human runs it directly after reviewing Kavach's verdict.

## Remote-Fetch Mode (Claude Code CLI — iAutomate/Shakti & iFix/Kavach)

`iAutomate (Shakti)` and `iFix (Kavach)` are run through Claude Code CLI, which does not read the account-level "Instructions for Claude." This file covers that gap when the CLI session's working directory is this repo — but these two stages must also work when it is not: no local checkout present, nothing to read from disk.

Trigger: a prompt of the form `Run Shakti <repo-link> <scenario/tag/feature>` or `Run Kavach <repo-link> <ticket/input>`, where `<repo-link>` is a GitHub repo reference (full URL, `owner/repo` shorthand, or a URL with `/tree/<branch>`). For Shakti, `<scenario/tag/feature>` follows the same "Accepted input" rules as the Agent File Resolution entry above (scenario name / tag name / feature file name — a feature file name means run every `@todo` scenario in it).

Resolution rule: when a repo link is supplied this way, it is authoritative — fetch from GitHub via the GitHub connector (`mcp__github__get_file_contents` or equivalent) instead of reading local disk, even if a local checkout of this repo happens to be open. Do not mix sources within one run.

1. Parse `<repo-link>`:
   * `owner`/`repo` from the path segment before `/tree/` or `/blob/` (or the whole `owner/repo` shorthand).
   * `ref` (branch) from the segment after `/tree/`, if present; otherwise use the repository's default branch.
2. Fetch the skill file at the fixed path for that agent, on that `owner/repo`/`ref`:
   * Shakti: `ai-skills/shakti/iAutomate.md`
   * Kavach: `.claude/agents/kavach-diagnose.md` (and its three preloaded skills, see above)
3. Read the fetched content and execute it exactly as written, substituting the given scenario/tag/feature for `[GHERKIN_FEATURES]` (Shakti — resolved per the Accepted-input rules above) or the failing-run reference (Kavach).
4. Any further file the skill instructs you to read (glue files, POM classes, feature files, other skill files) is fetched from the same `owner/repo`/`ref` via the GitHub connector — never assumed to exist locally, never guessed at. If the GitHub connector is unavailable or the fetch fails (bad link, missing branch, 404), stop and report the failure; do not fall back to local disk silently and do not fabricate file content.
5. Commits/PRs this stage produces (per its own skill file) go to the same `owner/repo` via the GitHub connector, not to a local git working tree.

If no `<repo-link>` is given and no local checkout is present either, ask for the repo link rather than guessing which repository/branch is intended.

## Google Drive Publishing & Variable Resolution (orchestrator-level — never edit any skill file for this)

Runs after iAnalyze (Netra) returns control and has reported `[XLSX_PATH]`, `[DOCX_PATH]`, and `[HTML_PATH]` as local paths:

1. For each of the three files, read its bytes and call `google-drive:uploadFile` with `contentBase64` (not `localPath` — that refers to the connector's own machine, not the workspace disk), `name` matching the original filename, its `mimeType` (`application/vnd.openxmlformats-officedocument.wordprocessingml.document` for the docx, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` for the xlsx, `text/html` for the dashboard), and `convertToGoogleFormat: false` (preserves Netra's TOC field, autofilter, and styling — there's no native Google format for HTML to convert into anyway, so this is a no-op there).
2. Call `google-drive:addPermission` on each returned file `id`: `type: anyone`, `role: reader`, `allowFileDiscovery: false`.
3. Build each link: `https://drive.google.com/uc?export=download&id=<id>`.
4. Overwrite `[XLSX_PATH]`, `[DOCX_PATH]`, and `[HTML_PATH]` with these links.

Resolution rule — applies before any downstream step consumes a pipeline variable: if `[XLSX_PATH]`, `[DOCX_PATH]`, `[HTML_PATH]`, or any future link-valued variable starts with `https://`, resolve it to a local file yourself, before acting on the target agent's skill file — extract the file id and call `google-drive:downloadFile(fileId, localPath="/home/claude/<original filename>")`, then proceed exactly as that skill file describes. No downstream skill file ever needs to know a value was a link.

Fresh-read rule — applies alongside the resolution rule above, to every file-valued pipeline variable (`[CACHE_FILE]`, `[XLSX_PATH]`, `[DOCX_PATH]`, `[HTML_PATH]`, `[GHERKIN_FEATURES]`, `[VERDICT_REPORT]`, `[COMPLIANCE_REPORT_PATH]`, `[SLACK_DRAFTS]`, and any future addition): whenever an agent step is about to consume one of these — whether it started local or was just resolved down from a Drive link via the rule above — read that file's actual current bytes from disk at the moment of consumption. Never substitute cached, remembered, or previously-generated content, even content from earlier in the same session, even content you generated yourself moments ago. The file on disk is the source of truth; memory of having authored it is not. This is orchestrator-level like the rule above — no individual agent's skill file needs its own version of it.

## Pipeline Memory Variables

Track and pass these dynamic state variables across sequential prompts and agent handoffs:

* `[JIRA_KEY]`: Active Jira ticket, fixVersion, or URL
* `[CACHE_FILE]`: Path to generated JSON cache (`Cache_<id>_<date>.json`)
* `[XLSX_PATH]`: Local path to the generated Excel Test Design Document immediately after Netra (`Test_Design_<id>_<date>.xlsx`) — becomes a Google Drive shareable link only once the Drive Publishing step above has actually run. Either state is subject to the fresh-read rule.
* `[DOCX_PATH]`: Same pattern as `[XLSX_PATH]`, for the Word Deep Analysis document (`Deep_Analysis_<id>_<date>.docx`).
* `[HTML_PATH]`: Same pattern as `[XLSX_PATH]`, for the Release Readiness Dashboard (`Release_Readiness_Dashboard_<id>_<date>.html`).
* `[BRANCH_NAME]`: Generated Git branch name (e.g., `Sutra_NNN`)
* `[PR_LINK]`: Generated GitHub Pull Request URL
* `[GHERKIN_FEATURES]`: Output directory/file paths for Gherkin features (`src/test/resources/features/`)
* `[VERDICT_REPORT]`: Kavach's timestamped fix/bug classification report for a failed test run
* `[COMPLIANCE_REPORT_PATH]`: Purna's (iClose) downloadable Excel/Sheet closure-compliance report
* `[SLACK_DRAFTS]`: Purna's (iTrack) drafted per-recipient Slack notification messages, pending confirmation

## Execution & Handoff Protocol

* Always load the target agent's skill file first before attempting to generate deliverables.
* Before consuming `[XLSX_PATH]` / `[DOCX_PATH]` / `[HTML_PATH]` / `[GHERKIN_FEATURES]` / `[VERDICT_REPORT]` / `[COMPLIANCE_REPORT_PATH]` / `[SLACK_DRAFTS]` in any agent step, apply the Google Drive Publishing resolution rule (if the value is a link) and the fresh-read rule (always, regardless of link-or-local state) — both happen here, never inside the agent's own skill file.
* Keep chat output concise (widgets/progress lines only as specified in Netra's skill file); full analysis text and test tables belong in the output files (`.docx` / `.xlsx`).
* Execute sequential agent pipelines automatically when chained (e.g., "Run Netra to Sutra for main", "Run Kavach then Purna for the last failed run").

## Diagnostic Marker — TEMPORARY, DELETE AFTER VERIFYING

Tests whether this config file is actually being read by a given surface (Claude.ai chat vs. Claude Code CLI), independent of whether Netra's own skill file loads correctly.

On receiving `/Run Netra` — or any message that triggers the iAnalyze (Netra) resolution above — print this line first, verbatim, on its own, before reading `netra/iAnalyze.md` or doing anything else:

`>>> ORCH-INSTRUCTIONS-LOADED`

Then continue into the Netra workflow exactly as normal — this line is a prefix, not a replacement. Run the same trigger in both Claude.ai chat and Claude Code CLI: if it prints in one but not the other, that surface isn't picking up this file. Once both are confirmed, delete this entire section — it has no purpose in real runs and would prepend noise to every future Netra output otherwise.