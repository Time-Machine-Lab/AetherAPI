## 1. Authority Documents

- [x] 1.1 Update `docs/sql/api-asset.sql` for the `api_asset` table with nullable `request_json_schema` and `response_json_schema` columns.
- [x] 1.2 Update `docs/api/api-asset-management.yaml` for `ApiAssetController.java` request/response schema fields.
- [x] 1.3 Update `docs/api/api-catalog-discovery.yaml` for `CatalogDiscoveryController.java` detail response schema fields.

## 2. Backend Domain And Service

- [x] 2.1 Add request/response schema fields to API asset aggregate creation, revision, reconstitution, and accessors.
- [x] 2.2 Add request/response schema fields to owner asset commands/models and Discovery detail models.
- [x] 2.3 Preserve nullable schema values through create, revise, publish, and detail use cases.

## 3. Persistence And Web Contracts

- [x] 3.1 Add request/response schema fields to `ApiAssetDo`, converters, query records, mapper SQL, and repository persistence.
- [x] 3.2 Add request/response schema fields to public Req/Resp DTOs.
- [x] 3.3 Map schema fields in `ApiAssetWebDelegate` and `CatalogDiscoveryWebDelegate`.

## 4. Verification

- [x] 4.1 Add or update focused backend tests for owner asset create/revise/detail schema mapping.
- [x] 4.2 Add or update focused backend tests for Discovery detail schema projection and list omission.
- [x] 4.3 Run targeted Maven tests for affected catalog service/adapter/infrastructure code.
