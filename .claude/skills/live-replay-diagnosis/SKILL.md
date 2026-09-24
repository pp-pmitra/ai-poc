---
name: live-replay-diagnosis
description: >-
  Drives a real, faithful live-browser replay of each failure group the
  failure-triage skill escalated, classifying each as a script issue
  (fix proposed), a confirmed/suspected product bug, or needs
  investigation. The only skill in Kavach's pipeline that touches
  Playwright MCP. Hands off its receipts to the verdict-reporting
  skill; never writes the final report itself, never applies fixes.
---

# Live Replay Diagnosis

<!-- Second stage of the kavach-diagnose agent's pipeline. Consumes escalate-groups.json from the failure-triage skill; hands off combined receipts to the verdict-reporting skill. -->

**TOOL USE: Bash, Read, Write, Grep/Glob, and Playwright MCP browser tools are pre-approved for this skill when it is run with `--permission-mode auto` or `--permission-mode bypassPermissions` and the repo's `allowedTools` are configured. Do not infer that Playwright is unavailable from the absence of an interactive prompt in `claude -p`. Do not ask the user to approve `mcp__playwright__browser_*` tools. Do not write "the browser tool needs permission", "please approve", "while waiting", or any static-only fallback before a real tool rejection. Call the required Playwright MCP tool directly. If Claude Code actually rejects the tool call (permission-model rejection), stop immediately and print exactly: `PLAYWRIGHT_TOOL_REJECTED: <tool name>: <error text>`. If instead the call fails with a connection-refused or "Target closed" error (dead CDP endpoint), follow the `CDP_ENDPOINT_DEAD` procedure in `_default.md`'s Bootstrap / CDP error handling section. Any such block still ends with a verdict report — see **Live replay blocked** below. Otherwise continue the live replay.**

**FIRST LIVE-REPLAY ACTION:** as soon as the failure-triage skill hands off `escalate-groups.json`, immediately call `playwright:browser_navigate` to the Background-selected app URL for the first escalated group. Do not write a static-only diagnosis for an escalated group before attempting this tool call — a static-only diagnosis is not a valid final answer for a group failure-triage could not resolve. (Groups failure-triage *did* resolve — mechanically intermittent, fix-pattern cache hit, deterministic, or text-only-LLM-resolved — correctly get no browser session at all; that is the intended cost saving, not a shortcut to flag.)

**Never decrypt credentials or submit the login form from inside this skill, in any mode.** Claude's own tool calls performing AES decryption + `#UserName`/`#Password` fill + submit get denied by Claude Code's `auto`-mode safety classifier — this has been observed to deny even a previously-working recipe, and reframing the call (different tool, different wrapping, loading a saved storage-state file instead) is working around the control rather than within it; don't attempt any of that.

Login is instead handled outside Claude's tool loop, by ordinary Playwright Java tests — not agent actions, so the classifier has no say over them. **Two bootstraps exist; pick the one matching what's actually being replayed, don't default to Life for everything:**

- **`tools.LifeAuthStateBootstrapTest`** (`src/test/java/tools/LifeAuthStateBootstrapTest.java`) — for ordinary Life scenarios. Logs in via `Navigation.enterUsername/enterPassword/clickLogin` (credentials decrypted via the existing `EncryptionDecryption`/`ConfigReader` utilities, the same code path `LifeSteps.set_environment()` uses) then holds the browser open on CDP port `9223`.
- **`tools.StudioAuthStateBootstrapTest`** (`src/test/java/tools/StudioAuthStateBootstrapTest.java`, run with `-Dauth.holdForCdp=true`) — for Studio-flavored features. Plain Life login lands on the default `buyer2@ppcom` account and never reaches Studio at all, so anything under a `Studio_*.feature` (or whose Background switches to the `"PP engineering test"` account) needs this one instead: it logs in, selects the buying platform, switches to `PP engineering test`, and navigates all the way into Studio before holding open on CDP port `9224`.

Before starting login, group this run's failures by which app they actually exercise (feature filename / Background target), not just by literal environment name — then start the matching bootstrap(s):

- **Unattended (`claude -p`, Jenkins):** for a Life-only batch, run `mvn test -Dtest=LifeAuthStateBootstrapTest -Dauth.environment=<Demo|Pre-release> -Dauth.userType=<Internal|External> -Dauth.holdSeconds=<budget>` before invoking this skill, then run Claude with `--mcp-config .claude/mcp-ci-life.json --strict-mcp-config` so Playwright MCP attaches to `http://localhost:9223`. For a Studio-only batch, run `mvn test -Dtest=StudioAuthStateBootstrapTest -Dauth.holdForCdp=true -Dauth.holdSeconds=<budget>` instead, then run Claude with `--mcp-config .claude/mcp-ci-studio.json --strict-mcp-config` so Playwright MCP attaches to `http://localhost:9224`. If the batch mixes both, use `.claude/run-mixed-batch.sh` to enforce the ordering — it starts the Life bootstrap, waits for CDP port 9223, runs kavach for Life failures, touches `.claude/auth/life-cdp-done` to release that bootstrap, then repeats the same sequence for Studio on port 9224. A single Playwright MCP server can only attach to one `--cdp-endpoint` at a time, so never start both bootstraps simultaneously. Running the script directly:
  ```
  .claude/run-mixed-batch.sh \\
    --environment <Demo|Pre-release> \\
    --user-type <Internal|External> \\
    --hold-seconds <budget>
  ```
  Pass `--life-only` or `--studio-only` to run one phase when the other has no failures. Skip the manual bootstrap steps below entirely in this mode — the page is already logged in when this skill starts; go straight to account switching (3.2 step 3).
- **Interactive:** if the page isn't already authenticated when live replay starts, ask the user to type their credentials into the open browser pane themselves, then continue navigation from their session. Do not attempt the decrypt/fill/submit yourself even though it's the pattern this doc used to document.

Diagnose failed scenarios via faithful live replay against the Demo app, classify each with one of the fixed Verdicts in 3.3 (script issue → propose a fix; product bug → flag only, no code change; not reproduced / needs investigation → no fix needed), and present findings + suggested fixes for the user to apply. Do not auto-apply fixes.


## Phase 0: Confirm CDP Bootstrap is Running

**Do this before starting any live replay.** Live replay (Phase 3) needs a held browser on a CDP port. Starting without a live bootstrap means this skill's first `browser_navigate` hits a dead endpoint and the whole run stops after the failure-triage skill's cost was already spent for nothing.

**Unattended (`claude -p`, Jenkins):**

- **Mixed Life+Studio batch** — use `.claude/run-mixed-batch.sh`; it handles bootstrap start, ordering, and done-marker lifecycle automatically. Skip the manual steps below.
- **Life-only batch** — start the bootstrap, confirm the port, then invoke:
  ```
  mvn test -Dtest=LifeAuthStateBootstrapTest \
    -Dauth.environment=<Demo|Pre-release> \
    -Dauth.userType=<Internal|External> \
    -Dauth.holdSeconds=<budget> &
  nc -z localhost 9223   # wait until this returns 0
  claude -p "/analyze-failure" \
    --mcp-config .claude/mcp-ci-life.json \
    --strict-mcp-config \
    --permission-mode auto \
    --allowedTools "Bash(*),Read,Write(*),Edit(*),Grep,Glob,mcp__playwright__browser_navigate,mcp__playwright__browser_tabs,mcp__playwright__browser_snapshot,mcp__playwright__browser_evaluate,mcp__playwright__browser_click,mcp__playwright__browser_fill_form,mcp__playwright__browser_type,mcp__playwright__browser_press_key,mcp__playwright__browser_wait_for,mcp__playwright__browser_run_code_unsafe,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_close,mcp__playwright__browser_select_option,mcp__playwright__browser_hover,mcp__playwright__browser_find" \
    --verbose \
    --output-format text
  touch .claude/auth/life-cdp-done
  ```
- **Studio-only batch** — start the bootstrap, confirm the port, then invoke:
  ```
  mvn test -Dtest=StudioAuthStateBootstrapTest \
    -Dauth.holdForCdp=true \
    -Dauth.holdSeconds=<budget> &
  nc -z localhost 9224   # wait until this returns 0
  claude -p "/analyze-failure" \
    --mcp-config .claude/mcp-ci-studio.json \
    --strict-mcp-config \
    --permission-mode auto \
    --allowedTools "Bash(*),Read,Write(*),Edit(*),Grep,Glob,mcp__playwright__browser_navigate,mcp__playwright__browser_tabs,mcp__playwright__browser_snapshot,mcp__playwright__browser_evaluate,mcp__playwright__browser_click,mcp__playwright__browser_fill_form,mcp__playwright__browser_type,mcp__playwright__browser_press_key,mcp__playwright__browser_wait_for,mcp__playwright__browser_run_code_unsafe,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_close,mcp__playwright__browser_select_option,mcp__playwright__browser_hover,mcp__playwright__browser_find" \
    --verbose \
    --output-format text
  touch .claude/auth/studio-cdp-done
  ```

**Interactive:** the bootstrap is not needed. Ask the user to log into the browser pane themselves, then continue from Phase 3 below.

If this command starts and the bootstrap is not running, Phase 3's first `browser_navigate` will fail with a `CDP_ENDPOINT_DEAD` error — the run still finishes with a verdict report that lists the blocked groups and the reason (see **Live replay blocked** in Phase 3); then start the correct bootstrap and re-run to diagnose them.

## Phase 3: Per-Group Worker Diagnosis Loop

**Per-group context load (before building the packet):** Read the feature-specific fix-pattern file for this group's `featureFile` now — derive the filename (e.g. `Life_PMP.feature` → `kavach-data/fix-patterns/life-pmp.md`) and read it if it exists. This is the lazy-load step deferred from the failure-triage skill. Empty or absent files are treated as having no known patterns.

Build compact live-replay worker packets **only for the groups the failure-triage skill escalated**:

```
python3 .claude/skills/kavach-diagnose/scripts/replay_workers.py --write-runner --only-groups kavach-data/history/triage-results/<triage-timestamp>/escalate-groups.json
```

If the script exits non-zero or `manifest.json` is not written under `history/replay-packets/<replay-timestamp>/`, stop and print `PHASE_SCRIPT_FAILED: replay_workers.py: <stderr>`. Do not proceed to worker execution without a valid manifest.

This writes `kavach-data/history/replay-packets/<replay-timestamp>/manifest.json` (a **freshly generated timestamp, distinct from `<triage-timestamp>`** — don't reuse the failure-triage skill's triage timestamp here), one `group-*.json` packet, one `group-*.prompt.md` prompt, and `run-workers.sh` — same mechanical packet builder as before (redacted, page text capped, small code-context windows), just scoped to fewer groups. If running unattended, pass the same Claude/MCP flags via repeated `--claude-arg`, for example:
```
python3 .claude/skills/kavach-diagnose/scripts/replay_workers.py --write-runner --only-groups kavach-data/history/triage-results/<triage-timestamp>/escalate-groups.json \
  --claude-arg --mcp-config --claude-arg .claude/mcp-ci-life.json \
  --claude-arg --strict-mcp-config \
  --claude-arg --permission-mode --claude-arg auto \
  --claude-arg --allowedTools --claude-arg "Bash(*),Read,Write(*),Grep,Glob,mcp__playwright__browser_navigate,mcp__playwright__browser_tabs,mcp__playwright__browser_snapshot,mcp__playwright__browser_evaluate,mcp__playwright__browser_click,mcp__playwright__browser_fill_form,mcp__playwright__browser_type,mcp__playwright__browser_press_key,mcp__playwright__browser_wait_for,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_close,mcp__playwright__browser_run_code_unsafe,mcp__playwright__browser_select_option,mcp__playwright__browser_hover,mcp__playwright__browser_find"
```
Run the generated `run-workers.sh` serially by default. Use at most two browser workers at once if you intentionally parallelize later; shared Demo account state makes high concurrency noisy.

After workers finish, produce ONE consolidated, completeness-checked receipt set spanning both the failure-triage skill's triage and this skill's live replay — pass both manifests explicitly so there's no ambiguity about which timestamp goes where:
```
python3 .claude/skills/kavach-diagnose/scripts/validate_replay_receipts.py kavach-data/history/replay-packets/<replay-timestamp>/manifest.json \
  --triage-manifest kavach-data/history/triage-results/<triage-timestamp>/manifest.json
```
If the script exits non-zero or `combined-receipts.json` is not written, stop and print `PHASE_SCRIPT_FAILED: validate_replay_receipts.py: <stderr>`. The verdict-reporting skill cannot proceed without this file.

This writes `kavach-data/history/triage-results/<triage-timestamp>/combined-receipts.json` and mechanically:
- downgrades any `confirmed_product_bug` receipt that did not prove every product-bug gate condition (from either phase — the failure-triage skill's receipts are re-validated here too, not trusted blindly, even though they already can't carry a forbidden verdict);
- assigns `analysisTier: "tier2_live_replay"` to every Phase-3-sourced row automatically (the failure-triage skill's rows already carry their own `analysisTier`);
- accounts for every group the failure-triage skill's manifest recorded (resolved or escalated) in exactly one row, with a `needs_investigation` fallback for any escalated group whose Phase 3 receipt is missing — this IS the verdict-reporting skill's row-completeness check, already done by the time you read `combined-receipts.json`, not something to redo by hand.

The parent session is now an orchestrator. Do not carry the full failure list into every diagnosis. For each escalated `groupId`, use the corresponding `group-*.prompt.md` as the worker's complete context and require a JSON receipt. If diagnosing manually in the current session instead of using `run-workers.sh`, still follow the packet boundary: open one packet, diagnose it, write one receipt, then clear mental context before the next packet.

Each worker receipt must be JSON only and must include:

```json
{
  "groupId": "...",
  "representativeScenario": "...",
  "affectedScenarios": ["..."],
  "distinctIssueCount": 1,
  "verdict": "script_issue_fix_proposed | confirmed_product_bug | suspected_product_bug | not_reproduced_intermittent | not_reproduced_passed_live | needs_investigation",
  "confidence": "high | medium | low",
  "backgroundMatched": true,
  "liveReplayPerformed": true,
  "alternateValidPathFound": false,
  "productBugGate": {
    "userLevelBehaviorReproduced": true,
    "targetAffordanceMissingOrBroken": true,
    "domStructureChecked": true,
    "staleLocatorRuledOut": true,
    "testDataOrEnvironmentRuledOut": true
  },
  "productBugArtifacts": {
    "domStructureChecked": "the actual outerHTML/DOM excerpt observed, e.g. via evaluate(el => el.outerHTML) — not a boolean restated as text",
    "staleLocatorRuledOut": "the actual count()/evaluate() output from the corrected-locator attempt you tried"
  },
  "evidence": ["2-5 concrete observed facts"],
  "recommendedAction": "short action"
}
```

If a receipt claims `confirmed_product_bug` but any `productBugGate` field is false/missing, or if `liveReplayPerformed`/`backgroundMatched` is false, it must downgrade to `suspected_product_bug` or `needs_investigation`. Historical findings and old verdicts are hints only; they are never evidence for a confirmed product bug. **This check is mechanical, not something the parent needs to redo by hand** — `validate_replay_receipts.py` (invoked at the end of this skill) applies it to every receipt automatically; write the worker receipt correctly the first time so it doesn't get downgraded later.

**`productBugArtifacts` is required for `confirmed_product_bug` specifically**, also checked mechanically by the same validator — a boolean gate claim is self-reported by the same session that did the replay, so nothing independently proves `domStructureChecked`/`staleLocatorRuledOut` were actually done rather than rushed. Both fields must be the *real* observed text (the literal DOM excerpt / count output), at least ~20 characters, not a placeholder like `"true"`/`"verified"`/`"checked"` — a receipt missing either, or with a placeholder-shaped value, gets mechanically downgraded to `suspected_product_bug` even if every `productBugGate` boolean was `true`. This is on top of, not instead of, the gate check above.

### Live replay blocked (any reason) — still write the verdict report

Live replay can be blocked for reasons outside the script under test: dead/unreachable CDP endpoint (`CDP_ENDPOINT_DEAD`), the held browser losing network (`ERR_NETWORK_CHANGED`, blank page, navigation timeout), a Playwright tool rejection, an expired session. When that happens, **do not end the run with only chat output** — the run must still finish with a `replay-verdict-*.md` file. Never retry the same failing action in a loop (a blocked session is not fixed by more retries).

1. Stop Phase 3 work at the first unrecoverable block. Write a one-line reason string exactly as observed, e.g. `CDP_ENDPOINT_DEAD: 9223 — connection refused` or `ERR_NETWORK_CHANGED on held Life browser (CDP 9223); navigation timed out after 60s`.
2. Groups already diagnosed live before the block keep their real receipts. Every remaining escalated group gets no receipt (or, if you write one, `verdict: "needs_investigation"`, `liveReplayPerformed: false` and a `blockedReason` field with the same string).
3. Run the validator with the reason so it lands on every receipt-less escalated group:
   ```
   python3 .claude/skills/kavach-diagnose/scripts/validate_replay_receipts.py <replay-manifest> \
     --triage-manifest <triage-manifest> --blocked-reason "<reason>"
   ```
   (If Phase 3 never produced a replay manifest, omit the positional manifest argument.) Blocked groups come out as `needs_investigation` with `blockedReason` set; failure-triage-resolved groups keep their verdicts, so their fix proposals are still reported.
4. Hand off to the verdict-reporting skill, which writes the report normally, with the **Blocked live replay** banner and Detail text described there. Print the block marker (`CDP_ENDPOINT_DEAD: ...` etc.) in chat as well.

For each representative failure group, in order:

### 3.1 Read code context (not the live app yet)

- Use `graphify query "<page-object class>.<method>"` / `graphify explain` first if `graphify-out/graph.json` exists — cheaper than opening full files.
- Read the step definition at `errorLocation.stepDef` and the page-object method at `errorLocation.pageObject`. Follow any loops/helpers/branches the step definition calls — don't assume a failure is data-related without reading what the code actually does with the input.
- Check for **sibling occurrences** of the same locator/class/idiom elsewhere in the same page object (or same graphify community) — if this is a locator-style issue, other methods likely share it; find them now rather than one at a time across repeated live checks.
- Check for **same-named or same-purpose methods on other page objects** (e.g. `isXAvailable()`/`verifyX()` defined once per page object for the same UI concept). `graphify query "<methodName>"` across the whole repo, not just the failing page object. If a sibling already implements the identical check with a different (working) locator, that's a strong signal the failing one is simply out of date — compare them structurally before writing a new locator from scratch.
- Consult the loaded fix pattern file(s) for a known-good approach to this exact symptom.

### 3.2 Faithful live replay via Playwright MCP — always, no shortcuts

Never jump to `pageUrl` (session-specific IDs won't resolve) and never substitute a shortcut like inspecting an unrelated pre-existing record instead of the scenario's own steps — that isn't a faithful replay and produces unreliable verdicts.

**Never type a fixed literal name into a field that must be unique** (workspace name, campaign name, etc.) during live replay. A repeat run typing the same literal string collides with the leftover record from the previous run's own replay and blocks on an "already exists" validation error. Get a unique suffix first (e.g. `date +%s` via Bash) and append it, matching the app's own naming convention (e.g. `Explorer_<timestamp>`), the same way the actual step-definition code does.

**Trust the reference until it's proven wrong.** The page-object locator you read in step 3.1 is the reference — drive every step by executing that locator/action directly, not by taking a `browser_snapshot`/DOM dump first to "see where you are" or confirm a step you haven't reached is fine. MCP's cost is paid in re-reading page state; only pay it when an action's outcome is actually in question:
- Steps *before* the failing step exist only to reach the right app state — execute them via their real locators and check nothing but pass/fail (did the action throw, did navigation/URL change as expected). Don't snapshot or inspect DOM on steps that aren't the one under diagnosis.
- Reserve `browser_snapshot`/full DOM dumps for the failing step itself, and only after a targeted `count()`/`.evaluate()` check on the existing locator already looks wrong. If the targeted check on the reference locator passes, you're done — no snapshot needed.
- If a pre-failure step's own action unexpectedly throws or the page doesn't reach the expected state, that step becomes the thing under diagnosis — inspect it with the same targeted-first approach.

1. Read the feature file: the `Background:` steps, the failing scenario's steps up to and past `failedStep`, and any `Examples:` values.
2. Check the `Given "..." environment as a "..."` step in the `Background:` to know which URL/account-type this feature expects (`demoURL`/`preReleaseURL`, internal/external) — it should match whatever `-Dauth.environment`/`-Dauth.userType` the bootstrap was launched with (unattended) or whatever the user logged into (interactive). If it doesn't match, that's a setup problem to raise, not something to fix by logging in again yourself. See the login note above this Phase 3 section — this command never performs the login itself.
3. Click the buyer portal link, then switch account using the selectors in `_default.md`'s **Navigation utilities** section. Login alone lands on the default `buyer2@ppcom` account with different data — skipping this makes every later observation unreliable. **Don't assume the target account is whatever literal string appears in the Background's `"..." application is logged in successfully with Account "..."` step** — some features switch to a different account than that literal text by the time the scenario's own steps run (e.g. `Studio_Explorer_Workspace.feature` actually operates under "PP engineering test", not the "automation@pulsepoint" named in its Background login step). When in doubt, ask the user which account the feature actually runs under rather than trusting the Background text at face value.
4. Replay `Background:` steps, then scenario steps in order up to the failing step, using the actual step-definition/page-object code (not the `pageText`/`actionTimeline` from the old trace data — replay live actions, not a transcript). Execute each via its existing locator; don't snapshot to verify these steps unless one of them fails.
5. Execute the failing step's action and observe directly: does the locator resolve (`count`)? Is it visible? Does the click register (does subsequent app state actually change)? Use targeted `page.locator(...).count()`/`.evaluate()` checks first — reach for a full `browser_snapshot` only if those targeted checks don't explain the failure.

### 3.3 Classify

Assign exactly one **Verdict** from this fixed list — no free-text variants, no mixing classification with outcome phrasing:

| Verdict | Meaning |
|---|---|
| `Script Issue — Fix Proposed` | Corrected locator/action resolves live; fix not yet applied |
| `Script Issue — Fix Applied` | Same, and the user approved applying it (Phase 3.4) before the report was written |
| `Product Bug — Confirmed` | Structurally-sound locator/action still fails after full, independent live verification (DOM-structure check done, not just count/visibility) |
| `Product Bug — Suspected` | Same failure pattern, but verification was partial — a single attempt, or a spot-check against a group representative rather than an independent replay |
| `Not Reproduced — Intermittent` | `stepReliability.likelyIntermittent` is true — skip live replay entirely |
| `Not Reproduced — Passed Live Replay` | Current, un-fixed code passes live as-is |
| `Needs Investigation` | 2 live attempts exhausted, evidence inconclusive either way (rare — only Phase 3 produces this) |

Worker receipts use lower-case machine values. Map them mechanically when writing the report:

| Receipt verdict | Report Verdict |
|---|---|
| `script_issue_fix_proposed` | `Script Issue — Fix Proposed` |
| `confirmed_product_bug` | `Product Bug — Confirmed` |
| `suspected_product_bug` | `Product Bug — Suspected` |
| `not_reproduced_intermittent` | `Not Reproduced — Intermittent` |
| `not_reproduced_passed_live` | `Not Reproduced — Passed Live Replay` |
| `needs_investigation` | `Needs Investigation` |

Alongside the Verdict, assign a **Confidence** — `High` / `Medium` / `Low`. This rubric describes Phase 3's live-replay confidence specifically:
- **High** — a full, independent live replay; a targeted `count()`/`.evaluate()` check or full DOM dump gives a deterministic answer
- **Medium** — live-verified but via spot-check against a group representative, or only partial downstream-state confirmation
- **Low** — inconclusive after 2 attempts, or evidence is circumstantial

**The failure-triage skill's receipts carry their own `confidence` value under a different evidentiary standard** — set by mechanical rules or a text-only LLM call, never a live replay (e.g. `not_reproduced_intermittent` from same-run reliability stats is always `High` by convention, not because a browser confirmed anything). Pass these values straight through into the report as-is; don't re-derive them against the live-replay definitions above, and don't treat a failure-triage `High` as equivalent evidentiary strength to a live-replay `High` — the `analysisTier` column/field is what actually distinguishes them.

Classification rules:

- **If `stepReliability.likelyIntermittent` is true, skip live replay entirely** → `Not Reproduced — Intermittent`, Confidence `High` (the run itself already proved it elsewhere). Record the passed/failed counts as Evidence (e.g. "not reproduced elsewhere this run — passed 12/13 times") and move to the next failure. Don't spend a browser session confirming what the mechanical signal already settled.
- **If the current, un-fixed code already passes live** (the original failure doesn't reproduce) → `Not Reproduced — Passed Live Replay`, Confidence `High`. Note this, don't force a fix; if there's an obvious hardening opportunity (see `_default.md`'s dismiss/wait patterns), mention it in the Conclusion as optional.
- **If it fails, and you can construct a corrected locator/action that resolves live** (`count >= 1`, `visible === true`, click produces the expected downstream state) → `Script Issue — Fix Proposed`. Test the corrected approach live yourself (via `browser_run_code_unsafe`) up to 2 live attempts (try approach 1, if it doesn't fully clear the step try a second approach) — do NOT edit the actual source file yet, see Phase 3.4. Confidence `High` if it cleared on attempt 1 with a clean downstream-state check, `Medium` if it took 2 attempts or the downstream check was partial.
- **If after 2 live attempts the step still fails with a structurally sound locator/action** (element genuinely absent from the DOM, click has no effect on app state, expected data isn't there) → `Product Bug — Confirmed` when verification was a full independent replay including a DOM-structure check, otherwise `Product Bug — Suspected`. Do not propose a code change. Record exactly what you observed as Evidence: e.g. "clicked X, DOM never changed / stayed on page Y", "waited for `//label[...]`, confirmed 0 matches in full DOM dump, feature panel never renders", "app returned [A, B] where test expects C — C absent from the list, not a near-variant".
- **If evidence remains genuinely inconclusive after the 2-attempt cap** (neither a clean pass nor a structurally-confirmed absence) → `Needs Investigation`, Confidence `Low`.
- **Before assigning `Product Bug — Confirmed`/`Suspected`, inspect the actual target element's DOM structure** (`.evaluate(el => el.outerHTML)` on whatever *did* match nearby, or on the element found via a broader/manual search) — don't infer soundness from `count()`/pagination/rendering checks alone. A locator can look reasonable and still fail structurally (e.g. `contains(text(),...)` matching only direct text nodes when the real text sits in a nested child element — see `_default.md`'s "label text moved into nested child span" pattern). This check is one `evaluate()` call; do it before letting reproduction cost push a failure to `Needs Investigation`. If a full fresh reproduction is genuinely the only way to get certainty, do it once, completely — "flagged for review, not fully reproduced" is not a valid resting state when a cheap DOM check was never attempted. **Keep the literal output of this `evaluate()` call and the corrected-locator `count()` output** — for `Product Bug — Confirmed` these go verbatim into the receipt's `productBugArtifacts`, not just into prose.

### 3.4 Present, don't apply

Once diagnosis is complete for a failure classified `Script Issue — Fix Proposed`: show the user the exact proposed change (file, line, before → after) and your live-verification evidence (the `count`/`visible` check output). Wait for the user's go-ahead before editing `src/main/java/pages/...`, `src/test/java/stepdefinitions/...`, or `.feature` files. Only edit those three categories of file — never application source code.

If the user approves multiple fixes at once, apply them together. If a fix is approved and applied (by kavach-imaintain) before the verdict-reporting skill's report is written, flip that scenario's Verdict to `Script Issue — Fix Applied` for the report. The receipt remains `script_issue_fix_proposed` — do not update it after a fix is applied; The verdict-reporting skill's report-writing step is what upgrades the display verdict.

### 3.5 Optional final confirmation via Maven

Live replay is the primary diagnostic and is normally sufficient (a corrected locator that resolves live with `count >= 1`/`visible === true` won't hit the timeout that caused the original failure). Run `mvn test -Dtest=TestRunner -Dcucumber.filter.name="<exact scenarioName>"` afterward only if the user wants an additional automated-run confirmation — one scenario at a time, never the full suite. Skip if Maven is unavailable.

## Rules (this skill)

- **Never auto-apply.** Present every proposed fix (diagnosis + before/after + live-verification evidence) and wait for approval before editing files — that approval and the actual edit both happen in kavach-imaintain, never here.
- **Live replay only, always faithful.** Background → every scenario step in order → failing step. No `pageUrl` shortcuts, no reusing an unrelated existing record as a stand-in for the scenario's own setup steps.
- **Reference-first, snapshot-last.** Drive every step through its existing page-object locator; don't `browser_snapshot`/DOM-dump a step just to see where you are. Only fall back to a snapshot for the failing step, and only once a targeted `count()`/`.evaluate()` check on the existing locator already looks wrong.
- **2-attempt cap on live diagnosis** — clears within 2 → `Script Issue — Fix Proposed`; still fails with a sound locator → `Product Bug — Confirmed`/`Suspected`; genuinely inconclusive → `Needs Investigation`.
- **Only these files may ever be edited, and only after approval, by kavach-imaintain — never by this skill directly:** `.feature` files, `src/test/java/stepdefinitions/`, `src/main/java/pages/`.
