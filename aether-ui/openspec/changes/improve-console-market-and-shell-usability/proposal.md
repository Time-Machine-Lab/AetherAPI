## Why

`aether-console` currently has three usability gaps in the main operator flow. The API Market page renders very sparse cards and detail content even though the discovery authority document already defines richer display fields, the asset-management forms rely too heavily on placeholders so prefilled data becomes hard to identify, and the left navigation scrolls away with the page while offering no expand/collapse control.

These issues now directly affect discoverability, editing confidence, and shell navigation efficiency. The change needs to tighten the console around the existing frontend stack, the current `aether-console/DESIGN.md`, and the authority contract in `../docs/api/api-catalog-discovery.yaml`, rather than inventing new backend behavior.

## What Changes

- Enrich the API Market list and detail experience so cards no longer look visually empty and detail content exposes the documented discovery metadata more clearly.
- Align the API Market frontend model with the authority discovery contract, especially around request-method display and structured request/response example snapshots.
- Add persistent visible labels to asset-management form controls so operators can understand fields even when values are already filled in.
- Make the console sidebar remain available during page scroll and add a shell-level expand/collapse interaction without redesigning the retained navigation model.
- Keep the scope frontend-only in `aether-console`; no new backend business flow is introduced by this proposal.

## Capabilities

### New Capabilities
- `console-market-discovery-usability`: Define the minimum information density and contract-aligned detail presentation for the API Market list and detail panel.
- `console-asset-form-clarity`: Define labeling and field-recognition rules for the asset-management forms in `console-workspace`.
- `console-shell-sidebar-usability`: Define sticky and collapsible shell-navigation behavior for the left sidebar.

### Modified Capabilities
- None.

## Impact

- Affected app: `aether-console`
- Affected frontend areas: `src/pages/index.vue`, `src/pages/workspace.vue`, `src/layouts/ConsoleLayout.vue`, discovery API DTO/type mapping, locales, and related UI tests
- Authority references: `../docs/api/api-catalog-discovery.yaml`, `aether-console/DESIGN.md`, and `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`
- Boundary note: this proposal assumes the existing discovery authority document remains the source of truth; if runtime payloads still diverge from `api-catalog-discovery.yaml`, implementation must first resolve that authority mismatch instead of hard-coding undocumented frontend assumptions
- Context gap noted during proposal: `aether-ui/openspec/project.md` is not present, so this proposal is grounded in `openspec/config.yaml`, authority docs, and current `aether-console` code
