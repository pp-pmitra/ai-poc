# Skill Review: verdict-reporting

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-25
**Skill location:** .claude/skills/verdict-reporting

**Files reviewed:**
- SKILL.md
- (referenced, read for cross-checking) append_fix_pattern.py
- (referenced, read for cross-checking) append_fix_history.py
- (referenced, read for cross-checking) validate_kavach_contract.py
- (referenced, read for cross-checking) review_verdicts.py
- (referenced, read for cross-checking) kavach-knowledge/SKILL.md

This skill has no bundled scripts/references/assets of its own — everything it invokes lives in the sibling `kavach-diagnose/scripts/` bundle, so those were read to verify the SKILL.md's claims about their behavior rather than taking the prose at face value.

## Re-verification of prior findings

All four previously-reported issues are confirmed fixed against the current file content (verified directly in the diff between this version and the prior commit):

1. **`append_fix_pattern.py` stop-and-report contract (was High/Risk) — FIXED.** The line immediately after the append instructions now reads: *"If `append_fix_pattern.py` exits non-zero, stop and print `PHASE_SCRIPT_FAILED: append_fix_pattern.py: <stderr>`."* and explicitly says not to proceed to cleanup or Phase 6, framed as "the same class of bad outcome" as the `append_fix_history.py` contract. Parallel structure confirmed.

2. **`append_fix_history.py` failure sentence naming "Clean up build artifacts" (was Medium/Inconsistency) — FIXED.** The sentence now reads *"do not proceed to the 'Clean up build artifacts' step below or to Phase 6 (browser close)"* — the step is named explicitly rather than left to be inferred from a heading number, and the same phrasing was carried over verbatim into the new `append_fix_pattern.py` contract for consistency.

3. **Dead `graphify update .` rule in Rules section (was Medium/Inconsistency) — FIXED.** The Rules section now ends after the `liveVerification` rule; the `graphify update .` line is gone entirely, confirmed by diff (the removed line was the last line of the file).

4. **Dedup/supersession check not instructed before fix-pattern append (was Medium/Risk) — FIXED.** The fix-pattern paragraph now reads: *"Before appending, run `kavach-knowledge`'s dedup check (normalize locator + target file + fix expression, compare against every existing entry in that feature's file) against the candidate entry. If it surfaces a same-locator/same-target-file match with a different fix expression, mark that earlier entry `**[superseded YYYY-MM-DD — see entry below]**` via a direct edit before appending the new one... If it's an exact duplicate, skip appending."* This matches `kavach-knowledge`'s own dedup-criteria and supersession sections verbatim in substance.

No regressions were introduced alongside these fixes — the surrounding paragraphs (Row completeness, Section assignment, Verified column, Phase 6 sequencing) are byte-for-byte unchanged from the prior version.

## Issues

### Critical
None found.

### High
None found.

### Medium
None found.

### Low

**Severity**: Low
**Category**: Improvement
**Location**: Phase 4, "`featureFile` is already present verbatim on every entry in `failures-for-replay.json`..."
**Problem**: The sentence says the field is "already present verbatim... just the bare filename" but then instructs stripping the `src/test/resources/features/life/` path and any leading `file:` — i.e., it is not actually present as a bare filename verbatim; a small string transform is required.
**Impact**: Purely cosmetic confusion on a first read; the instruction that follows is unambiguous about what to actually do, so no agent following it literally would misbehave. Not a new issue — unchanged from the prior version, out of scope for this fix round, but worth a light pass since it's in the same paragraph family as items being cleaned up.
**Recommendation**: Reword to something like: *"`featureFile` is present on every entry in `failures-for-replay.json`; derive the bare filename for the report's Feature file column by stripping the `src/test/resources/features/life/` path and any leading `file:` — no extra lookup or tokens needed."*
**Example**: N/A — wording fix only.

---

**Severity**: Low
**Category**: Improvement
**Location**: Phase 5 fix-history JSON template, `"featureFile": "src/test/resources/features/..."`
**Problem**: Phase 4 tells the agent to use the *bare* filename for the verdict report's table column; Phase 5's `fix-history.json` template shows the *full path* for the same underlying field name (`featureFile`). Both are internally consistent with their own phase, and the template's literal example makes the expected format clear, but nothing calls out the deliberate difference in one place.
**Impact**: Low — an attentive agent reading each phase's own instructions in order gets the right format each time (this was true before this fix round too, and no failures traced to this in the reviewed history). A careless agent that only skims Phase 4 before writing Phase 5's entries could plausibly reuse the just-derived bare filename for `fix-history.json`, producing a `featureFile` value that no longer matches `kavach-repair`'s expectation of a full path (`kavach-repair/SKILL.md` uses `"featureFile": "<path>"` the same way).
**Recommendation**: Add a one-line callout in Phase 5 next to the template: *"Note: unlike the report table's bare filename, `fix-history.json`'s `featureFile` is the full path exactly as `failures-for-replay.json` provides it — do not strip it."*
**Example**: N/A — wording addition only.

---

**Severity**: Low
**Category**: Risk
**Location**: `append_fix_history.py`'s `REQUIRED_ENTRY_KEYS` / `validate_entry`, referenced from Phase 5's JSON template (`"priority": "critical" | "high_confidence_script_fix" | "potential_product_bug" | "needs_investigation"`)
**Problem**: The SKILL.md's fix-history entry template documents `priority` as a closed four-value enum, but `append_fix_history.py`'s validator checks presence of the `priority` key only — it never checks the value against that enum (unlike `verdict`, `confidence`, and `analysisTier`, which are all validated against their respective `VALID_*` sets).
**Impact**: A typo'd or drifted `priority` value (e.g. `"high"` instead of `"high_confidence_script_fix"`) would pass validation silently and land in `fix-history.json`, only surfacing later if/when something downstream (a dashboard, `review_verdicts.py`-style tooling) groups or filters by this field. This is a pre-existing gap in the validator, not something this fix round touched or was asked to touch, and is low-blast-radius today since nothing currently reviewed reads `priority` back out programmatically.
**Recommendation**: Add a `VALID_PRIORITIES` set to `append_fix_history.py` mirroring `VALID_VERDICTS`/`VALID_CONFIDENCE`/`VALID_TIERS`, and validate `priority` the same way. This is a script change outside this SKILL.md file itself, so it's listed here as a flagged gap rather than a required change to this specific file.
**Example**:
```python
VALID_PRIORITIES = {
    "critical", "high_confidence_script_fix",
    "potential_product_bug", "needs_investigation",
}
...
priority = entry.get("priority")
if priority is not None and priority not in VALID_PRIORITIES:
    problems.append(f"{prefix}: priority {priority!r} not one of {sorted(VALID_PRIORITIES)}")
```

## Overall Assessment

**Purpose and scope:** This skill is the third and final stage of the kavach-diagnose pipeline: it writes the timestamped verdict report, appends fix-history and fix-pattern bookkeeping, cleans up build artifacts, and releases the held CDP browser. The scope is well-bounded — it consumes a single validated input (`combined-receipts.json`) and produces three clearly-separated outputs (report file, `fix-history.json` entries, `fix-patterns/*.md` entries) plus a cleanup/teardown step. It does not diagnose, does not apply fixes, and does not touch a browser tool directly — all correctly deferred to the skills upstream (failure-triage, live-replay-diagnosis) and downstream (kavach-repair). This is the right level of granularity for a distinct pipeline skill; folding it into live-replay-diagnosis would blur the "diagnosis vs. reporting" boundary that the rest of the pipeline maintains cleanly.

**Strengths:**
- **Concurrency safety is now genuinely end-to-end.** Both `fix-history.json` (via `append_fix_history.py`) and the per-feature `.md` pattern files (via `append_fix_pattern.py`) are written under an exclusive `flock` held for the whole read-modify-write, confirmed by reading both scripts directly. `review_verdicts.py`'s `mark` subcommand independently confirmed to take the same lock on the same file, so a human running a spot-check concurrently with a live run serializes rather than races. This was a documented strength in the prior review and still holds, byte-for-byte, in the current scripts.
- **Producer/consumer schema agreement is real, not just claimed.** `validate_kavach_contract.py`'s `VALID_VERDICTS`/`VALID_TIERS`/`VALID_CONFIDENCE` sets match the machine-value tables and enum lists in this SKILL.md exactly (`script_issue_fix_proposed`, `confirmed_product_bug`, `suspected_product_bug`, `not_reproduced_intermittent`, `not_reproduced_passed_live`, `needs_investigation`; the six `analysisTier` values). `append_fix_history.py` correctly extends the verdict set with `script_issue_fix_applied` for its own file only, and the SKILL.md's own footnote about that value never appearing in `combined-receipts.json` is accurate against the validator's actual `VALID_VERDICTS`.
- **All four of the prior review's findings are cleanly fixed**, each with wording that mirrors its sibling instruction closely enough that a reader scanning both `append_fix_history.py` and `append_fix_pattern.py` failure-handling paragraphs would reasonably expect (and get) identical behavior.
- **Run-identity and input-validation guards** (added in this same change, though not part of the explicit checklist) are a solid addition: the "confirm it belongs to this run" check against `triageManifest`/`replayManifest` correctly anticipates the same-second-timestamp collision risk this pipeline's directory-naming scheme creates, and was verified against `validate_replay_receipts.py`'s actual output fields (`triageManifest`, `replayManifest` are both written exactly as described).
- **Row-completeness discipline** (unchanged from before, re-confirmed still present and unweakened) remains one of the strongest parts of this skill: no bundling, no silent drops, and an explicit reconciliation step before finishing.

**Major concerns:** None outstanding. The prior review's Critical/High-risk item (the missing stop-and-report contract for `append_fix_pattern.py`) is fully resolved, and no new Critical/High/Medium issue was found in this pass.

**Missing capabilities:** Nothing material. One could imagine this skill also validating the freshly-written verdict report file against a schema (the way `combined-receipts.json` is validated on the way in), but the report is intentionally a human/agent-readable Markdown artifact, not a machine contract, so this isn't a real gap — it's consistent with the "no narrative report" design constraint already documented.

**Overall quality rating:** **Excellent** — all four previously-identified issues are fixed correctly and consistently, the concurrency and schema-agreement strengths independently re-verified against the actual scripts (not just the prose), and the only remaining findings are Low-severity polish items with no behavioral consequence.

## Required Changes

None — this skill is ready as-is.

## Optional Improvements

1. Reword the `featureFile` "already present verbatim... just the bare filename" sentence in Phase 4 to avoid the internal contradiction between "verbatim" and "strip the path."
2. Add a one-line callout in Phase 5 clarifying that `fix-history.json`'s `featureFile` is the full path, unlike the report table's bare filename, to prevent an agent from reusing the wrong derived value across phases.
3. (Script-level, outside this SKILL.md) Add enum validation for `priority` in `append_fix_history.py`, matching the existing pattern for `verdict`/`confidence`/`analysisTier`.

## Final Verdict

**APPROVED**

All four issues raised in the prior review — the missing `append_fix_pattern.py` stop-and-report contract, the ambiguous Phase 5/Phase 6 cleanup-scope wording, the dead `graphify update .` rule, and the missing dedup-check instruction before fix-pattern append — are verified fixed against the current file content, each with wording that mirrors its sibling instruction closely enough to behave identically in practice. The full methodology pass beyond the checklist found no new Critical, High, or Medium issues: the concurrency-safety guarantees (exclusive `flock` on both `fix-history.json` and the per-feature pattern files, plus `review_verdicts.py`'s matching lock) and the producer/consumer schema agreement (this skill's enums matching `validate_kavach_contract.py`'s `VALID_*` sets exactly) both independently re-confirmed by reading the actual scripts rather than trusting the prose. The three Low-severity items noted (a confusingly-worded "verbatim" sentence, an unstated `featureFile` format difference between phases, and a script-level validation gap for `priority`) are polish-only and do not block production use.
