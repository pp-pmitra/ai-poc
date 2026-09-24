---
name: kavach-diagnose-scripts
description: >-
  Reference-only index of the Python script bundle under scripts/ that
  the kavach-diagnose agent invokes directly via Bash, and that the
  failure-triage, live-replay-diagnosis, and verdict-reporting skills
  reference by path in their own procedures. Not triggered by name —
  nothing invokes this skill directly; it exists so this directory (a
  sibling of the real, triggerable skills under .claude/skills/) is
  self-describing instead of implying it's a skill with no description.
---

# kavach-diagnose script bundle

This directory holds the deterministic, non-LLM machinery the three real Kavach skills call into — it is a shared toolbox, not a procedure of its own. The owning agent is `.claude/agents/kavach-diagnose.md`; the calling skills are `failure-triage`, `live-replay-diagnosis`, and `verdict-reporting`.

## Scripts, in pipeline order

1. **`list_failures.py`** — mechanical-only extraction of `target/cucumber-reports/cucumber.json` into `kavach-data/history/failures-for-replay.json`, enriched with Playwright trace data. No LLM call. Called by `failure-triage` Phase 1.
2. **`triage_workers.py`** — the cheap, mostly-mechanical triage tier (fix-pattern cache → deterministic classifier → batched text-only LLM fallback). Writes `kavach-data/history/triage-results/<timestamp>/{manifest.json,receipts/*.receipt.json,escalate-groups.json}`. Called by `failure-triage` Phase 2.5, with no path arguments — its own defaults already resolve under the real repo-root `kavach-data/history/` directory.
3. **`replay_workers.py`** — builds per-group live-replay worker packets/prompts for whatever `triage_workers.py` escalated. Called by `live-replay-diagnosis` Phase 3.
4. **`validate_replay_receipts.py`** — validates individual worker receipts (enforcing the `confirmed_product_bug` evidence gate — boolean claims plus real, signal-bearing artifact text, not just field presence) and produces the final `combined-receipts.json`, unioning Phase 2.5's and Phase 3's rows into the one document `validate_kavach_contract.py` and `kavach-repair` both consume. Called at the end of `live-replay-diagnosis`.
5. **`validate_kavach_contract.py`** — the deterministic, dependency-free re-check of `combined-receipts.json`'s full document shape against the contract described by `.claude/contracts/kavach-verdict.schema.json` (kept in lockstep with that schema by `tests/test_kavach_verdict_schema_equivalence.py`). This is the same gate `.github/workflows/kavach.yml` runs independently after the Claude session exits.
6. **`append_fix_history.py`** — the only writer of `kavach-data/history/fix-history.json`: validates each entry against the same machine-format enums as above, appends under an exclusive file lock, and is idempotent against an identical resubmitted batch. Called by `verdict-reporting` Phase 5 and `kavach-repair` Phase 6 (both writers of this shared file route through it — neither hand-writes it directly).
7. **`append_fix_pattern.py`** — the only writer of `kavach-data/fix-patterns/<feature-slug>.md`: appends a block under an exclusive file lock, the same protection `append_fix_history.py` gives the JSON history, for the same dual-writer topology (`verdict-reporting` Phase 5 and `kavach-repair`'s fix-pattern update both append here).
8. **`record_history.py`, `refresh_glossary.py`, `review_verdicts.py`** — supporting/maintenance utilities (history bookkeeping helpers, glossary refresh, human-driven verdict spot-checking via `review_verdicts.py mark`/`report`, itself now lock-protected against racing `append_fix_history.py`) — not part of the per-run critical path above.

`requirements.txt` declares every third-party import the scripts and tests actually use (`anthropic`, `httpx`, `lxml`, `pytest`); `.github/workflows/kavach.yml` installs from it and runs the complete `tests/` suite with `pytest` (not `unittest discover`, since some test files use plain pytest fixtures/classes that discovery silently skips).

## Testing

```bash
python3 -m pytest .claude/skills/kavach-diagnose/scripts/tests/ -v
```
