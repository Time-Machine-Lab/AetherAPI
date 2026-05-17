## 1. Authority And Design Context

- [x] 1.1 Confirm backend authority YAML files expose `requestJsonSchema` and `responseJsonSchema` in owner asset and Discovery detail contracts.
- [x] 1.2 Confirm implementation remains aligned with `aether-console/DESIGN.md` JSON/code display and state feedback rules.

## 2. API Mapping And Workspace State

- [x] 2.1 Add schema fields to catalog DTOs, frontend types, and mapping functions.
- [x] 2.2 Add schema edit state to `useWorkspaceCatalog`, prefill from selected asset detail, and normalize blank values to null.
- [x] 2.3 Include schema fields in revise asset requests and preserve returned schema values in workspace state.

## 3. Schema Display Component And UI

- [x] 3.1 Implement reusable `JsonSchemaViewer` using existing console code display, copy, empty, and fallback patterns.
- [x] 3.2 Add request/response schema edit fields to the asset edit drawer.
- [x] 3.3 Render request/response schemas in owner asset detail through `JsonSchemaViewer`.
- [x] 3.4 Render request/response schemas in marketplace detail through `JsonSchemaViewer`.
- [x] 3.5 Add zh-CN and en-US i18n labels, helper text, empty states, and fallback copy.

## 4. Verification

- [x] 4.1 Add/update API mapping tests for owner asset and Discovery schema fields.
- [x] 4.2 Add/update workspace composable tests for schema prefill, save, and clearing behavior.
- [x] 4.3 Add/update component or UI tests for JSON formatting, plain-text fallback, and empty schema state.
- [x] 4.4 Run targeted `aether-console` tests and type checks.
