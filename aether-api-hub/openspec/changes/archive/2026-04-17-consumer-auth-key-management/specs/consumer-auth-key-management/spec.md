## ADDED Requirements

### Requirement: Current user credential issuance MUST implicitly ensure a Consumer
The system MUST allow the current logged-in user to create an API key without first registering a visible Consumer, and the issuance flow MUST ensure that exactly one internal Consumer mapping exists for that user.

#### Scenario: First key creation auto-creates internal Consumer
- **WHEN** the current user creates the first API key and no Consumer mapping exists yet
- **THEN** the system creates the internal Consumer mapping before persisting the new credential

#### Scenario: Later key creation reuses existing Consumer
- **WHEN** the current user creates another API key and an internal Consumer mapping already exists
- **THEN** the system reuses the existing Consumer instead of creating a second visible identity concept

### Requirement: Credential management APIs MUST stay scoped to the current user
The system MUST expose API key management behavior through current-user business interfaces and MUST NOT require or expose explicit Consumer identifiers in user-facing requests.

#### Scenario: Create key request does not include Consumer identifier
- **WHEN** the client submits a create-key request
- **THEN** the request contract does not require `consumerId`, `consumerCode`, or equivalent explicit Consumer fields

#### Scenario: List keys only returns current user's credentials
- **WHEN** the client requests the API key list for the current session
- **THEN** the system returns only the credentials that belong to the current user's internal Consumer mapping

### Requirement: Credential queries MUST never expose full plaintext keys after issuance
The system MUST return the plaintext API key only once during successful issuance and MUST return masked key information for all later query operations.

#### Scenario: Issuance returns plaintext once
- **WHEN** a new API key is created successfully
- **THEN** the create response includes the plaintext key exactly once together with masked management information

#### Scenario: List query returns masked keys only
- **WHEN** the client requests the API key list after issuance
- **THEN** the system returns masked keys and management metadata without the original plaintext key

#### Scenario: Detail query returns masked key only
- **WHEN** the client requests a credential detail view after issuance
- **THEN** the system returns the credential with masked key information and does not reveal the original plaintext secret

### Requirement: Credential lifecycle control MUST support multi-key management for one user
The system MUST allow one internal Consumer to hold multiple API keys and MUST support enable, disable, revoke, and expiration-aware management for each credential independently.

#### Scenario: One user owns multiple credentials
- **WHEN** the same current user creates two API keys for different purposes
- **THEN** the system persists both credentials under the same internal Consumer mapping

#### Scenario: Disable one credential without affecting another
- **WHEN** the current user disables one credential while another credential remains enabled
- **THEN** only the targeted credential changes lifecycle state

#### Scenario: Revoke credential permanently
- **WHEN** the current user revokes an existing credential
- **THEN** the credential becomes unavailable for future use and is no longer treated as an active key

#### Scenario: Expired credential is still visible as expired
- **WHEN** a credential reaches its configured expiration time
- **THEN** the management query shows the credential as expired instead of treating it as an enabled key
