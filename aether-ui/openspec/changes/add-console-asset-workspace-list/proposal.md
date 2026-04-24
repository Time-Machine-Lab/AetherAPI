## Why

`aether-console` currently exposes category management, single-asset lookup by `apiCode`, asset registration, and recent-asset shortcuts, but it still lacks a real management list for existing assets. That makes the workspace inconsistent with the rest of the console and forces operators to remember exact asset codes before they can continue management work.

## What Changes

- Add a console workspace asset-list experience so operators can browse existing API assets directly inside `console-workspace`.
- Reuse the existing protected workspace route and shell conventions instead of creating a new app or standalone route family.
- Introduce a frontend API list wrapper, workspace state orchestration, and i18n-backed UI for page/filter/list/detail interaction.
- Treat the backend management list contract as a prerequisite dependency and align the frontend with the updated authority document `../docs/api/api-asset-management.yaml`; if that contract is not yet updated, implementation must wait.
- Keep the change focused on asset management browsing; it does not redesign discovery market cards, credentials, API call logs, or placeholder sections like usage/orders/billing/docs.

## Capabilities

### New Capabilities
- `console-asset-workspace-list`: Define the console workspace behavior for browsing, filtering, paging, and selecting managed API assets.

### Modified Capabilities
- None.

## Impact

- Affected app: `aether-console`
- Affected frontend areas: `src/pages`, `src/composables`, `src/api`, `src/locales`, and console shell workspace composition
- Authority dependencies: `../docs/api/api-asset-management.yaml`, `aether-console/DESIGN.md`, and the shared frontend stack/spec guidance
- Boundary note: this proposal depends on the backend management list proposal and must not invent undocumented request parameters or response fields
