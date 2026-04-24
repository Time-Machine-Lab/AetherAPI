## Context

The current shell model in `aether-console/src/features/console/console-shell.ts` still publishes five sections that should be temporarily hidden from users:

- `category-manage`
- `usage`
- `orders`
- `billing`
- `docs`

Those entries are not isolated to one menu. They currently affect:

- the left sidebar navigation groups
- the top utility strip in `ConsoleLayout.vue`
- helper and preview surfaces that derive from `consoleWorkspacePanels`
- the workspace route behavior for `#category-manage`

This means the visibility change has to cover both menu rendering and the supporting shell state that can still advertise or activate hidden areas.

## Goals / Non-Goals

**Goals:**
- Keep the visible console focused on active sections only.
- Ensure hidden sections do not appear in sidebar, topbar, helper previews, or workspace-facing entry surfaces.
- Ensure direct navigation through stale hashes does not create a misleading active state or expose temporarily hidden management UI.
- Preserve the currently retained console paths, especially API asset management, API Key, API call logs, and playground.

**Non-Goals:**
- No backend contract or permission model changes.
- No permanent deletion of hidden capabilities.
- No information architecture redesign beyond the requested hiding scope.
- No changes to retained features unless they are required to absorb hidden-hash fallback behavior.

## Decisions

### 1. Treat the change as release-time visibility control, not feature removal
The proposal defines temporary hiding for specific sections and their related UI exposure. It does not require removing backend support or archived product intent for those areas.

Alternative considered: remove the features entirely.
Why not: the request explicitly says "temporarily hide", which implies the sections may be reintroduced later.

### 2. Hide across all standard console discovery surfaces
The implementation should not stop at removing sidebar labels. The same hidden sections must also disappear from topbar utilities and helper/previews that still imply the features are available.

Alternative considered: hide only sidebar entries.
Why not: users would still discover the hidden areas from other shell surfaces, leaving the console behavior inconsistent.

### 3. Normalize hidden hashes back to a visible destination
If a user reaches the console with a stale hash such as `#category-manage`, `#usage`, `#orders`, `#billing`, or `#docs`, the console should resolve to the default visible management destination instead of presenting a dead-end or partial hidden state.

Alternative considered: keep the hash but silently render whatever remains.
Why not: that makes the shell state misleading and complicates active navigation semantics.

### 4. Keep retained capabilities explicitly in scope
The proposal intentionally leaves `catalog-manage`, `credentials`, `api-call-logs`, and `unified-access-playground` visible so the hiding work stays narrow and does not accidentally regress still-supported operator workflows.

Alternative considered: broadly simplify the entire navigation model.
Why not: the user only requested hiding four specific sections and related exposure.

## Risks / Trade-offs

- [Risk] Hiding navigation without normalizing stale hashes can leave confusing landing states -> Mitigation: require fallback behavior for hidden destinations.
- [Risk] Removing only the sidebar can leave category management visible through workspace previews or helper cards -> Mitigation: explicitly cover supporting shell surfaces in the spec.
- [Risk] Unused locale keys may remain after the change -> Mitigation: keep cleanup optional unless the implementation chooses to remove dead references; the visibility requirement is about rendered exposure, not translation-file minimization.
- [Risk] Future re-enable work could become harder if code is physically removed -> Mitigation: frame the change as visibility control and avoid coupling it to backend or data-contract deletion.
