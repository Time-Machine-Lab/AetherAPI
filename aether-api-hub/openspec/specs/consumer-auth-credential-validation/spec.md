# consumer-auth-credential-validation Specification

## Purpose

Define the credential validation behavior used by unified access so the system can resolve a stable Consumer context from API keys, classify validation failures, and maintain credential usage snapshots without exposing Consumer as a user-facing concept.

## Requirements

### Requirement: Unified access validation MUST resolve a Consumer context from an API key
The system MUST allow the unified access layer to submit an API key for validation and MUST return a structured Consumer context when the credential is valid.

#### Scenario: Return context for a valid credential
- **WHEN** unified access submits a valid enabled API key that belongs to an available Consumer
- **THEN** the system returns a successful validation result with the credential identity and Consumer context

#### Scenario: Validation uses internal Consumer mapping
- **WHEN** unified access validates a credential that belongs to the current user's hidden Consumer mapping
- **THEN** the system resolves the internal Consumer without requiring any user-facing Consumer identifier

### Requirement: Validation failures MUST be categorized by business reason
The system MUST distinguish credential validation failures by business reason instead of returning only a boolean result.

#### Scenario: Reject unknown credential
- **WHEN** unified access submits an API key whose fingerprint matches no stored credential
- **THEN** the system returns a failure result categorized as credential not found

#### Scenario: Reject disabled credential
- **WHEN** unified access submits an API key that belongs to a disabled credential
- **THEN** the system returns a failure result categorized as credential disabled

#### Scenario: Reject revoked or expired credential
- **WHEN** unified access submits an API key that belongs to a revoked or expired credential
- **THEN** the system returns a failure result categorized according to the credential lifecycle state

#### Scenario: Reject unavailable Consumer
- **WHEN** unified access submits an API key whose credential is valid but whose Consumer is unavailable
- **THEN** the system returns a failure result categorized as Consumer unavailable

### Requirement: Validation MUST keep business rules out of adapters
The system MUST execute credential matching, lifecycle checks, Consumer availability checks, and context assembly in domain or application services rather than in controllers, filters, or persistence adapters.

#### Scenario: Adapter delegates validation
- **WHEN** an adapter layer receives a credential validation request
- **THEN** it delegates business validation to the application service and does not implement credential lifecycle rules directly

### Requirement: Validation MUST update credential usage snapshots consistently
The system MUST update the matched credential's last-used snapshot as part of the validation flow so later control-plane queries and governance features can reuse the same source of truth.

#### Scenario: Successful validation updates usage snapshot
- **WHEN** a credential is validated successfully
- **THEN** the system updates that credential's last-used timestamp and result snapshot

#### Scenario: Failed validation on matched credential updates failure snapshot
- **WHEN** a credential is found but validation fails because of credential or Consumer state
- **THEN** the system updates that credential's last-used snapshot with the failed result

#### Scenario: Unknown credential does not update any snapshot
- **WHEN** validation fails because no credential matches the submitted key
- **THEN** the system does not update a credential usage snapshot
