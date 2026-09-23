---
name: kavach
description: >-
  Failure analysis engine for Cucumber/Playwright test runs. Replays failed
  scenarios live in the browser, classifies each as a script issue (fix
  proposed) or a product bug (flagged only), and writes a timestamped verdict
  report. Invoke when the user asks to analyze, diagnose, or replay test
  failures. Never auto-applies fixes.
allowed-tools: >-
  Bash(*) Read Write(*) Grep Glob
  mcp__playwright__browser_navigate
  mcp__playwright__browser_navigate_back
  mcp__playwright__browser_snapshot
  mcp__playwright__browser_click
  mcp__playwright__browser_type
  mcp__playwright__browser_fill_form
  mcp__playwright__browser_press_key
  mcp__playwright__browser_hover
  mcp__playwright__browser_drag
  mcp__playwright__browser_drop
  mcp__playwright__browser_select_option
  mcp__playwright__browser_find
  mcp__playwright__browser_wait_for
  mcp__playwright__browser_evaluate
  mcp__playwright__browser_run_code_unsafe
  mcp__playwright__browser_handle_dialog
  mcp__playwright__browser_file_upload
  mcp__playwright__browser_emulate_media
  mcp__playwright__browser_resize
  mcp__playwright__browser_tabs
  mcp__playwright__browser_take_screenshot
  mcp__playwright__browser_console_messages
  mcp__playwright__browser_network_requests
  mcp__playwright__browser_network_request
  mcp__playwright__browser_close
---

<!-- ARCHITECTURAL NOTE: This file is intentionally a thin entry point.
     Team policy requires all kavach content to live under ai-skills/kavach/.
     This SKILL.md exists solely to register the skill for slash-command invocation.
     The canonical instruction body is ai-skills/kavach/iFix.md. -->

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

Read `ai-skills/kavach/iFix.md` in full and follow it exactly. Do not summarize it back to the user first — just follow it.
