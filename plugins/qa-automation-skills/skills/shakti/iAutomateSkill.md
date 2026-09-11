---
name: iautomate
description: >-
  Drive @todo Cucumber (BDD/Playwright/Java) scenarios to done, one at a time,
  in the connected qa-automation repo. Discovers @todo scenarios grouped by
  feature file; for each: grep-based reuse gate, live browser exploration of
  only the new/changed steps, a full dry-run in the browser, then retag
  @todo -> @regression and commit. Skips + logs blockers, never freezes. One PR
  per feature file. No local checkout; Claude stops at "green in the browser"
  and the developer runs mvn locally.
---

# iautomate — @todo queue driver

Turn `@todo`-tagged Gherkin scenarios into implemented, browser-verified
automation, one at a time, reusing existing glue and page objects. Claude's job
ENDS when the full flow runs cleanly in the browser; the developer runs the real
`mvn test` locally afterward.

## Surfaces (no local checkout)
- **GitHub connector** (`mcp__github__*`) — read feature/glue/POM files, create
  branch, commit files, open PRs. The repo is **never cloned to disk**; the
  developer's local working directory (whatever it's named) is irrelevant to this
  skill. All reads and writes go through the connector against the remote.
- **A connected browser MCP** — drive the live site like a human and harvest real
  selectors. Do **not** assume a specific server: at batch start detect whichever
  is wired up (e.g. `mcp__playwright__*`, `mcp__Claude_Browser__*`, or
  `mcp__claude-in-chrome__*`) and use that one throughout. If none is connected,
  every NEW/EXTEND step is a blocker — say so instead of guessing selectors.

## Batch preflight (do this BEFORE reading any scenario)
Confirm each item explicitly with the user or by a cheap probe; do not assume.
1. **Repo owner/name.** Default `pulsepointinc/qa-automation`; confirm with the
   user rather than trusting any cached note. (The remote repo is the only
   identity that matters — the developer's local folder name is irrelevant.)
2. **Connector access.** Do one read (repo root on the base branch) to prove the
   GitHub connector is authorized for this (private) repo. If it 404s/403s, stop
   and report — nothing downstream can work.
3. **Browser MCP present.** Confirm a browser server is connected (see Surfaces).
4. **Base branch** (default `main`) and **working branch**.
   - If the working branch **does not exist**, create it from the base branch.
   - If it **already exists**, do NOT force-reset it — surface that it exists and
     ask: reuse as-is, pick a new name, or (only on explicit confirmation) reset.
5. **Branch/PR model** (see Delivery) — confirm before the first commit.
6. **Test creds.** Pasted once per session; used only in the browser, **NEVER
   committed**. Note plainly that they will live in the chat transcript.

Env / app / account are NOT asked — they are read from each scenario's
`Background`.

## Guiding rules
1. **Live-first.** Validate every uncertain locator in the browser BEFORE
   writing it. The dry-run confirms; it is not the debug loop.
2. **Minimal, additive diff.** Locators -> existing POM; glue -> the app's Steps
   file. Guard new branches by the workspace-type/variant tracker. Never rewrite
   shared step bodies.
3. **Strictly serial.** Fully finish one scenario (dry-run passes -> retag ->
   commit) before starting the next. A blocked scenario is skipped + logged, it
   NEVER freezes the queue.
4. **Prereqs are mandatory.** Always treat each scenario's Background (env step +
   login step) as prerequisites; confirm their glue exists before Gate C.
5. **Navigation-first exploration.** `navigation-tree.html` at the repo root is the
   authoritative click-path source (same graph Sutra/iDesign resolved the
   scenario's Background/navigation preamble from) — consult it before free-form
   exploring so Gate C drives straight to the target page instead of guessing a
   route.

---

## Navigation context (once per batch)

Fetch `navigation-tree.html` from the repo root via the connector alongside the
glue/POM files in DISCOVER. Extract ONLY the `GRAPH` object literal — the value
assigned in `const GRAPH = { ... };` inside the page's `<script>` block; ignore
the surrounding HTML/CSS/JS (rendering code, the `MC` module-color map,
legend/DOM-building logic). Slice from that literal's opening `{` to its
matching closing `}` (before the trailing `;`) and parse it — it uses only
double-quoted keys/string values, so it parses directly. Build the same forward
adjacency map Sutra uses (node -> `[{target, action}]`), plus the reverse index
and `landing`/`MegaMenu` nodes. Reuse this parsed graph across every scenario in
the batch — never re-fetch or re-parse mid-batch.

Use it in Gate C as follows:
- Match the scenario's target page (from its Gherkin navigation steps, or the
  feature's module if the steps are still generic) against the graph's node keys.
- If matched, run the same shortest-path BFS Sutra used to author those steps, so
  you know in advance which real UI actions (`edge.action` strings, e.g. "Click
  Curated Markets") the scenario's navigation steps correspond to — drive Gate C's
  live exploration along that exact resolved path instead of clicking around to
  rediscover it. This does not replace live locator harvesting: the graph names
  the destination and the click sequence to get there, but the actual selector for
  each click/element is still harvested live per the Explore rules below (the
  graph carries no selectors, only page/action names).
- If the target node carries `framework_gap: true`, or no node matches at all,
  fall back to plain exploration as before — the graph gap is expected, not a
  blocker, and mirrors what Sutra would have flagged as a Framework Gap when the
  scenario was authored.
- A scenario whose live click sequence diverges from the graph's resolved path
  (a different menu item now reaches the page, an edge no longer exists) is a
  signal the map is stale, not that the app is broken — note it in the final
  report so the map can be refreshed; do not treat it as a scenario blocker on
  its own.

---

## DISCOVER (once per batch, after preflight)
1. Via the connector, list the feature files and find every scenario tagged
   `@todo`. Group them **by feature file**.
2. Fetch the app glue files (`*Steps.java`) and the POM classes once into
   context — the reuse gate and POM checks match against these in-memory (more
   reliable than remote code-search). Note the login/nav entry (`Navigation.java`).
3. Fetch and parse `navigation-tree.html` (see Navigation context below) once
   into context, alongside the glue/POM files.
4. Present the queue (feature file -> its @todo scenarios) and get the go-ahead.

Then loop feature files, and within each, its @todo scenarios — one at a time.

---

## Per-scenario pipeline (Gates A–D)

### A. Parse
Read the scenario. Read the feature's `Background`; extract env / app / account
from its prerequisite steps (do NOT ask the user for these). Produce the full
ordered list of concrete steps = Background steps + scenario steps. For a
Scenario Outline, bind the first data-bearing `Examples` row (or the row the
user pinned).

### B. Reuse gate (grep, no index)
For each step, normalize: `"..."` -> `{string}`, `<param>` -> `{string}`, bare
integer -> `{int}`. Search the loaded `*Steps.java` for a matching Cucumber
expression annotation.
- **Hit + body already covers this case** -> **REUSE** (drop; no work).
- **Hit but the body switches on a type/variant and has no branch for this
  scenario's case** -> **EXTEND** (add a guarded branch — known trap; text match
  != behavior match).
- **Miss** -> **NEW**.
Output the classified table. Only NEW + EXTEND proceed to the browser.

### C. Explore (browser — NEW/EXTEND only)
Log in with the pasted creds for the scenario's app; navigate using the
Navigation-context graph's resolved path when the target page matches a node
(see Navigation context above) — otherwise navigate as a human would. For each
NEW/EXTEND step:
- Perform the action live; harvest the authoritative selector + iframe chain
  from the MCP "Ran … code" output.
- Before minting a locator, grep the relevant POM for a reusable one; reuse if
  present, else mint from the harvested selector.
- Confirm structural/nested-iframe locators with `browser_evaluate` inside the
  frame (a11y snapshot abstracts the DOM; verify against the real element).

### C'. Dry-run (the success bar)
Click the **full** flow end-to-end in the browser — reused + new steps, in
order, from a clean state — using the final locators. This passing is the
definition of "done" for the scenario.
- On failure: re-inspect the failing locator/frame LIVE and fix, then retry.
- Bounded self-heal: after a few honest attempts, apply the **Blocker policy**.

### D. Write + retag + commit
Only after C' passes:
1. Write locators to the existing POM; write NEW glue to the app's Steps file;
   add guarded EXTEND branches.
2. **Retag**: in the feature file, change this scenario's `@todo` -> `@regression`
   (Hooks launches a browser only for @e2e/@regression at the developer's local run).
3. **Static self-check (no JVM)** — catch the two failures that would otherwise
   surface at the developer's local `mvn`:
   - each written `@When/@Then/...` annotation string is **character-identical**
     to its Gherkin step (normalized), and
   - method arity matches the `{string}`/`{int}` placeholder count.
4. Commit via the connector (one commit per scenario) to the branch chosen for
   this feature file (see Delivery).

---

## Blocker policy — SKIP & LOG, continue
If C' still fails after the bounded self-heals, or the scenario genuinely
contradicts the live app (e.g. Save disabled on an empty form while the scenario
expects inline errors on Save):
- Leave the scenario **as `@todo`** (untouched — no retag, no code committed for it).
- Record: scenario name, feature file, the failing step, the reason, and the
  failure screenshot.
- Continue to the next scenario. **Never freeze the queue on one scenario.**

## Delivery — one PR per feature file
Confirm the branch/PR model at preflight; two supported shapes:
- **Branch per feature file (default for "one PR per feature file").** Cut a child
  branch off the working branch for each feature file, commit that file's
  scenarios there, and open one PR (child -> base) per feature file. Keeps PRs
  isolated and independently mergeable.
- **Single branch.** Commit everything to the one working branch; open either one
  PR for the whole batch or, if still one-per-feature is wanted, sequential PRs
  from the same branch (later PRs include earlier commits — call this out).

Each PR body includes a **"Skipped / blocked"** section listing the scenarios left
as `@todo` and why. If every scenario in a feature file was blocked, open no PR
for it — just report. Then move to the next feature file.

## Final report
Summarize: per feature file — scenarios done (retagged, PR link) vs skipped
(with reasons). Remind the developer to run `mvn test` locally to confirm the
Java suite compiles and passes.