# Domain Checklist

Use this while reading a skill under review. It's organized by the disciplines the review draws on. You don't need to run through every line for every skill — use judgment about which sections are relevant to what the skill actually does — but skim all of them at least once so you know what's available.

## QA & test engineering

- Is "success" for this skill objectively defined, or only implied? A skill that will feed a QA decision needs a clear, checkable definition of pass/fail — not just "looks good."
- Does the skill distinguish between things it verifies directly (ran a check, read a file) and things it's inferring or assuming? Conflating the two is how false confidence creeps into a QA pipeline.
- If the skill produces a verdict, score, or gate decision, is the reasoning behind it shown, or is it a black box? A QA gate that can't explain itself can't be trusted or debugged.
- Are severity/priority levels (if any) defined with actual criteria, or left to vibes? Undefined severity scales mean two runs of the same skill can disagree with each other.

## Test automation

- If the skill runs or generates tests, does it handle a test that errors (crashes) differently from one that fails (assertion) differently from one that's flaky (inconsistent across runs)? Treating all three the same is a common and costly bug.
- Does the skill avoid tautological checks — assertions that will pass regardless of whether the thing under test actually works? (e.g., checking a file exists when the real question is whether its contents are correct.)
- Is there guidance on what to do with flaky or non-deterministic results, or does the skill assume everything is deterministic?

## Software engineering / maintainability

- Single-responsibility: does the skill do one coherent thing, or has it accumulated unrelated responsibilities that should be separate skills?
- Duplicated logic: is the same instruction, template, or check repeated in multiple places where it could drift out of sync? Prefer one source of truth referenced from multiple places.
- Are there contradictions between the frontmatter description and the body, or between two sections of the body?
- If the skill bundles scripts, are they used for genuinely deterministic/repetitive work (good use of a script) or did the author write a script for something that requires judgment (bad use — the agent should reason about it, not run a black box)?
- Is the skill's length and structure appropriate? A skill that's too sprawling to read end-to-end reliably will be followed inconsistently; a skill that's too terse to answer basic "what do I do if X" questions will be filled in with guesses.

## Cost & efficiency

- Is the skill's length and structural complexity proportionate to the actual difficulty of the task? A three-step task wrapped in a 400-line skill with several bundled scripts and reference files is a red flag; so is a genuinely hazardous, high-stakes task covered by five terse lines.
- Would a much shorter set of instructions — or no skill at all, just the agent's own judgment — plausibly get comparably reliable results? If the task doesn't have a sharp failure mode that the skill's extra structure is specifically preventing, the extra length is pure overhead: more tokens loaded into context every time it triggers, more tool calls, more latency, and (at scale) real cost.
- Do the bundled `scripts/`, `references/`, or `assets/` actually get used for something a plain instruction couldn't do as reliably (deterministic computation, a lookup table, a template)? Bundling a script or reference file that mostly restates what the agent already knows is padding, not safety.
- If timing/token data from real runs is available (e.g. a benchmark comparing with-skill vs. without-skill), use it directly — a skill that costs several times the tokens of a no-skill baseline for the same outcome is a concrete, quantified finding, not a vague impression. Note the actual numbers in the issue.
- For skills expected to trigger often or automatically (not just on rare explicit request), the cost per invocation compounds — weigh this more heavily than you would for a skill a person invokes by hand a few times a week.
- This cuts both ways: don't penalize complexity that exists because the task is genuinely error-prone without it. A validation step, a disambiguation script, or a longer explanation that prevents a real, recurring mistake is earning its cost. The question is always "does this length/complexity buy proportionate reliability," not "is short automatically better."

## AI-agent design / prompt & instruction engineering

- Is each instruction actionable by an agent with no other context, or does it assume the agent already knows something it hasn't been told (a file location, a term, a prior step)?
- Ambiguity check: for any instruction using a vague qualifier ("appropriately," "as needed," "reasonable," "correctly"), could two competent agents interpret it differently and both think they followed it correctly? If yes, that's under-specified.
- Explanation vs. rigid command: does the skill explain *why* a step matters, or does it just issue a bare imperative? Skills that explain their reasoning generalize better to situations the author didn't anticipate — bare MUST/NEVER lists tend to break on the first case that wasn't foreseen.
- Does the skill tell the agent what to do when something goes wrong (missing file, tool error, ambiguous user request), or does it only describe the happy path?
- Overfitting check: is the skill written generally, or does it bake in assumptions from one specific example that won't hold for other legitimate uses?

## Error handling & edge cases

- What happens on malformed, missing, empty, or unexpected-format input? If the skill is silent on this, that's a gap worth naming.
- Are failure modes surfaced to the user/agent, or silently swallowed? A skill that fails quietly is worse than one that fails loudly, because the former erodes trust invisibly.
- Does the skill say what NOT to do when uncertain (e.g., "don't guess at a verdict — say the input was insufficient") or does it push toward always producing an answer even when it shouldn't?

## Security & safe execution

- Does the skill ever execute untrusted input as code, or fetch and act on external content without treating it as data rather than instructions?
- Does the skill perform destructive or irreversible actions (deleting, overwriting, sending) without a confirmation step or a clear, deliberate design reason not to have one?
- If the skill handles credentials, personal data, or sensitive files, does it avoid leaking them into logs, reports, or unrelated services?

## CI/CD & quality gates

- If this skill is meant to gate something (a merge, a release, a deployment), is the pass/fail condition unambiguous enough to automate, or does it require human judgment that the skill doesn't account for?
- Is the skill's output machine-parseable if something downstream needs to consume it (consistent structure, stable field names), or is it free text that would break a downstream parser on the next run?

## Documentation & discoverability

- Frontmatter `description`: does it clearly state both *what* the skill does and *when* to trigger it? A description that only says what it does (and not when to use it) will under-trigger.
- If the skill references other files (`scripts/`, `references/`, `assets/`), does it say clearly when to open them, or leave the agent to guess?
- Are naming and terminology consistent between the description, the body, and any bundled files?

---

## How to use this while reviewing

Read the skill once straight through for comprehension. Then go through the sections above and ask, for each one, "does anything I just read fail this test?" You don't need to write down that a check passed — only write up what fails, using the issue format in the main `SKILL.md`. This checklist is a lens, not a form to fill out line by line.
