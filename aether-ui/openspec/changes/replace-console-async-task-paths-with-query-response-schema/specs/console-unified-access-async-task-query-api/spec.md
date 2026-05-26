## MODIFIED Requirements

### Requirement: Asset async task metadata mapping

The frontend catalog API layer SHALL map backend async task metadata for current-user assets using the current backend async task contract.

#### Scenario: Asset detail includes async task config

- **WHEN** the current-user asset detail response includes `asyncTaskConfig`
- **THEN** the mapped frontend asset MUST expose the config fields needed to understand whether task querying is enabled and how the backend is configured
- **AND** the mapped frontend asset MUST expose nullable `queryResponseJsonSchema` when present
- **AND** the mapped frontend asset MUST NOT expose `statusPath`, `resultPath`, or `errorPath` as supported async task metadata

#### Scenario: Asset summary includes task query availability

- **WHEN** the current-user asset list response includes `asyncTaskQueryEnabled`
- **THEN** the mapped frontend asset summary MUST expose this boolean without inferring it from unrelated asset fields

#### Scenario: Missing async metadata remains optional

- **WHEN** backend asset responses omit async task metadata
- **THEN** the mapped frontend asset and asset summary MUST remain compatible with existing synchronous assets

#### Scenario: Task query response schema does not affect passthrough result handling

- **WHEN** frontend code queries a Unified Access async task for an asset whose metadata includes `queryResponseJsonSchema`
- **THEN** the frontend task query result handling MUST preserve upstream passthrough payloads using the existing Unified Access result shape
- **AND** the frontend MUST NOT validate, extract, wrap, or normalize the task query response body using `queryResponseJsonSchema`
