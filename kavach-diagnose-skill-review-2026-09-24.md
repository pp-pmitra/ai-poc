# Skill Review: kavach-diagnose-scripts

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-24
**Skill location:** .claude/skills/kavach-diagnose

**Files reviewed:**
- SKILL.md
- scripts/list_failures.py
- scripts/triage_workers.py
- scripts/replay_workers.py
- scripts/validate_replay_receipts.py
- scripts/validate_kavach_contract.py
- scripts/append_fix_history.py
- scripts/record_history.py
- scripts/refresh_glossary.py
- scripts/review_verdicts.py
- scripts/failure_analyzer/__init__.py
- scripts/failure_analyzer/config.py
- scripts/failure_analyzer/main.py
- scripts/failure_analyzer/packet_utils.py
- scripts/config.json
- scripts/requirements.txt
- scripts/tests/*.py (13 test files)
- .claude/contracts/kavach-verdict.schema.json (external, but load-bearing for this skill)

## Issues

### Critical
None found.

### High
None found.

### Medium
None found.

### Low

**Severity:** Low
**Category:** Inconsistency
**Location:** `.claude/contracts/kavach-verdict.schema.json`, top-level (`"additionalProperties": true`) and `$defs.group` (`"additionalProperties": true`)
**Problem:** The schema permits arbitrary extra keys at both the document and group level. This is consistent with `validate_kavach_contract.py`'s own hand-rolled validator (which also never rejects unknown keys), so producer/consumer behavior matches — but it means neither layer catches a typo'd field name (e.g. `confidance` instead of `confidence`) silently coexisting with a missing/null real field.
**Impact:** A field-name typo introduced in a future edit to `validate_replay_receipts.py` would pass both the schema and the validator silently, and only surface as a missing-value symptom somewhere downstream (e.g. a `None`/default value flowing into a report) rather than a clear validation failure at the contract boundary.
**Recommendation:** Low priority given the intentional dependency-free design, but consider tightening `additionalProperties` to `false` at the group level once the field set stabilizes, or add a one-line comment in the schema explicitly noting this is a deliberate permissiveness choice mirrored in the validator (not an oversight) so a future reader doesn't "fix" one side without the other.
**Example:** N/A — optional hardening, not a required change.

## Overall Assessment

**Purpose and scope:** This is explicitly documented as a reference-only index over a shared script toolbox, not an invoked skill — its own frontmatter says so, and it is scoped correctly: deterministic, non-LLM machinery (extraction, triage, receipt validation, contract validation, fix-history bookkeeping) kept out of the three real LLM-driven skills (`failure-triage`, `live-replay-diagnosis`, `verdict-reporting`). That separation is appropriate and avoids duplicating procedure text across skills.

**Strengths:**
- `validate_kavach_contract.py` validates the **entire** `combined-receipts.json` document — top-level required keys, the full `groups` array, per-group required keys/enums/types, and cross-checks that `groupCount`, `summaryByGroup`, `affectedScenariosByVerdict`, `distinctIssuesByVerdict`, and `metrics.totalGroups` are actually consistent with what `groups` contains (not just present). An empty `groups: []` array is explicitly rejected with a clear message.
- The `confirmed_product_bug` evidence gate in `validate_replay_receipts.py` checks actual **values**, not just field presence: `productBugArtifacts` entries must clear a minimum length, fail a placeholder-phrase regex (`true`/`verified`/`done`/etc.), and match a signal regex requiring a real DOM/tool-call/quoted-value artifact — a fabricated prose sentence describing a check is explicitly tested and rejected (`test_fabricated_prose_artifact_without_dom_or_count_signal_is_downgraded`).
- `test_validate_kavach_contract.py` builds its fixtures by calling the *real* producer function (`combined_summary()`) and feeding the exact returned object into the *real* validator (`validate_document()`) — this is genuine producer/consumer integration testing, not hand-rolled fixtures that merely look plausible, and it includes a JSON round-trip test matching CI's actual write-then-read path.
- `test_kavach_verdict_schema_equivalence.py` keeps the (unexecuted, documentation-only) JSON Schema and the hand-rolled Python validator from silently drifting apart, by asserting their required-key sets and enums are identical.
- `append_fix_history.py` uses an exclusive `flock` for the whole read-modify-write and is idempotent against a resubmitted identical batch (deduped on `scenarioName`/`timestamp`/`groupId`), directly addressing a concurrent-CI-runs corruption risk.
- Malformed/tampered receipts are still caught even when they arrive on the "trusted" Tier-1 path: `combined_summary()` re-runs every Tier-1 receipt through the same `validate_receipt()` gate as Tier-2, which the tests explicitly exercise (`test_tier1_receipt_is_independently_revalidated_not_trusted_blindly`).

**Major concerns:** None rise above Low. The one Low-severity item above is a deliberate, documented design tradeoff rather than an oversight.

**Missing capabilities:** None identified relative to this skill's stated scope (deterministic script plumbing). Broader concerns about CI wiring (whether the full test suite and this validator actually run independently in `kavach.yml`) are out of scope for this skill-level review and are covered in the parent agent-level report.

**Overall quality rating:** Excellent — the specific gaps called out in the prior review round (schema validating only a row, presence-only evidence gates, and producer/consumer tests using unrealistic fixtures) are now demonstrably closed, with tests written to prove exactly those closures.

## Required Changes

None — this skill is ready as-is.

## Optional Improvements

1. Consider tightening `additionalProperties` on the JSON Schema (see Low finding above) once the field set is stable, purely as defense-in-depth against future typos — not required for correctness today.

## Final Verdict

**APPROVED**

The combined-receipts.json contract is now validated end-to-end as a whole document, the `confirmed_product_bug` evidence gate checks real artifact content rather than field presence, and the test suite directly exercises the real producer function against the real consumer validator — including empty, malformed, wrong-shaped, and internally-inconsistent documents. The single Low-severity finding is a documented, intentional permissiveness choice mirrored consistently on both sides of the contract, not a defect.
