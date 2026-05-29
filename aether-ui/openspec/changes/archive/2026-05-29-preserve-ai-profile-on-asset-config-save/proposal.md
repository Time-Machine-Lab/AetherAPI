## Why

The console asset editor can appear to clear AI capability configuration after saving base asset configuration when the revision response does not include AI profile fields. This creates an avoidable state regression in the same editor flow even though the existing current-user asset contract already models AI capability metadata on asset responses.

## What Changes

- Preserve the current AI profile state when saving base asset configuration if the revision response omits AI profile data.
- Keep the existing AI profile binding behavior that preserves base asset configuration.
- Add focused regression coverage for the base-config-save path so future response-shape changes do not empty the AI capability form.
- Do not change backend APIs, request fields, page layout, visual design, or i18n copy.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `console-asset-workspace-usability`: Asset editor state preservation also covers saving base asset configuration without clearing the loaded or recently edited AI capability configuration.

## Impact

- Affected frontend files:
  - `aether-console/src/composables/useWorkspaceCatalog.ts`
  - `aether-console/src/composables/useWorkspaceCatalog.spec.ts`
- Authority checked:
  - `../docs/api/api-asset-management.yaml` already defines `ApiAssetResp.aiCapabilityProfile`; no API contract update is required.
  - `aether-console/DESIGN.md` and the frontend technical specification remain compatible because this change is composable state handling only.
- No backend, database, dependency, route, or visual-system changes are expected.
