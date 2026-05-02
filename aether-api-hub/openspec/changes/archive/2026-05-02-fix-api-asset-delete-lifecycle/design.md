## Context

BUG-001 comes from the API Catalog current-user asset lifecycle. The authoritative design states that deletion is a soft-delete marker and is not mixed into the `DRAFT / PUBLISHED / UNPUBLISHED` status enum. The authoritative SQL already has `api_asset.is_deleted`, and the authoritative API already defines `DELETE /current-user/assets/{apiCode}` in `docs/api/api-asset-management.yaml` for `ApiAssetController.java`.

The current implementation path appears to call `ApiAssetAggregate.softDelete()` through `ApiAssetApplicationService.deleteAsset()`, but the observed behavior indicates that the delete marker is not consistently persisted or read paths still expose the row after deletion. `ApiAssetDo.isDeleted` is also annotated with MyBatis-Plus `@TableLogic`, so the repository update path must be verified carefully rather than assuming a normal `updateById` will always write the desired logical-delete transition.

## Goals / Non-Goals

**Goals:**

- Make owner asset deletion a persistent `is_deleted = true` transition for `api_asset`.
- Keep deletion separate from publication state; deleting a published asset must not merely turn it into an unpublished active asset.
- Ensure active read paths exclude deleted assets: current-user management list/detail, Catalog Discovery, and Unified Access target resolution.
- Add regression tests that reproduce the refresh-after-delete bug for draft, unpublished, and published assets.

**Non-Goals:**

- Do not add a hard-delete operation.
- Do not introduce a new asset status such as `DELETED`.
- Do not redesign the asset management API shape unless implementation proves the existing response contract is ambiguous; if it is changed, update `docs/api/api-asset-management.yaml` first through `tml-docs-spec-generate` with the API template.
- Do not change `docs/sql/api-asset.sql` unless implementation finds that the current `is_deleted` column definition is insufficient; any SQL authority update must use `tml-docs-spec-generate` with the SQL template.

## Decisions

1. Deletion remains a domain lifecycle action on `ApiAssetAggregate`.

   Rationale: API Catalog owns asset lifecycle rules, and the architecture says Controller, Mapper, and adapters must not own business rules. The aggregate should set the deletion marker and prevent further lifecycle operations after deletion.

   Alternative considered: perform deletion directly in the repository with a mapper update. That would risk bypassing owner checks and aggregate invariants, so it is only acceptable as a persistence mechanism after the application service has loaded and mutated the aggregate.

2. `is_deleted` is the only active removal marker.

   Rationale: `UNPUBLISHED` is reversible and still belongs to active owner workflows. Deleted assets are terminal for active management, discovery, and invocation workflows. A published asset may have its `published_at` cleared during deletion, but active visibility must be controlled by `is_deleted = true`, not by status alone.

   Alternative considered: convert deletes to unpublish. That matches the observed bad behavior and conflicts with the design document, so it is rejected.

3. Repository persistence must explicitly support the logical-delete transition.

   Rationale: `ApiAssetDo.isDeleted` uses `@TableLogic`. The implementation must verify whether `updateById` can persist a transition from false to true in this project configuration. If not, add an explicit mapper method for the owner-validated delete persistence path that updates `is_deleted`, `updated_at`, and optimistic version data without exposing deleted rows to normal reads.

   Alternative considered: remove `@TableLogic`. That would affect all MyBatis-Plus behavior for this table and is too broad for this bugfix.

4. Read-side filtering stays defensive.

   Rationale: `docs/design` requires Discovery and Unified Access to consume only published and non-deleted assets. Current-user management should also only list non-deleted assets. Even after persistence is fixed, query mappers and repository lookup methods should continue filtering `is_deleted = FALSE` for active reads.

   Alternative considered: rely only on MyBatis-Plus global logical-delete filtering. Existing query mappers use hand-written SQL in several places, so explicit filters remain clearer and safer.

## Risks / Trade-offs

- [Risk] Returning the deleted asset body after delete may confuse callers because the row is no longer available through normal detail reads. -> Mitigation: keep the existing API response shape for compatibility, but ensure the returned model has `deleted = true`; if this contract is changed, update `docs/api/api-asset-management.yaml` first.
- [Risk] Logic-delete behavior may conflict with optimistic locking. -> Mitigation: cover repository-level delete persistence with tests that assert the row is hidden from active lookups and visible only through including-deleted lookups when such a port exists.
- [Risk] Existing tests may assert that published delete becomes unpublished. -> Mitigation: update those tests to assert the separate deletion marker and active-read invisibility instead.
- [Risk] Discovery and Unified Access may already filter deleted rows, hiding the bug in service tests only. -> Mitigation: include end-to-end service or controller tests that delete then immediately list/detail/resolve the same `apiCode`.

## Migration Plan

No schema migration is expected. Deploy as an application behavior fix against the existing `api_asset.is_deleted` column.

If rollback is needed, revert the application change only. No data rollback is required because rows marked deleted are consistent with the documented lifecycle; restoring a mistakenly deleted asset would be a manual operational action outside this proposal.

## Open Questions

- Should the delete endpoint continue returning `ApiAssetResp`, or should a later API cleanup switch it to an empty success response? This proposal keeps the current documented response shape to avoid expanding the bugfix.
