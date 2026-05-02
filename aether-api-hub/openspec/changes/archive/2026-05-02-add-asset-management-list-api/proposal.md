## Why

The API asset write model already supports draft registration, detail lookup, editing, enable/disable, and AI profile binding, but the management side still has no first-class list endpoint. This leaves the console unable to browse existing assets in bulk and forces operators to know an `apiCode` up front before they can manage an asset.

## What Changes

- Add a management-facing asset list capability for `ApiAssetController` so operators can browse existing assets without going through discovery-only market APIs.
- Define a paged list contract that can return draft, enabled, and disabled assets, with basic management filters such as status, category, and keyword.
- Reuse the existing `api_asset` write-model table and DDD layering rather than introducing a separate read store or a new controller family.
- Update the top-level API authority document `docs/api/api-asset-management.yaml` before implementation so the new list endpoint remains aligned with the existing asset-management controller contract. This document update must be generated through `tml-docs-spec-generate` with the API template.
- Keep the change limited to management-side list/query behavior; it does not alter discovery-market read APIs, asset lifecycle rules, or database table structure.

## Capabilities

### New Capabilities
- `asset-management-list-api`: Define the management-facing API asset list contract, filtering behavior, and pagination semantics for `ApiAssetController`.

### Modified Capabilities
- None.

## Impact

- Affected authority docs: `docs/api/api-asset-management.yaml`
- Affected code: `aether-api-hub-standard` modules under `api`, `adapter`, `service`, and `infrastructure` for asset management query flow
- Database impact: no new table or column changes are expected; existing `docs/sql/api-asset.sql` remains the storage authority unless implementation later discovers missing indexed query support
- Boundary note: this change must stay separate from `catalog-discovery-read-api`, because discovery only exposes enabled market assets while the management list must cover draft and disabled assets as well
