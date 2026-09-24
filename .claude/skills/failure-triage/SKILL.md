---
name: failure-triage
description: >-
  Mechanically extracts failures from a Cucumber run, loads fix-pattern
  and fix-history context, and runs the cheap no-browser triage tier
  that decides which failure groups actually need live-replay
  diagnosis. Never drives a browser. Preloaded by the kavach-diagnose
  agent (.claude/agents/kavach-diagnose.md), hands off escalated
  groups to the live-replay-diagnosis skill.
---

# Failure Triage

<!-- First stage of the kavach-diagnose agent's pipeline: mechanical extraction + cheap triage. Escalated groups hand off to the live-replay-diagnosis skill; everything this stage resolves needs no browser at all. -->

No narrative LLM report-generation step is used here — that step (`failure_analyzer.main`'s old `script-fixes.json`/maintenance-story pipeline) is retired. Phase 2.5 (this skill's own cheap, no-browser triage tier) DOES make one or a few small text-only LLM calls, but it never drives a browser and is structurally forbidden from confirming a product bug on its own — see that phase for the safety constraints. Whatever this skill cannot resolve is escalated, by `groupId`, to the live-replay-diagnosis skill — that is the only handoff point between the two.

## Reference files

- [`kavach-data/fix-patterns/_default.md`](kavach-data/fix-patterns/_default.md) — cross-feature patterns: dismiss/wait hardening, nested-child-span locator guard, navigation utilities. Read in Phase 2 alongside `fix-history.json` — the only pattern file this skill itself ever reads (see Phase 2's note below on why feature-specific files are deliberately not loaded here).

The full current catalog of feature-specific pattern files lives at `kavach-data/fix-patterns/` and is not duplicated as a static list in this skill — it's read on demand elsewhere (see below), and a hand-maintained copy of the list here would just be one more place for it to drift out of sync with the real directory, as previously happened.

New feature files get their own pattern file bootstrapped by the verdict-reporting skill's history-writing phase, using the slug-derivation algorithm in the `kavach-knowledge` skill's Shape section (not a rule in this skill's own Phase 2 — this skill deliberately never loads feature-specific files at all, see Phase 2 below).

## Phase 1: Gather Failures & Mechanical Signals

1. If `kavach-data/history/failures-for-replay.json` doesn't exist or is older than the latest `target/cucumber-reports/cucumber.json`, generate it first:
   ```
   cd .claude/skills/kavach-diagnose/scripts && python3 list_failures.py
   ```
   If the script exits non-zero or `failures-for-replay.json` is not written, stop and print `PHASE_SCRIPT_FAILED: list_failures.py: <stderr>`. Do not attempt to reconstruct the output manually.

   This is a **mechanical-only** extraction (reuses `failure_analyzer.parsers.cucumber_parser` + `trace_parser` — no LLM call, no tokens spent) that parses the Cucumber JSON report and enriches each failure with Playwright trace data (page URL, DOM text, call log, screenshot). If `target/cucumber-reports/cucumber.json` doesn't exist, tell the user to run `mvn test` (or `mvn test -Dtest=FailedTestRunner`) first.
2. Read `failures-for-replay.json`. Each entry has: `scenarioName`, `featureFile`, `failedStep`, `errorMessage`, `errorLocation` (exact `stepDef`/`pageObject`/`utility`/`feature` file+line), `playwrightCallLog`, `pageUrl`, `pageText`, `screenshotFile`, `isBackgroundFailure`, `backgroundReliability`, `stepReliability`, `groupId`. Also read the top-level `groups` array.

3. Use the mechanical (no-LLM) signals already computed before doing any live replay:
   - **`stepReliability`** (set on *every* failure, Background or a regular scenario step alike): `{passed, failed, rate, likelyIntermittent}` — how many times this exact step (normalized) passed vs failed anywhere else in this same run. `likelyIntermittent: true` (rate ≥ 50%) means the same step succeeded elsewhere most of the time — that's obvious same-run evidence of a flaky timing race, not a consistently broken locator or a real bug. **Skip full live-replay diagnosis for these** — note it as "not reproduced elsewhere this run (passed N/M times)" in the verdict table and move on; don't spend a live-replay cycle re-confirming what the run itself already showed. Only fall back to a light spot-check (not a full replay) if the user specifically wants extra confirmation on a `likelyIntermittent` failure.
   - **`backgroundReliability`** (only set when `isBackgroundFailure` is true) is the older, Background-only version of the same idea — `stepReliability` supersedes it for triage purposes since it covers every step, but `backgroundReliability`'s feature-level framing ("how often this feature's Background passes") can still be a useful secondary data point when discussing a Background failure specifically.
   - **`groups`**: failures sharing the same `groupId` (same exception type + normalized step pattern) are likely the same root cause. **This applies only to groups escalated to the live-replay-diagnosis skill** — Phase 2.5 may resolve an entire group (representative and all) with zero live interaction, which is correct and not something to redo. For a group that *is* escalated: live-replay and fix **one representative scenario per group first**; for the rest of that group, apply the same fix and do a lighter live spot-check (confirm the same locator/element live) rather than a full independent replay from scratch. Note in the verdict table which scenarios were spot-checked vs fully replayed. **Before applying the representative's fix to other group members, check whether those members differ in their Examples table data** — a step exercised across an Examples matrix with different parameter sets can produce multiple root causes within one `groupId` (e.g. a Video-creative scenario and an HTML-creative scenario grouped together because they share the same failing step, but failing for different reasons). If members differ by data, treat each data-distinct subgroup as its own diagnosis rather than a spot-check.

Packet/prompt building and live-replay worker execution happen in the live-replay-diagnosis skill, not here — Phase 2.5 below decides which groups actually need them.

## Phase 2: Load History & Fix Patterns

- Read `kavach-data/fix-patterns/_default.md` (always needed — cross-feature patterns apply to every run). If the file is missing or empty (0 bytes or only a header line), treat it as having no patterns and continue — no prompt, no stop.
- **Do not load feature-specific pattern files here.** They are loaded on demand by the live-replay-diagnosis skill, one per escalated group, when that group's `featureFile` is known. Loading every pattern file upfront wastes context on features not present in this run. The full current list of available pattern files can be seen with a directory listing of `kavach-data/fix-patterns/` — not duplicated here (see Reference files above for why).
- Read `kavach-data/history/fix-history.json` (every fix attempted before, with `verdict`/`confidence`/`attempts`/`change`/`liveVerification`/`analysisTier`/`notes`) — reuse an approach whose entry has `verdict: "script_issue_fix_applied"` and `liveVerification.scenarioContinuedPastFixPoint: true`; never repeat an approach where the same scenario has `attempts >= 2` and any verdict other than `"script_issue_fix_applied"` — that's the signal a prior fix didn't hold.
- **If `fix-history.json` is missing, empty (0 bytes), or contains only `[]`:** treat it as empty history and continue immediately — no approval prompt, no git-restore attempt, no pause. Write `[]` to the file if it is missing entirely (the verdict-reporting skill's Phase 5 will append to it). An empty history is a valid starting state; the only reason to stop is if the file exists but contains malformed non-JSON content, in which case flag the parse error and stop.
- **Don't trust a prior fix as already live just because it was applied.** Before treating it as done, check the actual source file at the recorded line — `git log -p -- <file>` if unsure whether it was ever committed. History entries record what a session *intended* to apply; they aren't proof it survived (an apply can be skipped, reverted, or lost to an uncommitted session). If a failure recurs, check the real code first rather than re-diagnosing from scratch or wrongly assuming the app itself broke again.
- Print a summary table of all failures: scenario, feature file, failed step, whether it has prior history (✅ fixed before / ⚠️ prior fix failed / — none).

## Phase 2.5: Cheap Triage

Run the no-browser triage tier **before** building any live-replay packets — it decides which `groupId`s actually need the live-replay-diagnosis skill's expensive live replay:

```
cd .claude/skills/kavach-diagnose/scripts && python3 triage_workers.py
```

No path arguments — `-i`/`--fix-history`/`-o`/`--repo-root` all default to the correct repo-root-relative locations (`<repo-root>/kavach-data/history/...`) computed from the script's own location. Do not pass `-i history/failures-for-replay.json` (or the sibling `--fix-history`/`-o` flags) here: those are relative to the post-`cd` working directory (`.../scripts/`), which has no `history/` subdirectory, and the script crashes immediately with `FileNotFoundError` if given them.

If the script exits non-zero or `escalate-groups.json` is not written under `history/triage-results/<triage-timestamp>/`, stop and print `PHASE_SCRIPT_FAILED: triage_workers.py: <stderr>`. Do not hand off to live-replay-diagnosis without a valid `escalate-groups.json`.

This is mechanical for most groups and makes only a handful of small, **text-only** LLM calls (batched, no browser/tool access at all) for the remainder — nothing in this phase can drive a browser. For every group not already mechanically flagged intermittent (Phase 1), it tries, in order:

1. **Fix-pattern cache** — a prior run's fix for this exact scenario/group, re-verified as still present in the current source (not trusted from a stale `status` field).
2. **Deterministic classifier** — reuses `failure_analyzer.analyzers.assertion_analyzer.analyze_failure()` against the mechanical evidence already in `failures-for-replay.json` (expected/actual value, DOM page text, console/network errors). Zero LLM calls.
3. **Offline DOM analysis** (`tier2_offline_dom`, `failure_analyzer.analyzers.offline_dom_analyzer.analyze_offline()`) — tried once here for any group the deterministic classifier leaves with `cause == "unknown"`, before spending an LLM call: a deterministic, zero-LLM parse of the saved `page-source.html`/screenshot against the failing locator (stale-selector, hidden/disabled-element, and ambiguous-selector diagnosis). **A DOM match ambiguous across multiple elements (`diagnosisKind: "ambiguous_selector"`) always resolves as `needs_investigation`, never `script_issue_fix_proposed`** — the DOM alone cannot rule out a genuine product bug (duplicate-rendered elements), and this tier is structurally forbidden from confirming that on its own. It is tried a second time, after step 4 below, for anything LLM fallback still couldn't resolve.
4. **Batched text-only LLM fallback** — only for whatever's left with an undetermined cause. Any item that still comes back `confidence: "low"` gets one more single-item retry with its failure-moment screenshot attached (the JPEG already captured in the Playwright trace, via `extract_screenshot_base64` — no live browser involved) before giving up and escalating to the live-replay-diagnosis skill.

**Hard safety constraint, enforced in code (`enforce_tier1_verdict_constraints` in `llm_static_triage.py`) and independently re-checked again by the verdict-reporting skill, via `validate_replay_receipts.py`'s combined-summary step:** this phase can never itself finalize `confirmed_product_bug` / `suspected_product_bug` / `not_reproduced_passed_live` — none of those are provable without a live replay actually happening. It only ever finalizes `not_reproduced_intermittent`, `needs_investigation`, or `script_issue_fix_proposed` (and only the last one with a diff whose literal "before" text is mechanically re-verified against the actual source file — never a fabricated fix). Anything it isn't confident about gets `needsLiveReplay: true` and falls through to the live-replay-diagnosis skill untouched.

This writes `kavach-data/history/triage-results/<triage-timestamp>/`:
- `manifest.json` — every group, whether it was resolved here (including a `receiptPath` per resolved group, so the verdict-reporting skill's aggregator can find it directly).
- `receipts/*.receipt.json` — same shape as the live-replay-diagnosis skill's worker receipts, tagged with an additive `analysisTier` field. Only 5 of the 6 possible values can appear here (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom`) — `tier2_live_replay` is assigned later, by the verdict-reporting skill, only to rows sourced from the live-replay-diagnosis skill.
- `escalate-groups.json` — the `groupIds` this phase could not resolve; **only these are handed off to the live-replay-diagnosis skill**.

Read `escalate-groups.json` and hand off both the resolved `kavach-data/history/triage-results/<triage-timestamp>/` path and the `groupIds` inside `escalate-groups.json` to the live-replay-diagnosis skill — it needs the directory path again later (for `--only-groups` and, at the end of its own Phase 3, for `validate_replay_receipts.py --triage-manifest kavach-data/history/triage-results/<triage-timestamp>/manifest.json`), not just the IDs. This only works today by same-session context continuity if the timestamp itself isn't carried forward explicitly — state it, don't rely on it still being in context.

## Testing

Before modifying `failure_analyzer/triage/llm_static_triage.py` or `triage_workers.py`, run **both**:

```bash
python3 .claude/skills/kavach-diagnose/scripts/tests/test_llm_static_triage.py
python3 .claude/skills/kavach-diagnose/scripts/tests/test_triage_workers.py
```

All assertions must pass. `test_llm_static_triage.py`'s `EnforceTier1VerdictConstraintsTests` class is the hard safety constraint check: it verifies the Phase 2.5 triage tier cannot finalize a `confirmed_product_bug` or `suspected_product_bug` verdict without live replay, enforced by `enforce_tier1_verdict_constraints`. If a change to the triage logic breaks any assertion here, do not ship it — extend this test file's coverage rather than weakening the assertions.

`test_triage_workers.py`'s `DocumentedCliInvocationTests` class is the regression guard for this exact Phase 2.5 command: it asserts `triage_workers.py`'s no-argument defaults resolve under the real repo-root `kavach-data/history/` directory, not a path relative to the `scripts/` directory. This is the class of bug a change to argument parsing or default paths can silently reintroduce — a change to `triage_workers.py`'s CLI/path-handling code is not verified by `test_llm_static_triage.py` alone.

## Rules (this skill)

- **Mechanically-intermittent failures skip live replay.** `stepReliability.likelyIntermittent: true` (any step, Background or not) means the same run already proved this step usually passes elsewhere — classify as `Not Reproduced — Intermittent` from that signal alone, no browser session spent, no escalation to live-replay-diagnosis.
- **Cross-reference history first.** Never repeat an approach where the same scenario appears in `fix-history.json` with `attempts >= 2` and a verdict other than `script_issue_fix_applied`.
- Never finalize `confirmed_product_bug` / `suspected_product_bug` / `not_reproduced_passed_live` from this skill — none of those are provable without a live replay actually happening (enforced in code, see Testing above).
