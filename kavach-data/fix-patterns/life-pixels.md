# Life_Pixels fix patterns

## Run log

- 2026-09-24: Two `TimeoutError` failures in `Life_Pixels.feature` (Smart Pixel deactivate confirmation, Conversion Pixel cancel) both live-replay-confirmed as the same shadow-DOM migration pattern documented below. See `kavach-data/history/replay-verdict-2026-09-24-1353.md`.

## Known good fixes

### Deactivation Confirmation modal — Deactivate/Cancel buttons moved to `ds-button` shadow DOM

**Symptom:** `SmartPixel.deactivatePixel()` times out (120000ms) on `DEACTIVATE_PIXEL_BUTTON = page.locator("//span[text()='Deactivate']")` after clicking the pixel's deactivate ("20-clear.svg") icon.

**Cause:** the "Deactivation Confirmation" modal's action-footer buttons were migrated from plain `<span>` text elements to the `ds-button` design-system web component. The visible "Deactivate"/"Cancel" text now lives inside `ds-button`'s **open shadow root** (`<app-ds-button-wrapper variant="primary"><ds-button role="button" variant="primary"><button data-variant="primary"><span>Deactivate</span></button></ds-button></app-ds-button-wrapper>`), which XPath text predicates cannot see.

**Fix:** shadow-piercing CSS locator scoped to the modal's action footer:
```java
// Broken
this.DEACTIVATE_PIXEL_BUTTON = page.locator("//span[text()='Deactivate']");

// Fixed — CSS pierces the ds-button shadow root
this.DEACTIVATE_PIXEL_BUTTON = page.locator(".popup-actions-footer ds-button[variant='primary'] button");
```
Live-verified 2026-09-24: corrected locator resolved `count()=1`, `visible=true` on the first attempt; click produced the "Pixel Deactivated successfully" toast.

### New Conversion Pixel panel — Cancel button moved to `ds-button` shadow DOM

**Symptom:** `Pixels.clickCancelButton()` times out (120000ms) on `CANCEL_BUTTON = page.locator("//button[contains(@class,'cancel secondary button') and normalize-space(text())='Cancel']")` after the mandatory-field validation sequence on the New Conversion Pixel panel.

**Cause:** same shadow-DOM migration as above — the native `<button class="cancel secondary button">` no longer exists; the Cancel control is now `<app-ds-button-wrapper label="Cancel" variant="secondary"><ds-button>` with its label rendered inside the shadow root.

**Fix:**
```java
// Broken
this.CANCEL_BUTTON = page.locator(
    "//button[contains(@class,'cancel secondary button') and normalize-space(text())='Cancel']");

// Fixed — CSS pierces the ds-button shadow root; label attribute on the light-DOM wrapper is stable
this.CANCEL_BUTTON = page.locator("app-ds-button-wrapper[label='Cancel'] button");
```
Live-verified 2026-09-24: corrected locator resolved `count()=1`, `visible=true` on the first attempt; click reset the panel to a blank "New Pixel" state with validation errors cleared.

## Learned notes

- Both fixes are instances of `_default.md`'s "Locator false-positive guard: text/attributes inside an open shadow root" pattern — check that pattern file first for any new `Life_Pixels.feature` failure whose failing locator is an XPath `text()`/`contains(text(),...)` predicate on what used to be a plain `<span>`/`<button>`.
- A long-held CDP session (port 9223) can transiently show a `ChunkLoadError` / `ERR_INSUFFICIENT_RESOURCES` that leaves every `ds-button` element un-hydrated (empty shadow root, zero-size bounding box) sitewide. This is browser-side resource exhaustion, not a product bug — a plain `fetch()` of the failed chunk returns `200 OK` and a page reload hydrates normally. If a `ds-button`-based locator looks broken, reload first and recheck `el.classList.contains('hydrated')` before concluding it's a real regression.
