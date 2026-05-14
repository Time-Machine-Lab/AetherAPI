## ADDED Requirements

### Requirement: Discovery detail MUST expose async task query configuration

Published asset Discovery detail MUST include a nullable `asyncTaskConfig` field when the published asset declares async task query configuration.

#### Scenario: Published asset has async task config

- **WHEN** a published non-deleted asset has `api_asset.async_task_config`
- **THEN** `GET /api/v1/discovery/assets/{apiCode}` MUST return the async task query configuration in `asyncTaskConfig`

#### Scenario: Published asset has no async task config

- **WHEN** a published non-deleted asset has no async task query configuration
- **THEN** `GET /api/v1/discovery/assets/{apiCode}` MUST return no async task configuration or a null `asyncTaskConfig`
