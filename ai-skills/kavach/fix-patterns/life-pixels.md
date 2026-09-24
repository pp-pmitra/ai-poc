# Life_Pixels fix patterns

## Run log
- 2026-09-24: 5/5 scenarios failed at the Background login. The server rejected the Demo credentials ("Your login attempt was not successful"). Needs Investigation: environment/credentials, not a script issue.

## Learned notes
- A Background TimeoutError at `Navigation.selectAndClickExternalUserApplicationType` (waitForURL) while still on `LifeLogin.aspx?AspxAutoDetectCookieSupport=1` means login was rejected. Check the `.ValidatorMessage` text before diagnosing any locator. If it shows "Your login attempt was not successful", suspect the credentials or account lock, not the script.
