## Context

The current asset write model stores normal asset configuration and AI capability metadata in the same `api_asset` aggregate/table. The AI-profile binding endpoint is a partial update operation, but the console form may refresh itself from the response. If the backend response is partial, or if service/converter code accidentally drops existing fields while attaching AI metadata, the frontend can appear to lose the user's asset configuration.

Existing constraints:

- `docs/api/api-asset-management.yaml` is the authority contract for `ApiAssetController.java`.
- `docs/sql/api-asset.sql` already contains normal asset fields and AI capability fields.
- The fix should stay in Catalog asset management and should not change Unified Access, Discovery, or Observability behavior.

## Goals / Non-Goals

**Goals:**

- Make AI profile binding a safe partial update that preserves all non-AI asset configuration.
- Ensure backend response mapping returns the complete asset representation expected by the console.
- Add focused regression coverage for service, delegate/DTO mapping, and persistence conversion where needed.

**Non-Goals:**

- Redesigning the asset edit form.
- Changing asset publication rules.
- Adding new AI provider management features.
- Adding or changing database columns unless investigation proves a mapping gap.

## Decisions

### 1. Treat AI profile binding as a partial aggregate update

`attachAiCapabilityProfile` should update only the AI capability profile and metadata that is explicitly part of the operation. Existing upstream config, examples, request template, category, status, and deletion state must be retained.

### 2. Return a full asset response

The endpoint should return the same complete asset shape as normal asset detail/update operations. This keeps the frontend free to refresh form state from the response without losing fields.

### 3. Prefer tests before contract changes

If `docs/api/api-asset-management.yaml` already specifies a full `ApiAssetResp`, no contract change is required. If it is ambiguous, update that single API authority file before implementation.

## Risks / Trade-offs

- [Risk] The actual bug may be frontend state replacement rather than backend data loss. -> Mitigation: add backend response completeness tests first; if they pass, document the backend finding and route the remaining fix to frontend.
- [Risk] Persistence converter tests may reveal broader mapping drift. -> Mitigation: keep changes limited to asset field preservation and avoid unrelated DTO reshaping.

## Migration Plan

1. Reproduce the AI-profile binding flow against service or Web tests with an asset that already has category, upstream, template, and examples.
2. Fix any aggregate/service/converter/DTO mapping that drops existing fields.
3. Update `docs/api/api-asset-management.yaml` only if the full-response contract needs clarification.
4. Run focused asset domain, service, adapter, and infrastructure tests.
