## MODIFIED Requirements

### Requirement: Planner MUST auto-fill missing slots from current session evidence before asking the user again
The system MUST attempt deterministic slot filling from `documentSummary`, `currentPlan`, and recent turns before exposing clarification questions to the current authenticated user. The planner MAY internally orchestrate multiple specialized subagents for fact extraction, auth recognition, async-pattern recognition, review, and clarification strategy, but it MUST still return one unified draft plan for the current session.

#### Scenario: Internal subagents contribute to one unified draft plan
- **WHEN** the planner uses internal subagents to analyze the current document, conversation turns, or current plan
- **THEN** it MUST merge those intermediate results inside the planner and return one owner-scoped draft plan instead of exposing multiple partial plans to the application layer

#### Scenario: Apply a direct auth answer to the current asset plan
- **WHEN** the current session already contains an asset with a known `authScheme` and the latest user turn provides the missing header or query token string
- **THEN** the system MUST write that answer back into the matching asset plan and return the refreshed draft plan instead of repeating the same clarification

#### Scenario: Recover async query fields from the document summary
- **WHEN** the document summary already contains a task-query URL pattern or task-result endpoint that matches the planned submit asset
- **THEN** the system MUST fill the missing `asyncTaskConfig` fields before emitting a clarification question for that same slot

#### Scenario: Preserve existing current-plan fields during partial updates
- **WHEN** the planner receives a partial tool-calling patch that omits fields already present in the current plan
- **THEN** the system MUST preserve the existing values instead of clearing them from the refreshed draft plan

### Requirement: Planner MUST review internal subagent outputs before accepting a draft plan
When internal planner subagents are used, the system MUST review and reconcile their outputs before accepting a refreshed draft plan as the current session plan.

#### Scenario: Downgrade conflicting subagent outputs into clarification or omission
- **WHEN** two internal planner subagents produce conflicting values for the same high-impact field such as `authScheme`, `authConfig`, `upstreamUrl`, or `asyncTaskConfig.queryUrlTemplate`
- **THEN** the system MUST avoid silently persisting the conflict into the final draft plan and MUST instead keep the safer existing value, drop the low-confidence field, or emit targeted clarification

#### Scenario: Keep low-confidence inferred fields out of the executable draft plan
- **WHEN** an internal planner subagent infers a field without enough evidence from the document, current plan, or recent turns
- **THEN** the system MUST keep that field out of the executable draft plan until a later deterministic recovery step or user clarification confirms it

#### Scenario: Continue returning one planner result when a non-critical subagent fails
- **WHEN** an internal fact-extraction or review subagent fails to produce usable output but the remaining planner context is still sufficient to produce a safe non-final plan
- **THEN** the system MUST continue returning one unified planner result, preferring clarification over failing the whole session-planning request