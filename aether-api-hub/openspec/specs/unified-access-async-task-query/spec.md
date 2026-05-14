# unified-access-async-task-query Specification

## Purpose
TBD - created by archiving change add-unified-access-async-task-query. Update Purpose after archive.
## Requirements
### Requirement: Unified Access SHALL expose a governed async task query entry
The system SHALL provide a Unified Access task query entry for a target `apiCode` and caller-provided `taskId` when that target asset declares complete async task query configuration.

#### Scenario: Query task for configured async asset
- **WHEN** a caller sends a task query request for a published non-deleted API asset with complete async task query configuration
- **THEN** Unified Access validates the caller and forwards a task query request to the resolved upstream task query endpoint

#### Scenario: Reject task query for asset without async config
- **WHEN** a caller sends a task query request for an asset that has no async task query configuration
- **THEN** Unified Access returns a platform-side failure and MUST NOT send any task query request upstream

### Requirement: Task query MUST reuse Unified Access pre-forward checks
The system SHALL apply the same Unified Access pre-forward checks to task query requests that it applies to normal access invocations.

#### Scenario: Reject invalid credential before task query
- **WHEN** a task query request carries an invalid API Key
- **THEN** Unified Access rejects the request before resolving or forwarding the upstream task query

#### Scenario: Reject missing subscription before task query
- **WHEN** a task query request has a valid API Key but the caller is neither the asset owner nor actively subscribed to the target API
- **THEN** Unified Access returns a subscription entitlement failure and MUST NOT send any task query request upstream

#### Scenario: Use platform proxy routing for task query
- **WHEN** a task query request targets an asset bound to an enabled platform proxy profile
- **THEN** Unified Access executes the upstream task query through the configured platform proxy route

### Requirement: Task query URL MUST be rendered from the configured template and task id
The system SHALL render the upstream task query URL from the asset's task query URL template and the caller-provided `taskId`.

#### Scenario: Render task id placeholder
- **WHEN** the task query URL template is `https://provider.example.com/tasks/{taskId}` and the caller provides task id `task_123`
- **THEN** Unified Access forwards the task query to `https://provider.example.com/tasks/task_123`

#### Scenario: Reject blank task id
- **WHEN** the caller omits `taskId` or provides a blank task id
- **THEN** Unified Access returns a platform-side validation failure and MUST NOT send any task query request upstream

### Requirement: Successful task query responses MUST preserve upstream passthrough semantics
The system SHALL preserve successful upstream task query response semantics and MUST NOT wrap successful task query responses in TML-SDK `Result`.

#### Scenario: Passthrough successful task query response
- **WHEN** the upstream task query succeeds with a success HTTP status, response headers, and response body
- **THEN** Unified Access returns the upstream status/body semantics to the caller without TML-SDK `Result` wrapping

### Requirement: Task query execution failures MUST use Unified Access failure classification
The system SHALL classify task query execution failures using the same Unified Access execution failure families as normal upstream forwarding.

#### Scenario: Upstream task query timeout
- **WHEN** the upstream task query times out after target resolution and forwarding begins
- **THEN** Unified Access returns the upstream timeout failure family documented by the Unified Access API contract

#### Scenario: Malformed rendered task query URL
- **WHEN** the task query request reaches the downstream forwarding stage and the rendered upstream URL cannot be used to construct a valid HTTP request
- **THEN** Unified Access returns an upstream execution failure rather than a target-not-found or invalid-api-code failure

### Requirement: Unified Access API contract MUST document async task query
The system SHALL document the async task query entry in the top-level Unified Access API authority document mapped to `UnifiedAccessController.java`.

#### Scenario: Generate Unified Access authority doc
- **WHEN** the Unified Access API authority document is generated or updated for this change
- **THEN** it uses `docs/api/unified-access.yaml` mapped one-to-one to `UnifiedAccessController.java`
- **AND** it documents the async task query path, parameters, passthrough success behavior, and platform/execution failure responses

