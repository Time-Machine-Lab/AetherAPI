## Why

API assets can now declare `asyncTaskConfig`, and the Playground can query async tasks, but asset owners still cannot configure the async task query channel from the asset edit drawer. Without this form, users must rely on backend/database updates to enable task querying for their own APIs.

## What Changes

- Add async task query configuration fields to the `aether-console` asset edit drawer.
- Persist `asyncTaskConfig` through the current-user asset revise API.
- Sync existing asset `asyncTaskConfig` into the edit form and clear it when disabled.
- Add validation for the required task query URL template when async task query is enabled.
- Add i18n labels, helper text, and tests for API mapping and workspace save behavior.

## Capabilities

### New Capabilities

- `console-asset-async-task-config-form`: asset owner form behavior for editing Unified Access async task query configuration.

### Modified Capabilities

- None.

## Impact

- Affected app: `aether-console`.
- Backend authority dependencies: `docs/api/api-asset-management.yaml`, which already defines `asyncTaskConfig` on revise asset requests and responses.
- Frontend areas: `src/api/catalog/*`, `src/composables/useWorkspaceCatalog.ts`, `src/pages/workspace.vue`, locale files, and related tests.
- Contract changes: none expected.
