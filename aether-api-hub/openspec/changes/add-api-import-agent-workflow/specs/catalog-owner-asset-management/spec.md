## MODIFIED Requirements

### Requirement: Current authenticated users MUST create owned API assets
The system MUST allow the current authenticated console user to create an API asset that is owned by that user and starts in draft state. When a backend import execution creates assets on behalf of the current authenticated user, it MUST preserve the same ownership and draft-first semantics instead of bypassing them.

#### Scenario: Create an owned draft asset
- **WHEN** the current authenticated user submits a valid create-asset request
- **THEN** the system creates a new draft API asset owned by that user

#### Scenario: Reject duplicate API code across the marketplace
- **WHEN** the current authenticated user submits a create-asset request whose `apiCode` already exists for another asset
- **THEN** the system rejects the request instead of creating a second asset with the same code

#### Scenario: Import execution creates an owned draft asset
- **WHEN** a confirmed import execution creates a new asset for the current authenticated user
- **THEN** the system creates that asset as owned by the current authenticated user and applies the same draft-first ownership rules as the direct asset management flow

### Requirement: Owners MUST maintain configuration only for their own assets
The system MUST allow an asset owner to revise the configuration of their own asset, including AI capability metadata when applicable, and MUST keep ownership checks outside controllers and persistence adapters. Import execution that revises an existing owned asset MUST reuse the same ownership and lifecycle validation instead of applying a parallel write path.

#### Scenario: Owner revises an owned draft asset
- **WHEN** an asset owner updates the configuration of their own draft asset
- **THEN** the system persists the updated asset configuration successfully

#### Scenario: Owner updates AI capability metadata for an owned AI asset
- **WHEN** an asset owner updates AI capability metadata for their own `AI_API` asset
- **THEN** the system stores the AI capability metadata as part of that owned asset

#### Scenario: Non-owner cannot revise asset configuration
- **WHEN** a user attempts to revise an asset owned by another user
- **THEN** the system rejects the request instead of applying the configuration change

#### Scenario: Import execution reuses owner-scoped revision rules
- **WHEN** a confirmed import execution revises an existing asset for the current authenticated user
- **THEN** the system applies the same owner-scoped validation, AI profile rules, and lifecycle checks as the direct owner asset revision flow