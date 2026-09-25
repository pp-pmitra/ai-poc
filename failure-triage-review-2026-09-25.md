# Skill Review: failure-triage (re-review)

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-25
**Skill location:** .claude/skills/failure-triage

**Files reviewed:**
- SKILL.md
- failure_analyzer/triage/static_classifier.py
- failure_analyzer/triage/llm_static_triage.py
- failure_analyzer/analyzers/offline_dom_analyzer.py
- triage_workers.py
- tests/test_static_classifier.py
- tests/test_triage_workers.py
- tests/test_llm_static_triage.py

## Context

This is a re-review of a prior High/Bug + Medium/Inconsistency finding: `static_classifier.py`'s strict-mode-violation branch used to return `resolved: True, verdict: "script_issue_fix_proposed", proposedChange: None`, which `enforce_tier1_verdict_constraints()` always downgraded to `needs_investigation` in the real pipeline anyway — making the branch dead code — while its own unit tests asserted the unreachable pre-downgrade shape, creating a latent trap where a future "fix" aligning the code to its tests would reopen a real false-negative path. It also contradicted `offline_dom_analyzer.py`'s `ambiguous_selector` handling of the identical symptom (locator matching >1 element).

## Verification performed

1. **`static_classifier.py`'s strict-mode-violation branch (lines 152–180).** Now returns `resolved: False, verdict: None, confidence: None, proposedChange: None, needsLiveReplay: True` — a genuine escalation, not a false resolution. The evidence text and an inline code comment explicitly walk through why: `enforce_tier1_verdict_constraints()` requires a `proposedChange` for `script_issue_fix_proposed` and this branch has none, so asserting `resolved: True` would just be downgraded anyway; the comment then explicitly cross-references `offline_dom_analyzer.py`'s `ambiguous_selector` case as the same reasoning applied to a DOM snapshot instead of a Playwright error. This matches the required fix exactly.

2. **`tests/test_static_classifier.py`'s `DispositionStrictModeTests`.** Both tests were rewritten:
   - `test_strict_mode_violation_escalates_to_live_replay` (renamed from `test_strict_mode_violation_finalizes_as_script_issue`) now asserts `resolved` is False, `verdict` is None, `needsLiveReplay` is True, and `proposedChange` is None.
   - `test_strict_mode_check_is_case_insensitive` now asserts `resolved` is False and `needsLiveReplay` is True (previously it would have asserted the false-resolution shape).
   Neither test asserts the old, unreachable shape any longer.

3. **`tests/test_triage_workers.py`'s new `StrictModeViolationPipelineTests` class.** This is a genuine integration-level test, not a repeat of the isolated `disposition()` check. It calls `disposition("any-cause", failure, Path("."))`, then — matching `run_triage()`'s real gating logic exactly (`if disp["resolved"]: disp = enforce_tier1_verdict_constraints(disp, repo_root)`) — conditionally applies `enforce_tier1_verdict_constraints()`, and finally asserts `disp["resolved"] and not disp.get("needsLiveReplay")` is False, which is the literal condition `triage_workers.run_triage()` uses to decide whether to finalize a receipt vs. escalate a group. The docstring explicitly states its purpose: prevent a future change to either function from silently reopening the gap that isolated `disposition()`-only tests couldn't catch.

4. **Test run.** `python3 -m pytest .claude/skills/kavach-diagnose/scripts/tests/ -v` was run directly (not taken on prior claim): **220 passed, 3 subtests passed**, 0 failed/skipped/errored.

5. **SKILL.md alignment.** SKILL.md itself was not touched this round (confirmed via `git diff HEAD~1`), and it doesn't specifically describe the strict-mode-violation branch — its description of Phase 2.5's tier ordering (fix-pattern cache → deterministic classifier → offline DOM analysis → batched LLM fallback) and its stated hard safety constraint ("never finalize `confirmed_product_bug`/`suspected_product_bug`/`not_reproduced_passed_live`" and "only ever finalizes `not_reproduced_intermittent`, `needs_investigation`, or `script_issue_fix_proposed`... Anything it isn't confident about gets `needsLiveReplay: true`") remain accurate to the current code. No drift found.

6. **Broader sweep.** Searched the rest of the skill tree and `kavach-diagnose.md`/related scripts for any other stale reference to the old strict-mode behavior or to `disposition()`'s old shape — none found. `llm_static_triage.py`'s `enforce_tier1_verdict_constraints()` (the function the fix's reasoning depends on) was re-read directly and confirmed to behave as described: it downgrades any `script_issue_fix_proposed` whose `proposedChange["before"]` text can't be mechanically re-verified near the claimed line in the real source file, and it forces `needsLiveReplay: True` whenever confidence isn't explicitly `"high"`/`"medium"`. `offline_dom_analyzer.py`'s `ambiguous_selector` branch was re-read directly too and confirmed to still always resolve as `needs_investigation`, never `script_issue_fix_proposed`, with matching reasoning text.

## Issues

### Critical
None found.

### High
None found. The previously reported High/Bug (dead-code branch with misleading tests that could reopen a false-negative path) is fully resolved: the branch's actual behavior, its own unit tests, and a new integration-level pipeline test are now all in agreement, and the integration test specifically guards against this exact class of regression recurring.

### Medium
None found. The previously reported Medium/Inconsistency (contradiction with `offline_dom_analyzer.py`'s `ambiguous_selector` reasoning) is resolved — the two modules now state the identical reasoning about the same underlying symptom (locator matching >1 element can't rule out a product bug from static evidence alone), and `static_classifier.py`'s comment explicitly cross-references the other module by name.

### Low

**Severity**: Low
**Category**: Improvement
**Location**: `static_classifier.py`, strict-mode-violation branch (~lines 152–180)
**Problem**: The branch's returned dict is now structurally identical (modulo evidence text) to the module's generic fallback case at the bottom of `disposition()` (lines 253–261) — both return `resolved: False, verdict: None, confidence: None, proposedChange: None, needsLiveReplay: True`. The only difference is the evidence list content.
**Impact**: None functionally — this is purely a "could this be simpler" observation, not a defect. Two branches producing the same shape with different evidence text is a completely legitimate pattern.
**Recommendation**: No change required. Optionally, if a future refactor wants to reduce branch count, this evidence-only case could be expressed as a small `evidence` lookup rather than a full early return — but the current explicit branch is more readable and self-documenting given the reasoning comment attached to it, so this is genuinely optional.
**Example**: N/A — not recommended as a required change.

## Overall Assessment

**Purpose and scope:** The `failure-triage` skill's stated purpose (cheap, no-browser triage that decides which failure groups need live-replay diagnosis) remains well-bounded, and this round's changes stay entirely inside that scope — no scope creep introduced.

**Strengths:**
- The fix directly closes the exact gap the prior review identified: the branch's runtime behavior, its unit tests, and a new pipeline-level integration test are now all consistent with each other and with the real `run_triage()` code path.
- The new `StrictModeViolationPipelineTests` class is a genuinely well-designed regression guard — it doesn't just re-test `disposition()` in isolation again (which is exactly the mistake that let the original bug hide), it reproduces the actual two-function sequence and gating condition `run_triage()` uses, with a docstring that explains why this specific shape of test is necessary.
- The code comment in `static_classifier.py` cross-references `offline_dom_analyzer.py` by name and explains the shared reasoning, which is good practice for keeping two independently-evolving modules from drifting apart again silently.
- Full test suite (220 tests, 3 subtests) passes cleanly with no skips or errors, independently verified rather than taken on faith.

**Major concerns:** None. This round's change is narrowly scoped, directly verified against the actual failure mode it claims to fix, and backed by a test that specifically targets the systemic root cause (isolated unit tests missing a pipeline-level constraint) rather than just the symptom.

**Missing capabilities:** None identified specific to this round's change. (General missing capabilities for the broader skill, if any, were out of scope for this fix-verification pass and were not newly introduced or removed here.)

**Overall quality rating:** Excellent — the fix is correct, verified independently at every layer the prior review asked for (branch behavior, unit tests, integration test, full suite run), and it improves the codebase's resistance to the same class of bug recurring rather than just patching the immediate symptom.

## Required Changes

None — this skill is ready as-is.

## Optional Improvements

1. (Low, Improvement) Optionally simplify the strict-mode-violation branch's shared shape with the generic fallback case via an evidence-lookup pattern — not recommended given the current explicit branch is more readable; listed only for completeness.

## Final Verdict

**APPROVED**

The prior High/Bug and Medium/Inconsistency findings are both fully and correctly resolved in the current code. Independent verification confirms: the strict-mode-violation branch now escalates (`resolved: False, needsLiveReplay: True`) instead of falsely resolving; both existing unit tests were rewritten to assert the new, correct behavior; a new integration-level test (`StrictModeViolationPipelineTests`) exercises the real `disposition()` → `enforce_tier1_verdict_constraints()` sequence and asserts end-to-end escalation, closing the exact isolation gap that let the original bug hide behind passing unit tests; the full test suite (220 tests) passes cleanly; and SKILL.md's description of this tier's behavior remains accurate to the current code with no drift. No new issues were introduced by this round's changes.
