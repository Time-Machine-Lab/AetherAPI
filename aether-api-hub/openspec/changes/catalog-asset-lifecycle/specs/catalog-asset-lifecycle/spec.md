## ADDED Requirements

### Requirement: API assets can be registered as drafts with stable API codes
The system SHALL allow internal maintainers to register a new API asset with a globally unique and stable `API Code`, and the newly created asset SHALL enter draft status by default.

#### Scenario: Register a new asset
- **WHEN** an internal maintainer registers a new API asset with a new `API Code`
- **THEN** the system creates the asset in draft status

#### Scenario: Reject duplicate API code
- **WHEN** an internal maintainer registers a new API asset with an `API Code` that already exists
- **THEN** the system rejects the registration request

#### Scenario: Keep API code immutable after registration
- **WHEN** an internal maintainer revises an existing API asset
- **THEN** the system keeps the original `API Code` unchanged

### Requirement: Draft assets can be revised with configuration and examples
The system SHALL allow internal maintainers to revise draft API assets with base metadata, category reference, upstream endpoint configuration, authentication scheme, request template description, and example snapshots without forcing immediate activation.

#### Scenario: Save an incomplete draft
- **WHEN** an internal maintainer saves a draft API asset without all activation-required fields
- **THEN** the system persists the draft and keeps it unavailable for activation

#### Scenario: Example snapshots are optional for draft revision
- **WHEN** an internal maintainer revises a draft API asset without request or response example snapshots
- **THEN** the system accepts the draft revision

### Requirement: Activation SHALL enforce completeness and category validity
The system SHALL only enable an API asset after validating that the required base information, category reference, upstream endpoint configuration, and authentication scheme are complete and that the referenced category is valid for new asset assignment.

#### Scenario: Reject activation with missing required configuration
- **WHEN** an internal maintainer enables an API asset that is missing any required activation field
- **THEN** the system rejects the activation request

#### Scenario: Reject activation with an invalid category
- **WHEN** an internal maintainer enables an API asset whose category is missing or disabled
- **THEN** the system rejects the activation request

#### Scenario: Disable an enabled asset
- **WHEN** an internal maintainer disables an enabled API asset
- **THEN** the system changes the asset status to disabled

### Requirement: AI assets SHALL carry active AI capability profiles
The system SHALL require an `AiCapabilityProfile` for assets whose type is `AI_API`, and SHALL prevent non-AI assets from being treated as active AI capability assets.

#### Scenario: Reject AI asset activation without capability profile
- **WHEN** an internal maintainer enables an API asset whose type is `AI_API` and no AI capability profile is attached
- **THEN** the system rejects the activation request

#### Scenario: Attach AI capability profile to an AI asset
- **WHEN** an internal maintainer attaches provider, model, streaming capability, and capability tags to an `AI_API` asset
- **THEN** the system stores the AI capability profile for that asset

#### Scenario: Keep non-AI assets free of active AI metadata
- **WHEN** an internal maintainer revises an asset whose type is not `AI_API`
- **THEN** the system keeps AI capability information empty or inactive for that asset

### Requirement: Critical configuration changes SHALL trigger revalidation
The system SHALL require a previously enabled API asset to pass activation validation again after changes to critical access configuration such as upstream endpoint, authentication scheme, or request method.

#### Scenario: Revalidate after upstream change
- **WHEN** an internal maintainer changes the upstream endpoint of an enabled API asset
- **THEN** the system requires the asset to pass activation validation again before it is treated as enabled

#### Scenario: Revalidate after authentication change
- **WHEN** an internal maintainer changes the authentication scheme of an enabled API asset
- **THEN** the system requires the asset to pass activation validation again before it is treated as enabled
