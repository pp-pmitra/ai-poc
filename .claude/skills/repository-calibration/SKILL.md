---
name: repository-calibration
description: Calibrate Sutra test design against this repository's feature organization, step vocabulary, page objects, duplicate coverage, and append-versus-create conventions. Use before authoring or placing Gherkin.
---

# Repository Calibration

Inspect the checked-out repository before drafting Gherkin. Repository evidence overrides generic BDD phrasing.

## Search boundary

Read recursively across:

- `src/test/resources/features/`
- `src/test/java/stepdefinitions/`
- `src/main/java/pages/`
- relevant utilities under `src/main/java/utils/`

Do not search only the feature root; feature files live in module subdirectories.

## Calibration procedure

1. Identify the ticket's module and parent functional domain from Netra's analysis.
2. Search existing feature files recursively for the domain, requirement concepts, ticket key, and `# Source:` references.
3. Check the current branch and available local refs for an existing change covering the same ticket. Do not make network calls unless the selected delivery mode explicitly requires remote Git operations.
4. Compare behavior, not only ticket IDs. Classify overlap as:
   - `duplicate`: existing scenarios already cover the behavior;
   - `partial`: author only uncovered requirements;
   - `new`: no matching behavior exists.
5. Choose an existing parent-domain feature file whenever one exists. Create a new file only when no parent-domain file is suitable.
6. New target paths must be `src/test/resources/features/<module>/<Domain>_<Module>.feature`. Never create a ticket-named feature or write directly under the feature root.
7. Read the complete target feature. Preserve its `Feature:` header, description, `Background:`, tags, scenarios, and byte content.
8. Extract the target file's dominant step phrasing. If creating a file, sample at least eight existing steps per keyword across the module when available.
9. Inventory reusable `@Given`, `@When`, and `@Then` annotations and their Java methods. Inspect page-object methods supporting those steps.

## Calibration result

Return for each ticket:

- target repository path;
- action: `append`, `create`, `duplicate`, or `blocked`;
- overlap evidence;
- keyword phrasing profile;
- reusable step definitions;
- missing step definitions or page-object methods;
- framework readiness: `ready` or `gap`.

## Diff safety

When appending, create a staged copy and append after exactly two newline characters. Do not reformat or rewrite existing content. Before handoff, compare the staged copy with the source and confirm all pre-existing lines remain unchanged.

If existing content was deleted or modified, stop and restore the staged file before continuing. Do not hide the failure by producing a new file.
