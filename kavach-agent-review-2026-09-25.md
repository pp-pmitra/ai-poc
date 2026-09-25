# Agent Review: Kavach (kavach-diagnose + kavach-repair)

## Review Metadata
- **Reviewed by:** Agent Reviewer (QA governance perspective)
- **Review date:** 2026-09-25
- **Agent locations:** `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- **Intended runtime:** Claude Code CLI (`claude --agent ...`), invoked (a) unattended in GitHub Actions (`kavach-diagnose` only, via `.github/workflows/kavach.yml`), (b) via a local orchestration script (`.claude/run-mixed-batch.sh`, `kavach-diagnose` only), and (c) interactively by a human (`kavach-repair`, always; `kavach-diagnose`, optionally)

This review supersedes the prior `kavach-agent-review-2026-09-24.md` findings for the current state of the repository. Each of the 15 previously-reported issues is re-verified explicitly below (see "Verification of Prior Findings").

## Files Reviewed

**Agent definitions**
- `.claude/agents/kavach-diagnose.md`
- `.claude/agents/kavach-repair.md`

**Contracts**
- `.claude/contracts/kavach-verdict.schema.json`
- `.claude/skills/kavach-diagnose/scripts/validate_kavach_contract.py` (hand-rolled document validator)
- `.claude/skills/kavach-diagnose/scripts/validate_replay_receipts.py` (`validate_receipt`, `validate_manifest`, `combined_summary` — the actual producer)

**Orchestration / CI**
- `.github/workflows/kavach.yml`
- `.github/workflows/kavach-bootstrap-debug.yml`
- `.claude/run-mixed-batch.sh`
- `.github/CODEOWNERS`

**MCP configuration**
- `.claude/mcp-ci-life.json`, `.claude/mcp-ci-studio.json`

**Skills (reviewed via the `skill-reviewer` skill, run once per skill; results folded in below)**
- `.claude/skills/kavach-diagnose/` (the Python script bundle — `SKILL.md`, `requirements.txt`, `scripts/`, `scripts/tests/`)
- `.claude/skills/failure-triage/`
- `.claude/skills/live-replay-diagnosis/`
- `.claude/skills/verdict-reporting/`
- `.claude/skills/kavach-knowledge/`
- `.claude/skills/kavach-repair/`

**Not modified.** This review is read-only, as instructed.

## Referenced Skill Reviews

| Skill | Skill Reviewer Verdict | Blocking Findings (Required Changes) | Impact on Agent |
|---|---|---|---|
| kavach-diagnose (scripts) | **Approved** | None | No blocking impact — five previously-flagged gaps (test deps, evidence-value gating, whole-document validation, producer/consumer integration test, `append_fix_pattern.py` safety) all independently re-verified fixed, including a live pytest run (219/219 passing). |
| failure-triage | Approved with changes | Strict-mode-violation branch in `static_classifier.disposition()` is dead code that the pipeline's own gate (`enforce_tier1_verdict_constraints`) always overrides to `needs_investigation` — but its own unit tests assert the unreachable, unsafe behavior. Fails safe today; one plausible "fix" to the tests/branch would reopen a false-negative path. | Does not block `kavach-diagnose` today (the live pipeline never emits the unsafe verdict), but the test suite would not catch a regression here. Feed into AR-002 below. |
| live-replay-diagnosis | Approved with changes | (1) `Bash(*)` is granted in every invocation example alongside a `kavach-data/**`-scoped Write/Edit, undermining the stated write boundary; the only compensating control is a post-hoc CI diff-check, and it's absent entirely for manual/interactive runs. (2) A stray "against the Demo app" sentence contradicts the rest of the document's Demo/Pre-release handling. | Directly relevant to `kavach-diagnose`'s tool grants and to `run-mixed-batch.sh`'s manual entry point, which has no boundary check at all. Feeds AR-001 below. |
| verdict-reporting | Approved with changes | `append_fix_pattern.py` failing has no stop-and-report contract (unlike its sibling `append_fix_history.py`), risking a silently-lost fix-pattern on a run that otherwise reports success. | Affects `kavach-diagnose`'s "ready" handoff correctness — a run could report `ready` with a validated `combined-receipts.json` while silently failing to persist the pattern-cache learning. Feeds AR-003 below. |
| kavach-knowledge | Approved with changes | `append_fix_pattern.py`'s bootstrap-header path has a concrete bug against 9 of 12 real, currently-committed placeholder fix-pattern files (0-byte, "already exists" by path but not by content) — the documented guidance to omit `--feature-name` for an existing file produces a permanently headerless file for exactly these files. | Shared by both agents (kavach-diagnose writes, kavach-repair writes). Cosmetic today, but a real defect against this repository's actual state, not a hypothetical. Feeds AR-004 below. |
| kavach-repair | Approved with changes | Three High findings: (1) the `<module>` Maven-coordinate derivation algorithm has no defined behavior for a single-module, root-level-`pom.xml` repo — which is this repo's actual layout, so every `mvn -pl <module>` invocation the skill prescribes would misfire on first real use; (2) the cascade-fix "on approval" branch has no defined outcome (verdict, commit granularity, re-run-still-fails case); (3) the TOOL USE line self-contradicts about whether `fix-history.json` is in the agent's Edit scope. | Directly affects `kavach-repair`'s ability to run correctly in *this* repository at all. This is the single most concrete, verified functional defect found across the whole review. Feeds AR-002 (Critical, agent-level) below. |

## Dependency and Handoff Summary

```
kavach-diagnose (agent)
 ├─ preloads: failure-triage → live-replay-diagnosis → verdict-reporting, kavach-knowledge
 ├─ invokes scripts under: .claude/skills/kavach-diagnose/scripts/*.py (via Bash)
 ├─ writes: kavach-data/history/**, kavach-data/fix-patterns/** (Write grant); CI also grants Edit(kavach-data/**) — see AR-005
 ├─ uses: mcp__playwright__* (only inside live-replay-diagnosis's Phase 3), attached to an already-authenticated CDP session
 ├─ produces: kavach-data/history/replay-verdict-*.md, kavach-data/history/**/combined-receipts.json
 │            (validated against .claude/contracts/kavach-verdict.schema.json, independently re-validated by
 │             .github/workflows/kavach.yml's "Validate combined-receipts.json..." step via validate_kavach_contract.py)
 └─ hands off to: a human or orchestrator, who may then trigger kavach-repair. Never invokes kavach-repair itself.

kavach-repair (agent)
 ├─ preloads: kavach-repair (skill), kavach-knowledge
 ├─ consumes: kavach-data/history/triage-results/<timestamp>/combined-receipts.json (schema-validated input)
 ├─ writes/edits: **/*.feature, src/test/java/stepdefinitions/**, src/main/java/pages/**, kavach-data/fix-patterns/**
 ├─ writes fix-history.json only via: Bash → append_fix_history.py (no direct Edit/Write grant on that file)
 ├─ commits, pushes to a feature branch, opens exactly one PR per run
 └─ terminal stage: never hands off to another agent; a human reviews and merges.

CI (.github/workflows/kavach.yml)
 ├─ run-kavach job: contents: read only. Runs kavach-diagnose unattended, then independently re-validates
 │   combined-receipts.json (validate_kavach_contract.py) and the repo-boundary diff, gating everything downstream
 │   on both (contract_valid && boundary_ok).
 └─ open-fix-pattern-pr job: contents: write, pull-requests: write — the ONLY job with write credentials, and it
    only ever commits kavach-data/fix-patterns/**, fix-history.json, and the verdict report to a
    kavach/learned-patterns-run-<id> branch, then opens a PR to main. Never pushes to main directly; never merges.
```

## Critical Issues

None found at the agent level. No path was found by which either agent can push directly to `main`, merge its own PR, bypass CI's contract/boundary gates, or have `kavach-repair` run unattended. The Critical-severity risks flagged by name in the governing brief (credential exposure, direct-to-main pushes, self-merge, unattended repair) are all structurally prevented — see "Verification of Prior Findings" items 15 and the governance-policy check below.

## Major Issues

### AR-001 — `kavach-diagnose`'s effective write/action surface is materially broader than "Write(kavach-data/\*\*)" implies, with no preventive containment outside CI
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/agents/kavach-diagnose.md` (tools list: `Bash` unrestricted, `Write(kavach-data/**)`); `.claude/run-mixed-batch.sh` (`ALLOWED_TOOLS` — same `Bash(*)` grant, no compensating check); `.github/workflows/kavach.yml`'s "Verify repository boundaries" step (the only compensating check, and it is post-hoc).
- **Problem:** The agent definition's own tool list scopes `Write` to `kavach-data/**`, and the CI workflow's inline comment states plainly that this scoping "is not the real enforcement boundary on its own (Bash(*) is unrestricted and could write anywhere via shell redirection)" — the actual enforcement is a `git status --porcelain` diff check that runs **after** the agent session has already exited. `run-mixed-batch.sh`, the documented manual/local entry point for the identical agent, carries the identical `Bash(*)` grant with **no** equivalent post-hoc check at all — its own comment says so explicitly ("unlike `kavach.yml`'s CI job, this manual entry point has no compensating 'verify repository boundaries' step... Write/Edit scoping here is the only safeguard that actually exists for this path").
- **Impact:** In the CI path, an out-of-scope write (accidental or induced by adversarial page content the diagnosis agent is reading during live replay) is *detected*, not *prevented* — any damage, including exfiltration over the network before the job ends, has already happened by the time the check runs. In the `run-mixed-batch.sh` path, there is no detection at all: nothing stops the agent from overwriting `src/main/java/pages/**`, `.github/workflows/**`, or any other file reachable by the invoking OS user, and no CI-equivalent artifact/log records what happened if it did.
- **Recommendation:** For the CI path, this is an acceptable detective control given the constraints (a preventive sandbox around a full LLM tool loop is a harder problem), but it should be named as a detective, not a preventive, control in the agent's own documentation, not just the workflow comment. For `run-mixed-batch.sh`, either add the same `git status --porcelain` boundary check immediately after the `claude --agent kavach-diagnose` invocation (mirroring `kavach.yml`'s step, and halting/warning the human operator if it fails), or narrow the actual Bash grant to an allowlist of the specific commands the pipeline needs (git status/read commands, `python3` invocations of named scripts, `mvn test -Dtest=TestRunner`, `nc -z`) so a stray write has no reachable command to use.
- **Example:** Append to `run-mixed-batch.sh`, immediately after each `claude --agent kavach-diagnose ...` call:
  ```bash
  changes=$(git status --porcelain --untracked-files=all -- . \
    ':(exclude)target/**' ':(exclude)kavach-data/history/**' ':(exclude)kavach-data/fix-patterns/**')
  if [[ -n "$changes" ]]; then
    echo "ERROR: kavach-diagnose modified files outside its allowed write locations:" >&2
    printf '%s\n' "$changes" >&2
    exit 1
  fi
  ```

### AR-002 — `kavach-repair`'s core Maven-invocation procedure does not match this repository's actual build layout
- **Severity:** Major
- **Category:** Bug
- **Location:** `.claude/skills/kavach-repair/SKILL.md`, Phase 2 step 5 ("Derive Maven coordinates"); this repository's single root-level `pom.xml` with no submodules.
- **Problem:** Confirmed independently against the repo: exactly one `pom.xml` exists, at the repository root, and there is no `automation-tests`-style submodule anywhere `src/test/java/stepdefinitions` or `src/main/java/pages` sit directly under the root. The skill's documented derivation algorithm ("strip the repo root prefix and take the first directory segment that contains a `pom.xml`") has no defined outcome when no such segment exists below the root — every Maven invocation the skill prescribes downstream (the observation run, the `spotless` check, the mandatory three-run verification, and the cascade-regression check) depends on a `<module>` value this algorithm cannot produce for this repository as it exists today.
- **Impact:** This is not a hypothetical edge case — it is the literal, current, verified state of the repository `kavach-repair` is meant to operate against. Left unresolved, the agent has no defined behavior for its very first Maven command on any real run, which would force an ad hoc improvisation with no guidance, undermining the "mechanical, not prose" enforcement the rest of the skill relies on (particularly the three-run verification gate that stands between a proposed fix and a commit).
- **Recommendation:** Add an explicit root-module case to Phase 2 step 5: if a `pom.xml` exists at (or above) the repo root with no submodule-level `pom.xml` below it, treat the module as the repo root itself and omit `-pl` from every Maven invocation, rather than assuming a multi-module layout is the only case.
- **Example:** See the `kavach-repair` skill review's own worked example: *"if a pom.xml exists in an ancestor directory before any submodule is found, the module is the repo root itself: omit -pl entirely (plain `mvn test ...`). Only use `-pl <module>` when a submodule-level pom.xml exists below the root."*

### AR-003 — `kavach-diagnose` can report `status: ready` while silently failing to persist a fix-pattern
- **Severity:** Major
- **Category:** Risk
- **Location:** `.claude/agents/kavach-diagnose.md`, "Responsibilities" item 3 (apply `verdict-reporting` "regardless of whether live replay was needed at all"); `.claude/skills/verdict-reporting/SKILL.md` Phase 5.
- **Problem:** `verdict-reporting`'s Phase 5 has an explicit, mandatory stop-and-report contract for `append_fix_history.py` failing ("stop and print `PHASE_SCRIPT_FAILED: ...`"), but no equivalent instruction exists for `append_fix_pattern.py` failing — no exit-code check, no stop condition, no defined failure message. `kavach-knowledge`'s own SKILL.md describes the fix-pattern library as "the one part of Kavach's output that is never disposable."
- **Impact:** A run whose `append_fix_pattern.py` call fails (lock contention, bad slug, disk issue) will, per the skill's current text, continue straight into build-artifact cleanup and browser close, and be reported by `kavach-diagnose` as a normal `ready` completion with a validated `combined-receipts.json` — because the receipts document's validity is independent of whether the fix-pattern append succeeded. The human or orchestrator receiving that `ready` handoff has no signal that a learning opportunity was silently lost.
- **Recommendation:** Give `append_fix_pattern.py` the same stop-and-report contract `append_fix_history.py` already has in Phase 5, and have `kavach-diagnose`'s own `status` field reflect it (e.g. `blocked` rather than `ready` if the pattern-cache write failed on an otherwise-complete run).
- **Example:** *"If `append_fix_pattern.py` exits non-zero, stop and print `PHASE_SCRIPT_FAILED: append_fix_pattern.py: <stderr>`. Do not proceed to build-artifact cleanup or Phase 6 browser close — treat it the same as a failed `append_fix_history.py` call."*

### AR-004 — Verified, reproducible bug in the shared fix-pattern bootstrap contract against this repo's actual committed files
- **Severity:** Major
- **Category:** Bug
- **Location:** `.claude/skills/kavach-diagnose/scripts/append_fix_pattern.py` (`--feature-name` help text and `append_block()`'s header-bootstrap condition); `kavach-data/fix-patterns/*.md` (9 of 12 tracked files are 0-byte placeholders).
- **Problem:** The script's own documented guidance ("omit `--feature-name` for `_default.md` or any feature file that already exists") is satisfied by path-existence alone. 9 of the 12 fix-pattern files actually committed in this repository are tracked, empty (0-byte) placeholders — they "already exist" by that guidance's literal wording, so an agent correctly following the documented instruction will omit `--feature-name` the first time one of these needs a real fix, and the bootstrap header (`# <Feature> fix patterns` / `## Run log`) that the Shape section says every file has will never be written for it.
- **Impact:** This is a verified defect against the actual repository state today, not a hypothetical — the first real fix recorded against any of these 9 features produces a file that silently diverges from the documented, agent-relied-upon structure.
- **Recommendation:** As given in the `kavach-knowledge` skill review: change the guidance from "omit for a file that already exists" to "omit only for a file that already has content," and verify the bootstrap condition checks `not current.strip()` (which it already does structurally) is what callers are told to key off of — the fix is in the calling guidance, not necessarily the script body.

### AR-005 — CI grants `kavach-diagnose` a broader tool surface than its own agent definition declares
- **Severity:** Major
- **Category:** Inconsistency
- **Location:** `.claude/agents/kavach-diagnose.md` frontmatter `tools:` list (`Write(kavach-data/**)`, no `Edit` grant at all) vs. `.github/workflows/kavach.yml`'s `--allowedTools` string (`...Write(kavach-data/**),Edit(kavach-data/**)...`) and `.claude/run-mixed-batch.sh`'s identical addition.
- **Problem:** The agent definition — the artifact CODEOWNERS treats as one of "the highest-blast-radius paths in this repo" precisely because it "control[s] what an automated agent is allowed to read, write, and execute" — does not grant `Edit` at all. Both real invocation paths (CI and the manual script) grant `Edit(kavach-data/**)` anyway, justified only by an inline comment ("for editing files this run already wrote"). The actual enforced permission set for this agent is therefore defined by two CLI invocation sites, not by the reviewed-and-owned agent file itself.
- **Impact:** A reviewer approving a change to `.claude/agents/kavach-diagnose.md` under CODEOWNERS is reviewing a tool grant that isn't the one actually in force at runtime — the real grant lives in two separate, differently-owned files (a GitHub Actions workflow and a shell script) that could drift from the agent definition, or from each other, without either being flagged as an agent-definition change requiring the same scrutiny.
- **Recommendation:** Add `Edit(kavach-data/**)` to the agent definition's own `tools:` list so the file that CODEOWNERS treats as authoritative for permissions actually is authoritative, and have both invocation sites' `--allowedTools` strings state (in a comment) that they must be kept in exact lockstep with the agent file's declared tools, not silently broadened.

## Minor Issues

### AR-006 — `mcp__playwright__browser_run_code_unsafe` is granted without a stated justification or containment note in the agent definition
- **Severity:** Minor
- **Category:** Improvement
- **Location:** `.claude/agents/kavach-diagnose.md` tools list.
- **Problem:** This is the single most powerful Playwright MCP tool granted to the agent — arbitrary JS execution in the live browser context — and it is plausibly necessary (the evidence-gating design in `live-replay-diagnosis`/`validate_replay_receipts.py` explicitly requires real `.evaluate()`/`.count()` output as artifacts). But the agent definition itself gives no note on why this specific tool is required or what it must never be used for (e.g., must never submit forms, never navigate to a different origin, never execute code intended to alter application state rather than merely observe it).
- **Impact:** Low as currently used (the calling skill's own instructions constrain its use to observation), but a future edit to the skill's prompt text could repurpose this tool for something destructive against a live Demo/Pre-release environment with nothing in the agent definition itself to flag that as out of scope.
- **Recommendation:** Add one sentence to the agent definition's Tools and permissions section: *"`browser_run_code_unsafe` is for read-only DOM/state inspection only (e.g. `.evaluate()`, `.count()`) — never for submitting forms, triggering mutations, or navigating off the app under test."*

### AR-007 — Branch-protection enforcement for the governance policy is documented as a prerequisite, not verified as active
- **Severity:** Minor
- **Category:** Risk
- **Location:** `.github/CODEOWNERS` header comment: *"This file alone does not enable that requirement... Replace the placeholder team/usernames below with the actual maintainers... before relying on this file for enforcement."*
- **Problem:** The governance policy given for this review ("Kavach must not push directly to main," "must not bypass required reviewers, status checks, or branch protection") is correctly designed for at the workflow-permissions level (only one job has `contents: write`, and it only ever opens a PR to a feature branch) — but the file that would make code-owner review *mandatory* explicitly says it does not yet do so on its own, and flags its own team name (`@pulsepoint/qa-automation-maintainers`) as a placeholder pending replacement. This is a repository-settings concern outside what any file in the tree can prove, so it is reported as a residual risk rather than a verified defect.
- **Impact:** If the corresponding branch protection rule ("Require review from Code Owners" on `main`) is not actually turned on in GitHub's repository settings, or if `@pulsepoint/qa-automation-maintainers` is not a real team with real members, Kavach's own generated PRs (and any human's) could merge to `main` without the human-reviewed gate the governance policy requires — not because of anything in this codebase, but because the enforcing setting lives outside it.
- **Recommendation:** Confirm directly in GitHub (Settings → Branches → `main`) that "Require a pull request before merging," "Require review from Code Owners," and "Require status checks to pass" (naming `run-kavach`'s contract/boundary checks) are all enabled, and that the placeholder team name has been replaced with real maintainers. This is an operational verification step, not a code change — flagged here because it is the one governance requirement this review could not confirm from repository contents alone.

### AR-008 — `kavach-repair`'s effective write capability is broader than its "narrowly scoped" framing implies, mitigated only by interactive approval
- **Severity:** Minor
- **Category:** Risk
- **Location:** `.claude/agents/kavach-repair.md` tools list (`Bash` unrestricted alongside path-scoped `Edit`/`Write` grants).
- **Problem:** As with `kavach-diagnose` (AR-001), the `Edit`/`Write` grants are correctly scoped to test source and the fix-pattern library, excluding CI/workflow paths — but unrestricted `Bash` can write anywhere on disk via shell redirection, independent of the `Edit` allowlist. Unlike `kavach-diagnose`'s CI path, there is no post-hoc boundary check here at all; the only real control is that this agent runs interactively (`permissionMode: default`) and an out-of-allowlist Bash command would typically prompt the human operator.
- **Impact:** The claim that write permissions "do not extend to CI/workflow/automation source outside test code" is true for the declared `Edit`/`Write` tool surface, not for the agent's total write capability — the gap is currently closed only by human vigilance at approval time, not by the agent's own design.
- **Recommendation:** Add an explicit instruction (as the `kavach-repair` skill review also recommends) constraining Bash to git/mvn/gh/the two named append scripts, and stating that Bash must never be used as a substitute for a scoped `Edit` call.

## Overall Assessment

### Role and Scope
The two-agent split is correctly drawn and consistently reinforced across every layer reviewed: `kavach-diagnose` diagnoses and never fixes; `kavach-repair` fixes and never diagnoses; only `kavach-repair` can touch application/test source, and only after consuming a schema-validated, independently re-verified `combined-receipts.json`. Neither agent's own definition duplicates the procedural detail that belongs in its preloaded skills — both stay at the level of identity, tool grants, and stopping/handoff rules, which is the correct altitude for an agent definition per this review's own methodology.

### Agent-versus-Skill Separation
Clean. Each agent definition is short, names its preloaded skills, and defers all multi-step procedure to them. `kavach-knowledge` is correctly a non-triggerable, reference-only skill shared by both agents rather than a duplicated procedure in each. `kavach-diagnose`'s reference-only script bundle (also literally named `.claude/skills/kavach-diagnose/`) is unusual in occupying the same directory namespace as the agent, but its own SKILL.md is candid about why it exists and isn't itself invoked as a skill — this is a naming quirk, not a boundary violation.

### Contracts and Handoffs
This is the strongest part of the system. `combined-receipts.json` is validated on both ends — a real JSON Schema and an independently-tested, hand-rolled Python validator kept in lockstep by a dedicated equivalence test — and CI re-runs that validator independently after the Claude session exits, exactly as governance point 5 requires. The producer (`combined_summary()`) and consumer (`validate_kavach_contract.py`/the schema) were confirmed to share the same `groups` document shape, confirmed_product_bug's evidence gate requires real observed *values* (length, placeholder-rejection, and artifact-shape regexes), not mere field presence, and a genuine producer→consumer integration test exists and passes. All of governance points 1–7 are verified resolved at the contract level (see table below). The one contract-adjacent gap is operational, not structural: `verdict-reporting`'s missing failure contract for `append_fix_pattern.py` (AR-003) means the receipts contract can be valid while a *sibling* artifact silently fails.

### Tools, Permissions, and Security
Least-privilege intent is real and mostly enforced: path-scoped `Write`/`Edit` grants, a dedicated CI job split so the only job with `contents: write`/`pull-requests: write` never runs untrusted agent-driven Bash, workflow inputs passed through `env:` rather than interpolated into shell text, and every runtime dependency found (Playwright MCP `@playwright/mcp@0.0.82`, the CI container image `mcr.microsoft.com/playwright:v1.50.0-noble`, the Claude Code CLI version, and Python's `requirements.txt`) is pinned to an exact version — governance points 8 and 9 are both verified resolved. The recurring gap across both agents (AR-001, AR-008) is that `Bash(*)` is granted everywhere, and the stated path-scoping is a detective (CI) or purely human-mediated (manual/`kavach-repair`) control rather than a preventive one — worth fixing, but consistent across the whole system rather than a one-off oversight, and not something that has caused an actual boundary violation per the CI's own passing "Verify repository boundaries" step.

### Failure Handling and Operability
Good in the places that were clearly hardened by real incidents (concurrent-writer locking on `fix-history.json` and fix-pattern files; the `false_product_bug_downgrades` metric; three-run Maven verification with an explicit non-exit-code-driven classification table). Weaker in newer or less-exercised paths: `kavach-repair`'s cascade-approval branch has no defined outcome (folded into AR-002's broader Maven-layout finding as the more severe of the two), and `verdict-reporting`'s Phase 6 has an acknowledged, currently-unowned gap for reaping an orphaned CDP browser session on an unattended CI failure.

### Strengths
- Evidence-value gating for `confirmed_product_bug` is genuinely mechanical and was verified in code, not just documentation: minimum length, placeholder-string rejection, and artifact-shape regex, all exercised by passing tests.
- The document-level contract (`groupCount`, `summaryByGroup`, etc., cross-checked against the `groups` array itself) rejects internally inconsistent artifacts, not just malformed ones — confirmed by reading `validate_kavach_contract.py`'s recomputation logic.
- CI's job-splitting (`run-kavach`: `contents: read` only; `open-fix-pattern-pr`: the sole `contents: write` job, gated on both `contract_valid` and `boundary_ok`) is a well-reasoned, correctly-implemented separation of an untrusted agent loop from any credential capable of changing the repository.
- Every external runtime dependency identified across the whole system is version-pinned; no floating tags were found anywhere in the reviewed chain.
- `kavach-repair`'s non-interactive abort check and three-run Maven classification table are genuinely mechanical gates, not prose guidance, and are enforced before this reviewer's read of the `KAVACH_REPAIR_MODE`/`CI` handling found no path to unattended execution.

### Overall Quality Rating
**Good, approaching Excellent on the contract/CI layer, held back by concrete functional defects in the newer agent-side procedures.** The architecture, governance boundaries, and machine-checkable contract are all sound and, on the specific 15 points this review was asked to re-verify, 13 are cleanly resolved (see table below). The two that remain open (AR-002's Maven-layout bug, and the residual `Bash(*)` containment gap shared by AR-001/AR-008) are concrete, scoped, and fixable rather than design-level problems requiring rework.

## Verification of Prior Findings

| # | Finding | Status | Evidence |
|---|---|---|---|
| 1 | Producer/consumer share the same `groups` document structure | **Resolved** | `combined_summary()`'s output and `validate_kavach_contract.py`/the schema's `$defs.group` were confirmed to describe the same shape; a passing producer→consumer integration test (`ProducerConsumerIntegrationTests`) exercises this directly, including a JSON round-trip. |
| 2 | JSON Schema validates the complete document, not one row | **Resolved** | `.claude/contracts/kavach-verdict.schema.json` requires `totalFailures`, `groupCount`, `summaryByGroup`, `metrics`, `groups`, etc. at the top level, with `groups` itself required non-empty; `validate_kavach_contract.py` mirrors this and additionally cross-checks totals against the `groups` array. |
| 3 | Empty/malformed/mistyped/inconsistent artifacts are rejected | **Resolved** | Confirmed via direct reading of `validate_document()`'s recomputation-and-compare logic, and a dedicated regression test proving the old bare-`rows`-key shape is now rejected rather than silently passing with zero rows checked. |
| 4 | `confirmed_product_bug` evidence gates require correct values, not just presence | **Resolved** | `validate_replay_receipts.py`'s `_artifact_problems()` requires ≥20-char non-placeholder text with a real artifact-shape signal (DOM tag, digit, `.count(`/`.evaluate(`, or quoted literal) for all three checkable gate keys. Noted residual, non-blocking nuance: this closes the "empty/placeholder" gap completely but can only narrow, not fully close, a sufficiently well-crafted *fabricated-but-plausible* artifact (flagged by the `live-replay-diagnosis` skill review as a Medium risk, mitigated by an existing but not machine-tracked human-spot-check recommendation). |
| 5 | GitHub Actions runs the deterministic validator independently after Claude | **Resolved** | `kavach.yml`'s "Validate combined-receipts.json against the kavach-verdict contract" step invokes `validate_kavach_contract.py` after the agent session exits and gates the downstream PR job on its result. |
| 6 | Producer output tested directly against the consumer validator | **Resolved** | `test_validate_kavach_contract.py::ProducerConsumerIntegrationTests` builds a document via the real `combined_summary()` producer and feeds it into the real `validate_document()` consumer. |
| 7 | Python test dependencies declared, full suite runs in CI | **Resolved** | `requirements.txt` (`anthropic`, `httpx`, `lxml`, `pytest`) matches every third-party import found; `kavach.yml` installs from it and runs `pytest .../tests/ -v` (not `unittest discover`, which would silently skip pytest-style files) — confirmed to execute and pass all 18 test files (219 tests) in an independent run. |
| 8 | Workflow inputs passed safely through environment variables and validated | **Resolved** | `feature_path`/`cucumber_tags`/`hold_seconds`/`max_budget_usd` are passed via `env:` blocks, never interpolated directly into `run:` script text; `app`/`environment`/`user_type` are GitHub `choice` inputs restricted to a fixed enum. |
| 9 | Playwright MCP and other runtime dependencies pinned | **Resolved** | `@playwright/mcp@0.0.82` (both MCP configs), `mcr.microsoft.com/playwright:v1.50.0-noble` (CI container, deliberately matched to `pom.xml`'s Playwright Java version), `@anthropic-ai/claude-code@2.1.281` (CI), exact `requirements.txt` pins. No floating tags found. |
| 10 | Diagnosis and repair permissions follow least privilege | **Partially resolved** | Path-scoped `Edit`/`Write` grants are correctly narrow and correctly exclude the other agent's domain in both directions. Residual gap: `Bash(*)` is unrestricted in both agents, so the real containment is either a post-hoc CI diff-check (`kavach-diagnose` in CI) or human approval at the interactive prompt (`kavach-repair`, and `kavach-diagnose` under `run-mixed-batch.sh`), not a preventive tool-level restriction. See AR-001, AR-008. |
| 11 | Diagnosis agent cannot modify automation source | **Resolved, with one documentation caveat** | `kavach-diagnose.md`'s only path-scoped grant is `Write(kavach-data/**)`; it holds no `Edit` grant on `.feature`/stepdefinitions/pages at all. The CI/manual-script practice of granting `Edit(kavach-data/**)` beyond the agent file's own declared tools (AR-005) is a governance-documentation inconsistency, not a boundary violation — the added grant is still confined to `kavach-data/**`. |
| 12 | Repair agent accepts only validated `script_issue_fix_proposed` findings | **Resolved** | Confirmed in the `kavach-repair` skill review: Phase 1 step 1 mechanically filters to the literal string `script_issue_fix_proposed` from the schema-validated `combined-receipts.json`; nothing routes an unvalidated or raw LLM claim into the repair path. |
| 13 | Product/environment/test-data/flaky/inconclusive failures cannot trigger repair | **Resolved** | Same mechanism as #12 — every other verdict value is excluded by construction at Phase 1 step 1, and `validate_receipt()`'s gate logic (governance #4) ensures a receipt can't reach `script_issue_fix_proposed` status through a self-reported claim alone. |
| 14 | Artifact paths unique and unambiguous per workflow run | **Resolved** | `kavach.yml`'s contract-validation step explicitly fails if zero or more-than-one `combined-receipts.json` is found under `kavach-data/history/`; uploaded artifact names are keyed on `${{ github.run_id }}-${{ github.run_attempt }}`; `verdict-reporting`'s own collision-avoidance logic for `replay-verdict-<timestamp>.md` was confirmed present, though noted (in the `verdict-reporting` skill review) as check-then-write rather than lock-protected — a low-probability, not-yet-observed race, not a defect in the "uniqueness" guarantee itself. |
| 15 | Official repository prevents the workflow identity from merging or bypassing review | **Resolved at the workflow-permissions level; unverifiable at the repository-settings level from files alone** | `GITHUB_TOKEN` in the one write-capable CI job is scoped to `contents: write`/`pull-requests: write` only (no `merge`-capable admin scope), and the job only ever runs `gh pr create`, never `gh pr merge` or any auto-merge equivalent — confirmed by reading the entire job. `CODEOWNERS` correctly assigns review ownership over the highest-blast-radius paths, but its own header comment states plainly that the corresponding branch-protection rule must be turned on separately in GitHub's repository settings, which no file in the tree can confirm is actually the case. See AR-007. |

## Required Changes Before Approval

1. Fix `kavach-repair`'s Maven-module derivation to handle this repository's actual single-module, root-level `pom.xml` layout (AR-002) — otherwise the agent's core verification loop cannot run correctly against this codebase as it exists today.
2. Give `append_fix_pattern.py` failures in `verdict-reporting`'s Phase 5 the same stop-and-report contract `append_fix_history.py` already has, and have `kavach-diagnose`'s `status` output reflect a failed pattern-cache write rather than reporting `ready` (AR-003).
3. Fix the `--feature-name`/bootstrap-header guidance in `append_fix_pattern.py`'s help text so "omit for an existing file" means "existing *and non-empty*," closing the concrete, currently-reproducible bug against the 9 placeholder fix-pattern files already committed in this repo (AR-004).
4. Add `Edit(kavach-data/**)` to `kavach-diagnose.md`'s own declared `tools:` list so the CODEOWNERS-protected agent definition is the actual source of truth for its runtime permissions, rather than two separately-owned invocation sites silently broadening it (AR-005).
5. Confirm in GitHub's repository settings (not in this codebase) that "Require review from Code Owners" and required status checks are actually enabled on `main`, and that the CODEOWNERS placeholder team has been replaced with real maintainers (AR-007) — the one governance requirement this review could not verify from files alone.
6. Fix `kavach-repair`'s cascade-fix "on approval" branch (Phase 3.5) to specify its outcome (verdict, commit granularity, and the re-run-still-fails case), and remove the self-contradictory `fix-history.json` mention from the agent's TOOL USE line (both folded into AR-002's severity but distinct, independently-fixable defects — see the `kavach-repair` skill review for the full text).
7. Add a real boundary check (or a narrowed Bash allowlist) to `.claude/run-mixed-batch.sh`, mirroring `kavach.yml`'s "Verify repository boundaries" step, since this is currently the one invocation path for `kavach-diagnose` with no compensating control at all (AR-001).

## Optional Improvements

1. Add a containment note to `kavach-diagnose.md` scoping `browser_run_code_unsafe` to observation-only use (AR-006).
2. Constrain `kavach-repair`'s Bash usage explicitly to git/mvn/gh/the two append scripts, rather than relying solely on interactive approval to catch an out-of-scope write (AR-008).
3. Wire `kavach-knowledge`'s documented supersession mechanism into `verdict-reporting`'s and `kavach-repair`'s actual append phases — it is currently well-designed but unreachable from either producer's step-by-step procedure (per the `kavach-knowledge` skill review).
4. Reconcile the fix-pattern file header-format inconsistency (`# Life_CampaignDashboard` vs. the script's own humanized worked example) per the `kavach-knowledge` skill review.
5. Align `static_classifier.py`'s strict-mode-violation reasoning with `offline_dom_analyzer.py`'s explicit "cannot rule out a product bug from a static snapshot alone" treatment of the identical underlying symptom, and add the integration-level test recommended in the `failure-triage` skill review so a future edit can't silently reopen a false-negative path.
6. Tighten the ≥50% same-run "likely intermittent" boundary in `assertion_analyzer.py` (use `>` instead of `>=`, and/or downgrade confidence near the boundary) per the `failure-triage` skill review.
7. Fix the `Demo`-only wording and the `playwright:browser_navigate` vs. `mcp__playwright__browser_navigate` naming slip in `live-replay-diagnosis`'s SKILL.md.
8. Make the verdict-report filename collision-avoidance in `verdict-reporting` atomic (lock or exclusive-create) rather than check-then-write, and explicitly assign ownership of reaping an orphaned CDP browser session on an unattended CI failure.

## Final Verdict

**Approved with changes**

No Critical findings were identified: the governance boundaries this review was specifically asked to enforce (no direct pushes to `main`, no self-merge, no bypass of required reviewers/status checks, `kavach-repair` never runs unattended) all hold up under direct inspection of the workflow permissions, the agent definitions, and the skill procedures. Thirteen of the fifteen previously-reported findings are cleanly resolved, verified against actual code and, where applicable, a live passing test run rather than taken on faith from comments. The remaining Major findings are concrete, scoped, and independently fixable rather than symptomatic of a deeper design problem: a Maven-layout assumption that doesn't match this repository's real structure (AR-002), a missing failure contract for one bookkeeping script call (AR-003), a reproducible bootstrap bug against files already committed to this repo (AR-004), an agent-definition/runtime-grant drift (AR-005), and a residual `Bash(*)` containment gap shared by both agents that is currently mitigated by a detective CI check or human approval rather than closed by design (AR-001/AR-008). Land the seven items under "Required Changes Before Approval" before trusting `kavach-repair` against this repository in particular, and before treating `kavach-diagnose`'s CODEOWNERS-reviewed permission surface as fully self-describing.
