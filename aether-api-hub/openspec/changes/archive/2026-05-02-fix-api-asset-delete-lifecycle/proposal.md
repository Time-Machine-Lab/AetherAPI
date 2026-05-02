## Why

BUG-001 shows that deleting an API asset currently returns success but does not persist the delete lifecycle correctly: the asset disappears from the frontend list optimistically, then reappears after refresh, and a published asset may be converted to unpublished instead of deleted. This breaks the API Catalog lifecycle contract where deletion is represented by `is_deleted`, not by the publication status.

## What Changes

- Fix the current-user asset delete behavior so `DELETE /api/v1/current-user/assets/{apiCode}` persistently soft-deletes the owner-scoped asset.
- Ensure deletion does not use `UNPUBLISHED` as a substitute for deletion; status and deletion state remain separate lifecycle concepts.
- Ensure deleted assets are excluded from current-user asset lists/details, Catalog Discovery, and Unified Access target resolution.
- Add regression coverage for deleting draft, unpublished, and published assets, including refresh/read-after-delete behavior.
- No table-structure change is expected because `docs/sql/api-asset.sql` already defines `api_asset.is_deleted`.
- No endpoint shape change is expected because `docs/api/api-asset-management.yaml` already maps the delete operation to `ApiAssetController.java`; if the response semantics need clarification during implementation, update that authority document with `tml-docs-spec-generate` using the API generation template before code changes.

## Capabilities

### New Capabilities

- `catalog-asset-delete-lifecycle`: API Catalog must persist owner asset deletion as a true soft-delete lifecycle transition and consistently hide deleted assets from management, discovery, and invocation paths.

### Modified Capabilities

- None.

## Impact

- Affected authority docs: `docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`, `docs/api/api-asset-management.yaml`, and `docs/sql/api-asset.sql`.
- Expected code areas: API Catalog domain aggregate/service, owner-scoped asset repository/mapper, `ApiAssetController.java`, Catalog Discovery read-side filtering, Unified Access target resolution, and related tests.
- Potential boundary conflict: this change must not redefine delete as unpublish. `UNPUBLISHED` remains a reversible publication state, while `is_deleted = true` is the terminal asset-removal marker for active workflows.
