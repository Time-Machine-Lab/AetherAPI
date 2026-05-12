## Why

Unified Access can submit asynchronous upstream APIs today, but it only passthroughs the initial `202` or task-id response and provides no first-class channel for callers to query the resulting upstream task. This leaves asynchronous API assets awkward to consume through Aether: callers must know the provider-specific task query endpoint instead of using the same governed Unified Access path.

## What Changes

- Add an optional asynchronous task configuration to API assets so an asset can declare how its upstream task query endpoint is called.
- Extend Unified Access with a task query entry for the same `apiCode`, reusing API Key validation, target resolution, subscription entitlement, platform proxy routing, response passthrough, failure classification, and call-log recording.
- Treat task query as a runtime capability of the same API asset, not as a separate platform-managed task center.
- Keep successful task query responses passthrough-first in phase one; status extraction and mapping may be configured for future normalization but MUST NOT force a TML-SDK `Result` wrapper on successful Unified Access responses.
- Record task query invocations as platform call logs with a distinguishable access channel/result context so the console can tell submit calls from polling/query calls.
- Do not add platform task ownership storage, task lists, background polling, callback/webhook handling, task result caching, retry orchestration, or a full async task lifecycle table in this change.

## Capabilities

### New Capabilities

- `catalog-async-task-config`: API Catalog can store and expose optional asynchronous task query configuration on an API asset while preserving current owner-scoped asset management semantics.
- `unified-access-async-task-query`: Unified Access can query an upstream asynchronous task for a target API asset through a governed task query path.

### Modified Capabilities

- `observability-call-log-foundation`: task query invocations are recorded as regular platform call logs with a distinguishable access channel or result context.

## Impact

- Top-level docs:
  - Update `docs/sql/api-asset.sql` for table `api_asset` to add the async task configuration storage needed by API Catalog.
  - Update `docs/api/api-asset-management.yaml`, mapped one-to-one to `ApiAssetController.java`, to allow asset owners to maintain the optional async task query configuration.
  - Update `docs/api/unified-access.yaml`, mapped one-to-one to `UnifiedAccessController.java`, to document the task query entry under the Unified Access API contract.
  - Update the API Catalog, Unified Access, and Observability design documents under `docs/design/aehter-api-hub/` to describe async task query boundaries.
  - Any new or updated `docs/api/` and `docs/sql/` files must be generated with `tml-docs-spec-generate`; SQL files use the SQL template and API files use the API template.
- Backend code:
  - Extend API asset domain/application/persistence/DTO mapping with optional async task configuration.
  - Extend Unified Access application and adapter paths with a task query use case while preserving existing direct invocation behavior.
  - Extend downstream forwarding so task query URL templates can be resolved from `{taskId}` and executed with the same upstream auth/proxy semantics unless explicitly configured otherwise.
  - Extend call-log recording so submit and task query invocations can be distinguished.
- Boundary notes:
  - Aether does not prove `taskId` ownership in this phase because it does not persist submitted tasks; upstream providers must enforce task ownership through their own credentials or unguessable task ids.
  - If product requirements later need current-user task lists, platform-owned task ids, ownership enforcement, cached status, or callbacks, that should be a separate task-center change with a dedicated table such as `api_async_task`.
