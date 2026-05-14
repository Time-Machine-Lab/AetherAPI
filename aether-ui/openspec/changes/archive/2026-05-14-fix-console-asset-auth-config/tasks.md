## 1. Authority Check

- [x] 1.1 Confirm `../docs/api/api-asset-management.yaml` already defines `authConfig` for asset revision and response, so no backend contract document update is required.
- [x] 1.2 Re-read `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` and `aether-console/DESIGN.md` before implementation to keep API layering, i18n, and field styling aligned.

## 2. API Contract Mapping

- [x] 2.1 Add `authConfig?: string | null` to `ReviseAssetBody` in `src/api/catalog/catalog.dto.ts`.
- [x] 2.2 Update `src/api/catalog/asset.api.ts` so `reviseAsset()` sends `authConfig` in the `PUT v1/current-user/assets/{apiCode}` request body.
- [x] 2.3 Update API adapter tests to assert asset response mapping preserves `authConfig` and revision requests include `authConfig`.

## 3. Workspace Form State

- [x] 3.1 Extend `useWorkspaceCatalog` asset configuration form state with `authConfig`.
- [x] 3.2 Hydrate, clear, and normalize `authConfig` with the same owned-asset load, delete/reset, and save flow used by other optional text fields.
- [x] 3.3 Update composable tests for `HEADER_TOKEN`, `QUERY_TOKEN`, and `NONE` save payloads.

## 4. Workspace UI

- [x] 4.1 Add an i18n-backed auth configuration field near the `authScheme` selector in `src/pages/workspace.vue`.
- [x] 4.2 Add `zh-CN` and `en-US` workspace copy for the auth configuration label, placeholder, and any concise helper text.
- [x] 4.3 Keep the new field within the existing workspace field styling and responsive grid rhythm.

## 5. Mock And Verification

- [x] 5.1 Update catalog mock seed data or handlers so token-auth assets can carry `authConfig`.
- [x] 5.2 Run targeted catalog API and workspace composable Vitest suites.
- [x] 5.3 Run `pnpm type-check` for `aether-console`.
