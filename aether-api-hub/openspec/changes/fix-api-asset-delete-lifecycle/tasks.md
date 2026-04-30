## 1. Authority Document Check

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` before implementation and follow its backend code, test, and layering constraints.
- [x] 1.2 Confirm `docs/sql/api-asset.sql` already covers the required storage contract through `api_asset.is_deleted`; only update this file with `tml-docs-spec-generate` using the SQL template if implementation proves the current DDL is insufficient.
- [x] 1.3 Confirm `docs/api/api-asset-management.yaml` already covers `DELETE /current-user/assets/{apiCode}` mapped one-to-one to `ApiAssetController.java`; only update this file with `tml-docs-spec-generate` using the API template if delete response semantics must be clarified.

## 2. Regression Tests

- [x] 2.1 Add or update `ApiAssetAggregateTest` coverage so `softDelete()` marks the aggregate deleted without treating `UNPUBLISHED` as the successful delete condition.
- [x] 2.2 Add or update `ApiAssetApplicationServiceTest` coverage for deleting draft, unpublished, and published owner assets, then asserting subsequent active owner lookups/lists do not return them.
- [x] 2.3 Add or update repository tests around `MybatisApiAssetRepository` / `ApiAssetMapper` to prove the logical-delete transition persists and active `findByCode` excludes deleted rows.
- [x] 2.4 Add or update read-path tests for Catalog Discovery and Unified Access so a deleted published asset is not discoverable and cannot be resolved as a callable target.
- [x] 2.5 Add or update `ApiAssetControllerWebMvcTest` coverage for `DELETE /api/v1/current-user/assets/{apiCode}` followed by active read behavior where practical.

## 3. Delete Lifecycle Implementation

- [x] 3.1 Adjust `ApiAssetAggregate.softDelete()` so deletion is represented by the deletion marker and does not rely on status demotion as the delete result.
- [x] 3.2 Ensure `ApiAssetApplicationService.deleteAsset()` keeps owner validation before deletion and saves the deleted aggregate through the repository.
- [x] 3.3 Fix `MybatisApiAssetRepository` / `ApiAssetMapper` persistence if `@TableLogic` prevents `updateById` from writing `is_deleted = true`; use an explicit mapper update when needed while preserving optimistic version behavior.
- [x] 3.4 Verify current-user management query mappers continue to filter `a.is_deleted = FALSE` for active list and detail reads.
- [x] 3.5 Verify Catalog Discovery query mappers and Unified Access target resolution only consume published, non-deleted assets.

## 4. Validation

- [x] 4.1 Run the focused domain, service, infrastructure, and adapter tests touched by this change.
- [x] 4.2 Run the relevant Maven module test suite for `aether-api-hub-standard` if focused tests pass.
- [x] 4.3 Manually review that no API contract or SQL DDL changed without the required `tml-docs-spec-generate` authority-document step.
