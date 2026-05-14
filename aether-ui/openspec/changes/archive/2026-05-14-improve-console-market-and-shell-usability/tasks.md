## 1. Authority And Design Alignment

- [x] 1.1 Re-check `../docs/api/api-catalog-discovery.yaml`, `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, and `aether-console/DESIGN.md` to confirm the market-detail contract and shell/layout rules before implementation
- [x] 1.2 If runtime discovery payloads still drift from `api-catalog-discovery.yaml`, update the authority mapping or blocking notes first instead of implementing undocumented frontend assumptions

## 2. API Market Usability

- [x] 2.1 Update the discovery DTO/type mapping and market detail composition so request method and structured request/response example snapshots follow the authority contract
- [x] 2.2 Redesign the API Market card and detail metadata layout to improve information density while staying within the current console design system

## 3. Workspace Form Clarity

- [x] 3.1 Add visible labels for the asset lookup, registration, configuration, and AI profile form controls in `console-workspace`
- [x] 3.2 Keep placeholders, spacing, and field grouping aligned with the current field/surface grammar after labels are introduced

## 4. Sidebar Shell Usability

- [x] 4.1 Update `ConsoleLayout.vue` so the left sidebar remains available during page scroll and becomes independently scrollable when content overflows
- [x] 4.2 Add an expand/collapse interaction for the sidebar that preserves icon navigation, active-state clarity, and retained route/hash behavior

## 5. Verification

- [x] 5.1 Add or update tests for discovery contract mapping, market-detail rendering expectations, and shell/sidebar behavior where feasible
- [x] 5.2 Run the relevant frontend validation commands and confirm the updated market, workspace, and shell experiences do not regress existing retained console paths
