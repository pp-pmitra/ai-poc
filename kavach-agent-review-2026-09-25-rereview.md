# Agent Review: Kavach (kavach-diagnose + kavach-repair) — Re-Review

## Review Metadata
- **Reviewed by:** Agent Reviewer (QA governance perspective)
- **Review date:** 2026-09-25 (re-review, same day as `kavach-agent-review-2026-09-25.md`)
- **Agent locations:** `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- **Intended runtime:** Claude Code CLI (`claude --agent ...`) — unattended in GitHub Actions (`kavach-diagnose` only), via `.claude/run-mixed-batch.sh` (`kavach-diagnose` only), and interactively by a human (`kavach-repair`, always)

This re-review verifies every finding in `kavach-agent-review-2026-09-25.md` against the code as it now stands, not against the intent of the fix. Two rounds of fixes were applied: the eight findings (AR-001 through AR-008) from the original agent-level review, and a further two concrete defects that the re-review's own skill-reviewer passes surfaced (a recurrence of the fix-pattern bootstrap bug in `kavach-repair`'s own Phase 6, and two internal scope inconsistencies introduced by the first round of fixes in `kavach-repair/SKILL.md`). All ten are re-verified resolved below.

## Files Reviewed
Same file set as the original review (agents, contracts, CI workflows, MCP config, CODEOWNERS, and all six Kavach skills), re-read against current content. `python3 -m pytest .claude/skills/kavach-diagnose/scripts/tests/ -v` was re-run directly: **220 passed, 3 subtests passed, 0 failed/skipped/errored.**

## Referenced Skill Reviews (re-run this round)

| Skill | Re-Review Verdict | Findings This Round | Status |
|---|---|---|---|
| kavach-diagnose (scripts) | Approved (carried forward; targeted re-check via full pytest run, not a full re-review) | None | No change needed — one static_classifier.py fix and two test additions verified green. |
| failure-triage | **Approved** | None | Strict-mode-violation dead-code/test-mismatch fully resolved: branch now escalates (`resolved: False, needsLiveReplay: True`), both unit tests rewritten to match, and a new `StrictModeViolationPipelineTests` integration test in `test_triage_workers.py` reproduces `run_triage()`'s actual gating logic end to end. |
| live-replay-diagnosis | **Approved** | None | `Bash(*)` containment gap now correctly documented as closed via `run-mixed-batch.sh`'s `verify_boundaries()`; "Demo app" wording generalized to Demo/Pre-release; `mcp__playwright__browser_navigate` naming fixed everywhere. |
| verdict-reporting | **Approved** | None (3 Low/optional items noted, none blocking) | `append_fix_pattern.py` failure contract added, Phase 6 labeling disambiguated, dead `graphify` rule removed, dedup/supersession check now instructed before append. |
| kavach-knowledge | Approved with changes → **fixed this session** | 1 new Medium finding: `kavach-repair`'s own Phase 6 fix-pattern append never passed `--feature-name`, reintroducing the same headerless-placeholder-file bug this round's fix closed everywhere else. | **Fixed**: `--feature-name` added to `kavach-repair/SKILL.md`'s Phase 6 invocation, with the same "always safe to pass" guidance verdict-reporting already uses. Two Low/optional items (humanized test fixtures, placeholder-coverage claim) also cleaned up. |
| kavach-repair | Approved with changes → **fixed this session** | 2 new High findings, both self-inflicted by the first round's own fixes: (1) the new Bash-scope sentence, read literally, would block Phase 5's PR-body write and Phase 0.5.2's log `tee`, both of which use `/tmp`; (2) the root-pom/no-submodule `-pl <module>` omission rule was scoped to "Phases 2–3" in its own wording, leaving Phase 3.5 and Phase 4's identical `-pl <module>` templates outside that stated scope. | **Fixed**: Bash-scope sentence now explicitly carves out `/tmp` scratch writes and names the actual Edit-scoped paths it must not substitute for; the omission rule's scope statement now reads "every phase (0.5, 2, 3, 3.5, 4) — not only Phases 2–3." |

No skill review this round surfaced a Critical or unresolved High finding. Every High/Medium finding raised by any of the five re-reviews was fixed within this same session, verified by direct file inspection (not taken on the sub-review's word), and the full Python test suite re-run green after the last edit.

## Dependency and Handoff Summary
Unchanged from the original review — see `kavach-agent-review-2026-09-25.md`'s diagram. No structural change was made to the pipeline topology; all fixes were corrections to existing procedure text, tool grants, and one dead-code branch, not new components or new handoffs.

## Critical Issues
None found, in this round or the original.

## Major Issues
None remain open. All Major findings from the original review (AR-001 through AR-005) and the two new High-severity findings this round (the kavach-knowledge recurrence and the kavach-repair self-contradictions) were fixed and independently re-verified against current file content within this session.

## Minor Issues

### AR-006 (carried forward, addressed) — `browser_run_code_unsafe` containment note
Fixed in the original round: `.claude/agents/kavach-diagnose.md`'s Tools and permissions section now states this tool is for read-only DOM/state inspection only. No further action.

### AR-007 (carried forward, explicitly out of scope) — Branch-protection enforcement is a GitHub settings check, not a repo-content fact
Unchanged from the original review: `.github/CODEOWNERS`'s own header comment still correctly states that the file alone does not turn on "Require review from Code Owners" for `main`, and that the placeholder team name needs replacing with real maintainers. This cannot be verified or fixed from repository contents — it requires a human to check GitHub's Settings → Branches page directly. It is noted here as an accepted residual risk per the user's explicit instruction, not a blocker.

### AR-008 (carried forward, addressed) — Bash-as-write-substitute constraint
Fixed in the original round for `kavach-repair`, then refined again this round (see kavach-repair's re-review row above) after the first fix's wording turned out to be over-broad. The current wording (verified directly): *"Bash may run git, mvn, and gh; pipe through `append_fix_history.py`/`append_fix_pattern.py`; and write scratch files under `/tmp`... It must never create, write, or move a file inside the Edit-scoped paths... as a substitute for the scoped `Edit` grant."* This is now internally consistent with Phase 5's `/tmp` PR-body write and Phase 0.5.2's `/tmp` log capture.

## Overall Assessment

### Role and Scope
Unchanged and still correct: `kavach-diagnose` diagnoses and never fixes, `kavach-repair` fixes and never diagnoses, and neither agent definition duplicates procedure that belongs in its preloaded skills.

### Agent-versus-Skill Separation
Unchanged and still correct.

### Contracts and Handoffs
Unchanged and still strong — the `combined-receipts.json` contract, its whole-document validation, and the producer/consumer integration test were not touched this round and remain verified sound from the original review.

### Tools, Permissions, and Security
Materially improved this round. `kavach-diagnose.md`'s own `tools:` list is now the accurate, CODEOWNERS-reviewed source of truth for its runtime permissions (AR-005 fixed). `.claude/run-mixed-batch.sh` now has a real, preventive-adjacent boundary check (`verify_boundaries()`) for the one invocation path that previously had none at all (AR-001 fixed). `kavach-repair`'s Bash-scope constraint is now both real and internally consistent with the skill's own procedure (the two self-inflicted contradictions from the first round's fix are resolved).

### Failure Handling and Operability
Materially improved this round. The fix-pattern bookkeeping path (`append_fix_pattern.py`) now has a defined failure contract in `verdict-reporting` (AR-003) and a defined, non-headerless bootstrap behavior against this repo's actual committed placeholder files, correctly wired into *both* producers that write to it (`verdict-reporting` and, after this session's additional fix, `kavach-repair`'s own Phase 6). `kavach-repair`'s core Maven-invocation procedure (AR-002) now has a defined outcome for this repository's actual single-module, root-`pom.xml` layout, applied consistently across every phase that contains a `-pl <module>` template, not only some of them.

### Strengths
All strengths noted in the original review still hold (evidence-value gating, document-level contract validation, CI job-splitting, pinned dependencies). Additionally: the fix cycle for this review surfaced and closed a real, previously-undetected recurrence of the fix-pattern bootstrap bug in a second call site — evidence that the re-review's independent verification (rather than trusting the first round's own claim of "fixed") was worth doing once, concretely, rather than being repeated indefinitely.

### Overall Quality Rating
**Excellent.** Every Critical, Major, and High finding raised across the original review and this round's five independent skill re-reviews is now resolved and independently re-verified against current file content, with the full test suite passing. The system is internally consistent: agent-declared tool grants match what CI and the manual script actually invoke, the Maven-invocation procedure matches this repository's real build layout in every phase that needs it, and the shared fix-pattern/fix-history bookkeeping contract is now correctly wired into both of its writers rather than only one.

## Required Changes Before Approval
None remain.

## Optional Improvements
1. Confirm in GitHub's repository settings (not in this codebase) that "Require review from Code Owners" and required status checks are enabled on `main`, and that the CODEOWNERS placeholder team has been replaced with real maintainers (AR-007) — the one item this review has never been able to verify from files alone, in either round.
2. `verdict-reporting`'s three Low-severity polish items (the `featureFile` "verbatim" wording, a callout distinguishing the report table's bare filename from `fix-history.json`'s full path, and a `VALID_PRIORITIES` enum check in `append_fix_history.py`) — cosmetic, no behavioral risk.
3. `kavach-knowledge`'s remaining Low item (the "one file per feature that has ever needed a fix" claim overstates actual placeholder coverage across the repo's 43 feature files) — cosmetic, `append_fix_pattern.py` auto-bootstraps a missing file regardless.

## Final Verdict

**Approved**

No Critical or Major findings remain, in this review or across any of the five skill re-reviews run this round. Every finding raised — including two new ones this round's independent verification surfaced, both concrete regressions introduced by the first round's own fix rather than anything missed originally — was fixed and re-verified directly against current file content and a passing full test suite (220 tests, 3 subtests, 0 failures) before this verdict was issued. What remains is a single item outside this review's reach (confirming GitHub's branch-protection settings are actually turned on, which no file in the repository can prove) and a handful of Low-severity, non-blocking polish items. This is suitable to present as the approved verdict for the record.
