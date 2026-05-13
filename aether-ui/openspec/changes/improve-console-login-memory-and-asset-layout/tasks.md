## 1. Authority And Design Alignment

- [x] 1.1 Re-check `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, `aether-console/DESIGN.md`, `../docs/api/console-auth.yaml`, and `../docs/api/api-asset-management.yaml` before implementation
- [x] 1.2 Update `aether-console/DESIGN.md` to record the API asset management workspace composition: recent assets upper-left, selected/API asset lower-left, API asset list right-side, and no reserved blank edit column
- [x] 1.3 Confirm no `../docs/api/*.yaml` update is required because the change uses existing console-auth and asset-management contracts

## 2. Login Credential Memory

- [x] 2.1 Add a dedicated localStorage-backed credential memory helper inside or near `useSignInForm` with graceful fallback for missing, malformed, or unavailable storage
- [x] 2.2 Initialize `loginName`, `password`, and `rememberPassword` from saved credential state when the sign-in form is created
- [x] 2.3 Persist the login account after successful sign-in, persist the password only when remember password is checked, and clear any saved password when unchecked
- [x] 2.4 Add the remember-password checkbox/control to `src/pages/sign-in.vue` using existing form, i18n, and console field/action styling
- [x] 2.5 Add or update sign-in locale entries for the remember-password control in `zh-CN` and `en-US`

## 3. Asset Workspace Layout

- [x] 3.1 Recompose the default `src/pages/workspace.vue` asset management content into a responsive two-region layout with the left stack and right list positions defined in the spec
- [x] 3.2 Move the recent-assets card above the selected/API asset card in the left region while preserving the existing no-recent-assets behavior
- [x] 3.3 Move the API asset list card into the right region and keep filtering, list states, row actions, and pagination behavior unchanged
- [x] 3.4 Verify the existing right-side asset editor drawer still opens and closes without relying on a reserved blank workspace column
- [x] 3.5 Ensure narrow viewports collapse into a single-column order of recent assets, selected/API asset card, then API asset list

## 4. Verification

- [x] 4.1 Add or update `useSignInForm` tests for hydration, successful persistence, unchecked password clearing, failed sign-in no-overwrite, and malformed storage fallback
- [x] 4.2 Add or update workspace rendering tests where feasible to assert the asset workspace section order and existing actions
- [x] 4.3 Run the relevant `aether-console` validation commands for tests, type-check, lint, and build as available
