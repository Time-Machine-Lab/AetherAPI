## MODIFIED Requirements

### Requirement: Planner draft persistence MUST require constrained structured values for execution-critical fields
The system MUST treat execution-critical Import Agent planner fields as a constrained structured contract. Before a draft plan is accepted as executable session state, execution-critical fields MUST come from declared planner fields or deterministic current-plan merge, and MUST satisfy enum, required, and conditional rules.

#### Scenario: Keep a publishable asset non-executable when required auth fields are omitted
- **WHEN** planner output marks an asset as `publishAfterImport = true` and sets `authScheme` to `HEADER_TOKEN` or `QUERY_TOKEN` without a corresponding `authConfig`
- **THEN** the system MUST keep the draft plan non-executable and emit a targeted clarification instead of promoting the asset into executable state

#### Scenario: Keep async query planning non-executable when required query fields are omitted
- **WHEN** planner output sets `asyncTaskConfig.enabled = true` but omits `queryMethod`, omits `queryUrlTemplate`, or provides a query URL template without the `{taskId}` placeholder
- **THEN** the system MUST keep the draft plan non-executable and emit a targeted clarification instead of synthesizing a runnable async query configuration

#### Scenario: Reject or ignore undeclared execution-critical fields
- **WHEN** planner output includes undeclared execution-critical fields or writes a legal enum value into the wrong field, such as `asyncTaskConfig.authMode = HEADER_TOKEN`
- **THEN** the system MUST reject or normalize that payload without treating it as a valid executable plan state

### Requirement: Planner MUST not infer execution-critical fields from free-text evidence alone
The system MUST not turn free-text session evidence into executable Import Agent planner state for execution-critical fields. Free-text evidence may support clarification or compatibility parsing, but it MUST not be the sole source of truth for executable auth, routing, or async-task fields.

#### Scenario: Do not synthesize authConfig from free text when structured planner fields are still missing
- **WHEN** the document summary or the latest user turn contains a header token string or query token string, but planner output still omits the matching structured `authScheme` or `authConfig`
- **THEN** the system MUST keep the draft plan non-executable and ask for the missing structured field instead of silently synthesizing a publishable auth configuration

#### Scenario: Do not synthesize async query routing from free text when structured planner fields are still missing
- **WHEN** the document summary or recent turns mention a task query URL pattern, but planner output still omits `asyncTaskConfig.queryMethod` or `asyncTaskConfig.queryUrlTemplate`
- **THEN** the system MUST keep the draft plan non-executable and ask for the missing structured field instead of silently synthesizing an executable async-task plan

### Requirement: Partial structured planner updates MUST preserve current-plan fields without reinterpreting unrelated prose
The system MUST preserve already-known current-plan values during partial planner updates, but MUST only merge declared structured fields or deterministic existing values instead of reinterpreting unrelated prose to refill omitted execution-critical fields.

#### Scenario: Preserve existing structured fields during partial draft updates
- **WHEN** the planner returns a partial structured patch that omits execution-critical fields already present in the current plan
- **THEN** the system MUST preserve those current-plan values instead of clearing them or re-deriving them from unrelated free-text evidence

#### Scenario: Ignore unrelated prose while applying a partial structured patch
- **WHEN** the planner returns a partial structured patch and the surrounding session text contains unrelated provider descriptions, example requests, or auth prose
- **THEN** the system MUST apply only the declared structured patch plus deterministic existing values, instead of using that unrelated prose to synthesize additional executable fields