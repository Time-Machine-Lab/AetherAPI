## 1. Readiness and Contract Alignment

- [x] 1.1 Re-read `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, `aether-ui/aether-console/DESIGN.md`, and `docs/api/unified-access.yaml` before implementation.
- [x] 1.2 Confirm this change remains frontend-only and does not require updates to `docs/api/*.yaml`, the shared frontend spec document, or `aether-console/DESIGN.md`.
- [x] 1.3 Inspect the current Unified Access playground page, composable, API client, code-display components, and i18n structure to choose the smallest compatible integration point.

## 2. Curl Formatter Logic

- [x] 2.1 Add a typed curl formatter utility or composable that accepts normalized playground command inputs and returns Linux/macOS and Windows command strings.
- [x] 2.2 Implement path, method, `X-Aether-Api-Key`, optional header, and body handling according to `docs/api/unified-access.yaml` and existing playground invocation rules.
- [x] 2.3 Implement shell-safe quoting and line continuation for both Linux/macOS and Windows formats.
- [x] 2.4 Add unit tests covering required-input validation, manual `apiCode`, optional headers, no-body methods, body-capable methods, and escaping edge cases.

## 3. Playground UI Integration

- [x] 3.1 Wire the formatter into the existing Unified Access playground state without changing invocation execution, target assist, subscription guidance, or API Key lifecycle behavior.
- [x] 3.2 Add UI controls for selecting Linux/macOS or Windows format, previewing the generated command, and copying the selected command.
- [x] 3.3 Disable or guard copy actions when required command inputs are missing, and render internationalized missing-input feedback.
- [x] 3.4 Add clipboard success and failure feedback while keeping the generated command preview inspectable.
- [x] 3.5 Add or update `zh-CN` and `en-US` i18n resources for all visible labels, descriptions, and feedback.

## 4. Verification

- [x] 4.1 Run the focused formatter/composable tests and any existing playground tests affected by the change.
- [x] 4.2 Run the relevant `aether-console` type-check or build command available in the package scripts.
- [x] 4.3 Manually inspect the playground UI at desktop and narrow widths to verify command preview, format switcher, copy actions, and feedback align with `aether-console/DESIGN.md`.
- [x] 4.4 Run OpenSpec validation for `add-unified-access-playground-copy-curl`.
