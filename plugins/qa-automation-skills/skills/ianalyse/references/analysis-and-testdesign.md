# ianalyse — Deep Analysis (STEP 11) & Test Design (STEP 12)

The "how to think" core of the skill. Read this before STEP 11. In both steps the model emits
**content as data** (validated JSON merged into the cache) — it never hand-writes file-formatting code.

---

## STEP 11 — the eight Deep-Analysis sections

Each ticket's analysis JSON carries these eight sections as fields. The formatter renders them as
`level 2` headings under the ticket's `level 1` heading — **the model never writes heading code.**

1. **Background** — state → change → behavior.
2. **Intent** — business value / problem solved.
3. **Cross-Functional Impact** — subsystems, apps, APIs affected.
4. **Requirement Gap** — `GAP-1, GAP-2 …`; must reason, not just say "none". Scope to
   **functional/logical gaps**: unspecified business/calculation rules, undefined error or edge
   behaviour, missing validation limits or thresholds, unhandled states or transitions, absent
   integration/API contracts, open permission conditions. Cosmetic/visual omissions (styling,
   layout, spacing, copy wording) are **not** gaps. **Missing AC is not itself a gap** — first infer
   the baseline functional rules from the epic/parent/linked context, then log a `GAP` only when a
   *critical* behaviour (a negative state, a data/format limit, a named error path, a permission
   boundary) genuinely cannot be inferred from that context. Vague-summary tickets get inferred
   coverage in STEP 12, not a blanket "entire ticket is a gap".
5. **Ambiguity** — `AMB-1, AMB-2 …`; flag **functional/logical ambiguity**: contradictory or
   conflicting rules, undefined terms that change behaviour, unclear state transitions, scope
   conflicts, unquantified limits/thresholds, **or a Title/Summary that mismatches the
   Description / AC**. Ignore purely visual/UX wording ambiguity with no behavioural consequence.
6. **Dependencies** — hard/soft blockers, open sub-tasks, unready `DPD-` tickets. A live DPD sets a
   flag the formatter renders as a `#FFF2CC` note.
7. **Historical Analysis** — cite actual `<productionProjectKey>` bugs from STEP 7 with a risk
   rating (LOW / MEDIUM / ELEVATED / HIGH).
8. **References** — Jira / Confluence / production sources.

If a ticket's analysis is drawn mainly from parent/epic/linked context, set a `contextNote` field
(source attribution) — the formatter renders it as a `#EBF3FB` paragraph.

Per ticket: emit the object, `json.loads`-validate it, merge into `cache.analysis[<key>]`. A
malformed object is regenerated for **that ticket alone** — never a whole batch.

---

## STEP 12 — Test Cases

### Focus — functional and logical, not UX

The exercise targets **functional and logical correctness**: what the system computes, stores,
validates, transforms, permits, rejects, and returns, and how it moves between states.

Pure UX/visual concerns — layout, spacing, alignment, color, typography, copy wording,
hover/animation styling, responsiveness — are **out of scope** and do not become requirements or
test cases. A UI or Figma detail is only in scope when it **encodes a functional rule** (e.g. Save
disabled until required fields validate; a field that must reflect a persisted value; a control that
appears only for an authorized role). There, the UI element is the *observable signal* of a
functional outcome — test the outcome, cite the surface as the check.

### 12a — Context-first decomposition *(the core rule; produces the JSON)*

Before emitting any row, build an internal coverage checklist from **all** available information
(ticket description + any AC, comments, parent/epic, linked issues, sub-tasks, Figma references,
implementation notes, and STEP 11 GAP/AMB items). Formal AC is one input, not a prerequisite.

**Part A — Requirement inventory.** List every discrete testable behaviour; count them as `R`;
attribute each to its source. A behaviour counts if it is an AC item, a behaviour in a parent/linked
ticket, a named error state, a field/format/limit rule, a business or calculation rule, a
data-persistence or transformation requirement, a permission condition, a state transition, an
integration/API contract, or a negative/excluded behaviour.

Keep the inventory **functional and logical**. A UI/Figma item counts only when it carries a
functional rule — count the rule it enforces, not the visual. Cosmetic/layout/styling details never
enter the inventory and never contribute to `R`.

**Part B — Coverage dimensions.** For each, add ≥1 test case if it applies: happy path (always) ·
secondary happy paths · field/input validation · business-rule & calculation correctness · data
integrity/persistence · error states · permission/role · state transitions · boundary/limit ·
multi-value/batch · integration/cross-system · regression (per relevant production bug) ·
out-of-scope guard.

**Minimum count (floor, never ceiling):**
```
minimum_TCs = R × 2                                    # one positive + one negative per requirement
            + applicable dimensions not already covered
            + regression anchors (one per relevant production bug)
```

**Each ticket's JSON carries the inventory + dimensions as auditable metadata** (not comments):
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
Calibration examples: 3 AC + 2 errors + 1 permission + 1 transition + 1 bug → (7)×2 + regression =
**14+**. · 9 AC + 6 errors → **30+**. · simple rename → 1×2 + 1 boundary = **3**. · no AC but a
parent epic describing 5 behaviours + 2 errors → **14** (AC absence never lowers the count).

### 12b — Row rules

- **Test ID:** `TC_<TICKET-KEY>_NN` (zero-padded).
- **Test Description:** opens with `[POSITIVE]` / `[NEGATIVE]` / `[EDGE]` / `[REGRESSION]` /
  `[BLOCKED]` and names the requirement it covers, e.g.
  `[POSITIVE — R03] File state shows "Uploaded" after valid template upload`.
- **Test Data:** concrete — actual account type, file format, field values, permission config, or
  system state. Write the real example value when it matters (e.g. a 21-char Deal ID).
- **Expected Result:** independently verifiable, stated as a **functional outcome** — the persisted
  value or resulting system state, the API field + value returned, the computed/derived result, the
  record created/updated/rejected, or the exact error/validation message that enforces a rule. A
  tester who never read the ticket must be able to judge pass/fail from this alone. Reference a UI
  surface (a toast, a disabled button, a highlighted field) only as the *observable signal* of that
  outcome — never as the thing under test.
- **Comments:** requirement source + any regression/DPD anchor (e.g.
  `Src: parent ET-25000 · Reg: HT-5039`). This is where source attribution lives in the sheet.
- **Owner / Actual Result:** leave blank.
- **BLOCKED** applies only when *zero* testable behaviour can be derived from the ticket, parent,
  epic, links, or comments (rare): one row, `Test Status = BLOCKED`, Comments state what's missing.

Each row is a JSON object with keys matching the columns (`testId`, `requirementId`, `description`,
`testData`, `expected`, `status`, `comments`; `owner`/`actual` empty). `build_xlsx.py` owns the
fills (BLOCKED `#FCE4D6` · PASS `#E2EFDA`), wrapping, alignment, and row height — the model sets
`status`, not colors.

Per ticket: emit `{ inventory, dimensions, rows[] }`, `json.loads`-validate, merge into
`cache.testcases[<key>]`, and update that ticket's `[Summary]` row data in the cache as you go.
