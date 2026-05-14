# console-unified-access-async-task-query-api Specification

## Purpose
TBD - created by archiving change add-console-async-task-query-api. Update Purpose after archive.
## Requirements
### Requirement: Unified Access task query API client

The frontend SHALL expose a typed `aether-console` API-layer function for querying a Unified Access async task by `apiCode`, `taskId`, and API key.

#### Scenario: Query request is sent through Unified Access task endpoint

- **WHEN** frontend code queries task id `task/123` for API code `image-generator` with an API key
- **THEN** the request MUST call `GET v1/access/image-generator/tasks/task%2F123`
- **AND** the request MUST send `X-Aether-Api-Key` with the supplied API key
- **AND** the request MUST use the standalone Unified Access client rather than the console bearer-token HTTP client

#### Scenario: Query response preserves upstream passthrough payloads

- **WHEN** the task query endpoint returns JSON, text, event-stream, or binary data from upstream
- **THEN** the frontend result MUST preserve the status, content type, raw headers, and corresponding parsed body kind using the existing Unified Access result shape

#### Scenario: Query platform failure is classified

- **WHEN** the task query endpoint returns a JSON platform failure containing `failureType`
- **THEN** the frontend result MUST be classified as `platform-failure`
- **AND** the platform failure MUST expose code, message, failure type, trace id, and API code when present

### Requirement: Asset async task metadata mapping

The frontend catalog API layer SHALL map backend async task metadata for current-user assets.

#### Scenario: Asset detail includes async task config

- **WHEN** the current-user asset detail response includes `asyncTaskConfig`
- **THEN** the mapped frontend asset MUST expose the config fields needed to understand whether task querying is enabled and how the backend is configured

#### Scenario: Asset summary includes task query availability

- **WHEN** the current-user asset list response includes `asyncTaskQueryEnabled`
- **THEN** the mapped frontend asset summary MUST expose this boolean without inferring it from unrelated asset fields

#### Scenario: Missing async metadata remains optional

- **WHEN** backend asset responses omit async task metadata
- **THEN** the mapped frontend asset and asset summary MUST remain compatible with existing synchronous assets

