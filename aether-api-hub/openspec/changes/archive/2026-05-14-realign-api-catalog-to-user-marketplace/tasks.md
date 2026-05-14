## 1. Top-Level Document Realignment

- [x] 1.1 Update `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md` to redefine the hub as a user-empowered API marketplace where everyone can share and use API assets.
- [x] 1.2 Update `docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md` to replace platform-maintainer language with user-owned asset lifecycle, publisher/consumer marketplace semantics, and publication-state rules.
- [x] 1.3 Update `docs/AetherAPI 第一期项目规划书.md` so phase-one scope describes owner-scoped asset publishing and marketplace consumption instead of platform-operated asset entry.

## 2. Authority Contract Updates

- [x] 2.1 Use `tml-docs-spec-generate` with the SQL template to update `docs/sql/api-asset.sql` for `api_asset` ownership fields, publication-state semantics, and any required legacy backfill notes.
- [x] 2.2 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-asset-management.yaml`, keeping it mapped one-to-one to `ApiAssetController.java`, so the historical global list from `add-asset-management-list-api` is replaced by current-user asset create/list/detail/revise/publish/unpublish/delete behavior.
- [x] 2.3 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-catalog-discovery.yaml`, keeping it mapped one-to-one to `CatalogDiscoveryController.java`, for published-only discovery and publisher summary fields.

## 3. Catalog Write-Model Refactor

- [x] 3.1 Refactor the catalog domain model (`ApiAssetAggregate`, related repository interfaces, and domain tests) to enforce owner scoping, publication-state transitions, and publish validation rules.
- [x] 3.2 Refactor the service layer (`ApiAssetApplicationService`, commands/models/use cases, and tests) to derive current-user ownership from console-authenticated context and to expose owner-scoped asset workspace behavior.
- [x] 3.3 Refactor the web layer (`ApiAssetController`, `ApiAssetWebDelegate`, `ListApiAssetReq`, `ApiAssetPageResp`, related request/response DTOs, and web tests) so the already-added historical list API becomes an owner-scoped asset workspace contract.
- [x] 3.4 Refactor the infrastructure layer (`ApiAssetDo`, converter, mapper, repository implementation, and persistence tests) to store ownership fields and new publication semantics from `docs/sql/api-asset.sql`.
- [x] 3.5 Refactor the existing list-query service and persistence artifacts from `add-asset-management-list-api` such as `ApiAssetPageResult` and related query implementations so they enforce owner isolation and marketplace publication vocabulary.

## 4. Boundary Module Alignment

- [x] 4.1 Update catalog discovery read-side code and tests so marketplace list/detail only expose published, non-deleted assets and include the required publisher summary fields.
- [x] 4.2 Update unified access target resolution and related tests so only published, non-deleted assets are eligible for invocation.
- [x] 4.3 Verify console session auth integration for asset management APIs so owner-scoped asset operations resolve current user the same way as other protected console APIs.
- [x] 4.4 Verify observability integration continues to log against the corrected asset identity without introducing explicit Consumer management into the asset flow.

## 5. Migration And Regression Verification

- [x] 5.1 Implement and verify any required legacy `api_asset` ownership backfill or migration handling for environments with preexisting assets.
- [x] 5.2 Add or update regression tests covering owner isolation, publish/unpublish transitions, soft delete behavior, discovery visibility, unified access eligibility, and replacement of the old global management list semantics.
- [x] 5.3 Run module-level verification for adapter, service, domain, and infrastructure tests affected by the marketplace asset realignment.
