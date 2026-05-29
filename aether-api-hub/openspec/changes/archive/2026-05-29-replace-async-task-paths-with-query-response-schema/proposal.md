## Why

Async task configuration currently exposes `statusPath`, `resultPath`, and `errorPath`, which asks users to reason about hidden JSONPath-style extraction instead of seeing the actual task-query response shape. The existing asset response schema model already gives users a clearer contract pattern, so async task query responses should use the same schema-oriented language.

## What Changes

- **BREAKING** Remove `statusPath`, `resultPath`, and `errorPath` from async task configuration contracts, stored JSON, Discovery responses, and Import Agent plans.
- Add a nullable `queryResponseJsonSchema` JSON Schema snapshot to `asyncTaskConfig` to describe the upstream task-query response body.
- Keep Unified Access task-query execution passthrough: the schema is a contract/display snapshot, not a runtime response-normalization rule.
- Update owner asset management, catalog Discovery, and Import Agent authority API documents so async task configuration presents the query response schema instead of field paths.
- Update Import Agent planning so generated plans infer `queryResponseJsonSchema` from upstream docs/examples and ask for task-query response evidence when insufficient.
- Preserve compatibility for old stored JSON by ignoring legacy path fields during read, while new writes no longer emit them.

## Capabilities

### New Capabilities

- `api-import-agent-async-task-response-schema`: Import Agent can plan async task query response schemas and no longer generates JSONPath-style task status/result/error fields.

### Modified Capabilities

- `catalog-async-task-config`: API assets declare async task query response shape through `queryResponseJsonSchema` instead of `statusPath`, `resultPath`, and `errorPath`.
- `catalog-discovery-async-task-config`: published Discovery detail exposes async task query response schema and omits legacy path fields.
- `unified-access-async-task-query`: Unified Access keeps passthrough task-query behavior while carrying the schema as non-executing asset metadata.

## Impact

- Authority documents: update `docs/api/api-asset-management.yaml` for `ApiAssetController.java`, `docs/api/api-catalog-discovery.yaml` for `CatalogDiscoveryController.java`, `docs/api/api-import-agent.yaml` for `ApiImportAgentController.java`, and `docs/sql/api-asset.sql` for table `api_asset`. These are top-level authority artifacts and must be generated or revised with `tml-docs-spec-generate` using the API/SQL templates before code implementation.
- Backend domain/service: `AsyncTaskConfig`, `AsyncTaskConfigModel`, asset commands/models, target snapshots, and async task normalization must replace path fields with `queryResponseJsonSchema`.
- Persistence: `api_asset.async_task_config` remains the storage location; serialization emits the new field and deserialization tolerates old JSON with legacy path fields.
- Web contracts: request/response DTOs and delegates for owner asset management, Discovery, and Import Agent plan responses must map the new field.
- Import Agent: planner tool schema, draft parser/validator, clarification paths, JSON persistence converter, and schema-generation orchestration must support task-query response schema generation.
- Tests: update catalog domain/service/adapter/persistence tests, Discovery projection tests, Unified Access task-query snapshot tests, and Import Agent planner/parser tests.
