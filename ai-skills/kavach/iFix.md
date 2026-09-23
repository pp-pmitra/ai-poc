# Analyze Failures

<!-- kavach canonical instruction body. Entry point: .claude/skills/kavach/SKILL.md -->

## Contents
- [Phase 0: Confirm CDP Bootstrap](#phase-0-confirm-cdp-bootstrap-is-running)
- [Phase 1: Gather Failures & Mechanical Signals](#phase-1-gather-failures--mechanical-signals)
- [Phase 2: Load History & Fix Patterns](#phase-2-load-history--fix-patterns)
- [Phase 2.5: Cheap Triage](#phase-25-cheap-triage)
- [Phase 3: Per-Group Worker Diagnosis Loop](#phase-3-per-group-worker-diagnosis-loop)
- [Phase 4: Write Verdict Report](#phase-4-write-verdict-report)
- [Phase 5: Record History & Patterns](#phase-5-record-history--patterns)
- [Phase 6: Close Held Browser](#phase-6-close-held-browser)


**TOOL USE: Bash, Read, Write, Grep/Glob, and Playwright MCP browser tools are pre-approved for this command when it is run with `--permission-mode auto` or `--permission-mode bypassPermissions` and the repo's `allowedTools` are configured. Do not infer that Playwright is unavailable from the absence of an interactive prompt in `claude -p`. Do not ask the user to approve `mcp__playwright__browser_*` tools. Do not write "the browser tool needs permission", "please approve", "while waiting", or any static-only fallback before a real tool rejection. Call the required Playwright MCP tool directly. If Claude Code actually rejects the tool call (permission-model rejection), stop immediately and print exactly: `PLAYWRIGHT_TOOL_REJECTED: <tool name>: <error text>`. If instead the call fails with a connection-refused or "Target closed" error (dead CDP endpoint), follow the `CDP_ENDPOINT_DEAD` procedure in `_default.md`'s Bootstrap / CDP error handling section. Any such block still ends with a verdict report — see **Live replay blocked** in Phase 3. Otherwise continue the live replay.**

**FIRST LIVE-REPLAY ACTION:** after Phase 2.5's cheap triage tier has run and produced `escalate-groups.json`, immediately call `playwright:browser_navigate` to the Background-selected app URL for the first *escalated* group in Phase 3. Do not write a static-only diagnosis for an escalated group before attempting this tool call — a static-only diagnosis is not a valid final answer for a group Phase 2.5 could not resolve. (Groups Phase 2.5 *did* resolve — mechanically intermittent, fix-pattern cache hit, deterministic, or text-only-LLM-resolved — correctly get no browser session at all; that is the intended cost saving, not a shortcut to flag.)

**Never decrypt credentials or submit the login form from inside this command, in any mode.** Claude's own tool calls performing AES decryption + `#UserName`/`#Password` fill + submit get denied by Claude Code's `auto`-mode safety classifier — this has been observed to deny even a previously-working recipe, and reframing the call (different tool, different wrapping, loading a saved storage-state file instead) is working around the control rather than within it; don't attempt any of that.

Login is instead handled outside Claude's tool loop, by ordinary Playwright Java tests — not agent actions, so the classifier has no say over them. **Two bootstraps exist; pick the one matching what's actually being replayed, don't default to Life for everything:**

- **`tools.LifeAuthStateBootstrapTest`** (`src/test/java/tools/LifeAuthStateBootstrapTest.java`) — for ordinary Life scenarios. Logs in via `Navigation.enterUsername/enterPassword/clickLogin` (credentials decrypted via the existing `EncryptionDecryption`/`ConfigReader` utilities, the same code path `LifeSteps.set_environment()` uses) then holds the browser open on CDP port `9223`.
- **`tools.StudioAuthStateBootstrapTest`** (`src/test/java/tools/StudioAuthStateBootstrapTest.java`, run with `-Dauth.holdForCdp=true`) — for Studio-flavored features. Plain Life login lands on the default `buyer2@ppcom` account and never reaches Studio at all, so anything under a `Studio_*.feature` (or whose Background switches to the `"PP engineering test"` account) needs this one instead: it logs in, selects the buying platform, switches to `PP engineering test`, and navigates all the way into Studio before holding open on CDP port `9224`.

Before starting login, group this run's failures by which app they actually exercise (feature filename / Background target), not just by literal environment name — then start the matching bootstrap(s):

- **Unattended (`claude -p`, Jenkins):** for a Life-only batch, run `mvn test -Dtest=LifeAuthStateBootstrapTest -Dauth.environment=<Demo|Pre-release> -Dauth.userType=<Internal|External> -Dauth.holdSeconds=<budget>` before invoking this command, then run Claude with `--mcp-config .claude/mcp-ci-life.json --strict-mcp-config` so Playwright MCP attaches to `http://localhost:9223`. For a Studio-only batch, run `mvn test -Dtest=StudioAuthStateBootstrapTest -Dauth.holdForCdp=true -Dauth.holdSeconds=<budget>` instead, then run Claude with `--mcp-config .claude/mcp-ci-studio.json --strict-mcp-config` so Playwright MCP attaches to `http://localhost:9224`. If the batch mixes both, use `.claude/run-mixed-batch.sh` to enforce the ordering — it starts the Life bootstrap, waits for CDP port 9223, runs kavach for Life failures, touches `.claude/auth/life-cdp-done` to release that bootstrap, then repeats the same sequence for Studio on port 9224. A single Playwright MCP server can only attach to one `--cdp-endpoint` at a time, so never start both bootstraps simultaneously. Running the script directly:
  ```
  .claude/run-mixed-batch.sh \\
    --environment <Demo|Pre-release> \\
    --user-type <Internal|External> \\
    --hold-seconds <budget>
  ```
  Pass `--life-only` or `--studio-only` to run one phase when the other has no failures. Skip step 2 below entirely in this mode — the page is already logged in when this command starts; go straight to account switching (step 3).
- **Interactive:** if the page isn't already authenticated when live replay starts, ask the user to type their credentials into the open browser pane themselves, then continue navigation from their session. Do not attempt the decrypt/fill/submit yourself even though it's the pattern this doc used to document.

Diagnose failed scenarios via faithful live replay against the Demo app, classify each with one of the fixed Verdicts in Phase 3.3 (script issue → propose a fix; product bug → flag only, no code change; not reproduced / needs investigation → no fix needed), and present findings + suggested fixes for the user to apply. Do not auto-apply fixes.

No narrative LLM report-generation step is used here — that step (`failure_analyzer.main`'s old `script-fixes.json`/maintenance-story pipeline) is retired. Diagnosis of anything that reaches Phase 3 still comes entirely from live replay, the actual source of truth. Phase 2.5 (a cheap, no-browser triage tier) DOES make one or a few small text-only LLM calls, but it never drives a browser and is structurally forbidden from confirming a product bug on its own — see that phase for the safety constraints.

This command runs as seven phases, in order. Phase 2.5 is a cheap triage pass that decides which `groupId`s actually need Phase 3's expensive live replay. Phase 3 is receipt-driven: diagnose one escalated `groupId` per fresh worker session, then aggregate receipts. Phases 1, 2, 2.5, 4, 5, and 6 each run once per invocation.

## Reference files

Read in Phase 2 alongside `fix-history.json`. Feature-specific files supplement `_default.md`; they never override it.

- [`fix-patterns/_default.md`](fix-patterns/_default.md) — cross-feature patterns: dismiss/wait hardening, nested-child-span locator guard, navigation utilities
- [`fix-patterns/life-campaign.md`](fix-patterns/life-campaign.md)
- [`fix-patterns/life-campaign-dashboard.md`](fix-patterns/life-campaign-dashboard.md)
- [`fix-patterns/life-create-campaign.md`](fix-patterns/life-create-campaign.md)
- [`fix-patterns/life-create-creative.md`](fix-patterns/life-create-creative.md)
- [`fix-patterns/life-create-pixel.md`](fix-patterns/life-create-pixel.md)
- [`fix-patterns/life-create-report-template.md`](fix-patterns/life-create-report-template.md)
- [`fix-patterns/life-creatives.md`](fix-patterns/life-creatives.md)
- [`fix-patterns/life-export-download.md`](fix-patterns/life-export-download.md)
- [`fix-patterns/life-line-item-creation.md`](fix-patterns/life-line-item-creation.md)
- [`fix-patterns/life-lineitem.md`](fix-patterns/life-lineitem.md)
- [`fix-patterns/life-npilists.md`](fix-patterns/life-npilists.md)
- [`fix-patterns/life-pixels.md`](fix-patterns/life-pixels.md)
- [`fix-patterns/life-pmp.md`](fix-patterns/life-pmp.md)
- [`fix-patterns/life-reporttemplates.md`](fix-patterns/life-reporttemplates.md)
- [`fix-patterns/life-runreport.md`](fix-patterns/life-runreport.md)
- [`fix-patterns/life-schedulereport.md`](fix-patterns/life-schedulereport.md)
- [`fix-patterns/life-tactic.md`](fix-patterns/life-tactic.md)
- [`fix-patterns/life-tactic-creation.md`](fix-patterns/life-tactic-creation.md)
- [`fix-patterns/life-targeting-template-creation.md`](fix-patterns/life-targeting-template-creation.md)
- [`fix-patterns/life-targetings.md`](fix-patterns/life-targetings.md)
- [`fix-patterns/life-targetingtemplates.md`](fix-patterns/life-targetingtemplates.md)
- [`fix-patterns/studio-explorerworkspace.md`](fix-patterns/studio-explorerworkspace.md)
- [`fix-patterns/life-curatedmarket.md`](fix-patterns/life-curatedmarket.md)

New feature files get their own pattern file bootstrapped by Phase 5 (Phase 2's derivation rule handles discovery).

## Phase 0: Confirm CDP Bootstrap is Running

**Do this before Phase 1.** Live replay (Phase 3) needs a held browser on a CDP port. Starting phases without a live bootstrap means Phase 3's first `browser_navigate` hits a dead endpoint and the whole run stops after spending Phase 2.5's triage cost for nothing.

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

**Interactive:** the bootstrap is not needed. Ask the user to log into the browser pane themselves, then continue from Phase 1.

If this command starts and the bootstrap is not running, Phase 3's first `browser_navigate` will fail with a `CDP_ENDPOINT_DEAD` error — the run still finishes with a verdict report that lists the blocked groups and the reason (see **Live replay blocked** in Phase 3); then start the correct bootstrap and re-run to diagnose them.

## Phase 1: Gather Failures & Mechanical Signals

1. If `ai-skills/kavach/failure-analyzer/history/failures-for-replay.json` doesn't exist or is older than the latest `target/cucumber-reports/cucumber.json`, generate it first:
   ```
   cd ai-skills/kavach/failure-analyzer && python3 list_failures.py
   ```
   If the script exits non-zero or `failures-for-replay.json` is not written, stop and print `PHASE_SCRIPT_FAILED: list_failures.py: <stderr>`. Do not attempt to reconstruct the output manually.

   This is a **mechanical-only** extraction (reuses `failure_analyzer.parsers.cucumber_parser` + `trace_parser` — no LLM call, no tokens spent) that parses the Cucumber JSON report and enriches each failure with Playwright trace data (page URL, DOM text, call log, screenshot). If `target/cucumber-reports/cucumber.json` doesn't exist, tell the user to run `mvn test` (or `mvn test -Dtest=FailedTestRunner`) first.
2. Read `failures-for-replay.json`. Each entry has: `scenarioName`, `featureFile`, `failedStep`, `errorMessage`, `errorLocation` (exact `stepDef`/`pageObject`/`utility`/`feature` file+line), `playwrightCallLog`, `pageUrl`, `pageText`, `screenshotFile`, `isBackgroundFailure`, `backgroundReliability`, `stepReliability`, `groupId`. Also read the top-level `groups` array.

3. Use the mechanical (no-LLM) signals already computed before doing any live replay:
   - **`stepReliability`** (set on *every* failure, Background or a regular scenario step alike): `{passed, failed, rate, likelyIntermittent}` — how many times this exact step (normalized) passed vs failed anywhere else in this same run. `likelyIntermittent: true` (rate ≥ 50%) means the same step succeeded elsewhere most of the time — that's obvious same-run evidence of a flaky timing race, not a consistently broken locator or a real bug. **Skip full live-replay diagnosis for these** — note it as "not reproduced elsewhere this run (passed N/M times)" in the verdict table and move on; don't spend a live-replay cycle re-confirming what the run itself already showed. Only fall back to a light spot-check (not a full replay) if the user specifically wants extra confirmation on a `likelyIntermittent` failure.
   - **`backgroundReliability`** (only set when `isBackgroundFailure` is true) is the older, Background-only version of the same idea — `stepReliability` supersedes it for triage purposes since it covers every step, but `backgroundReliability`'s feature-level framing ("how often this feature's Background passes") can still be a useful secondary data point when discussing a Background failure specifically.
   - **`groups`**: failures sharing the same `groupId` (same exception type + normalized step pattern) are likely the same root cause. **This applies only to groups that reach Phase 3** — Phase 2.5 may resolve an entire group (representative and all) with zero live interaction, which is correct and not something to redo. For a group that *does* reach Phase 3: live-replay and fix **one representative scenario per group first**; for the rest of that group, apply the same fix and do a lighter live spot-check (confirm the same locator/element live) rather than a full independent replay from scratch. Note in the verdict table which scenarios were spot-checked vs fully replayed. **Before applying the representative's fix to other group members, check whether those members differ in their Examples table data** — a step exercised across an Examples matrix with different parameter sets can produce multiple root causes within one `groupId` (e.g. a Video-creative scenario and an HTML-creative scenario grouped together because they share the same failing step, but failing for different reasons). If members differ by data, treat each data-distinct subgroup as its own diagnosis rather than a spot-check.

Packet/prompt building and live-replay worker execution no longer happen unconditionally here — Phase 2.5 decides which groups actually need them, and Phase 3 builds packets only for those. See Phase 2.5 below.

## Phase 2: Load History & Fix Patterns

- Read `fix-patterns/_default.md` (always needed — cross-feature patterns apply to every run). If the file is missing or empty (0 bytes or only a header line), treat it as having no patterns and continue — no prompt, no stop.
- **Do not load feature-specific pattern files here.** They are loaded on demand in Phase 3, one per escalated group, when that group's `featureFile` is known. Loading all 24 pattern files upfront wastes context on features not present in this run. The full list of available pattern files is in the SKILL.md entry point's Reference files section.
- Read `ai-skills/kavach/failure-analyzer/history/fix-history.json` (every fix attempted before, with `verdict`/`confidence`/`attempts`/`change`/`liveVerification`/`analysisTier`/`notes`) — reuse an approach whose entry has `verdict: "script_issue_fix_applied"` and `liveVerification.scenarioContinuedPastFixPoint: true`; never repeat an approach where the same scenario has `attempts >= 2` and any verdict other than `"script_issue_fix_applied"` — that's the signal a prior fix didn't hold.
- **If `fix-history.json` is missing, empty (0 bytes), or contains only `[]`:** treat it as empty history and continue immediately — no approval prompt, no git-restore attempt, no pause. Write `[]` to the file if it is missing entirely (Phase 5 will append to it). An empty history is a valid starting state; the only reason to stop is if the file exists but contains malformed non-JSON content, in which case flag the parse error and stop.
- **Don't trust a prior fix as already live just because it was applied.** Before treating it as done, check the actual source file at the recorded line — `git log -p -- <file>` if unsure whether it was ever committed. History entries record what a session *intended* to apply; they aren't proof it survived (an apply can be skipped, reverted, or lost to an uncommitted session). If a failure recurs, check the real code first rather than re-diagnosing from scratch or wrongly assuming the app itself broke again.
- Print a summary table of all failures: scenario, feature file, failed step, whether it has prior history (✅ fixed before / ⚠️ prior fix failed / — none).

## Phase 2.5: Cheap Triage

Run the no-browser triage tier **before** building any live-replay packets — it decides which `groupId`s actually need Phase 3's expensive live replay:

```
cd ai-skills/kavach/failure-analyzer && python3 triage_workers.py -i history/failures-for-replay.json --fix-history history/fix-history.json -o history/triage-results --repo-root ../../..
```

If the script exits non-zero or `escalate-groups.json` is not written under `history/triage-results/<triage-timestamp>/`, stop and print `PHASE_SCRIPT_FAILED: triage_workers.py: <stderr>`. Do not proceed to Phase 3 without a valid `escalate-groups.json`.

This is mechanical for most groups and makes only a handful of small, **text-only** LLM calls (batched, no browser/tool access at all) for the remainder — nothing in this phase can drive a browser. For every group not already mechanically flagged intermittent (Phase 1), it tries, in order:

1. **Fix-pattern cache** — a prior run's fix for this exact scenario/group, re-verified as still present in the current source (not trusted from a stale `status` field).
2. **Deterministic classifier** — reuses `failure_analyzer.analyzers.assertion_analyzer.analyze_failure()` against the mechanical evidence already in `failures-for-replay.json` (expected/actual value, DOM page text, console/network errors). Zero LLM calls.
3. **Batched text-only LLM fallback** — only for whatever's left with an undetermined cause. Any item that still comes back `confidence: "low"` gets one more single-item retry with its failure-moment screenshot attached (the JPEG already captured in the Playwright trace, via `extract_screenshot_base64` — no live browser involved) before giving up and escalating to Phase 3.

**Hard safety constraint, enforced in code (`enforce_tier1_verdict_constraints` in `llm_static_triage.py`) and independently re-checked again at Phase 4 (below) by `validate_replay_receipts.py`'s combined-summary step:** this phase can never itself finalize `confirmed_product_bug` / `suspected_product_bug` / `not_reproduced_passed_live` — none of those are provable without a live replay actually happening. It only ever finalizes `not_reproduced_intermittent`, `needs_investigation`, or `script_issue_fix_proposed` (and only the last one with a diff whose literal "before" text is mechanically re-verified against the actual source file — never a fabricated fix). Anything it isn't confident about gets `needsLiveReplay: true` and falls through to Phase 3 untouched.

This writes `ai-skills/kavach/failure-analyzer/history/triage-results/<triage-timestamp>/`:
- `manifest.json` — every group, whether it was resolved here (including a `receiptPath` per resolved group, so Phase 4's aggregator can find it directly).
- `receipts/*.receipt.json` — same shape as Phase 3's worker receipts, tagged with an additive `analysisTier` field. Only 5 of the 6 possible values can appear here (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom`) — `tier2_live_replay` is assigned later, by Phase 4, only to Phase-3-sourced rows.
- `escalate-groups.json` — the `groupIds` this phase could not resolve; **only these go into Phase 3**.

Read `escalate-groups.json` and carry its `groupIds` forward into Phase 3.

## Phase 3: Per-Group Worker Diagnosis Loop

**Per-group context load (before building the packet):** Read the feature-specific fix-pattern file for this group's `featureFile` now — derive the filename (e.g. `Life_PMP.feature` → `fix-patterns/life-pmp.md`) and read it if it exists. This is the lazy-load step deferred from Phase 2. Empty or absent files are treated as having no known patterns.

Build compact live-replay worker packets **only for the groups Phase 2.5 escalated**:

```
python3 ai-skills/kavach/failure-analyzer/replay_workers.py --write-runner --only-groups failure-analyzer/history/triage-results/<triage-timestamp>/escalate-groups.json
```

If the script exits non-zero or `manifest.json` is not written under `history/replay-packets/<replay-timestamp>/`, stop and print `PHASE_SCRIPT_FAILED: replay_workers.py: <stderr>`. Do not proceed to worker execution without a valid manifest.

This writes `ai-skills/kavach/failure-analyzer/history/replay-packets/<replay-timestamp>/manifest.json` (a **freshly generated timestamp, distinct from `<triage-timestamp>`** — don't reuse Phase 2.5's timestamp here), one `group-*.json` packet, one `group-*.prompt.md` prompt, and `run-workers.sh` — same mechanical packet builder as before (redacted, page text capped, small code-context windows), just scoped to fewer groups. If running unattended, pass the same Claude/MCP flags via repeated `--claude-arg`, for example:
```
python3 ai-skills/kavach/failure-analyzer/replay_workers.py --write-runner --only-groups failure-analyzer/history/triage-results/<triage-timestamp>/escalate-groups.json \
  --claude-arg --mcp-config --claude-arg .claude/mcp-ci-life.json \
  --claude-arg --strict-mcp-config \
  --claude-arg --permission-mode --claude-arg auto \
  --claude-arg --allowedTools --claude-arg "Bash(*),Read,Write(*),Grep,Glob,mcp__playwright__browser_navigate,mcp__playwright__browser_tabs,mcp__playwright__browser_snapshot,mcp__playwright__browser_evaluate,mcp__playwright__browser_click,mcp__playwright__browser_fill_form,mcp__playwright__browser_type,mcp__playwright__browser_press_key,mcp__playwright__browser_wait_for,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_close,mcp__playwright__browser_run_code_unsafe,mcp__playwright__browser_select_option,mcp__playwright__browser_hover,mcp__playwright__browser_find"
```
Run the generated `run-workers.sh` serially by default. Use at most two browser workers at once if you intentionally parallelize later; shared Demo account state makes high concurrency noisy.

After workers finish, produce ONE consolidated, completeness-checked receipt set spanning both Phase 2.5 and Phase 3 — pass both manifests explicitly so there's no ambiguity about which timestamp goes where:
```
python3 ai-skills/kavach/failure-analyzer/validate_replay_receipts.py ai-skills/kavach/failure-analyzer/history/replay-packets/<replay-timestamp>/manifest.json \
  --triage-manifest ai-skills/kavach/failure-analyzer/history/triage-results/<triage-timestamp>/manifest.json
```
If the script exits non-zero or `combined-receipts.json` is not written, stop and print `PHASE_SCRIPT_FAILED: validate_replay_receipts.py: <stderr>`. Phase 4 cannot proceed without this file.

This writes `ai-skills/kavach/failure-analyzer/history/triage-results/<triage-timestamp>/combined-receipts.json` and mechanically:
- downgrades any `confirmed_product_bug` receipt that did not prove every product-bug gate condition (from either phase — Phase 2.5's receipts are re-validated here too, not trusted blindly, even though they already can't carry a forbidden verdict);
- assigns `analysisTier: "tier2_live_replay"` to every Phase-3-sourced row automatically (Phase 2.5 rows already carry their own `analysisTier`);
- accounts for every group Phase 2.5's manifest recorded (resolved or escalated) in exactly one row, with a `needs_investigation` fallback for any escalated group whose Phase 3 receipt is missing — this IS Phase 4's row-completeness check, already done by the time you read `combined-receipts.json`, not something to redo by hand.

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

If a receipt claims `confirmed_product_bug` but any `productBugGate` field is false/missing, or if `liveReplayPerformed`/`backgroundMatched` is false, it must downgrade to `suspected_product_bug` or `needs_investigation`. Historical findings and old verdicts are hints only; they are never evidence for a confirmed product bug. **This check is mechanical, not something the parent needs to redo by hand** — `validate_replay_receipts.py` (invoked at Phase 4) applies it to every receipt automatically; write the worker receipt correctly the first time so it doesn't get downgraded later.

**`productBugArtifacts` is required for `confirmed_product_bug` specifically**, also checked mechanically at Phase 4 — a boolean gate claim is self-reported by the same session that did the replay, so nothing independently proves `domStructureChecked`/`staleLocatorRuledOut` were actually done rather than rushed. Both fields must be the *real* observed text (the literal DOM excerpt / count output), at least ~20 characters, not a placeholder like `"true"`/`"verified"`/`"checked"` — a receipt missing either, or with a placeholder-shaped value, gets mechanically downgraded to `suspected_product_bug` even if every `productBugGate` boolean was `true`. This is on top of, not instead of, the gate check above.

### Live replay blocked (any reason) — still write the verdict report

Live replay can be blocked for reasons outside the script under test: dead/unreachable CDP endpoint (`CDP_ENDPOINT_DEAD`), the held browser losing network (`ERR_NETWORK_CHANGED`, blank page, navigation timeout), a Playwright tool rejection, an expired session. When that happens, **do not end the run with only chat output** — the run must still finish with a `replay-verdict-*.md` file. Never retry the same failing action in a loop (a blocked session is not fixed by more retries).

1. Stop Phase 3 work at the first unrecoverable block. Write a one-line reason string exactly as observed, e.g. `CDP_ENDPOINT_DEAD: 9223 — connection refused` or `ERR_NETWORK_CHANGED on held Life browser (CDP 9223); navigation timed out after 60s`.
2. Groups already diagnosed live before the block keep their real receipts. Every remaining escalated group gets no receipt (or, if you write one, `verdict: "needs_investigation"`, `liveReplayPerformed: false` and a `blockedReason` field with the same string).
3. Run the validator with the reason so it lands on every receipt-less escalated group:
   ```
   python3 ai-skills/kavach/failure-analyzer/validate_replay_receipts.py <replay-manifest> \
     --triage-manifest <triage-manifest> --blocked-reason "<reason>"
   ```
   (If Phase 3 never produced a replay manifest, omit the positional manifest argument.) Blocked groups come out as `needs_investigation` with `blockedReason` set; Phase 2.5-resolved groups keep their verdicts, so their fix proposals are still reported.
4. Continue to Phase 4 and write the report normally, with the **Blocked live replay** banner and Detail text described there. Print the block marker (`CDP_ENDPOINT_DEAD: ...` etc.) in chat as well.

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

**Phase 2.5 receipts carry their own `confidence` value under a different evidentiary standard** — set by mechanical rules or a text-only LLM call, never a live replay (e.g. `not_reproduced_intermittent` from same-run reliability stats is always `High` by convention, not because a browser confirmed anything). Pass these values straight through into the report as-is; don't re-derive them against the live-replay definitions above, and don't treat a Phase-2.5 `High` as equivalent evidentiary strength to a Phase-3 `High` — the `analysisTier` column/field is what actually distinguishes them.

Classification rules:

- **If `stepReliability.likelyIntermittent` is true, skip live replay entirely** → `Not Reproduced — Intermittent`, Confidence `High` (the run itself already proved it elsewhere). Record the passed/failed counts as Evidence (e.g. "not reproduced elsewhere this run — passed 12/13 times") and move to the next failure. Don't spend a browser session confirming what the mechanical signal already settled.
- **If the current, un-fixed code already passes live** (the original failure doesn't reproduce) → `Not Reproduced — Passed Live Replay`, Confidence `High`. Note this, don't force a fix; if there's an obvious hardening opportunity (see `_default.md`'s dismiss/wait patterns), mention it in the Conclusion as optional.
- **If it fails, and you can construct a corrected locator/action that resolves live** (`count >= 1`, `visible === true`, click produces the expected downstream state) → `Script Issue — Fix Proposed`. Test the corrected approach live yourself (via `browser_run_code_unsafe`) up to 2 live attempts (try approach 1, if it doesn't fully clear the step try a second approach) — do NOT edit the actual source file yet, see Phase 3.4. Confidence `High` if it cleared on attempt 1 with a clean downstream-state check, `Medium` if it took 2 attempts or the downstream check was partial.
- **If after 2 live attempts the step still fails with a structurally sound locator/action** (element genuinely absent from the DOM, click has no effect on app state, expected data isn't there) → `Product Bug — Confirmed` when verification was a full independent replay including a DOM-structure check, otherwise `Product Bug — Suspected`. Do not propose a code change. Record exactly what you observed as Evidence: e.g. "clicked X, DOM never changed / stayed on page Y", "waited for `//label[...]`, confirmed 0 matches in full DOM dump, feature panel never renders", "app returned [A, B] where test expects C — C absent from the list, not a near-variant".
- **If evidence remains genuinely inconclusive after the 2-attempt cap** (neither a clean pass nor a structurally-confirmed absence) → `Needs Investigation`, Confidence `Low`.
- **Before assigning `Product Bug — Confirmed`/`Suspected`, inspect the actual target element's DOM structure** (`.evaluate(el => el.outerHTML)` on whatever *did* match nearby, or on the element found via a broader/manual search) — don't infer soundness from `count()`/pagination/rendering checks alone. A locator can look reasonable and still fail structurally (e.g. `contains(text(),...)` matching only direct text nodes when the real text sits in a nested child element — see `_default.md`'s "label text moved into nested child span" pattern). This check is one `evaluate()` call; do it before letting reproduction cost push a failure to `Needs Investigation`. If a full fresh reproduction is genuinely the only way to get certainty, do it once, completely — "flagged for review, not fully reproduced" is not a valid resting state when a cheap DOM check was never attempted. **Keep the literal output of this `evaluate()` call and the corrected-locator `count()` output** — for `Product Bug — Confirmed` these go verbatim into the receipt's `productBugArtifacts`, not just into prose.

### 3.4 Present, don't apply

Once diagnosis is complete for a failure classified `Script Issue — Fix Proposed`: show the user the exact proposed change (file, line, before → after) and your live-verification evidence (the `count`/`visible` check output). Wait for the user's go-ahead before editing `src/main/java/pages/...`, `src/test/java/stepdefinitions/...`, or `.feature` files. Only edit those three categories of file — never application source code.

If the user approves multiple fixes at once, apply them together. If a fix is approved and applied before Phase 4's report is written, flip that scenario's Verdict to `Script Issue — Fix Applied` for the report. The receipt remains `script_issue_fix_proposed` — do not update it after a fix is applied; Phase 4's report-writing step is what upgrades the display verdict.

### 3.5 Optional final confirmation via Maven

Live replay is the primary diagnostic and is normally sufficient (a corrected locator that resolves live with `count >= 1`/`visible === true` won't hit the timeout that caused the original failure). Run `mvn test -Dtest=TestRunner -Dcucumber.filter.name="<exact scenarioName>"` afterward only if the user wants an additional automated-run confirmation — one scenario at a time, never the full suite. Skip if Maven is unavailable.

## Phase 4: Write Verdict Report

Read `ai-skills/kavach/failure-analyzer/history/triage-results/<triage-timestamp>/combined-receipts.json` (written by Phase 3's `validate_replay_receipts.py --triage-manifest` step) — it already unions Phase 2.5's and Phase 3's receipts and accounts for every group in exactly one row, with an `analysisTier` field on each (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom` | `tier2_live_replay`) telling you whether that row's evidence came from a live browser or not. This is the single source Phase 4 reads from — don't separately open the two underlying receipt directories or re-derive completeness by hand, that reconciliation is already done.

No narrative/story report (no per-failure prose write-up, no LLM-generated analysis text). Write one structured report to `ai-skills/kavach/failure-analyzer/history/replay-verdict-<YYYY-MM-DD-HHmm>.md`, timestamped to the minute the report is written (24h clock, e.g. `replay-verdict-2026-07-09-1432.md`). Each run gets its own timestamped file — never append to or overwrite a prior run's file, even if run on the same day. Multiple runs per day are expected and each is a distinct, independently referenceable artifact.

### Structure

```markdown
# Live-replay verdict — <YYYY-MM-DD HH:mm>

> ⛔ **Blocked live replay** — only when any row in `combined-receipts.json` has `blockedReason`: state the reason once here, the number of groups/scenarios left undiagnosed, and what to do (e.g. restart the Life bootstrap and re-run). Omit this banner entirely on a normal run. **A report without this banner asserts that all escalated groups received live replay — never omit it when `blockedReason` is present.**

## Summary

- 🔴 Critical (confirmed product bugs): N
- 🟢 High-confidence script fixes: N
- 🟡 Potential product bugs (needs review): N
- ⚪ Needs investigation / not reproduced: N

## 🔴 Critical

| Scenario | Feature file | Failed step | Verdict | Confidence | Verified |
|---|---|---|---|---|---|
| ... | Life_Targetings.feature | ... | Product Bug — Confirmed | High | Live |

### <Scenario name>

**Feature:** Life_Targetings.feature · **Failed step:** ... · **Confidence:** High

**Evidence**
- Failure occurs in `saveTacticDetails()`
- Reproduced save manually, completed in 5ms
- No timeout observed; full DOM dump confirms element absent

**Conclusion**
Likely load-dependent — genuine app bug, not a script issue.

## 🟢 High-confidence script fixes

(same table + card structure — grouped by root cause, see below)

## 🟡 Potential product bugs

(same table + card structure)

## ⚪ Needs investigation / not reproduced

| Scenario | Feature file | Failed step | Verdict | Confidence | Verified | Detail |
|---|---|---|---|---|---|---|
| ... | Life_PMP.feature | ... | Not Reproduced — Intermittent | High | Static | passed 12/13 times elsewhere this run |

**Detail column for blocked rows** (`blockedReason` set): `Live replay blocked: <blockedReason>` — never a guessed diagnosis. These rows go in this section as `Needs Investigation`, Verified `Static`.

**Detail column for `Needs Investigation` rows** (Phase 3 inconclusive only): brief summary of what was tried and why it remained ambiguous after 2 live attempts
```

`featureFile` is already present verbatim on every entry in `failures-for-replay.json` — just the bare filename (e.g. `Life_Tactic_Creation.feature`, strip the `src/test/resources/features/life/` path and any leading `file:`), no extra lookup or tokens needed.

### Verified column

Derive mechanically from each row's `analysisTier` in `combined-receipts.json` — never left to guesswork: `tier2_live_replay` → `Live`; anything else (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom`) → `Static`. This is the one place a reader can tell whether a row's evidence came from an actual browser click or from Phase 2.5's static/text-only analysis — a `Static` row in 🟢 High-confidence script fixes is exactly as valid a fix proposal, but wasn't confirmed live before being proposed.

### Section assignment

Mechanical, derived from Verdict × Confidence — not a separate judgment call. `fix-history.json` stores machine-format verdict values; map them to display strings here when writing the report:

| Machine value (`combined-receipts.json`) | Display string (report) |
|---|---|
| `script_issue_fix_proposed` | `Script Issue — Fix Proposed` |
| `script_issue_fix_applied` | `Script Issue — Fix Applied` |
| `confirmed_product_bug` | `Product Bug — Confirmed` |
| `suspected_product_bug` | `Product Bug — Suspected` |
| `not_reproduced_intermittent` | `Not Reproduced — Intermittent` |
| `not_reproduced_passed_live` | `Not Reproduced — Passed Live Replay` |
| `needs_investigation` | `Needs Investigation` |

- **🔴 Critical**: `confirmed_product_bug`
- **🟢 High-confidence script fixes**: `script_issue_fix_proposed` or `script_issue_fix_applied`, any confidence
- **🟡 Potential product bugs**: `suspected_product_bug`
- **⚪ Needs investigation / not reproduced**: `needs_investigation`, `not_reproduced_intermittent`, `not_reproduced_passed_live`

### Root-cause grouping

Failures sharing a `groupId` (Phase 1) get **one Evidence/Conclusion card**, written for whichever scenario in the group was fully, independently replayed (the representative). Every other scenario in that group still gets its own row in the section's table — never bundled into "+N more" (see the completeness rule below) — but its Detail just points at the representative, e.g. `same root cause as <representative scenario name> — see card above`. Never write a second card duplicating the same diagnosis.

### Cards

Write a full Evidence/Conclusion card only for the representative scenario of each distinct diagnosis (one per `groupId`, or per ungrouped failure) that falls in 🔴/🟢/🟡. Skip cards entirely for ⚪ — a one-line Detail cell is enough since these rows aren't actionable.

- **Evidence**: 2–5 bullets, each a concrete observed fact (not reasoning/speculation) — the failing method/locator, what live verification showed (`count`/`visible` output, DOM-structure check, timing), what passed vs failed.
- **Conclusion**: one line — the classification reasoning in plain language.

### Row completeness (unchanged — still enforced)

**One row per scenario — no bundling, no silent drops.** Every `scenarioName` present in `failures-for-replay.json` gets its own explicit row in exactly one section's table, by full name, even when it shares a `groupId`/root cause with others. Never collapse multiple scenarios into a single row via "(+N more)"/"(+N same-group scenarios)" or reference them only by count or row-number in prose — a reader must be able to find every scenario by name in some table. Same rule for `fix-history.json`: one entry per scenario, never a merged entry covering several scenarios at once.

Before writing the report, count the rows across all four sections against `failures-for-replay.json`'s total failure count and reconcile — every scenario must appear in exactly one row in exactly one section. If the counts don't match, find the missing scenario(s) and add their row(s) (with a real verdict — `Not Reproduced — Intermittent` still counts, silent omission does not) before finishing.

Print the same structure in chat too, but the file is the durable artifact for this run (referenced by scenario name so it can be cross-checked against `fix-history.json` later).

## Phase 5: Record History & Patterns

Mechanical bookkeeping, still useful — do this directly, no analyzer code needed.

For every failure diagnosed (whether fixed, flagged as a bug, or not reproduced), append an entry to `ai-skills/kavach/failure-analyzer/history/fix-history.json` (create as `[]` if missing). One entry per scenario — never merge several scenarios into one entry (e.g. `"scenarioName": "X (+ 1 same-group scenario)"`); a scenario not individually searchable by its exact name in this file is a bookkeeping bug: **Exception:** scenarios in groups with `blockedReason` (live replay blocked) get no `fix-history.json` entry — nothing was diagnosed, and an entry would wrongly count toward the `attempts >= 2` rule in Phase 2.

```json
{
  "timestamp": "<ISO timestamp>",
  "runDate": "<YYYY-MM-DD>",
  "scenarioName": "...",
  "featureFile": "src/test/resources/features/...",
  "verdict": "script_issue_fix_proposed" | "script_issue_fix_applied" | "confirmed_product_bug" | "suspected_product_bug" | "not_reproduced_intermittent" | "not_reproduced_passed_live" | "needs_investigation",
  "confidence": "high" | "medium" | "low",
  "priority": "critical" | "high_confidence_script_fix" | "potential_product_bug" | "needs_investigation",
  "groupId": "... | null",
  "isGroupRepresentative": true,
  "attempts": 1,
  "change": { "file": "...", "line": 0, "description": "...", "before": "...", "after": "..." },
  "liveVerification": { "elementFound": true, "scenarioContinuedPastFixPoint": true } | null,
  "notes": "Free-text — for Product Bug entries, describe exactly what was observed live. For non-representative group members, name the representative scenario here.",
  "analysisTier": "tier0_intermittent | fix_pattern_cache | tier1_deterministic | tier1_llm_static | tier2_offline_dom | tier2_live_replay",
  "review": { "status": "unreviewed", "reviewedAt": null, "note": null }
}
```

Always write `review` as `{"status": "unreviewed", "reviewedAt": null, "note": null}` for a new entry — never pre-fill it as `"agreed"`. It exists so a human can later spot-check verdicts and mark whether the analyzer got it right; see `ai-skills/kavach/failure-analyzer/review_verdicts.py` (`mark`/`report` subcommands) for that separate, human-driven step. This command never marks its own entries reviewed.

**`liveVerification` means what its name says — a browser actually observed this.** Write it only for `analysisTier: "tier2_live_replay"` entries, with the real `count()`/state-check outcome. For any Phase-2.5-sourced entry (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom`), write `liveVerification: null` — never fabricate `{"elementFound": true, ...}` to satisfy the shape; a downstream reader (script, dashboard, or person) trusts this field as proof a browser ran, and a null here correctly says one didn't.

For every unique `featureFile` touched, ensure `fix-patterns/<derived-name>.md` exists (bootstrap it with a `# <Feature> fix patterns` header + `## Run log` line if missing) and append (never overwrite) any reusable fix pattern discovered under `## Known good fixes` or `## Learned notes` — dedupe against existing entries.

### Clean up build artifacts

After the verdict file is confirmed written, delete the packet/prompt build artifacts from the live-replay directory — they are fully reproducible from `failures-for-replay.json` and accumulate across runs. Keep the `receipts/` subdirectory untouched (those are the source of truth for `validate_replay_receipts.py` and `fix-history.json`):

```bash
find ai-skills/kavach/failure-analyzer/history/replay-packets/<replay-timestamp> \
  -maxdepth 1 \( -name "*.prompt.md" -o -name "*.json" -o -name "run-workers.sh" \) \
  -delete
```

`-maxdepth 1` ensures only the top-level packet files are removed — the `receipts/` subdirectory is one level deeper and is not touched. If Phase 3 was skipped entirely (all groups resolved by Phase 2.5 with no live replay), there is no `<replay-timestamp>` directory and this step is a no-op.

## Phase 6: Close Held Browser

After Phase 4's verdict report and Phase 5's history/pattern writes have both completed, close the held CDP browser by touching the matching done marker:

```bash
# Life CDP bootstrap, port 9223
touch .claude/auth/life-cdp-done

# Studio CDP bootstrap, port 9224
touch .claude/auth/studio-cdp-done
```

Only touch the marker for the bootstrap actually used in this run. For a mixed Life+Studio batch, finish and close the first app's held browser before starting the second app's bootstrap. If the command exits early before the final report is written, do not touch the done marker automatically unless the user explicitly asks to stop the held browser.

## Testing

Before modifying `failure_analyzer/triage/llm_static_triage.py` or `triage_workers.py`, run:

```bash
python3 ai-skills/kavach/failure-analyzer/tests/test_llm_static_triage.py
```

All assertions must pass. This file's `EnforceTier1VerdictConstraintsTests` class is the hard safety constraint check: it verifies the Phase 2.5 triage tier cannot finalize a `confirmed_product_bug` or `suspected_product_bug` verdict without live replay, enforced by `enforce_tier1_verdict_constraints`. If a change to the triage logic breaks any assertion here, do not ship it — extend this test file's coverage rather than weakening the assertions.

## Rules (apply across every phase)

- **Never auto-apply.** Present every proposed fix (diagnosis + before/after + live-verification evidence) and wait for approval before editing files. This overrides any general "just proceed" default for this workflow specifically.
- **Live replay only, always faithful — for whatever reaches Phase 3.** Background → every scenario step in order → failing step. No `pageUrl` shortcuts, no reusing an unrelated existing record as a stand-in for the scenario's own setup steps. This governs Phase 3 specifically — it does not mean every group needs a live touch: Phase 2.5 correctly resolves some groups with zero browser interaction by design (see Phase 2.5), and that is not a violation of this rule.
- **Reference-first, snapshot-last.** Drive every step through its existing page-object locator; don't `browser_snapshot`/DOM-dump a step just to see where you are. Only fall back to a snapshot for the failing step, and only once a targeted `count()`/`.evaluate()` check on the existing locator already looks wrong.
- **2-attempt cap on live diagnosis**, same as before — clears within 2 → `Script Issue — Fix Proposed`; still fails with a sound locator → `Product Bug — Confirmed`/`Suspected`; genuinely inconclusive → `Needs Investigation`.
- **Mechanically-intermittent failures skip live replay.** `stepReliability.likelyIntermittent: true` (any step, Background or not) means the same run already proved this step usually passes elsewhere — classify as `Not Reproduced — Intermittent` from that signal alone, no browser session spent.
- **Only these files may ever be edited** (after approval): `.feature` files, `src/test/java/stepdefinitions/`, `src/main/java/pages/`. Never application source code, never `fix-history.json`/pattern files by hand beyond the appends described above.
- **Cross-reference history first.** Never repeat an approach where the same scenario appears in `fix-history.json` with `attempts >= 2` and a verdict other than `script_issue_fix_applied`.
- Run `graphify update .` after any applied fix so the graph stays current.
