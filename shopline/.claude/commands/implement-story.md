Implement the following user story end-to-end by running three agents in sequence: UX design, coding, and UI testing.

**User Story:**
$ARGUMENTS

---

## Pipeline

You must execute the following three stages **in order**. Do not proceed to the next stage until the current one completes.

---

### Stage 1 — UX Design

Use the Agent tool to invoke the `ux-design-planner` sub-agent with the user story above. Pass the full user story text as the prompt. Wait for the agent to return a complete UX design specification (layouts, components, interaction flows, developer checklist).

Capture the full design spec output — it is the input for Stage 2.

---

### Stage 2 — Implementation

Use the Agent tool to invoke a `general-purpose` sub-agent to implement the feature. Pass:
- The original user story
- The complete UX design specification produced in Stage 1

Instruct the coding agent to:
1. Read existing project files to understand the codebase before writing any code
2. Implement the feature exactly as specified in the UX design spec
3. Follow existing code style and conventions
4. Return a summary of every file created or modified

Capture the implementation summary — it is the input for Stage 3.

---

### Stage 3 — UI Testing

Use the Agent tool to invoke the `playwright-feature-verifier` sub-agent. Pass:
- The original user story
- A description of what was implemented (from Stage 2)
- The key UI elements and interactions to verify (from the UX spec in Stage 1)

The verifier will connect to the running app, exercise the feature, take a screenshot, and save it to `test-output/`.

---

## Final Report

After all three stages complete, output a concise summary:

```
## Implementation Complete

**User Story:** <one-line restatement>

**UX Spec:** <2-3 bullet highlights from the design>

**Files Changed:**
- <list from Stage 2>

**Verification:** <pass/fail + screenshot path from Stage 3>
```
