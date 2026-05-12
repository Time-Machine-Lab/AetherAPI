## ADDED Requirements

### Requirement: API assets MAY declare async task query configuration
The system SHALL allow an API asset to carry optional asynchronous task query configuration owned by API Catalog and stored under the `api_asset` authority table design.

#### Scenario: Store async task config on an asset
- **WHEN** an asset owner saves an API asset with async task query configuration
- **THEN** the system persists the configuration as part of that asset without requiring a separate task-query asset

#### Scenario: Keep synchronous assets unchanged
- **WHEN** an API asset has no async task query configuration
- **THEN** the asset remains a normal synchronous Unified Access target and existing publish/call behavior is unchanged

### Requirement: Async task config MUST include a query template before it can be used
The system SHALL reject or ignore incomplete async task query configuration that does not provide enough information to build an upstream task query request.

#### Scenario: Missing task query URL template
- **WHEN** an asset is configured as supporting async task query but the task query URL template is blank or missing
- **THEN** the system treats the async task query configuration as incomplete and does not allow Unified Access task query execution for that asset

#### Scenario: URL template does not include task id placeholder
- **WHEN** an asset async task query URL template does not contain a `{taskId}` placeholder
- **THEN** the system treats the configuration as invalid because it cannot resolve a caller-provided task id into the upstream query URL

### Requirement: Asset management API contract MUST document async task config fields
The system SHALL document async task query configuration fields in the top-level asset management API authority document mapped to `ApiAssetController.java`.

#### Scenario: Generate asset management authority doc
- **WHEN** the asset management API authority document is generated or updated for this change
- **THEN** it uses `docs/api/api-asset-management.yaml` mapped one-to-one to `ApiAssetController.java`
- **AND** it documents async task query configuration request and response fields for API assets

### Requirement: Asset storage authority MUST document async task config storage
The system SHALL document async task query configuration storage in the `api_asset` SQL authority document.

#### Scenario: Generate asset storage authority doc
- **WHEN** the asset storage authority document is generated or updated for this change
- **THEN** it uses `docs/sql/api-asset.sql` mapped one-to-one to table `api_asset`
- **AND** it documents the nullable async task configuration storage needed by API Catalog
