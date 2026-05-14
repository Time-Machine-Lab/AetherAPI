## Context

The affected areas are both already present in `aether-console`:

- `src/pages/sign-in.vue` renders a standard username/password form backed by `useSignInForm`, but the composable initializes both fields empty and only delegates to the existing console-auth API.
- `../docs/api/console-auth.yaml` defines the sign-in request as `loginName` plus `password` and returns a bearer-token session. This proposal does not change that contract.
- `src/pages/workspace.vue` renders the default API asset management workspace. The current page places the API asset list, selected asset card, and recent asset card inside the same left column, while the edit experience opens in a fixed right-side drawer.
- `../docs/api/api-asset-management.yaml` already defines the data used by the list and selected asset card; the requested layout change does not require new fields.
- `aether-console/DESIGN.md` defines console surfaces, row rhythm, workspace alignment, drawers, and the semantic distinction between fields, actions, status tags, and passive surfaces.

## Goals / Non-Goals

**Goals:**
- Update the target app design authority before page code so the asset-management workspace composition is documented in `aether-console/DESIGN.md`.
- Hydrate the sign-in form from `localStorage` when the page opens.
- Persist the login account after successful sign-in.
- Persist the password only when the operator explicitly checks remember password, and clear any saved password when they do not.
- Preserve the existing `console-auth.yaml` sign-in request and session storage behavior.
- Recompose the asset management workspace so recent assets are upper-left, selected/API asset is lower-left, and the API asset list is on the right.
- Use the existing drawer for edit flow without reserving additional blank page space for it.
- Keep existing card, row, tag, button, field, empty/loading/error, and i18n conventions.

**Non-Goals:**
- No backend authentication changes, password encryption service, or token lifecycle redesign.
- No new API asset management endpoints or response fields.
- No update to `../docs/api/console-auth.yaml` or `../docs/api/api-asset-management.yaml`, because the requested behavior uses existing request/response contracts.
- No redesign of hidden workspace sections such as credentials, subscriptions, call logs, or platform proxy profiles.
- No replacement of the existing asset editor drawer.

## Decisions

### 1. Keep credential memory inside the sign-in composable

`useSignInForm` should own hydrating, saving, and clearing the local sign-in memory because it already owns the form state, submission flow, and redirect behavior. The page component should only render the checkbox and bind it to the composable state.

Alternative considered: store this in the global auth store.
Why not: the auth store already persists bearer-token session state under `appConfig.storageKey`; mixing optional raw password memory into session state would blur security boundaries and make logout/session expiration behavior harder to reason about.

### 2. Use a dedicated localStorage key and small schema

The local value should be separate from session storage and contain only the fields needed for the form: login name, remember-password flag, and the optional password. Hydration should tolerate missing, malformed, or browser-blocked storage and fall back to empty fields.

Alternative considered: write the remembered values directly into the existing session payload.
Why not: `console-auth.yaml` and the current auth store describe bearer-token session state, not reusable sign-in form input.

### 3. Save remembered values only after successful sign-in

The form should refresh the stored login account only after `deps.signIn` succeeds. If remember password is enabled, the password is stored at the same time; otherwise, any previous password is removed while the account remains available for next time.

Alternative considered: persist on every keystroke.
Why not: keystroke persistence can leave mistyped accounts or passwords in local storage even when sign-in fails.

### 4. Treat password persistence as explicit opt-in and reversible

The remember-password checkbox should default from the stored value. When checked, a successful sign-in saves the password; when unchecked, a successful sign-in removes saved password data. If a saved password exists, opening the page pre-fills the password and checks the control.

Alternative considered: always store the password after successful sign-in.
Why not: storing raw passwords in `localStorage` is a convenience with security trade-offs, so it must remain explicit and visible to the operator.

### 5. Recompose the workspace with a real two-region layout

The default asset management workspace should use a responsive two-region grid: a left stack for recent assets above the selected/API asset card, and a right region for the API asset list. On narrower screens, the regions collapse into a single column while preserving the order recent assets, selected/API asset, API asset list.

Alternative considered: keep the asset list left and put selected/recent content right.
Why not: the requested information architecture explicitly asks for recent assets upper-left, API asset lower-left, and list right; it also better matches the existing right drawer by avoiding a blank placeholder near the drawer side.

## Risks / Trade-offs

- [Risk] Storing passwords in `localStorage` exposes them to local browser access and any successful script injection in the same origin. -> Mitigation: make it opt-in, keep it separate from bearer-token storage, clear saved password when unchecked, and document the boundary in tests/specs.
- [Risk] Browser storage can be unavailable or contain malformed data. -> Mitigation: hydration and persistence must fail gracefully without blocking sign-in.
- [Risk] Moving the asset list to the right can make dense list controls feel cramped on medium breakpoints. -> Mitigation: use responsive grid tracks and collapse to a single column before card content or actions become crowded.
- [Risk] Recent assets may be absent, leaving the upper-left region visually thin. -> Mitigation: the layout should keep the selected/API asset card in the left stack and avoid rendering an empty recent card unless the existing UX already has recent items.
