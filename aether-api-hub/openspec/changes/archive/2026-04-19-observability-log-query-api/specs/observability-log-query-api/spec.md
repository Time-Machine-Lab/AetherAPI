## ADDED Requirements

### Requirement: Developers MUST be able to list their own API call logs
The system MUST provide a paged API call log list for the current authenticated user so the developer console can display recent invocation history.

#### Scenario: Query the current user's log list
- **WHEN** an authenticated user requests the call log list
- **THEN** the system returns a paged set of that user's call logs only

### Requirement: Developers MUST be able to filter call logs by API and time range
The system MUST support minimal filtering on the current user's call logs by target API identifier and invocation time range.

#### Scenario: Filter logs by target API
- **WHEN** the current user queries call logs with a target API filter
- **THEN** the system returns only logs that match that target API within the user's own scope

#### Scenario: Filter logs by time range
- **WHEN** the current user queries call logs with a start time and end time
- **THEN** the system returns only logs whose invocation time falls within that requested range

### Requirement: Developers MUST be able to view a single call log detail
The system MUST provide a call log detail query for one log record that belongs to the current authenticated user.

#### Scenario: View an owned call log detail
- **WHEN** the current user requests the detail of a call log that belongs to their own scope
- **THEN** the system returns the detail of that single call log record

#### Scenario: Reject access to another user's log
- **WHEN** the current user requests a call log detail outside their own scope
- **THEN** the system rejects the request instead of returning another user's log detail

### Requirement: Call log query APIs MUST map to a single controller contract
The system MUST define phase-one call log query APIs in one top-level API document mapped directly to one controller implementation.

#### Scenario: Generate the call log API authority file
- **WHEN** the project generates or updates the top-level API contract for call log queries
- **THEN** it uses `tml-docs-spec-generate` with the API template to maintain `docs/api/api-call-log.yaml` mapped to `ApiCallLogController.java`

### Requirement: Call log query APIs MUST keep Consumer implicit
The system MUST keep Consumer as an internal identity concept and MUST NOT require front-end clients to register, manage, or explicitly submit Consumer identifiers when querying logs.

#### Scenario: Query logs without explicit Consumer input
- **WHEN** the front end calls the call log query API
- **THEN** the request is scoped by the authenticated user context without exposing explicit Consumer management behavior
