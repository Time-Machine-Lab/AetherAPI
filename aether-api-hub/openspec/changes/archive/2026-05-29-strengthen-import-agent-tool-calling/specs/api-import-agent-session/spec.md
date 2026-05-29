## MODIFIED Requirements

### Requirement: Planner tool calling MUST constrain structured import-plan submissions before a draft plan is persisted
The system MUST treat tool-calling planner output as a constrained structured contract, not just a loosely structured text wrapper. When tool calling is enabled for Import Agent planning, the planner layer MUST enforce declared enums, required fields, conditional field rules, and undeclared-field rejection before a draft plan is accepted as the current session plan.

#### Scenario: Reject a publishable asset plan with incomplete auth fields
- **WHEN** tool-calling planning returns a publishable asset with `authScheme` of `HEADER_TOKEN` or `QUERY_TOKEN` but without `authConfig`
- **THEN** the system MUST keep the plan non-executable and recover the missing slot or emit a targeted clarification instead of accepting the asset as a complete publishable draft

#### Scenario: Reject undeclared planner fields
- **WHEN** tool-calling planning returns fields outside the declared planner tool schema
- **THEN** the system MUST ignore or reject those undeclared fields instead of treating them as valid import-plan state

#### Scenario: Require publishable asset routing and upstream fields
- **WHEN** tool-calling planning submits an asset with `publishAfterImport = true`
- **THEN** the planner contract MUST require `categoryCode`, `requestMethod`, `upstreamUrl`, and `authScheme` before the asset can be treated as executable

#### Scenario: Require async query fields for enabled async-task planning
- **WHEN** tool-calling planning submits `asyncTaskConfig.enabled = true`
- **THEN** the planner contract MUST require `queryMethod`, `queryUrlTemplate`, and `authMode`, and the query URL template MUST retain the `{taskId}` placeholder

### Requirement: Planner MUST auto-fill missing slots from current session evidence before asking the user again
The system MUST attempt deterministic slot filling from `documentSummary`, `currentPlan`, and recent turns before exposing clarification questions to the current authenticated user.

#### Scenario: Apply a direct auth answer to the current asset plan
- **WHEN** the current session already contains an asset with a known `authScheme` and the latest user turn provides the missing header or query token string
- **THEN** the system MUST write that answer back into the matching asset plan and return the refreshed draft plan instead of repeating the same clarification

#### Scenario: Recover async query fields from the document summary
- **WHEN** the document summary already contains a task-query URL pattern or task-result endpoint that matches the planned submit asset
- **THEN** the system MUST fill the missing `asyncTaskConfig` fields before emitting a clarification question for that same slot

#### Scenario: Preserve existing current-plan fields during partial updates
- **WHEN** the planner receives a partial tool-calling patch that omits fields already present in the current plan
- **THEN** the system MUST preserve the existing values instead of clearing them from the refreshed draft plan

### Requirement: Planner output MUST follow the existing asset-management field conventions already used by batch import workflows
The system MUST normalize Import Agent planner output so it matches the field names and value formats already accepted by the backend asset-management flow and by `.codex/skills/batch-import-api-skill/SKILL.md`.

#### Scenario: Keep authConfig in backend-consumable string format
- **WHEN** the planner prepares an asset that uses upstream authentication
- **THEN** the resulting draft plan MUST store `authConfig` as the plain string format already accepted by the backend, instead of introducing custom JSON auth structures

#### Scenario: Fold async query endpoints into asyncTaskConfig
- **WHEN** the planner identifies a submit-and-query asynchronous upstream API pattern
- **THEN** the resulting draft plan MUST fold the query endpoint into the submit asset's `asyncTaskConfig` instead of defaulting to a separate publishable asset plan

#### Scenario: Use existing JSON schema and AI profile field names
- **WHEN** the planner extracts request schema, response schema, or AI capability metadata from the source material
- **THEN** the resulting draft plan MUST populate `requestJsonSchema`, `responseJsonSchema`, and `aiProfile` using the existing backend contract field names instead of introducing alias fields

#### Scenario: Reject unsupported auth JSON structures
- **WHEN** the planner attempts to describe upstream auth using nested JSON helper fields such as `secretRef`, `headerName`, or `queryParamName`
- **THEN** the resulting draft plan MUST reject or normalize those structures before persistence so `authConfig` remains in the backend-consumable plain-string format