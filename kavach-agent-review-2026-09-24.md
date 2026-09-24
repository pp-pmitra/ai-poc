# Agent Review: Kavach (kavach-diagnose + kavach-repair) — Post-Remediation Independent Re-Audit

## Review Metadata
- **Reviewed by:** Agent Reviewer (QA governance perspective), with independent skill-reviewer sub-reviews for all six referenced Kavach skills
- **Review date:** 2026-09-24
- **Agent locations:** `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- **Intended runtime:** Claude Code `--agent` mode, interactively (kavach-repair, always) and unattended in GitHub Actions (kavach-diagnose, via `.github/workflows/kavach.yml`)

**Why this report exists in this form:** an earlier review (same day) found 2 Critical, 8 Major, and 9 Minor findings. A remediation pass (commit `2e724504`, "Fix all findings from Kavach agent-level production-readiness review") claimed to fix all of them. This report is a **second, independent audit** that does not trust that commit message — every claimed fix was re-verified against the actual current file contents, by re-running scripts, re-running the full test suite, and diffing tool-grant strings byte-for-byte, not by reading the commit's own description of itself. Six background skill-reviewer sub-agents each re-examined one skill from scratch; the lead reviewer independently re-checked the CI workflow, the contract schema, the fix-pattern directory, and the test suite in parallel. **This second pass itself found three small-but-real regressions the remediation had introduced or missed** (detailed below) — these have also been fixed and re-verified as part of producing this report.

## Files Reviewed
- `.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`
- `.claude/skills/failure-triage/SKILL.md`
- `.claude/skills/live-replay-diagnosis/SKILL.md`
- `.claude/skills/verdict-reporting/SKILL.md`
- `.claude/skills/kavach-knowledge/SKILL.md`
- `.claude/skills/kavach-repair/SKILL.md`
- `.claude/skills/kavach-diagnose/SKILL.md` (new reference-only index, added during remediation) and all scripts/tests under `.claude/skills/kavach-diagnose/scripts/`
- `.claude/contracts/kavach-verdict.schema.json`
- `.claude/mcp-ci-life.json`, `.claude/mcp-ci-studio.json`
- `.github/workflows/kavach.yml`, `.github/workflows/kavach-bootstrap-debug.yml`
- `src/test/resources/config/config.properties`, `src/main/java/factory/DriverFactory.java`
- `kavach-data/fix-patterns/**` (all 13 files) and all 42 real `.feature` files under `src/test/resources/features/`
- `.claude/run-mixed-batch.sh`

## Referenced Skill Reviews

| Skill | Skill Reviewer Verdict | Blocking Findings | Impact on Agent |
|---|---|---|---|
| failure-triage | Approved | None remaining — the previously Critical `triage_workers.py` path bug was reproduced-then-confirmed-fixed by actually running the documented command; the sub-review found 2 new Medium stale cross-references left over from the remediation edit itself, both fixed during this pass | None — core triage path verified executable |
| kavach-diagnose scripts (new SKILL.md) | Approved | None — all 7 specifically-targeted remediation claims independently confirmed by direct code inspection, plus a from-scratch pytest run (216 passed) | None |
| kavach-knowledge | Approved | None — the Critical slug-derivation bug was independently re-derived by hand against all 42 real feature files and all 13 fix-pattern files, with zero mismatches found; one Low (non-blocking) improvement suggestion only | None |
| kavach-repair | Approved with changes → fixed to Approved | All 6 claimed fixes and all 4 governance-critical properties confirmed intact; found one genuine Medium (a Phase-2 step-number cross-reference off by one) plus a cosmetic duplicate horizontal rule — both fixed during this pass | None remaining |
| live-replay-diagnosis | Approved with changes → fixed to Approved | The evidence-gate extension (3rd `productBugGate` artifact field) fully confirmed; the tool-grant hardening was **only partially correct** — the remediation dropped `Write(kavach-data/**)` from all three worked examples, diverging from what `kavach.yml` actually grants and from what this skill's own documented manual-receipt-writing fallback needs — fixed during this pass, plus an added note disclosing that `Bash(*)` remains the real residual risk | None remaining |
| verdict-reporting | Approved | All 6 claimed fixes independently verified against their actual dependencies (`kavach-knowledge`'s algorithm, `validate_replay_receipts.py`'s real field names, `run-mixed-batch.sh`'s real sequencing) — not just plausible-sounding prose; two Low optional-improvement suggestions only | None |

## Dependency and Handoff Summary

Unchanged from the original review's map — the remediation did not alter the pipeline's shape, only closed gaps in it:

```
kavach-diagnose (agent) --preloads--> failure-triage -> live-replay-diagnosis -> verdict-reporting -> kavach-knowledge
  writes: kavach-data/** only (Write(kavach-data/**), no bare Edit); CI grants Write+Edit(kavach-data/**) together
  handoff -> kavach-data/history/triage-results/<ts>/combined-receipts.json, schema-validated twice
    (once in-session, once independently by validate_kavach_contract.py as a separate CI step)

kavach-repair (agent, human-triggered only, never CI)
  input: combined-receipts.json, filtered to verdict == "script_issue_fix_proposed" only
  edits: **/*.feature, stepdefinitions/**, pages/**, its own kavach-data write paths
  verification: mandatory 3x Maven green before any commit
  output: one branch, one PR — terminal stage
```

## Checkpoint-by-Checkpoint Re-Verification (all 15, independently re-run)

| # | Checkpoint | Status | Evidence |
|---|---|---|---|
| 1 | Producer/consumer share the same `groups` structure | **Confirmed holding** | `python3 -c` load of the schema: top-level `required` includes `groups`; unchanged by remediation, re-verified directly |
| 2 | Schema validates the full document | **Confirmed holding** | Same check as above |
| 3 | Malformed/empty/inconsistent artifacts rejected | **Confirmed holding** | `ValidatorRejectsRealisticBreakageTests` re-run, all pass |
| 4 | `confirmed_product_bug` gates require correct values | **Confirmed fixed** — now 3 of 5 gate fields (was 2) | `REQUIRED_ARTIFACT_KEYS` grep'd directly: `("domStructureChecked", "staleLocatorRuledOut", "testDataOrEnvironmentRuledOut")`; distinct regex per key (`_ARTIFACT_SIGNAL_RE_BY_KEY`) confirmed by two independent sub-reviews reading the code |
| 5 | GitHub Actions independently re-runs the deterministic validator | **Confirmed holding** | `kavach.yml`'s `contract` step, unchanged |
| 6 | Producer output tested directly against consumer validator | **Confirmed holding** | `ProducerConsumerIntegrationTests` re-run, passes |
| 7 | Python deps declared, full suite runs in CI | **Confirmed holding** | `requirements.txt` re-read (no `requests`); full suite re-run independently by the lead reviewer AND by two separate sub-agents: all three runs report `216 passed, 3 subtests passed, 0 failed` |
| 8 | Workflow inputs passed safely via env vars | **Confirmed holding** | Unchanged by remediation |
| 9 | Playwright MCP and other deps pinned | **Confirmed holding** | `@playwright/mcp@0.0.82`, `playwright:v1.50.0-noble`, `CLAUDE_CODE_VERSION: "2.1.281"` all re-read directly |
| 10 | Least privilege | **Confirmed fixed, with one correction made this pass** | See live-replay-diagnosis finding below — the remediation itself had a gap here that this re-audit caught and fixed |
| 11 | Diagnosis agent cannot modify automation source | **Confirmed holding** | Boundary-check step + its new `boundary_ok` gate on the PR job, re-read directly in `kavach.yml` |
| 12 | Repair agent accepts only `script_issue_fix_proposed` | **Confirmed holding** | Allow-list wording unchanged and re-verified by the kavach-repair sub-review |
| 13 | Other classifications cannot trigger repair | **Confirmed holding** | Same allow-list mechanism |
| 14 | Artifact paths unique per run | **Confirmed fixed** | Verdict-report filename now `HHmmss` + explicit collision-suffix rule, re-verified by the verdict-reporting sub-review against the actual current text |
| 15 | Repo prevents the workflow identity from merging/bypassing review | **Still not verifiable from code** — unchanged limitation | This remains a GitHub branch-protection/ruleset setting outside this repository's tracked files; the workflow's own token scoping (`open-fix-pattern-pr` only ever gets `contents: write`/`pull-requests: write`, never attempts a merge) is consistent with the stated governance policy, but confirming the platform actually enforces it requires someone with repository admin access to check Settings → Rules directly |

## Issues Found and Fixed During This Re-Audit

These are new findings this second pass surfaced — either things the first remediation pass missed entirely, or small inconsistencies the remediation edits themselves introduced. All were fixed and re-verified (full test suite re-run green, YAML re-validated) before this report was finalized.

### RA-001 — live-replay-diagnosis's tool-grant fix dropped `Write(kavach-data/**)`, diverging from production
- **Severity:** High (at time of finding; now fixed)
- **Category:** Bug
- **Location:** `.claude/skills/live-replay-diagnosis/SKILL.md`, all three worked `--allowedTools`/`--claude-arg` examples
- **Problem:** The original remediation replaced unrestricted `Write(*)/Edit(*)` with `Edit(kavach-data/**)` only. But `.github/workflows/kavach.yml`'s real invocation (line 258) grants **both** `Write(kavach-data/**)` and `Edit(kavach-data/**)` together — the remediation's own claim that the fix "matches production exactly" was not true. This also meant the skill's own documented manual-receipt-writing fallback path ("open one packet, diagnose it, write one receipt") would have been denied under its own worked examples, since writing a brand-new file needs `Write`, not `Edit`.
- **Fix applied:** Added `Write(kavach-data/**)` back alongside `Edit(kavach-data/**)` in all three examples, now token-for-token identical to `kavach.yml`'s real invocation. Also added a note to the file's summary line disclosing that `Bash(*)` remains unrestricted in every example, and that the real enforcement backstop is the CI boundary-check step (not the `Write`/`Edit` scoping itself) — this was flagged as a related Medium finding by the same sub-review and folded into the same edit.
- **Verified:** `grep -n allowedTools` re-run post-fix; all three lines now read `...Read,Write(kavach-data/**),Edit(kavach-data/**),Grep,Glob,...`, byte-identical to the token order in `kavach.yml`.

### RA-002 — kavach-repair's Rules section pointed at the wrong Phase 2 step
- **Severity:** Medium (at time of finding; now fixed)
- **Category:** Inconsistency
- **Location:** `.claude/skills/kavach-repair/SKILL.md`, `## Rules` section
- **Problem:** The infrastructure-inconclusive retry guidance ("confirm the broken pattern is still present in `targetFile`") cited "(Phase 2 step 3)", but that content actually lives in Phase 2 step 4 (step 3 is the earlier patch-derivation step). This is exactly the kind of pointer a candidate needing careful double-apply avoidance would follow.
- **Fix applied:** Corrected to "(Phase 2 step 4)". Also collapsed a cosmetic duplicated `---`/`---` horizontal rule left over from the same remediation edit that removed the static fix-pattern-file list.
- **Verified:** Direct re-read confirms the corrected reference and single horizontal rule.

### RA-003 — failure-triage retained two stale cross-references from its own remediation edit
- **Severity:** Medium (at time of finding; now fixed)
- **Category:** Inconsistency
- **Location:** `.claude/skills/failure-triage/SKILL.md`, Phase 2
- **Problem:** (a) A stale factual claim — "loading all 24 pattern files" — when the real directory now has 12 feature-specific files (post-remediation cleanup); (b) a dangling pointer telling the reader "the full list of available pattern files is in this skill's Reference files section," when that section was deliberately rewritten during remediation to no longer contain any such list.
- **Fix applied:** Removed the specific stale count in favor of non-numeric phrasing that can't silently go stale again; redirected the pointer to "a directory listing of `kavach-data/fix-patterns/`" instead of a section that no longer has the content it's pointing to.
- **Verified:** Direct re-read confirms both corrections; no remaining reference to a specific file count or to the now-removed static list.

No other new Critical, Major/High, or blocking findings were raised by any of the six independent sub-reviews or by the lead reviewer's own direct checks.

## Overall Assessment

### Contracts and Handoffs
Every specific claim in the original remediation was checked against the actual dependency it referenced (a script's real constant, a sibling skill's real section, a workflow's real token string, a shell script's real sequencing) — not accepted on the strength of its own prose. Of eighteen distinct "claimed fix" items checked across the six skills (roughly 3 per skill on average), seventeen were genuine, correct, non-cosmetic fixes on first inspection. The eighteenth (`live-replay-diagnosis`'s tool-grant claim) was directionally correct — the dangerous unrestricted wildcard was gone — but its specific "matches production exactly" claim was false by omission, caught here by a literal token diff, and has now been corrected.

### Tools, Permissions, and Security
Least-privilege intent holds across both agents and all CI invocations after RA-001's fix. The residual, disclosed (not hidden) risk is that `Bash(*)` remains unrestricted in every invocation across this pipeline — this was true before, during, and after both remediation passes, and is compensated for only by the CI-side "Verify repository boundaries" detective control (which itself now correctly gates the downstream PR job, per checkpoint 11/AR-009 from the prior report). This residual risk is now explicitly documented in `live-replay-diagnosis/SKILL.md` rather than left implicit.

### Failure Handling and Operability
The full pytest suite was independently re-run three separate times during this audit (once by the lead reviewer, twice by different sub-agents working from a cold context) and reported an identical `216 passed, 3 subtests passed, 0 failed` each time — a meaningful cross-check that the suite's green state isn't an artifact of stale caching or a flaky run.

### Strengths
- The remediation was substantive, not cosmetic, in the overwhelming majority of cases: e.g. the `testDataOrEnvironmentRuledOut` evidence-gate extension uses a genuinely distinct signal regex per field rather than uniformly relaxing the check; the slug-derivation fix was cross-checked by hand against all 42 real feature files with zero mismatches; the fix-history idempotency guard was verified to still allow a genuinely new entry for a repeat scenario, not just block all repeats.
- Every governance-critical property flagged as already-sound in the original review (allow-list-only intake, non-interactive abort, mandatory 3x Maven, bounded edit scope, credential-handling design) was independently re-confirmed intact and undisturbed by either round of edits.
- The one real gap this second pass found (RA-001) was caught specifically because the audit methodology insisted on a literal diff against the actual CI file rather than trusting a "matches exactly" claim in prose — validating the value of this second, independent pass over simply re-reading the first report.

### Overall Quality Rating
**Very good.** The system's contract-integrity core (schema, validators, evidence gates, least-privilege job-splitting) is sound and independently verified twice now. The gaps found in this second pass were narrow, quickly fixed, and of a kind (a dropped token, an off-by-one step reference, two stale cross-references) that reflects normal remediation-pass residue rather than a deeper structural problem.

## Required Changes Before Approval
None outstanding. All three findings from this re-audit (RA-001, RA-002, RA-003) have been fixed and re-verified in this same pass.

## Optional Improvements
Carried forward from the six sub-reviews, none blocking:
1. Add a trailing/adjacent-acronym worked example to `kavach-knowledge`'s slug table to pre-empt a future ambiguous feature name (Low).
2. Mention the shared `failure_analyzer/` library package in `kavach-diagnose/SKILL.md`'s script index (Low).
3. Add explicit `gh` CLI auth-failure handling in `kavach-repair`'s Phase 5, mirroring its existing `PUSH_REJECTED` pattern (Low).
4. Carry an `isolationMode` flag into the Phase 6 fix-history entry template for per-entry traceability (Low).
5. Make the verdict-report filename collision loop's iteration mechanics explicit rather than only stating the end condition (Low).
6. Point `verdict-reporting`'s fix-pattern bootstrap-structure instruction at `kavach-knowledge`'s Shape section instead of restating the header/line format inline, mirroring the fix already applied to the slug-derivation instruction (Low).
7. Add a one-sentence clarification of what a `PHASE_SCRIPT_FAILED` stop means for the overall kavach-diagnose run, for consistency across sibling skills (Low).
8. Confirm directly in GitHub repository settings (not visible to this code-only review) that branch protection/rulesets prevent the Actions bot identity from merging PRs or bypassing required reviews/status checks — checkpoint 15, a standing manual-verification item.

## Final Verdict
**Approved**

Both Critical findings from the original review are confirmed fixed and independently re-verified (the `triage_workers.py` path bug, by actually running the documented command; the `config.properties` regression, by direct file inspection). All eight Major findings are confirmed fixed. Of the original nine Minor findings, all are confirmed fixed. This second, independent audit — six fresh skill-reviewer passes plus direct lead-reviewer verification of the CI workflow, schema, and test suite — found three additional small issues (RA-001 through RA-003) that the first remediation pass introduced or missed; all three have been fixed and re-verified within this same pass, with the full test suite green (216 passed, 0 failed) and both workflow YAML files parsing correctly throughout. The only remaining item, checkpoint 15 (branch protection actually blocking the Actions bot from merging or bypassing review), is a GitHub repository setting outside this review's visibility and remains a standing manual-verification item rather than a code defect — it does not on its own justify withholding approval of the code and documentation reviewed here.
