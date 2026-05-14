## Context

`docs/api/api-asset-management.yaml` already defines `authConfig` on both `ReviseApiAssetReq` and `ApiAssetResp`. The backend publish contract can reject assets whose upstream endpoint configuration is incomplete, and token-based schemes need this configuration to be present before publication.

`aether-console` currently maps `authConfig` from asset responses into the frontend domain model, but the workspace configuration form only exposes `authScheme`, and `reviseAsset()` does not include `authConfig` in the `PUT v1/current-user/assets/{apiCode}` request body. This creates a front-end-only dead end: users can select `HEADER_TOKEN` or `QUERY_TOKEN`, but cannot provide or persist the matching configuration required by the existing contract.

The implementation must stay within the current Vue 3, TypeScript, Vite, Tailwind, shadcn-vue, API-layer, composable-layer, and i18n structure. The field styling should follow `aether-console/DESIGN.md` field semantics: white field surface, subtle border, existing focus ring, and the workspace grid rhythm.

## Goals / Non-Goals

**Goals:**

- Add `authConfig` to the asset workspace configuration editing flow.
- Preserve existing `authConfig` values when loading an owned asset detail.
- Send normalized `authConfig` values through the catalog API adapter on asset revision.
- Keep `NONE` authentication usable without `authConfig`.
- Add tests for DTO mapping, save orchestration, and token-auth regression coverage.

**Non-Goals:**

- No backend API, SQL, or domain model changes.
- No new auth scheme types beyond the documented `NONE`, `HEADER_TOKEN`, and `QUERY_TOKEN`.
- No custom client-side parser for `authConfig`; the frontend treats it as the documented string value and lets the backend validate publish readiness.
- No redesign of the workspace page or `aether-console/DESIGN.md`.

## Decisions

### Decision 1: Keep `authConfig` as a plain optional string in the existing asset configuration form

Add a single text input or compact textarea near the `authScheme` selector. Show it as part of the same upstream-auth field group so users understand it belongs to `HEADER_TOKEN` / `QUERY_TOKEN`.

Why this over scheme-specific subforms:

- The authority contract exposes `authConfig` as one nullable string, not a structured object.
- A plain string avoids inventing frontend-only fields that the API does not document.
- It keeps the fix narrow and minimizes translation, validation, and migration surface.

### Decision 2: Put state synchronization in `useWorkspaceCatalog`

Extend `assetConfigForm` with `authConfig`, hydrate it from `ApiAsset.authConfig`, clear it when there is no current asset, and include it in `handleSaveAssetConfig()` using the same trim-to-null normalization used by other optional text fields.

Why this over page-local state:

- The existing workspace already centralizes asset form state and save orchestration in the composable.
- Keeping form shape in one place makes unit tests cheap and avoids page-level request shaping.
- It aligns with the frontend spec's page/composable/API layering.

### Decision 3: Send `authConfig` through the API adapter

Add `authConfig` to `ReviseAssetBody` and include it in the `http.put()` body in `src/api/catalog/asset.api.ts`. Keep response mapping unchanged except for test coverage, because `mapAsset()` already preserves `dto.authConfig`.

Why this over sending raw form data directly:

- Existing catalog APIs already centralize request DTO reshaping in the API layer.
- It prevents the page or composable from knowing backend request field names beyond the typed API contract.

### Decision 4: Validate behavior through focused unit tests and mocks

Update API adapter tests to assert `authConfig` is sent and mapped. Update workspace composable tests to assert load/save round-trips for token auth. Update mock seed or handlers only as needed so local flows can represent complete token-auth assets.

Why this over manual verification only:

- The bug is a missing field propagation issue; unit tests are the fastest way to pin it down.
- The fix touches DTO, composable, and UI binding surfaces, so regression coverage should follow the data path.

## Risks / Trade-offs

- [Risk] Users may not know the exact string format expected by their upstream service. -> Mitigation: provide concise i18n-backed placeholder/help copy without enforcing a frontend-only parser.
- [Risk] Sending `authConfig: null` while switching to `NONE` could clear an existing token config. -> Mitigation: treat that as intentional when saving the form because the selected scheme no longer requires token configuration.
- [Risk] Existing tests may assume the revise body has no `authConfig`. -> Mitigation: update expectations to match the documented contract.

## Migration Plan

1. Confirm no authority-doc update is required because `authConfig` already exists in `docs/api/api-asset-management.yaml`.
2. Extend frontend DTO/types and API adapter request mapping.
3. Extend `useWorkspaceCatalog` form state, hydration, reset, and save payload.
4. Add the workspace UI field and i18n labels/placeholders.
5. Update mocks and unit tests for complete token-auth assets.
6. Run targeted Vitest suites, then `pnpm type-check` for `aether-console`.

Rollback strategy: revert the frontend change only. Existing backend behavior and contracts remain compatible because this change starts sending an already documented optional field.

## Open Questions

- Should later UX work replace the plain string with scheme-specific guided inputs if the API contract evolves to a structured `authConfig` object? This change deliberately does not do that until the authority contract changes.
