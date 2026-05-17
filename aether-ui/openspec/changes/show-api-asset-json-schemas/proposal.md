## Why

The backend will add nullable request/response JSON Schema snapshots to API assets. `aether-console` needs to let owners maintain those fields and let marketplace/asset viewers inspect them with a reusable schema display component instead of raw scattered text blocks.

## What Changes

- Update `aether-console` API DTOs and mapping for current-user asset management and Discovery detail schema fields.
- Add request/response JSON Schema fields to the asset edit workflow, preserving null when a schema is absent.
- Design and implement a reusable console component for displaying JSON Schema content with formatting, copy support, empty state, and plain-text fallback.
- Render schemas in owner asset detail and published marketplace detail using the new component.
- Add i18n copy and focused tests for API mapping, workspace save behavior, and schema display formatting.

## Capabilities

### New Capabilities

- `console-json-schema-display`: reusable console behavior for rendering nullable JSON Schema content.

### Modified Capabilities

- `console-user-asset-workspace`: asset owners can edit and inspect request/response JSON Schema fields on owned API assets.
- `console-published-marketplace-discovery`: marketplace detail can display published request/response JSON Schema fields when Discovery returns them.

## Impact

- Authority dependencies: backend `docs/api/api-asset-management.yaml` and `docs/api/api-catalog-discovery.yaml` must expose `requestJsonSchema` and `responseJsonSchema` before frontend implementation.
- Frontend app: `aether-console`.
- Frontend areas: `src/api/catalog/*`, `src/composables/useWorkspaceCatalog.ts`, catalog/workspace UI components, locale files, and related tests.
- Design system: the new component must align with `aether-console/DESIGN.md` section 11 JSON/code display and state feedback rules.
