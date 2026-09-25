---
name: test-coverage
description: Convert source-backed functional requirements into traceable QA coverage and detailed test cases. Use when Netra or another QA workflow needs positive, negative, boundary, permission, integration, or regression coverage without cosmetic UI cases.
---

# Functional Test Coverage

Generate test coverage from the complete supplied context. Formal acceptance criteria are useful but not required.

## Requirement inventory

Create one requirement for each discrete functional behavior found in the ticket, comments, parent or epic, linked issues, subtasks, documentation, implementation notes, or named production regression.

A requirement may cover:

- Business or calculation rules.
- Validation, formats, limits, and boundaries.
- Persistence, transformation, and returned data.
- Permissions and role behavior.
- States and transitions.
- Errors and rejected behavior.
- API or cross-system contracts.

Assign stable IDs as `<TICKET>-R01`, `<TICKET>-R02`, and so on. Attach source references and a confidence level to every requirement.

Exclude cosmetic layout, spacing, color, typography, animation, and copy checks. A UI control belongs only when its state demonstrates a functional rule.

## Coverage calculation

For every requirement, create at least one positive and one negative case. Add a distinct case for each applicable dimension not already covered:

- Secondary happy path.
- Input validation.
- Business-rule or calculation correctness.
- Data integrity or persistence.
- Error state.
- Permission or role.
- State transition.
- Boundary or limit.
- Multi-value or batch behavior.
- Integration or cross-system behavior.
- Relevant production regression.
- Explicit out-of-scope guard.

Use this floor, not a target:

```text
minimumTestCases = requirementCount * 2
                 + uncovered applicable dimensions
                 + regression anchors
```

Do not add redundant cases solely to increase the count.

## Test-case rules

- ID: `TC_<TICKET>_NN`, zero-padded.
- Type: `positive`, `negative`, `edge`, `regression`, or `blocked`.
- Requirement IDs: list every requirement exercised by the case.
- Description: state the behavior and expected decision point without implementation details.
- Test data: provide concrete roles, values, formats, records, permissions, or system states.
- Expected result: state a verifiable functional outcome such as persisted data, computed value, state transition, API value, created or rejected record, or exact enforcing validation.
- Actual result and owner: leave empty before execution.
- Status: `not_executed` unless the case is genuinely blocked.
- Comments: cite the requirement source and any regression or dependency anchor.

Use resolved navigation context only to identify the real module or surface. Do not invent a click path.

## Blocked coverage

Use one `blocked` case only when no testable behavior can be derived from any supplied source. State the exact missing information in its comments. Do not use blocked status merely because acceptance criteria are absent.

Before returning coverage, verify that every requirement has positive and negative coverage, every regression anchor has a case, IDs are unique, and the produced count meets the calculated floor.
