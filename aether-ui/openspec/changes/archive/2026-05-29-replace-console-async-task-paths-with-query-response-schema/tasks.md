## 1. Authority And Context

- [x] 1.1 Read and follow `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` and `aether-console/DESIGN.md` before implementation.
- [x] 1.2 Confirm backend authority API docs `../docs/api/api-asset-management.yaml`, `../docs/api/api-catalog-discovery.yaml`, and `../docs/api/api-import-agent.yaml` already document `asyncTaskConfig.queryResponseJsonSchema` and omit legacy path fields.
- [x] 1.3 Confirm no frontend authority document or `aether-console/DESIGN.md` update is required because this change reuses existing form/code-display/plan-card patterns.

## 2. Catalog API Models And Asset Editing

- [x] 2.1 Update catalog DTO/domain types so `AsyncTaskConfig` uses nullable `queryResponseJsonSchema` and removes `statusPath`, `resultPath`, and `errorPath`.
- [x] 2.2 Update current-user asset and Discovery API adapter mappings to read/write `queryResponseJsonSchema` and stop emitting legacy path fields.
- [x] 2.3 Update `useWorkspaceCatalog` form state initialization and revise request construction to replace async task path fields with the task query response schema field.
- [x] 2.4 Update the asset edit drawer UI and i18n copy to show a localized task query response schema field and remove status/result/error path inputs.

## 3. Marketplace Detail And Export

- [x] 3.1 Update Marketplace detail display to show task query response schema metadata when `asyncTaskConfig.queryResponseJsonSchema` is present.
- [x] 3.2 Update Markdown export to include task query endpoint, forwarding metadata, and the optional task query response schema.
- [x] 3.3 Remove Marketplace export dependence on path-derived async task response structure generation and ensure exports do not include legacy path fields.

## 4. Import Agent Workspace

- [x] 4.1 Update Import Agent DTO/domain types and adapter mapping to use `queryResponseJsonSchema` and remove legacy path fields.
- [x] 4.2 Update Import Agent plan card rendering to display the schema in an existing code/display surface and omit `statusPath`, `resultPath`, and `errorPath`.
- [x] 4.3 Update Import Agent workspace i18n copy for task query response schema labels and empty/optional schema wording.

## 5. Verification

- [x] 5.1 Update catalog asset API adapter tests for new async task schema mapping and request serialization.
- [x] 5.2 Update Discovery adapter and Marketplace export tests for schema display/export behavior and no legacy path output.
- [x] 5.3 Update `useWorkspaceCatalog` tests for loading, editing, clearing, and saving nullable `queryResponseJsonSchema`.
- [x] 5.4 Update Import Agent API/workspace tests for plan schema mapping and display.
- [x] 5.5 Run focused `aether-console` Vitest tests for catalog, workspace catalog, Marketplace export, and Import Agent.
- [x] 5.6 Run frontend type-check or build command required by the repo for the touched `aether-console` code.
