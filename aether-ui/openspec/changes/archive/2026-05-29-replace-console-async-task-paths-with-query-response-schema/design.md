## Context

The backend authority documents now define async task configuration through `asyncTaskConfig.queryResponseJsonSchema` and intentionally remove `statusPath`, `resultPath`, and `errorPath`. The console currently still carries those legacy fields across `src/api/catalog`, `src/api/import-agent`, `useWorkspaceCatalog`, Marketplace detail/export helpers, and Import Agent plan rendering.

`aether-console` uses Vue 3, TypeScript, Vite, Tailwind CSS, shadcn-vue-style primitives, API adapter modules under `src/api`, composables under `src/composables`, feature logic under `src/features`, and localized copy under `src/locales`. The implementation must stay within those layers and follow the existing console display component patterns from `DESIGN.md`.

No frontend authority design document needs to change before implementation because the backend API documents under `../docs/api/` have already been updated. This proposal changes frontend consumption of those contracts.

## Goals / Non-Goals

**Goals:**

- Replace all user-facing and request-emitting async task path fields with `queryResponseJsonSchema`.
- Keep asset owner configuration, Discovery detail, Marketplace export, and Import Agent plan review aligned with backend contracts.
- Preserve Unified Access task-query passthrough behavior; the frontend must not validate or transform task query responses using the schema.
- Keep UI behavior accessible and localized, using existing field groups, code display surfaces, and plan card patterns.

**Non-Goals:**

- Do not introduce a visual schema editor or tree viewer as part of this change.
- Do not infer schema from legacy paths in frontend code.
- Do not add runtime JSON Schema validation for task query responses.
- Do not update backend API documents or backend code in this frontend change.

## Decisions

### Decision 1: Treat `queryResponseJsonSchema` as a string snapshot in frontend models

DTOs and domain types will model `queryResponseJsonSchema` as `string | null | undefined`, matching the existing request/response schema fields. API adapters should map it directly and stop mapping path fields.

Rationale: backend contracts store and expose schema snapshots as JSON strings, and the console already has patterns for request/response schema text.

Alternative considered: parse into a structured JSON object in the API layer. This would increase adapter responsibility and create parse failures for metadata that should remain optional display content.

### Decision 2: Replace path inputs with one schema textarea/code-style field

The asset edit drawer should keep the existing async task group and core forwarding fields (`enabled`, `queryMethod`, `queryUrlTemplate`, `authMode`, `authScheme`, `authConfig`), then replace status/result/error path controls with a nullable task query response schema field.

Rationale: owners need to submit the new contract field without learning JSONPath-style extraction. The schema belongs near the async task query endpoint fields.

Alternative considered: keep hidden backward-compatible path fields in the form. That would keep emitting removed contract fields and confuse users.

### Decision 3: Marketplace export uses schema verbatim, not generated path examples

Markdown export should include the platform task query endpoint, method/auth metadata, and a task query response schema block when present. The existing helper that builds response examples from JSONPath paths should be retired or left unused by export.

Rationale: the backend no longer treats paths as the source of truth, and the schema can represent the full task-query body rather than three leaves.

Alternative considered: generate a sample JSON object from JSON Schema. This is useful later but risks inventing values and is outside the current contract-alignment scope.

### Decision 4: Import Agent plan card mirrors the new async task contract

Import Agent plan display should show query method, query URL, auth mode/scheme/config as it does today, and add a code/display block for `queryResponseJsonSchema`. It should remove the legacy path rows.

Rationale: the plan review card is a contract review surface. Showing removed fields would make generated plans look stale even after backend planning changes.

Alternative considered: show both schema and paths for legacy sessions. This change intentionally moves the UI away from old path semantics; older sessions without schema simply omit the optional schema block.

## Risks / Trade-offs

- Legacy local mocks and tests still contain path fields -> Update fixtures and add mapping tests that prove new requests omit legacy fields.
- Large schema strings may make plan cards or detail panels noisy -> Use existing `CodeBlock`/preformatted display surfaces with copy/overflow behavior rather than inline text rows.
- Users may assume schema changes task-query execution -> Field labels and export text should describe it as a response schema snapshot, while task-query API behavior remains passthrough.
- Removing path-derived response examples reduces convenience -> This is intentional to avoid exporting invented structures from removed fields.

## Migration Plan

1. Update frontend DTOs/types and API adapters for catalog assets, Discovery detail, and Import Agent plans.
2. Update workspace asset edit state, save request construction, and localized field labels.
3. Update Marketplace detail/export and remove usage of path-derived response structure generation.
4. Update Import Agent plan rendering and related tests.
5. Run focused Vitest tests for catalog/import-agent adapters, workspace catalog composable, Marketplace export helpers, and Import Agent workspace behavior.

Rollback is limited to frontend display and request construction. If rolled back while backend remains on the new contract, asset async task schema editing/export would regress to stale path fields.

## Open Questions

- Should a future change add a dedicated JSON Schema viewer/editor for task-query response schemas, sharing the existing Marketplace schema viewer patterns?
