## Why

`aether-console` already reserves a `credentials` navigation slot, but it still lacks a real developer-facing API Key workflow. The backend changes `2026-04-17-consumer-auth-key-management` and `2026-04-17-consumer-auth-unified-access-auth` now define the credential contract, the hidden `Consumer` model, and the last-used snapshot semantics, so the frontend needs a matching proposal that turns those authority documents into a usable console experience without inventing new auth behavior.

## What Changes

- Add an `aether-console` credential workspace for the current signed-in user, aligned with `docs/api/api-credential.yaml`, covering API Key creation, masked list browsing, detail viewing, and enable / disable / revoke actions.
- Add a one-time plaintext key reveal experience with explicit security guidance, ensuring the console never pretends that a full API Key can be fetched again after creation.
- Add credential status, expiration, and `lastUsedSnapshot` visibility in the console so developers can understand whether a key is usable and whether unified access has already consumed it.
- Reuse the existing `credentials` navigation entry, `ConsoleLayout`, API layer, i18n structure, and console semantic roles from `aether-console/DESIGN.md`; do not introduce page-layer raw requests, new global business stores, or an explicit `Consumer` management surface.
- Add bounded credential guidance in the console that explains the current-user API Key model and points developers to existing authority docs; if a future implementation needs new unified-access request examples or new visual rules, the corresponding `docs/api/*.yaml` or `aether-console/DESIGN.md` must be updated first.

## Capabilities

### New Capabilities

- `console-api-key-management`: Define the current-user API Key management workflow in `aether-console`, including create, list, detail, lifecycle actions, one-time plaintext reveal, and last-used snapshot display.
- `console-api-key-guidance`: Define the console-side credential guidance experience, including hidden-Consumer messaging, security reminders, and bounded usage guidance that does not invent undocumented unified-access request contracts.

### Modified Capabilities

- None.

## Impact

- Affected application: `aether-console`
- Affected frontend areas: `src/pages`, `src/features`, `src/api`, `src/composables`, `src/locales`, and console workspace navigation / notices
- Consumed authority documents: `docs/api/api-credential.yaml`, `docs/design/aehter-api-hub/Aether API Hub Consumer & Auth领域设计文档.md`, `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, and `aether-console/DESIGN.md`
- Upstream dependency: backend delivery of the archived `consumer-auth-key-management` contract and the `consumer-auth-unified-access-auth` semantics for `lastUsedSnapshot`
- No new frontend authority document changes are assumed by this proposal; if implementation discovers gaps in API contract, auth guidance, or console visual rules, those top-level documents must be updated before code work begins
