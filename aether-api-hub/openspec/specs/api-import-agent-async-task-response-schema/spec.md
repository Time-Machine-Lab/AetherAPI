# api-import-agent-async-task-response-schema Specification

## Purpose
TBD - created by archiving change replace-async-task-paths-with-query-response-schema. Update Purpose after archive.
## Requirements
### Requirement: Import Agent MUST plan async task query response schemas
The system SHALL allow Import Agent asset plans to include `asyncTaskConfig.queryResponseJsonSchema` as the optional JSON Schema snapshot for the upstream task-query response body.

#### Scenario: Plan contains task query response schema
- **WHEN** Import Agent can infer the task-query response body shape from provider documentation, examples, existing asset values, or explicit schema hints
- **THEN** the generated asset plan includes the schema in `asyncTaskConfig.queryResponseJsonSchema`
- **AND** the schema value is a valid JSON object string or a JSON object serialized to a valid JSON object string

#### Scenario: Plan omits task path fields
- **WHEN** Import Agent generates or refreshes an asset plan with async task configuration
- **THEN** the plan does not include `statusPath`, `resultPath`, or `errorPath`
- **AND** downstream registration or revision commands do not receive those legacy path fields from the plan

### Requirement: Import Agent MUST ask for task-query response evidence when schema is unclear
The system SHALL keep `asyncTaskConfig.queryResponseJsonSchema` blank when there is insufficient evidence to generate a valid task-query response schema, and MAY ask a Chinese clarification question for the missing business evidence.

#### Scenario: Insufficient task-query evidence leaves schema blank
- **WHEN** the source material identifies an async task query endpoint but does not document or exemplify the task-query response body
- **THEN** Import Agent leaves `asyncTaskConfig.queryResponseJsonSchema` blank
- **AND** the absence of that optional schema does not by itself make the plan non-executable

#### Scenario: Clarification asks for response shape
- **WHEN** Import Agent needs more information to describe the task-query response body
- **THEN** it asks for a task-query response example, field description, or provider documentation
- **AND** it does not ask the user to provide JSONPath-style `statusPath`, `resultPath`, or `errorPath` backend configuration

### Requirement: Import Agent API contract MUST document async task query response schema
The system SHALL document Import Agent plan async task configuration in the top-level Import Agent API authority document mapped to `ApiImportAgentController.java`.

#### Scenario: Generate Import Agent authority doc
- **WHEN** the Import Agent API authority document is generated or updated for this change
- **THEN** it uses `docs/api/api-import-agent.yaml` mapped one-to-one to `ApiImportAgentController.java`
- **AND** it documents `asyncTaskConfig.queryResponseJsonSchema`
- **AND** it does not document `statusPath`, `resultPath`, or `errorPath` as Import Agent plan fields

