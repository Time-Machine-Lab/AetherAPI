## Context

The backend change `realign-api-catalog-to-user-marketplace` has updated the asset domain, API contracts, and implementation so API assets are owned by the current console user and exposed to the marketplace only when published. The current `aether-console` implementation still reflects the older platform-management model:

- `src/api/catalog/asset.api.ts` calls `v1/assets`, `v1/assets/{apiCode}/enable`, and `v1/assets/{apiCode}/disable`.
- `src/api/catalog/catalog.dto.ts` and `catalog.types.ts` model asset status as `DRAFT / ENABLED / DISABLED`.
- `src/composables/useWorkspaceCatalog.ts` exposes enable/disable asset orchestration.
- `src/pages/workspace.vue`, i18n, mocks, and tests still present enabled/disabled asset labels.
- Discovery mapping does not yet preserve publisher summary or published time from the updated discovery contract.

The implementation must stay within the existing Vue 3, TypeScript, Vite, Tailwind, shadcn-vue, API-layer, composable-layer, and i18n structure defined by the shared frontend spec and `aether-console/DESIGN.md`.

## Goals / Non-Goals

**Goals:**

- Align the `aether-console` asset workspace with `docs/api/api-asset-management.yaml`.
- Replace frontend asset lifecycle vocabulary with `DRAFT / PUBLISHED / UNPUBLISHED`.
- Ensure all owner workspace asset calls use `v1/current-user/assets`.
- Add frontend support for publish, unpublish, soft delete, publisher display summary, and published time where the authority contracts define them.
- Keep marketplace discovery read-only and mapped to published-only discovery responses.
- Keep Unified Access target assist aligned with published marketplace assets.
- Update tests and mocks so local verification catches accidental fallback to the old contract.

**Non-Goals:**

- No backend API, SQL, or authority-doc updates.
- No redesign of `aether-console/DESIGN.md` or shared frontend architecture rules.
- No explicit Consumer management, billing, subscription, review, moderation, or publisher analytics UI.
- No new app route family for a separate provider portal.
- No changes to category, credential, API call log, or console-auth status semantics except where copy references asset publishability.

## Decisions

### 1. Keep asset workspace inside the existing protected workspace route

Use `src/pages/workspace.vue` and `useWorkspaceCatalog` as the asset-workspace integration point instead of creating a new route.

Why this over a new page:

- The current console already groups category, credential, log, and asset workflows under the protected console shell.
- The backend contract calls this a current-user asset workspace, not a separate provider portal.
- Reusing the existing route limits navigation churn and preserves the `ConsoleLayout` route-guard model.

### 2. Rename frontend asset actions to publication semantics at the API boundary

Replace `enableAsset` and `disableAsset` with `publishAsset` and `unpublishAsset`, mapped to:

- `PATCH v1/current-user/assets/{apiCode}/publish`
- `PATCH v1/current-user/assets/{apiCode}/unpublish`

Why this over keeping function names and changing URLs:

- Keeping enable/disable names would preserve the retired platform-operator mental model.
- Tests should fail loudly if old lifecycle terms reappear in asset code.
- Category and credential APIs still use enable/disable, so the separation must be explicit.

### 3. Preserve frontend domain compatibility through a focused status union update

Update `AssetStatus` to `DRAFT | PUBLISHED | UNPUBLISHED` and keep `CategoryStatus` and credential statuses unchanged.

Why this over a generic shared status type:

- Asset publication is now a distinct marketplace lifecycle.
- Category and API Key enablement remain valid separate concepts.
- A narrow type update reduces accidental status leakage between domains.

### 4. Map new contract fields in API adapters, not page components

Handle DTO reshaping in `src/api/catalog/*.api.ts` and `catalog.dto.ts`, including:

- asset workspace paths under `v1/current-user/assets`
- `publisherDisplayName`, `publishedAt`, `deleted`, `createdAt`, `updatedAt`
- discovery `publisher.displayName` and `publishedAt`
- AI capability request body using `streamingSupported` and `capabilityTags`

Why this over page-level mapping:

- Project rules require pages to orchestrate, not issue or reshape raw requests.
- Existing code already centralizes catalog DTO mapping in the API layer.
- It keeps composables and templates stable and testable.

### 5. Treat the old asset list proposal as superseded semantics

The open `add-console-asset-workspace-list` proposal introduced useful list UI shape, but it depended on the older management-list language. This change should absorb list behavior only after aligning it to current-user ownership and publication states.

Why this over implementing both:

- Parallel global and owner-scoped list semantics would conflict.
- The new backend contract explicitly removed the global management surface.
- Frontend tests should assert owner-workspace wording and paths.

## Risks / Trade-offs

- [Risk] Existing tests and mocks may keep old `ENABLED / DISABLED` asset fixtures alive. -> Mitigation: update asset-specific fixtures and add negative assertions for old asset paths/actions.
- [Risk] Category and credential enablement could be accidentally renamed to publication language. -> Mitigation: scope type and copy updates to catalog asset code only.
- [Risk] Discovery API currently returns no pagination parameters in the YAML list schema, while frontend code expects page metadata. -> Mitigation: implement tolerant mapping that defaults missing `total`, `page`, and `pageSize` without inventing new required contract fields.
- [Risk] Current workspace UI may need a delete action that was not part of the older visible flow. -> Mitigation: add the minimum expected owner soft-delete control using existing action styling and confirmation/error feedback patterns.
- [Risk] Published asset critical revisions can return `UNPUBLISHED`, surprising users after save. -> Mitigation: surface the returned status immediately and use i18n copy that distinguishes "published" from "draft" and "unpublished".

## Migration Plan

1. Update asset DTO/types and API adapter functions to the new authority contract.
2. Update workspace composable dependencies, action names, and list filters to current-user publication semantics.
3. Update workspace UI labels, filters, badges, actions, and delete flow.
4. Update discovery DTO mapping and marketplace display for publisher and published time.
5. Update Unified Access guidance and target-assist copy from enabled to published assets.
6. Refresh mocks and automated tests for API paths, status transitions, discovery mapping, and workspace orchestration.
7. Run `pnpm test`, `pnpm type-check`, and the existing frontend quality gates for `aether-console`.

Rollback strategy: revert this frontend change together with the backend contract realignment only if the backend authority docs also roll back. A frontend-only rollback would point the console back to retired API paths.

## Open Questions

- The discovery YAML currently defines list response `items` but not explicit pagination fields. Implementation should preserve current UI pagination only if the backend response still supplies those fields; otherwise the marketplace list should degrade to item-only rendering.
