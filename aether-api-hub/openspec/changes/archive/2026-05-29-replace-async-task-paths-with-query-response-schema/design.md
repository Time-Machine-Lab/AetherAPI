## Context

Async task query configuration is stored as JSON in `api_asset.async_task_config` and exposed through owner asset management, catalog Discovery, Import Agent plans, and Unified Access target snapshots. The current contract includes `statusPath`, `resultPath`, and `errorPath`, but the documented Unified Access behavior is successful upstream passthrough rather than normalized status/result/error extraction.

The project already has nullable JSON Schema snapshots for asset submit request/response bodies. This change applies the same contract style to the second response shape owned by async task APIs: the upstream task-query response body.

Authority documents must be updated before implementation:

- `docs/api/api-asset-management.yaml` maps to `ApiAssetController.java`.
- `docs/api/api-catalog-discovery.yaml` maps to `CatalogDiscoveryController.java`.
- `docs/api/api-import-agent.yaml` maps to `ApiImportAgentController.java`.
- `docs/sql/api-asset.sql` maps to the `api_asset` table.

These documents must be generated or revised with `tml-docs-spec-generate` using the API/SQL templates.

## Goals / Non-Goals

**Goals:**

- Replace async task path fields with a nullable `queryResponseJsonSchema` field inside `asyncTaskConfig`.
- Make owner, Discovery, and Import Agent API contracts show the query response schema consistently.
- Keep storage in the existing nullable `api_asset.async_task_config` JSON column.
- Let Import Agent infer task-query response schemas from provider docs/examples and avoid producing path fields.
- Tolerate legacy stored JSON containing `statusPath`, `resultPath`, or `errorPath` during reads.

**Non-Goals:**

- Do not normalize Unified Access task-query responses into platform-level `status`, `result`, or `error`.
- Do not validate upstream runtime responses against `queryResponseJsonSchema`.
- Do not add a separate task-query asset or a new database table.
- Do not change subscription, credential, proxy routing, timeout, or failure classification behavior for task queries.

## Decisions

### Decision 1: Store `queryResponseJsonSchema` inside `asyncTaskConfig`

Use the existing `api_asset.async_task_config` JSON column and add a nullable `queryResponseJsonSchema` member alongside `enabled`, `queryMethod`, `queryUrlTemplate`, `authMode`, `authScheme`, and `authConfig`.

Rationale: the schema belongs to the task-query endpoint, not the submit endpoint. Keeping it inside `asyncTaskConfig` prevents confusion with asset-level `responseJsonSchema`, which describes the submit response.

Alternative considered: add `async_task_response_json_schema` as a separate `api_asset` column. This would make querying easier later, but it increases schema migration scope without a current filtering/reporting requirement.

### Decision 2: Treat schema as contract metadata, not execution logic

Unified Access continues to render `{taskId}`, forward the query request, and passthrough successful upstream response semantics. `queryResponseJsonSchema` is carried in asset metadata for owners, Discovery consumers, and Import Agent review, but it is not used to extract task status or reshape responses.

Rationale: existing `statusPath`, `resultPath`, and `errorPath` are documented as reserved/path-like fields, while runtime code does not use them for response extraction. Making schema executable would introduce new behavior and error modes outside this change.

Alternative considered: use JSON Schema extensions such as `x-aether-task-role` to mark status/result/error fields. This may be useful later if the platform introduces normalized task status, but it should be a separate change.

### Decision 3: New writes omit legacy path fields; reads tolerate them

Domain, service, web, and Import Agent models remove the path fields and expose `queryResponseJsonSchema`. Persistence deserialization ignores legacy path fields when loading old `async_task_config` JSON. Serialization writes only the new field.

Rationale: the external contract is intentionally simplified, while tolerant reads avoid breaking existing assets immediately after deployment.

Alternative considered: support both old and new fields in public APIs for a transition period. This would keep ambiguity alive for users and Import Agent, which is the core problem being fixed.

### Decision 4: Import Agent generates task-query response schema from evidence

Planner tool schemas and draft parsing accept `asyncTaskConfig.queryResponseJsonSchema` as a JSON Schema object string, following the same normalization rules used for `requestJsonSchema` and `responseJsonSchema`. The agent should use task-query response examples, field documentation, current asset values, and explicit schema hints as evidence. If evidence is insufficient, it leaves the field blank and asks a clarification about the task-query response body shape.

Rationale: the user should not be asked to hand-write backend extraction paths. The Import Agent already has schema-generation behavior for asset request/response snapshots and can extend that pattern to async task query responses.

Alternative considered: derive schema from `statusPath/resultPath/errorPath`. That preserves the old mental model and cannot show complete nested result payloads.

## Risks / Trade-offs

- Legacy clients may still send path fields -> The backend ignores those fields after the contract update and tests cover new DTO behavior.
- Users may expect schema to normalize the task query result -> API descriptions and specs state that Unified Access passthrough behavior is unchanged.
- Large schemas inside `async_task_config` may make JSON snapshots harder to inspect manually -> The field remains optional and stored as normalized compact JSON text.
- Import Agent may hallucinate a schema without enough evidence -> Planner normalization accepts only JSON object schemas and clarification asks for task-query response examples when evidence is weak.

## Migration Plan

1. Update authority documents with `tml-docs-spec-generate`: `docs/sql/api-asset.sql`, `docs/api/api-asset-management.yaml`, `docs/api/api-catalog-discovery.yaml`, and `docs/api/api-import-agent.yaml`.
2. Update domain/service/API models and persistence serialization to write `queryResponseJsonSchema` and stop writing legacy path fields.
3. Keep deserialization tolerant of existing JSON that contains legacy path fields.
4. Update Import Agent planner schemas, parsers, validators, clarification handling, JSON converters, and schema generation orchestration.
5. Update focused tests, then run targeted Maven tests for catalog, Unified Access, and Import Agent modules.

Rollback is low-risk because the database column remains the same. A rollback can redeploy previous code; new JSON containing `queryResponseJsonSchema` will be ignored by older code unless it strictly depends on legacy path fields.

## Open Questions

- Should stored legacy `statusPath`, `resultPath`, and `errorPath` be actively removed by a data cleanup task, or only disappear on the next asset save?
- Should the UI label this field as "Task query response schema" to distinguish it from the submit response schema?
