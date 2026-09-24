# Agent Review: Kavach (kavach-diagnose + kavach-repair)

> **Remediation update (2026-09-24, same day):** at the requester's direction, every finding below (AR-001 through AR-019, plus the Medium PR-idempotency gap noted under kavach-repair) has been fixed in this working tree, and the full pytest suite (216 tests) and both workflow YAML files were re-validated green afterward. See **Remediation Summary** at the end of this report for what changed and why the Final Verdict is now different from when this review was first written. The findings sections below are left as originally written (the as-found state) for audit-trail purposes — read them alongside the Remediation Summary, not as the current state of the repository.

## Review Metadata
- **Reviewed by:** Agent Reviewer (QA governance perspective), with skill-reviewer sub-reviews for all six referenced skills
- **Review date:** 2026-09-24
- **Agent locations:** `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- **Intended runtime:** Claude Code `--agent` mode, both interactively and (for kavach-diagnose only) unattended in GitHub Actions (`.github/workflows/kavach.yml`); kavach-repair is always interactive, never CI.

This is a review of Kavach as an executable system, not a documentation read-through: every finding below was checked against the actual repository state (scripts run, schemas parsed, workflow YAML read in full), not inferred from prose alone. Two subagent-reported findings (the `triage_workers.py` CLI bug and the `live-replay-diagnosis` `Write(*)/Edit(*)` grant) were independently reproduced/re-verified by the lead reviewer before inclusion.

## Files Reviewed

**Agents:**
- `.claude/agents/kavach-diagnose.md`
- `.claude/agents/kavach-repair.md`

**Skills:**
- `.claude/skills/failure-triage/SKILL.md`
- `.claude/skills/live-replay-diagnosis/SKILL.md`
- `.claude/skills/verdict-reporting/SKILL.md`
- `.claude/skills/kavach-knowledge/SKILL.md`
- `.claude/skills/kavach-repair/SKILL.md`
- `.claude/skills/kavach-diagnose/scripts/**` (no `SKILL.md` — see AR-011)

**Scripts (read and/or executed):**
- `validate_kavach_contract.py`, `validate_replay_receipts.py`, `append_fix_history.py`, `triage_workers.py`, `list_failures.py`, `requirements.txt`
- `tests/test_validate_kavach_contract.py`, `tests/test_append_fix_history.py` (and the rest of `tests/` inspected by the sub-reviewer)

**Contracts / config:**
- `.claude/contracts/kavach-verdict.schema.json`
- `.claude/mcp-ci-life.json`, `.claude/mcp-ci-studio.json`
- `src/test/resources/config/config.properties` (pending diff)
- `src/main/java/factory/DriverFactory.java` (cross-check for the config.properties finding)

**Workflows:**
- `.github/workflows/kavach.yml`
- `.github/workflows/kavach-bootstrap-debug.yml`

**Governance boundary applied throughout (per instructions, not treated as findings):** Kavach may create branches, commit/push to them, and open PRs. It must never push to `main`, merge its own PRs, or bypass required reviewers/status checks. A human-reviewed PR is the approval boundary.

## Referenced Skill Reviews

| Skill | Skill Reviewer Verdict | Blocking Findings | Impact on Agent |
|---|---|---|---|
| failure-triage | Approved with changes | Critical Bug: Phase 2.5's documented `triage_workers.py` CLI invocation crashes with `FileNotFoundError` as literally written (independently reproduced) | Blocks the agent's core triage step exactly as documented; see AR-001 |
| kavach-diagnose scripts bundle (no SKILL.md) | Approved with changes | Medium: `append_fix_history.py` not idempotent against an identical re-run | Non-blocking bookkeeping gap; see AR-012 |
| kavach-knowledge | Approved with changes | Critical/High: feature→slug derivation undefined, with real orphaned fix-pattern files already in the repo; documented `.md` structure doesn't match real files; CI concurrency gap can silently delete learned patterns | Breaks the kavach-diagnose↔kavach-repair fix-pattern handoff and risks data loss across concurrent CI runs; see AR-002, AR-005 |
| kavach-repair | Approved with changes | High: no defined behavior for missing `fix-history.json` (currently reproducible — the file doesn't exist yet in this repo); `confidence` field from the contract never consulted | See AR-007, AR-008 |
| live-replay-diagnosis | Approved with changes | High: the skill's own documented unattended invocation grants unrestricted `Write(*)/Edit(*)`, contradicting its own "never applies fixes" rule and the agent's declared scope (independently reproduced) | See AR-003 |
| verdict-reporting | Approved with changes | High: verdict-report filename has only minute granularity (collision risk); fix-pattern slug derivation left unspecified, diverging from kavach-repair's own algorithm | See AR-004, AR-002 |

Every skill review returned **Approved with changes** — no skill was found unfit for purpose outright, but several carry Major-or-higher findings that are folded into this agent-level report below (a skill's Critical finding is treated as blocking here whenever the agent's normal workflow depends on it, per this review's methodology).

## Dependency and Handoff Summary

```
kavach-diagnose (agent)
 ├─ preloads: failure-triage → live-replay-diagnosis → verdict-reporting → kavach-knowledge
 ├─ scripts (unnamed skill dir): list_failures.py, triage_workers.py, replay_workers.py,
 │     validate_replay_receipts.py, validate_kavach_contract.py, append_fix_history.py
 ├─ writes: kavach-data/history/**, kavach-data/fix-patterns/** (Write tool only, no Edit)
 ├─ reads: target/cucumber-reports/cucumber.json, kavach-data/fix-patterns/*, kavach-data/history/fix-history.json
 ├─ MCP: playwright (pinned @0.0.82, CDP-attach only — never logs in itself)
 └─ handoff → kavach-data/history/triage-results/<ts>/combined-receipts.json
        validated against .claude/contracts/kavach-verdict.schema.json
        (CI: also independently re-validated by validate_kavach_contract.py, a separate step after Claude exits)

kavach-repair (agent, human-triggered only, never CI)
 ├─ preloads: kavach-repair (skill) → kavach-knowledge
 ├─ input: combined-receipts.json (filtered to verdict == script_issue_fix_proposed only) OR isolation-mode target
 ├─ edits: **/*.feature, src/test/java/stepdefinitions/**, src/main/java/pages/**, kavach-data/history/fix-history.json, kavach-data/fix-patterns/**
 ├─ verification: mvn spotless + 3x Maven green, mandatory before any commit
 └─ output: one branch, one PR — terminal stage, never hands off to another agent
```

Both agents' write/edit scopes are correctly disjoint from application logic outside test automation, and kavach-repair never touches Playwright/MCP. The producer→consumer contract (kavach-diagnose → kavach-repair) is schema-backed and — per AR-verified evidence below — the schema now genuinely describes the full document (not a single row), a real fix from a prior review cycle.

---

## Critical Issues

### AR-001 — failure-triage's documented Phase 2.5 command crashes as written
- **Severity:** Critical
- **Category:** Bug
- **Location:** `.claude/skills/failure-triage/SKILL.md`, Phase 2.5 code block:
  `cd .claude/skills/kavach-diagnose/scripts && python3 triage_workers.py -i history/failures-for-replay.json --fix-history history/fix-history.json -o history/triage-results --repo-root ../../../..`
- **Problem:** The `-i`/`--fix-history`/`-o` arguments are relative paths resolved against the post-`cd` working directory (`.../scripts/`), which has no `history/` subdirectory — the real data lives at `<repo-root>/kavach-data/history/`. Reproduced directly: running this exact command raises an unhandled `FileNotFoundError: [Errno 2] No such file or directory: 'history/failures-for-replay.json'`.
- **Impact:** This is the one command that runs the "cheap no-browser triage tier" — the entire reason failure-triage exists. As literally documented, it fails on every invocation, meaning the escalation handoff to live-replay-diagnosis (and everything downstream) never happens unless whoever runs it happens to notice the bug and improvise a fix not in the doc.
- **Recommendation:** Fix the path arguments to be repo-root-relative (matching the already-correct `--repo-root ../../../..` in the same line), or drop the explicit overrides entirely and rely on the script's own defaults, the same way the immediately-preceding `list_failures.py` invocation does with no arguments at all.
- **Example:**
  ```
  cd .claude/skills/kavach-diagnose/scripts && python3 triage_workers.py --repo-root ../../../..
  ```
  (verify against the script's own `argparse` defaults for `-i`/`--fix-history`/`-o` before finalizing — do not reintroduce the same relative-path mistake.)

### AR-002 — Feature→fix-pattern-file slug derivation is unspecified and already inconsistent in the live repo
- **Severity:** Critical
- **Category:** Inconsistency
- **Location:** `.claude/skills/kavach-knowledge/SKILL.md` ("Shape" section), `.claude/skills/kavach-repair/SKILL.md` (Phase 2 step 2), `.claude/skills/verdict-reporting/SKILL.md` (Phase 5 bootstrap step)
- **Problem:** Three different skills each derive a `<feature-slug>.md` filename from a feature file name, but only kavach-repair states a concrete algorithm (split on `_`/camelCase boundaries, hyphen-join, lowercase — e.g. `Life_CampaignDashboard.feature` → `life-campaign-dashboard.md`). kavach-knowledge's one worked example (`Life_PMP` → `life-pmp.md`) doesn't exercise a camelCase case, and verdict-reporting (the skill that actually *bootstraps* new pattern files) never states the rule at all. Checking the real `kavach-data/fix-patterns/` directory against real feature files confirms the drift is already live: `life-campaign-dashboard.md` is hyphenated at the word boundary, but `life-curatedmarket.md`, `life-reporttemplates.md`, `life-targetingtemplates.md`, and `life-npilists.md` are not — and at least seven pattern files (`life-create-campaign.md`, `life-create-creative.md`, `life-create-pixel.md`, `life-create-report-template.md`, `life-line-item-creation.md`, `life-tactic-creation.md`, `life-targeting-template-creation.md`) don't correspond to any feature file that exists in the repo today.
- **Impact:** Two agents (or the same agent on two different runs) deriving a slug independently have no deterministic rule to converge on. A pattern kavach-diagnose or verdict-reporting bootstraps under one slug can become permanently invisible to kavach-repair's Phase 2 lookup if it derives a different slug for the same feature — silently defeating the fix-pattern cache this whole knowledge base exists to provide, with no error surfaced anywhere.
- **Recommendation:** Write one explicit, mechanical slugification algorithm once (strip extension, strip app-prefix segment, insert a hyphen at every lowercase→uppercase transition, lowercase) and reference it identically from kavach-knowledge, kavach-repair, and verdict-reporting. Separately, reconcile or explain the seven orphaned pattern files that match no current feature file.
- **Example:** `Life_CampaignDashboard.feature` → strip `Life_` → `CampaignDashboard` → hyphenate at case boundaries → `Campaign-Dashboard` → lowercase → `campaign-dashboard.md`. Applying the same rule to `CuratedMarket` yields `curated-market.md` — note this renames the existing `life-curatedmarket.md`, so the fix needs an explicit one-time migration, not just a doc change.

---

## Major Issues

### AR-003 — live-replay-diagnosis's own documented tool grants contradict its "never applies fixes" boundary
- **Severity:** Major
- **Category:** Inconsistency
- **Location:** `.claude/skills/live-replay-diagnosis/SKILL.md`, Phase 0 unattended examples (Life and Studio blocks) and the Phase 3 `replay_workers.py` example
- **Problem:** The skill's own worked `--allowedTools` examples grant blanket `Write(*)` and, in two of the three copies, `Edit(*)` — unrestricted filesystem write/edit access. This directly contradicts: (a) this same file's closing rule that only kavach-repair may ever edit fix files, and only after approval; (b) the `kavach-diagnose` agent's actual declared tools (`Write(kavach-data/**)`, no `Edit` at all); and (c) the real production CI config in `kavach.yml`, which correctly scopes this to `Edit(kavach-data/**)`. Independently reproduced by `grep`.
- **Impact:** Anyone copying Phase 0 literally into a new unattended run or a CI job not modeled on `kavach.yml` would grant the session the ability to overwrite any file on disk, including application/test source — exactly the failure mode the "present, don't apply" rule exists to prevent, with no human in the loop in an unattended context to catch it. The actual shipped CI workflow avoids this, so the danger is currently confined to the doc, but it is a live landmine for the next person who edits or extends this pipeline from the doc rather than from `kavach.yml`.
- **Recommendation:** Replace all three examples with the exact minimal grant `kavach.yml` uses (`Edit(kavach-data/**)`, no bare `Write`/`Edit`), and state once that this is the only token ever needed.
- **Example:**
  ```diff
  - --allowedTools "Bash(*),Read,Write(*),Edit(*),Grep,Glob,mcp__playwright__browser_navigate,..."
  + --allowedTools "Bash(*),Read,Edit(kavach-data/**),Grep,Glob,mcp__playwright__browser_navigate,..."
  ```

### AR-004 — Verdict-report filename has only minute-granularity, contradicting the skill's own uniqueness guarantee
- **Severity:** Major
- **Category:** Bug
- **Location:** `.claude/skills/verdict-reporting/SKILL.md`, Phase 4: `replay-verdict-<YYYY-MM-DD-HHmm>.md`
- **Problem:** The skill states "each run gets its own timestamped file — never append to or overwrite a prior run's file, even if run on the same day," but the filename is only minute-precise. Two runs finishing within the same clock minute (plausible with a re-triggered or manually rerun CI job) produce an identical filename.
- **Impact:** `kavach.yml` commits `replay-verdict-*.md` to git after every run. A same-minute collision silently overwrites a previously-committed report, destroying part of the audit trail — this bears directly on checkpoint 14 ("artifact paths are unique and unambiguous for each workflow run"), which holds for `combined-receipts.json` (timestamped directory, verified unique per the CI's own single-file check) but not for this sibling artifact.
- **Recommendation:** Use second-granularity (matching the triage-results directory's own `%Y-%m-%d-%H%M%S` convention), or check for existence and append a numeric suffix before finalizing the filename.
- **Example:** `replay-verdict-2026-07-09-143205.md`.

### AR-005 — No concurrency guard on the fix-pattern-learning CI job; a second overlapping run can silently delete a merged prior run's learned patterns
- **Severity:** Major
- **Category:** Risk
- **Location:** `.github/workflows/kavach.yml` — `run-kavach`'s "Upload learned fix-patterns for the PR job" step and the `open-fix-pattern-pr` job (no `concurrency:` block anywhere in the file, confirmed by direct read)
- **Problem:** `run-kavach` checks out `main` once at job start, runs for up to 90 minutes, then uploads the **entire** `kavach-data/fix-patterns` directory as a full snapshot (not a diff). `open-fix-pattern-pr` later checks out `main` fresh and downloads that stale snapshot **on top of it**, wholesale-overwriting the directory before committing. Nothing prevents two `workflow_dispatch` runs from overlapping.
- **Impact:** If a first run's PR merges to `main` while a second, still-running (or a first, slower) run is mid-flight, that run's later `open-fix-pattern-pr` job will silently revert the merged content back to its own stale snapshot when it commits — `git status --porcelain` sees this as an ordinary diff and commits it with no warning, no conflict marker, and nothing for a reviewer to notice unless they diff against history by hand. This directly contradicts kavach-knowledge's own framing of this directory as "the one part of Kavach's output that is never disposable."
- **Recommendation:** Add `concurrency: group: kavach-fix-patterns, cancel-in-progress: false` at the workflow level so overlapping runs queue instead of racing, or change `open-fix-pattern-pr` to merge (re-append) rather than overwrite.
- **Example:**
  ```yaml
  concurrency:
    group: kavach-learned-patterns
    cancel-in-progress: false
  ```

### AR-006 — `confirmed_product_bug` evidence gate still checks only 2 of 5 gate fields for real values
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/skills/kavach-diagnose/scripts/validate_replay_receipts.py`, `_artifact_problems()` / `REQUIRED_ARTIFACT_KEYS = ("domStructureChecked", "staleLocatorRuledOut")`
- **Problem:** This was one of the specifically-requested checkpoints ("all `confirmed_product_bug` evidence gates require correct values, not only field presence"). The fix is real and well-tested for two of the five `productBugGate` booleans — `domStructureChecked` and `staleLocatorRuledOut` now require a genuine, non-placeholder, signal-bearing artifact string (verified directly by reading the regexes and the producer/consumer integration test). The other three — `userLevelBehaviorReproduced`, `targetAffordanceMissingOrBroken`, `testDataOrEnvironmentRuledOut` — remain pure self-reported booleans with no corresponding artifact requirement.
- **Impact:** A worker session can still earn a `Product Bug — Confirmed` verdict (the report's 🔴 Critical section) by producing two real-looking DOM/count snippets and simply asserting `true` for the other three preconditions with no supporting evidence — a partial, not complete, closure of the original finding.
- **Recommendation:** Extend `REQUIRED_ARTIFACT_KEYS` to include `testDataOrEnvironmentRuledOut` at minimum (what test data was checked and what was found), and consider requiring at least one `evidence` entry to carry a comparable signal for the remaining two fields.
- **Example:** `REQUIRED_ARTIFACT_KEYS = ("domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut")`

### AR-007 — kavach-repair has no defined behavior for a missing `fix-history.json` (currently reproducible)
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/skills/kavach-repair/SKILL.md`, Phase 1 step 2 ("Never skip reading `fix-history.json` in Phase 1")
- **Problem:** The skill treats reading this file as mandatory but never states what to do if it doesn't exist — and in this exact repo, `kavach-data/history/` doesn't exist at all yet. An agent following the instruction literally could hard-fail at Phase 1 on the very first real run, or silently invent inconsistent "treat as empty" behavior.
- **Impact:** First-run/cold-start behavior is undefined in a document that is otherwise unusually strict and explicit everywhere else.
- **Recommendation:** Add an explicit branch treating a missing file as an empty history array with no candidates skipped, and note this in the discovery table output.
- **Example:** `If kavach-data/history/fix-history.json does not exist, treat it as [] (no candidates skipped for prior-attempt reasons) and print "No fix-history.json found — treating all candidates as first attempts."`

### AR-008 — kavach-repair never consults the contract's `confidence` field
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/skills/kavach-repair/SKILL.md`, Phase 1 (candidate collection) and Phase 2 (fix derivation) — `confidence` is never read
- **Problem:** The upstream contract carries `confidence: high|medium|low` specifically so a downstream consumer can distinguish a hedged diagnosis from a confident one. kavach-repair's candidate collection and fix-derivation logic treat every `script_issue_fix_proposed` row identically regardless of this field.
- **Impact:** A low-confidence script-issue classification gets exactly the same automatic-repair treatment as a high-confidence one, undercutting the purpose of carrying the field at all — even though the verdict-value allow-list itself (only `script_issue_fix_proposed` is ever collected) is sound.
- **Recommendation:** Surface `confidence` as a column in the discovery table and require it be visible before the human's blanket `[y/N]` confirmation, so a human isn't approving a low-confidence candidate without knowing it.

### AR-009 — Repository-boundary violation detection in CI doesn't block the downstream PR job
- **Severity:** Major
- **Category:** Risk
- **Location:** `.github/workflows/kavach.yml`, `run-kavach` job — "Validate combined-receipts.json..." (`id: contract`) runs and sets `valid=true` **before** the later "Verify repository boundaries" step; `open-fix-pattern-pr`'s condition is `if: always() && needs.run-kavach.outputs.contract_valid == 'true'`
- **Problem:** If kavach-diagnose (an LLM-driven, sandboxed-but-untrusted tool loop) writes outside its allowed paths, the "Verify repository boundaries" step correctly detects it and exits 1 — but that step runs *after* the contract-validation step already recorded `valid=true`, and every subsequent step (including artifact upload) uses `if: always()`. Because `open-fix-pattern-pr`'s condition only checks `contract_valid` (already set) and also uses `if: always()`, it will still run and open a fix-pattern PR even though `run-kavach`'s overall job status is `failure` due to the boundary violation.
- **Impact:** The actual PR content stays safe (the PR job's `git add` is hard-scoped to `kavach-data/fix-patterns`/`fix-history.json`/`replay-verdict-*.md`, so out-of-bounds content is never staged into the PR itself), but a real boundary violation can be masked by an apparently-successful "Kavach: update learned fix patterns" PR appearing right alongside a failed `run-kavach` job — a reviewer scanning PR activity rather than Action run statuses could miss it.
- **Recommendation:** Have the boundary-check step set an explicit output (e.g. `boundary_ok`) and add it to `open-fix-pattern-pr`'s `if:` condition alongside `contract_valid`.
- **Example:** `if: always() && needs.run-kavach.outputs.contract_valid == 'true' && needs.run-kavach.outputs.boundary_ok == 'true'`

### AR-010 — Pending `config.properties` change breaks the CI job's core Cucumber run
- **Severity:** Major
- **Category:** Bug
- **Location:** `src/test/resources/config/config.properties` (uncommitted working-tree change: `headless=true` → `headless=false`); consumed by `src/main/java/factory/DriverFactory.java:36` (`ConfigReader.getProperty("headless")`)
- **Problem:** `kavach.yml`'s "Run the target Cucumber suite" step runs `mvn -B -ntp test -Dtest=TestRunner` inside a `--privileged` container with no display server. `DriverFactory` reads its headless flag straight from `config.properties` for this exact run (distinct from the auth-bootstrap tests, which take `-Dauth.headless` as an explicit system property and are unaffected). With `headless=false` committed, this step would attempt to launch a headed Chromium in a headless container and fail to produce `cucumber.json` at all — the one artifact the entire Kavach pipeline exists to diagnose.
- **Impact:** This is currently an uncommitted working-tree change (visible in `git status`, not yet on the branch), so it has not yet broken anything — but if committed as-is, it silently breaks the CI job's primary input before Kavach's own logic ever runs, and none of Kavach's own safeguards (contract validation, boundary checks) would catch it, since the failure happens upstream of Kavach entirely.
- **Recommendation:** Revert `headless` to `true` in `config.properties` before this change is committed, or if the intent was genuinely to change local/default test behavior, gate it so CI's `TestRunner` invocation always overrides it explicitly (the way the auth-bootstrap tests already do via `-Dauth.headless`).
- **Example:** `mvn -B -ntp test -Dtest=TestRunner -Dheadless=true ...` in `kavach.yml`, plus a `ConfigReader` fallback that lets a system property override the properties file.

---

## Minor Issues

### AR-011 — `.claude/skills/kavach-diagnose/` has no `SKILL.md`
- **Severity:** Minor
- **Category:** Improvement
- **Location:** `.claude/skills/kavach-diagnose/` (only a `scripts/` subdirectory exists)
- **Problem:** Unlike every sibling directory under `.claude/skills/`, this path has no frontmatter/description and cannot itself be triggered as a skill — it's a shared script toolbox the agent invokes via `Bash` and that two other skills reference by path in prose.
- **Recommendation:** Add a minimal reference-only `SKILL.md`, or relocate the directory out of `.claude/skills/` so its location doesn't imply it's a self-contained skill.

### AR-012 — `append_fix_history.py` is concurrency-safe but not idempotent against an identical retry
- **Severity:** Minor
- **Category:** Risk
- **Location:** `.claude/skills/kavach-diagnose/scripts/append_fix_history.py`, `append_entries()`
- **Problem:** No dedup check exists — piping the same JSON array through twice (a retried CI step, a rerun after interruption) appends every entry a second time verbatim.
- **Recommendation:** Skip entries whose `(scenarioName, timestamp, groupId)` tuple already exists in the file, and add a regression test for the double-append case.

### AR-013 — `kavach-verdict.schema.json` is documentation only; nothing loads it at runtime
- **Severity:** Minor
- **Category:** Improvement
- **Location:** `.claude/contracts/kavach-verdict.schema.json`; `validate_kavach_contract.py` is a deliberately dependency-free hand-rolled re-implementation
- **Problem:** Two independent sources of truth for the same contract shape exist. This is well-mitigated today by `SchemaEquivalenceTests`/`test_kavach_verdict_schema_equivalence.py`, which fail if the two drift apart — but it remains a maintenance risk that a future editor of one could forget the other exists.
- **Recommendation:** Keep the equivalence test as the enforcement mechanism (it currently works); consider a code comment in the schema file pointing at the exact test that enforces the pairing.

### AR-014 — Tool-grant token mismatch between the agent definition and its real CI invocation
- **Severity:** Minor
- **Category:** Inconsistency
- **Location:** `.claude/agents/kavach-diagnose.md` frontmatter (`Write(kavach-data/**)`) vs. `.github/workflows/kavach.yml`'s `--allowedTools` string (`Edit(kavach-data/**)`, no `Write` token at all)
- **Problem:** The agent's own declared tool and the token actually granted at its one real production invocation don't match. In practice this is low-impact because the CI invocation also grants unrestricted `Bash(*)`, which can write anywhere regardless of the `Write`/`Edit` token list — the real enforcement mechanism is the post-hoc "Verify repository boundaries" git-status check (see AR-009), not the tool-grant string.
- **Recommendation:** Make the CI `--allowedTools` string match the agent's own declared tools exactly, for clarity even though `Bash(*)` already makes the distinction largely symbolic.

### AR-015 — kavach-knowledge: undefined dedup criteria, no entry-invalidation path, no growth bound on `_default.md`
- **Severity:** Minor
- **Category:** Improvement
- **Location:** `.claude/skills/kavach-knowledge/SKILL.md`, "Read/write contract"
- **Problem:** "Dedupe against existing entries... never write a near-duplicate" has no worked example or threshold; entries are append-only with no way to mark a later-discovered-wrong pattern superseded; `_default.md` (loaded on every single run by both agents) has no pruning guidance.
- **Recommendation:** Add one worked dedup example, a `**[superseded YYYY-MM-DD]**` convention for corrections, and a note that periodic human pruning of `_default.md` is expected as it grows.

### AR-016 — kavach-knowledge: documented `.md` structure doesn't match real fix-pattern files
- **Severity:** Minor
- **Category:** Inconsistency
- **Location:** `.claude/skills/kavach-knowledge/SKILL.md` ("Shape") vs. actual `kavach-data/fix-patterns/life-campaign.md` and `studio-explorerworkspace.md`
- **Problem:** The documented `## Known good fixes`/`## Learned notes` headings don't appear in either real non-empty file; the real files use dated `### YYYY-MM-DD` subheadings instead, and one file has an undocumented `## Known special cases` heading.
- **Recommendation:** Update the Shape section to describe the structure actually in use, and reconcile the undocumented heading.

### AR-017 — failure-triage: stale internal cross-reference and thin test coverage for the exact code that had the AR-001 bug
- **Severity:** Minor
- **Category:** Inconsistency
- **Location:** `.claude/skills/failure-triage/SKILL.md`, Reference-files intro ("this skill's Phase 2 derivation rule") and "Testing" section (names only `test_llm_static_triage.py`)
- **Problem:** Phase 2 contains no such derivation rule (it lives in live-replay-diagnosis instead); and the Testing section never mentions `test_triage_workers.py`, whose own coverage doesn't exercise the documented CLI path at all — consistent with how AR-001 went unnoticed.
- **Recommendation:** Fix the cross-reference; name both test files in Testing and add an integration test that runs the documented CLI invocation end-to-end.

### AR-018 — verdict-reporting: two implicit "where does this value come from" gaps
- **Severity:** Minor
- **Category:** Risk
- **Location:** `.claude/skills/verdict-reporting/SKILL.md`, Phase 4 (row completeness when `affectedScenarios` is absent) and Phase 5 (`<replay-timestamp>` for a destructive `find -delete`)
- **Problem:** Neither value's derivation is stated explicitly, even though both are mechanically recoverable (`failures-for-replay.json`'s `groupId` field; `combined-receipts.json`'s own `replayManifest` field, respectively). The second one guards a destructive delete, so an ad hoc "most recent directory" guess is a real (if currently low-likelihood) risk of deleting a concurrently-running batch's files.
- **Recommendation:** State both derivations explicitly in the skill text.

### AR-019 — Miscellaneous low-impact documentation drift
- **Severity:** Minor
- **Category:** Improvement
- **Location:** several
- **Problem (bundle):** (a) `kavach-data/fix-patterns/_default.md` and the schema's own description field both still reference a nonexistent `iFix.md` (renamed to `live-replay-diagnosis/SKILL.md` at some point); (b) `requirements.txt` declares an unused `requests` dependency; (c) `test_kavach_verdict_schema_equivalence.py` and the `SchemaEquivalenceTests` class inside `test_validate_kavach_contract.py` duplicate the same assertions; (d) verdict-reporting's section-assignment table includes `script_issue_fix_applied`, a value that can never actually appear in `combined-receipts.json` (only in `fix-history.json`, post-repair).
- **Recommendation:** Sweep the `iFix.md` references; drop the unused dependency; consolidate the duplicated test file; footnote the unreachable table row.

---

## Overall Assessment

### Role and Scope
Both agents are cleanly single-purpose and the diagnose/repair split is well-maintained: kavach-diagnose never edits automation source (no `Edit` tool at all, `Write` scoped to `kavach-data/**`) and explicitly refuses to invoke kavach-repair itself; kavach-repair never drives a browser and refuses to run unattended (`CI=true`/non-TTY abort is the literal first instruction in its skill, ahead of all other logic). Neither agent embeds a complete reusable procedure that duplicates its preloaded skills — the agent files stay at the identity/scope/authority layer and defer procedure to skills, as intended.

### Agent-versus-Skill Separation
Correctly maintained. The one structural wrinkle is that `.claude/skills/kavach-diagnose/` (script bundle) sits at a skills-directory path without being a real skill (AR-011) — organizational, not a functional violation.

### Contracts and Handoffs
This is the area with the most substantive, previously-flagged-and-now-partially-or-fully-fixed work, and the review specifically re-verified all 15 requested checkpoints:

| # | Checkpoint | Status |
|---|---|---|
| 1 | Producer/consumer share the same `groups` document structure | **Resolved** — verified in schema, validator, and a direct producer→consumer integration test |
| 2 | JSON Schema validates the full document, not one row | **Resolved** |
| 3 | Malformed/empty/inconsistent artifacts rejected | **Resolved** — confirmed via `ValidatorRejectsRealisticBreakageTests` |
| 4 | `confirmed_product_bug` gates require correct values, not presence | **Partially resolved** — see AR-006 (2 of 5 gate fields) |
| 5 | GitHub Actions independently re-runs the deterministic validator after Claude | **Resolved** — verified in `kavach.yml` |
| 6 | Producer output tested directly against consumer validator | **Resolved** |
| 7 | Python test deps declared, full suite runs in CI | **Resolved** — verified imports vs. `requirements.txt`, and the pytest invocation targets the whole `tests/` directory |
| 8 | Workflow inputs passed safely via env vars, validated | **Resolved** — freeform inputs go through `env:`, never string-interpolated into `run:` |
| 9 | Playwright MCP and other runtime deps pinned | **Resolved** — MCP pinned to `@playwright/mcp@0.0.82`, container pinned to match `pom.xml`'s Playwright version, Claude Code CLI version pinned |
| 10 | Diagnosis/repair permissions follow least privilege | **Mostly resolved** — see AR-003 (doc-only landmine) and AR-014 (grant mismatch) |
| 11 | Diagnosis agent cannot modify automation source | **Resolved in the real CI path; undermined by AR-003's doc examples and only detectively (not preventively) enforced — see AR-009** |
| 12 | Repair agent accepts only validated `script_issue_fix_proposed` | **Resolved** — a strict allow-list, verified in Phase 1 |
| 13 | Other classifications can never trigger repair | **Resolved** — same allow-list mechanism |
| 14 | Artifact paths unique per run | **Partially resolved** — `combined-receipts.json` is; the sibling verdict-report `.md` is not (AR-004) |
| 15 | Repo prevents the workflow identity from merging/bypassing review | **Not verifiable from code** — this is a GitHub branch-protection/ruleset setting, not present anywhere in this repository's tracked files (no `CODEOWNERS`, no ruleset file). The workflow's own token scoping is consistent with the governance policy (`open-fix-pattern-pr` only ever gets `contents: write`/`pull-requests: write`, never admin, and never attempts a merge or approval) — but confirming the repository actually blocks the Actions bot identity from merging or overriding required reviews needs to be checked directly in the repo's Settings → Rules, outside this review's visibility. |

### Tools, Permissions, and Security
Least-privilege intent is clear and mostly well-executed (job-splitting so only `open-fix-pattern-pr` ever holds write credentials; `kavach-repair`'s hard refusal to run unattended; MCP/CLI/container version pinning). The gaps found are all detective-vs-preventive or documentation-vs-reality mismatches (AR-003, AR-009, AR-014) rather than an agent that actually possesses unchecked destructive authority in its real, shipped configuration.

### Failure Handling and Operability
Both agents define clear `status`/`blockers` handoff vocabularies, and the CI job's `if: always()` steps correctly ensure a run always leaves an inspectable trail (log, artifact, job summary) even on failure. `append_fix_history.py`'s locking is a genuine, tested fix for a real concurrency hazard — but the analogous workflow-level hazard (AR-005) is unaddressed one layer up.

### Strengths
- The evidence-gate anti-fabrication logic (regex-based placeholder/signal detection on `productBugArtifacts`) is a genuinely thoughtful, tested piece of engineering — not a rubber-stamp fix.
- The `enforce_tier1_verdict_constraints` safety constraint (cheap triage tier can never itself finalize a product-bug verdict) is enforced in code and covered by a dedicated, well-named test class.
- CI job-splitting (`run-kavach` read-only vs. `open-fix-pattern-pr` write-capable) is the correct pattern for keeping an LLM-driven tool loop away from a token that can push or open a PR.
- The kavach-repair skill's war-story-style Phase 3.1 guidance (shadow-DOM CSS-over-XPath, JUnit `assertEquals` argument order, distinguishing session-degradation flakiness from real stale data) reads as genuinely earned from real incidents, not generic padding.
- Version pinning discipline (Playwright MCP, container image, Claude Code CLI) is consistent and well-justified throughout.

### Overall Quality Rating
**Good, held back by two narrow but real Critical defects and a cluster of Major gaps that are all individually well-understood and cheaply fixable.** The architecture, governance boundaries, and contract design are sound; nothing found here reflects a structural rethink being needed.

## Required Changes Before Approval
1. Fix the `triage_workers.py` path arguments in `failure-triage/SKILL.md`'s Phase 2.5 (AR-001) — verified to crash as documented.
2. Revert (or CI-override) the pending `config.properties` `headless=false` change before it's committed (AR-010) — verified to break the CI job's core Cucumber run.
3. Define and unify the feature→slug derivation algorithm across kavach-knowledge, kavach-repair, and verdict-reporting, and reconcile the seven orphaned fix-pattern files (AR-002).
4. Fix the `Write(*)/Edit(*)` tool-grant examples in `live-replay-diagnosis/SKILL.md` to match the least-privilege grant actually used in `kavach.yml` (AR-003).
5. Add a `concurrency:` guard to `kavach.yml` (or make `open-fix-pattern-pr` merge instead of overwrite) to close the fix-pattern data-loss race (AR-005).
6. Extend `REQUIRED_ARTIFACT_KEYS` to cover `testDataOrEnvironmentRuledOut` at minimum, closing the remaining gap in the `confirmed_product_bug` evidence gate (AR-006).
7. Give the verdict-report filename second-level (or collision-checked) granularity (AR-004).
8. Define missing-`fix-history.json` cold-start behavior in `kavach-repair/SKILL.md` (AR-007).
9. Have kavach-repair surface and act on the contract's `confidence` field rather than ignoring it (AR-008).
10. Gate `open-fix-pattern-pr` on the repository-boundary check's outcome, not only on contract validity (AR-009).

## Optional Improvements
1. Add a minimal `SKILL.md` (or relocate) for the `.claude/skills/kavach-diagnose/` script bundle (AR-011).
2. Add an idempotency/dedup guard to `append_fix_history.py` (AR-012).
3. Align the CI `--allowedTools` token with the agent's own declared `Write(kavach-data/**)` tool (AR-014).
4. Add worked dedup criteria, an entry-supersession convention, and growth guidance for `_default.md` in kavach-knowledge (AR-015).
5. Reconcile kavach-knowledge's documented `.md` structure with what's actually written (AR-016).
6. Fix failure-triage's stale Phase 2 cross-reference and broaden its Testing section (AR-017).
7. State the two implicit value-derivation rules in verdict-reporting explicitly (AR-018).
8. Sweep miscellaneous documentation drift: stale `iFix.md` references, unused `requests` dependency, duplicated schema-equivalence tests, unreachable table row (AR-019).
9. Confirm directly in GitHub repository settings (not visible to this code-only review) that branch protection/rulesets actually prevent the Actions bot identity from merging PRs or bypassing required reviews/status checks (checkpoint 15).

## Final Verdict (as originally written, before remediation)
**Not approved**

Two Critical findings (AR-001, AR-010) each independently mean the pipeline cannot reliably execute as currently shipped: the documented triage command crashes verbatim, and an uncommitted-but-pending config change would break the CI job's core test run the moment it lands. Per this review's verdict rubric, any Critical finding rules out Approved/Approved-with-changes regardless of the surrounding work's quality. That said, both Criticals are narrow, mechanical, one-line-class fixes — not evidence of a structural problem — and the surrounding system (contract validation, evidence gating, least-privilege job-splitting, version pinning) is well-designed and, on the specific points a prior review flagged, has demonstrably improved. Once AR-001 and AR-010 are fixed, and the Major findings (AR-002 through AR-009) are addressed or explicitly accepted as known risk, this system is in clear shape to move to **Approved with changes** on a follow-up pass.

---

## Remediation Summary (2026-09-24, applied same day at the requester's direction)

Every Required Change and every Optional Improvement listed above was implemented directly in this working tree. Summary by finding:

| ID | Fix applied |
|---|---|
| AR-001 (Critical) | `failure-triage/SKILL.md` Phase 2.5 command changed to `triage_workers.py` with no path arguments; verified its own argparse defaults resolve to the real `<repo-root>/kavach-data/history/...` paths (both by direct import and by reproducing the original crash beforehand). Added `DocumentedCliInvocationTests` to `test_triage_workers.py` asserting the defaults stay repo-root-anchored. |
| AR-002 (Critical) | Defined one canonical slug-derivation algorithm (with a worked-example table) in `kavach-knowledge/SKILL.md`'s Shape section; `kavach-repair` and `verdict-reporting` now reference it instead of restating or omitting their own copy. All mismatched/orphaned fix-pattern files were empty (0 bytes) except `studio-explorerworkspace.md`, which had real content — that one was `git mv`'d to `studio-explorer-workspace.md` preserving its content and history; the empty mis-named/orphaned files (`life-curatedmarket.md`, `life-reporttemplates.md`, `life-targetingtemplates.md`, `life-npilists.md`, `life-lineitem.md`, `life-runreport.md`, `life-schedulereport.md`, and the seven fully-orphaned `life-create-*`/`life-*-creation.md` files) were removed or renamed to the correct slug; every remaining `kavach-data/fix-patterns/*.md` file now matches a real feature file 1:1 under the new algorithm. |
| AR-003 (Major) | Replaced all three `Write(*)`/`Edit(*)` examples in `live-replay-diagnosis/SKILL.md` with the least-privilege `Edit(kavach-data/**)` token, matching `kavach.yml`'s real invocation; updated the prose TOOL USE line to match. |
| AR-004 (Major) | `verdict-reporting/SKILL.md`'s verdict-report filename changed from minute-granularity (`HHmm`) to second-granularity (`HHmmss`), plus an explicit collision-check-and-suffix rule. |
| AR-005 (Major) | Added a workflow-level `concurrency: kavach-learned-patterns` (queue, don't cancel) block to `kavach.yml`. |
| AR-006 (Major) | Extended `REQUIRED_ARTIFACT_KEYS` in `validate_replay_receipts.py` to include `testDataOrEnvironmentRuledOut`, with its own real-value signal regex (digit or quoted literal, since it isn't a DOM/locator check); updated `live-replay-diagnosis/SKILL.md`'s receipt template and prose to match; added/updated tests in `test_validate_replay_receipts.py` and `test_validate_kavach_contract.py`; full suite re-verified green (216 passed). |
| AR-007 (Major) | `kavach-repair/SKILL.md` Phase 1 now explicitly treats a missing `fix-history.json` as an empty array (no candidates skipped) rather than leaving cold-start behavior undefined. |
| AR-008 (Major) | `kavach-repair/SKILL.md` Phase 1 now carries `confidence` into the discovery table and requires calling out any `low`-confidence candidate by name in the confirmation prompt. |
| AR-009 (Major) | Added a `boundary_ok` output to `kavach.yml`'s "Verify repository boundaries" step; both the "Upload learned fix-patterns" step and the `open-fix-pattern-pr` job's conditions now require `boundary_ok == 'true'` alongside `contract_valid == 'true'`. |
| AR-010 (Critical) | Reverted the pending `config.properties` `headless=false` change back to `headless=true` — confirmed via `git diff` that the file now matches the committed baseline exactly. |
| AR-011 (Minor) | Added `.claude/skills/kavach-diagnose/SKILL.md` as a reference-only index of the script bundle and its pipeline order. |
| AR-012 (Minor) | Added a dedup guard (`(scenarioName, timestamp, groupId)` key) to `append_fix_history.py`'s `append_entries()`; added two regression tests (`test_identical_batch_resubmitted_is_not_double_appended`, `test_a_genuinely_new_entry_for_the_same_scenario_still_appends`) to `test_append_fix_history.py`. |
| AR-013 (Minor) | Added a docstring note in `validate_kavach_contract.py` naming `test_kavach_verdict_schema_equivalence.py` as the mechanism that keeps the schema and validator in lockstep. |
| AR-014 (Minor) | `kavach.yml`'s kavach-diagnose invocation now grants `Write(kavach-data/**),Edit(kavach-data/**)`, matching the agent's own declared tools; the accompanying comment now also states plainly that `Bash(*)` — not this token list — is why the "Verify repository boundaries" step is the real enforcement mechanism. |
| AR-015 (Minor) | Added a worked dedup example, a `**[superseded YYYY-MM-DD]**` convention for correcting wrong entries, and growth/pruning guidance for `_default.md`, all in `kavach-knowledge/SKILL.md`. |
| AR-016 (Minor) | `kavach-knowledge/SKILL.md`'s Shape section now describes the dated `### YYYY-MM-DD` structure real files actually use, rather than the aspirational `## Known good fixes`/`## Learned notes`-only structure. |
| AR-017 (Minor) | Fixed the stale "Phase 2 derivation rule" cross-reference in `failure-triage/SKILL.md`; Testing section now names both `test_llm_static_triage.py` and `test_triage_workers.py`; the 23-entry static pattern-file list was replaced with a pointer to the real directory (never duplicated here again). |
| AR-018 (Minor) | `verdict-reporting/SKILL.md` now states explicitly how to recover a group's full scenario list from `failures-for-replay.json`'s `groupId` field when `affectedScenarios` is absent, and how to derive `<replay-timestamp>` from `combined-receipts.json`'s own `replayManifest` field rather than "the most recent directory." |
| AR-019 (Minor) | Removed the unused `requests` dependency from `requirements.txt`; fixed both remaining `iFix.md` references (`_default.md`, `kavach-verdict.schema.json`) to point at `live-replay-diagnosis/SKILL.md`; removed the duplicated `SchemaEquivalenceTests` class from `test_validate_kavach_contract.py` (kept the standalone `test_kavach_verdict_schema_equivalence.py` as sole owner) and cleaned up its now-dead imports; footnoted `verdict-reporting/SKILL.md`'s section-assignment table to explain `script_issue_fix_applied` never actually appears in `combined-receipts.json`. |
| (bonus, Medium, from the kavach-repair skill review) | Made `kavach-repair/SKILL.md`'s Phase 5 PR creation idempotent — checks for an already-open PR on the branch before calling `gh pr create` again. |
| Checkpoint 15 | Not a code defect — left as a manual verification item. This review cannot see GitHub's branch-protection/ruleset configuration; someone with repository admin access should confirm it blocks the Actions bot identity from merging or bypassing required reviews. |

**Verification performed after remediation:** the full `.claude/skills/kavach-diagnose/scripts/tests/` suite (216 passed, 3 subtests passed, 0 failed) passes — the count moved during remediation as new regression tests were added for AR-001/AR-006/AR-012 and the duplicated `SchemaEquivalenceTests` class (5 methods) was removed for AR-019, netting to 216; both `kavach.yml` and `kavach-bootstrap-debug.yml` parse as valid YAML; `git diff` on `config.properties` is empty (fully reverted); the `triage_workers.py` path-resolution fix was independently re-run and confirmed to no longer raise `FileNotFoundError`; every file remaining under `kavach-data/fix-patterns/` was confirmed to match a real feature file under the new slug algorithm.

## Final Verdict (post-remediation)
**Approved with changes**

Both Critical findings are fixed and verified (AR-001's path-resolution fix was re-run directly; AR-010's revert was confirmed via an empty `git diff`), and every Major and Minor finding has a corresponding code or documentation change in this tree, cross-checked against the existing test suite where one exists. This moves the verdict from **Not approved** to **Approved with changes** rather than a clean **Approved** for two reasons, both explicitly out of this review's control: (1) checkpoint 15 (branch protection actually blocking the Actions bot identity from merging or bypassing review) is a GitHub repository setting this code-only review cannot observe and must be confirmed separately by someone with admin access; (2) these fixes have not yet been exercised against a real, live Kavach run end-to-end (a real Cucumber failure set, a real live-replay session) — the test suite, YAML parsing, and direct script re-execution performed here verify the specific defects found, not a full live rehearsal of the pipeline. A live dry-run of `kavach.yml` (or the equivalent interactive session for `kavach-repair`) is the natural next step before this is exercised in anger.
