## Context

`aether-console` already maps current-user asset management and Discovery detail through `src/api/catalog/*`, renders examples with `CodeBlock`, and follows `aether-console/DESIGN.md` section 11 for display components. The backend change adds nullable `requestJsonSchema` and `responseJsonSchema` to owner asset APIs and published Discovery detail.

The API authority documents must be updated before frontend code relies on the fields: `../docs/api/api-asset-management.yaml` and `../docs/api/api-catalog-discovery.yaml`.

## Goals / Non-Goals

**Goals:**

- Map nullable schema fields through the typed API layer and frontend domain types.
- Let asset owners edit and clear request/response JSON Schema fields in the existing asset edit drawer.
- Show schemas in owner asset detail and marketplace detail through one reusable component.
- Keep display behavior aligned with `CodeBlock`: format valid JSON, preserve plain text when invalid, expose copy, and show empty state when absent.

**Non-Goals:**

- No in-browser JSON Schema validation against sample payloads.
- No visual schema graph/tree editor.
- No schema dialect selection UI.
- No backend contract invention if the authority YAML does not expose the fields.

## Decisions

### 1. Add `JsonSchemaViewer` as a console display component

Create a dedicated component under `src/components/console/JsonSchemaViewer.vue`. It wraps existing formatting/copy conventions and state feedback, but names the behavior as schema-specific so asset and marketplace surfaces do not duplicate parsing and empty-state logic.

Alternative considered: reuse `CodeBlock` directly everywhere. Rejected because the requirement asks for a schema display component, and a thin wrapper gives consistent headings, nullable behavior, and future schema-specific affordances.

### 2. Edit schemas as text areas in the existing asset drawer

The backend stores schema snapshots as optional text. The edit drawer should provide two multiline fields for request and response JSON Schema. Blank values are normalized to `null`; non-blank values are sent as entered.

### 3. Display schemas only when contract-backed fields are present

Owner detail uses current-user asset detail fields. Marketplace detail uses Discovery detail fields. The UI must not infer schema from examples or request templates.

### 4. Keep API calls in the API/composable layers

DTO changes stay in `src/api/catalog`, save orchestration stays in `useWorkspaceCatalog`, and page/components consume mapped models. No page-level raw request handling is introduced.

## Risks / Trade-offs

- [Risk] Users paste invalid JSON. -> Mitigation: display falls back to plain text and edit fields do not block unless the backend rejects the value.
- [Risk] Long schemas make detail panels hard to scan. -> Mitigation: the viewer uses the existing compact code display pattern and supports copy, with empty states for absent schemas.
- [Risk] Marketplace may render fields before backend deployment. -> Mitigation: mappings treat missing values as null and render stable empty states.

## Open Questions

- None. Schema validation and graphical schema browsing are reserved for a future enhancement.
