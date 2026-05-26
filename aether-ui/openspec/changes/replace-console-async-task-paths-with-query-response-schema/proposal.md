## Why

The backend async task contract now replaces `statusPath`, `resultPath`, and `errorPath` with `asyncTaskConfig.queryResponseJsonSchema`. `aether-console` still maps, edits, displays, and exports the legacy path fields, so owners and Marketplace users would see stale configuration that no longer matches the API authority documents.

## What Changes

- **BREAKING** Remove console use of async task `statusPath`, `resultPath`, and `errorPath` from API DTOs, domain types, form state, Marketplace detail display, Markdown export, and Import Agent plan cards.
- Add `queryResponseJsonSchema` to current-user asset, Discovery, and Import Agent async task DTO/type mappings.
- Update the asset edit drawer to load and submit a nullable task query response schema instead of JSONPath-style status/result/error paths.
- Update Marketplace detail and Markdown export to show the task query response schema as contract metadata, without generating response examples from JSONPath fields.
- Update Import Agent workspace plan rendering to display `asyncTaskConfig.queryResponseJsonSchema` and stop presenting legacy path fields.
- Preserve Unified Access task-query execution behavior: the schema is display/export metadata only, not frontend response parsing or validation logic.

## Capabilities

### New Capabilities

- `console-import-agent-async-task-response-schema`: Import Agent plan review in `aether-console` displays async task query response schema metadata and does not show legacy task path fields.

### Modified Capabilities

- `console-asset-async-task-config-form`: Asset edit drawer async task configuration replaces legacy path inputs with nullable `queryResponseJsonSchema`.
- `console-marketplace-doc-export`: Marketplace detail and exported Markdown include async task query response schema metadata instead of status/result/error paths or derived response structures.
- `console-unified-access-async-task-query-api`: Frontend catalog API mappings expose `queryResponseJsonSchema` as optional async task metadata while keeping task-query passthrough behavior unchanged.

## Impact

- Authority dependencies: backend API documents under `../docs/api/` already define `asyncTaskConfig.queryResponseJsonSchema`; no new backend API document is required for this frontend proposal.
- Frontend app: `aether-console` API DTOs/types, catalog/import-agent adapters, workspace asset edit state, Marketplace detail view, Markdown export helpers, Import Agent workspace plan card, and related i18n text.
- Tests: update Vitest coverage for catalog mapping, Discovery mapping, Import Agent mapping, asset config composable behavior, Marketplace export output, and async task response schema display.
- Compatibility: old persisted or mocked frontend data containing `statusPath`, `resultPath`, or `errorPath` should no longer be emitted by new requests; display code should tolerate missing `queryResponseJsonSchema`.
