## 1. Authority Docs First

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` before implementation and confirm DDD/module dependency constraints.
- [x] 1.2 Use `tml-docs-spec-generate` with the SQL template to update `docs/sql/api-asset.sql`, mapped one-to-one to table `api_asset`, with nullable async task configuration storage.
- [x] 1.3 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-asset-management.yaml`, mapped one-to-one to `ApiAssetController.java`, with async task configuration request/response fields.
- [x] 1.4 Use `tml-docs-spec-generate` with the API template to update `docs/api/unified-access.yaml`, mapped one-to-one to `UnifiedAccessController.java`, with the async task query path and failure contracts.
- [x] 1.5 Use `tml-docs-spec-generate` with the domain design template to update API Catalog, Unified Access, and Observability design docs under `docs/design/aehter-api-hub/` with async task query boundaries.

## 2. API Catalog Async Task Configuration

- [x] 2.1 Add an async task configuration value object/model for enabled state, query method, URL template, auth mode or override, and optional extraction/mapping paths.
- [x] 2.2 Extend API asset aggregate rules so incomplete async task config is rejected or treated as unavailable, including missing URL template and missing `{taskId}` placeholder.
- [x] 2.3 Extend asset application commands/results for create/revise/detail/list flows to carry optional async task configuration aligned with `docs/api/api-asset-management.yaml`.
- [x] 2.4 Extend API asset persistence entity/converter/repository/query records for nullable async task config aligned 100% with `docs/sql/api-asset.sql`.

## 3. Unified Access Task Query

- [x] 3.1 Add Unified Access task query command/model/use-case methods for `{apiCode}` plus `{taskId}` while preserving existing invoke behavior.
- [x] 3.2 Add `UnifiedAccessController.java` and web delegate handling for the documented task query path aligned 100% with `docs/api/unified-access.yaml`.
- [x] 3.3 Reuse API Key validation, target asset resolution, subscription entitlement, and platform proxy profile resolution for task query requests.
- [x] 3.4 Render the task query upstream URL from the configured template and caller task id, validating blank task ids and incomplete async config before forwarding.
- [x] 3.5 Execute task query through the existing downstream forwarding boundary with same-as-submit auth semantics or documented task-query auth override behavior.
- [x] 3.6 Preserve successful task query passthrough semantics and classify task query execution failures as upstream execution failure or upstream timeout.

## 4. Observability And Logging

- [x] 4.1 Record completed task query invocations through the existing Observability call-log path.
- [x] 4.2 Ensure task query call logs are distinguishable from normal Unified Access invocations through access channel or result context.
- [x] 4.3 Confirm no platform async task lifecycle table, current-user task list, cached task status, or background polling behavior is introduced in this change.

## 5. Verification

- [x] 5.1 Add domain/application tests for async task config validation, default synchronous assets, and asset response mapping.
- [x] 5.2 Add persistence/API adapter tests for `api_asset` async task config and `ApiAssetController` contract behavior.
- [x] 5.3 Add Unified Access tests for successful task query passthrough, asset without async config rejection, blank task id rejection, missing subscription rejection, proxy routing, malformed rendered URL classification, and upstream timeout classification.
- [x] 5.4 Add Observability tests proving task query invocations write distinguishable call logs without requiring task lifecycle storage.
- [x] 5.5 Run relevant Maven tests for affected modules and record any environment-related gaps.
- [x] 5.6 Run `openspec status --change add-unified-access-async-task-query` and confirm the change is apply-ready.
