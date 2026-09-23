# Studio_ExplorerWorkspace fix patterns

## Known special cases

- **Background account mismatch.** `Studio_Explorer_Workspace.feature` actually operates under the "PP engineering test" account, not the "automation@pulsepoint" account named in its own `Background:` login step's literal text. Don't trust the Background's `"..." application is logged in successfully with Account "..."` string at face value for this feature — switch to "PP engineering test" via the buyer-portal account switcher (see `_default.md`'s Navigation utilities section) before replaying any scenario step, or every later observation will be against the wrong account's data. When in doubt, confirm the actual target account with the user rather than assuming the Background text is accurate.

## Run log

(No script-issue fixes logged yet for this feature.)
