## Why

Marketplace API document export is generated from the published Discovery detail contract. The authority contract in `docs/api/api-catalog-discovery.yaml` already exposes `requestJsonSchema` and `responseJsonSchema`, but exported API documents still omit those machine-readable request and response schema snapshots. Consumers therefore have to rely on free-form examples or inspect raw Discovery payloads instead of reading the exported document directly.

## What Changes

- Require Discovery-backed API document export to render request schema and response schema sections when the published asset detail contains `requestJsonSchema` or `responseJsonSchema`.
- Omit only the missing schema sections when either field is null or blank, while preserving the rest of the exported document.
- Reuse the existing Discovery detail contract as the single source for schema snapshots; this change does not add new storage fields or controller DTO fields.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `catalog-discovery-read-api`: Discovery-backed API document export includes request and response schema sections sourced from published asset detail.

## Impact

- Authority documents: no new top-level `docs/api/` or `docs/sql/` fields are introduced. This change consumes the existing `requestJsonSchema` and `responseJsonSchema` fields in `docs/api/api-catalog-discovery.yaml`, which remains the authority file for `CatalogDiscoveryController.java`.
- Dependency boundary: if `add-api-asset-json-schemas` is not yet merged on the target branch, this change must sequence after that change rather than redefining the same schema fields.
- Backend or export assembly: the code path that transforms Discovery detail into exported marketplace API documents must render request/response schema sections.
- Tests: add focused export rendering coverage for present, partially absent, and fully absent schema snapshots.