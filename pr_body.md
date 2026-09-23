## Summary
Adds new Mega Menu navigation smoke coverage under `shared` module for ticket **ET-25052**. No requirements payload was supplied with this ticket, so coverage was synthesized directly from the codebase navigation graph, targeting menu-driven navigation paths that currently have no corresponding feature file coverage.

## Scope
- New feature file: `src/test/resources/features/shared/Navigation_MegaMenu.feature`
- Covers:
  1. Primary Mega Menu module link navigation (Campaigns, Studio, Signal)
  2. Mega Menu "Menu Links" submenu navigation (NPI Lists, Domain & App Lists, Keyword Lists, IP Address Lists, Email Lists, Curated Markets, Pixels, Targeting Templates, Creative Library, Smart Actions)
  3. Campaign Reporting submenu navigation (Run a Report, Report Templates, Scheduled Reports, Generated Reports)

## Exclusions
- Pages flagged `framework_gap: true` in the navigation graph (e.g., HCP365 Dashboard, Audience Manager, Fabric, Deals, Deal Groups, Tag Manager, Personalization API, IBIQ Dashboards, Medscape* pages, Collections, HCP365 Data Mapping, Destinations, Analytics Chatbot, Media Planner, Campaign Settings, HCP365 Reports submenu, Knowledge Base, Customer Support, Audience Marketplace, Administration submenu deep pages) were excluded, as they represent known automation gaps not yet supported by the framework.

## Notes for Reviewers
- All new scenarios are tagged `@todo` since step definitions for Mega Menu interaction (`User clicks Hamburger icon to open Mega Menu`, `User clicks on "<MENU_ACTION>" link in Mega Menu`, generic page-navigation assertion) could not be confirmed against the existing step glue layer in the provided context.
- Please confirm in review whether equivalent step definitions already exist under `admin`/`shared` step files before implementation to avoid duplication.
- Background block reuses the standard Life login + Campaign Dashboard verification pattern consistent with existing `life/*.feature` files for framework consistency.

## Test Coverage Type
- @regression (navigation smoke coverage)

## Related Ticket
ET-25052