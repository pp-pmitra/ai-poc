# Skill Review: kavach-repair

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-25
**Skill location:** .claude/skills/kavach-repair

**Files reviewed:**
- SKILL.md
- .claude/agents/kavach-repair.md (consumer of this skill)
- .claude/skills/kavach-knowledge/SKILL.md (referenced dependency)
- .claude/skills/verdict-reporting/SKILL.md (cross-checked for the relocated graphify step and shared fix-history/fix-pattern contract)
- .claude/skills/kavach-diagnose/scripts/append_fix_history.py (referenced writer script)
- .claude/contracts/kavach-verdict.schema.json (existence check only)

This is a re-review. All four previously-raised Required Changes were checked against current file content rather than taken on faith. Three are confirmed fixed; the fourth (module-flag derivation) is fixed at its point of definition but the fix's own stated scope leaves two later phases inconsistent, which is reported below as a new issue in the same family.

## Issues

### Critical
None found.

### High

**1. The new Bash-scope restriction line contradicts later phases that explicitly write files via Bash**

- **Location:** TOOL USE line (top of file): *"Bash is for git, mvn, gh, and piping through `append_fix_history.py`/`append_fix_pattern.py` only — never use it to create, write, or move a file... as a substitute for the scoped Edit grant; if a change is needed outside the paths above, stop and report it as a blocker instead of writing it via Bash."*
- **Problem:** This sentence was added to fix the prior review's issue #4 (Bash must not substitute for the Edit grant), and it does that correctly for the source-file case. But its opening clause is written as an exhaustive list — "Bash is for git, mvn, gh, and piping ... **only**" — and that list does not include writing to `/tmp`. Two later phases do exactly that with Bash:
  - Phase 5: `cat > /tmp/kavach-repair-pr-body.md << 'EOF' ... EOF` to build the PR body.
  - Phase 0.5.2: `mvn test ... | tee /tmp/kavach-repair-isolation-run.txt` to capture the isolation observation run's output.
  Taken literally, both are "using Bash to create/write a file" outside the enumerated "git, mvn, gh, piping" list, and the same sentence's fallback clause ("if a change is needed outside the paths above, stop and report it as a blocker") would tell the agent to refuse and stop instead of completing Phase 5's PR body or Phase 0.5's log capture.
- **Impact:** An agent that applies this line literally could refuse to write the PR body file, blocking Phase 5 (PR creation) entirely — the skill's actual deliverable — or refuse to `tee` the isolation Maven output, breaking Phase 0.5.2's parsing step. This is the same class of self-contradiction the prior review flagged for the TOOL USE line's fix-history clause (list something as covered, then say it isn't), just relocated to a new sentence.
- **Recommendation:** Narrow the restriction to what it actually means — writes to files this skill's Edit grant is scoped over (`.feature`, `stepdefinitions/**`, `pages/**`, `kavach-data/fix-patterns/**`) — and explicitly carve out scratch/log writes to `/tmp` (Phase 5's PR body, Phase 0.5.2's tee target) as allowed, since they aren't a substitute for editing scoped source files at all.
- **Example:** *"Bash may run git, mvn, gh, and pipe through `append_fix_history.py`/`append_fix_pattern.py`, and may write scratch files under `/tmp` (the Phase 5 PR body, the Phase 0.5.2 Maven log). It must never create, write, or move a file inside the Edit-scoped paths (`.feature`, `stepdefinitions/**`, `pages/**`, `kavach-data/fix-patterns/**`, or any other repo source file) as a substitute for the scoped Edit grant — if a change is needed there, stop and report it as a blocker instead of writing it via Bash."*

**2. The root-pom/no-submodule `-pl <module>` omission rule is scoped to "Phases 2–3" but literal `-pl <module>` commands also appear in Phase 3.5 and Phase 4**

- **Location:** Phase 2 step 5 (module derivation) vs. Phase 3.5 (cascade check) and Phase 4 step 1 (spotless recheck).
- **Problem:** Phase 2 step 5 now correctly defines the root-level-pom.xml, no-submodule case (confirmed this repo's actual layout via `find . -name pom.xml`, which returns only `./pom.xml`) and says: *"omit `-pl <module>` entirely from every Maven command in **Phases 2–3**."* That scoping phrase is precise, and it is honored inside Phase 3 (3.2, 3.3). But two other command templates containing a literal `-pl <module>` fall outside that stated scope:
  - Phase 3.5: `mvn test -Dtest=TestRunner -Dcucumber.filter.tags="@<featureTag>" -pl <module> -q`
  - Phase 4 step 1: `mvn spotless:check -pl <module> -q`
  Neither phase repeats or cross-references the omission rule the way Phase 0.5.1 does (Phase 0.5.1 explicitly says "using the same rules as Phase 2 step 5 (including the root-pom.xml, no-submodule case: omit `-pl <module>` entirely)"). Phases 3.5 and 4 have no equivalent pointer.
- **Impact:** In this repo's actual layout (single root `pom.xml`, no submodule), every Maven invocation with a literal `-pl <module>` misfires (`mvn` errors with "no module named ... in project") unless the agent independently generalizes a rule that, as written, is scoped to two specific phases. This is exactly the failure mode the prior review's issue #1 described — it has been fixed at its point of definition, but two of the five phases containing the same literal template are left out of the fix's stated scope.
- **Recommendation:** Either broaden Phase 2 step 5's scope statement to cover every phase that contains a `-pl <module>` template (0.5.2, 3.2, 3.3, 3.5, 4.1), or add the same one-line cross-reference Phase 0.5.1 already uses to Phase 3.5 and Phase 4 step 1.
- **Example:** *"omit `-pl <module>` entirely from every Maven command this skill runs, in every phase (0.5, 2, 3, 3.5, 4) — not only Phases 2–3."*

### Medium
None found.

### Low

**1. Phase 6's "every field below is required" overstates what `append_fix_history.py` actually enforces**

- **Location:** Phase 6, "fix-history.json — one entry per candidate": *"Every field below is required by `append_fix_history.py`'s validation — an entry missing any of them is rejected."*
- **Problem:** `append_fix_history.py`'s `REQUIRED_ENTRY_KEYS` is 12 keys (`timestamp`, `runDate`, `scenarioName`, `featureFile`, `verdict`, `confidence`, `priority`, `groupId`, `isGroupRepresentative`, `attempts`, `analysisTier`, `review`). The JSON block in this section also shows `liveVerification` and `kavachRepairDetail`, neither of which the script actually requires or validates the shape of — it only conditionally checks `liveVerification` is null for non-live tiers, and doesn't touch `kavachRepairDetail` at all (extra keys are simply passed through).
- **Impact:** Purely cosmetic — omitting `kavachRepairDetail` would not actually be rejected by the script, contrary to what the sentence claims. Low risk of confusion only if someone debugs a rejected batch by cross-checking this claim against the script's actual behavior.
- **Recommendation:** Soften to something like: *"The keys marked below are validated by `append_fix_history.py` and rejected if missing; `kavachRepairDetail` and `liveVerification` are this skill's own convention, not separately enforced, but should still be populated for a complete audit trail."*

## Overall Assessment

**Purpose and scope:** The skill remains well-bounded — apply a proposed script fix, verify with Maven, commit, PR — and it still resists scope creep (explicitly refuses to diagnose, refuses live-replay evidence gathering, refuses headless/CI operation). A full skill is the right mechanism here given the number of state-dependent branches (three-run classification, cascade regression, isolation mode, prior-attempt skip rules) that a shorter prompt would reliably under-specify.

**Strengths:**
- All four previously-flagged Required Changes are substantively addressed: the root-pom case is now defined at its source, the cascade "on approval" branch now defines both re-run outcomes with distinct verdict/commit behavior, the TOOL USE line no longer self-contradicts about `fix-history.json`'s Edit scope, and Bash is now explicitly told not to substitute for the Edit grant (even though that new sentence itself needs the scope fix in High #1 above).
- The two smaller additions are both implemented correctly: the fix-pattern dedup/supersession check is now run before appending (matching `kavach-knowledge`'s own dedup criteria and supersession mechanism verbatim), and the new `graphify update .` step in Phase 4 is conditional (`if graphify-out/ exists ... skip silently if it doesn't`) — it does not assume a graph exists, and it makes more sense per-commit in kavach-repair's own Phase 4 than it did previously in verdict-reporting (confirmed verdict-reporting's SKILL.md no longer mentions graphify at all, so there's no duplication).
- The concurrency-safety story (exclusive-lock writers for both `fix-history.json` and fix-pattern `.md` files, explicit "never hand-edit" reasoning tied to a concrete race) is unusually well justified for an agent-facing document — it explains *why*, not just *what*.
- Extensive, concretely-worded tribal knowledge in Phase 3.1 (shadow-DOM CSS-over-XPath, JUnit `assertEquals` argument order, distinguishing session-degradation flakiness from genuine stale data) is the kind of guidance that generalizes rather than overfits to one incident, despite being drawn from specific past sessions.

**Major concerns:**
1. The newly-added Bash-scope sentence (High #1) risks blocking the skill's own Phase 5 PR-creation step and Phase 0.5.2's log capture if read literally — this is a regression introduced by the very fix meant to close the prior review's issue #4.
2. The root-pom omission rule (High #2) is correctly defined but under-scoped, leaving Phase 3.5 and Phase 4's own `-pl <module>` templates without an explicit "omit this too" pointer — a partial fix for the prior review's issue #1.
3. Everything else checked (cascade re-run outcomes, TOOL USE self-contradiction, fix-pattern dedup, graphify placement) is now correct and internally consistent.

**Missing capabilities:** None newly surfaced by this pass beyond the two High findings above — the skill's error-path coverage (infra-inconclusive retry, source-drift skip, spotless-gate revert, cascade depth cap, non-fast-forward push) remains thorough.

**Overall quality rating:** Good — the remediation pass genuinely fixed three of four prior issues cleanly and the fourth at its root definition, but introduced one new self-contradiction and left one fix's scope incomplete, both concrete and correctable without restructuring.

## Required Changes

1. Narrow the Bash-scope sentence in the TOOL USE line so it doesn't contradict Phase 5's `cat > /tmp/...` PR-body write and Phase 0.5.2's `tee /tmp/...` log capture (High #1).
2. Extend the root-pom/no-submodule `-pl <module>` omission rule's stated scope (or add an explicit cross-reference, as Phase 0.5.1 already does) to cover Phase 3.5 and Phase 4 step 1, both of which contain the same literal `-pl <module>` template (High #2).

## Optional Improvements

1. Soften Phase 6's "every field below is required" claim to reflect that `kavachRepairDetail`/`liveVerification` aren't enforced by `append_fix_history.py`'s schema check the way the 12 `REQUIRED_ENTRY_KEYS` are (Low #1).

## Final Verdict

**APPROVED WITH CHANGES.** All four originally-flagged High-severity issues are substantively resolved, and both smaller additions (fix-pattern dedup/supersession, relocated conditional `graphify update .`) are implemented correctly and consistently with their dependencies. However, the remediation pass introduced one new High-severity self-contradiction (the Bash-scope sentence vs. Phase 5/0.5.2's actual file writes) and left one of the four original fixes incomplete in scope (the root-pom omission rule doesn't reach Phase 3.5 or Phase 4's own `-pl <module>` templates). Both are narrow, mechanical fixes — no restructuring needed — but should land before this is trusted unattended in the exact single-module, root-pom repo layout it's meant to run against.
