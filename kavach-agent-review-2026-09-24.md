# Agent Review: Kavach (kavach-diagnose + kavach-repair)

## Review Metadata
- **Reviewed by:** Agent Reviewer (QA governance perspective)
- **Review date:** 2026-09-24
- **Agent locations:** `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- **Intended runtime:** GitHub Actions (`.github/workflows/kavach.yml`, `workflow_dispatch`-triggered) for kavach-diagnose; interactive Claude Code session only for kavach-repair. Both also have documented non-CI/manual invocation paths (`.claude/run-mixed-batch.sh`, ad hoc `claude -p`/`claude --agent` calls).

This review evaluates Kavach as an executable system, not just its SKILL.md prose. It folds in six separate `skill-reviewer` passes (one per referenced skill) and two focused fact-finding passes (CI workflow/MCP/branch-protection; the `combined-receipts.json` contract).

**Governance policy applied:** Kavach may create branches, commit, push to those branches, and open PRs — none of that is treated as a finding. Findings below are scoped to: pushing to `main`, merging its own PRs, bypassing required reviewers/status checks/branch protection, and — per the review brief — the 15 specific previously-identified gaps.

## Files Reviewed
- `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- `.claude/skills/failure-triage/SKILL.md`
- `.claude/skills/live-replay-diagnosis/SKILL.md`
- `.claude/skills/verdict-reporting/SKILL.md`
- `.claude/skills/kavach-repair/SKILL.md`
- `.claude/skills/kavach-knowledge/SKILL.md`
- `.claude/skills/kavach-diagnose/SKILL.md` (script-bundle index) and every script under `scripts/`, `scripts/failure_analyzer/`, and `scripts/tests/`
- `.claude/contracts/kavach-verdict.schema.json`
- `.github/workflows/kavach.yml`, `.github/workflows/kavach-bootstrap-debug.yml`
- `.claude/mcp-ci-life.json`, `.claude/mcp-ci-studio.json`, `.claude/settings.ci.json`
- `.claude/run-mixed-batch.sh`
- `kavach-data/fix-patterns/_default.md` and one per-feature pattern file (sample)
- `src/test/java/hooks/Hooks.java` (cross-checked against a skill's claims about it)

## Referenced Skill Reviews

| Skill | Skill Reviewer Verdict | Blocking Findings | Impact on Agent |
|---|---|---|---|
| kavach-diagnose (script bundle) | APPROVED | None | Contract validation and evidence-gate machinery for `confirmed_product_bug` are solid; feeds directly into this report's item-by-item verification below. |
| failure-triage | APPROVED WITH CHANGES | Offline-DOM "ambiguous selector" case can emit `script_issue_fix_proposed` for a possible product bug, bypassing the tier-1 verdict-constraint gate; hand-off to live-replay-diagnosis under-specifies the required timestamp path | Blocking for the agent: this is a live path by which kavach-diagnose can mis-classify a product bug as a script fix (see AR-001). |
| live-replay-diagnosis | APPROVED WITH CHANGES | `run-mixed-batch.sh` grants unrestricted `Write(*)/Edit(*)`, contradicting the skill's own stated security model; two documented unattended invocations reference nonexistent slash commands; `script_issue_fix_proposed` has no evidence gate | Blocking for the agent: the tool-scoping contradiction (AR-002) is a real path to unauthorized repository writes outside the CI-governed agent definition. |
| verdict-reporting | APPROVED WITH CHANGES | No stop/fail instruction for a missing/invalid `combined-receipts.json` or a rejected `append_fix_history.py` batch | Non-blocking for the agent's core contract, but affects reliability of the `ready` handoff kavach-diagnose reports (AR-009). |
| kavach-repair | APPROVED WITH CHANGES | Phase 6 hand-writes `fix-history.json` instead of routing through the locked `append_fix_history.py`; Phase 3.1 depends on a nonexistent `LocatorProbe` class; failure-artifact fallback doesn't match this repo's actual `Hooks.java` | Blocking for the kavach-repair agent: the agent's own tool grants (`Write`/`Edit` on `fix-history.json`) are what make the bypass possible (AR-003, AR-004, AR-005). |
| kavach-knowledge | APPROVED WITH CHANGES | Shared `.md` fix-pattern files have the same dual-writer topology as `fix-history.json` but no locking | Blocking-adjacent: both agents write here; a lost entry silently degrades the shared pattern library both agents depend on (AR-008). |

## Dependency and Handoff Summary

```
kavach-diagnose agent (CI: kavach.yml, contents:read only)
  ├─ failure-triage skill        → list_failures.py, triage_workers.py (Tier 0/1, incl. undocumented offline-DOM sub-tier)
  ├─ live-replay-diagnosis skill → replay_workers.py, Playwright MCP (browser tools), validate_replay_receipts.py
  ├─ verdict-reporting skill     → writes replay-verdict-*.md, combined-receipts.json, append_fix_history.py, kavach-knowledge
  └─ validates combined-receipts.json against .claude/contracts/kavach-verdict.schema.json
        → status: ready | needs_input | blocked | failed
        → handoff: combinedReceiptsPath, verdictReportPath  (to orchestrator/human only — never auto-invokes kavach-repair)

[CI, independent of the agent session]
  kavach.yml "contract" step → validate_kavach_contract.py <combined-receipts.json>  (deterministic re-check)
  kavach.yml "boundary" step → git status --porcelain diff against an allow-list
  → gates a separate, narrowly-scoped `open-fix-pattern-pr` job (contents:write, pull-requests:write) that only
    commits fix-patterns/history to a branch and opens a PR — never merges, never touches main directly.

kavach-repair agent (interactive only, human-triggered, never by an orchestrator)
  ├─ requires a validated combined-receipts.json (or explicit isolation-mode target)
  ├─ kavach-repair skill  → candidate selection restricted to verdict == script_issue_fix_proposed,
  │                          patch derived only from a matching kavach-knowledge pattern file,
  │                          3x Maven verification + cascade check, human approves every commit/PR
  └─ kavach-knowledge skill → reads/writes shared per-feature .md pattern files (dual-writer with kavach-diagnose)
        → handoff: branchName/prLink — terminal stage, human reviews and merges
```

## Verification of the 15 Previously-Identified Findings

| # | Finding | Status |
|---|---|---|
| 1 | Producer/consumer use the same `groups` document structure | **Resolved.** `combined_summary()`'s return shape, `validate_kavach_contract.py`'s `DOC_REQUIRED_KEYS`/`GROUP_REQUIRED_KEYS`, and `kavach-verdict.schema.json` all agree on a top-level `groups` array, cross-checked by a dedicated equivalence test. |
| 2 | Schema validates the complete document, not one row | **Resolved.** `validate_document()` checks the whole document (top-level keys, every group, and cross-consistency of counts/summaries), not a single receipt. |
| 3 | Empty/malformed/mistyped/inconsistent artifacts are rejected | **Resolved**, and demonstrated: empty `groups: []`, wrong types, count mismatches, and a `confirmed_product_bug` with non-empty `gateProblems` are all explicitly tested and rejected. |
| 4 | All `confirmed_product_bug` evidence gates require correct values, not just presence | **Mostly resolved for `confirmed_product_bug`** (3 of 5 gate keys require real, pattern-matched artifact content, not booleans) — see AR-006 for the residual heuristic-vs-truth gap. **New gap surfaced:** the same discipline is absent for `script_issue_fix_proposed` (AR-007). |
| 5 | GitHub Actions runs the deterministic contract validator independently after Claude | **Resolved.** `kavach.yml`'s `contract` step runs `validate_kavach_contract.py` after the agent step exits, and its output gates the PR job — not agent self-report. |
| 6 | Producer output is tested directly against the consumer validator | **Resolved.** `test_validate_kavach_contract.py` builds a document via the real `combined_summary()` producer and feeds it into the real `validate_document()` consumer, including a JSON round-trip test. |
| 7 | Python test dependencies declared and full suite runs in CI | **Resolved.** `requirements.txt` is installed, and CI runs `pytest .claude/skills/kavach-diagnose/scripts/tests/ -v` against the whole directory (deliberately not `unittest discover`, which would skip pytest-style files). |
| 8 | Workflow inputs passed safely via env vars, validated | **Resolved.** All freeform `workflow_dispatch` inputs go through job-level `env:` bindings, never spliced directly into `run:` script text; only enum-restricted `choice` inputs are ever used in `${{ }}` form, and only in non-executable contexts. |
| 9 | Playwright MCP and other runtime dependencies pinned | **Partially resolved.** `@playwright/mcp@0.0.82` is pinned by exact version string in both MCP configs, but there is no `package.json`/lockfile entry, so its transitive dependency tree is not integrity-locked (AR-012). |
| 10 | Diagnosis and repair permissions follow least privilege | **Partially resolved.** The agent frontmatter for both agents is properly scoped. But `run-mixed-batch.sh` (AR-002) and kavach-repair's own `fix-history.json` write grant (AR-003) each undercut this in practice. |
| 11 | Diagnosis agent cannot modify automation source | **Resolved.** `kavach-diagnose.md` grants `Write(kavach-data/**)` only and explicitly forbids touching `.feature`/stepdefinitions/pages. |
| 12 | Repair agent accepts only validated `script_issue_fix_proposed` findings | **Resolved.** Verdict allow-list is unambiguous and enforced in exactly one place, per the skill-reviewer's simulated execution. |
| 13 | Product/environment/test-data/flaky/inconclusive failures cannot trigger automation repair | **Not fully resolved.** The offline-DOM "ambiguous selector" path (AR-001) is a live counterexample: it can emit `script_issue_fix_proposed` — the verdict kavach-repair acts on — for a symptom its own evidence text admits may be a product bug. |
| 14 | Artifact paths unique/unambiguous per workflow run | **Resolved for CI-uploaded artifacts** (`${{ github.run_id }}-${{ github.run_attempt }}` on every `upload-artifact`). **Not resolved for internal working directories**: `triage-results/<timestamp>`/`replay-packets/<timestamp>` use second-granularity timestamps with `mkdir(exist_ok=True)` and no collision guard; CI's `concurrency: group: kavach-learned-patterns` gate serializes automated runs, but manual/interactive runs sharing a checkout are unprotected (AR-011). |
| 15 | Official repository prevents the workflow identity from merging or bypassing review | **Resolved as far as repo-tracked configuration shows.** No `gh pr merge`/`auto-merge`/elevated-PAT usage exists anywhere; the only write-capable job uses the default `GITHUB_TOKEN` and only creates branches/PRs. **Gap:** there is no CODEOWNERS file and no in-repo evidence that a `main` branch-protection rule (required reviewers/status checks) is actually configured — `README.md:92` only documents this as something a human admin should set up manually (AR-013). |

## Critical Issues

### AR-001 — Offline-DOM ambiguous-selector path can label a possible product bug as a script fix
- **Severity:** Critical
- **Category:** Bug
- **Location:** `failure_analyzer/analyzers/offline_dom_analyzer.py` (multiple-DOM-match branch, `diagnosisKind: "ambiguous_selector"`), called from `triage_workers.py`'s `cause == "unknown"` path
- **Problem:** When static analysis finds more than one DOM node matching a locator, this path returns `verdict: "script_issue_fix_proposed"` with `confidence: "medium"`, even though its own evidence string says the underlying cause ("product rendering duplicate elements") cannot be ruled out. It does not pass through `enforce_tier1_verdict_constraints`, the exact function that is supposed to structurally forbid Tier-1 from confirming anything beyond the three safe outcomes — that gate is only wired into the deterministic-classifier and LLM paths, not this one.
- **Impact:** A genuine product defect (duplicate-rendered elements) can surface as a confident "Script Issue — Fix Proposed" verdict from a tier whose entire design premise is that it cannot make that call. `kavach-repair` then derives a locator-narrowing patch from free text with no structured diff to check against, human review and three green Maven runs will likely pass (the narrowed locator now resolves), and the PR ships a fix that silences the symptom while leaving the real product bug in place — directly violating the governance requirement that product-side failures must not trigger automation repair.
- **Recommendation:** Route `offline_dom_analyzer.py`'s output through the same `enforce_tier1_verdict_constraints` gate the other Tier-1 paths use, and special-case the ambiguous-selector/multiple-match result to `needs_investigation` rather than `script_issue_fix_proposed`.
- **Example:** In the multiple-matches branch, change `verdict = "script_issue_fix_proposed"` to `verdict = "needs_investigation"`, keeping the existing evidence text, so the case escalates to live replay instead of resolving statically.

### AR-002 — `run-mixed-batch.sh` grants unrestricted `Write(*)/Edit(*)` with no compensating boundary check
- **Severity:** Critical
- **Category:** Risk
- **Location:** `.claude/run-mixed-batch.sh` (`ALLOWED_TOOLS="Bash(*),Read,Write(*),Edit(*),Grep,Glob,..."`), contradicting `live-replay-diagnosis/SKILL.md`'s own stated security model
- **Problem:** `live-replay-diagnosis/SKILL.md` states plainly that `Write`/`Edit` scoping to `kavach-data/**` is the only real safeguard outside CI, since `Bash(*)` is already unrestricted. Its own documented mixed-batch entry point, `run-mixed-batch.sh`, ignores that and launches every session with fully unrestricted `Write(*)/Edit(*)` — the opposite of every other worked example in the same file. This script is never invoked from `kavach.yml`, so the CI "verify repository boundaries" `git status` check — the actual backstop for an unrestricted `Bash(*)` — does not exist for this path.
- **Impact:** A live-replay session already holding `Bash(*)` and `browser_run_code_unsafe`, run through this script, can write or edit any file in the repository — application source, `.feature` files, CI workflow files — with no compensating check anywhere in that path. This is a credible, documented route to unauthorized repository-wide writes, not a hypothetical one.
- **Recommendation:** Scope `run-mixed-batch.sh`'s `ALLOWED_TOOLS` to `Write(kavach-data/**),Edit(kavach-data/**)`, matching every other worked example, or add an equivalent repository-boundary check inside the script itself before it ships as a documented entry point.
- **Example:**
  ```diff
  - ALLOWED_TOOLS="Bash(*),Read,Write(*),Edit(*),Grep,Glob,\
  + ALLOWED_TOOLS="Bash(*),Read,Write(kavach-data/**),Edit(kavach-data/**),Grep,Glob,\
  ```

## Major Issues

### AR-003 — kavach-repair hand-writes `fix-history.json`, bypassing the locked/validated writer
- **Severity:** Major
- **Category:** Bug
- **Location:** `.claude/skills/kavach-repair/SKILL.md` Phase 6; `.claude/agents/kavach-repair.md` tool grants (`Write`/`Edit` on `kavach-data/history/fix-history.json`)
- **Problem:** kavach-repair's Phase 6 shows a raw JSON template and instructs a direct append, and the agent is granted raw `Write`/`Edit` on that exact file — reintroducing the read-modify-write race `append_fix_history.py`'s `flock` was purpose-built to prevent, on the one file both kavach-diagnose (via `verdict-reporting`) and kavach-repair write to. The shown template also omits four keys (`confidence`, `priority`, `isGroupRepresentative`, `review`) that `append_fix_history.py` requires.
- **Impact:** A kavach-repair run finishing close to a kavach-diagnose/verdict-reporting run can silently drop one side's entries with no error, and every entry kavach-repair writes is schema-incomplete, silently breaking `review_verdicts.py` and the `attempts >= 2` skip logic that reads this file.
- **Recommendation:** Route Phase 6 through `append_fix_history.py` exactly as `verdict-reporting` does, with the full field set; narrow the agent's own `Edit`/`Write` grant so it targets `Bash` (to invoke the script) rather than the file directly.
- **Example:** `cat entries.json | python3 .claude/skills/kavach-diagnose/scripts/append_fix_history.py`, entry including all twelve required keys.

### AR-004 — `LocatorProbe` referenced by kavach-repair does not exist
- **Severity:** Major
- **Category:** Bug
- **Location:** `.claude/skills/kavach-repair/SKILL.md` Phase 3.1 ("Use `utils.LocatorProbe` for live disambiguation"); `.claude/agents/kavach-repair.md` line 38
- **Problem:** `src/main/java/utils/LocatorProbe.java` does not exist anywhere in the repository, and the agent's `Edit`/`Write` grants don't cover `src/main/java/utils/**` even if it wanted to create one.
- **Impact:** Phase 3.1's live-disambiguation guidance — part of the anti-guessing safeguard — points at a tool that cannot be invoked or built by this agent, so an agent following it literally either stalls or silently skips the guidance.
- **Recommendation:** Either ship `LocatorProbe.java` and grant the agent a narrow `Edit`/`Write` scope on that one file, or rewrite Phase 3.1 to use tools the agent actually has (a temporary inline probe statement, verified through the existing three-run Maven check, removed before commit).

### AR-005 — kavach-repair's failure-artifact fallback doesn't match this repo's `Hooks.java`
- **Severity:** Major
- **Category:** Inconsistency
- **Location:** `.claude/skills/kavach-repair/SKILL.md` Phase 3.1 ("via `hooks.Hooks.saveFailureArtifacts`, if the target repo's hooks include it")
- **Problem:** This repo's actual `Hooks.java` has no `saveFailureArtifacts` method and does not produce `page-source.html`/`screenshot.png`/`failure-context.json` under `target/failure-artifacts/**`; it attaches a screenshot and a Playwright trace to the Cucumber report instead. The skill hedges ("if... include it") but gives no fallback for the case where they don't — which is this repo's actual state.
- **Impact:** Phase 3.1's core "read DOM and screenshot before proposing a fix, never DOM-only" rule has no artifacts to act on in this repo today, with no documented alternative.
- **Recommendation:** Add an explicit fallback to the trace `.zip`/Cucumber-attached screenshot this repo's `Hooks.java` actually produces, and only skip DOM/screenshot inspection (flagging it explicitly) if neither exists.

### AR-006 — Documented unattended invocation commands don't exist
- **Severity:** Major
- **Category:** Bug
- **Location:** `live-replay-diagnosis/SKILL.md` Phase 0 (`claude -p "/analyze-failure"`); `run-mixed-batch.sh` (`claude -p "/kawach"`)
- **Problem:** Neither `/analyze-failure` nor `/kawach` exists anywhere in the repo (there is no `.claude/commands/` directory at all). The only proven-working invocation is `kavach.yml`'s `claude --agent kavach-diagnose --print "..."`.
- **Impact:** An operator following the skill's own non-CI instructions verbatim issues a command that doesn't exist, producing an error or a degraded, unscoped run instead of a diagnosis.
- **Recommendation:** Replace both with the proven `claude --agent kavach-diagnose --print "..."` pattern, or create the slash commands for real.

### AR-007 — No evidence gate for `script_issue_fix_proposed`
- **Severity:** Major
- **Category:** Bug
- **Location:** `.claude/skills/kavach-diagnose/scripts/validate_replay_receipts.py`, `validate_receipt()` (lines 107–143)
- **Problem:** `confirmed_product_bug` and `suspected_product_bug`/`not_reproduced_passed_live` each have a mechanical evidence check (live-replay proof, non-empty evidence, and — for confirmed — content-validated artifacts). `script_issue_fix_proposed` — the verdict that most directly leads to kavach-repair editing code — has none: it falls straight through to `return verdict, []`.
- **Impact:** A receipt of `{"verdict": "script_issue_fix_proposed", "evidence": [], ...}` passes unchanged into `combined-receipts.json`, with zero mechanical proof any live verification happened, even though this is the verdict class the whole pipeline exists to hand toward a real code change. kavach-repair's own pattern-match + 3x-Maven gate is a real downstream mitigation, but the receipt itself is untrustworthy on this point.
- **Recommendation:** Add a gate branch mirroring the lighter one already used for `suspected_product_bug`: require `liveReplayPerformed is True` and non-empty `evidence`, downgrading to `needs_investigation` otherwise.

### AR-008 — Shared fix-pattern `.md` files have no concurrency protection
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/skills/kavach-knowledge/SKILL.md`, "Read/write contract"
- **Problem:** Both kavach-diagnose (via `verdict-reporting`) and kavach-repair append to the same per-feature `.md` pattern file, with no lock and no script mediating the write — the identical dual-writer topology `fix-history.json` was explicitly re-architected (via `append_fix_history.py`'s `flock`) to protect against, but the lesson wasn't carried forward here.
- **Impact:** Two close-together appends (a CI diagnosis run and a human-run kavach-repair session against the same checkout) can silently drop one side's entry, with no error and no way to detect it later — undermining the file's stated purpose as "never disposable."
- **Recommendation:** Add a lightweight `flock`-guarded append writer analogous to `append_fix_history.py`, or explicitly document the assumption the current design relies on (that the two writers never realistically collide) so a future reader can judge whether it still holds.

### AR-009 — verdict-reporting has no failure path for its own two critical operations
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/skills/verdict-reporting/SKILL.md` Phase 4 (reading `combined-receipts.json`) and Phase 5 (`append_fix_history.py`)
- **Problem:** Every other script invocation across the pipeline has an explicit "stop and print `PHASE_SCRIPT_FAILED: ...`" convention. This skill — whose entire job is "always produce a report" — has none for its own input being missing/invalid, or for `append_fix_history.py` rejecting the whole batch.
- **Impact:** A missing/corrupt `combined-receipts.json` leaves the agent to improvise (fabricate, produce nothing, or stall); a rejected history batch means a run's bookkeeping silently fails while the verdict report looks complete, with no signal to a human that `fix-history.json` didn't actually update.
- **Recommendation:** Add explicit stop/print instructions for both cases, matching the sibling skills' convention, and state that the run isn't complete until `append_fix_history.py` succeeds.

### AR-010 — failure-triage's hand-off under-specifies what live-replay-diagnosis needs
- **Severity:** Major
- **Category:** Risk
- **Location:** `failure-triage/SKILL.md` Phase 2.5, "hand its groupIds off to the live-replay-diagnosis skill"
- **Problem:** `live-replay-diagnosis` needs the full `<triage-timestamp>` directory path twice (for `--only-groups` and later `--triage-manifest`), but failure-triage's instruction only says to hand off `groupIds`.
- **Impact:** This only works today because both skills run in one continuous agent session with the timestamp still in context. Split across separate invocations (the pattern the repo's own Life/Studio bootstrap examples use), live-replay-diagnosis has no way to locate the manifest it needs.
- **Recommendation:** State explicitly that the resolved timestamp directory path, not just the `groupIds` array, is part of the hand-off.

### AR-011 — Timestamp-based artifact directories have no collision guard outside CI
- **Severity:** Major
- **Category:** Risk
- **Location:** `triage_workers.py` / `replay_workers.py`, `out_dir = out_root / timestamp; out_dir.mkdir(parents=True, exist_ok=True)`
- **Problem:** Second-granularity timestamps with no PID/nonce suffix and no collision detection. `kavach.yml`'s `concurrency: group: kavach-learned-patterns` gate serializes CI-triggered runs, but a manual/interactive invocation sharing the same checkout as another run is not protected.
- **Impact:** Two runs landing in the same wall-clock second can interleave one run's manifest/receipt files with another's, and downstream skills (which treat `combined-receipts.json` as the single reconciled source) have no way to detect the contamination.
- **Recommendation:** Have `verdict-reporting` defensively verify that `combined-receipts.json`'s `triageManifest`/`replayManifest` paths match the timestamps this run itself produced; longer-term, add a PID/random suffix to the directory name and fail loudly on a non-empty target directory.

## Minor Issues

### AR-012 — Playwright MCP pin has no lockfile behind it
- **Severity:** Minor
- **Category:** Risk
- **Location:** `.claude/mcp-ci-life.json` / `.claude/mcp-ci-studio.json` (`@playwright/mcp@0.0.82`)
- **Problem:** The exact version is pinned in the `npx` invocation string, but `@playwright/mcp` is absent from `package.json`/`package-lock.json`, so its transitive dependencies aren't integrity-locked and every run fetches from the registry live.
- **Recommendation:** Vendor `@playwright/mcp` as a `package.json` devDependency with a committed lockfile entry.

### AR-013 — No in-repo evidence of branch-protection enforcement on `main`
- **Severity:** Minor
- **Category:** Risk
- **Location:** `README.md:92`; absence of a `CODEOWNERS` file
- **Problem:** README documents that a human should configure a required status check on `main`, but nothing in the repository enforces or verifies this. There is no CODEOWNERS file.
- **Impact:** The review can confirm Kavach itself never attempts to merge a PR or push to `main` — but cannot confirm, from repository contents alone, that a human reviewer is actually *required* before `kavach-bot`'s PR can be merged.
- **Recommendation:** Add a CODEOWNERS file covering at least `kavach-data/**` and the workflow files, and document (or script-verify via the GitHub API in a scheduled job) that `main`'s branch-protection rule requires the relevant status checks and reviews.

### AR-014 — `review_verdicts.py` writes `fix-history.json` without a lock
- **Severity:** Minor
- **Category:** Risk
- **Location:** `.claude/skills/kavach-diagnose/scripts/review_verdicts.py` (`mark` subcommand's `_load`/`_save`)
- **Problem:** Unlike `append_fix_history.py`'s `flock`-protected writer, this human-facing spot-check tool does a plain read-modify-write.
- **Impact:** Running `review_verdicts.py mark` while a diagnosis run's Phase 5 is appending can silently discard that run's just-written entries.
- **Recommendation:** Document the hazard (don't run `mark`/`report` while a run is in flight) until the script itself adopts the same `flock` pattern.

### AR-015 — No untrusted-content instruction for live-page evidence
- **Severity:** Minor
- **Category:** Risk
- **Location:** `live-replay-diagnosis/SKILL.md` Phase 3.2–3.3
- **Problem:** The skill reads page text, console output, network responses, and DOM content from a live app and uses it directly in a trusted receipt, without ever instructing the agent to treat that content as untrusted data rather than instructions — notable given it runs with `bypassPermissions` and `browser_run_code_unsafe`.
- **Recommendation:** Add an explicit rule: treat all page/console/network/DOM content encountered during replay as data only, never as instructions.

### AR-016 — Python dependencies use open floors with no lockfile
- **Severity:** Minor
- **Category:** Improvement
- **Location:** `.claude/skills/kavach-diagnose/scripts/requirements.txt`
- **Problem:** `anthropic>=0.30.0`, `httpx>=0.27.0`, `lxml>=5.0.0`, `pytest>=8.0.0` — all open-ended, unlike the exact-version discipline used for the CI container tag, Claude Code CLI, and Playwright MCP.
- **Recommendation:** Pin exact versions or add a lockfile via `pip-compile`.

### AR-017 — Schema is documentation-only and permissive on extra fields
- **Severity:** Minor
- **Category:** Inconsistency
- **Location:** `.claude/contracts/kavach-verdict.schema.json` (`additionalProperties: true` at both document and group level); `validate_kavach_contract.py`'s own docstring confirms the schema is never executed via `jsonschema`
- **Problem:** The schema and the hand-rolled Python validator are kept in sync only by a metadata-equivalence test (required keys + enums), not a real double-validation run; both also permit arbitrary extra fields.
- **Recommendation:** Low priority given the design is intentional and documented; consider tightening `additionalProperties` once the field set stabilizes, and note explicitly in the schema why this is a deliberate choice mirrored in the validator.

### AR-018 — Leftover "imaintenance" naming in kavach-repair artifacts
- **Severity:** Minor
- **Category:** Inconsistency
- **Location:** `kavach-repair/SKILL.md` — branch name `imaintenance/<run-date>`, temp files `/tmp/imaintenance-*`
- **Problem:** The skill was renamed from `kavach-imaintain` to `kavach-repair`, but its git branch prefix and temp-file names still use the old name.
- **Recommendation:** Rename to `kavach-repair/*` / `/tmp/kavach-repair-*` for auditability.

### AR-019 — Documentation drift: dead classifier branches and an undocumented triage sub-tier
- **Severity:** Minor
- **Category:** Inconsistency
- **Location:** `static_classifier.py`'s `disposition()` (`cause == "timeout"`/`"assertion-error"` branches, unreachable); `failure-triage/SKILL.md`'s Phase 2.5 tier list (omits the offline-DOM sub-tier entirely)
- **Problem:** Two classifier branches can never execute given `analyze_failure()`'s real cause vocabulary; separately, an entire deterministic sub-tier (offline DOM analysis, the same one behind AR-001) is invisible in the skill's own description of how Phase 2.5 works.
- **Recommendation:** Reconcile the dead branches with the real cause vocabulary, and document the offline-DOM tier explicitly in the ordered Phase 2.5 list.

### AR-020 — kavach-knowledge dedup criteria are an undefined judgment call
- **Severity:** Minor
- **Category:** Improvement
- **Location:** `kavach-knowledge/SKILL.md`, "Dedup criteria"
- **Problem:** "Same broken locator/pattern with the same fix" has no concrete comparison rule, so two agents can reasonably diverge on whether an entry is a duplicate.
- **Recommendation:** Give one concrete anchor, e.g. same normalized locator string and target file.

## Overall Assessment

### Role and Scope
The two-agent split is well-conceived: kavach-diagnose is read-mostly (browser + `kavach-data/**` only) and structurally forbidden from touching automation source; kavach-repair is the only agent that edits real files, is explicitly human-triggered, and is a terminal stage with no further hand-off. This is the right shape for a system that makes test-failure classification and code-edit decisions.

### Agent-versus-Skill Separation
Clean. Each agent's `.md` file holds identity, tool grants, and stopping rules; the reusable procedures live entirely in the six skills. No duplication of procedure text between an agent file and its skills was found.

### Contracts and Handoffs
The core `combined-receipts.json` contract is now genuinely solid — a real shared `groups` structure, a whole-document validator, and producer-output-through-real-validator integration tests. The weak points are at the edges of that contract: the evidence gate for the pipeline's most consequential verdict (`script_issue_fix_proposed`, AR-007), the classification path that can mislabel a product bug in the first place (AR-001), and two under-specified inter-skill hand-offs (AR-009, AR-010).

### Tools, Permissions, and Security
The CI-governed path (`kavach.yml` → `--agent kavach-diagnose`) is well designed: read-only agent job, a separately-scoped PR-opening job, safe `env:`-based handling of untrusted inputs, and an independent deterministic gate before any write-capable job runs. The documented alternate paths are where the real gaps are: `run-mixed-batch.sh`'s tool-scope contradiction (AR-002) and kavach-repair's own file-write grant enabling a bypass of its sibling's locked writer (AR-003).

### Failure Handling and Operability
Most of the pipeline has a consistent, explicit `PHASE_SCRIPT_FAILED`-style stop convention. verdict-reporting is the one skill missing it for its own inputs (AR-009), which matters because it's the stage responsible for guaranteeing every run ends in a real, traceable artifact.

### Strengths
- The `confirmed_product_bug` evidence gate is genuinely strong and mechanically enforced, not self-reported.
- Producer/consumer contract testing is real integration testing, not hand-rolled fixtures.
- Governance boundaries (no self-merge, no direct `main` push, no auto-merge, default-scope `GITHUB_TOKEN` only) hold up cleanly everywhere they were checked in `kavach.yml`.
- Both agent definitions state their stopping/handoff rules unambiguously and are internally consistent with their own tool grants (except where noted above).

### Overall Quality Rating
**Adequate.** The production CI path and the core data contract are close to production-ready, but a live counterexample to "product bugs can't trigger automation repair" (AR-001) and a documented, unauthorized-write-capable alternate invocation path (AR-002) are real, not hypothetical, gaps — plus several Major-severity contradictions between what skills claim and what the repository actually contains.

## Required Changes Before Approval
1. Fix `offline_dom_analyzer.py`'s ambiguous-selector case to escalate to `needs_investigation` and route through `enforce_tier1_verdict_constraints` (AR-001).
2. Scope `run-mixed-batch.sh`'s `ALLOWED_TOOLS` to `kavach-data/**`, or add an equivalent repository-boundary check inside it (AR-002).
3. Route kavach-repair's `fix-history.json` writes through `append_fix_history.py`, and narrow the agent's direct file grant accordingly (AR-003).
4. Resolve the `LocatorProbe` dangling reference — ship the class with a narrow permission grant, or rewrite Phase 3.1 without it (AR-004).
5. Add a working failure-artifact fallback in kavach-repair Phase 3.1 for repos whose `Hooks.java` doesn't implement `saveFailureArtifacts` (AR-005).
6. Replace the `/analyze-failure` and `/kawach` slash-command references with the proven `--agent kavach-diagnose` invocation (AR-006).
7. Add an evidence gate for `script_issue_fix_proposed` in `validate_receipt()` (AR-007).
8. Add locking (or an explicitly documented safety rationale) for the shared `.md` fix-pattern writes (AR-008).
9. Add `PHASE_SCRIPT_FAILED`-style stop instructions to verdict-reporting for a missing/invalid `combined-receipts.json` and a rejected `append_fix_history.py` batch (AR-009).
10. Make the failure-triage → live-replay-diagnosis hand-off explicitly include the triage-timestamp directory path (AR-010).

## Optional Improvements
1. Add a defensive check in verdict-reporting that `combined-receipts.json`'s manifest paths match this run's own timestamps (AR-011).
2. Vendor `@playwright/mcp` with a committed lockfile (AR-012).
3. Add a CODEOWNERS file and verify/document `main`'s branch-protection configuration (AR-013).
4. Add locking to `review_verdicts.py`'s writer, or document the concurrency hazard in the interim (AR-014).
5. Add an untrusted-content rule to live-replay-diagnosis Phase 3.2 (AR-015).
6. Pin exact Python dependency versions or add a lockfile (AR-016).
7. Note the schema's intentional permissiveness explicitly, or tighten `additionalProperties` (AR-017).
8. Rename leftover "imaintenance" branch/temp-file naming to "kavach-repair" (AR-018).
9. Reconcile dead classifier branches and document the offline-DOM triage sub-tier (AR-019).
10. Give kavach-knowledge's dedup criteria a concrete comparison anchor (AR-020).

## Final Verdict
**Not approved**

The CI-automated production path and the core `combined-receipts.json` contract are well-engineered and largely resolve the 15 previously-identified gaps — items 1, 2, 3, 5, 6, 7, 8, 11, and 12 are cleanly closed. But two Critical issues remain live in the shipped bundle: a real classification path (AR-001) that can label a possible product bug as an actionable script fix, directly contradicting the governance requirement that product-side failures must never trigger automation repair, and a documented alternate invocation script (AR-002) that grants unrestricted repository write access with no compensating boundary check outside the CI workflow. Neither requires restructuring the system — both are small, scoped, mechanical fixes — but per the review's own severity rubric, either one is sufficient to withhold approval until fixed. Combined with several Major-severity contradictions between what the skills document and what the repository actually contains (AR-003 through AR-011), this should be re-reviewed after the Required Changes land rather than approved as-is.
