## ADDED Requirements

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