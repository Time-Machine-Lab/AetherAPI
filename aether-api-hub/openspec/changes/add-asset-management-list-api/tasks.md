## 1. Authority Contract

- [x] 1.1 Update `docs/api/api-asset-management.yaml` for `ApiAssetController` to add the management-side list endpoint and its page/filter/item schemas using `tml-docs-spec-generate`
- [x] 1.2 Confirm whether `docs/sql/api-asset.sql` needs no change; if implementation requires a new index, update that single-table authority doc before backend code changes

## 2. Backend Query Flow

- [x] 2.1 Add the adapter-level list endpoint and request/response DTOs for paged asset management browsing
- [x] 2.2 Add service-layer query orchestration and page/filter models for the new list use case
- [x] 2.3 Implement infrastructure query logic over `api_asset` and category lookup data for pagination and management filters

## 3. Verification

- [x] 3.1 Add or update backend tests covering mixed lifecycle visibility, filters, pagination, and validation failures
- [x] 3.2 Verify the new management list contract stays separate from discovery-market behavior and does not regress existing asset detail/lifecycle endpoints
