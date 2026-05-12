## ADDED Requirements

### Requirement: Asset edit drawer exposes async task config

The asset edit drawer SHALL allow asset owners to configure the Unified Access async task query channel for an API asset.

#### Scenario: Existing config is loaded into form

- **WHEN** an asset detail response includes `asyncTaskConfig`
- **THEN** opening the asset editor MUST prefill the async task fields from that config

#### Scenario: Enabled config is saved

- **WHEN** async task query is enabled and the user saves the asset config
- **THEN** the revise asset request MUST include `asyncTaskConfig` with all known async task config fields

#### Scenario: Disabled config is cleared

- **WHEN** async task query is disabled and the user saves the asset config
- **THEN** the revise asset request MUST include `asyncTaskConfig: null`

#### Scenario: Missing query URL blocks save

- **WHEN** async task query is enabled but the query URL template is empty
- **THEN** the frontend MUST block save and show a localized validation error
