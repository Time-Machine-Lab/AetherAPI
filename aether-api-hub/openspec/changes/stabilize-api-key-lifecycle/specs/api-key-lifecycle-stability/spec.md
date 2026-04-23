## ADDED Requirements

### Requirement: Current-user API Key state changes MUST succeed for valid transitions
The system MUST allow the current user to disable, enable, and revoke their own API Keys according to the existing lifecycle contract.

#### Scenario: Disable enabled API Key
- **WHEN** the current user disables an enabled API Key that belongs to them
- **THEN** the system returns the credential with status `DISABLED` and persists the status change

#### Scenario: Revoke enabled API Key
- **WHEN** the current user revokes an enabled API Key that belongs to them
- **THEN** the system returns the credential with status `REVOKED`, persists the status change, and records a revoked timestamp

### Requirement: API Key lifecycle conflicts MUST return business errors
The system MUST classify invalid API Key lifecycle transitions as business errors instead of returning default server error responses.

#### Scenario: Disable already disabled API Key
- **WHEN** the current user disables an API Key that is already disabled
- **THEN** the system returns an API credential lifecycle conflict error

#### Scenario: Revoke already revoked API Key
- **WHEN** the current user revokes an API Key that is already revoked
- **THEN** the system returns an API credential lifecycle conflict error

#### Scenario: Enable revoked API Key
- **WHEN** the current user enables a revoked API Key
- **THEN** the system rejects the operation with a business error

### Requirement: API Key lifecycle changes MUST preserve current-user boundary
The system MUST only allow a current user to operate API Keys associated with that user's hidden Consumer mapping.

#### Scenario: Credential does not belong to current user
- **WHEN** the current user requests a lifecycle action for a credential not owned by their hidden Consumer
- **THEN** the system returns an API credential not found error
