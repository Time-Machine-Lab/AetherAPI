# catalog-async-task-config Specification

## Purpose
TBD - created by archiving change add-unified-access-async-task-query. Update Purpose after archive.
## Requirements
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
- **AND** it documents `queryResponseJsonSchema` as the nullable task-query response schema snapshot
- **AND** it does not document `statusPath`, `resultPath`, or `errorPath` as supported async task configuration fields

### Requirement: Asset storage authority MUST document async task config storage
The system SHALL document async task query configuration storage in the `api_asset` SQL authority document.

#### Scenario: Generate asset storage authority doc
- **WHEN** the asset storage authority document is generated or updated for this change
- **THEN** it uses `docs/sql/api-asset.sql` mapped one-to-one to table `api_asset`
- **AND** it documents the nullable async task configuration storage needed by API Catalog
- **AND** it describes `async_task_config` as storing task query configuration JSON that may contain `queryResponseJsonSchema`

### Requirement: Async task config MUST describe query response schema
The system SHALL allow an async task query configuration to include a nullable `queryResponseJsonSchema` JSON Schema snapshot describing the upstream task-query response body, and SHALL NOT expose `statusPath`, `resultPath`, or `errorPath` as async task configuration fields in new API contracts or new stored JSON.

#### Scenario: Store task query response schema
- **WHEN** an asset owner saves an API asset with async task query configuration and a valid task-query response schema snapshot
- **THEN** the system stores that schema in `asyncTaskConfig.queryResponseJsonSchema`
- **AND** the system does not store new `statusPath`, `resultPath`, or `errorPath` members for that configuration

#### Scenario: Omit task query response schema
- **WHEN** an asset owner saves an API asset with async task query configuration but no task-query response schema evidence
- **THEN** the system allows `asyncTaskConfig.queryResponseJsonSchema` to be null or absent
- **AND** the absence of that optional schema does not by itself make the async task query configuration incomplete

#### Scenario: Read legacy task path fields
- **WHEN** the system loads an existing asset whose stored `async_task_config` JSON contains `statusPath`, `resultPath`, or `errorPath`
- **THEN** the system ignores those legacy members during model projection
- **AND** subsequent writes emit `queryResponseJsonSchema` when present instead of legacy path members

