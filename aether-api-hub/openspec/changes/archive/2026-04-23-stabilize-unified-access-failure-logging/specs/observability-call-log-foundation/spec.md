## ADDED Requirements

### Requirement: Valid-credential platform failures MUST be queryable by current user
The system MUST record Unified Access platform failures that happen after successful API Key validation with enough Consumer and credential snapshot data for the current user call-log query API to return them.

#### Scenario: Unknown target failure appears in current-user logs
- **WHEN** a current user creates an API Key and uses that valid key to invoke an unknown target API
- **THEN** the system records a failed call log associated with that user's hidden Consumer and the current-user call-log list can return it

#### Scenario: Failed call log includes platform failure summary
- **WHEN** a valid-key Unified Access invocation fails before forwarding because the target API is missing
- **THEN** the call log stores `TARGET_NOT_FOUND` style result classification, error code, error type, and error summary
