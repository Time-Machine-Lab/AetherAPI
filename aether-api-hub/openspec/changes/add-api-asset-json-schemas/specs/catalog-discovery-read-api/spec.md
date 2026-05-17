## ADDED Requirements

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
