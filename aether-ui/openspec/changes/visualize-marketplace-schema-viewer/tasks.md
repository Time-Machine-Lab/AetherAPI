## 1. Authority And Design Context

- [ ] 1.1 Update `aether-console/DESIGN.md` to codify the schema inspection pattern, including overlay choice, tree hierarchy, metadata badges, and expand/collapse behavior.
- [ ] 1.2 Confirm implementation continues to consume only existing contract-backed `requestJsonSchema` and `responseJsonSchema` fields from Discovery detail, with no new `docs/api/` changes.

## 2. Shared Schema Inspection Component

- [ ] 2.1 Replace the current raw-JSON-only `JsonSchemaViewer` behavior with a schema-specific visual inspection component or wrapper that can parse common JSON Schema structure into display nodes.
- [ ] 2.2 Support visual metadata for required state, type, enum values, array items, description, and common hints such as `format`, `default`, or `nullable` when present.
- [ ] 2.3 Support field expand/collapse behavior and preserve a raw JSON/text fallback plus copy action for invalid or unsupported schema content.
- [ ] 2.4 Use a desktop dialog and a narrow-screen full-height sheet/drawer for the main inspection surface.

## 3. Marketplace Detail Integration

- [ ] 3.1 Keep marketplace detail compact with request/response schema summary triggers instead of rendering the full schema tree inline.
- [ ] 3.2 Open the visual schema inspection surface from marketplace detail for request and response schema independently.
- [ ] 3.3 Keep absent schema behavior stable and do not infer schema from request templates or examples.
- [ ] 3.4 Add i18n labels and helper copy for schema metadata, overlay actions, fallback mode, and empty states.

## 4. Verification

- [ ] 4.1 Add focused component tests for schema parsing, metadata rendering, and expand/collapse behavior.
- [ ] 4.2 Add focused tests for invalid/unsupported schema fallback to raw JSON/text display.
- [ ] 4.3 Add focused marketplace detail tests for opening the schema viewer and handling absent request/response schema.
- [ ] 4.4 Run targeted `aether-console` tests and type checks for the touched schema-viewer and marketplace detail slices.