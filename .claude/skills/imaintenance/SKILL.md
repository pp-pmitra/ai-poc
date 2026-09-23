---
name: imaintenance
description: >-
  Applies kavach-proposed script fixes to Cucumber/Java page-object files,
  verifies each fix passes Maven three times, and raises a single PR per run.
  Invoke after kavach has produced a verdict file containing
  script_issue_fix_proposed entries, or in isolation mode (IMAINTENANCE_MODE=isolation
  or IMAINTENANCE_TARGET set, no receipt file) to apply a known fix pattern from
  a live Maven observation pass. Never diagnoses unknown failures — kavach does that.
  Never commits without a Maven green. Always interactive; never runs unattended.
allowed-tools: Bash(*) Read Write(*) Edit Grep Glob
user-invocable: true
---

<!-- ARCHITECTURAL NOTE: This file is intentionally a thin entry point.
     Team policy requires all kavach content to live under ai-skills/kavach/.
     This SKILL.md exists solely to register the skill for slash-command invocation.
     The canonical instruction body is ai-skills/kavach/iMaintenance.md. -->

## Reference files
- [`ai-skills/kavach/fix-patterns/_default.md`](ai-skills/kavach/fix-patterns/_default.md)
- [`ai-skills/kavach/fix-patterns/life-campaign.md`](ai-skills/kavach/fix-patterns/life-campaign.md)
- [`ai-skills/kavach/fix-patterns/life-campaign-dashboard.md`](ai-skills/kavach/fix-patterns/life-campaign-dashboard.md)
- [`ai-skills/kavach/fix-patterns/life-create-campaign.md`](ai-skills/kavach/fix-patterns/life-create-campaign.md)
- [`ai-skills/kavach/fix-patterns/life-create-creative.md`](ai-skills/kavach/fix-patterns/life-create-creative.md)
- [`ai-skills/kavach/fix-patterns/life-create-pixel.md`](ai-skills/kavach/fix-patterns/life-create-pixel.md)
- [`ai-skills/kavach/fix-patterns/life-create-report-template.md`](ai-skills/kavach/fix-patterns/life-create-report-template.md)
- [`ai-skills/kavach/fix-patterns/life-creatives.md`](ai-skills/kavach/fix-patterns/life-creatives.md)
- [`ai-skills/kavach/fix-patterns/life-curatedmarket.md`](ai-skills/kavach/fix-patterns/life-curatedmarket.md)
- [`ai-skills/kavach/fix-patterns/life-export-download.md`](ai-skills/kavach/fix-patterns/life-export-download.md)
- [`ai-skills/kavach/fix-patterns/life-line-item-creation.md`](ai-skills/kavach/fix-patterns/life-line-item-creation.md)
- [`ai-skills/kavach/fix-patterns/life-lineitem.md`](ai-skills/kavach/fix-patterns/life-lineitem.md)
- [`ai-skills/kavach/fix-patterns/life-npilists.md`](ai-skills/kavach/fix-patterns/life-npilists.md)
- [`ai-skills/kavach/fix-patterns/life-pixels.md`](ai-skills/kavach/fix-patterns/life-pixels.md)
- [`ai-skills/kavach/fix-patterns/life-pmp.md`](ai-skills/kavach/fix-patterns/life-pmp.md)
- [`ai-skills/kavach/fix-patterns/life-reporttemplates.md`](ai-skills/kavach/fix-patterns/life-reporttemplates.md)
- [`ai-skills/kavach/fix-patterns/life-runreport.md`](ai-skills/kavach/fix-patterns/life-runreport.md)
- [`ai-skills/kavach/fix-patterns/life-schedulereport.md`](ai-skills/kavach/fix-patterns/life-schedulereport.md)
- [`ai-skills/kavach/fix-patterns/life-tactic.md`](ai-skills/kavach/fix-patterns/life-tactic.md)
- [`ai-skills/kavach/fix-patterns/life-tactic-creation.md`](ai-skills/kavach/fix-patterns/life-tactic-creation.md)
- [`ai-skills/kavach/fix-patterns/life-targeting-template-creation.md`](ai-skills/kavach/fix-patterns/life-targeting-template-creation.md)
- [`ai-skills/kavach/fix-patterns/life-targetings.md`](ai-skills/kavach/fix-patterns/life-targetings.md)
- [`ai-skills/kavach/fix-patterns/life-targetingtemplates.md`](ai-skills/kavach/fix-patterns/life-targetingtemplates.md)
- [`ai-skills/kavach/fix-patterns/studio-explorerworkspace.md`](ai-skills/kavach/fix-patterns/studio-explorerworkspace.md)

Read `ai-skills/kavach/iMaintenance.md` in full and follow it exactly. Do not summarize it back to the user first — just follow it.
