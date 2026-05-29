## Why

API asset evolution is getting more expensive because every new platform first-class field tends to fan out across owner-scoped request DTOs, service commands, aggregate state, persistence entities, converters, and query models. Recent additions such as JSON Schema snapshots and async task configuration are valid product changes, but they also show that the current flat asset field shape makes future capability work harder than it needs to be.

Upcoming platform features such as AI testing, AI-driven document ingestion, and future policy controls need an additive place to land without forcing a broad top-level field expansion or a migration of already-stable asset fields. The project therefore needs explicit extension blocks on API assets that future features can consume incrementally while keeping the existing asset contract authoritative.

## What Changes

- Add three optional owner-scoped extension block properties to API assets: `capabilityExtensions`, `policyExtensions`, and `metadataExtensions`.
- Keep all existing first-class API asset fields unchanged and authoritative in this change; `aiProfile`, `asyncTaskConfig`, examples, and JSON Schema snapshots are not migrated into the new blocks.
- Allow current authenticated asset owners to create, revise, clear, and read the nullable extension blocks through the existing owner-scoped asset management contract in `docs/api/api-asset-management.yaml`.
- Persist the extension blocks on `api_asset` as nullable JSON text snapshots so future nested fields can be added inside a block without introducing new top-level asset fields immediately.
- Limit phase-one scope to owner asset management and storage. Discovery, Unified Access, publish readiness rules, and platform routing do not consume or expose the generic extension blocks in this change.

## Capabilities

### New Capabilities

- `catalog-asset-extension-blocks`: API Catalog stores owner-scoped capability, policy, and metadata extension blocks on an API asset as additive future-feature entry points.

### Modified Capabilities

- `catalog-owner-asset-management`: owner-scoped asset create, revise, and detail responses accept and return nullable extension blocks without changing existing top-level asset fields.

## Impact

- Authority documents:
  - Update `docs/sql/api-asset.sql`, the existing authority file for table `api_asset`, to add nullable storage for the three extension blocks. Keep using the existing authority file rather than creating a duplicate `api_asset.sql`.
  - Update `docs/api/api-asset-management.yaml`, mapped one-to-one to `ApiAssetController.java`, to document owner-scoped request and response fields for the extension blocks.
  - Update the API Catalog design document under `docs/design/aehter-api-hub/` to describe the extension-block boundary and to state that existing first-class fields remain authoritative.
  - Any changed `docs/api/` and `docs/sql/` authority files must be generated or updated with `tml-docs-spec-generate` using the API and SQL templates.
- Backend code:
  - Extend owner asset DTOs, delegates, service commands/models, and `ApiAssetAggregate` with the three nullable extension blocks while keeping current fields intact.
  - Extend persistence entities, converters, repositories, and owner detail mapping to round-trip the extension blocks.
  - Do not add Discovery or Unified Access contract fields for the generic extension blocks in this change.
- Database:
  - Add three nullable text columns on `api_asset` for `capabilityExtensions`, `policyExtensions`, and `metadataExtensions` storage.
- Tests:
  - Add focused coverage for create/revise/detail clear semantics, persistence round-trips, and non-exposure through Discovery or Unified Access.
- Dependency note:
  - This change touches the same API Catalog authority files as `add-api-asset-json-schemas`. If both changes land on the same branch, sequence or merge them carefully instead of redefining the same existing fields.