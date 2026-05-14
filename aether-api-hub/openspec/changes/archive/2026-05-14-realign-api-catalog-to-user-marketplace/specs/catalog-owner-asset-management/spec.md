## ADDED Requirements

### Requirement: Current authenticated users MUST create owned API assets
The system MUST allow the current authenticated console user to create an API asset that is owned by that user and starts in draft state.

#### Scenario: Create an owned draft asset
- **WHEN** the current authenticated user submits a valid create-asset request
- **THEN** the system creates a new draft API asset owned by that user

#### Scenario: Reject duplicate API code across the marketplace
- **WHEN** the current authenticated user submits a create-asset request whose `apiCode` already exists for another asset
- **THEN** the system rejects the request instead of creating a second asset with the same code

### Requirement: Current authenticated users MUST query only their own asset workspace
The system MUST provide owner-scoped asset workspace queries for the current authenticated user and MUST NOT expose another user's write-model asset workspace through those APIs.

#### Scenario: List the current user's assets
- **WHEN** the current authenticated user requests their asset workspace list
- **THEN** the system returns only assets owned by that user

#### Scenario: View detail of an owned asset
- **WHEN** the current authenticated user requests the detail of an asset they own
- **THEN** the system returns that owned asset's workspace detail

#### Scenario: Reject another user's asset workspace access
- **WHEN** the current authenticated user requests asset workspace detail for an asset owned by another user
- **THEN** the system rejects or hides that asset instead of exposing another user's write-model asset data

### Requirement: Owners MUST maintain configuration only for their own assets
The system MUST allow an asset owner to revise the configuration of their own asset, including AI capability metadata when applicable, and MUST keep ownership checks outside controllers and persistence adapters.

#### Scenario: Owner revises an owned draft asset
- **WHEN** an asset owner updates the configuration of their own draft asset
- **THEN** the system persists the updated asset configuration successfully

#### Scenario: Owner updates AI capability metadata for an owned AI asset
- **WHEN** an asset owner updates AI capability metadata for their own `AI_API` asset
- **THEN** the system stores the AI capability metadata as part of that owned asset

#### Scenario: Non-owner cannot revise asset configuration
- **WHEN** a user attempts to revise an asset owned by another user
- **THEN** the system rejects the request instead of applying the configuration change

### Requirement: Owners MUST publish and unpublish their own assets through validation
The system MUST allow an asset owner to publish and unpublish their own assets, and publication MUST succeed only when the asset satisfies the required marketplace completeness rules.

#### Scenario: Publish a complete owned asset
- **WHEN** an asset owner publishes an owned asset whose required configuration is complete
- **THEN** the system marks the asset as published

#### Scenario: Reject publication of an incomplete owned asset
- **WHEN** an asset owner attempts to publish an owned asset whose required configuration is incomplete
- **THEN** the system rejects the publish request

#### Scenario: Critical revision withdraws a previously published asset
- **WHEN** an asset owner changes a published asset's critical upstream configuration
- **THEN** the system transitions that asset out of published state until the owner republishes it

#### Scenario: Owner unpublishes an owned asset
- **WHEN** an asset owner unpublishes their own published asset
- **THEN** the system removes that asset from the marketplace callable set

### Requirement: Owners MUST be able to soft-delete their own assets
The system MUST allow an asset owner to soft-delete their own asset so it is no longer discoverable or callable while preserving historical identity for governance and logs.

#### Scenario: Owner soft-deletes an asset
- **WHEN** an asset owner deletes their own asset
- **THEN** the system marks the asset deleted instead of exposing it as an active marketplace asset

#### Scenario: Deleted asset is absent from owner active workspace list
- **WHEN** the current user requests their active asset workspace list after deleting one of their assets
- **THEN** the deleted asset is not returned as an active asset

### Requirement: Asset management APIs MUST map to one controller authority file
The system MUST define the owner-scoped asset management contract in `docs/api/api-asset-management.yaml`, and that authority file SHALL map one-to-one to `ApiAssetController.java`. Any required asset schema updates MUST be reflected in `docs/sql/api-asset.sql`.

#### Scenario: Generate asset management authority files
- **WHEN** the project updates the owner-scoped asset management contract or the `api_asset` table structure
- **THEN** it updates `docs/api/api-asset-management.yaml` and `docs/sql/api-asset.sql` with `tml-docs-spec-generate` before code implementation
