# Agent Review Checklist

Use the applicable sections. Mark unavailable evidence as a review limitation rather than assuming compliance.

## 1. Identity, scope, and discoverability

- Is the definition in the correct platform discovery directory?
- Does the filename follow the expected convention?
- Is the frontmatter or manifest valid and limited to supported fields?
- Is the agent name unique and stable?
- Does the description say when this agent should and should not run?
- Are scope boundaries and non-goals explicit?
- Is the agent clearly distinct from neighboring agents?
- Is any prose-based discovery workaround masking an invalid folder or filename?

## 2. Agent-versus-skill separation

- Does the agent retain identity, model, tools, permissions, inputs, outputs, authority, and handoffs?
- Are reusable procedures located in canonical `SKILL.md` files?
- Was every referenced skill reviewed using the separate installed `skill-reviewer` skill?
- Are the skill-reviewer verdicts and blocking findings reflected in the agent-level verdict?
- Are instructions duplicated between the agent and any referenced skill?
- Does the agent reference skills by a stable, discoverable name?
- Would any supposed agent be more accurately represented as a skill?
- Would any overloaded agent benefit from separation because it needs a different repository, permission set, model, or execution context?

## 3. Models and context

- Is the selected model supported in every intended runtime?
- Is the model choice proportional to task complexity and risk?
- Are context-heavy sources loaded only when needed?
- Does the agent avoid repeating upstream analysis already present in an artifact?
- Are token, latency, and cost risks acknowledged for large tickets, logs, or repositories?
- Is there a bounded strategy for long-running or iterative work?

## 4. Skills and local dependencies

- Does every declared skill directory exist?
- Is each entry file canonically named `SKILL.md`?
- Are relative links resolvable from the defining file?
- Do referenced scripts and schemas exist and have clear ownership?
- Are generated artifacts kept separate from source definitions where appropriate?
- Are absolute developer-specific paths avoided?
- Are required executables and versions documented or validated?

## 5. Tools, MCP, and external systems

- Does every referenced tool or MCP server exist in the intended environment?
- Does MCP configuration contain commands and placeholders rather than committed secrets?
- Is authentication supplied through environment variables, OAuth, workload identity, or an approved secret manager?
- Are local, CI, and managed-runtime authentication differences explicit?
- Are first-run trust approval and OAuth requirements documented when applicable?
- Are tool permissions limited to what the role needs?
- Does the agent handle an unavailable or denied connector cleanly?
- Is external content treated as untrusted data rather than governing instruction?
- Does the agent avoid logging credentials, tokens, cookies, or sensitive payloads?

## 6. Input contract

- Is the upstream producer named?
- Are required and optional fields distinguished?
- Is the schema version explicit?
- Are source ticket, run, commit, environment, and artifact identifiers preserved as needed?
- Are freshness and completeness requirements defined?
- Does the agent validate input before using tools or changing state?
- Is behavior defined for missing, malformed, contradictory, or stale input?

## 7. Output contract

- Is the downstream consumer named?
- Is the output format or schema explicit and machine-validatable?
- Are status, confidence, evidence, assumptions, and unresolved questions represented separately?
- Are classification values enumerated consistently?
- Is there a deterministic artifact name or location when required?
- Does the agent validate output before handoff?
- Can downstream stages consume the artifact without repeating the full upstream task?

## 8. Orchestration and handoffs

- Are entry conditions and completion conditions explicit?
- Is responsibility for choosing the next agent unambiguous?
- Does each handoff name the artifact, owner, and validation gate?
- Are stop, retry, skip, escalation, and human-input conditions defined?
- Are loops bounded and protected from duplicate actions?
- Is idempotency addressed for reruns?
- Can an observer derive status without mutating the workflow?
- Are parallel activities safe when they touch the same files or tickets?

## 9. Decision logic and evidence

- Are decisions based on observable evidence rather than unsupported inference?
- Are confidence and uncertainty represented honestly?
- Are competing classifications resolved by explicit criteria?
- Are source references retained so a reviewer can reproduce the decision?
- Does the agent distinguish relevant failure classes instead of collapsing them?
- Is the agent prevented from turning every test failure into a defect?
- Are recommendations separated from facts?

## 10. Permissions and consequential actions

- Does the agent have the minimum repository, Jira, browser, Slack, and test-management access it needs?
- Are read and write capabilities distinguished?
- Are branch, pull-request, merge, ticket, notification, and destructive-operation permissions explicit?
- Are approval rules consistent across the agent, skills, settings, and orchestrator?
- Are target repositories, projects, and channels scoped narrowly?
- Can the agent recover safely from a partially completed external action?
- Is there an audit trail for consequential changes?

## 11. Security and privacy

- Are API keys, tokens, passwords, private keys, cookies, and connection strings absent from committed files?
- Are secret variable names documented without exposing values?
- Are secrets separated by user or environment where feasible?
- Is sensitive ticket, log, or customer data minimized in prompts and artifacts?
- Are secret rotation and revocation paths understood?
- Could prompt injection from tickets, logs, web pages, or repository content alter permissions or policy?
- Does any report redact discovered secrets?

## 12. Failure handling and resilience

- Are tool errors, timeouts, rate limits, authentication failures, and partial responses handled?
- Are retries bounded and limited to retryable failures?
- Is partial progress recorded without falsely reporting completion?
- Are temporary and persistent failures distinguished?
- Are cleanup and rollback behaviors safe?
- Does the agent stop when required evidence is unavailable?
- Can a rerun avoid duplicate Jira issues, pull requests, comments, or notifications?

## 13. Testability and observability

- Is there at least one realistic example invocation?
- Are happy-path and failure-path fixtures or scenarios available?
- Can contracts be validated independently of the model?
- Are meaningful lifecycle events and tool outcomes recorded?
- Can reviewers trace the model, prompt version, skill version, input artifact, and repository revision that produced an output?
- Are logs useful without exposing sensitive data?
- Is there a measurable acceptance criterion for agent quality?

## 14. Portability and source of truth

- Is Git the authoritative source for definitions when that is the stated architecture?
- Can local Claude Code or Desktop Code discover the same project files after clone or pull?
- Are CI-only credentials and local interactive credentials clearly separated?
- Are runtime-specific configuration layers documented without duplicating agent logic?
- Is external-console configuration synchronized or intentionally limited to runtime concerns?
- Is drift detectable between the repository and any deployed copy?

## 15. Review scenarios

Run at least:

1. A complete valid input through the normal workflow.
2. A missing dependency, malformed input, or denied tool permission.
3. A consequential-action case when the agent can write to an external system.

For each scenario, record:

- Inputs and preconditions.
- Expected tool and skill use.
- Expected decisions and stopping behavior.
- Expected output artifact and validation.
- Actual ambiguity or failure risk found in the definition.
