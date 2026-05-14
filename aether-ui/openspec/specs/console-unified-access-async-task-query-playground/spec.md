# console-unified-access-async-task-query-playground Specification

## Purpose
TBD - created by archiving change add-console-async-task-query-playground. Update Purpose after archive.
## Requirements
### Requirement: Manual task query from Playground

The Unified Access Playground SHALL allow users to manually query an async task by entering a task id while using the current API code and API Key.

#### Scenario: Query task with required inputs

- **WHEN** a user enters API code, API Key, and task id, then clicks the task query action
- **THEN** the frontend MUST call the Unified Access task query API layer with those values
- **AND** the latest response panel MUST show the returned Unified Access result

#### Scenario: Missing task id disables query

- **WHEN** the task id is empty
- **THEN** the task query action MUST be disabled

#### Scenario: Task query result preserves passthrough rendering

- **WHEN** the task query returns JSON, text, event-stream, binary, or a platform failure
- **THEN** the Playground MUST render it using the same response panel semantics as normal invocation

#### Scenario: Task query failures have guidance

- **WHEN** the task query returns `ASYNC_TASK_QUERY_UNAVAILABLE` or `INVALID_TASK_ID`
- **THEN** the Playground MUST show localized guidance instead of an untranslated failure key

