## 1. Documentation And Contract Boundary

- [x] 1.1 Reread `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, `aether-console/DESIGN.md`, and `../docs/api/api-catalog-discovery.yaml`; confirmed export content only uses Discovery detail fields and `/api/v1/access/{apiCode}` derivation.
- [x] 1.2 Confirmed this change adds no backend API, does not modify the Discovery contract, and does not require updates to top-level frontend specs or `aether-console/DESIGN.md`.

## 2. Markdown Generation And Download

- [x] 2.1 Added catalog document export helper that generates single API Markdown from `DiscoveryAssetDetail`, covering basic information, platform Unified Access URL, request method, auth scheme, request template, request example, response example, and AI capability.
- [x] 2.2 Added batch Markdown merge logic that preserves selection order and lists failed detail loads at the top of the file.
- [x] 2.3 Added Markdown file-name generation for single API and market batch export files.
- [x] 2.4 Added browser Markdown download helper using `Blob`, `URL.createObjectURL`, a temporary anchor, and object URL cleanup.

## 3. Marketplace Export State

- [x] 3.1 Added export selection state in a marketplace-domain composable, supporting toggle, clear, selected count, and separation from current detail selection.
- [x] 3.2 Implemented single-detail export with loading, success, and failure feedback.
- [x] 3.3 Implemented multi-select export with detail loading, success/failure collection, all-failed guard, and partial-failure summaries.
- [x] 3.4 Disabled related export actions while export is in progress.

## 4. API Marketplace UI Integration

- [x] 4.1 Added independent export selection checkbox on marketplace cards and stopped event propagation so card detail selection remains unchanged.
- [x] 4.2 Added selected-for-export visual state that stays distinct from current detail selection.
- [x] 4.3 Added batch export toolbar with selected count, clear selection, and export action.
- [x] 4.4 Added "Export API document" action to the detail panel.
- [x] 4.5 Added `zh-CN` and `en-US` i18n copy for loading, success, failure, all-failed, and partial-failure export feedback.

## 5. Tests And Verification

- [x] 5.1 Added unit tests for Markdown generation, optional fields, standard API, AI API, internal-field exclusion, and file names.
- [x] 5.2 Added unit tests for batch export orchestration, including all success, partial failure, all failure, in-progress guard, and selection order.
- [x] 5.3 Verified marketplace interaction behavior through composable tests and UI integration: card click remains detail selection, checkbox toggles export selection, and toolbar state follows selection count.
- [x] 5.4 Ran `aether-console` test, lint, type-check, and build. Full `format:check` remains blocked only by pre-existing unrelated formatting issues in `src/features/console/console-shell.spec.ts`, `src/features/console/console-shell.ts`, `src/pages/workspace.vue`, and `vite.config.ts`.
