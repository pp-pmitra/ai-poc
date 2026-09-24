# Live-replay verdict — 2026-09-24 15:15

> ⛔ **Blocked live replay** — the held Life browser's authenticated session was lost mid-replay: `Session expired on held Life browser (CDP 9223): renderer target crashed during account switch to automation@pulsepoint; all subsequent Buyer app navigations (allPixels, campaign) redirect to LifeLogin.aspx`. 2 of 2 escalated groups (2 scenarios) were left undiagnosed by live replay. **Action:** restart the Life auth bootstrap (`mvn test -Dtest=LifeAuthStateBootstrapTest -Dauth.environment=Demo -Dauth.userType=<Internal|External> -Dauth.holdSeconds=<budget>`), confirm CDP port 9223 is serving an authenticated session (not `LifeLogin.aspx`), and re-run kavach-diagnose to diagnose these two groups.

## Summary

- 🔴 Critical (confirmed product bugs): 0
- 🟢 High-confidence script fixes: 0
- 🟡 Potential product bugs (needs review): 0
- ⚪ Needs investigation / not reproduced: 2

## 🔴 Critical

None.

## 🟢 High-confidence script fixes

None.

## 🟡 Potential product bugs

None.

## ⚪ Needs investigation / not reproduced

| Scenario | Feature file | Failed step | Verdict | Confidence | Verified | Detail |
|---|---|---|---|---|---|---|
| Manage a Smart Pixel without associated Smart List (Create, Edit and Deactivate) (Example #1) | Life_Pixels.feature | When User deactivates the created pixel | Needs Investigation | Low | Static | Live replay blocked: Session expired on held Life browser (CDP 9223): renderer target crashed during account switch to automation@pulsepoint; all subsequent Buyer app navigations (allPixels, campaign) redirect to LifeLogin.aspx |
| Manage a Conversion Pixel (Create, Edit and Remove) (Example #1) | Life_Pixels.feature | When User tries to save the Conversion pixel without entering any details, an error message should be displayed | Needs Investigation | Low | Static | Live replay blocked: Session expired on held Life browser (CDP 9223): renderer target crashed during account switch to automation@pulsepoint; all subsequent Buyer app navigations (allPixels, campaign) redirect to LifeLogin.aspx |

**What was attempted before the block:** Both failures were escalated by the failure-triage skill's cheap no-browser tier (neither was mechanically intermittent, neither had a fix-pattern-cache hit, and the batched text-only LLM fallback could not resolve either from static evidence alone — a 401 on `LifeSSEHandler.ashx` appears alongside both failures but is a background SSE polling call unrelated to either target locator). Live replay then navigated into the Life buyer app (`/Buyer/#/setup/allPixels`), confirmed the session had landed on the default `buyer2@ppcom` account (per `_default.md`'s account-switch step), and began switching to the Background's `automation@pulsepoint` account via the documented account-switcher locators (`ACCOUNT_NAME` → `ACCOUNT_SEARCH` → `ACCOUNT_ITEM`). The browser tab's renderer crashed (`Target crashed`) immediately after clicking the matched account item; every navigation attempted afterward (`/Buyer/#/setup/allPixels`, `/Buyer/#/campaign`) redirected to `LifeLogin.aspx`, confirming the authenticated session itself — not just the in-app account context — was lost. No login/credential action was attempted, per this skill's constraints; the run stopped and escalated the block rather than retrying in a loop.

Neither scenario's own step definition or page-object locator was reached live, so no fix can be proposed or ruled out yet:
- `SmartPixel.deactivatePixel()` (`src/main/java/pages/life/SmartPixel.java:101-105`) — clicks `DEACTIVATE_PIXEL_ICON` (`//app-icon-lable-link[@icon='20-clear.svg']`) then `DEACTIVATE_PIXEL_BUTTON` (`//span[text()='Deactivate']`). The captured trace's `pageText` at failure time shows a "Deactivation Confirmation ... Cancel Deactivate" dialog was open, so the confirmation dialog itself rendered — worth checking on re-run whether `//span[text()='Deactivate']` is a stale exact-text-node locator (see `_default.md`'s "text in nested child element" pattern) now that the dialog is a design-system component, rather than the dialog failing to open at all.
- `Pixels.clickCancelButton()` (`src/main/java/pages/life/Pixels.java:196-198`) — clicks `CANCEL_BUTTON` (`//button[contains(@class,'cancel secondary button') and normalize-space(text())='Cancel']`) after 3 validation-error round trips on the Conversion Pixel form; captured `pageText` at failure was just `"toast-container"`, suggesting a toast/snackbar may have been covering or delaying the Cancel button — worth checking `_default.md`'s toast-overlay hardening pattern on re-run.

These are hypotheses from static code reading only (Phase 3.1), not live-verified — do not treat them as fix proposals.
