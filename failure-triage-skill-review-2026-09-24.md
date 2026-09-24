# Skill Review: failure-triage

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-24
**Skill location:** .claude/skills/failure-triage

**Files reviewed:**
- SKILL.md
- kavach-diagnose/scripts/list_failures.py (via kavach-diagnose skill, referenced by path)
- kavach-diagnose/scripts/triage_workers.py
- kavach-diagnose/scripts/failure_analyzer/triage/llm_static_triage.py
- kavach-diagnose/scripts/failure_analyzer/triage/static_classifier.py
- kavach-diagnose/scripts/failure_analyzer/triage/fix_pattern_cache.py
- kavach-diagnose/scripts/failure_analyzer/analyzers/assertion_analyzer.py
- kavach-diagnose/scripts/failure_analyzer/analyzers/offline_dom_analyzer.py
- kavach-diagnose/scripts/failure_analyzer/packet_utils.py
- kavach-diagnose/scripts/validate_replay_receipts.py
- kavach-diagnose/scripts/tests/test_static_classifier.py, test_llm_static_triage.py, test_triage_workers.py (spot-checked)
- kavach-data/fix-patterns/_default.md (existence confirmed)
- live-replay-diagnosis/SKILL.md (downstream contract consumer)
- kavach-repair/SKILL.md (downstream consumer of resolved receipts)

This skill has no bundled `scripts/`/`references/`/`assets/` of its own — every script and pattern file it names lives in the sibling `kavach-diagnose` skill's directory or in `kavach-data/`, all of which were reachable and reviewed.

## Issues

### Critical
None found. The hard safety constraint — this skill can never itself finalize `confirmed_product_bug` / `suspected_product_bug` / `not_reproduced_passed_live` — holds structurally across every code path that can write a receipt (`enforce_tier1_verdict_constraints`'s allow-list, and `offline_dom_analyzer.py`'s own verdict vocabulary, both restricted to the same three safe outcomes), and is independently re-checked by `validate_replay_receipts.py` downstream. Even a mis-triaged `script_issue_fix_proposed` cannot reach application/test code unreviewed: `kavach-repair` (the only consumer that edits files) refuses to run unattended, requires an explicit `y/N` discovery-table confirmation naming low-confidence candidates by number, and requires three green Maven runs before ever committing.

### High

**Severity**: High
**Category**: Risk
**Location**: `triage_workers.py`'s `cause == "unknown"` branch, backed by `offline_dom_analyzer.py`'s "multiple matches" case (`diagnosisKind: "ambiguous_selector"`)
**Problem**: When a locator's static analysis finds more than one matching DOM node, `analyze_offline()` returns `verdict: "script_issue_fix_proposed"` with `confidence: "medium"`, even though its own evidence string says "product rendering duplicate elements cannot be ruled out." This path does not go through `enforce_tier1_verdict_constraints` (that function is only called from the deterministic-`disposition()` and LLM paths in `triage_workers.py`, not from the offline-DOM path), and `proposedChange` is always written as `None` for every `tier2_offline_dom` receipt — there is no mechanically-verified diff behind this verdict at all.
**Impact**: A failure whose real cause is a genuine product defect (e.g. the app duplicate-renders an element) can be classified with an actionable-sounding "Script Issue — Fix Proposed / Medium" label from a phase whose whole design premise (per this skill's own opening paragraph) is that it is "structurally forbidden from confirming a product bug on its own." A human skimming the triage summary table sees a confident script-issue verdict, not a flagged ambiguity, and `kavach-repair`'s Phase 2 will try to derive a locator-narrowing patch from `recommendedAction` text alone (there's no structured diff to check against source) — which can "fix" the symptom by picking one of the duplicate elements while leaving the actual duplicate-render bug in place. Downstream human review and Maven verification will likely catch an outright wrong locator, but they have no mechanism to catch "the locator now resolves to one of two elements that shouldn't both exist."
**Recommendation**: Route `offline_dom_analyzer.py`'s output through the same `enforce_tier1_verdict_constraints` gate the other two Tier-1 paths use (it already only emits the three allowed verdict strings, so this is a structural-consistency fix, not a new capability), and additionally special-case `ambiguous_selector`/multiple-match results to `needs_investigation` rather than `script_issue_fix_proposed` — narrowing a locator is only a safe auto-verdict when there's exactly one plausible corrected target, not when the DOM itself contains more elements than the UI should show.
**Example**: In `offline_dom_analyzer.py`'s multiple-matches branch (~line 776), change `verdict = "script_issue_fix_proposed"` to `verdict = "needs_investigation"` and keep the existing evidence text describing the ambiguity, so this case falls through to live replay instead of resolving statically.

**Severity**: High
**Category**: Risk
**Location**: Phase 2.5, "hand off" instruction — `Read escalate-groups.json and hand its groupIds off to the live-replay-diagnosis skill.`
**Problem**: `live-replay-diagnosis`'s own procedure needs the *full path* `kavach-data/history/triage-results/<triage-timestamp>/escalate-groups.json`, and later needs that same `<triage-timestamp>` again for `validate_replay_receipts.py --triage-manifest kavach-data/history/triage-results/<triage-timestamp>/manifest.json`. `failure-triage`'s SKILL.md never instructs the agent to record or pass forward the actual timestamp value it just generated — only "hand its groupIds off" (plural, i.e. the JSON list of IDs), which is a strict subset of what the next skill actually needs to do its job.
**Impact**: This works today only because both skills execute inside one continuous agent session that still has the printed `Wrote triage results: <out_dir>` output in context. If the pipeline is ever split across separate `claude -p` invocations per phase (the repo's own unattended examples for `live-replay-diagnosis` show exactly this pattern for the Life/Studio bootstrap split), or if a human relays "the groupIds" literally as instructed without the directory, live-replay-diagnosis has no way to locate `escalate-groups.json` or the triage manifest it needs for its own combined-receipts step, and the run stalls or mis-resolves group provenance.
**Recommendation**: State explicitly, in the same sentence that introduces the hand-off, that the resolved `<triage-timestamp>` directory path (not just the `groupIds` array) is part of what gets passed to `live-replay-diagnosis`, since that skill needs it twice more downstream.
**Example**: Change the closing line of Phase 2.5 to: `Hand off both the resolved kavach-data/history/triage-results/<triage-timestamp>/ path and the groupIds inside escalate-groups.json to the live-replay-diagnosis skill — it needs the directory again later for --triage-manifest, not just the IDs.`

### Medium

**Severity**: Medium
**Category**: Bug
**Location**: `static_classifier.py`'s `disposition()` function, the `cause == "timeout"` and `cause == "assertion-error"` branches
**Problem**: These two branches contain the most nuanced logic in the whole deterministic tier — distinguishing "text is in the DOM but the locator still timed out" (timing race) from "text genuinely isn't there" (potential regression) — but `classify()` (this file's only entry point, a thin wrapper around `assertion_analyzer.analyze_failure()`) never produces a `cause` value of `"timeout"` or `"assertion-error"`. `analyze_failure()`'s actual vocabulary is `environment-or-runner-failure`, `network-aborted`, `api-error`, `ui-value-missing`, `ui-value-present-timing`, `console-error`, `unknown`, and `same-run-intermittent` — confirmed by reading `CAUSE_TO_CATEGORY` and by `test_static_classifier.py`, which never exercises either branch.
**Impact**: The branches are dead code — they read as intentional, carefully-reasoned handling but can never execute. Every failure that would have hit them instead falls to the deterministic tier's final catch-all (`api-error, ui-value-present-timing, console-error, and anything else: ... always escalate`), which happens to be the safe outcome here, but only by accident of the catch-all's breadth, not because the intended logic ran. A future maintainer reading this file would reasonably believe timeout/assertion nuance is handled deterministically today; it is not.
**Recommendation**: Either wire `disposition()`'s cause-check to the real vocabulary (`ui-value-present-timing` and `ui-value-missing` already cover most of the same intent) and delete the dead `"timeout"`/`"assertion-error"` branches, or rename them to the causes `analyze_failure()` actually emits so the documented behavior matches what runs.
**Example**: Replace `if cause == "timeout":` with `if cause == "ui-value-present-timing":` (this is effectively what the dead branch's own logic already re-derives from `page_text`), and fold the dead `assertion-error` branch's DOM-presence check into the existing `ui-value-missing` branch, which already does the same "expected text present vs absent" check.

**Severity**: Medium
**Category**: Inconsistency
**Location**: Phase 2.5's ordered description — "For every group not already mechanically flagged intermittent (Phase 1), it tries, in order: 1. Fix-pattern cache ... 2. Deterministic classifier ... 3. Batched text-only LLM fallback"
**Problem**: The actual code (`triage_workers.py`) runs a fourth, undocumented deterministic sub-tier between steps 2 and 3: offline DOM analysis (`analyze_offline()`, tagged `tier2_offline_dom` in receipts), which is tried once when the deterministic classifier returns `cause == "unknown"`, and tried *again* for anything still escalated after the LLM fallback. This tier has its own, fairly elaborate evidence model (selector extraction, shadow-DOM text search, invisible-character detection, class-token guessing) that a reader of SKILL.md would have no way to know exists.
**Impact**: Someone auditing this skill's classification logic for evidence requirements (exactly the kind of review this document is doing) would review only the three documented tiers and miss an entire deterministic decision path — including the High-severity ambiguous-selector issue above, which lives entirely inside this undocumented tier. Traceability/reproducibility of a verdict ("why did this resolve without live replay?") is harder for anyone who trusts the SKILL.md's own tier list as complete.
**Recommendation**: Add "offline DOM analysis" as a numbered step in the ordered list (between the current 2 and 3, and again as the escalation fallback after 3), naming `analyze_offline()`/`offline_dom_analyzer.py` and its `tier2_offline_dom` receipt tag, matching the level of detail already given to the fix-pattern cache and deterministic classifier.

**Severity**: Medium
**Category**: Risk
**Location**: Phase 1, step 1 — mechanical extraction failure handling only covers `list_failures.py` exiting non-zero or not writing the output file
**Problem**: There's no instruction for what to do if `failures-for-replay.json` is written successfully but is missing expected fields (e.g. `groupId`, `stepReliability`) on some entries — a plausible outcome if `list_failures.py` partially parses a malformed or truncated `cucumber.json`.
**Impact**: Downstream code reads fields with `.get(...)` throughout (defensive), so a missing field mostly degrades to "treated as absent" rather than crashing — but a failure silently missing `groupId` would be difficult to group, triage, or escalate correctly, and nothing in this skill calls that out as a distinct failure mode from "the script crashed."
**Recommendation**: Add a brief instruction: if any failure entry in `failures-for-replay.json` lacks a `groupId` or `scenarioName`, treat that as a schema anomaly worth flagging to the user rather than silently triaging it with degraded signal.

### Low

**Severity**: Low
**Category**: Improvement
**Location**: `assertion_analyzer.py`'s `analyze_failure()` — the `pattern == "intermittent"` and `pattern == "regression"` narrative branches
**Problem**: `triage_workers.py`'s own `_frequency_by_scenario()` docstring explains that `fix-history.json` (the only history source this pipeline's Phase 2.5 actually populates) can only ever produce a `"recurring"` pattern or `None`, never `"intermittent"` or `"regression"` — those need real pass data this pipeline doesn't record. The two narrative branches for those patterns in the shared `analyze_failure()` are therefore dead code from this skill's call path (they may still be live for `main.py`'s separate, "retired" pipeline).
**Impact**: Low — purely a maintainability/clarity note, not a correctness issue, since `analyze_failure()` is shared code and the dead branches don't affect what this skill produces.
**Recommendation**: No action required for this skill specifically; worth a comment in `assertion_analyzer.py` noting which callers can and can't reach the `intermittent`/`regression` branches, if that file is touched again.

## Overall Assessment

**Purpose and scope:** This skill does one coherent thing — mechanical extraction plus a cheap, no-browser triage gate that decides what actually needs an expensive live-replay session — and it stays disciplined about that scope. It explicitly defers feature-specific pattern loading, packet building, and all browser interaction to later stages, and it says so with reasons rather than just asserting boundaries. A full skill (rather than an ad hoc prompt) is justified here: the safety property this skill exists to guarantee (never confidently claim a product bug without live replay) is exactly the kind of invariant that benefits from being enforced in code and re-checked independently downstream, not left to per-run agent judgment.

**Strengths:**
- The hard safety constraint (no `confirmed_product_bug`/`suspected_product_bug`/`not_reproduced_passed_live` from this tier) is enforced in code, tested (`EnforceTier1VerdictConstraintsTests`), and independently re-verified by a separate downstream script — a genuine defense-in-depth design, not just documentation asserting a rule.
- Every phase that shells out to a script has an explicit "script exited non-zero or expected file missing → stop and print `PHASE_SCRIPT_FAILED: ...`" rule. This is exactly the kind of actionable, unambiguous failure handling the checklist looks for — no "handle errors appropriately" vagueness.
- The `groupId`-not-`scenarioName` correlation contract is called out repeatedly and correctly, with a real motivating case (two failure groups legitimately sharing a scenario name) and a regression test guarding the CLI default-path behavior specifically (`DocumentedCliInvocationTests`).
- `fix_pattern_cache.py` correctly refuses to trust a stale `status`/`verdict` field and re-verifies the claimed fix is still textually present in source before treating it as a skip — good "verify, don't just trust history" discipline, and the SKILL.md states this rule plainly (Phase 2, "Don't trust a prior fix as already live just because it was applied").
- The downstream safety net for the exact question this review was asked to focus on — can a mis-triaged failure trigger unreviewed automated repair — is solid: `kavach-repair` refuses to run unattended at all, shows a discovery table calling out low-confidence candidates by name, and requires three green Maven runs before any commit.

**Major concerns:**
1. The offline-DOM tier's "ambiguous selector" case can produce a confident `script_issue_fix_proposed` for a symptom pattern the analyzer's own evidence text admits might be a product bug — the one classification gap that could actually mislead the evidence-requirements story this skill is built around (High, Risk).
2. The failure-triage → live-replay-diagnosis handoff contract under-specifies what gets passed forward (`groupIds` only, when the timestamp directory is also required downstream) — currently masked by same-session continuity, not by design (High, Risk).
3. Two dead branches in the deterministic classifier (`"timeout"`, `"assertion-error"`) mean documented-sounding logic never executes; the safe fallback is coincidental, not intentional (Medium, Bug).
4. An entire deterministic sub-tier (offline DOM analysis) is invisible in SKILL.md's own description of "how Phase 2.5 works," undermining the document's usefulness as a source of truth for auditing verdict provenance (Medium, Inconsistency).

**Missing capabilities:** No explicit guidance for a `failures-for-replay.json` that parses successfully but has anomalous/missing per-failure fields (only "script crashed" is treated as a distinct failure mode). Not a bug, but a gap worth naming given how much of this skill's safety story depends on every failure having a well-formed `groupId`/`stepReliability`.

**Overall quality rating:** Good — the architecture's safety-critical property (no unverified product-bug confirmation, no unreviewed auto-repair) is real and independently enforced, not just asserted in prose, and failure handling for script invocation is consistently strict. It falls short of Excellent because one classification path actively works against that same safety story (the ambiguous-selector case), and the skill's own documentation of its triage tiers and hand-off requirements has drifted slightly behind what the code and its downstream consumer actually need.

## Required Changes

1. Fix or gate the `offline_dom_analyzer.py` "ambiguous selector" (multiple DOM matches) case so it escalates to `needs_investigation`/live replay instead of resolving as `script_issue_fix_proposed`, and route `tier2_offline_dom` results through the same `enforce_tier1_verdict_constraints` gate the other Tier-1 paths use.
2. Update the Phase 2.5 hand-off instruction to explicitly name the `<triage-timestamp>` directory path as part of what's passed to `live-replay-diagnosis`, not just the `groupIds`.
3. Reconcile `static_classifier.py`'s `disposition()` cause-checks (`"timeout"`, `"assertion-error"`) with the actual cause vocabulary `analyze_failure()` produces, so the branches either run as intended or are removed.

## Optional Improvements

1. Document the offline-DOM analysis sub-tier explicitly in Phase 2.5's ordered list, including where it re-runs after LLM escalation.
2. Add guidance for `failures-for-replay.json` entries with missing `groupId`/`scenarioName` after a successful (non-crashing) extraction.
3. Note in `assertion_analyzer.py` which callers can and cannot reach the `intermittent`/`regression` narrative branches, to avoid future confusion about dead paths.

## Final Verdict

**APPROVED WITH CHANGES**

The skill's core safety design — mechanical verdict constraints enforced in code, independently re-validated downstream, and backstopped by a repair skill that refuses to run unattended or commit without triple-verified Maven passes — is genuinely sound, and this is not a case of a QA gate that can't explain itself or makes irreversible calls without a human checkpoint. It does not warrant REQUIRES REWORK: every issue found is a fixable, localized change (one classification branch, one documentation sentence, two dead code branches) rather than a structural flaw in the triage approach. But the ambiguous-selector classification gap and the under-specified hand-off contract are real gaps in exactly the two areas this review was asked to scrutinize most closely — evidence requirements for classification, and the groupId hand-off to live-replay-diagnosis — so this should not ship as fully production-ready until the three Required Changes above land.
