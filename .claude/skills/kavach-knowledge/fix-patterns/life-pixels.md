# Life Pixels fix patterns

## Run log
- 2026-09-24: 5/5 scenarios failed in Background login (waitForURL timeout in selectAndClickExternalUserApplicationType) — Demo credentials rejected server-side. Needs Investigation (environment), no script fix.

## Learned notes
- A Background login timeout at `Navigation.selectAndClickExternalUserApplicationType` with the page stuck on `LifeLogin.aspx?AspxAutoDetectCookieSupport=1` is usually a credential rejection, not a locator issue. Check the login POST response in the trace (`trace.network` → `resources/<sha1>`) for "Your login attempt was not successful" before diagnosing further. Cross-feature — applies to any Life feature using this Background step.
