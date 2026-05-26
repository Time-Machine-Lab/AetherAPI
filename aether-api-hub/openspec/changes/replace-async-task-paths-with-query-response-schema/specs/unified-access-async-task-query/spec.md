## ADDED Requirements

### Requirement: Task query response schema MUST NOT alter passthrough execution
The system SHALL treat `asyncTaskConfig.queryResponseJsonSchema` as asset contract metadata and MUST NOT use it to validate, extract, wrap, or normalize successful Unified Access task-query responses.

#### Scenario: Passthrough ignores response schema metadata
- **WHEN** a caller queries an async task for an asset whose async task configuration includes `queryResponseJsonSchema`
- **THEN** Unified Access forwards the task query using the configured query method, URL template, and authentication
- **AND** Unified Access returns successful upstream task-query response semantics without validating or transforming the body against the schema

#### Scenario: Missing response schema still allows configured task query
- **WHEN** a caller queries an async task for an asset whose async task configuration is complete for forwarding but has no `queryResponseJsonSchema`
- **THEN** Unified Access still allows task query execution when all existing pre-forward checks pass

## MODIFIED Requirements

### Requirement: Successful task query responses MUST preserve upstream passthrough semantics
The system SHALL preserve successful upstream task query response semantics and MUST NOT wrap successful task query responses in TML-SDK `Result`.

#### Scenario: Passthrough successful task query response
- **WHEN** the upstream task query succeeds with a success HTTP status, response headers, and response body
- **THEN** Unified Access returns the upstream status/body semantics to the caller without TML-SDK `Result` wrapping
- **AND** Unified Access does not derive platform `status`, `result`, or `error` fields from async task configuration metadata
