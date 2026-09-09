# AI Orchestrator Config — QA Pipeline

This file holds the detailed mechanics for the QA agent pipeline
(iAnalyze -> iDesign -> iAutomate -> iFix -> iClose). The account-level
"Instructions for Claude" only points here — read this file whenever any
pipeline stage is invoked; do not rely on session memory for these details.

Repository: `pulsepointinc/qa-automation`
Branch: `QA-1793`

---

## Agent File Resolution (do not search local memory)

When "Netra", "Sutra", "Shakti", "Kavach", or "Purna" is mentioned, do NOT check
internal session memory, installed CLI plugins, or ask clarification popups.
IMMEDIATELY read the skill definition file from workspace disk or the remote
GitHub branch above:

1. **iAnalyze (Netra):**
   - **Path:** `plugins/qa-automation-skills/skills/netra/iAnalyzeSkill.md`
   - **Action:** Read the file immediately. Execute its 14-step requirement analysis workflow.
   - **Assets to Inspect:**
     * `plugins/qa-automation-skills/skills/netra/Test Design Template.xlsx`
     * `plugins/qa-automation-skills/skills/netra/Confluence.md` (if Confluence docs are in scope)
   - **Outputs Generated:** Update `[CACHE_FILE]`, `[XLSX_PATH]`, `[DOCX_PATH]`, and `[HTML_PATH]`.

2. **iDesign (Sutra):**
   - **Path:** `plugins/qa-automation-skills/skills/sutra/iDesignSkill.md`
   - **Action:** Read the file immediately. Consume `[XLSX_PATH]` / `[DOCX_PATH]` from Netra and parse `navigation-map.html`.
   - **Outputs Generated:** Generate Gherkin scenarios in `src/test/resources/features/`, commit, open PR, and update `[GHERKIN_FEATURES]`, `[BRANCH_NAME]`, `[PR_LINK]`.

3. **iAutomate (Shakti):**
   - **Path:** `plugins/qa-automation-skills/skills/shakti/iAutomateSkill.md`
   - **Action:** Read the file immediately. Process `@todo` scenarios in `[GHERKIN_FEATURES]` to build glue code and step definitions.

4. **iFix (Kavach):**
   - **Path:** `plugins/qa-automation-skills/skills/kavach/iFixSkill.md`
   - **Action:** Read the file immediately. Replay failing Cucumber/Playwright scenarios live in the browser; classify each as a script issue (fix proposed) or a product bug (flagged only); write a timestamped verdict report.
   - **Outputs Generated:** Update `[VERDICT_REPORT]`.

5. **iClose (Purna):**
   - **Path (closure/compliance audit):** `plugins/qa-automation-skills/skills/purna/iCloseSkill.md`
   - **Action:** Read the file immediately. Audit the given QA ticket or fix version for comment/test-evidence closure compliance, applying the Global Exclusion Filter, and produce the downloadable Excel/Sheet report (compliance-gap tables, Ready for Release table, Scope Notes sheet).
   - **Path (status/tracking — iTrack, folded into iClose):** `plugins/qa-automation-skills/skills/purna/iTrackSkill.md`
   - **Action:** Read the file immediately. Query the Jira "QA" project for the release; run staleness detection, comment analysis, and/or Slack notification drafting per the request's detected intent.
   - **Outputs Generated:** Update `[COMPLIANCE_REPORT_PATH]` and, when iTrack is invoked, `[SLACK_DRAFTS]`.

Note: iMaintenance (Trishul) is named in the pipeline but has no skill file in the repo yet — nothing to resolve there until it's added.

---

## Google Drive Publishing & Variable Resolution (orchestrator-level — never edit any skill file for this)

Runs after **iAnalyze (Netra)** returns control and has reported `[XLSX_PATH]`, `[DOCX_PATH]`, and `[HTML_PATH]` as local paths:

1. For each of the three files, read its bytes and call `google-drive:uploadFile` with `contentBase64` (not `localPath` — that refers to the connector's own machine, not the workspace disk), `name` matching the original filename, its `mimeType` (`application/vnd.openxmlformats-officedocument.wordprocessingml.document` for the docx, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` for the xlsx, `text/html` for the dashboard), and `convertToGoogleFormat: false` (preserves Netra's TOC field, autofilter, and styling — there's no native Google format for HTML to convert into anyway, so this is a no-op there).
2. Call `google-drive:addPermission` on each returned file `id`: `type: anyone`, `role: reader`, `allowFileDiscovery: false`.
3. Build each link: `https://drive.google.com/uc?export=download&id=<id>`.
4. **Overwrite** `[XLSX_PATH]`, `[DOCX_PATH]`, and `[HTML_PATH]` with these links.

**Resolution rule — applies before *any* downstream step consumes a pipeline variable:** if `[XLSX_PATH]`, `[DOCX_PATH]`, `[HTML_PATH]`, or any future link-valued variable starts with `https://`, resolve it to a local file **yourself, before** acting on the target agent's skill file — extract the file id and call `google-drive:downloadFile(fileId, localPath="/home/claude/<original filename>")`, then proceed exactly as that skill file describes. No downstream skill file ever needs to know a value was a link.

**Fresh-read rule — applies alongside the resolution rule above, to every file-valued pipeline variable** (`[CACHE_FILE]`, `[XLSX_PATH]`, `[DOCX_PATH]`, `[HTML_PATH]`, `[GHERKIN_FEATURES]`, `[VERDICT_REPORT]`, `[COMPLIANCE_REPORT_PATH]`, `[SLACK_DRAFTS]`, and any future addition): whenever an agent step is about to consume one of these — whether it started local or was just resolved down from a Drive link via the rule above — read that file's actual current bytes from disk at the moment of consumption. Never substitute cached, remembered, or previously-generated content, even content from earlier in the same session, even content you generated yourself moments ago. The file on disk is the source of truth; memory of having authored it is not. This is orchestrator-level like the rule above — no individual agent's skill file needs its own version of it.

---

## Pipeline Memory Variables

Track and pass these dynamic state variables across sequential prompts and agent handoffs:
- `[JIRA_KEY]`: Active Jira ticket, fixVersion, or URL
- `[CACHE_FILE]`: Path to generated JSON cache (`Cache_<id>_<date>.json`)
- `[XLSX_PATH]`: Local path to the generated Excel Test Design Document immediately after Netra (`Test_Design_<id>_<date>.xlsx`) — becomes a Google Drive shareable link only once the Drive Publishing step above has actually run. Either state is subject to the fresh-read rule.
- `[DOCX_PATH]`: Same pattern as `[XLSX_PATH]`, for the Word Deep Analysis document (`Deep_Analysis_<id>_<date>.docx`).
- `[HTML_PATH]`: Same pattern as `[XLSX_PATH]`, for the Release Readiness Dashboard (`Release_Readiness_Dashboard_<id>_<date>.html`).
- `[BRANCH_NAME]`: Generated Git branch name (e.g., `Sutra_NNN`)
- `[PR_LINK]`: Generated GitHub Pull Request URL
- `[GHERKIN_FEATURES]`: Output directory/file paths for Gherkin features (`src/test/resources/features/`)
- `[VERDICT_REPORT]`: Kavach's timestamped fix/bug classification report for a failed test run
- `[COMPLIANCE_REPORT_PATH]`: Purna's (iClose) downloadable Excel/Sheet closure-compliance report
- `[SLACK_DRAFTS]`: Purna's (iTrack) drafted per-recipient Slack notification messages, pending confirmation

---

## Execution & Handoff Protocol

- Always load the target agent's skill file first before attempting to generate deliverables.
- Before consuming `[XLSX_PATH]` / `[DOCX_PATH]` / `[HTML_PATH]` / `[GHERKIN_FEATURES]` / `[VERDICT_REPORT]` / `[COMPLIANCE_REPORT_PATH]` / `[SLACK_DRAFTS]` in any agent step, apply the Google Drive Publishing resolution rule (if the value is a link) and the fresh-read rule (always, regardless of link-or-local state) — both happen here, never inside the agent's own skill file.
- Keep chat output concise (widgets/progress lines only as specified in Netra's skill file); full analysis text and test tables belong in the output files (`.docx` / `.xlsx`).
- Execute sequential agent pipelines automatically when chained (e.g., *"Run Netra to Sutra for QA-1793"*, *"Run Kavach then Purna for the last failed run"*).

---

## Diagnostic Marker — TEMPORARY, DELETE AFTER VERIFYING

Tests whether this config file is actually being read by a given surface (Claude.ai chat vs. Claude Code CLI), independent of whether Netra's own skill file loads correctly.

On receiving `/Run Netra` — or any message that triggers the iAnalyze (Netra) resolution above — print this line first, verbatim, on its own, before reading `netra/iAnalyzeSkill.md` or doing anything else:

`>>> ORCH-INSTRUCTIONS-LOADED :: QA-1793-NETRA-CHECK`

Then continue into the Netra workflow exactly as normal — this line is a prefix, not a replacement. Run the same trigger in both Claude.ai chat and Claude Code CLI: if it prints in one but not the other, that surface isn't picking up this file. Once both are confirmed, **delete this entire section** — it has no purpose in real runs and would prepend noise to every future Netra output otherwise.
