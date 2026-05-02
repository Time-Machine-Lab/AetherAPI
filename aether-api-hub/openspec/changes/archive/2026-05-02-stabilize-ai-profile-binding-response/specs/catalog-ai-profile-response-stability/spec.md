## ADDED Requirements

### Requirement: AI profile binding MUST preserve existing asset configuration
The system MUST preserve an owned asset's existing non-AI configuration when binding or updating its AI capability profile.

#### Scenario: Bind AI profile without clearing upstream configuration
- **WHEN** an asset owner binds an AI capability profile to an `AI_API` asset that already has upstream configuration
- **THEN** the asset retains its existing request method, upstream URL, auth scheme, and auth config

#### Scenario: Bind AI profile without clearing examples and template
- **WHEN** an asset owner binds an AI capability profile to an asset that already has request template and example snapshots
- **THEN** the asset retains its existing request template, request example, and response example

### Requirement: AI profile binding response MUST return a complete asset view
The system MUST return a complete asset response after AI profile binding so clients can safely refresh asset edit state from the response.

#### Scenario: Return complete asset after AI profile binding
- **WHEN** AI profile binding succeeds
- **THEN** the response includes the asset's normal configuration fields and the updated AI capability profile

### Requirement: AI profile binding MUST remain owner-scoped
The system MUST enforce current-user ownership for AI profile binding the same way it does for other asset write operations.

#### Scenario: Reject non-owner AI profile binding
- **WHEN** a user attempts to bind an AI profile to another user's asset
- **THEN** the system rejects the request without changing the asset
