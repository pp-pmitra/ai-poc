---
name: verdict-reporting
description: >-
  Writes Kavach's timestamped verdict report from the live-replay-
  diagnosis skill's combined receipts, appends fix-history and
  fix-pattern bookkeeping, cleans up build artifacts, and closes the
  held CDP browser. Final stage of the kavach-diagnose agent's
  pipeline — produces the artifact kavach-repair (or a human)
  consumes next.
---

# Verdict Reporting

<!-- Third and final stage of the kavach-diagnose agent's pipeline. Consumes combined-receipts.json from the live-replay-diagnosis skill; its output (the verdict report + updated fix-history.json) is what kavach-repair or a human acts on next. -->

## Phase 4: Write Verdict Report

Read `kavach-data/history/triage-results/<triage-timestamp>/combined-receipts.json` (written by the live-replay-diagnosis skill's `validate_replay_receipts.py --triage-manifest` step) — it already unions the failure-triage skill's and the live-replay-diagnosis skill's receipts and accounts for every group in exactly one row, with an `analysisTier` field on each (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom` | `tier2_live_replay`) telling you whether that row's evidence came from a live browser or not. This is the single source this skill reads from — don't separately open the two underlying receipt directories or re-derive completeness by hand, that reconciliation is already done.

**If this file does not exist, is not valid JSON, or `python3 .claude/skills/kavach-diagnose/scripts/validate_kavach_contract.py <path>` exits non-zero, stop immediately and print `PHASE_SCRIPT_FAILED: combined-receipts.json invalid: <reason>`.** Do not attempt to write a verdict report from partial data, memory, or chat context — a report is only valid when it reflects this file's actual, validated contents.

**Before trusting this file, confirm it belongs to this run.** Since `triage-results`/`replay-packets` directories are named by a second-granularity timestamp with no uniqueness guard, two runs finishing in the same wall-clock second (or a manual run sharing a checkout with another in-flight run) could otherwise interleave. Check that `combined-receipts.json`'s `triageManifest` field points at the exact `<triage-timestamp>` directory this session's own Phase 2.5 call produced (and, if present, that `replayManifest` matches the `<replay-timestamp>` this session's own Phase 3 call produced). If either doesn't match, stop and print `PHASE_SCRIPT_FAILED: combined-receipts.json belongs to a different run (expected <triage-timestamp>, found <actual>)` rather than reporting on a manifest this run did not generate.

No narrative/story report (no per-failure prose write-up, no LLM-generated analysis text). Write one structured report to `kavach-data/history/replay-verdict-<YYYY-MM-DD-HHmmss>.md`, timestamped to the **second** the report is written (24h clock, e.g. `replay-verdict-2026-07-09-143205.md`) — matching the `triage-results`/`replay-packets` directories' own `%Y-%m-%d-%H%M%S` timestamp convention, not a coarser minute-only one. Each run gets its own timestamped file — never append to or overwrite a prior run's file, even if run on the same day or the same minute. Multiple runs per day (and, rarely, per minute — a retriggered or rerun CI job) are expected; if a file at the computed path already exists, append `-2`, `-3`, … before the `.md` extension until the path is free, rather than overwriting it. Multiple runs per day are expected and each is a distinct, independently referenceable artifact.

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

**Detail column for `Needs Investigation` rows** (live-replay-diagnosis inconclusive only): brief summary of what was tried and why it remained ambiguous after 2 live attempts
```

`featureFile` is already present verbatim on every entry in `failures-for-replay.json` — just the bare filename (e.g. `Life_Tactic_Creation.feature`, strip the `src/test/resources/features/life/` path and any leading `file:`), no extra lookup or tokens needed.

### Verified column

Derive mechanically from each row's `analysisTier` in `combined-receipts.json` — never left to guesswork: `tier2_live_replay` → `Live`; anything else (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom`) → `Static`. This is the one place a reader can tell whether a row's evidence came from an actual browser click or from the failure-triage skill's static/text-only analysis — a `Static` row in 🟢 High-confidence script fixes is exactly as valid a fix proposal, but wasn't confirmed live before being proposed.

### Section assignment

Mechanical, derived from Verdict × Confidence — not a separate judgment call. `fix-history.json` stores machine-format verdict values; map them to display strings here when writing the report:

| Machine value (`fix-history.json`; see note below for `combined-receipts.json`) | Display string (report) |
|---|---|
| `script_issue_fix_proposed` | `Script Issue — Fix Proposed` |
| `script_issue_fix_applied`* | `Script Issue — Fix Applied` |
| `confirmed_product_bug` | `Product Bug — Confirmed` |
| `suspected_product_bug` | `Product Bug — Suspected` |
| `not_reproduced_intermittent` | `Not Reproduced — Intermittent` |
| `not_reproduced_passed_live` | `Not Reproduced — Passed Live Replay` |
| `needs_investigation` | `Needs Investigation` |

\* `script_issue_fix_applied` never appears in `combined-receipts.json` (it is outside the contract schema's `verdict` enum) — it is written only into `fix-history.json`, and only by `kavach-repair`, after this skill's own run has already finished. This row exists so the mapping table stays complete for anyone tracing a `fix-history.json` entry back to a display string; when writing the verdict report itself, only `combined-receipts.json` rows are ever consulted, so this specific value never appears in the report you're producing.

- **🔴 Critical**: `confirmed_product_bug`
- **🟢 High-confidence script fixes**: `script_issue_fix_proposed` or `script_issue_fix_applied`, any confidence
- **🟡 Potential product bugs**: `suspected_product_bug`
- **⚪ Needs investigation / not reproduced**: `needs_investigation`, `not_reproduced_intermittent`, `not_reproduced_passed_live`

### Root-cause grouping

Failures sharing a `groupId` (assigned by the failure-triage skill) get **one Evidence/Conclusion card**, written for whichever scenario in the group was fully, independently replayed (the representative). Every other scenario in that group still gets its own row in the section's table — never bundled into "+N more" (see the completeness rule below) — but its Detail just points at the representative, e.g. `same root cause as <representative scenario name> — see card above`. Never write a second card duplicating the same diagnosis.

### Cards

Write a full Evidence/Conclusion card only for the representative scenario of each distinct diagnosis (one per `groupId`, or per ungrouped failure) that falls in 🔴/🟢/🟡. Skip cards entirely for ⚪ — a one-line Detail cell is enough since these rows aren't actionable.

- **Evidence**: 2–5 bullets, each a concrete observed fact (not reasoning/speculation) — the failing method/locator, what live verification showed (`count`/`visible` output, DOM-structure check, timing), what passed vs failed.
- **Conclusion**: one line — the classification reasoning in plain language.

### Row completeness (unchanged — still enforced)

**One row per scenario — no bundling, no silent drops.** Every `scenarioName` present in `failures-for-replay.json` gets its own explicit row in exactly one section's table, by full name, even when it shares a `groupId`/root cause with others. Never collapse multiple scenarios into a single row via "(+N more)"/"(+N same-group scenarios)" or reference them only by count or row-number in prose — a reader must be able to find every scenario by name in some table. Same rule for `fix-history.json`: one entry per scenario, never a merged entry covering several scenarios at once.

**Recovering a group's full scenario list when `affectedScenarios` is absent:** a `combined-receipts.json` row only carries `affectedScenarios` when it came from a real worker receipt (per the schema); a missing-receipt fallback row (e.g. a group escalated to live replay whose worker crashed) carries only `scenarioCount` and one `representativeScenario`. For those rows, recover every member scenario's name by filtering `failures-for-replay.json`'s entries for the matching `groupId` — do not open the replay-packets/triage-results directories by hand for this, and do not guess or omit the other members just because `affectedScenarios` is missing from the row.

Before writing the report, count the rows across all four sections against `failures-for-replay.json`'s total failure count and reconcile — every scenario must appear in exactly one row in exactly one section. If the counts don't match, find the missing scenario(s) and add their row(s) (with a real verdict — `Not Reproduced — Intermittent` still counts, silent omission does not) before finishing.

Print the same structure in chat too, but the file is the durable artifact for this run (referenced by scenario name so it can be cross-checked against `fix-history.json` later).

## Phase 5: Record History & Patterns

Mechanical bookkeeping, still useful — but never hand-write `fix-history.json` directly (read-whole-file/append-in-memory/write-whole-file-back has no schema check and no protection against two runs finishing close together clobbering each other's writes). Build one entry per failure diagnosed (whether fixed, flagged as a bug, or not reproduced) in the shape below, collect the whole run's entries into one JSON array, and pipe that array through `python3 .claude/skills/kavach-diagnose/scripts/append_fix_history.py`: it validates every entry against the same machine-format enums the contract gate checks (rejecting the whole batch, writing nothing, if any entry is malformed) and appends under an exclusive file lock. One entry per scenario — never merge several scenarios into one entry (e.g. `"scenarioName": "X (+ 1 same-group scenario)"`); a scenario not individually searchable by its exact name in this file is a bookkeeping bug: **Exception:** scenarios in groups with `blockedReason` (live replay blocked) get no `fix-history.json` entry — nothing was diagnosed, and an entry would wrongly count toward the `attempts >= 2` rule in the failure-triage skill.

**If `append_fix_history.py` exits non-zero, stop and print `PHASE_SCRIPT_FAILED: append_fix_history.py: <stderr>`.** The run is not considered complete until this step succeeds — do not proceed to the "Clean up build artifacts" step below or to Phase 6 (browser close), and do not report the run as finished if the batch was rejected, even though Phase 4's report file has already been written. A written report with silently-unrecorded history is a worse outcome than a visibly incomplete run, since nothing else signals that `fix-history.json` didn't actually update.

`.claude/skills/kavach-diagnose/scripts/review_verdicts.py`'s `mark` subcommand now takes the same exclusive lock `append_fix_history.py` uses for its own read-modify-write, so a human running `review_verdicts.py mark ...` concurrently with this phase's `append_fix_history.py` call serializes with it instead of racing.

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

Always write `review` as `{"status": "unreviewed", "reviewedAt": null, "note": null}` for a new entry — never pre-fill it as `"agreed"`. It exists so a human can later spot-check verdicts and mark whether the analyzer got it right; see `.claude/skills/kavach-diagnose/scripts/review_verdicts.py` (`mark`/`report` subcommands) for that separate, human-driven step. This command never marks its own entries reviewed.

**`liveVerification` means what its name says — a browser actually observed this.** Write it only for `analysisTier: "tier2_live_replay"` entries, with the real `count()`/state-check outcome. For any Phase-2.5-sourced entry (`tier0_intermittent` | `fix_pattern_cache` | `tier1_deterministic` | `tier1_llm_static` | `tier2_offline_dom`), write `liveVerification: null` — never fabricate `{"elementFound": true, ...}` to satisfy the shape; a downstream reader (script, dashboard, or person) trusts this field as proof a browser ran, and a null here correctly says one didn't.

For every unique `featureFile` touched, derive `<feature-slug>.md` using the exact algorithm in the `kavach-knowledge` skill's Shape section (e.g. `Life_CampaignDashboard.feature` → `life-campaign-dashboard.md`). Use this same algorithm every time — a different-looking slug derived here than the one `kavach-repair` looks up later silently orphans the pattern this run just recorded. Before appending, run `kavach-knowledge`'s dedup check (normalize locator + target file + fix expression, compare against every existing entry in that feature's file) against the candidate entry. If it surfaces a same-locator/same-target-file match with a *different* fix expression, mark that earlier entry `**[superseded YYYY-MM-DD — see entry below]**` via a direct edit before appending the new one (see `kavach-knowledge`'s "Correcting a wrong entry" section). If it's an exact duplicate, skip appending. Otherwise append it (never hand-edit or overwrite an existing entry) via:

```bash
echo "<the new dated block, e.g. '### 2026-09-25\n\n- ...'>" | \
  python3 .claude/skills/kavach-diagnose/scripts/append_fix_pattern.py <feature-slug> --feature-name "<Feature Name>"
```

`--feature-name` only matters the first time a feature file gets its header written (it supplies the `# <Feature> fix patterns` / `## Run log` header) — omit it for `_default.md` or any feature file that already has content. A tracked-but-still-empty placeholder file counts as "no content yet": pass `--feature-name` for it exactly as if the file were brand new, since path-existence alone does not mean it already has a header. This script holds an exclusive lock for the whole read-modify-write, the same protection `append_fix_history.py` gives `fix-history.json` — never hand-edit the `.md` file directly, since kavach-repair can be appending to the same file at close to the same time.

**If `append_fix_pattern.py` exits non-zero, stop and print `PHASE_SCRIPT_FAILED: append_fix_pattern.py: <stderr>`.** Treat this exactly like a failed `append_fix_history.py` call above: the run is not complete, and do not proceed to the "Clean up build artifacts" step below or to Phase 6 (browser close) — a report with a silently-lost fix-pattern is the same class of bad outcome as one with silently-unrecorded history.

### Clean up build artifacts

After the verdict file is confirmed written, delete the packet/prompt build artifacts from the live-replay directory — they are fully reproducible from `failures-for-replay.json` and accumulate across runs. Keep the `receipts/` subdirectory untouched (those are the source of truth for `validate_replay_receipts.py` and `fix-history.json`):

**Derive `<replay-timestamp>` from this run's own `combined-receipts.json`'s `replayManifest` field** (its path is `kavach-data/history/replay-packets/<replay-timestamp>/manifest.json`) — never from "the most recent directory under `replay-packets/`". This is a destructive `-delete`; guessing the most-recent directory risks deleting a different, concurrently-running batch's still-in-progress packet files instead of this run's own.

```bash
find kavach-data/history/replay-packets/<replay-timestamp> \
  -maxdepth 1 \( -name "*.prompt.md" -o -name "*.json" -o -name "run-workers.sh" \) \
  -delete
```

`-maxdepth 1` ensures only the top-level packet files are removed — the `receipts/` subdirectory is one level deeper and is not touched. If `combined-receipts.json`'s `replayManifest` is `null` (the live-replay-diagnosis skill was skipped entirely — all groups resolved by failure-triage with no live replay), there is no `<replay-timestamp>` directory and this step is a no-op.

## Phase 6: Close Held Browser

After Phase 4's verdict report and Phase 5's history/pattern writes have both completed, close the held CDP browser by touching the matching done marker:

```bash
# Life CDP bootstrap, port 9223
touch .claude/auth/life-cdp-done

# Studio CDP bootstrap, port 9224
touch .claude/auth/studio-cdp-done
```

Only touch the marker for the bootstrap actually used in this run. If the command exits early before the final report is written, do not touch the done marker automatically unless the user explicitly asks to stop the held browser.

**Mixed Life+Studio batch sequencing is not this skill's responsibility.** `.claude/run-mixed-batch.sh` owns starting each app's bootstrap in order and touching each done-marker as it finishes with that app — this skill only ever touches the one marker for whichever single app it was run against. Do not attempt to start a second app's bootstrap from within this skill; it has no tool access to do so, and the wrapper script already handles that ordering.

## Rules (this skill)

- **Row completeness is mandatory, not optional.** Every scenario the failure-triage skill extracted must appear in exactly one row in exactly one section — reconcile counts before finishing (Phase 4's Row completeness subsection above).
- **Never fabricate `liveVerification`.** Write it only for `analysisTier: "tier2_live_replay"` entries; everything else gets `liveVerification: null`.
