---
name: kavach-imaintain
description: >-
  Applies kavach-diagnose's proposed script fixes to Cucumber/Java
  page-object files, verifies each fix passes Maven three times, and
  raises a single PR per run. Invoke after kavach-diagnose has produced
  a verdict containing script_issue_fix_proposed entries, or in
  isolation mode (IMAINTENANCE_MODE=isolation or IMAINTENANCE_TARGET
  set, no receipt file) to apply a known fix pattern from a live Maven
  observation pass. Never diagnoses unknown failures — kavach-diagnose
  does that. Never commits without a Maven green. Always interactive;
  never runs unattended.
---

# iMaintenance — Apply and Verify kavach Script Fixes

<!-- Canonical procedure for the kavach-imaintain skill, consumed by .claude/agents/kavach-imaintain.md. -->

## Contents
- [Phase 0.5: Isolation mode input normalisation](#phase-05--isolation-mode-input-normalisation)
- [Phase 1: Discovery](#phase-1--discovery)
- [Phase 2: Fix preparation](#phase-2--fix-preparation)
- [Phase 3: Apply, format-check, and verify](#phase-3--apply-format-check-and-verify)
- [Phase 3.5: Cascade regression check](#phase-35--cascade-regression-check)
- [Phase 4: Commit](#phase-4--commit)
- [Phase 5: Push branch and open PR](#phase-5--push-branch-and-open-pr)
- [Phase 6: Fix-history update and summary](#phase-6--fix-history-update-and-summary)


**TOOL USE: Bash, Read, Write, Edit, Grep, and Glob are pre-approved. No Playwright browser tools are used in this skill — all verification is Maven-only. Never call `mcp__playwright__*` tools from iMaintenance.** `src/main/java/utils/LocatorProbe.java` is a Maven-only diagnostic helper (checks candidate locators live via non-waiting `count()`/`isVisible()`) — see Phase 3.1 for when and how to use it.

**NON-INTERACTIVE ABORT (runs before everything else, including mode detection):** If stdin is not a TTY or `CI=true` is set in the environment, print exactly:
```
IMAINTENANCE_ABORT: This skill requires interactive confirmation and cannot run unattended.
Review kavach-diagnose's verdict output manually, then invoke /kavach-imaintain from an interactive session.
```
Then exit 1. Do not detect mode. Do not run Phase 0.5. Do not read any files.

iMaintenance reads kavach's verdict output, applies each proposed script fix to the relevant Java page-object or step-definition file, verifies end-to-end with Maven (three runs), and commits passing fixes to a branch for PR review. It never auto-applies without a Maven green. It never converts an infrastructure failure to a `test_failed` verdict.

This skill runs as six phases in order. Phases 1–2 run once (setup). Phase 3 loops once per candidate. Phase 3.5 runs after each Phase 3 pass. Phases 4–6 run once (close out).

## Execution modes

Detect mode at startup — do not ask the user:

- **Interactive** — the default mode. Phase 1 pauses after printing the discovery table and waits for explicit confirmation before touching any files. Phase 3.5 offers a cascade-fix attempt before continuing.
- **Isolation** — `IMAINTENANCE_MODE=isolation` or `IMAINTENANCE_TARGET` is set and no `combined-receipts.json` is present. Phase 0.5 runs first to derive a synthetic receipt from a live Maven observation pass. **Always interactive** — isolation mode never runs unattended. All interactive confirmation gates apply: Phase 1 pauses for approval, Phase 0.5.3 pauses to show the matched pattern before proceeding, and Phase 3.5 offers a cascade-fix attempt. Pattern-matching only — no Playwright.

iMaintenance never runs unattended. There is no headless or CI mode — do not detect or act on `CI=true` or `IMAINTENANCE_MODE=headless`.

Print the detected mode at startup: `Mode: INTERACTIVE` or `Mode: ISOLATION (interactive)`.

## Input sources

- **Primary:** `combined-receipts.json` written by kavach's Phase 3 (`validate_replay_receipts.py`). Contains one receipt per `groupId` with machine-format `verdict`, free-text `recommendedAction`, and free-text `evidence`. There is no structured `fixSuggestion` object on a kavach receipt — Phase 2 derives `targetFile`/`targetLine`/the patch itself from `recommendedAction` plus the matching fix-pattern file, the same way isolation mode already does (Phase 0.5.2).
- **Fix-pattern files:** `../kavach-knowledge/fix-patterns/` — the same files kavach uses. Read the feature-specific file plus `_default.md` for every candidate.
- **Fix history:** `.claude/skills/kavach-diagnose/scripts/history/fix-history.json` — the same file kavach writes (see the verdict-reporting skill's schema). Cross-referenced in Phase 1 to skip candidates already tried with ≥ 2 failed attempts.

If `combined-receipts.json` is not found and mode is **not** `isolation`, check the kavach Phase 4 markdown report for the run date and ask the user to confirm the path before exiting. In isolation mode, `combined-receipts.json` is never required — Phase 0.5 synthesises the receipt.

## Reference files

- [`../kavach-knowledge/fix-patterns/_default.md`](../kavach-knowledge/fix-patterns/_default.md)
- [`../kavach-knowledge/fix-patterns/life-campaign.md`](../kavach-knowledge/fix-patterns/life-campaign.md)
- [`../kavach-knowledge/fix-patterns/life-campaign-dashboard.md`](../kavach-knowledge/fix-patterns/life-campaign-dashboard.md)
- [`../kavach-knowledge/fix-patterns/life-create-campaign.md`](../kavach-knowledge/fix-patterns/life-create-campaign.md)
- [`../kavach-knowledge/fix-patterns/life-create-creative.md`](../kavach-knowledge/fix-patterns/life-create-creative.md)
- [`../kavach-knowledge/fix-patterns/life-create-pixel.md`](../kavach-knowledge/fix-patterns/life-create-pixel.md)
- [`../kavach-knowledge/fix-patterns/life-create-report-template.md`](../kavach-knowledge/fix-patterns/life-create-report-template.md)
- [`../kavach-knowledge/fix-patterns/life-creatives.md`](../kavach-knowledge/fix-patterns/life-creatives.md)
- [`../kavach-knowledge/fix-patterns/life-export-download.md`](../kavach-knowledge/fix-patterns/life-export-download.md)
- [`../kavach-knowledge/fix-patterns/life-line-item-creation.md`](../kavach-knowledge/fix-patterns/life-line-item-creation.md)
- [`../kavach-knowledge/fix-patterns/life-lineitem.md`](../kavach-knowledge/fix-patterns/life-lineitem.md)
- [`../kavach-knowledge/fix-patterns/life-npilists.md`](../kavach-knowledge/fix-patterns/life-npilists.md)
- [`../kavach-knowledge/fix-patterns/life-pixels.md`](../kavach-knowledge/fix-patterns/life-pixels.md)
- [`../kavach-knowledge/fix-patterns/life-pmp.md`](../kavach-knowledge/fix-patterns/life-pmp.md)
- [`../kavach-knowledge/fix-patterns/life-reporttemplates.md`](../kavach-knowledge/fix-patterns/life-reporttemplates.md)
- [`../kavach-knowledge/fix-patterns/life-runreport.md`](../kavach-knowledge/fix-patterns/life-runreport.md)
- [`../kavach-knowledge/fix-patterns/life-schedulereport.md`](../kavach-knowledge/fix-patterns/life-schedulereport.md)
- [`../kavach-knowledge/fix-patterns/life-tactic.md`](../kavach-knowledge/fix-patterns/life-tactic.md)
- [`../kavach-knowledge/fix-patterns/life-tactic-creation.md`](../kavach-knowledge/fix-patterns/life-tactic-creation.md)
- [`../kavach-knowledge/fix-patterns/life-targeting-template-creation.md`](../kavach-knowledge/fix-patterns/life-targeting-template-creation.md)
- [`../kavach-knowledge/fix-patterns/life-targetings.md`](../kavach-knowledge/fix-patterns/life-targetings.md)
- [`../kavach-knowledge/fix-patterns/life-targetingtemplates.md`](../kavach-knowledge/fix-patterns/life-targetingtemplates.md)
- [`../kavach-knowledge/fix-patterns/studio-explorerworkspace.md`](../kavach-knowledge/fix-patterns/studio-explorerworkspace.md)

---

---

## Phase 0.5 — Isolation mode input normalisation

Runs **only when mode is `isolation`**. Skip in interactive mode.

**Purpose:** given a feature file or tag, run one Maven observation pass to capture the live failure, then synthesise a minimal receipt so Phases 2–6 can proceed without a kavach `combined-receipts.json`.

### 0.5.1 Resolve input

Read `IMAINTENANCE_TARGET` (env var) or the `--target` CLI argument. Accept either form:

- A Cucumber tag: `@TC_12345` or `@LifeCampaignDashboard`
- A repo-relative feature file path: `automation-tests/src/test/resources/features/Life_Campaign.feature`

If neither is set, print `ISOLATION_NO_TARGET: set IMAINTENANCE_TARGET or pass --target` and exit 1.

Derive `<module>`, `<featureTag>`, and `<scenarioTag>` using the same rules as Phase 2 step 4.

### 0.5.2 Collect the live failure

Run one Maven pass (observation only — not the three-run verification rule):

```bash
mvn test -Dtest=TestRunner -Dcucumber.filter.tags="@<featureTag or scenarioTag>" \
  -pl <module> -q 2>&1 | tee /tmp/imaintenance-isolation-run.txt
```

Parse `/tmp/imaintenance-isolation-run.txt` for:

- **Failing step text** — the `Step ... FAILED` line from the Cucumber/Surefire output
- **Exception message** — the first `NoSuchElementException` (or similar WebDriver exception) line
- **Broken locator** — the XPath or CSS selector in the exception, if present

Three early exits:

| Condition | Action |
|---|---|
| No Cucumber summary line (Maven died before test execution) | Print `ISOLATION_INFRA_FAILURE: Maven did not reach test execution. Check CDP bootstrap (Life 9223 / Studio 9224) and retry.` Exit 1. |
| Cucumber summary present, all scenarios passed | Print `ISOLATION_NO_FAILURE: all scenarios passed — nothing to fix.` Exit 0. |
| Cucumber summary present, at least one scenario failed | Proceed to 0.5.3. |

### 0.5.3 Pattern lookup

Read `_default.md` and the feature-specific fix-pattern file (derived from `<featureTag>`). Search for a known-good fix block whose broken pattern matches the observed locator or exception type.

**Match found:** synthesise a receipt object:

```json
{
  "groupId": "<featureSlug>/<failingScenarioName>",
  "scenarioId": "<failingScenarioName>",
  "featureFile": "<path>",
  "verdict": "script_issue_fix_proposed",
  "isolationMode": true,
  "fixSuggestion": {
    "targetFile": "<Java page-object file containing the locator — from pattern file>",
    "targetLine": null,
    "description": "<fix description from pattern file>",
    "patchHint": "<patchHint from pattern file>"
  }
}
```

Pause and present the match to the user before proceeding:

```
ISOLATION_MATCH_FOUND
  Scenario:      <scenarioName>
  Failing step:  <step text>
  Broken locator: <XPath or CSS>
  Pattern file:  <fix-pattern filename>
  Proposed fix:  <fix description>
  Target file:   <Java file path>

Proceed with this fix? [y/N]
```

Wait for explicit user confirmation. On N, exit 0 without creating a branch. If the session ends without a response, treat the result as N on next invocation.
On Y: skip Phase 1 step 1 (no `combined-receipts.json` to read) and proceed to Phase 1 step 2 using this synthetic receipt as the candidate list.

**No match found:** print structured no-match report and exit 0:

```
ISOLATION_NO_PATTERN
  Scenario:       <scenarioName>
  Failing step:   <step text>
  Broken locator: <XPath or CSS from exception, or "not extracted">
  Exception:      <exception class>

No known-good fix pattern covers this failure.
Next step: run kavach on this tag for live-replay diagnosis.
  kavach target: @<featureTag or scenarioTag>
```

Do not attempt a fix. Do not open a browser. Do not create a branch.

## Phase 1 — Discovery

1. Read `combined-receipts.json`. Collect all receipts where `verdict == "script_issue_fix_proposed"`. *(In isolation mode, skip this step — use the synthetic receipt from Phase 0.5 as the candidate list.)*
2. Read `.claude/skills/kavach-diagnose/scripts/history/fix-history.json`. For each candidate apply the skip rules:
   - **Skip** if any prior entry for this `groupId` has `verdict == "script_issue_fix_applied"`. Confirm with `git log` that the commit is reachable on main before skipping.
   - **Skip** if `attempts >= 2` and no prior entry has `verdict == "script_issue_fix_applied"` (exhausted attempts, no point retrying).

   **Attempts counter policy:** only `test_failed` and `spotless_failed` verdicts increment `attempts`. `infrastructure_inconclusive` and `source_drift` do not — they are environment or source-state issues, not failed fix attempts. A candidate skipped by the infra-inconclusive guard twice must still get a real attempt before being abandoned.
3. Print discovery table:

   | # | groupId | Feature | Scenario | Fix approach | Prior attempts |
   |---|---------|---------|----------|--------------|----------------|

4. Print `Found <N> candidates. Confirm to proceed? [y/N]` and wait for explicit user confirmation. Exit cleanly on N.
5. Create working branch:
   ```bash
   git branch --list "imaintenance/<YYYY-MM-DD>"
   ```
   - **Branch does not exist:** `git checkout -b imaintenance/<YYYY-MM-DD>`.
   - **Branch already exists** (same-day re-run or resume after failure): `git checkout imaintenance/<YYYY-MM-DD>` and continue — do not create a new branch. Log: `Resuming existing branch imaintenance/<YYYY-MM-DD>`.

---

## Phase 2 — Fix preparation

For each candidate:

1. Read `../kavach-knowledge/fix-patterns/_default.md` (cross-feature patterns — read once here, applies to all candidates).
2. Read the feature-specific fix-pattern file for this candidate: derive from the feature slug (e.g. `Life_CampaignDashboard` → `../kavach-knowledge/fix-patterns/life-campaign-dashboard.md`) and read it if it exists. Empty or missing files are treated as having no known patterns. The full list of available pattern files is in the kavach-knowledge skill's Reference files section.
3. Read the receipt's `recommendedAction` and `evidence`. There is no structured `fixSuggestion` field on a kavach receipt — match the broken locator/exception described there against the fix-pattern file(s) just read (the same match-against-pattern-file approach isolation mode uses, Phase 0.5.2) to derive `targetFile`, `targetLine` (approximate), and the concrete patch. If no pattern-file entry matches closely enough to derive a concrete patch, record as `needs_investigation` and skip — do not guess at a fix from `recommendedAction` text alone.
4. Open `targetFile`. Confirm the broken locator or code pattern is still present at or near `targetLine`. If the file has changed and the pattern is gone, record as `source_drift` and skip — do not attempt the fix.
   **Note for `infrastructure_inconclusive` retry:** if a prior run left `targetFile` in its edited state (patch applied but not committed), the broken pattern may already be absent. In that case the patch is already applied — skip Phase 3.1 and proceed directly to Phase 3.2 spotless check with the file as-is. Do not re-apply the patch to a file that already has it.
5. **Derive Maven coordinates** from `targetFile`'s path and the feature file:
   - **`<module>`** — strip the repo root prefix and take the first directory segment that contains a `pom.xml`. For example, `automation-tests/src/test/java/com/...` → module is `automation-tests`. Confirm with `ls <module>/pom.xml`.
   - **`<featureTag>`** — the `@Tag` annotation on the top of the feature file (e.g. `@LifeCampaignDashboard`). Read the feature file header to find it.
   - **`<scenarioTag>`** — the `@Tag` annotation on the specific scenario line (e.g. `@TC_12345`). Read the scenario block. If no scenario-level tag is present, use `<featureTag>` and add `-Dcucumber.filter.name="<scenario name>"` to scope the run.
6. Write down the concrete patch (the exact edit to make), the module path, and the tags before Phase 3 applies it.

---

## Phase 3 — Apply, format-check, and verify

Run this loop per prepared candidate. A failure on one candidate does not stop the others.

### 3.1 Apply patch

Edit `targetFile` to apply the prepared patch. Prefer surgical edits (change only the affected locator or line) over large rewrites.

**Prefer CSS over XPath by default.** This codebase has an ongoing design-system migration to web components (`ds-button`, `ds-toggle`, tab-switch groups, etc.) that render content inside **open shadow roots**. Playwright's CSS engine pierces open shadow DOM automatically; XPath never does. When rewriting a broken locator, reach for a CSS selector (`page.locator("css...")`, `.filter(new Locator.FilterOptions().setHasText(...))`, `getByRole(...)`) first, and only fall back to XPath when there's a specific reason CSS can't express the match (e.g. text-node-only predicates XPath handles natively). This isn't just style — an XPath rewrite of a locator that's actually broken because of a shadow-root boundary will silently fail again with a *different* symptom (a plain timeout instead of a clear 0-match), costing another full diagnosis cycle.

**When the fix doesn't resolve on the first live run, diagnose from the actual failure artifacts before guessing again.** A Maven test failure captures three things automatically (via `hooks.Hooks.saveFailureArtifacts`, if the target repo's hooks include it) under `target/failure-artifacts/<scenario-slug>/`:
- `page-source.html` — the full DOM at failure time, including shadow-root content flattened into `<!--[shadow-root]-->...<!--[/shadow-root]-->` markers (so plain-text/regex search finds shadow content that live XPath never could — this is a diagnostic aid, not evidence that XPath would work live)
- `screenshot.png` — a full-page screenshot at the same moment
- `failure-context.json` — scenario name, URL, timestamp

**Read both the DOM and the screenshot before proposing a fix — never DOM-only.** A locator returning 0 matches doesn't always mean "stale locator." It can mean the application is legitimately in a different state than kavach's diagnosis assumed — a conditional dialog, a blocked action, a business-rule message. In one session, a "Delete" confirmation locator that returned 0 matches for `Ok`/`Delete Field` turned out to be timing out because the app was correctly showing a *different* dialog ("Custom Field Can't Be Removed" — the field was in use by a campaign) than the one the original locator's `Ok` branch was written for. The DOM alone confirmed the button existed; only the screenshot made clear *why* it was a different button with different semantics, and that the underlying behavior was correct, not broken.

**A fix confirmed against one live failure snapshot is not confirmed for every code path that locator serves.** If a locator is used across multiple conditional branches (e.g. a delete-confirmation button that appears in a "can be deleted" modal in one case and a "can't be removed" modal in another), a fix that only accounts for the DOM structure seen in the first failure can break the *other* branch on the very next run. Before finalizing, check the page-object method's other call sites and, if the scenario exercises multiple branches, verify against each one live — don't stop at the first green run if the same locator field serves more than one UI state.

**Use `utils.LocatorProbe` for live disambiguation when static analysis alone is inconclusive** (e.g. `.count()`/`.isVisible()` on several candidate selectors, checked non-destructively before the real action runs). It exists specifically because kavach's static, receipt-time diagnosis and iMaintenance's live verification can disagree, and re-running the whole scenario per hypothesis is expensive. Two caveats learned the hard way:
- **Timing matters.** A probe call inserted at the wrong point (e.g. immediately after a click, before an async render settles) can report a false "0 matches" that has nothing to do with the real locator being wrong — the element just hadn't rendered yet. If a probe result and a `page-source.html` snapshot disagree, trust the live failure snapshot's timing (captured at the actual moment Playwright's own action timeout expired) over an ad-hoc probe placed earlier in the flow.
- **Always remove the probe call once the fix is confirmed.** It's a diagnostic scaffold, not part of the shipped fix — leaving it in adds an extra round-trip to every future run of that method and pollutes `target/imaintenance-probe/` with stale files.

**A search/lookup returning nothing isn't always a locator bug.** Helper methods like this codebase's `isElementVisible()` (poll a few times, silently move on if never found — by design, not a bug) exist because some values genuinely aren't found. If a rule-adding or item-selection loop silently skips a value and a downstream step then fails on an unrelated locator (e.g. a Save button that never appears because nothing was actually added upstream), the real root cause can be several steps upstream of the reported failure. Trace the actual execution path (temporary probes at each loop iteration, or reading intermediate log output) rather than assuming the locator named in the final stack trace is the one to fix.

**Do not conclude "stale test data" from a single live search-miss inside a long, multi-step scenario — that conclusion is unverified until checked in isolation.** A search that returns 0 results deep inside a scenario that has already run many prior actions (many tactics, many rule-value searches, growing DOM/state) can fail simply because the app or its search/typeahead has slowed down under accumulated load — not because the value is missing. This looks identical to genuine stale data from a single check: same symptom (0 results, `isElementVisible()`'s poll budget exhausted), different cause. The tell that distinguishes them: **rerun the same scenario (or the same candidate) more than once and watch where it fails.** If the failing value is different each time, and each run's failure point is *later* in the scenario than the previous run's (not just different at random), that's the signature of session-degradation flakiness, not N independent missing values — a real "AutoSegment747695 doesn't exist" bug would fail on that exact value every time, at the same point, not on a different value further along on the next attempt. Confirm this pattern (at least two runs, failure point strictly advancing) before writing anything off as data; if confirmed, the fix belongs in the search-and-select helper (a longer or adaptive wait/retry budget for later iterations, not a fixed one-size poll), not in the test data or a locator. Only treat a miss as genuine stale data if the *same* value fails at the *same* point on repeated isolated runs (e.g. that one row/tactic run alone, with nothing preceding it) — that isolation test is the actual evidence; a single in-context miss is not.

**Fast way to run that isolation test on a scenario with a large inline DataTable** (a single step whose `DataTable` has many rows processed in one long-running session — as opposed to a Cucumber `Examples:` table, which Cucumber already runs as separate scenario instances): temporarily comment out the DataTable rows that already passed, leaving only the row at and after the one that failed, and re-run. This turns a 5-minute, 8-row run into a run that starts right at the suspect row, so each diagnostic iteration is fast instead of paying the full setup cost every time. Once confident every remaining row passes on its own, **uncomment every row before finalizing or committing** — a `.feature` file left with rows commented out for diagnostic convenience is not a valid end state, and this file-editing is inside iMaintenance's allowed scope (`.feature` files) only for this temporary purpose, not as a permanent test-data reduction.

**Watch for JUnit's `assertEquals(message, expected, actual)` parameter order when reading a failure message.** The `expected:<...>` / `was:<...>` text in the failure output reflects that order — not the semantic meaning of the local variable names in the calling code. A variable named `actualFoo` can be passed as the *expected* argument. Misreading this once cost an entire fix cycle diagnosing the wrong locator in a real session (the "expected" side, from a variable named `actualAdvertiserList`, was actually clean; the real bug was in the variable serving as `actual`). Before proposing a fix based on an assertion failure, check the exact call site's argument order, not just the variable names.

### 3.2 Format check (spotless gate)

```bash
mvn spotless:check -pl <module> -q
```

- **Exit 0:** proceed to 3.3.
- **Non-zero:** run `mvn spotless:apply -pl <module> -q`, then re-check. If the re-check still fails, revert the patch (`git checkout -- <targetFile>`), record `spotless_failed`, and move to the next candidate. **Never commit a file that fails spotless.**

### 3.3 Maven verification — three-run rule

Run the target scenario three times:

```bash
mvn test -Dtest=TestRunner -Dcucumber.filter.tags="@<scenarioTag>" -pl <module> -q
```

After each run inspect the Surefire output for a Cucumber execution summary line (e.g. `1 Scenarios (1 passed)` or `1 Scenarios (1 failed)`). **Exit code alone never decides the outcome.**

Classify each run:

| Observed output | Single-run result |
|---|---|
| Cucumber summary present, scenario passed | `maven-pass` |
| Cucumber summary present, scenario failed | `maven-fail` |
| No Cucumber summary (build died before test execution) | `maven-infra` |

Three-run verdict:

| All three results | Candidate verdict |
|---|---|
| All `maven-pass` | `passed` → go to Phase 3.5 |
| Any `maven-fail` (Cucumber summary seen at least once) | `test_failed` |
| Any `maven-infra`, no `maven-fail` ever | `infrastructure_inconclusive` |
| Mix of `maven-pass` and `maven-infra`, no `maven-fail` | `infrastructure_inconclusive` |

**On `test_failed`:** determine whether the failing step is the *same* step that was broken before or a *new* step further along in the scenario.
- **Same step still failing:** revert patch. Record `test_failed`.
- **New step failing (failure shifted):** the scenario is still not passing end-to-end — this is still `test_failed`. Revert patch. Then:
  1. **Print immediately:** `⚠️ Failure shifted — <groupId>: original step now passes, new failure at: <new step text>`. This is meaningful progress; the user needs to see it.
  2. Pause and show the new failing step in context (step text + any stack excerpt). Ask whether to continue with remaining candidates or stop.
  3. Record `test_failed` in fix-history with `"note": "failure shifted to step: <new step text>"` and `"priorStep": "<original step>"` so kavach re-diagnoses the new step on the next run, not the original one. Do not re-record the original step as an open candidate.

**On `infrastructure_inconclusive`:** do NOT revert the patch. Do NOT commit it. Leave `targetFile` in its edited state locally. Record outcome with `infrastructureReason` (last Maven error line). The candidate stays eligible for retry on the next invocation.

**On `passed`:** proceed to Phase 3.5.

---

## Phase 3.5 — Cascade regression check

Runs immediately after a `passed` result, before committing. Do not run this for `test_failed` or `infrastructure_inconclusive` candidates.

Run the full feature file containing the repaired scenario:

```bash
mvn test -Dtest=TestRunner -Dcucumber.filter.tags="@<featureTag>" -pl <module> -q
```

Inspect the output. If any scenario that was **not** in the current candidate list now appears as failed, a cascade regression has occurred.

**No cascade detected:** proceed to Phase 4 commit.

**Cascade detected:**
1. Print: `Cascade detected — <N> new failure(s) in <featureFile>: <scenarioNames>`.
2. Offer to attempt a cascade fix (depth cap: 1 — one fix attempt, no further cascade check after it).
3. Wait for user approval. On approval, apply fix and re-run the feature. On rejection or no response: record original fix as `applied_with_regression` and proceed to Phase 4 (the original fix stands; the user has been shown the regression).

---

## Phase 4 — Commit

For each candidate with verdict `passed` or `applied_with_regression`:

1. Re-run `mvn spotless:check -pl <module> -q` to confirm the file is still clean after any cascade-fix edits.
2. `git add <targetFile>`
3. `git commit -m "fix(<featureSlug>): <fixDescription> [imaintenance/<run-date>]"`

**One commit per fix.** Do not batch multiple fixes into one commit — this keeps per-fix revert (`git revert <sha>`) practical for a reviewer who wants to drop one fix from the batch.

Candidates with verdict `test_failed`, `spotless_failed`, `source_drift`, or `infrastructure_inconclusive` are not committed.

---

## Phase 5 — Push branch and open PR

```bash
git push -u origin imaintenance/<run-date>
```

If `git push` exits non-zero with "rejected" or "non-fast-forward":
```
PUSH_REJECTED: Remote branch imaintenance/<run-date> already exists with diverged history.
Do NOT force-push. Resolve manually:
  git pull --rebase origin imaintenance/<run-date>
Then re-run Phase 5.
```
Stop immediately. Do not attempt force-push under any circumstances.

Build the PR body and write it to `/tmp/imaintenance-pr-body.md` before opening the PR. Use the Write tool or a heredoc:

```bash
cat > /tmp/imaintenance-pr-body.md << 'EOF'
<rendered PR body — all sections below, in order>
EOF
```

Then open the PR:

```bash
gh pr create \
  --title "fix: apply <N> script fixes [<run-date>]" \
  --body "$(cat /tmp/imaintenance-pr-body.md)"
```

Build the PR body with sections in this order. A reviewer must not be able to merge without seeing cascade failures.

1. **⚠️ Cascade failures — manual review required** *(if any `applied_with_regression` entries)* — scenario, feature file, the new failure introduced, and the commit sha of the fix that caused it.
2. **✅ Applied (clean)** — fixes that passed verification with no cascade.
3. **❌ Still failing** — `test_failed` outcomes. Include the failing step and note if failure shifted.
4. **🔧 Infrastructure inconclusive** — `infrastructure_inconclusive` outcomes with recorded reason. These are not failures; they are retryable.
5. **↕️ Source drift** — files that changed since kavach's diagnosis; need re-diagnosis.
6. **🚫 Format gate** — `spotless_failed` outcomes.
7. **⏭️ Skipped** — candidates with ≥ 2 prior failed attempts, not retried this run.

Each row: `scenario | file:line | fix description | outcome`.

If there are no passing fixes (all candidates failed or were skipped), do not push and do not open a PR. Print: `No passing fixes — branch not pushed.`

---

## Phase 6 — Fix-history update and summary

### fix-history.json — one entry per candidate

This is the same file and array kavach appends to (see the verdict-reporting skill's schema) — the top-level fields below match kavach's field names exactly so the shared ≥2-failed-attempts skip logic in Phase 1 can read entries from either writer. Everything specific to fix-application (as opposed to diagnosis) lives under `imaintenanceDetail`.

```json
{
  "timestamp": "<ISO timestamp>",
  "runDate": "<YYYY-MM-DD>",
  "scenarioName": "<scenarioId>",
  "featureFile": "<path>",
  "verdict": "<machine-format verdict>",
  "groupId": "<groupId>",
  "attempts": "<prior attempts + 1>",
  "liveVerification": null,
  "analysisTier": null,
  "imaintenanceDetail": {
    "targetFile": "<path>",
    "fixApproach": "<the patch derived in Phase 2 step 3, summarised>",
    "commitSha": "<sha or null>",
    "prUrl": "<url or null>",
    "cascadeFailures": [],
    "infrastructureReason": null,
    "priorStep": null,
    "note": null
  }
}
```

Set `liveVerification: null` and `analysisTier: null` for all iMaintenance entries — live-browser replay and tiered diagnosis are kavach's responsibility, not iMaintenance's.

**Verdict values (machine format — never display strings):**

| Value | Meaning |
|---|---|
| `script_issue_fix_applied` | Passed all three Maven runs, no cascade, committed clean |
| `applied_with_regression` | Passed own verification, committed, cascade regression present |
| `test_failed` | Scenario still fails after patch (reverted) |
| `infrastructure_inconclusive` | Maven did not reach test execution; eligible for retry |
| `spotless_failed` | Format check failed; not committed |
| `source_drift` | Target file changed since kavach's diagnosis; not attempted |

### Fix-pattern update

For each `script_issue_fix_applied` entry: append the fix as a **Known good fix** block to the relevant fix-pattern file (same format kavach's Phase 5 uses). This closes the loop — kavach's Phase 2 cache-hit path will recognise the same locator pattern on the next run.

### Summary table

```
iMaintenance run — <run-date>
Mode: <INTERACTIVE|ISOLATION (interactive)>
Branch: imaintenance/<run-date>
PR: <url or "not opened">

| Result                    | Count |
|---------------------------|-------|
| Applied (clean)           |     N |
| Applied (with regression) |     N |
| Still failing             |     N |
| Infra inconclusive        |     N |
| Source drift              |     N |
| Spotless failed           |     N |
| Skipped (prior attempts)  |     N |
| Total candidates          |     N |
```

---

## Rules

- Never apply a fix without completing all three Maven verification passes (3.3).
- Never commit a file that fails `mvn spotless:check`.
- Never write `test_failed` for an `infrastructure_inconclusive` result. The three-run classification table in 3.3 is the authority; apply it.
- Never revert a patch for an `infrastructure_inconclusive` result — leave the file edited locally so the candidate can be retried next invocation.
- Never run Phase 3.5's cascade check without first seeing `passed` from Phase 3.3.
- Never batch multiple fixes into one commit.
- Never call `mcp__playwright__*` tools — all verification is Maven-only.
- Never skip the Phase 0.5.3 match-confirmation gate (isolation) or the Phase 1 discovery-table confirmation (both modes). iMaintenance is always interactive — no environment variable overrides this.
- Never skip reading `fix-history.json` in Phase 1. The skip conditions prevent repeating documented dead-end approaches.
- A fix that caused a cascade regression must appear at the top of the PR body under ⚠️, not buried in the applied list.
- Before re-applying a patch for a candidate with a prior `infrastructure_inconclusive` entry, confirm the broken pattern is still present in `targetFile` (Phase 2 step 3). If the patch was already applied in a prior run (pattern absent), skip Phase 3.1 and proceed directly to Phase 3.2. Never double-apply a patch.
