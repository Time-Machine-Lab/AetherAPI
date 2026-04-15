## ADDED Requirements

### Requirement: Discovery list SHALL expose only enabled API assets
The system SHALL provide a discovery list for API marketplace browsing that returns only API assets whose exposure status is enabled.

#### Scenario: Exclude draft assets from discovery list
- **WHEN** a discovery list request is executed and an API asset is still in draft status
- **THEN** the draft asset is not returned in the list

#### Scenario: Exclude disabled assets from discovery list
- **WHEN** a discovery list request is executed and an API asset is in disabled status
- **THEN** the disabled asset is not returned in the list

#### Scenario: Include enabled asset summary
- **WHEN** a discovery list request is executed and an API asset is enabled
- **THEN** the list returns the asset with its category and asset type summary

### Requirement: Discovery detail SHALL return a readable enabled asset view
The system SHALL provide a detail view for enabled API assets that returns the descriptive information needed for browsing and access evaluation without exposing draft-only or disabled assets.

#### Scenario: Return detail for an enabled normal API
- **WHEN** a discovery detail request targets an enabled non-AI API asset
- **THEN** the system returns the enabled asset's descriptive detail view

#### Scenario: Do not expose a non-enabled asset through discovery detail
- **WHEN** a discovery detail request targets an API asset that is not enabled
- **THEN** the system does not expose that asset through the discovery detail API

#### Scenario: Preserve detail access when example snapshots are absent
- **WHEN** a discovery detail request targets an enabled API asset that has no example snapshots
- **THEN** the system still returns the asset detail successfully

### Requirement: Discovery detail SHALL differentiate AI assets from normal assets
The system SHALL present AI API assets as capability assets by returning their AI-specific metadata in addition to the common asset detail fields.

#### Scenario: Return AI capability metadata in detail
- **WHEN** a discovery detail request targets an enabled `AI_API` asset
- **THEN** the system returns the asset's provider, model, streaming capability, and capability tags as part of the detail view
