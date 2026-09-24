---
name: kavach-knowledge
description: >-
  The persistent, cross-run body of knowledge shared by kavach-diagnose
  and kavach-imaintain: known-good fix patterns per feature. Not a
  procedure — nothing invokes this skill directly. Both kavach-diagnose
  (writes) and kavach-imaintain (reads, and also writes when it applies
  a fix) declare it as a preloaded dependency.
allowed-tools: Read Write(.claude/skills/kavach-knowledge/**) Grep Glob
user-invocable: false
---

# Kavach Knowledge

This directory holds the one part of Kavach's output that is never disposable: known-good fixes for specific broken locators/patterns, organized per feature. Everything else either agent produces (verdict reports, receipts, `fix-history.json`) lives under `kavach-diagnose`'s own `history/` working directory — this skill is deliberately narrower, just the pattern library, because it's the one artifact both agents read *and* write, so it can't live inside either one's own folder without creating a cross-agent dependency on internal layout.

## Shape

- `fix-patterns/_default.md` — cross-feature patterns (dismiss/wait hardening, nested-child-span locator guard, navigation utilities, shadow-DOM CSS-over-XPath guidance). Always read, by both agents, for every candidate — supplements but never overrides a feature-specific file.
- `fix-patterns/<feature-slug>.md` — one file per feature that has ever needed a fix (e.g. `Life_PMP.feature` → `life-pmp.md`). Structure: a `# <Feature> fix patterns` header, a `## Run log` line, then accumulated entries under `## Known good fixes` and `## Learned notes`.

## Read/write contract

- **kavach-diagnose** reads `_default.md` in its Phase 2 (always) and the feature-specific file lazily in Phase 3 (only for a group that actually escalates to live replay, once its `featureFile` is known). It writes to `## Known good fixes`/`## Learned notes` in its Phase 5, appending only — never overwrite an existing entry, and bootstrap a new feature file if one doesn't exist yet.
- **kavach-imaintain** reads both `_default.md` and the feature-specific file in its Phase 2, to derive a concrete patch (`targetFile`/`targetLine`/the edit itself) from a receipt's free-text `recommendedAction` — a receipt never carries a structured fix suggestion by design, so a pattern-file match is required before any patch is attempted; without one, the candidate is recorded `needs_investigation` and skipped, never guessed. It also appends a `## Known good fixes` entry itself, in its Phase 6, for every fix it successfully applied and verified.
- Both agents dedupe against existing entries before appending — never write a near-duplicate of a pattern already recorded.

## Why this survives across CI runs

Unlike a typical agent's disposable per-run output, this is the whole point of the fix-pattern cache: a symptom kavach-diagnose or kavach-imaintain has already solved once should be recognized cheaply (no live browser, no LLM call) the next time it appears. `kavach.yml`'s "Open PR for kavach's learned fix patterns" step commits any changes under this directory to a reviewable branch after each CI run specifically so this knowledge accumulates instead of being discarded when the runner is destroyed.
