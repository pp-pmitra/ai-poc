# Default fix patterns (all features)

Cross-feature patterns consulted during every kawach run. Feature-specific pattern files
(e.g. `life-campaign.md`) supplement these; they never override them.

---

## Dismiss / wait hardening patterns

When a scenario passes live as-is (verdict `Not Reproduced — Passed Live Replay`), check
whether any of these instabilities could cause intermittent failure in CI. If one applies,
mention it as an optional hardening note in the Conclusion — do not force a fix.

| Symptom | Hardening pattern |
|---|---|
| Modal/dialog still animating when next click fires | Add `waitForSelector` on the modal's close button before proceeding, or `page.waitForFunction(() => !document.querySelector('.modal'))` after dismissal |
| Toast/snackbar overlaying the next target element | Wait for toast to disappear: `page.waitForSelector('.toast', { state: 'detached' })` |
| Loader/spinner visible after navigation | `page.waitForSelector('.loading-indicator', { state: 'detached' })` before interacting |
| Dropdown closes and re-opens immediately (focus trap) | Confirm the dropdown is detached before the next click: `page.locator('//div[@id="accountSwitcher"]').waitFor({ state: 'detached' })` |
| Button present but stale after SPA re-render | Re-query the locator after the triggering action instead of holding the element reference across a navigation |

---

## Locator false-positive guard: text in nested child element

**Pattern:** `contains(text(), 'X')` or `text() = 'X'` matches only direct text nodes. When
the element's visible label is rendered inside a nested `<span>`, `<strong>`, or other child
element (common after a design-system migration), these XPath predicates match 0 elements
even though the element is visible on screen.

**How to detect:** Run `.evaluate(el => el.innerHTML)` on a broader selector (e.g., the parent
`<button>` or `<label>`). If the text appears inside a child tag, the predicate is the problem,
not the element's presence.

**Fixes (in preference order):**
1. `normalize-space(.) = 'X'` — matches full subtree text, trims whitespace
2. `descendant::text() = 'X'` — explicit subtree text match
3. Switch to an attribute-based locator if the element has a stable `data-testid`, `aria-label`,
   or `id` — these are migration-proof

**Example:**
```
// Broken — matches only direct text node, fails when label is in <span>
//button[contains(text(),'Save Draft')]

// Fixed — full subtree match
//button[normalize-space(.) = 'Save Draft']

// Best — attribute-based (migration-proof)
//button[@data-testid='save-draft-btn']
```

Keep the literal `.evaluate(el => el.innerHTML)` output as evidence in `productBugArtifacts`
if this check rules out a stale locator for a `confirmed_product_bug` receipt.

---

## Locator false-positive guard: text/attributes inside an open shadow root

**Pattern:** after a design-system migration to web components (`ds-toggle`, `ds-button`,
`ds-tab-switch`, etc.), the element's visible text and sometimes its meaningful attributes
(e.g. an icon's `title`) live inside the component's **open shadow root**, not in its light-DOM
children. XPath cannot pierce shadow roots at all, so `contains(text(), 'X')` and similar
predicates match 0 elements even though the text is visible on screen. A case-sensitivity
change often rides along with the same migration (e.g. `archive` → `Archive`).

**How to detect:** `.evaluate(el => el.shadowRoot ? el.shadowRoot.textContent.trim() : 'NO_SHADOW')`
on the custom-element host. If real text comes back, the light-DOM locator is the problem, not
the element's presence. For attribute-based locators, dump `el.outerHTML` and check for a design
wrapper (`app-ds-button-wrapper`, `app-ds-tab-switch-wrapper`, etc.) and whether the attribute's
casing changed.

**Fixes (in preference order):**
1. CSS locator that pierces the shadow root: `ds-toggle button[aria-label="Active"]`,
   `ds-button[variant='primary'] button` — Playwright CSS selectors pierce **open** shadow DOM;
   XPath never does.
2. `.getByRole(AriaRole.X)` chained off a light-DOM ancestor locator, when the component exposes
   a proper ARIA role (e.g. `tab`) — works even without a stable CSS hook on the host.
3. If only an attribute casing changed (not a shadow-root move), a case-insensitive XPath match:
   `contains(translate(@title,'A','a'), 'archive')`.

**Example (seen 2026-09-10, Life_Creatives / Life_CuratedMarket):**
```
// Broken — text lives in shadow root, XPath can't see it
//div[contains(text(), 'Active')]/parent::button

// Fixed — CSS pierces shadow DOM
ds-toggle button[aria-label="Active"]

// Broken — button tag no longer exists, replaced by a tab component
//button[contains(@name,'VideoTechType')]

// Fixed — ARIA role on the new component
page.locator("//label[@id='label-format-type']/following-sibling::div//app-ds-tab-switch-wrapper").getByRole(AriaRole.TAB)

// Broken — save button is now a shadow-DOM ds-button, label text not in light DOM
//button[contains(@class,'okButton')]

// Fixed — CSS descendant selector pierces the open shadow root
ds-button[variant='primary'] button
```

---

## Locator false-positive guard: invisible/zero-width character appended to label text

**Pattern:** a design-system change appends an invisible codepoint right after a visible
label — typically an icon/tooltip hook or an a11y marker (`⁠` WORD JOINER, `​`
ZERO WIDTH SPACE, `‌`/`‍` ZWNJ/ZWJ, `﻿` BOM, `­` soft hyphen). The label
still *reads* correctly on screen, but the rendered text node is now `"X⁠"` instead of
`"X"`. `normalize-space()` strips ASCII whitespace only, not these codepoints, so an
**exact-match** XPath text predicate — `text()='X'` or `normalize-space(...)='X'` — never
matches and times out. A `contains(text(), 'X')` predicate is unaffected, since `"X"` is
still a substring of `"X⁠"`.

**How to detect:** Phase 2.5's offline DOM analyzer (`offline_dom_analyzer.py`) now catches
this mechanically — it's a `tier2_offline_dom` receipt with `diagnosisKind:
"invisible_char_text_mismatch"`, `verdict: "script_issue_fix_proposed"`, `confidence: "high"`.
It works even when the panel holding the label never made it into the saved `page-source.html`
(e.g. a CDK overlay/portal) — the check runs against the separately-captured `pageText`
(plain innerText), regex-searching for the target immediately followed by one of the
invisible codepoints above, and reports the exact `U+XXXX` value found.

If diagnosing manually: `repr()` the raw `pageText` string around the target label and look
for stray codepoints between the label and the next visible token — a lowercase substring
check (`"household" in pagetext.lower()`) will silently pass even when this bug is present,
since the invisible character sits *after* the match, not inside it.

**Fix:** strip the invisible character(s) before comparing, rather than assuming the element
is missing or renamed:
```
// Broken — normalize-space() doesn't strip U+2060, exact match never succeeds
//button[normalize-space(text())='Household']

// Fixed — translate() removes the invisible codepoint before the equality check
//button[translate(normalize-space(text()), '&#8288;', '') = 'Household']
```
(In Java, pass the literal character via `"⁠"` in the format string rather than the
HTML entity shown above.) Alternatively, switch to a `contains()`-style or attribute-based
locator if exact equality isn't required.

**Do not** propose renaming/removing the locator's target text or escalate to a product-bug
verdict for this pattern — the label is correct; only the equality comparison is broken.

---

## Navigation utilities

Hardcoded selectors for the buyer portal and account switcher used in Phase 3.2. Update here
if the app migrates these UI elements to new design-system components.

| Step | Locator |
|---|---|
| Open buyer portal | `//span[contains(@class,'buyerPortalLink')]` |
| Check current account name | `//div[@class='accountname']` |
| Account switcher search input | `//div[@id='accountSwitcher']/input[@placeholder='Search']` |
| Account switcher result item | `//div[@id='accountSwitcher']//div[@class='item']` |

If live replay universally fails at the account-switch step across multiple features, suspect
these selectors before diagnosing any individual test failure.

---

## Bootstrap / CDP error handling

### Dead CDP endpoint

**Symptom:** The first `browser_navigate` call (or any early browser action) fails with a connection-refused or "Target closed" error rather than a Playwright tool-permission error.

**Cause:** The CDP bootstrap (`LifeAuthStateBootstrapTest` / `StudioAuthStateBootstrapTest`) has died or was never started. This is distinct from a `PLAYWRIGHT_TOOL_REJECTED` error (which is a permission-model rejection, not a connection failure).

**Action:** Stop immediately and print:
```
CDP_ENDPOINT_DEAD: <port> — <error text>
```
Do not attempt to reconnect, re-navigate, or fall back to a static-only diagnosis of the blocked groups. Then follow **Live replay blocked** in the `live-replay-diagnosis` skill's Phase 3: run the validator with `--blocked-reason "CDP_ENDPOINT_DEAD: <port> — <error text>"` and still write the verdict report (blocked groups listed as Needs Investigation with the reason). Ask the operator to restart the matching bootstrap and re-run kawach to diagnose them.

- Life bootstrap uses port `9223`. Restart with `mvn test -Dtest=LifeAuthStateBootstrapTest -Dauth.environment=<env> -Dauth.userType=<type> -Dauth.holdSeconds=<budget>`.
- Studio bootstrap uses port `9224`. Restart with `mvn test -Dtest=StudioAuthStateBootstrapTest -Dauth.holdForCdp=true -Dauth.holdSeconds=<budget>`.
