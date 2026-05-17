## Context

API Catalog uses `api_asset` as the owner-scoped asset write model and Discovery as a read-only published projection. The current authority files already expose free-form `requestExample` and `responseExample`, but there is no machine-readable contract field for request body or response body JSON Schema.

This change touches storage, owner asset management, and Discovery detail, so the top-level authority documents must be updated before code: `docs/sql/api-asset.sql`, `docs/api/api-asset-management.yaml`, and `docs/api/api-catalog-discovery.yaml`. The project configuration says table SQL files should use the table name; the existing authority file for `api_asset` is currently `docs/sql/api-asset.sql`, so this implementation updates the existing single authority file rather than creating a duplicate `api_asset.sql`.

## Goals / Non-Goals

**Goals:**

- Store nullable request and response JSON Schema snapshots on API assets.
- Allow owners to set, clear, and read the schema snapshots through current-user asset create/revise/detail APIs.
- Return the schema snapshots from published Discovery detail.
- Keep the fields as optional JSON text so the platform can support different JSON Schema dialects without adding a validator dependency.

**Non-Goals:**

- No runtime request or response validation against JSON Schema.
- No JSON Schema dialect enforcement, schema registry, versioning, or compatibility analysis.
- No change to Unified Access forwarding or upstream invocation behavior.
- No exposure of upstream URL or credential secrets through Discovery.

## Decisions

### 1. Store schemas as nullable text columns

Add `request_json_schema TEXT NULL` and `response_json_schema TEXT NULL` to `api_asset`. This matches existing JSON-like asset fields such as `auth_config`, `async_task_config`, and example snapshots, and avoids introducing a database JSON dependency or dialect-specific validation.

Alternative considered: native JSON columns. Rejected for now because existing table conventions use `TEXT` for JSON snapshots and because the immediate requirement only needs storage and display.

### 2. Use camelCase contract names

Expose the fields as `requestJsonSchema` and `responseJsonSchema` in API DTOs and OpenAPI YAML. This follows the existing external DTO convention and keeps the names distinct from `requestExample` and `responseExample`.

### 3. Thread fields through existing asset models

Add the properties to `RegisterApiAssetCommand`, `ReviseApiAssetCommand`, `ApiAssetModel`, `CatalogDiscoveryAssetDetailModel`, and the aggregate reconstitution/save path. Controller classes remain thin; mapping stays in delegates and converters.

### 4. Discovery returns only published schema snapshots

Discovery detail reads the same snapshots only for published, non-deleted assets that already pass Discovery visibility rules. The schema text is not treated as sensitive, but it remains optional and nullable.

## Risks / Trade-offs

- [Risk] Invalid JSON Schema text may be stored. -> Mitigation: keep this change as storage/display only and let frontend formatting provide a non-blocking plain-text fallback; future validation can be added as a separate requirement.
- [Risk] Large schemas increase row size. -> Mitigation: use nullable `TEXT`, do not index the schema columns, and avoid returning them in Discovery list responses.
- [Risk] Duplicate SQL authority naming could appear if a new `api_asset.sql` file is created. -> Mitigation: update the existing `docs/sql/api-asset.sql` authority file for this change.

## Migration Plan

- Update authority docs first with the two nullable columns and API fields.
- Add columns as nullable so existing rows remain valid and rollback can ignore the fields if needed.
- Deploy backend code that can read/write null values and maps absent columns as null in tests.
- Rollback behavior: older code can ignore the nullable columns; no data migration is required beyond the additive DDL.

## Open Questions

- None for this change. JSON Schema validation, dialect selection, and schema versioning are intentionally left for a future proposal.
