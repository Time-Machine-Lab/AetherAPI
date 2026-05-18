## Why

`aether-console` marketplace detail already receives `requestJsonSchema` and `responseJsonSchema`, but the current `JsonSchemaViewer` only wraps `CodeBlock` and renders raw JSON text. That makes large nested schemas hard to scan, especially when users need to understand required fields, value types, enum ranges, array items, and nested object structure at a glance.

For marketplace browsing, the schema block competes with request template, examples, async task configuration, and AI capability content. A large inline schema tree would consume too much vertical space, so the schema viewer should move into a dedicated inspection surface rather than expanding the detail page indefinitely.

## What Changes

- Add a visual JSON Schema inspection component for marketplace detail instead of relying on raw JSON-only display.
- Keep the detail page compact with request/response schema entry cards or buttons, and open a dedicated inspection surface when the user wants to inspect structure.
- Prefer a desktop dialog and a narrow-screen full-height sheet/drawer for the same schema viewer content, so large schemas have enough room without overwhelming the page layout.
- Render schema nodes in an expandable tree with clear metadata for required state, type, enum values, array item shape, nullable/default/format hints, and descriptions when present.
- Preserve raw JSON/text fallback and copy support for unsupported or invalid schema content.

## Capabilities

### New Capabilities

- `console-schema-visual-inspection`: reusable console behavior for inspecting JSON Schema through a visual tree with metadata and expand/collapse controls.

### Modified Capabilities

- `console-published-marketplace-discovery`: marketplace detail exposes request/response schema through a compact trigger and visual inspection overlay instead of raw JSON-only blocks.
- `console-json-code-display`: schema inspection builds on the existing JSON/code display rules and extends them with schema-specific visual structure.

## Impact

- No backend API contract change is required. The frontend continues to consume existing `requestJsonSchema` and `responseJsonSchema` fields from `docs/api/api-catalog-discovery.yaml`.
- Because this introduces a new reusable schema inspection pattern, `aether-console/DESIGN.md` should be updated before or alongside implementation as the UI authority artifact.
- Frontend app: `aether-console`.
- Likely frontend areas: `src/components/console/JsonSchemaViewer.vue` or a successor component, dialog/sheet wrappers, marketplace detail page, locale files, and focused component/page tests.