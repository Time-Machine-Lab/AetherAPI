## 1. Authority Alignment

- [ ] 1.1 Read `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, `aether-console/DESIGN.md`, `docs/api/api-credential.yaml`, and the Consumer & Auth domain design document before implementation begins.
- [ ] 1.2 If implementation needs new unified-access calling examples, API fields, or credential-specific visual rules that are not already covered, update the corresponding authority document first (`docs/api/*.yaml` via `tml-docs-spec-generate` when applicable, or `aether-console/DESIGN.md`) before writing feature code.

## 2. Credential API Integration

- [ ] 2.1 Add a credential-focused API module, DTO mapping, and error normalization in `aether-console` aligned with `docs/api/api-credential.yaml`.
- [ ] 2.2 Add page-scoped composition helpers for credential filtering, selection, refresh, lifecycle actions, and one-time plaintext reveal handling without introducing a new global business store.

## 3. Credential Workspace UI

- [ ] 3.1 Replace the `#credentials` placeholder with a real protected workspace section that supports API Key list browsing, detail viewing, and empty / loading / error states.
- [ ] 3.2 Implement the create API Key flow with one-time plaintext reveal, security notice copy, and masked-only follow-up behavior.
- [ ] 3.3 Implement enable, disable, and revoke actions with state-aware affordances and post-action refresh.
- [ ] 3.4 Render status, expiration, revocation, and `lastUsedSnapshot` information without fabricating local usage history.

## 4. Guidance and Localization

- [ ] 4.1 Add credential guidance content that explains the current-user API Key model, the hidden-`Consumer` boundary, and where official calling docs live.
- [ ] 4.2 Add or update `zh-CN` and `en-US` locale resources for credential navigation, forms, notices, guidance, empty states, and recoverable errors.

## 5. Verification

- [ ] 5.1 Add tests for credential workspace rendering, one-time plaintext reveal behavior, lifecycle action state transitions, and locale-backed guidance copy.
- [ ] 5.2 Run `lint`, `type-check`, and `build` for `aether-console`, and document any remaining backend-dependent verification gaps if the full contract is not yet available.
