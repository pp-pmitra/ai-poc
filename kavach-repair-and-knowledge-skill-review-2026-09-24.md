# Skill Review: kavach-repair

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-24
**Skill location:** `.claude/skills/kavach-repair`

**Files reviewed:**
- SKILL.md
- `.claude/agents/kavach-repair.md`
- `.claude/contracts/kavach-verdict.schema.json`
- `.claude/skills/kavach-knowledge/SKILL.md` (as referenced dependency)
- `.claude/skills/verdict-reporting/SKILL.md` (as referenced dependency)
- `.claude/skills/kavach-diagnose/scripts/append_fix_history.py` (as referenced dependency)
- `src/test/java/hooks/Hooks.java` (referenced by name, `hooks.Hooks.saveFailureArtifacts`)
- `src/main/java/utils/LocatorProbe.java` (referenced by name — does not exist)
- `.github/workflows/kavach.yml`

## Issues

### Critical

**1. Severity:** Critical
**Category:** Bug
**Location:** Phase 6, "fix-history.json — one entry per candidate"
**Problem:** kavach-repair's own procedure never instructs the agent to write `fix-history.json` through `append_fix_history.py`. It shows a raw JSON object and says to append it, and the agent (per `.claude/agents/kavach-repair.md`) is granted direct `Write(kavach-data/history/fix-history.json)` and `Edit(kavach-data/history/fix-history.json)` tool access — i.e. hand-editing the file is not just possible but is the literal path the skill describes. This directly contradicts the sibling `verdict-reporting` skill (Phase 5), which states in as many words: "never hand-write `fix-history.json` directly (read-whole-file/append-in-memory/write-whole-file-back has no schema check and no protection against two runs finishing close together clobbering each other's writes)," and mandates piping every batch through `append_fix_history.py`. Compounding this, the JSON template shown in kavach-repair's Phase 6 is missing four of the twelve keys `append_fix_history.py` requires (`confidence`, `priority`, `isGroupRepresentative`, `review`) — a hand-written entry built from this template would fail that script's validation if it were ever run through it.
**Impact:** This is the exact concurrent-writer scenario the file's own locking mechanism (`fcntl.flock`) was purpose-built to prevent, left open on one of the two writers. A kavach-repair run and a kavach-diagnose/verdict-reporting run finishing close together can race on a read-modify-write of the same file with no lock on kavach-repair's side — flock only serializes cooperating lock-holders, so an unlocked writer can freely interleave and silently drop the other side's entries. Even without a race, every fix-history entry kavach-repair writes is schema-incomplete (missing `confidence`/`priority`/`isGroupRepresentative`/`review`), silently breaking downstream consumers that assume the schema `append_fix_history.py` enforces (`review_verdicts.py`, the `≥2`-attempts skip logic, any future contract validation of this file).
**Recommendation:** Change Phase 6 to build the same JSON array shape `verdict-reporting` uses (adding the four missing keys — `confidence` from the receipt, `priority` derived the same way verdict-reporting derives it, `isGroupRepresentative: true` since kavach-repair operates per-candidate, and `review: {"status": "unreviewed", "reviewedAt": null, "note": null}`), then pipe it through `python3 .claude/skills/kavach-diagnose/scripts/append_fix_history.py` exactly as verdict-reporting does, instead of writing the file directly. Narrow the agent's tool grant from `Write`/`Edit` on `fix-history.json` to only what invoking the script via `Bash` requires (the script itself needs filesystem write access, but the *agent's own* Edit/Write tool should not target that path directly, to close off the hand-write path entirely).
**Example:**
```bash
# Phase 6, replace direct Write/Edit of fix-history.json with:
cat <<'EOF' | python3 .claude/skills/kavach-diagnose/scripts/append_fix_history.py
[
  {
    "timestamp": "...", "runDate": "...", "scenarioName": "...",
    "featureFile": "...", "verdict": "script_issue_fix_applied",
    "confidence": "high", "priority": "high_confidence_script_fix",
    "groupId": "...", "isGroupRepresentative": true, "attempts": 1,
    "liveVerification": null, "analysisTier": null,
    "review": {"status": "unreviewed", "reviewedAt": null, "note": null},
    "imaintenanceDetail": { "...": "..." }
  }
]
EOF
```

**2. Severity:** Critical
**Category:** Bug
**Location:** Tool-use preamble — `src/main/java/utils/LocatorProbe.java is a Maven-only diagnostic helper`; Phase 3.1 "Use `utils.LocatorProbe` for live disambiguation"; agent file line 38
**Problem:** `LocatorProbe.java` does not exist anywhere in this repository (verified: no file at `src/main/java/utils/LocatorProbe.java`, no class of that name anywhere under `src/`). The skill states its existence as settled fact, not as something to create or check for. Even if the agent tried to create it, its Edit/Write grants (`.feature`, `src/test/java/stepdefinitions/**`, `src/main/java/pages/**`, plus the two `kavach-data` paths) do not cover `src/main/java/utils/**`, so the agent has no permission to create it either.
**Impact:** Phase 3.1's live-disambiguation guidance — a load-bearing part of the "diagnose from actual failure artifacts before guessing again" safeguard — points at a tool that cannot be invoked and cannot be built by this agent. An agent following the instructions literally will either stall trying to locate/import a nonexistent class, or silently skip the guidance without flagging that the skill's own tooling is missing, in either case eroding the deliberate anti-guessing design of Phase 3.1.
**Recommendation:** Either add `LocatorProbe.java` to the repo (and grant kavach-repair's agent `Edit`/`Write` on that specific file, not all of `utils/**`) before shipping this skill, or rewrite Phase 3.1 to drop the `LocatorProbe` references and describe live disambiguation using tools the agent actually has (e.g., a temporary inline probe statement added to the page-object method under test, run through the existing three-run Maven verification, removed before commit).
**Example:** If keeping the reference, add a guard at the top of Phase 3.1: "Before using `LocatorProbe`, confirm `src/main/java/utils/LocatorProbe.java` exists (`ls src/main/java/utils/LocatorProbe.java`). If missing, skip probe-based disambiguation and note `LocatorProbe unavailable` in the candidate's outcome instead of guessing."

### High

**3. Severity:** High
**Category:** Risk
**Location:** Phase 3.1, "A Maven test failure captures three things automatically (via `hooks.Hooks.saveFailureArtifacts`, if the target repo's hooks include it)"
**Problem:** The actual `Hooks.java` in this repo has no `saveFailureArtifacts` method and does not write `page-source.html`, `screenshot.png`, or `failure-context.json` to `target/failure-artifacts/<scenario-slug>/`. It attaches a screenshot and a Playwright trace to the Cucumber report on failure — a different mechanism entirely. The skill does hedge with "if the target repo's hooks include it," so this isn't a flat contradiction, but no fallback instructions exist for the case where they don't — which is this repo's actual, current state.
**Impact:** Phase 3.1's core "read both the DOM and the screenshot before proposing a fix — never DOM-only" rule, along with the detailed worked examples built on it (the "Delete confirmation" dialog story, the multi-branch-locator warning), has no artifacts to act on in this repo today. An agent following the instructions literally will look for files that don't exist, and the skill gives no guidance on what to fall back to (e.g., the trace `.zip` and Cucumber-attached screenshot that this repo's hooks actually produce).
**Recommendation:** Add an explicit fallback: check for `target/failure-artifacts/<scenario-slug>/` first; if absent, fall back to the trace zip and Cucumber-report-attached screenshot this repo's `Hooks.java` actually produces (`target/trace_<scenario>.zip`, extractable with `npx playwright show-trace` or `unzip`), and only skip DOM/screenshot inspection entirely — noting so explicitly in the candidate's outcome — if neither is available.
**Example:** "If `target/failure-artifacts/<scenario-slug>/page-source.html` is absent, check for `target/trace_<scenario-slug-pattern>.zip` (this repo's actual failure-capture mechanism per `hooks.Hooks.takeScreenshotAndTrace`) before concluding no failure evidence exists."

### Medium

**4. Severity:** Medium
**Category:** Inconsistency
**Location:** Throughout (branch name `imaintenance/<run-date>`, temp file `/tmp/imaintenance-pr-body.md`, log line `Resuming existing branch imaintenance/...`, isolation-run temp file `/tmp/imaintenance-isolation-run.txt`)
**Problem:** The skill is named and branded `kavach-repair` (per its own frontmatter, the agent file, and the recent repo history renaming `kavach-imaintain` → `kavach-repair`), but every branch, PR title component, and temp artifact it creates still uses the old `imaintenance` name.
**Impact:** Not a functional bug, but a real discoverability/audit cost: a reviewer looking at open PRs or branches for "kavach" activity won't find kavach-repair's work by name, and a future maintainer grepping the repo for `kavach-repair` to find its git artifacts will miss all of them.
**Recommendation:** Rename the branch prefix and temp file names to match the current skill name (`kavach-repair/<run-date>`, `/tmp/kavach-repair-pr-body.md`, etc.) in one pass, consistently across all six phases.

**5. Severity:** Medium
**Category:** Risk
**Location:** Phase 3.3, three-run classification table
**Problem:** The "any `maven-fail`" rule means a candidate that passes 2 of 3 runs is still classified `test_failed`, i.e., all three runs must complete regardless of an early failure. This is stated correctly and is reinforced by the "Never apply a fix without completing all three Maven verification passes" rule, closing the obvious time-pressure shortcut (stopping after the first failure). No bug here — but the skill never explains *why* all three runs must still complete once one has already failed (e.g., to confirm consistent failure at the same step for the "same step vs. shifted step" logic immediately below). A bare imperative rule without the reasoning is more likely to be quietly shortened under real time pressure than one that explains its own purpose.
**Impact:** Low probability given the explicit "Never" rule, but if an agent under a tight budget rationalizes stopping after run 1 fails (since the three-run table's outcome for "test_failed" is already determined), the "same step / new step" diagnosis in the very next paragraph loses a data point it may have needed.
**Recommendation:** Add one sentence explaining why all three runs are still required after an early failure — to confirm the failure is deterministic at the same step (vs. an intermittent pass) before deciding whether to record `test_failed` and whether the failure "shifted."

### Low

**6. Severity:** Low
**Category:** Improvement
**Location:** Phase 2 step 3, Rules section
**Problem:** The verdict-eligibility check (only `verdict == "script_issue_fix_proposed"` receipts become candidates, Phase 1 step 1) and the "never guess from `recommendedAction` alone" rule (Phase 2 step 3) are both correctly and unambiguously stated exactly once each, in the right place — this is a genuine strength, not an issue, but it's worth calling out explicitly since it's one of the two things this review was specifically asked to check.
**Impact:** None — positive finding.
**Recommendation:** None needed.

## Overall Assessment

**Purpose and scope:** kavach-repair is a well-bounded, single-responsibility skill: apply a known-good pattern to a specific, pre-diagnosed failure, verify it mechanically, and hand off to a human via PR. It correctly refuses to diagnose (that's kavach-diagnose's job) and correctly refuses to guess from free text alone. The scope is appropriate for a skill whose actions touch production test source and must be trustworthy under human review.

**Strengths:**
- The verdict allow-list (`script_issue_fix_proposed` only) and the "no patch without a pattern-file match" rule are both unambiguous, stated in exactly one place, and reinforced by the isolation-mode path following the identical rule — this is the strongest part of the skill and directly answers this review's first two focus questions with a clean "yes, correctly gated."
- The three-run Maven verification and cascade-regression check are stated as absolute "Never" rules with no environment-variable escape hatch, and the non-interactive abort check runs before mode detection — genuinely hard to bypass under time pressure or ambiguous instructions, answering this review's third focus question cleanly.
- Push/PR discipline is exemplary: never merges its own PR, refuses force-push under any circumstance with an explicit stop-and-manual-resolve path, and the agent file states plainly "A human reviews and merges its PR." No contradiction found anywhere — this answers the fourth focus question cleanly.
- The `PUSH_REJECTED` handling, same-day branch resumption, and idempotent "check for existing PR before creating" logic show real thought about re-run safety.

**Major concerns:**
1. Fix-history writing (Critical #1) bypasses the very locking/validation mechanism its sibling skill treats as mandatory, on the exact same shared file — this is the concurrency risk this review was asked to specifically check for, and it is real, not hypothetical.
2. `LocatorProbe` (Critical #2) is a dangling reference to a nonexistent class the agent also lacks permission to create.
3. The `hooks.Hooks.saveFailureArtifacts` failure-artifact path (High #3) doesn't match this repo's actual `Hooks.java`, leaving a core anti-guessing safeguard without a working fallback.

**Missing capabilities:** No guidance on what to do if `gh pr create` itself fails for a reason other than a duplicate PR (e.g., missing `gh` auth, repo permission issues) — Phase 5 only handles the "already open" and "push rejected" cases. No guidance on a maximum candidate batch size or run-time budget, which matters for an interactive skill a human is expected to sit through.

**Overall quality rating:** Adequate. The governance-critical properties this review was commissioned to check (verdict gating, pattern-only patch derivation, unskippable verification, no self-merge/no bypass) are all genuinely solid. But two Critical bugs — one of which reintroduces the exact data-corruption race the project already solved once for the sibling skill — mean this is not yet production-ready as written.

## Required Changes

1. Route all `fix-history.json` writes in Phase 6 through `append_fix_history.py` (as verdict-reporting already does), and complete the JSON entry with the four schema fields it currently omits (`confidence`, `priority`, `isGroupRepresentative`, `review`). Narrow the agent's direct `Write`/`Edit` grant on `fix-history.json` accordingly. (Critical #1)
2. Resolve the `LocatorProbe` dangling reference — either ship the class and grant the narrow file permission it needs, or rewrite Phase 3.1 to not depend on it. (Critical #2)
3. Add a documented fallback for failure-artifact inspection in Phase 3.1 for repos (including this one) whose `Hooks.java` doesn't implement `saveFailureArtifacts`. (High #3)

## Optional Improvements

1. Rename `imaintenance/*` branch and temp-file naming to `kavach-repair/*` for consistency with the current skill name. (Medium #4)
2. Add one explanatory sentence on why all three Maven runs must complete even after an early failure. (Medium #5)
3. Add explicit handling for a `gh pr create` failure unrelated to a duplicate PR.
4. Add an explicit batch-size or time-budget note for interactive sessions.

## Final Verdict

**APPROVED WITH CHANGES**

The properties this review was specifically commissioned to verify — verdict gating restricted to `script_issue_fix_proposed`, patches derived only from a matched fix-pattern file, an unskippable three-run-plus-cascade verification gate, and a clean no-self-merge/no-bypass boundary — are all genuinely well-designed and hold up under simulated execution. However, two Critical bugs disqualify this from shipping as-is: Phase 6's fix-history write path silently reintroduces the exact concurrent-writer corruption risk the project already fixed once (via `append_fix_history.py`'s locking) for the sibling `verdict-reporting` skill, and Phase 3.1 depends on a Java helper class that does not exist in the repository and that the agent has no permission to create. Both are narrow, mechanical fixes (route through the existing script; either ship the class or rewrite the paragraph) rather than structural problems, so a rewrite of the whole skill is not warranted — but neither should ship un-patched.

---

# Skill Review: kavach-knowledge

**Reviewed by:** Skill Reviewer (QA perspective)
**Date:** 2026-09-24
**Skill location:** `.claude/skills/kavach-knowledge`

**Files reviewed:**
- SKILL.md
- `kavach-data/fix-patterns/_default.md` (sample content)
- `kavach-data/fix-patterns/life-campaign-dashboard.md` (sample — empty)
- `.claude/skills/kavach-diagnose/scripts/failure_analyzer/triage/fix_pattern_cache.py` (as referenced dependency, for the "not the cache" disambiguation)
- `.claude/skills/verdict-reporting/SKILL.md` (as the other writer, Phase 5)
- `.claude/skills/kavach-repair/SKILL.md` (as the other reader/writer)

## Issues

### Critical
None found.

### High

**1. Severity:** High
**Category:** Risk
**Location:** "Read/write contract" section — describes kavach-diagnose (verdict-reporting Phase 5) and kavach-repair (Phase 6) both appending to the same per-feature `.md` file, with no mention of any concurrency control
**Problem:** This skill describes exactly the same dual-writer topology as `kavach-data/history/fix-history.json` — two independent agents/skills reading, then appending to, the same file on disk. For `fix-history.json`, the project deliberately solved this with `append_fix_history.py`'s exclusive `flock` plus schema validation, and documents the race it was built to prevent ("two runs finishing close together could lose one's writes to a read-modify-write race"). For the `.md` fix-pattern files described in this skill, no equivalent mechanism is mentioned anywhere — appending is described purely as an editing convention (read the file, append a block, mark superseded entries in place), with no lock, no atomic write, and no script mediating it.
**Impact:** If a kavach-diagnose run's `verdict-reporting` Phase 5 and a kavach-repair Phase 6 append to the same feature file close together (plausible if a human runs kavach-repair against a checkout that a concurrent or just-finished CI diagnosis run also touched), a plain read-modify-write on both sides can lose one side's entry with no error, no conflict marker, and no way to detect it later — silently discarding a known-good fix pattern or a newly-learned dismiss/wait hardening note. This directly undermines the stated purpose of the file ("the one part of Kavach's output that is never disposable").
**Recommendation:** Either extend `append_fix_history.py`'s pattern (or a lightweight analog) to cover appends to these `.md` files — even a simple `flock`-guarded append-only writer would close the gap — or, if a full script is judged not worth the cost for prose content, explicitly document the mitigating assumption this design actually relies on (e.g., "in practice these two writers never touch the same feature file inside the same few-second window because kavach-repair is always interactive and human-paced, while kavach-diagnose runs to completion in CI before a human would ever invoke kavach-repair against the same run's output") so a future reader can judge whether that assumption still holds rather than discovering the gap the hard way.
**Example:** A minimal fix: before appending, `git diff --quiet -- <feature-slug>.md` to detect any uncommitted changes made since the file was last read, and re-read/re-check for the entry before writing if the file changed underneath — a cheap, no-new-script way to at least detect (if not fully prevent) the race.

### Medium

**2. Severity:** Medium
**Category:** Risk
**Location:** "Dedup criteria" — "check whether an existing entry already describes the same broken locator/pattern *with the same fix*"
**Problem:** Unlike the machine-checkable dedup key `append_fix_history.py` uses for `fix-history.json` (`scenarioName`, `timestamp`, `groupId` tuple), dedup here is entirely a semantic judgment call left to whichever agent is appending — "the same broken locator/pattern" and "the same fix expression" have no defined comparison rule (exact string match? semantic equivalence? same file:line?).
**Impact:** Two different agents (or the same agent on two different runs) can reasonably disagree about whether a new observation is "the same" as an existing entry, leading to inconsistent behavior: one run treats a reworded description of the same fix as a duplicate and skips it, another appends it as new, growing the file with near-duplicates that then need the human pruning pass mentioned under "Growth."
**Impact (cont'd):** This isn't fatal since the file is explicitly append-only and human-reviewed via PR, but it does mean the file's growth rate and cleanliness depend on how consistently different LLM invocations apply an undefined judgment call.
**Recommendation:** Give at least one concrete anchor for "same" — e.g., "same normalized locator string (whitespace/quote-style differences ignored) and same target file" — so two different agents applying the rule converge more often, even if a fully mechanical check isn't practical for the fix-expression side.

**3. Severity:** Medium
**Category:** Documentation
**Location:** "Why this survives across CI runs" section, final paragraph
**Problem:** The disambiguation note ("this is not the fix-pattern cache mechanism... reads `fix-history.json` directly") is accurate and a good catch by the skill's author, but the `fix_pattern_cache.py` module it points to contains a stale comment of its own ("Reads `fix-history.json` directly rather than `.claude/fix-patterns/*.md`") using the wrong path (`.claude/fix-patterns/` instead of the real `kavach-data/fix-patterns/`, which this very skill defines as canonical in its Shape section).
**Impact:** Minor — a reader chasing the cross-reference from this skill into the script could be briefly confused by the wrong path in the script's own docstring, though it doesn't affect behavior since the script never actually reads that path.
**Recommendation:** Fix the stale path in `fix_pattern_cache.py`'s module docstring to `kavach-data/fix-patterns/*.md` while touching this area next (outside kavach-knowledge's own file, but worth flagging since this skill explicitly cross-references that script by name).

### Low

**4. Severity:** Low
**Category:** Improvement
**Location:** Shape section, slug algorithm
**Problem:** The slug-derivation algorithm is unusually thorough — worked examples, a documented history of the under-specified version it replaced, and explicit instructions for handling orphaned files — which is good, but it is fully duplicated by reference (not by copy, correctly) in `kavach-repair` and `verdict-reporting`. This is the right pattern (single source of truth), just worth noting as a strength rather than leaving unremarked.
**Impact:** None — positive finding, no action needed.
**Recommendation:** None.

## Overall Assessment

**Purpose and scope:** This is a reference/data-contract skill, not a procedure — correctly scoped as "the one artifact both agents read and write" and deliberately narrower than the full `kavach-data/history/` bookkeeping. The single-responsibility boundary (pattern library only, not fix-history, not verdict reports) is well drawn and consistently respected by both the skill's own text and its actual callers.

**Strengths:**
- The slug-derivation algorithm is genuinely excellent: a single, precisely specified, example-verified algorithm that every consumer defers to by reference, with an honest account of the prior under-specified version's failure mode (orphaned files) and how to detect/fix a recurrence. This is exactly the kind of shared, deterministic logic that belongs in one place.
- The explicit "this is not the cache" disambiguation from `fix-history.json`/`fix_pattern_cache.py` is a genuinely useful piece of institutional memory that prevents a very plausible confusion (two similarly-named "known fix" mechanisms) for a future maintainer.
- The append-only + "mark superseded, never delete" convention is a sound design for a knowledge base meant to survive across runs and be human-auditable in a PR diff.

**Major concerns:**
1. No concurrency protection for the shared `.md` pattern files despite the identical dual-writer topology that `fix-history.json` was explicitly re-architected to protect against (High #1) — this is the central question this review was asked to check, and the answer is "not currently protected, and not currently acknowledged as a risk in the skill text."
2. Dedup criteria are undefined enough that two agents could reasonably diverge (Medium #2).

**Missing capabilities:** No guidance on what happens if two entries for the same feature end up genuinely contradicting each other (not just "superseded" but actively conflicting, e.g., from a bad merge) — only the single-writer "mark superseded" flow is covered. No stated maximum size or pruning trigger for the *per-feature* files (only `_default.md`'s growth is called out) — a long-lived, high-churn feature could grow one file indefinitely with no equivalent nudge toward human consolidation.

**Overall quality rating:** Good. The core mechanism (slug algorithm, append-only contract, cache-vs-library disambiguation) is well thought through and well documented; the one substantive gap is the unaddressed concurrency question this review specifically probed for, which is a real risk but not one that undermines the skill's core design.

## Required Changes

None — the High-severity finding is a Risk, not a Bug, and the file's own append-only/PR-reviewed design provides a partial mitigation (a lost entry is a silent omission, not a corrupted document). It should be addressed soon but does not block current use.

## Optional Improvements

1. Add either a lightweight locking mechanism for `.md` appends or an explicit documented rationale for why the current unprotected design is believed safe in practice. (High #1)
2. Give the dedup criteria at least one concrete, mechanically-checkable anchor. (Medium #2)
3. Fix the stale path reference in `fix_pattern_cache.py`'s docstring. (Medium #3)
4. Add a pruning/consolidation trigger for large per-feature files, mirroring the one already defined for `_default.md`.

## Final Verdict

**APPROVED WITH CHANGES**

The knowledge base's core contract — one algorithm for naming files, an append-only convention with explicit supersession marking, and a clear disambiguation from the separate `fix-history.json` cache — is sound and well-documented. It falls short of full approval only because it defines a dual-writer file topology without addressing the concurrency risk that topology carries, especially notable since the project already had to solve this exact problem once for the sibling `fix-history.json` file and did not carry that lesson forward here. This is a Risk rather than a Bug, and the append-only/PR-reviewed design limits the damage a lost write could do, so it does not block current use — but it should be resolved or explicitly justified soon.
