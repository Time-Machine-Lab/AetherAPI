## 1. Authority And Baseline Alignment

- [x] 1.1 Re-read `../docs/api/api-asset-management.yaml`, `../docs/api/api-catalog-discovery.yaml`, the API Hub catalog design docs, the shared frontend stack spec, and `aether-console/DESIGN.md` before code changes.
- [x] 1.2 Confirm the backend authority docs already cover the required endpoint, field, and status changes; if drift is found, stop implementation and update the authority docs first.
- [x] 1.3 Compare the open `add-console-asset-workspace-list` change with this change and mark any useful list behavior to absorb under current-user workspace semantics.

## 2. Catalog API Contract Mapping

- [x] 2.1 Update `src/api/catalog/catalog.types.ts` so asset status is `DRAFT | PUBLISHED | UNPUBLISHED` while category and credential statuses stay unchanged.
- [x] 2.2 Update `src/api/catalog/catalog.dto.ts` for current-user asset DTOs, publisher display fields, published timestamps, deleted flag, and discovery publisher response fields.
- [x] 2.3 Update `src/api/catalog/asset.api.ts` to call `v1/current-user/assets`, expose `publishAsset`, `unpublishAsset`, and `deleteAsset`, and remove old asset `enableAsset` / `disableAsset` exports.
- [x] 2.4 Update `src/api/catalog/discovery.api.ts` to map publisher summary and `publishedAt`, and tolerate discovery list responses that contain only `items`.
- [x] 2.5 Update `src/api/catalog/asset.api.spec.ts` and `discovery.api.spec.ts` to assert the new paths, field mappings, and publication lifecycle names.

## 3. Workspace Orchestration

- [x] 3.1 Update `src/composables/useWorkspaceCatalog.ts` dependencies and handlers from enable/disable to publish/unpublish, including current-user asset list filters.
- [x] 3.2 Add delete orchestration for the current asset, including list refresh or local list removal after successful delete.
- [x] 3.3 Ensure revision and AI profile save flows send only documented fields and immediately reflect returned `UNPUBLISHED` status after critical revisions.
- [x] 3.4 Update `useWorkspaceCatalog.spec.ts` and autoload tests for current-user list, publish, unpublish, delete, and failure states.

## 4. Console UI And Copy

- [x] 4.1 Update `src/pages/workspace.vue` asset list filters, badges, action buttons, detail panel, and delete affordance to use draft/published/unpublished marketplace language.
- [x] 4.2 Update `src/pages/index.vue` or related marketplace rendering to show publisher summary and published time when available.
- [x] 4.3 Update Unified Access playground guidance and target-assist copy so it refers to published marketplace assets, not enabled assets.
- [x] 4.4 Update `src/locales/zh-CN/common.ts` and `src/locales/en-US/common.ts` for all new and changed user-visible copy.

## 5. Mocks And Regression Coverage

- [x] 5.1 Update `src/api/catalog/catalog.mock.ts` seed data and routes to use `PUBLISHED / UNPUBLISHED`, `v1/current-user/assets`, publish/unpublish, delete, publisher summary, and published timestamps.
- [x] 5.2 Search `aether-console/src` for old asset-specific `v1/assets`, `enableAsset`, `disableAsset`, `ENABLED`, and `DISABLED` usage and remove or justify remaining non-asset occurrences.
- [x] 5.3 Run `pnpm test` in `aether-console` and fix regressions caused by the contract realignment.
- [x] 5.4 Run `pnpm type-check` and the existing frontend quality gates required by the project before implementation is considered complete.
