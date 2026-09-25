# Skill Review: kavach-knowledge

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-25
**Skill location:** .claude/skills/kavach-knowledge

**Files reviewed:**
- SKILL.md
- append_fix_pattern.py (.claude/skills/kavach-diagnose/scripts/)
- test_append_fix_pattern.py (.claude/skills/kavach-diagnose/scripts/tests/)
- kavach-data/fix-patterns/*.md (13 files: _default.md + 11 feature files + studio-explorer-workspace.md)
- verdict-reporting/SKILL.md (cross-checked: Phase 5 fix-pattern write path)
- kavach-repair/SKILL.md (cross-checked: Phase 6 fix-pattern write path)

This is a re-review of a prior pass that found three issues. Each is verified below against current file content, plus the standard full-methodology pass.

## Prior findings — verification

1. **"already exists" → "has content yet" wording.** Fixed. `append_fix_pattern.py`'s docstring (lines 23–27) and `--feature-name` help text (lines 78–83), and this skill's Shape section (line 18), now consistently say "has no content yet," explicitly call out a tracked-but-empty placeholder as counting as no content, and state that path-existence alone doesn't mean a header was written.

2. **Header-name convention (raw stem vs. humanized).** Fixed. The Shape section (line 18) and the script's docstring/usage example (line 18: `--feature-name "Life_CampaignDashboard"`) now both specify the raw `.feature` file stem and explicitly disclaim the humanized form. The two real committed files with headers (`life-campaign.md` → `# Life_Campaign fix patterns`, `studio-explorer-workspace.md` → `# Studio_ExplorerWorkspace fix patterns`) match this convention.

3. **Supersession mechanism not wired into producers' procedures.** Fixed. Both `verdict-reporting/SKILL.md` (Phase 5, line 157) and `kavach-repair/SKILL.md` (Phase 6, "Fix-pattern update" section, line 439) now contain an actual step — run the dedup check, and on a same-locator/same-target-file match with a different fix expression, mark the earlier entry `**[superseded YYYY-MM-DD — see entry below]**` via direct edit before appending — not just a description living in this reference skill alone.

All three prior findings are resolved. However, the fuller pass below surfaced two new issues in the same family as #1 and #2 that the original pass didn't catch, because they live in the *other* two files (kavach-repair's own snippet, and the test suite) rather than in `append_fix_pattern.py` or this skill's own text.

## Issues

### Critical
None found.

### High
None found.

### Medium

**Severity:** Medium
**Category:** Bug (re-emergence of prior Bug #1, relocated)
**Location:** `kavach-repair/SKILL.md`, Phase 6, "Fix-pattern update" section (the `append_fix_pattern.py <feature-slug>` invocation, no `--feature-name` flag anywhere in that block)
**Problem:** `verdict-reporting/SKILL.md`'s Phase 5 invocation of `append_fix_pattern.py` always includes `--feature-name "<Feature Name>"` (harmless even when the file already has content, since the script only consumes it when the file is currently empty). `kavach-repair/SKILL.md`'s Phase 6 invocation of the same script, for the same purpose (recording a `script_issue_fix_applied` fix pattern), never includes `--feature-name` at all — not conditionally, not in any form.
**Impact:** kavach-repair can be the very first writer to touch a given feature's fix-pattern file — e.g. isolation mode applying a known pattern to a feature kavach-diagnose has never run live-replay on for. If that feature's file is a tracked-but-empty placeholder (9 of 13 files in `kavach-data/fix-patterns/` are exactly this), following kavach-repair's own documented command literally appends the block with `feature_name=None`, and per `append_block()`'s logic (`if not current.strip(): ... else: current = ""`) no header is ever written. Because the script never rewrites a file that already has content, this is permanent — a second, later writer to the same file can't retroactively add the header. This is functionally the same failure mode the prior review's Bug #1 described, just moved from the script's help text (now fixed) into one of the two producers that actually calls it.
**Recommendation:** Add the same conditional guidance kavach-repair already uses correctly elsewhere (mirroring `kavach-knowledge`'s and verdict-reporting's own wording) and include `--feature-name` in the example command.
**Example:**
```bash
echo "<the new dated block>" | \
  python3 .claude/skills/kavach-diagnose/scripts/append_fix_pattern.py <feature-slug> --feature-name "<raw .feature stem, e.g. Life_CampaignDashboard>"
```
plus a sentence identical in spirit to verdict-reporting's: "`--feature-name` only matters the first time this feature's file gets a header — always safe to pass; the script ignores it once the file has content. A tracked-but-still-empty placeholder counts as no content yet."

**Severity:** Medium
**Category:** Inconsistency
**Location:** `.claude/skills/kavach-diagnose/scripts/tests/test_append_fix_pattern.py`, `test_bootstraps_a_new_feature_file_with_header` (line 21) and `test_two_sequential_appends_both_survive` (line 55)
**Problem:** These tests pass `"Life Campaign Dashboard"` and `"Life PMP"` — the humanized form — as the `feature_name` argument, and the first test's assertion (line 24) explicitly checks the header renders as `# Life Campaign Dashboard fix patterns`. This is exactly the humanized convention that prior finding #2 flagged as wrong and that the SKILL.md/docstring fix now explicitly disclaims ("not a humanized form").
**Impact:** The test suite still demonstrates and locks in the deprecated convention. It doesn't break runtime behavior (the script is convention-agnostic about the string it's given), but tests are a form of documentation an engineer is likely to trust over prose — someone modifying this script and running the tests to "confirm" behavior sees the old, now-contradicted example passing green, undermining the "single documented convention" that #2's fix was meant to establish.
**Recommendation:** Update both test fixtures to use the raw stem convention, matching the rest of the codebase.
**Example:**
```python
append_block(path, "### 2026-09-25\n\n- fixed a stale locator", "Life_CampaignDashboard")
...
self.assertTrue(text.startswith("# Life_CampaignDashboard fix patterns\n\n## Run log\n"))
```
and similarly replace `"Life PMP"` with `"Life_PMP"` in the second test.

### Low

**Severity:** Low
**Category:** Inconsistency
**Location:** Shape section, first bullet — "one file per feature, including a pre-created empty placeholder reserved for a feature that has never yet needed a fix"
**Problem:** The actual `kavach-data/fix-patterns/` directory has 12 feature files (plus `_default.md`), but the repo has 43 `.feature` files across `life/`, `studio/`, `hcp/`, `api/`, and `e2e/`. Several Life features referenced by name in this very skill's own worked-examples table — `Life_CuratedMarket`, `Life_NPILists`, `Life_ReportTemplates` — have no placeholder file at all, and no Studio feature except `Studio_ExplorerWorkspace` has one; `hcp`/`api`/`e2e` features have none.
**Impact:** Low — `append_fix_pattern.py` auto-bootstraps a missing file on first write (`path.touch(exist_ok=True)`), so nothing breaks operationally. But the "one file per feature, including a pre-created placeholder" claim overstates what's actually maintained, and a reader relying on `ls kavach-data/fix-patterns/` to enumerate "features that have never needed a fix" (as `kavach-repair/SKILL.md`'s Reference files section explicitly says to do) will incorrectly conclude those un-listed features have never been fixed, when really no placeholder was ever created for them.
**Recommendation:** Either soften the claim ("a file per feature *once a pattern-relevant fix has been recorded or a placeholder created*—not every feature is guaranteed a file up front") or actually backfill the missing placeholders so the claim is accurate. The former is cheaper and matches reality; the latter matches the stated design intent.

## Overall Assessment

**Purpose and scope:** This skill is a narrow, well-bounded reference document — not a procedure — describing the shape, derivation algorithm, and read/write contract for a shared knowledge store used by two other skills. That scope is appropriate: it correctly refuses to duplicate the algorithm elsewhere (both `verdict-reporting` and `kavach-repair` point back to this file as the single source of truth) and stays out of the read/write procedures those two skills own.

**Strengths:**
- The `<feature-slug>` derivation algorithm is unusually precise for a prose spec (acronym-splitting rule, worked-examples table cross-checked against real feature files), and both consumer skills defer to it rather than re-implementing it.
- The dedup/supersession contract (Read/write contract section) gives a concrete, mechanical definition of "duplicate" (normalized locator + target file + fix expression) rather than leaving it to agent judgment — exactly the kind of actionability this review class looks for.
- All three previously-flagged issues are cleanly fixed at their original location, with matching language now present in both the script and the skill text.
- The lock-based concurrency design (`flock` held for the whole read-modify-write, mirroring `append_fix_history.py`) is correctly described and matches the script's actual implementation.

**Major concerns:**
- The Medium finding above (kavach-repair's Phase 6 omitting `--feature-name`) is the practical one to fix — it's a real path to a permanently headerless fix-pattern file, just relocated from where the prior review found it.
- The test suite still encodes the deprecated humanized-name convention, which will actively mislead the next person who touches this script.

**Missing capabilities:** None that are core to this skill's stated scope. A tool to audit `kavach-data/fix-patterns/` for orphaned or missing-placeholder files (referenced obliquely in the "prior version of this pipeline" paragraph as a past manual cleanup) doesn't exist as an automated check, but that's an improvement, not a gap in the skill's own responsibility.

**Overall quality rating:** Good — the reference content is precise and now internally consistent with the script it governs; the remaining issues are narrow, concrete, and cheap to fix, not structural.

## Required Changes

1. Add `--feature-name` (with the same "safe to always pass, ignored once content exists" guidance verdict-reporting uses) to `kavach-repair/SKILL.md`'s Phase 6 `append_fix_pattern.py` invocation.

## Optional Improvements

1. Update `test_append_fix_pattern.py`'s two humanized-name fixtures (`"Life Campaign Dashboard"`, `"Life PMP"`) to the raw-stem convention (`"Life_CampaignDashboard"`, `"Life_PMP"`) to match the now-standardized convention.
2. Soften or backfill the Shape section's "one file per feature, including a pre-created empty placeholder" claim so it doesn't overstate the current directory's actual coverage.

## Final Verdict

**APPROVED WITH CHANGES** — All three originally-flagged issues are genuinely fixed, and the skill's core content (derivation algorithm, dedup contract, supersession wiring) is solid and now internally consistent. The one Required Change (kavach-repair's Phase 6 missing `--feature-name`) is a real, if narrow, recurrence of the same headerless-file risk the prior review targeted, and should land before this is trusted to run unattended in a mixed diagnose/repair pipeline — but it's a one-line fix, not a structural problem.
