---
name: risk-assessment
description: Assess QA readiness, dependencies, and evidence-based production risk for Jira work items. Use when Netra or another QA workflow needs consistent readiness flags and risk ratings from ticket context and real production issues.
---

# QA Risk Assessment

Assess readiness and production risk independently from test-case count. Return only conclusions supported by supplied sources.

## Readiness flags

Apply these flags exactly:

| Flag | Level | Condition |
| --- | --- | --- |
| `MISSING_COMPONENTS` | blocker | No component identifies the area under test. |
| `MISSING_ASSIGNEE` | warning | The ticket has no assignee. |
| `UNEXPECTED_STATE` | warning | Status and resolution are inconsistent with the requested scope. |
| `UNREADY_DPD_DEPENDENCY` | warning | A linked `DPD-` issue remains open or active. |
| `NO_LINKED_CONTEXT` | warning | Description, links, and parent context provide no substantive behavior. |

Set the verdict to `blocked` when any blocker flag exists, `warning` when only warning flags exist, and `ready` otherwise.

Missing acceptance criteria alone is not a flag and never prevents test design when behavior can be inferred from other sources.

## Dependencies

- Record hard and soft dependencies separately through `blocking`.
- Preserve the dependency's real status and source reference.
- Include open subtasks, relevant linked issues, cross-system prerequisites, and navigation framework gaps.
- Do not convert an informational dependency into a blocker without evidence that testing cannot proceed.

## Production risk

Use only real issues returned from the configured production project.

Assess relevance from shared components, labels, affected workflow, parent or epic context, failure mode, and recency. Do not use keyword overlap alone.

Assign:

- `HIGH`: a strongly related unresolved or recurring issue threatens a critical flow, data integrity, permission boundary, or release viability.
- `ELEVATED`: one or more clearly related issues show meaningful regression likelihood but do not establish a current critical failure.
- `MEDIUM`: the issue is relevant historical evidence with limited current impact or an indirect relationship.
- `LOW`: the issue is weakly related but still useful as a regression reference.

For each retained risk, provide the issue key, rating, concise reason, and source reference. An empty result is valid; never manufacture historical risk to populate the report.
