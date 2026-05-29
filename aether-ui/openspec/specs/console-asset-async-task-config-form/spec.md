# console-asset-async-task-config-form Specification

## Purpose
TBD - created by archiving change add-console-asset-async-task-config-form. Update Purpose after archive.
## Requirements
### Requirement: Asset edit drawer exposes async task config

The asset edit drawer SHALL allow asset owners to configure the Unified Access async task query channel for an API asset using the current async task contract fields.

#### Scenario: Existing config is loaded into form

- **WHEN** an asset detail response includes `asyncTaskConfig`
- **THEN** opening the asset editor MUST prefill the async task forwarding fields from that config
- **AND** the editor MUST prefill nullable `queryResponseJsonSchema` when present
- **AND** the editor MUST NOT show `statusPath`, `resultPath`, or `errorPath` fields

#### Scenario: Enabled config is saved

- **WHEN** async task query is enabled and the user saves the asset config
- **THEN** the revise asset request MUST include `asyncTaskConfig` with all known async task forwarding fields
- **AND** the request MUST include nullable `queryResponseJsonSchema`
- **AND** the request MUST NOT include `statusPath`, `resultPath`, or `errorPath`

#### Scenario: Disabled config is cleared

- **WHEN** async task query is disabled and the user saves the asset config
- **THEN** the revise asset request MUST include `asyncTaskConfig: null`

#### Scenario: Missing query URL blocks save

- **WHEN** async task query is enabled but the query URL template is empty
- **THEN** the frontend MUST block save and show a localized validation error

#### Scenario: Missing response schema remains optional

- **WHEN** async task query is enabled and the task query response schema field is empty
- **THEN** the frontend MUST allow save when the required forwarding fields are valid
- **AND** the revise asset request MUST send `queryResponseJsonSchema` as null or omit it according to existing request serialization patterns

