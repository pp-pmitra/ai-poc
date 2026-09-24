# Skill Review: verdict-reporting

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-24
**Skill location:** .claude/skills/verdict-reporting

**Files reviewed:**
- SKILL.md
- .claude/skills/kavach-diagnose/scripts/validate_kavach_contract.py
- .claude/skills/kavach-diagnose/scripts/validate_replay_receipts.py
- .claude/skills/kavach-diagnose/scripts/append_fix_history.py
- .claude/skills/kavach-diagnose/scripts/review_verdicts.py
- .claude/skills/kavach-diagnose/scripts/triage_workers.py
- .claude/skills/kavach-diagnose/scripts/replay_workers.py
- .claude/skills/kavach-diagnose/scripts/tests/test_append_fix_history.py
- .claude/contracts/kavach-verdict.schema.json
- .claude/skills/kavach-knowledge/SKILL.md (Shape section, referenced for slug derivation)
- .claude/skills/live-replay-diagnosis/SKILL.md (upstream producer of combined-receipts.json)
- .claude/skills/failure-triage/SKILL.md (upstream producer of the triage manifest)
- .claude/run-mixed-batch.sh
- .github/workflows/kavach.yml (concurrency gate)

## Issues

### Critical

None found.

### High

**Severity:** High
**Category:** Risk
**Location:** Phase 4, opening paragraph — "Read `kavach-data/history/triage-results/<triage-timestamp>/combined-receipts.json`..."
**Problem:** The skill has no instruction for what to do if this file is missing, unreadable, or fails `validate_kavach_contract.py`'s shape checks. Every upstream phase in the sibling skills (`list_failures.py`, `triage_workers.py`, `replay_workers.py`, `validate_replay_receipts.py`) has an explicit "stop and print `PHASE_SCRIPT_FAILED: ...`" convention for its own script failures. This skill — the one whose entire job is to guarantee a report always gets written — has no equivalent fallback for its own single input being absent or malformed.
**Impact:** If `combined-receipts.json` doesn't exist yet (wrong timestamp passed in, upstream skill crashed before writing it, disk/permissions issue) or is corrupt, the agent is left to improvise: it could fabricate a plausible-looking report from memory/chat context, silently produce nothing, or stall. Any of those defeats the pipeline's core invariant (stated elsewhere in this very file) that a run always ends with a real, traceable verdict file.
**Recommendation:** Add an explicit rule mirroring the sibling skills' convention, e.g.: "If the file does not exist, is not valid JSON, or `python3 .claude/skills/kavach-diagnose/scripts/validate_kavach_contract.py <path>` exits non-zero, stop immediately and print `PHASE_SCRIPT_FAILED: combined-receipts.json invalid: <reason>` — do not attempt to write a verdict report from partial or reconstructed data."
**Example:** Insert directly after the sentence ending "...that reconciliation is already done." in Phase 4.

---

**Severity:** High
**Category:** Risk
**Location:** Phase 5 — "pipe that array through `python3 .claude/skills/kavach-diagnose/scripts/append_fix_history.py`: it validates every entry ... (rejecting the whole batch, writing nothing, if any entry is malformed)"
**Problem:** The skill correctly explains that a malformed batch is rejected atomically, but never tells the agent what to do when that rejection actually happens (non-zero exit). There is no "stop and print X" instruction here, unlike every other script invocation in the Kavach pipeline.
**Impact:** A rejected batch means the entire run's history/bookkeeping — every scenario diagnosed this run — silently fails to be recorded in `fix-history.json`, while Phase 4's report file has already been written and looks complete. A human reading the report has no signal that the fix-pattern-cache / `attempts >= 2` logic in `failure-triage` will not see this run's results next time, since nothing was actually appended.
**Recommendation:** Add: "If `append_fix_history.py` exits non-zero, stop and print `PHASE_SCRIPT_FAILED: append_fix_history.py: <stderr>` — the verdict report is not considered complete until this step succeeds; do not proceed to Phase 6 (browser close) or claim the run finished cleanly."
**Example:** N/A — the fix is the added sentence above, placed immediately after the batch-rejection explanation.

### Medium

**Severity:** Medium
**Category:** Risk
**Location:** Whole-pipeline concern, but consumed directly by this skill's opening read of `combined-receipts.json` — see `triage_workers.py` line ~338 and `replay_workers.py` line ~208 (`out_dir = out_root / timestamp; out_dir.mkdir(parents=True, exist_ok=True)`)
**Problem:** `<triage-timestamp>` and `<replay-timestamp>` directories are named with second-granularity wall-clock timestamps and created with `mkdir(..., exist_ok=True)` — no uniqueness check, no PID/nonce suffix, no collision detection. Two runs whose `triage_workers.py`/`replay_workers.py` invocations land in the same wall-clock second write into the *same* directory, silently interleaving one run's `manifest.json`/packet/receipt files with another's. Since this skill treats `combined-receipts.json` as "the single source ... don't separately open the two underlying receipt directories or re-derive completeness by hand," it has no way to detect that the file it just read was actually contaminated by a second, unrelated run.
**Impact:** GitHub Actions' `concurrency: group: kavach-learned-patterns` (with `cancel-in-progress: false`) serializes CI-triggered runs, which substantially mitigates this for the one automated path — but it does not protect a manual/interactive invocation (`claude -p "/kawach"` run locally, or by a second engineer) that shares the same repo checkout as a CI run or another manual run. In that case, this skill could read a `combined-receipts.json` whose `groups` array is a hybrid of two unrelated diagnosis runs, and it would report that hybrid as if it were one coherent run's verdicts — a data-integrity problem this skill's own row-completeness reconciliation (matching against `failures-for-replay.json`) would not catch, since `failures-for-replay.json` itself could also have been overwritten by the second run.
**Recommendation:** This is primarily a fix for `triage_workers.py`/`replay_workers.py` (out of this skill's own file), but this skill should defensively guard its own read: before trusting `combined-receipts.json`, verify its `triageManifest`/`replayManifest` paths actually match the `<triage-timestamp>`/`<replay-timestamp>` this run itself produced (not just "the file that happens to exist at the expected path"), and stop with a clear error if they don't. Longer-term, the upstream scripts should append a short random suffix or PID to the timestamp directory name and fail loudly (not silently reuse) if the target directory already exists and is non-empty.
**Example:** "Before reading `combined-receipts.json`, confirm its `triageManifest` field's directory name equals the `<triage-timestamp>` this session's own Phase 2.5 call produced. If it differs, stop — do not report on a manifest this run did not generate."

---

**Severity:** Medium
**Category:** Risk
**Location:** Phase 5, footnote referencing `.claude/skills/kavach-diagnose/scripts/review_verdicts.py` for human spot-checking
**Problem:** `append_fix_history.py` takes an exclusive `flock` for its whole read-modify-write cycle specifically to prevent two concurrent writers from clobbering each other (its own docstring explains this was a known prior bug). `review_verdicts.py`'s `_load`/`_save` (used by its `mark` subcommand) do a plain read-whole-file / write-whole-file-back with **no locking at all**. `flock` is advisory — it only blocks other processes that themselves call `flock`. Since `review_verdicts.py` never does, a human running `review_verdicts.py mark ...` at the same time a `verdict-reporting` run's Phase 5 is appending new entries can lose the append: `review_verdicts.py` reads the pre-append file, `append_fix_history.py` writes its update under lock, then `review_verdicts.py` writes its (stale) in-memory copy back over it, silently dropping the entries the concurrent run just added.
**Impact:** A human doing routine calibration work (`review_verdicts.py mark "<scenario>" agreed`) while a CI run is finishing could silently erase that run's `fix-history.json` entries, with no error, no warning, and no way to detect it after the fact other than noticing the file is short a run's worth of entries.
**Recommendation:** Either note this hazard explicitly in `verdict-reporting`'s SKILL.md (so an operator knows not to run `review_verdicts.py mark` while a diagnosis run is in flight), or — better — fix `review_verdicts.py` to take the same exclusive `flock` around its own read-modify-write, matching `append_fix_history.py`'s pattern. Since this review is scoped read-only, the immediate ask is to document the hazard; the actual code fix is a follow-up.
**Example:** Add a line under Phase 5: "Do not run `review_verdicts.py mark`/`report` while a `verdict-reporting` run is in flight — it reads and rewrites `fix-history.json` without a file lock and can silently discard this run's just-appended entries."

---

**Severity:** Medium
**Category:** Inconsistency
**Location:** "Rules (this skill)" — "Run `graphify update .` after any applied fix so the graph stays current (relevant once kavach-repair has actually applied fixes from this report)."
**Problem:** This rule is listed under verdict-reporting's own "Rules (this skill)" section, but by its own parenthetical it never actually applies to anything verdict-reporting itself does — `verdict-reporting` never applies fixes (only `kavach-repair` does, in a later, separate run). A search of `kavach-repair`'s agent definition and the `kavach-knowledge`/`live-replay-diagnosis` skills turns up no matching `graphify update` rule anywhere else in the pipeline — so this instruction is both misplaced (it's a rule for a different skill, restated as if it belongs to this one) and, as far as this codebase shows, not actually present in the skill that would need to act on it.
**Impact:** An agent running verdict-reporting could reasonably wonder whether it is expected to run `graphify update .` itself during this run (since the rule sits in its own "Rules (this skill)" list), even though no fix was applied here — at minimum this wastes a beat of reasoning; at worst a literal-minded agent runs an unnecessary/no-op graphify update, or, more importantly, the actual owner of this responsibility (`kavach-repair`) never gets the rule at all, since it isn't written into that skill/agent's own file.
**Recommendation:** Remove this line from `verdict-reporting`'s Rules section and add the equivalent rule to `kavach-repair`'s own definition (`.claude/agents/kavach-repair.md`), where the action it describes actually happens.
**Example:** Delete the line from `verdict-reporting/SKILL.md`; add to `kavach-repair.md`: "After successfully applying and Maven-verifying a fix, run `graphify update .` so the graph reflects the changed file before the PR is raised."

---

**Severity:** Medium
**Category:** Risk
**Location:** Phase 4, timestamped report filename collision handling — "if a file at the computed path already exists, append `-2`, `-3`, … before the `.md` extension until the path is free"
**Problem:** This collision-avoidance is described as a check-then-write loop with no atomicity guarantee (unlike `append_fix_history.py`'s `flock`-protected write). Two agent sessions finishing in the same second, both checking "does `replay-verdict-<ts>.md` exist," could both see "no" and both write to the same path (or one to `-2` after a TOCTOU race), overwriting one report with another's content.
**Impact:** Lower likelihood given the CI concurrency gate serializes automated runs, but a real risk for two interactive/manual sessions run close together against the same checkout — the failure mode is a silently lost verdict report, which is exactly the kind of gap the rest of this skill goes out of its way to prevent (row completeness, blocked-reason banners, etc.).
**Recommendation:** Note that this filename-uniqueness check should use an atomic file-creation primitive (e.g. open with `O_CREAT|O_EXCL` semantics, or an equivalent "create if not exists, fail if it does" step) rather than a plain existence check followed by a separate write, when this skill's file-writing is eventually scripted rather than done by direct agent Write calls.
**Example:** N/A — documentation/robustness note, not a rewrite of running prose.

### Low

**Severity:** Low
**Category:** Improvement
**Location:** Throughout Phase 4 (e.g. "a `combined-receipts.json` row only carries `affectedScenarios`...")
**Problem:** The skill consistently calls entries in `combined-receipts.json` "rows," but the actual document (per `validate_kavach_contract.py` and `kavach-verdict.schema.json`) calls its top-level array `groups`, and each entry a "group" (with `groupId`, `scenarioCount`, etc.) — "row" doesn't appear anywhere in the schema or validator.
**Impact:** Minor only — the skill always establishes what it means by "row" in context, so it doesn't cause a wrong action. But a reader cross-referencing the schema/validator source while debugging a contract mismatch has to mentally translate "row" ↔ "group" every time, which slows down exactly the kind of investigation this review is being done to support.
**Recommendation:** Standardize on "group" (the schema's own term) when referring to `combined-receipts.json` entries, reserving "row" only for the verdict-report Markdown tables it produces (where "row" is the correct, natural term for a Markdown table line).
**Example:** Change "a `combined-receipts.json` row only carries `affectedScenarios`..." to "a `combined-receipts.json` group entry only carries `affectedScenarios`...".

---

**Severity:** Low
**Category:** Improvement
**Location:** "Section assignment" table header — "Machine value (`fix-history.json`; see note below for `combined-receipts.json`)"
**Problem:** The table is actually used to map values found in `combined-receipts.json` (that's the only file this skill reads at report-writing time, per Phase 4's own opening statement), yet the column header names `fix-history.json` first and treats `combined-receipts.json` as the exception requiring a footnote.
**Impact:** Purely a readability nit — the footnote does correctly resolve the ambiguity, so no one following the skill literally would get it wrong. But the ordering makes the reader work harder than necessary to figure out which file actually matters for the step they're on (writing the report, not writing history).
**Recommendation:** Flip the framing: title the column for `combined-receipts.json` values (the ones actually used here) and footnote the `fix-history.json`-only value (`script_issue_fix_applied`) as the exception, matching how the values are actually consumed in this phase.

## Overall Assessment

**Purpose and scope:** This skill has a tightly bounded, single responsibility: turn an already-reconciled `combined-receipts.json` into a human-readable verdict report, append machine-validated bookkeeping, clean up reproducible build artifacts, and release the held browser. It correctly delegates all actual diagnosis, fix-application, and low-level file-locking/validation to other scripts and skills rather than reimplementing them, which is the right shape for a "final stage" skill. The scope is appropriate — not too broad (it explicitly refuses to write "narrative/story" analysis), not too narrow (it owns the full verdict-report lifecycle including cleanup and browser teardown).

**Strengths:**
- The document-shape question this review was asked to focus on checks out: `combined-receipts.json`'s top-level `groups` array, as produced by `validate_replay_receipts.py`'s `combined_summary()`, is fully consistent with both `validate_kavach_contract.py`'s hand-rolled validator and `.claude/contracts/kavach-verdict.schema.json` — all three agree on the same required keys and verdict/tier/confidence enums, and a dedicated test (`test_kavach_verdict_schema_equivalence.py`) exists specifically to keep the schema and validator in lockstep.
- The Phase 5 cleanup instructions for build artifacts are genuinely careful about not racing a concurrent run: deriving `<replay-timestamp>` from *this run's own* `combined-receipts.json` (never "the most recent directory") is exactly the right defense against deleting another in-flight run's packet files, and the reasoning is explained, not just asserted.
- Row/scenario completeness is enforced mechanically and explained well: the skill tells the agent exactly how to recover a full scenario list when `affectedScenarios` is missing, and requires reconciling counts against `failures-for-replay.json` before finishing — this is a genuine, specific safeguard against the common QA failure mode of silently dropping failures from a report.
- `append_fix_history.py` is a well-designed, tested writer: atomic-under-lock, idempotent against retries (keyed on scenario+timestamp+groupId), and validates against the same enums the contract gate checks — the skill correctly refuses to let the agent hand-write `fix-history.json` directly, closing off a previously-real class of bug (per that script's own docstring).
- The `liveVerification: null` discipline (never fabricate proof a browser ran) is stated clearly, explained with *why* it matters ("a downstream reader trusts this field as proof"), and is independently re-checked by `append_fix_history.py`'s validator — defense in depth rather than a single point of trust.

**Major concerns:**
1. No failure-handling instruction for this skill's own two critical script calls (reading `combined-receipts.json`, and `append_fix_history.py` failing) — every sibling skill in the pipeline has an explicit "stop and print `PHASE_SCRIPT_FAILED`" convention; this skill, whose entire purpose is "always produce a report," is missing it for its own inputs/outputs.
2. Timestamp-based directory naming upstream (`triage_workers.py`/`replay_workers.py`) has no collision guard, and this skill has no defensive check that the `combined-receipts.json` it read actually belongs to the run it thinks it does. CI's concurrency gate covers the automated path; interactive/manual runs are not protected.
3. `review_verdicts.py`'s unlocked read-modify-write can silently clobber `append_fix_history.py`'s locked writes if run concurrently — a real, currently-undocumented data-loss hazard in a file this skill treats as durable, auditable history.
4. The orphaned `graphify update .` rule under "this skill's" own Rules section belongs to `kavach-repair`, not here — a small but real scope-boundary slip in an otherwise well-bounded skill.

**Missing capabilities:** The skill doesn't say what to do if the verdict report and `fix-history.json` writes both succeed but the subsequent `graphify`/PR-opening step (owned elsewhere) never runs — i.e., there's no mention of how a human or later automation would notice that Phase 4/5 completed but Phase 6 (browser close) or the outer workflow's PR step didn't. This may be intentionally out of scope (owned by `kavach.yml`), but it's not stated either way.

**Overall quality rating:** Good — the core mechanism (contract shape, completeness reconciliation, locked history writes, cleanup ordering) is sound and consistent with its downstream consumers, but two High-severity gaps in error handling for this skill's own critical path, plus a real (if narrow) concurrency hazard in a tool it hands off to, keep it from "Excellent."

## Required Changes

1. Add an explicit stop/print instruction (matching the pipeline's `PHASE_SCRIPT_FAILED` convention) for a missing, unreadable, or contract-invalid `combined-receipts.json` at the start of Phase 4.
2. Add an explicit stop/print instruction for `append_fix_history.py` exiting non-zero in Phase 5, and state that the run is not considered complete (and Phase 6 must not proceed) until it succeeds.
3. Document the `review_verdicts.py` unlocked-write hazard (do not run `mark`/`report` concurrently with an in-flight verdict-reporting run) until that script itself is fixed to take the same file lock as `append_fix_history.py`.

## Optional Improvements

1. Move the `graphify update .` rule out of this skill's "Rules (this skill)" section and into `kavach-repair`'s own definition, where the action it describes actually occurs.
2. Add a defensive check that `combined-receipts.json`'s `triageManifest`/`replayManifest` paths match the timestamps this run itself generated, as a guard against silently reading a directory another concurrent run wrote into.
3. Note that the report-filename collision-avoidance loop (`-2`, `-3`, …) should eventually use an atomic create-if-absent write rather than a check-then-write pattern.
4. Standardize on "group" (the schema's term) rather than "row" when describing `combined-receipts.json` entries, reserving "row" for the Markdown report tables.
5. Reframe the "Section assignment" mapping table to lead with `combined-receipts.json` values (what this phase actually consumes) rather than `fix-history.json` values, with the one `fix-history.json`-only exception footnoted.

## Final Verdict

**APPROVED WITH CHANGES**

The skill's core contract — the shape of `combined-receipts.json` it consumes, and its consistency with `validate_kavach_contract.py` and `kavach-verdict.schema.json` — is sound and well cross-checked by an equivalence test. Its cleanup and history-bookkeeping logic shows real, specific concurrency awareness (the replay-timestamp derivation rule, the locked/idempotent `append_fix_history.py` writer). What keeps this from an unconditional APPROVED is the absence of a defined failure path for its own two load-bearing operations — reading `combined-receipts.json` and writing `fix-history.json` — in a pipeline that otherwise enforces "stop and print" discipline everywhere else, plus a real (if narrow, and partially CI-mitigated) concurrency hazard in the timestamp-based artifact paths and in `review_verdicts.py`'s unlocked writes. None of these are fundamental design flaws; all three Required Changes are additive documentation/instruction fixes, not a rethink of the skill's structure.
