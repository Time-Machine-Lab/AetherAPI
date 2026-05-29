# catalog-discovery-read-api Specification

## Purpose

Define the read-only discovery experience for API marketplace browsing so callers can list and inspect published API assets without depending on write-model internals.
## Requirements
### Requirement: Discovery list SHALL expose only published API assets
The system SHALL provide a discovery list for API marketplace browsing that returns only API assets whose marketplace publication state is published and whose deletion flag is not active.

#### Scenario: Exclude draft assets from discovery list
- **WHEN** a discovery list request is executed and an API asset is still in draft status
- **THEN** the draft asset is not returned in the list

#### Scenario: Exclude unpublished assets from discovery list
- **WHEN** a discovery list request is executed and an API asset is in unpublished status
- **THEN** the unpublished asset is not returned in the list

#### Scenario: Exclude deleted assets from discovery list
- **WHEN** a discovery list request is executed and an API asset is soft-deleted
- **THEN** the deleted asset is not returned in the list

#### Scenario: Include published asset summary
- **WHEN** a discovery list request is executed and an API asset is published
- **THEN** the list returns the asset with its category, asset type summary, and marketplace publisher summary

### Requirement: Discovery detail SHALL return a readable published asset view
The system SHALL provide a detail view for published API assets that returns the descriptive information needed for browsing and access evaluation without exposing draft, unpublished, deleted, or write-model-only fields.

#### Scenario: Return detail for a published normal API
- **WHEN** a discovery detail request targets a published non-AI API asset
- **THEN** the system returns the published asset's descriptive detail view

#### Scenario: Do not expose a non-published asset through discovery detail
- **WHEN** a discovery detail request targets an API asset that is not in published state
- **THEN** the system does not expose that asset through the discovery detail API

#### Scenario: Do not expose a deleted asset through discovery detail
- **WHEN** a discovery detail request targets an API asset that is soft-deleted
- **THEN** the system does not expose that asset through the discovery detail API

#### Scenario: Preserve detail access when example snapshots are absent
- **WHEN** a discovery detail request targets a published API asset that has no example snapshots
- **THEN** the system still returns the asset detail successfully

### Requirement: Discovery detail SHALL differentiate AI assets from normal assets
The system SHALL present published AI API assets as capability assets by returning their AI-specific metadata in addition to the common marketplace asset detail fields.

#### Scenario: Return AI capability metadata in detail
- **WHEN** a discovery detail request targets a published `AI_API` asset
- **THEN** the system returns the asset's provider, model, streaming capability, and capability tags as part of the detail view

### Requirement: Discovery reads SHALL expose minimal publisher context
The system SHALL expose minimal publisher-facing summary data in discovery list and detail responses so marketplace users can understand who shared an API asset without leaking sensitive account data.

#### Scenario: Return publisher summary in discovery list
- **WHEN** the discovery list returns a published asset
- **THEN** each returned item includes the asset's publisher summary fields required by the marketplace UI

#### Scenario: Return publisher summary in discovery detail
- **WHEN** the discovery detail returns a published asset
- **THEN** the detail includes the asset's publisher summary fields required by the marketplace UI

### Requirement: Discovery detail MUST expose published request and response JSON schemas

Discovery detail MUST include nullable request/response JSON Schema snapshots for published, non-deleted API assets when those snapshots are present on the asset. The authority file for this behavior is `docs/api/api-catalog-discovery.yaml`, which maps to `CatalogDiscoveryController.java`.

#### Scenario: Published asset has schema snapshots

- **WHEN** a published non-deleted API asset has `requestJsonSchema` and `responseJsonSchema`
- **THEN** `GET /api/v1/discovery/assets/{apiCode}` returns both schema snapshots in the Discovery detail response

#### Scenario: Published asset has no schema snapshots

- **WHEN** a published non-deleted API asset has no request or response JSON Schema snapshots
- **THEN** `GET /api/v1/discovery/assets/{apiCode}` returns null or absent values for the corresponding schema fields without failing

#### Scenario: Discovery list stays compact

- **WHEN** a caller requests the Discovery asset list
- **THEN** the list response does not include request/response JSON Schema snapshots

#### Scenario: Update Discovery authority before code

- **WHEN** the project exposes schema snapshots through Discovery detail
- **THEN** it updates `docs/api/api-catalog-discovery.yaml` before backend code implementation

### Requirement: Discovery-backed API document export MUST render request and response schema sections

API document export derived from published Discovery detail MUST include machine-readable request and response schema sections when `requestJsonSchema` or `responseJsonSchema` is present on the published asset detail. The authority contract for those source fields remains `docs/api/api-catalog-discovery.yaml`, which maps to `CatalogDiscoveryController.java`.

#### Scenario: Export both schema sections from published Discovery detail

- **WHEN** an exported API document is generated for a published asset whose Discovery detail contains both `requestJsonSchema` and `responseJsonSchema`
- **THEN** the exported document includes both a request schema section and a response schema section populated from those values

#### Scenario: Export only the available schema section

- **WHEN** an exported API document is generated for a published asset and only one of `requestJsonSchema` or `responseJsonSchema` is present
- **THEN** the exported document renders the available schema section and omits only the missing schema section

#### Scenario: Preserve export when schema snapshots are absent

- **WHEN** an exported API document is generated for a published asset whose Discovery detail has no request or response schema snapshots
- **THEN** the export still succeeds without rendering request or response schema sections

#### Scenario: Reuse the existing Discovery authority boundary

- **WHEN** the project adds request and response schema sections to exported API documents
- **THEN** it reuses `requestJsonSchema` and `responseJsonSchema` from `docs/api/api-catalog-discovery.yaml` instead of introducing duplicate export-only schema fields

