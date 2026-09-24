# Life_LineItem.feature fix patterns

## Run log

- 2026-09-24: 7 failures triaged (1 mechanically intermittent, 6 escalated to live replay). All 6 live-replayed failures were confirmed stale-locator script issues (design-system migration to `ds-*` web components + one invisible-character label + one CSS class rename on campaign rows) — no product bugs found. See `replay-verdict-2026-09-24-1444.md`.

## Known good fixes

| Symptom | Old locator | Fix |
|---|---|---|
| `verifyLineItemStatus()` times out waiting for the Incomplete/Complete label | `//span[contains(@class,'status-label')]/span` | `ds-status` — status label now renders as a bare `<ds-status>` custom element, no more `span.status-label` wrapper |
| `generateSequentialFlights()` times out clicking "Generate Flights" | `//span[text()='Generate Flights']` | `app-ds-button-wrapper[label='Generate Flights']` — button label text lives inside the `<ds-button>`'s open shadow root; target the light-DOM wrapper's `label` attribute instead |
| `selectBudgetDistribution()` times out clicking a Priority/Percentage/Dollars option | `container.locator("//button[normalize-space(.)='<option>']")` | `container.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions().setName(Pattern.compile(Pattern.quote(optionName))))` — options are now an `app-ds-tab-switch-wrapper`/`ds-tab-switch` shadow-DOM tab component, **and** each tab's visible label has a trailing U+2060 WORD JOINER invisible character, so use a substring/regex name match, not exact-equals |
| `clickCampaignFromDashboard()` times out clicking a campaign name in the dashboard list | `//span[contains(@class,'adv-camp-name')]//span` | `//a[contains(@class,'cl-entity__name--campaign')]` — campaign name is now a direct `<a>` anchor (with `href="#/campaign/<id>/dashboard"`), not a nested span; `adv-camp-name` class no longer exists anywhere in the DOM |
| `addCustomField()` times out clicking Save in the "Add Custom Field" popover | `//button[normalize-space()='Save']` | `//input[@placeholder='Field Name']/ancestor::div[contains(@class,'popover-box')]//app-ds-button-wrapper[@label='Save']` — there are 2 `app-ds-button-wrapper[label='Save']` elements on the page (page-level + popover); scope to the popover ancestor to disambiguate, in addition to swapping to the component attribute |

## Learned notes

- `//tr[contains(@class,'cl-li-row')]` (used to wait for the campaign list to finish loading) matches **both** campaign rows and line-item rows in the tree table — it happens to still "work" as a loading-finished signal today, but don't rely on it to mean "a campaign row is present"; campaign rows specifically use `cl-campaign-row`.
- The `401 GET /SSEManager/Buyer/LifeSSEHandler.ashx` network error seen right before several of these timeouts in the original trace did **not** reproduce as a blocking condition during live replay — treat it as incidental background noise on this feature rather than a root cause, unless a future run shows it actually blocking a structurally-sound locator.
