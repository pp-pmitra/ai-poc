# qa-automation

## Overview

This project / repository contains automation code for LIFE, HCP365 & STUDIO applications. Regression & Functional
testing scenario's written using business language for better understanding. Project has modular approach, it uses
features, step definitions, page classes & common functions etc.

## Technology

#### Playwright

#### JAVA

#### Cucumber

#### Maven

## Getting started

1. Clone repository as maven project, ensure all dependencies are resolved correctly. (**Note** - JDK version must be
   set in project properties)
2. Update TestRunner file with feature name / tag values as required to execute scenario's.
3. Create run configuration as shown below & execute.
   <img width="595" alt="image" src="https://github.com/user-attachments/assets/86967dc0-fc6b-47fe-8873-aad598bdff31" />

## Code Style (Checkstyle)

This repo runs [Checkstyle](https://checkstyle.org) with a deliberately lightweight ruleset
(`config/checkstyle/checkstyle.xml`) suited to an automation framework rather than a strict
production-app style guide. It checks:

- Line length (max 120 chars)
- No wildcard imports, no unused imports
- No 2+ consecutive blank lines, no trailing whitespace
- camelCase for methods, parameters, local variables & fields (fields may also be
  `UPPER_SNAKE_CASE`, since that's the existing convention for locator constants)

Only `src/main/java` is checked - tests, resources, and everything outside `src/` are out of
scope.

### When it runs

- `mvn verify` runs Checkstyle as part of the build.
- Every `git commit` runs it via the `.githooks/pre-commit` hook and cancels the commit on
  failure.

### Enabling the pre-commit hook

Hooks live in `.githooks/` (not `.git/hooks/`) so they're versioned with the repo, but
`core.hooksPath` itself is a local git setting - each clone needs to opt in once:

```
git config core.hooksPath .githooks
```

### Running it manually

```
mvn checkstyle:check
```

### Pre-existing violations

Violations that existed before Checkstyle was introduced are grandfathered in via
`config/checkstyle/suppressions.xml`, so turning this on didn't break the build for legacy
code. New or modified code is fully enforced. As a suppressed file gets cleaned up, remove its
entry from `suppressions.xml` to enforce it again; deleting the whole file lifts every
suppression at once.

## Feature File Formatting (gherkin-utils)

`.feature` files are checked for consistent Gherkin formatting (indentation, table column
alignment, blank lines) using [`@cucumber/gherkin-utils`](https://github.com/cucumber/gherkin-utils),
Cucumber's own formatter. A project wrapper removes the formatter's blank line between the last
step of a Scenario Outline and its `Examples` block. It only normalizes style - it doesn't check
step wording, tags, or scenario semantics.

### Setup

Requires Node/npm. Install the tooling once per clone:

```
npm install
```

### When it runs

Every `git commit` runs it via `.githooks/pre-commit`, but only against the `.feature` files
you've actually staged - untouched files aren't checked until you edit them. Pull requests to
`main` run the `Feature format check` GitHub Actions job against the full feature suite. Configure
that job as a required status check in the `main` branch protection rule to block unformatted PRs.

### Running it manually

```
npm run feature:check          # reports files that don't match project formatting
npm run feature:format         # reformats files in place
npm run feature:formatter:test # tests the project-specific formatting rule
```

## Kavach — automated failure diagnosis and repair

Kavach (`.claude/agents/kavach-diagnose.md`, `.claude/agents/kavach-repair.md`, `.github/workflows/kavach.yml`) may create branches, commit to them, push, and open pull requests against `main` — it never pushes to `main` directly, never merges its own PRs, and never bypasses required reviewers or status checks. That boundary is enforced by this repository's own configuration, not by Kavach's good behavior alone, so both of the following must actually be turned on for `main` under **Settings > Branches**:

- **Require a pull request before merging**, with at least one required approving review.
- **Require review from Code Owners** — `.github/CODEOWNERS` names the owners for `.claude/**` and `kavach-data/**`; without this setting enabled, that file is only documentation, not an enforced gate.

`kavach.yml`'s PR-opening job authenticates as the default `GITHUB_TOKEN` (never an elevated PAT) and only runs `gh pr create`, so there is no code path in this repository that could merge or bypass review even with these settings off — but a human-reviewed PR is the intended approval boundary for everything Kavach changes, and that boundary only exists once these settings are confirmed.

## References

For additional details on Approach, Roadmap, Documentation etc. refer to
https://docs.google.com/spreadsheets/d/1_gUs2ZiAKbCh49CFMMxLETey8sO9Ug7VkQhyNF0tZZo/edit?usp=drive_link
