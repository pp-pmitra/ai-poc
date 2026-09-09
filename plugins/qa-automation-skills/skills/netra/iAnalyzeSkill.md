# JIRA Requirement Analysis — Workflow

A single, end-to-end QA workflow. One input (Fix Version, ticket key, comma-separated list, or Jira URL) runs the full pipeline: fetch work items, expand linked/parent context, triage, optionally gather Confluence docs, pull production issues, then **process tickets in batches of 3–5 — each batch emits validated, per-ticket structured JSON into the cache, and reusable formatter scripts turn that JSON into the deliverables**. The run produces **three deliverables** — a `.docx` **Deep Analysis** (one section per ticket, plus a front Release Summary), an `.xlsx` **Test Design Document** (one tab per ticket, plus a `[Summary]` tab), and a standalone `.html` **Release Readiness Dashboard** — plus the working **cache** (`.json`) shipped alongside for reference. All are built locally via bash and presented together at the end.

**Chat shows only:** progress lines, the visual widgets, the pre-flight summary, and the final dashboard. Full Deep Analysis text and test-case tables never appear in chat — they go into the files.

---

## 0. Quick map

| Step | What it does | Live call | Chat output |
|------|--------------|-----------|-------------|
| 1 | Build JQL | — | — |
| 2 | Fetch work items | ✅ #1 | Ticket-scan widget |
| 3 | Handle paging | (part of #1) | — |
| 4 | Expand linked + parent/epic | ✅ #2 | Context-map widget |
| 5 | Test-readiness triage | — | — |
| 6 | Confluence docs | ✅ conditional | — |
| 7 | Production issue fetch | ✅ #3 | — |
| 8 | Cache | — | — |
| 9 | Pre-flight summary (sanity gate) | — | Brief text block |
| 10 | Setup: write formatter scripts + init cache | — | Batch-plan widget |
| 11 | Batch Deep Analysis → per-ticket JSON → `.docx` | — | Progress line + widget/batch |
| 12 | Batch Test Cases → per-ticket JSON → `.xlsx` | — | Progress line + widget/batch |
| 13 | **Release Summary → front of .docx + dashboard widget + Dashboard `.html`** | — | Dashboard widget |
| 14 | Verify all artifacts, present downloads (3 files + cache) | — | Handoff text |

**Before the first live call:** Atlassian Rovo is a third-party connector — surface it for user approval before calling it.

**Live Jira calls: three, always** (STEP 2, 4, 7), plus STEP 6 only when its gate passes. STEP 11–13 add no Jira calls — they read the cache.

---

## Chat visuals (shared convention)

All widgets are HTML `show_widget` blocks sharing one palette — header `#1F3864`, accent `#2E75B6`/`#4FC3F7`, status **green `#43A047` / amber `#FFA726` / red `#E53935`**, white cards with subtle shadow. Keep each widget compact; no full-screen output.

| Widget | Renders at | Shows |
|--------|-----------|-------|
| Ticket-scan | end of STEP 2 | Each fetched ticket as a card (key · truncated summary · status badge); footer "N tickets fetched" |
| Context-map | end of STEP 4 | Primary tickets → linked/parent tickets with relationship labels; footer "N additional loaded" |
| Batch-plan | end of STEP 10 | Kanban board, one column per batch, tickets as pending cards |
| Batch-progress | per batch (STEP 11–12) | Two progress bars — Deep Analysis and Test Cases — with per-ticket ✓/⟳/pending and TC counts once STEP 12 starts |
| Dashboard | STEP 13 | Final release view (see STEP 13); the same view is also written to the standalone `.html` deliverable |

---

## Output aesthetics & readability (shared convention)

The three represented documents are read by reviewers and PMs, not just parsed — treat their visual quality as part of correctness. Every generated `.docx`, `.xlsx`, and `.html` must be **clean, consistent, and easy to scan**, with the same palette as the chat widgets (header `#1F3864`, accent `#2E75B6`/`#4FC3F7`, status green `#43A047` / amber `#FFA726` / red `#E53935`). Attend to detail in every step: no truncated content, no orphaned headings, no raw placeholders left in a finished file, no ragged tables.

- **`.docx` (Deep Analysis).** Styled title page (centered title, generated date, muted subtitle). A **real generated Table of Contents** (Word TOC field), not a hand-typed list. Each ticket starts on a **new page**; `level 1`/`level 2` headings only (they drive the nav pane). Readable body (11 pt, ~1.15 line spacing, space-after on paragraphs), short paragraphs over walls of text, consistent bullet style. Callout notes use their shaded fills (`#FFF2CC` DPD, `#EBF3FB` context) with padding, never bare text. Any inline tables get header shading, thin borders, and no cell overflow. Page numbers in the footer.
- **`.xlsx` (Test Design Document).** Frozen, bold, shaded header row (`#D9E1F2`) with **autofilter**; the column widths already specified; all cells wrap + top-aligned; generous row height. **Status-colored fills** applied consistently (PASS `#E2EFDA`, BLOCKED `#FCE4D6`) with a small **status legend** on the `[Summary]` tab. Subtle zebra banding for long tabs. Tab names ≤ 31 chars, meaningful, optionally color-coded by triage verdict. `[Summary]` tab reads as a clean scoreboard with a totals row.
- **`.html` (Release Readiness Dashboard).** Self-contained, responsive card grid with clear hierarchy, generous whitespace/padding, legible system font stack, subtle card shadows, accessible contrast, a generated-on timestamp, and section headings. No clutter, no external assets.

All of this styling lives in the **STEP 10 formatter scripts**, written once and applied identically to every ticket — so consistency is structural, not re-improvised per batch.

Detail bar (programmatic, not visual): the agent cannot "see" a `.docx`/`.xlsx`, so verification is by inspection, not eyeballing. Before presenting, assert in code: no placeholder tokens remain (unzip the OOXML and grep for `Release Summary placeholder`, `<...>`, `TODO`); every ticket tab/section exists; row counts per tab match the JSON; each `Test Description` and `Expected Result` string length is within a sane bound for its column width (flag likely overflow); status fills and heading styles are present. Optionally render the first page/tab to PNG and `view` it for a genuine visual spot-check.

---

## Generation architecture (shared convention)

**Decouple content generation from file formatting.** The model's job in STEP 11–12 is to produce *content as data*, never to hand-write file-formatting code inline.

- **Generate → validate → cache, per ticket.** For each ticket the model emits one structured JSON object (analysis in STEP 11, test rows in STEP 12), `json.loads`-validates it immediately, and merges it into `Cache_<identifier>_<date>.json` before moving on. A malformed object is regenerated for that ticket alone — never a whole batch. Content lives on disk the moment it's produced, so nothing generated is ever lost.
- **Format with static, reusable scripts.** Three formatters — `build_docx.py`, `build_xlsx.py`, `build_dashboard.py` — are written **once** in STEP 10 and read the cache to (re)build the deliverables. They are deterministic and idempotent: rebuilding the whole file from the cache any number of times yields the same output, so there is no partial-append corruption. All formatting/styling from the aesthetics convention lives here, not in per-batch code.
- **Recovery = re-run a formatter, never re-generate.** If a file is corrupt or a formatter has a bug, fix the script and re-run it over the existing cached JSON. The expensive generation step is not repeated; only the cheap, deterministic formatting is.
- **Why:** embedding ~150 detailed strings plus `openpyxl`/`python-docx` calls into a generated per-batch script is the workflow's most fragile point — string-escaping breaks syntax, and a formatting bug would otherwise force regenerating content. Data-first generation removes both failure modes.

---

## INPUT

Provide **exactly one**:

- **`fixVersion`** — e.g. `"July-2026-portal"`. All Stories in that release.
- **`ticketKey`** — e.g. `"ABC-1234"`. Single issue, any type.
- **`ticketList`** — e.g. `"ABC-1234, ABC-1235"`. Comma-separated.
- **`jiraUrl`** — board/filter/backlog/release URL. Extract all visible ticket keys. If the page is behind auth, extract the fix-version or filter ID from the URL and build the equivalent JQL via the Rovo connector.

Rules: none given → ask first. Multiple given → prefer `ticketList`/`ticketKey` over `fixVersion`/`jiraUrl`, or ask.

| Option | Values | Default | Meaning |
|--------|--------|---------|---------|
| `detailMode` | `full` / `fast` | `full` | `fast` skips STEP 4 |
| `productionProjectKey` | project key | `HT` | Project holding production bugs (STEP 7) — referenced, never hardcoded |

---

## STEP 1 — Build the JQL

- **`fixVersion`:** `issuetype = Story AND fixVersion = "<fixVersion>"` — always double-quote (it contains hyphens).
- **`ticketKey`:** `key = <ticketKey>`
- **`ticketList` / `jiraUrl` keys:** `key in (<KEY-1>, <KEY-2>, ...)`

> Keep placeholders in code formatting. Bare `<...>` angle brackets get stripped as HTML and produce malformed JQL.

---

## STEP 2 — Fetch work items *(live call #1)*

Call `searchJiraIssuesUsingJql`:
```
fields: ["summary","description","status","issuetype","priority","assignee",
         "labels","components","resolution","issuelinks","comment","parent","subtasks"]
responseContentFormat: "markdown"
maxResults: 100
```
Capture per issue: summary, key details, description, comments, links, parent/epic, sub-tasks. → **Ticket-scan widget.**

---

## STEP 3 — Handle paging

If a `nextPageToken` is returned, repeat STEP 2 with it and append until none returns.

---

## STEP 4 — Expand linked + parent/epic *(live call #2)*

Collect unique related keys from `fields.issuelinks[]` and `fields.parent`. Deduplicate against already-fetched keys, then batch-fetch the remaining keys with `key in (...)`. **Chunk the `IN (...)` clause into ≤ 50 keys per call** and loop — a single clause with hundreds of keys can exceed Jira's JQL length/clause limits and fail. In practice the related set is usually small (a story's `parent` is one epic; links are a handful), so this is a guard, not the common path. Capture the same fields; record `referencedBy`. Skipped in `fast` mode. → **Context-map widget.**

---

## STEP 5 — Test-Readiness Triage

Compute flags for every item and store in cache. **Triage is informational — it never gates test-case generation.** Test cases are always synthesised from the full information set (ticket + parent/epic + linked issues + comments + Figma). Absence of formal acceptance criteria is normal, not a blocker.

| Code | Severity | Fires when |
|------|----------|------------|
| `MISSING_COMPONENTS` | 🔴 BLOCKER | No `components` — area under test unidentified |
| `MISSING_ASSIGNEE` | 🟠 WARNING | No `assignee` |
| `UNEXPECTED_STATE` | 🟠 WARNING | Status/resolution inconsistent |
| `UNREADY_DPD_DEPENDENCY` | 🟠 WARNING | A linked `DPD-` ticket is Open / In Progress / On Hold / Code Review |
| `NO_LINKED_CONTEXT` | 🟠 WARNING | No links, no informative parent/epic, and no description substance |

---

## STEP 6 — Confluence docs *(conditional)*

**Gate:** runs **only if `Confluence.md` is uploaded in the Files section** of the request. That upload is the sole trigger — no input flag turns it on. If absent, skip entirely (no search calls, no `confluenceDocs` cache section). When present, run the `Confluence.md` contextual-search flow and fold `confluenceDocs` into the cache.

---

## STEP 7 — Production issue fetch *(live call #3 — mandatory)*

Runs every time; no gate. Query the project named by **`productionProjectKey`** (default `HT`) — reference the variable so an override takes effect. **Bound the query** so the context window isn't flooded with years of noise: prefer structured clauses over free-text, add a recency window, and cap results.
```
project = <productionProjectKey>
  AND (component in (<derived-components>) OR labels in (<derived-labels>))
  AND created >= -365d
ORDER BY created DESC
maxResults: 50
```
Free-text `text ~ "theme"` is noisy and weakly indexed — use it only as a fallback when no component/label maps to the analyzed items, and keep the `created >= -365d` window and `maxResults` cap regardless. Derive components/labels/feature keywords from the analyzed items (components, parent/epic context). Score the returned bugs for relevance, cache the top results for STEP 11's Historical Analysis. If nothing matches, record an empty result — never fabricate history.

---

## STEP 8 — Cache

Store fetched items, expanded context, production bugs, Confluence docs (if run), and triage results. This is the **single source of truth** for STEP 9–14. No re-fetching after this point.

Persist the cache to `/home/claude/Cache_<identifier>_<date>.json` and keep it current as STEP 11–13 add content. It holds the fetched data plus the generated content the formatters read: `items` / `context` / `prodBugs` / `triage` (from here), and `analysis{}` (STEP 11, keyed by ticket), `testcases{}` (STEP 12, keyed by ticket), and `releaseSummary{}` (STEP 13) — the latter three initialized empty in STEP 10 and filled per ticket. It ships as a reference artifact in STEP 14 — never a primary deliverable, but included so the run is auditable and re-runnable from cache alone.

---

## STEP 9 — Pre-flight summary *(sanity gate)*

Print a compact text block so the run can be sanity-checked before the expensive batch work:
```
Input:          <identifier>
Tickets:        N  |  Warnings: Y  |  Blockers: Z
Live DPD:       <key — status — assignee> or None
Top prod risk:  <theme — relevant bug keys>
```

---

## STEP 10 — Setup: formatters + cache init

Build the reusable toolchain **once**, before any batch work — no per-batch formatting code exists after this step.

```bash
pip install openpyxl python-docx --break-system-packages -q
```

**Write three static, deterministic formatter scripts** (each reads `Cache_<identifier>_<date>.json` and rebuilds its whole deliverable from scratch — idempotent, safe to re-run any number of times):

- **`build_xlsx.py`** → Test Design Document. One worksheet per ticket key in fetch order (tab name ≤ 31 chars), plus a final `[Summary]` tab. Owns all `.xlsx` styling from the aesthetics convention:
  - Header row (bold, fill `#D9E1F2`, freeze at A2, **autofilter on**): `Test ID | Requirement ID | Test Description | Test Data | Expected Result | Actual Result | Test Status | Owner | Comments`
  - Column widths: 18 / 16 / 55 / 40 / 50 / 20 / 14 / 18 / 55; all cells wrap + top-aligned; min row height 35; zebra banding on long tabs.
  - Status fills: PASS `#E2EFDA`, BLOCKED `#FCE4D6`. `[Summary]` header: `Ticket Key | Summary | Triage Verdict | Total TCs | R (context items) | Blockers | Warnings`, with a status legend and a totals row.
  - Save to `/home/claude/Test_Design_<identifier>_<date>.xlsx`
- **`build_docx.py`** → Deep Analysis. Owns all `.docx` styling: styled title page (centered `Deep Analysis — <input>`, generated date, muted subtitle); the **Release Summary** section right after the title page (from `cache.releaseSummary`, empty until STEP 13); a **real generated Table of Contents** (Word TOC field over the `level 1`/`level 2` headings); then one section per ticket from `cache.analysis`, each starting on a **new page**, `level 1` heading for the ticket and `level 2` per section, callout fills (`#FFF2CC` DPD, `#EBF3FB` context), footer page numbers. Save to `/home/claude/Deep_Analysis_<identifier>_<date>.docx`
- **`build_dashboard.py`** → Release Readiness Dashboard. Holds the HTML template as an embedded string with `{{placeholders}}`; reads `dashboard_data.json` (written in STEP 13) and injects the metrics. Self-contained, shared palette, responsive. Save to `/home/claude/Release_Readiness_Dashboard_<identifier>_<date>.html`

**Initialize the cache** content sections: add empty `analysis{}`, `testcases{}`, `releaseSummary{}` keyed slots to `Cache_<identifier>_<date>.json`.

**Smoke-test:** run `build_xlsx.py` and `build_docx.py` once now against the empty content sections to confirm they produce a valid skeleton file (title page, TOC, one empty tab per ticket). A failure here is cheap to fix; a failure after generation is not.

Print: `✓ Setup complete — formatters written, <N> tabs scaffolded, cache initialized.` → **Batch-plan widget.**

---

## STEP 11 — Batch Deep Analysis → per-ticket JSON → `.docx`

Process tickets in batches of 3–5 (see Batch rules). **The model generates content, not formatting code.** Per ticket: emit one analysis JSON object with the eight sections as fields, `json.loads`-validate it, merge it into `cache.analysis[<key>]`. After each batch, run `build_docx.py` to rebuild the `.docx` from the cache, print one line, render the widget. **No analysis text in chat.**

Each analysis object carries these eight sections (the formatter renders them as `level 2` headings under the ticket's `level 1` heading — the model never writes heading code):

1. **Background** — state → change → behavior.
2. **Intent** — business value / problem solved.
3. **Cross-Functional Impact** — subsystems, apps, APIs affected.
4. **Requirement Gap** — `GAP-1, GAP-2 …`; must reason, not just say "none". Scope to **functional/logical gaps**: unspecified business/calculation rules, undefined error or edge behaviour, missing validation limits or thresholds, unhandled states or transitions, absent integration/API contracts, and open permission conditions. Cosmetic/visual omissions (styling, layout, spacing, copy wording) are **not** gaps. **Missing AC is not itself a gap** (STEP 5: absence of formal AC is normal) — first infer the baseline functional rules from the epic/parent/linked context, then log a `GAP` only when a *critical* behaviour (a negative state, a data/format limit, a named error path, a permission boundary) genuinely cannot be inferred from that surrounding context. Vague-summary tickets get inferred coverage in STEP 12, not a blanket "entire ticket is a gap".
5. **Ambiguity** — `AMB-1, AMB-2 …`; flag **functional/logical ambiguity**: contradictory or conflicting rules, undefined terms that change behaviour, unclear state transitions, scope conflicts, unquantified limits/thresholds, **or a Title/Summary that mismatches the Description / AC**. Ignore purely visual/UX wording ambiguity that has no behavioural consequence.
6. **Dependencies** — hard/soft blockers, open sub-tasks, unready `DPD-` tickets. A live DPD sets a flag the formatter renders as a `#FFF2CC` note.
7. **Historical Analysis** — cite actual `<productionProjectKey>` bugs from STEP 7 with a risk rating (LOW / MEDIUM / ELEVATED / HIGH).
8. **References** — Jira / Confluence / production sources.

If a ticket's analysis is drawn mainly from parent/epic/linked context, set a `contextNote` field (source attribution) — the formatter renders it as a `#EBF3FB` paragraph.

Per batch: `✓ Deep Analysis batch N/M — ET-XXXXX, ET-YYYYY, ET-ZZZZZ` + **Batch-progress widget** (Panel A advancing). After all batches: `✓ All Deep Analysis sections written (<N>).`

---

## STEP 12 — Batch Test Cases → per-ticket JSON → `.xlsx`

Same batches and order. **The model generates test rows as data, not formatting code.** Per ticket: build the coverage decomposition (12a), emit a JSON object `{ inventory, dimensions, rows[] }`, `json.loads`-validate it, merge into `cache.testcases[<key>]`, and update that ticket's `[Summary]` row data. After each batch, run `build_xlsx.py` to rebuild the `.xlsx` from the cache, print one line, update the widget. **No test-case tables in chat.**

### Focus — functional and logical, not UX

The exercise targets **functional and logical correctness**: what the system computes, stores, validates, transforms, permits, rejects, and returns, and how it moves between states. That is the primary surface under test.

Pure UX/visual concerns — layout, spacing, alignment, color, typography, copy wording, hover/animation styling, responsiveness — are **out of scope** and do not become requirements or test cases. A UI or Figma detail is only in scope when it **encodes a functional rule** (e.g. Save disabled until required fields validate; a field that must reflect a persisted value; a control that appears only for an authorized role). In those cases the UI element is the *observable signal* of a functional outcome — test the outcome, cite the surface as the check.

### 12a — Context-first decomposition *(the core rule; produces the JSON)*

Before emitting any row for a ticket, build an internal coverage checklist from **all** available information (ticket description + any AC, comments, parent/epic, linked issues, sub-tasks, Figma references, implementation notes, and STEP 11 GAP/AMB items). Formal AC is one input, not a prerequisite.

**Part A — Requirement inventory.** List every discrete testable behaviour; count them as `R`; attribute each to its source. A behaviour counts if it is an AC item, a behaviour in a parent/linked ticket, a named error state, a field/format/limit rule, a business or calculation rule, a data-persistence or transformation requirement, a permission condition, a state transition, an integration/API contract, or a negative/excluded behaviour.

Keep the inventory **functional and logical**. A UI or Figma-sourced item counts only when it carries a functional rule (per the Focus note above) — count the rule it enforces, not the visual itself. Cosmetic/layout/styling details never enter the inventory and never contribute to `R`.

**Part B — Coverage dimensions.** For each, add ≥1 test case if it applies: happy path (always) · secondary happy paths · field/input validation · business-rule & calculation correctness · data integrity/persistence · error states · permission/role · state transitions · boundary/limit · multi-value/batch · integration/cross-system · regression (per relevant production bug) · out-of-scope guard.

**Minimum count (floor, never ceiling):**
```
minimum_TCs = R × 2                                    # one positive + one negative per requirement
            + applicable dimensions not already covered
            + regression anchors (one per relevant production bug)
```

**Each ticket's JSON carries the inventory + dimensions as auditable metadata** (not Python comments), e.g.:
```json
{
  "ticket": "ET-25052",
  "inventory": { "R": 14, "items": ["R01 [ticket] …", "R05 [parent epic ET-25000] …", "R11 [comment] …"] },
  "dimensions": ["happy path", "field validation", "error states(6)", "permission(PG)",
                 "state transition(upload→preview→save)", "boundary(Deal ID>20)", "batch", "integration"],
  "regression": ["HT-5039", "HT-5142", "HT-4357"],
  "minimumTCs": 31,
  "rows": [ /* one object per test case — see 12b */ ]
}
```
Calibration examples: 3 AC + 2 errors + 1 permission + 1 transition + 1 bug → (7)×2 + regression = **14+**. · 9 AC + 6 errors → **30+**. · simple rename → 1×2 + 1 boundary = **3**. · no AC but a parent epic describing 5 behaviours + 2 errors → **14** (AC absence never lowers the count).

### 12b — Row rules

- **Test ID:** `TC_<TICKET-KEY>_NN` (zero-padded).
- **Test Description:** opens with `[POSITIVE]` / `[NEGATIVE]` / `[EDGE]` / `[REGRESSION]` / `[BLOCKED]` and names the requirement it covers, e.g. `[POSITIVE — R03] File state shows "Uploaded" after valid template upload`.
- **Test Data:** concrete — actual account type, file format, field values, permission config, or system state. Write the real example value when it matters (e.g. a 21-char Deal ID).
- **Expected Result:** independently verifiable, stated as a **functional outcome** — the persisted value or resulting system state, the API field + value returned, the computed/derived result, the record created/updated/rejected, or the exact error/validation message that enforces a rule. A tester who never read the ticket must be able to judge pass/fail from this alone. Reference a UI surface (a toast, a disabled button, a highlighted field) only as the *observable signal* of that outcome — never as the thing under test.
- **Comments:** requirement source + any regression/DPD anchor (e.g. `Src: parent ET-25000 · Reg: HT-5039`). This is where source attribution lives in the sheet.
- **Owner / Actual Result:** leave blank.
- **BLOCKED** applies only when *zero* testable behaviour can be derived from the ticket, parent, epic, links, or comments (rare): one row, `Test Status = BLOCKED`, Comments state what's missing.

Each row is a JSON object with keys matching the columns (`testId`, `requirementId`, `description`, `testData`, `expected`, `status`, `comments`; `owner`/`actual` empty). `build_xlsx.py` owns the fills (BLOCKED `#FCE4D6` · PASS `#E2EFDA`), wrapping, alignment, and row height — the model sets `status`, not colors.

Per batch: `✓ Test case batch N/M — ET-XXXXX (X TCs), ET-YYYYY (Y TCs)` + widget (Panel B advancing, counts filled). Populate each ticket's `[Summary]` row data in the cache as you go. After all batches: `✓ All test cases written — <total> across <N> tabs (avg <X>/ticket).`

---

## STEP 13 — Release Summary + Release Readiness Dashboard *(post-analysis)*

Now that `cache.analysis` and `cache.testcases` are complete, compute release metrics **once**, write them to `cache.releaseSummary` and `dashboard_data.json`, then emit the single summary in three co-located surfaces (all from the same numbers — never recomputed differently):

1. **In the `.docx`:** with `cache.releaseSummary` populated, re-run `build_docx.py` — it renders the Release Summary section in place (after the title page, before the TOC). Contents: totals (tickets, total TCs, avg TCs/ready ticket, highest-coverage ticket), triage breakdown (ready / warning / blocked), live DPD dependencies, top production-risk themes with bug keys, and open AMBs to resolve before testing.
2. **In chat:** render the **Dashboard widget** — metric tiles (Total Tickets · Total Test Cases · Avg TCs/Ticket), a ready/warning/blocked bar, top production-risk themes, and the highest-coverage ticket called out. This is the live preview during the run — no separate handoff widget.
3. **As a file — the Release Readiness Dashboard deliverable:** run `build_dashboard.py`, which injects `dashboard_data.json` into its embedded HTML template and writes `/home/claude/Release_Readiness_Dashboard_<identifier>_<date>.html`. Self-contained (inline CSS, no CDN), shared palette, same metrics and layout as the widget but full-page. `.html`, never `.docx` (Word is the wrong surface for a visual dashboard). Because the template is fixed, the dashboard looks identical across runs — only the injected numbers change.

---

## STEP 14 — Verify and present

**Verify (programmatically — the agent inspects, it doesn't eyeball):**
```bash
ls -lh /mnt/user-data/outputs/
```
- All four artifacts exist: `.docx`, `.xlsx`, `.html` dashboard, `.json` cache. `.xlsx` > 20 KB (single) / > 100 KB (10+ tickets); `.docx` > 10 KB / > 80 KB; `.html` > 3 KB; cache `.json` parses.
- **Content matches cache:** open the workbook and assert each ticket tab's row count equals `len(cache.testcases[key].rows)`; assert the `.docx` has one section per `cache.analysis` key. If a workbook average is below 5 TCs per ready ticket, generation under-produced — regenerate the affected *tickets'* JSON (not the formatting) and re-run the formatter.
- **No leftovers / no overflow:** unzip the OOXML and grep for placeholder tokens (`Release Summary placeholder`, `<...>`, `TODO`); confirm none remain. Flag any `Test Description`/`Expected Result` whose length is implausible for its column width.
- **One computation, three surfaces:** the dashboard `.html`, the `.docx` Release Summary, and the chat widget carry identical numbers (all from `cache.releaseSummary`).
- Optional visual spot-check: render page 1 of the `.docx` and the first `.xlsx` tab to PNG and `view` them.
- **Recovery:** any file-level failure → fix the formatter script and re-run it over the cached JSON. Never re-generate content that is already in the cache.

**Present:** move all four artifacts to `/mnt/user-data/outputs/`, then call `present_files` in deliverable order — **Deep Analysis (`.docx`) · Test Design Document (`.xlsx`) · Release Readiness Dashboard (`.html`) · Cache (`.json`, reference)** — so the three primary files lead and the cache trails. Then print:
```
── HANDOFF ────────────────────────────────────────────────
Input:        <identifier>
Tickets:      N — X ready, Y warnings, Z blocked
Deep Analysis: <N> sections (+ Release Summary) in .docx
Test Design:   <N> tabs + [Summary] in .xlsx — <total> TCs, avg <X>/ticket
Dashboard:     Release Readiness Dashboard (.html)
Cache:         Cache_<identifier>_<date>.json (reference)
Highest cov:   <key> (<N> TCs, <R> context items)
Live DPDs:     <key — status — contact> / None
Prod risk:     <theme — bug keys>
Open AMBs:     <AMB-x (key): one-line>
───────────────────────────────────────────────────────────
```

---

## Batch rules

| Ticket count | Batch size | Note |
|---|---|---|
| 1–5 | all at once | no announcement |
| 6–15 | 3–5 | announce plan first |
| 16–30 | 4–5 | progress line per batch |
| 31–50 | 5 | warn upfront: long runtime |
| 51+ | stop & ask | recommend the Research feature |

Complete every ticket fully within a batch before the next; never split a ticket across batches. If a ticket's JSON fails to validate: regenerate that one ticket's JSON — never the batch. If a formatter or file fails: fix the script and re-run it over the cached JSON — never re-generate content already in the cache. Never call `present_files` until all artifacts pass STEP 14.

---

## Non-negotiables

- **Cache-only content.** Every ticket, status, link, bug, and AC comes from the STEP 8 cache. Missing field → write "Not specified". Never invent tickets, URLs, statuses, or history.
- **Functional/logical focus — analysis *and* test cases.** Test cases, Requirement Gaps (Section 4), and Ambiguities (Section 5) all target what the system computes, stores, validates, transforms, permits, and returns — not visual/cosmetic behaviour. Gaps and ambiguities are functional/logical (rules, states, limits, integrations, permissions), never styling/layout/copy. UI and Figma sources are read for the **functional** rules they specify; they do not generate layout/styling/copy checks or cosmetic gap/ambiguity items.
- **Count is derived, not estimated.** The STEP 12a inventory + dimensions are mandatory per ticket. `R × 2` is the floor; complex tickets reaching 20–40+ TCs is correct.
- **Every requirement maps to ≥1 test case** — R items ⇒ ≥ R positive and ≥ R negative cases before edges/regressions.
- **Error states, permissions, business rules, and regression anchors are first-class** — each named error state, each permission condition (tested with and without), each calculation/validation rule, and each relevant production bug generates its own case.
- **Deep Analysis is substantive** — Sections 4 and 5 reason about **functional/logical** gaps/ambiguities rather than declaring "none" or raising cosmetic ones; Section 7 cites real bugs with a justified rating.
- **Generate content as data, format with scripts.** In STEP 11–12 the model emits validated per-ticket JSON into the cache; the STEP 10 formatters (`build_docx.py`/`build_xlsx.py`/`build_dashboard.py`) turn cache → files. Never embed test-case strings or `openpyxl`/`python-docx` formatting in generated per-batch code. Recovery re-runs a formatter over cached JSON; it never re-generates content.
- **Represented documents are finished, not rough.** Every `.docx`, `.xlsx`, and `.html` meets the Output aesthetics & readability bar — consistent palette, real TOC, page breaks, status legends, autofilter, no overflow, no leftover placeholders. Verify this **programmatically** (row/section counts vs cache, placeholder grep, length-vs-width flags), not by "eyeballing"; an image spot-check is optional, not the check.
- **Container tickets** — `.docx`: explain scope from the sub-task list; `.xlsx`: one placeholder row whose Comments list the sub-task keys and note that cases follow once sub-tasks are analyzed. Never skip.