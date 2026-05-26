## 1. Authority Documents

- [x] 1.1 Read and follow the backend development conventions under `docs/spec/` before implementation.
- [x] 1.2 Use `tml-docs-spec-generate` with the SQL template to update `docs/sql/api-asset.sql` for table `api_asset`, documenting `async_task_config` JSON with nullable `queryResponseJsonSchema` and no new legacy path members.
- [x] 1.3 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-asset-management.yaml` mapped one-to-one to `ApiAssetController.java`, replacing async task `statusPath`/`resultPath`/`errorPath` with `queryResponseJsonSchema`.
- [x] 1.4 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-catalog-discovery.yaml` mapped one-to-one to `CatalogDiscoveryController.java`, exposing published `asyncTaskConfig.queryResponseJsonSchema` and omitting legacy path fields.
- [x] 1.5 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-import-agent.yaml` mapped one-to-one to `ApiImportAgentController.java`, updating Import Agent plan async task config fields.

## 2. Domain And Service Models

- [x] 2.1 Replace async task path fields with nullable `queryResponseJsonSchema` in `AsyncTaskConfig` and its validation/equality/accessor logic.
- [x] 2.2 Replace async task path fields with nullable `queryResponseJsonSchema` in `AsyncTaskConfigModel` and all constructor call sites.
- [x] 2.3 Update catalog service create/revise/detail mapping so owner asset workflows preserve `queryResponseJsonSchema`.
- [x] 2.4 Update Unified Access target snapshot mapping so task-query forwarding behavior is unchanged and schema metadata is non-executing.

## 3. Persistence And Web Contracts

- [x] 3.1 Update `ApiAssetConverter` async task JSON serialization to emit `queryResponseJsonSchema` and stop emitting `statusPath`, `resultPath`, or `errorPath`.
- [x] 3.2 Update async task JSON deserialization in catalog query/repository paths to tolerate legacy path fields while projecting only the new schema field.
- [x] 3.3 Update `AsyncTaskConfigReq` and `AsyncTaskConfigResp` plus `ApiAssetWebDelegate`, `CatalogDiscoveryWebDelegate`, and `ApiImportAgentWebDelegate` mappings.
- [x] 3.4 Update persisted Import Agent plan JSON conversion so session snapshots read/write `queryResponseJsonSchema` and no longer write legacy path fields.

## 4. Import Agent Planning

- [x] 4.1 Update planner tool schemas to accept `asyncTaskConfig.queryResponseJsonSchema` and remove `statusPath`, `resultPath`, and `errorPath`.
- [x] 4.2 Update draft parser, validator allowlists, changed-field handling, and clarification answer application for the new async task schema field.
- [x] 4.3 Extend schema-generation or orchestration prompts so task-query response examples/docs can generate `queryResponseJsonSchema`.
- [x] 4.4 Update clarification behavior to ask for task-query response examples or field descriptions when the schema cannot be inferred.

## 5. Verification

- [x] 5.1 Update domain and service tests for async task config schema storage, optional schema behavior, and legacy path tolerance.
- [x] 5.2 Update persistence tests for serialized `async_task_config` JSON and legacy JSON deserialization.
- [x] 5.3 Update web adapter tests for owner asset, Discovery, and Import Agent DTO mappings.
- [x] 5.4 Update Unified Access tests proving `queryResponseJsonSchema` does not alter successful task-query passthrough.
- [x] 5.5 Update Import Agent planner/parser tests for schema generation, invalid schema rejection, and clarification paths.
- [x] 5.6 Run targeted Maven tests for affected catalog, Unified Access, and Import Agent modules.
