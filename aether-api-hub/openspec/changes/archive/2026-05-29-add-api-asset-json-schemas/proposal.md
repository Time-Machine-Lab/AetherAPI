## Why

API assets already store request and response examples, but they cannot declare machine-readable request/response JSON Schema. This makes marketplace browsing and consumer integration depend on free-form examples rather than a stable contract snapshot.

## What Changes

- Add nullable request body and response body JSON Schema properties to API asset storage and contracts.
- Extend owner-scoped asset create, revise, and detail responses in `docs/api/api-asset-management.yaml`.
- Extend published Discovery detail in `docs/api/api-catalog-discovery.yaml` so consumers can inspect published schemas without exposing owner-only configuration.
- Persist and project the new fields through the existing catalog domain, service, infrastructure, and web adapter layers.
- Treat the schemas as optional JSON text snapshots; no backend JSON Schema dialect validation is introduced in this change.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `catalog-owner-asset-management`: owner-scoped API asset management can create, revise, and return nullable request/response JSON Schema snapshots.
- `catalog-discovery-read-api`: published asset Discovery detail can return nullable request/response JSON Schema snapshots for marketplace consumers.

## Impact

- Authority documents: `docs/sql/api-asset.sql`, `docs/api/api-asset-management.yaml`, and `docs/api/api-catalog-discovery.yaml` must be updated before implementation. These are top-level authority artifacts and should be generated or revised with `tml-docs-spec-generate` using the SQL/API templates.
- Backend code: `ApiAssetController.java` contract DTOs, `ApiAssetWebDelegate`, `CatalogDiscoveryWebDelegate`, catalog service models/commands, `ApiAssetAggregate`, query ports, MyBatis-Plus DO/mapper/converter/repository.
- Database: add nullable `request_json_schema` and `response_json_schema` columns to the `api_asset` table.
- Tests: owner asset lifecycle mapping, Discovery detail projection, and persistence conversion tests.
