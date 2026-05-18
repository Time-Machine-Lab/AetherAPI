## ADDED Requirements

### Requirement: API assets MAY store additive extension blocks for future platform features

The system SHALL allow an API asset to carry nullable owner-scoped extension blocks named `capabilityExtensions`, `policyExtensions`, and `metadataExtensions` so future first-class platform features can land without introducing new top-level asset fields immediately.

#### Scenario: Store capability extension block on an asset

- **WHEN** an asset owner saves an API asset with a non-null `capabilityExtensions` JSON object
- **THEN** the system persists that block as part of the same asset

#### Scenario: Store policy extension block on an asset

- **WHEN** an asset owner saves an API asset with a non-null `policyExtensions` JSON object
- **THEN** the system persists that block as part of the same asset

#### Scenario: Leave extension blocks absent

- **WHEN** an API asset is created or revised without any extension block content
- **THEN** the system keeps the three extension blocks null and the asset continues to behave like a normal asset without extension data

### Requirement: Extension blocks MUST remain additive to existing first-class asset fields

The system SHALL treat the extension blocks as additive entry points only. Existing first-class asset fields remain authoritative in this change and are not migrated into the new blocks.

#### Scenario: Existing AI profile field remains authoritative

- **WHEN** an `AI_API` asset stores both its existing AI capability profile and a `capabilityExtensions` block
- **THEN** the platform continues to use the existing AI capability profile field as the authoritative current feature path in this change

#### Scenario: Existing async task config remains authoritative

- **WHEN** an asset stores both its existing async task config and a `capabilityExtensions` block
- **THEN** Unified Access task-query behavior continues to depend only on the existing async task config in this change

### Requirement: Generic extension blocks MUST NOT affect publish or consumer-facing runtime behavior by default

The system SHALL ignore generic extension block content for publish readiness, Discovery exposure, and Unified Access execution unless a later dedicated feature change explicitly claims a typed sub-block.

#### Scenario: Publish readiness ignores extension blocks

- **WHEN** an asset owner adds or updates generic extension block content on an asset
- **THEN** the platform does not treat that change alone as satisfying or invalidating current publish-readiness requirements

#### Scenario: Discovery omits generic extension blocks

- **WHEN** a published asset contains generic extension block data
- **THEN** Discovery does not expose those generic extension blocks in this change

### Requirement: Asset storage authority MUST document extension block storage

The system SHALL document extension block storage in the `api_asset` SQL authority document.

#### Scenario: Generate asset storage authority doc

- **WHEN** the asset storage authority document is generated or updated for this change
- **THEN** it uses the existing `docs/sql/api-asset.sql` authority file for table `api_asset`
- **AND** it documents nullable storage for `capabilityExtensions`, `policyExtensions`, and `metadataExtensions`