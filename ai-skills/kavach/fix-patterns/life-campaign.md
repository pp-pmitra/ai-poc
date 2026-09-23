# Life_Campaign fix patterns

## Run log

### 2026-09-16

- `TacticSettings.HOUSEHOLD_TAB` / `RULE_LEGAL_POPULATIONS_HOUSEHOLD_TAB` (`TacticSettings.java:127,134`) — `//button[normalize-space(text())='Household']` is stale; the tab was migrated to a design-system tab-switch component (`div[role='tablist'][aria-label='Tab switch']`). Fix: `getByRole(AriaRole.TAB)` scoped under the tablist container. See `_default.md`'s tab-switch/shadow-root migration pattern.
- `Accounts.SEARCH_BUTTON` (`Accounts.java:125`) — `//span[text()='Search']` text now lives inside a `<ds-toggle>` shadow root; XPath can't see it. Fix: `ds-toggle button[aria-label='Search']` (CSS pierces open shadow DOM).
- `TacticSettings.GEO_TARGETS_UPLOAD_BUTTON` (`TacticSettings.java:142`) — same shadow-root pattern as above for "Upload": `//button[normalize-space()='Upload']` → `ds-toggle button[aria-label='Upload']`. Affects both the multi-line-item creation flow and the targeting-rules-preserved-across-tabs flow (two scenarios, one fix).
- `Campaigns.SAVE_CUSTOM_FIELD` (`Campaigns.java:139`) — `//button[normalize-space()='Save']` matches 2 nodes (ambiguous — another "Save" button exists elsewhere on the campaign-creation page). Needs scoping to the custom-field panel; exact wrapper class not yet confirmed live (proposed direction: scope relative to `CUSTOM_FIELD_INPUT`'s ancestor panel/modal).
