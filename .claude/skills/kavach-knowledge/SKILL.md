---
name: kavach-knowledge
description: >-
  The persistent, cross-run body of knowledge shared by kavach-diagnose
  and kavach-repair: known-good fix patterns per feature. Not a
  procedure — nothing invokes this skill directly. Both kavach-diagnose
  (writes) and kavach-repair (reads, and also writes when it applies
  a fix) declare it as a preloaded dependency.
---

# Kavach Knowledge

This skill describes the one part of Kavach's output that is never disposable: known-good fixes for specific broken locators/patterns, organized per feature. The actual data lives at repo-root `kavach-data/fix-patterns/` — deliberately outside `.claude/`, since both agents' sandboxed Bash/Write calls can only write outside that directory (`.claude/` is bind-mounted read-only inside the `--agent` sandbox). Everything else either agent produces (verdict reports, receipts, `fix-history.json`) lives under `kavach-data/history/` instead — this skill is deliberately narrower, just the pattern library, because it's the one artifact both agents read *and* write, so it can't live inside either one's own folder without creating a cross-agent dependency on internal layout.

## Shape

- `kavach-data/fix-patterns/_default.md` — cross-feature patterns (dismiss/wait hardening, nested-child-span locator guard, navigation utilities, shadow-DOM CSS-over-XPath guidance). Always read, by both agents, for every candidate — supplements but never overrides a feature-specific file.
- `kavach-data/fix-patterns/<feature-slug>.md` — one file per feature that has ever needed a fix. Structure: a `# <Feature> fix patterns` header, a `## Run log` line, then dated `### YYYY-MM-DD` entries as they accumulate (this is what real files actually look like today — `## Known good fixes`/`## Learned notes` subheadings are optional refinements *within* a dated entry once a pattern proves reusable across multiple runs, not a separate top-level structure every file must have from day one).

### `<feature-slug>` derivation — the one algorithm every skill in this pipeline must use

This is the single point of definition; `kavach-repair` and `verdict-reporting` reference this section rather than restating their own copy.

1. Take the feature file's stem (drop the `.feature` extension), e.g. `Life_CampaignDashboard`.
2. Split on `_`. The first segment is the app prefix (`Life`, `Studio`, …) — lowercase it as-is (`life`, `studio`).
3. Join the remaining segments back together with no separator, then split *that* string into words at case boundaries: an uppercase run immediately followed by a capitalized word is its own word (so an acronym like `NPI` in `NPILists` splits as `NPI` + `Lists`, not `N`+`P`+`I`+`Lists`); otherwise, each uppercase letter starts a new word.
4. Lowercase every word and hyphen-join the prefix with all the words, then append `.md`.

Worked examples (verified against every feature file actually in this repo, and against the current, now-consistent `kavach-data/fix-patterns/` directory):

| Feature file | `<feature-slug>.md` |
|---|---|
| `Life_CampaignDashboard.feature` | `life-campaign-dashboard.md` |
| `Life_CuratedMarket.feature` | `life-curated-market.md` |
| `Life_NPILists.feature` | `life-npi-lists.md` |
| `Life_ReportTemplates.feature` | `life-report-templates.md` |
| `Life_LineItem.feature` | `life-line-item.md` |
| `Life_RunReport.feature` | `life-run-report.md` |
| `Life_PMP.feature` | `life-pmp.md` (a bare acronym stays one word — nothing to split) |
| `Studio_ExplorerWorkspace.feature` | `studio-explorer-workspace.md` |

A prior version of this pipeline used an under-specified rule that different skills implemented differently, producing files like `life-curatedmarket.md` (no hyphen) and several fully-orphaned files (`life-create-campaign.md`, `life-tactic-creation.md`, etc. — apparently scenario-level, not feature-level, slugs from an even earlier convention) that matched no real feature file at all. Those have been renamed/removed to match the algorithm above (all were empty placeholders except `studio-explorerworkspace.md` → `studio-explorer-workspace.md`, whose real content was preserved). If you ever find a fix-pattern filename that doesn't match a real feature file under this algorithm, that's a bug — either rename it to the correct slug (merging content if the correct-slug file already has some) or delete it if it's an empty leftover, never leave it in place as an orphan.

## Read/write contract

- **kavach-diagnose** reads `_default.md` in its `failure-triage` skill (always) and the feature-specific file lazily in its `live-replay-diagnosis` skill (only for a group that actually escalates to live replay, once its `featureFile` is known). Its `verdict-reporting` skill writes to the feature file, appending only — never overwrite an existing entry, and bootstrap a new feature file if one doesn't exist yet (using the slug algorithm above).
- **kavach-repair** reads both `_default.md` and the feature-specific file in its Phase 2, to derive a concrete patch (`targetFile`/`targetLine`/the edit itself) from a receipt's free-text `recommendedAction` — a receipt never carries a structured fix suggestion by design, so a pattern-file match is required before any patch is attempted; without one, the candidate is recorded `needs_investigation` and skipped, never guessed. It also appends an entry itself, in its Phase 6, for every fix it successfully applied and verified.
- **Dedup criteria (worked example):** before appending, check whether an existing entry already describes the same broken locator/pattern *with the same fix*. Same broken locator + same fix expression = duplicate, skip appending (a re-run confirming a known pattern still holds is not new information). Same broken locator + a *different* fix expression = not a duplicate — append the new entry, and note in it that an earlier entry for the same locator may now be stale (see supersession below), rather than silently dropping either one.
- **Correcting a wrong entry:** this file is append-only — never delete or rewrite a prior entry, even one that later turns out to be wrong (e.g. it worked once but the page changed again). Mark it instead: prefix the now-wrong entry's line with `**[superseded YYYY-MM-DD — see entry below]**` and append a new, correct entry. This keeps the append-only guarantee while still letting a future reader (human or agent) know not to trust the superseded one.
- **Growth:** `_default.md` is loaded on every single run by both agents regardless of feature, so unlike a feature-specific file it isn't meant to grow without bound — if it starts exceeding a few hundred lines, that's a signal for a human to do a periodic pruning/consolidation pass (merge near-duplicate cross-feature patterns, retire ones superseded by a more general one), not something either agent does automatically.

## Why this survives across CI runs

Unlike a typical agent's disposable per-run output, this directory is meant to accumulate permanently rather than be discarded when the runner is destroyed. `kavach.yml`'s "Open PR for kavach's learned fix patterns" step commits any changes here to a reviewable branch after each CI run for that reason.

**Note — this is not the fix-pattern *cache* mechanism.** The actual cost-saving fast path that skips re-diagnosing an already-solved symptom (no live browser, no LLM call) reads `kavach-data/history/fix-history.json` directly (`fix_pattern_cache.py`'s `find_cached_fix`), not the `.md` files described here. This directory's `.md` files serve a different, related role: always-read reference material both agents load as LLM prompt context (Phase 2/live-replay-diagnosis) when deriving a concrete patch from a matched pattern — a human-curated library, not the cache-hit lookup itself. Look at `fix-history.json` when debugging a cache miss, not here.
