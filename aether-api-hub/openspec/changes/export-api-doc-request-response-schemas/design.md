## Context

The project already persists request and response JSON Schema snapshots on API assets and exposes them through the published Discovery detail contract. The authority file `docs/api/api-catalog-discovery.yaml` includes `requestJsonSchema` and `responseJsonSchema`, and backend DTO/query code already carries those values. The remaining gap is the export step that turns a published Discovery detail into a user-facing API document.

This change must stay within the existing authority boundary. It should not introduce a second schema source, a parallel export-only DTO field, or any new database column. If the active `add-api-asset-json-schemas` change is not present on the target branch yet, this export change depends on that field exposure work landing first.

## Goals / Non-Goals

**Goals:**

- Show request and response schema snapshots in exported API documents when Discovery detail contains them.
- Keep exported documents valid when one or both schema snapshots are absent.
- Preserve existing example sections and other published asset metadata.

**Non-Goals:**

- No new schema persistence fields or Discovery API fields.
- No JSON Schema validation, formatting normalization, or dialect conversion.
- No change to Discovery list responses.
- No change to Unified Access forwarding or subscription behavior.

## Decisions

### 1. Reuse Discovery detail as the only export source

Export rendering reads `requestJsonSchema` and `responseJsonSchema` from the existing published Discovery detail model. This keeps `docs/api/api-catalog-discovery.yaml` as the single authority source for exported contract content and avoids creating export-only data flow.

### 2. Render schema sections independently

Request and response schema sections are rendered independently. If only one schema snapshot is present, the export includes only that section and omits the missing section without failing the document export.

### 3. Preserve example snapshots alongside schemas

Schema sections complement existing example sections; they do not replace them. Exported documents should continue to show example snapshots when available so consumers get both machine-readable structure and human-readable samples.

## Risks / Trade-offs

- [Risk] Export output becomes verbose for large schema snapshots. -> Mitigation: keep rendering limited to detail export only and omit empty sections.
- [Risk] Divergence if export introduces its own field names or source mapping. -> Mitigation: require the export path to consume the existing Discovery field names directly.
- [Risk] Branch conflict with `add-api-asset-json-schemas`. -> Mitigation: declare the dependency explicitly and do not duplicate schema-field authority changes in this proposal.

## Open Questions

- None for this change. Schema pretty-printing or alternate export formats can be proposed separately if needed.