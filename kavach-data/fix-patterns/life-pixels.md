# Life_Pixels fix patterns

## Run log

- 2026-09-24: Diagnosed 3 failures (Life_Pixels.feature, Demo). 1 not-reproduced-intermittent, 2 script-issue fixes proposed (both design-system shadow-DOM / attribute-migration locator staleness). See `replay-verdict-2026-09-24-1321.md`.

## Known good fixes

### Deactivate-confirmation button — label moved into `ds-button` shadow root

**Symptom:** `SmartPixel.deactivatePixel()`'s click on `//span[text()='Deactivate']` times out even though the "Deactivation Confirmation" modal is open and visibly shows "Deactivate".

**Cause:** the modal's Cancel/Deactivate buttons were migrated to `<app-ds-button-wrapper><ds-button variant="...">` — the visible label lives in the component's open shadow root, not in the light-DOM `<span>` the old XPath targets (same class of issue as `_default.md`'s shadow-root guard).

**Fix:** scope a shadow-piercing CSS locator to the confirmation modal:
```java
// Before
this.DEACTIVATE_PIXEL_BUTTON = page.locator("//span[text()='Deactivate']");
// After
this.DEACTIVATE_PIXEL_BUTTON = page.locator("div.ui.modal.tiny ds-button[variant='primary'] button");
```

### Conversion Pixel "Cancel" button — migrated to `app-ds-button-wrapper`

**Symptom:** `Pixels.clickCancelButton()`'s click on `//button[contains(@class,'cancel secondary button') and normalize-space(text())='Cancel']` times out on the Conversion Pixel creation form.

**Cause:** the Cancel action moved to the same `app-ds-button-wrapper` design-system component already used for the adjacent Save button (`//app-ds-button-wrapper[@label='Save']`); the old locator's class names and light-DOM text no longer exist on the page (a coincidentally-matching but unrelated `button.cancelBtn` belonging to a hidden date-range-picker widget elsewhere on the page can produce a false positive if searching by text alone — verify visibility/bounding-rect before trusting a text match).

**Fix:** switch to the same attribute-based pattern already proven for Save:
```java
// Before
this.CANCEL_BUTTON = page.locator(
        "//button[contains(@class,'cancel secondary button') and normalize-space(text())='Cancel']");
// After
this.CANCEL_BUTTON = page.locator("//app-ds-button-wrapper[@label='Cancel']");
```

## Learned notes

- Pixels page account switch: `automation@pulsepoint` (per Background), reached via the top-nav "Life" link from the Admin Dashboard landing page, then the standard `_default.md` account-switcher flow (`accountname` → search → `.item`).
- Both fixes proposed this run follow the same root pattern: a broader design-system migration to `ds-button`/`app-ds-button-wrapper` components on the Pixels forms. If more Pixels-flow locators start timing out on button clicks, suspect this same migration before diagnosing from scratch.
