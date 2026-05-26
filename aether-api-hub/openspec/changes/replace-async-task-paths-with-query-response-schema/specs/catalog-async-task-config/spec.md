## ADDED Requirements

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

## MODIFIED Requirements

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
