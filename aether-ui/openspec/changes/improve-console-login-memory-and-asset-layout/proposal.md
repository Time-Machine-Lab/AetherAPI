## Why

`aether-console` currently requires operators to re-enter the login account on each visit, and the sign-in page has no remember-password option even though the workflow is for a retained developer console. The API asset management workspace also keeps the asset list, selected asset card, and recent assets stacked in the left column while edit actions already open in a right-side drawer, leaving unnecessary empty space and making the management surface feel cramped.

## What Changes

- Add a frontend-only sign-in memory experience that reads the saved login account from `localStorage` on page open, always refreshes the stored account after successful sign-in, and stores the password only when the operator explicitly enables remember password.
- Add a visible remember-password control on the sign-in form, with clear persisted-state behavior for saved and unsaved passwords.
- Rework the API asset management default workspace layout so "recent assets" appears in the upper-left region, the selected/API asset card appears in the lower-left region, and the API asset list owns the right-side region.
- Remove the unused right-side blank reservation from the asset management page now that editing happens in the existing right-side drawer/modal surface.
- Keep the scope frontend-only in `aether-console`; no authentication endpoint, asset API contract, or backend session behavior changes are introduced.

## Capabilities

### New Capabilities
- `console-login-credential-memory`: Define local sign-in account/password persistence and hydration behavior for the console login page.
- `console-asset-workspace-layout`: Define the asset management workspace composition where recent assets, selected asset, and asset list use the available page width according to the requested positions.

### Modified Capabilities
- None.

## Impact

- Affected app: `aether-console`
- Affected frontend areas: `src/pages/sign-in.vue`, `src/composables/useSignInForm.ts`, sign-in locales/tests, `src/pages/workspace.vue`, workspace layout tests where feasible
- Required authority-doc sync: update `aether-console/DESIGN.md` before page implementation to record the API asset management workspace composition rule introduced by this proposal
- Authority references: `../docs/api/console-auth.yaml`, `../docs/api/api-asset-management.yaml`, `aether-console/DESIGN.md`, and `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`
- Boundary note: password persistence is an explicit opt-in convenience on the local browser only; it must not alter `console-auth.yaml` request/response contracts or the existing bearer-token session storage model, and this change does not require API contract updates.
