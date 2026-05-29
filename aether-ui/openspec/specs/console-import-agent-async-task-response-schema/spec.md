# console-import-agent-async-task-response-schema Specification

## Purpose
TBD - created by archiving change replace-console-async-task-paths-with-query-response-schema. Update Purpose after archive.
## Requirements
### Requirement: Import Agent plan displays async task query response schema

The Import Agent workspace SHALL render async task plan metadata using `asyncTaskConfig.queryResponseJsonSchema` as the optional task query response schema snapshot.

#### Scenario: Plan contains task query response schema

- **WHEN** an Import Agent asset plan contains enabled `asyncTaskConfig` with `queryResponseJsonSchema`
- **THEN** the plan card MUST display the task query response schema in a readable code or schema display surface
- **AND** the plan card MUST continue to show query method, query URL template, auth mode, auth scheme, and non-private auth config according to existing visibility rules

#### Scenario: Plan omits task query response schema

- **WHEN** an Import Agent asset plan contains enabled `asyncTaskConfig` without `queryResponseJsonSchema`
- **THEN** the plan card MUST still display the async task forwarding metadata
- **AND** the absence of the optional schema MUST NOT make the plan card appear invalid by itself

#### Scenario: Plan does not show legacy path fields

- **WHEN** an Import Agent asset plan is rendered
- **THEN** the plan card MUST NOT display `statusPath`, `resultPath`, or `errorPath`
- **AND** the frontend Import Agent DTO/type mapping MUST NOT expose those fields as supported plan metadata

