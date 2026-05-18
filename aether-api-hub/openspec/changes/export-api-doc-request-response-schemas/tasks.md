## 1. Dependency And Authority Alignment

- [ ] 1.1 Confirm `docs/api/api-catalog-discovery.yaml` remains the authority source for `requestJsonSchema` and `responseJsonSchema`, and do not duplicate those fields in this change.
- [ ] 1.2 If `add-api-asset-json-schemas` is still pending on the target branch, merge or sequence that dependency before export-layer implementation.

## 2. Export Rendering

- [ ] 2.1 Update the Discovery-backed API document export assembler or template to render a request schema section from `requestJsonSchema` when present.
- [ ] 2.2 Update the same export path to render a response schema section from `responseJsonSchema` when present.
- [ ] 2.3 Keep export output valid when one or both schema fields are null or blank, omitting only the missing sections and preserving existing example content.

## 3. Verification

- [ ] 3.1 Add focused tests for exported documents that include both request and response schema sections.
- [ ] 3.2 Add focused tests covering assets with only one schema snapshot present.
- [ ] 3.3 Add focused tests covering assets with no schema snapshots so export output stays compact and valid.