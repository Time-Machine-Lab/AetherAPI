## MODIFIED Requirements

### Requirement: Discovery detail MUST expose async task query configuration

Published asset Discovery detail MUST include a nullable `asyncTaskConfig` field when the published asset declares async task query configuration.

#### Scenario: Published asset has async task config

- **WHEN** a published non-deleted asset has `api_asset.async_task_config`
- **THEN** `GET /api/v1/discovery/assets/{apiCode}` MUST return the async task query configuration in `asyncTaskConfig`
- **AND** the returned configuration MUST include nullable `queryResponseJsonSchema` when the published configuration has a task-query response schema
- **AND** the returned configuration MUST NOT include `statusPath`, `resultPath`, or `errorPath`

#### Scenario: Published asset has no async task config

- **WHEN** a published non-deleted asset has no async task query configuration
- **THEN** `GET /api/v1/discovery/assets/{apiCode}` MUST return no async task configuration or a null `asyncTaskConfig`

#### Scenario: Discovery authority doc exposes query response schema

- **WHEN** the Discovery API authority document is generated or updated for this change
- **THEN** it uses `docs/api/api-catalog-discovery.yaml` mapped one-to-one to `CatalogDiscoveryController.java`
- **AND** it documents `asyncTaskConfig.queryResponseJsonSchema` as published task-query response schema metadata
- **AND** it omits legacy task path fields from the published async task configuration schema
