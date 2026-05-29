## ADDED Requirements

### Requirement: Owner asset management MUST accept and return asset extension blocks

The system MUST allow the current authenticated asset owner to create, revise, clear, and read nullable `capabilityExtensions`, `policyExtensions`, and `metadataExtensions` fields for an owned API asset through the owner-scoped asset management contract.

#### Scenario: Create asset with extension blocks

- **WHEN** the current authenticated user submits a create-asset request with one or more non-null extension blocks
- **THEN** the system stores those blocks on the created draft asset

#### Scenario: Revise extension blocks on an owned asset

- **WHEN** an asset owner revises an owned asset with updated extension block content
- **THEN** the system persists the revised extension block content for that asset

#### Scenario: Clear extension blocks on an owned asset

- **WHEN** an asset owner revises an owned asset with null values for one or more extension blocks
- **THEN** the system stores those extension blocks as null instead of retaining stale content

#### Scenario: Return extension blocks in owner detail

- **WHEN** the current authenticated user requests detail for an owned API asset
- **THEN** the owner-scoped asset response includes nullable `capabilityExtensions`, `policyExtensions`, and `metadataExtensions`

### Requirement: Asset management authority MUST document extension block fields without removing existing fields

The system MUST update the owner-scoped asset management authority document to add the extension block fields while preserving the existing top-level field contract.

#### Scenario: Generate owner asset management authority doc

- **WHEN** the project updates owner asset management for this change
- **THEN** it updates `docs/api/api-asset-management.yaml`, mapped one-to-one to `ApiAssetController.java`
- **AND** it adds the extension block request and response fields without removing existing asset fields from the contract