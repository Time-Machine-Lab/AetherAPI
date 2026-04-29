## Why

When a console user selects `HEADER_TOKEN` or `QUERY_TOKEN` for an owned API asset, the workspace has no visible way to enter the required `authConfig`, and the save request does not send it to `PUT v1/current-user/assets/{apiCode}`. As a result, publication fails with the existing incomplete-configuration error even when the user has filled the other upstream fields.

## What Changes

- Add frontend support for editing and preserving the documented `authConfig` field in the `aether-console` asset configuration form.
- Send `authConfig` through the catalog API adapter when saving an owned asset configuration.
- Keep `NONE` authentication usable without `authConfig`, while making `HEADER_TOKEN` and `QUERY_TOKEN` flows publishable after users provide the required value.
- Preserve the existing current-user asset endpoints, publication actions, i18n patterns, shadcn-vue/Tailwind field styling, and workspace layout rhythm.
- Add focused tests so the API adapter and workspace composable catch future regressions where `authConfig` is dropped.
- Do not change backend API contracts, SQL, unified access proxy behavior, or `aether-console/DESIGN.md`; the current authority docs already define the required field and visual baseline.

## Capabilities

### New Capabilities

- `console-asset-auth-config`: The console asset workspace lets users configure and save upstream auth configuration for token-based auth schemes before publishing owned assets.

### Modified Capabilities

- None. The active baseline spec is `console-api-call-log-pages`, which is not affected. This change extends the asset workspace behavior introduced by the unarchived `realign-console-assets-to-user-marketplace` change without modifying backend contracts.

## Impact

- Affected app: `aether-console`
- Authority dependencies: `../docs/api/api-asset-management.yaml`, `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, and `aether-console/DESIGN.md`
- Likely frontend files: `src/api/catalog/catalog.dto.ts`, `src/api/catalog/asset.api.ts`, `src/api/catalog/catalog.types.ts`, `src/composables/useWorkspaceCatalog.ts`, `src/pages/workspace.vue`, `src/locales/**/common.ts`, `src/api/catalog/catalog.mock.ts`, and related `*.spec.ts`
- API contract impact: no contract change; the frontend will start honoring the already documented `authConfig` request/response field
- User impact: users can complete token-based upstream auth configuration and publish assets without being blocked by a missing field they could not previously enter
