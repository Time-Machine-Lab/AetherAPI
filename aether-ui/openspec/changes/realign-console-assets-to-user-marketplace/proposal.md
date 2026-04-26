## Why

The API Hub asset authority docs and backend contract have moved from platform-operated asset management to a current-user-owned marketplace model, but `aether-console` still calls the old `/api/v1/assets` management surface and uses `ENABLED / DISABLED` asset language. The frontend must realign now so the console does not keep integrating against a retired ownership and publication model.

## What Changes

- **BREAKING** Replace `aether-console` asset-management integration from global `v1/assets` endpoints to current-user workspace endpoints under `v1/current-user/assets`.
- **BREAKING** Replace asset exposure UI and client types from `ENABLED / DISABLED` to marketplace publication states `DRAFT / PUBLISHED / UNPUBLISHED`; keep category and credential enablement language unchanged.
- Update the workspace asset list, lookup, detail, create, revise, AI profile, publish, unpublish, and delete flows so they describe and operate on the current authenticated user's own assets only.
- Update discovery list/detail mapping and marketplace UI to display published marketplace assets with publisher summary and `publishedAt` fields from `docs/api/api-catalog-discovery.yaml`, without exposing owner workspace internals.
- Update Unified Access playground copy and target-selection assumptions so it refers to published callable assets instead of enabled assets.
- Refresh local mocks, API DTOs, frontend domain types, composables, i18n, and tests to match the new asset contracts.
- Treat the open `add-console-asset-workspace-list` change as superseded by this realignment for asset semantics; any still-useful list UI work should be absorbed under current-user workspace semantics.
- Do not update backend `docs/api/*.yaml`, `docs/sql/*.sql`, `aether-console/DESIGN.md`, or shared frontend stack docs in this change; the current authority docs already define the required contract and visual baseline.

## Capabilities

### New Capabilities

- `console-user-asset-workspace`: Current authenticated console users manage only their own API marketplace assets, including list, detail, create, revise, publish, unpublish, delete, and AI profile maintenance.
- `console-published-marketplace-discovery`: The console marketplace browse experience consumes published-only discovery responses and presents publisher-facing marketplace fields.
- `console-unified-access-published-targets`: Unified Access target-assist UI uses published marketplace assets as callable targets and removes old enabled-asset wording.

### Modified Capabilities

- None. The only active baseline spec is `console-api-call-log-pages`, which is not changed by the asset realignment.

## Impact

- Affected app: `aether-console`
- Backend authority dependencies: `../docs/api/api-asset-management.yaml`, `../docs/api/api-catalog-discovery.yaml`, `../docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`, and `../docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`
- Frontend authority dependencies: `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` and `aether-console/DESIGN.md`
- Likely frontend areas: `src/api/catalog/*.ts`, `src/composables/useWorkspaceCatalog.ts`, `src/composables/useCatalogDiscovery.ts`, `src/features/unified-access`, `src/pages/workspace.vue`, `src/pages/index.vue`, `src/locales/**/common.ts`, `src/api/catalog/catalog.mock.ts`, and related `*.spec.ts`
- Contract changes: no new frontend dependency is expected, but API paths, DTO fields, asset status enums, actions, labels, and tests must be updated together
