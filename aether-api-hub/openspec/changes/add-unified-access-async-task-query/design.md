## Context

Aether API Hub already separates API Catalog, Consumer/Auth, Unified Access, Platform Proxy, and Observability. API Catalog owns API asset metadata and lifecycle. Unified Access consumes a published target API snapshot, validates API Key and subscription entitlement, forwards the upstream request, preserves successful upstream response semantics, and records a call log.

The current model is synchronous from Aether's point of view. If an upstream provider returns a task id for asynchronous work, Aether can passthrough that response but has no governed channel to query the task. The only available workaround is to model the provider's task query endpoint as a second unrelated API asset, which loses the relationship between submit and query semantics and pushes provider-specific URLs back to callers.

This change introduces the lightweight scheme B discussed in exploration: an API asset may declare an optional asynchronous task query configuration, and Unified Access may use that configuration to query an upstream task through the same governed runtime path. It intentionally does not persist submitted tasks.

## Goals / Non-Goals

**Goals:**

- Let asset owners declare that an API asset supports asynchronous task querying.
- Store async task query configuration on `api_asset` through an authority update to `docs/sql/api-asset.sql`.
- Extend `docs/api/api-asset-management.yaml` so `ApiAssetController.java` can maintain the optional async task configuration.
- Extend `docs/api/unified-access.yaml` so `UnifiedAccessController.java` documents a task query entry for the same `apiCode`.
- Reuse existing Unified Access validation, subscription enforcement, platform proxy routing, upstream auth injection, failure classification, response passthrough, and observability logging for task queries.
- Keep successful task query responses passthrough-first in phase one.
- Distinguish task query invocations in call logs without requiring a new log table.
- Update authoritative `docs/` artifacts before implementation.

**Non-Goals:**

- No platform-owned async task table.
- No current-user task list, task detail history, cached status, background polling, retry orchestration, callback/webhook handling, or task completion notification.
- No guarantee that a task id belongs to the current caller beyond what upstream credentials and task ids enforce.
- No forced platform-normalized success wrapper for task query responses.
- No full AI gateway task abstraction or provider-specific adapter catalog.

## Decisions

### Decision 1: Model async task querying as optional asset configuration

Add optional async task configuration to the API asset model. The storage should remain on `api_asset`, most likely as a JSON/text field such as `async_task_config`, because provider task query contracts vary and phase one should not over-normalize provider-specific response extraction.

The configuration should express at least:

- whether async task query is enabled for the asset;
- the task query HTTP method;
- the task query URL template containing `{taskId}`;
- whether task query upstream auth uses the submit endpoint auth configuration or an explicit override;
- optional paths for task id/status/result/error extraction for future normalization.

Rationale: submit and task query are two runtime behaviors of one business API asset. Keeping the configuration on the asset preserves ownership and avoids creating a second unrelated asset just to poll a task.

Alternative considered: require owners to create a second API asset for task queries. Rejected because the platform would not understand the relationship between submit and query, making permissions, docs, logs, and future UX weaker.

Alternative considered: create a structured `api_async_task_config` table. Deferred because phase one needs one optional config object, not a lifecycle of independently managed configs.

### Decision 2: Add a Unified Access task query path under the same controller

Expose task querying as part of `UnifiedAccessController.java`, documented in `docs/api/unified-access.yaml`, using a path shaped like:

`GET /api/v1/access/{apiCode}/tasks/{taskId}`

The exact path must be finalized in the authority API doc, but it should remain under Unified Access rather than under current-user management APIs because it is an upstream business invocation, not a console management read.

Rationale: callers should query asynchronous results through the same governed access surface they used to submit the task. This keeps API Key auth, subscription checks, proxy routing, response passthrough, and failure semantics consistent.

Alternative considered: add task query under `ApiCallLogController`. Rejected because call logs are platform facts, not live upstream task status.

### Decision 3: Reuse existing access checks and forwarding infrastructure

Task query MUST follow the same pre-forward checks as a normal Unified Access invocation:

- API Key validation;
- published, non-deleted target asset resolution;
- complete task query configuration;
- owner/subscription entitlement;
- platform proxy resolution if the asset is bound to a proxy profile.

After pre-forward checks, the downstream forwarding boundary should execute the resolved task query URL template. If task query auth is configured as same-as-submit, existing upstream auth injection semantics should apply. If an explicit task query auth override is allowed by the authority docs, it must follow the same supported auth schemes as normal upstream config.

Rationale: task query is still an upstream call for the same API asset; inventing a parallel auth and proxy path would create inconsistent behavior.

Alternative considered: bypass subscription checks for task query because the caller already has a task id. Rejected because this would allow a caller with a leaked or guessed task id to reach provider task endpoints through Aether without entitlement.

### Decision 4: Keep phase-one responses passthrough-first

Successful task query responses should preserve upstream status, headers, and body semantics like existing Unified Access success responses. Optional status/result/error extraction fields can be accepted in configuration for future UI normalization, but phase-one task query success MUST NOT be wrapped in TML-SDK `Result`.

Rationale: Unified Access already draws a strong line between platform management APIs and upstream business responses. Task query is an upstream business response.

Alternative considered: immediately normalize all task statuses into `PENDING/RUNNING/SUCCEEDED/FAILED/CANCELLED/EXPIRED/UNKNOWN`. Deferred because provider task schemas differ and forced wrapping would be a breaking semantic shift for the access surface.

### Decision 5: Record task queries as call logs, not tasks

Task query invocations should write normal `api_call_log` records using the existing Observability foundation. They should be distinguishable through access channel or result context, for example `UNIFIED_ACCESS_TASK_QUERY`, while preserving the target API snapshot and caller snapshot.

Rationale: task query is a completed upstream invocation and should be visible in logs. It is not itself a platform task record in this phase.

Alternative considered: add `api_async_task` now and write submit/query state transitions. Rejected for this change because it changes the product capability from "query channel" into "task center".

### Decision 6: Accept the task-id ownership limitation explicitly

Because this change does not store submitted tasks, Aether cannot independently prove that `{taskId}` was created by the same caller. The implementation must validate API Key, subscription, target asset, task query config, and task id format, then rely on upstream credentials, provider-side authorization, and unguessable task ids for task ownership enforcement.

Rationale: this is the core trade-off that keeps scheme B lightweight.

Alternative considered: persist every submit response and map upstream task ids to consumers. Rejected here because that belongs to the later task-center design.

## Risks / Trade-offs

- [Risk] A caller could query a task id they did not create if the upstream provider does not enforce task ownership. -> Mitigation: document this limitation, require entitlement checks, validate task id shape, and reserve platform-owned task storage for a future task-center change.
- [Risk] JSON/text async config may become hard to query or validate if it grows. -> Mitigation: keep the phase-one config narrow and validate it through domain/application rules before publishing.
- [Risk] Different providers use different status/result/error fields. -> Mitigation: preserve passthrough success responses in phase one and treat status extraction/mapping as optional future normalization data.
- [Risk] Asset owners might configure a task query URL template that misses `{taskId}` or exposes unsafe URLs. -> Mitigation: reject incomplete templates before publish or task-query execution and classify malformed resolved URLs as upstream execution failures after target resolution where appropriate.
- [Risk] Task query logging may blur together submit and query calls. -> Mitigation: use a distinguishable access channel or result context and document it in Observability design.

## Migration Plan

1. Generate or update authoritative docs first:
   - `docs/sql/api-asset.sql` using the SQL template for table `api_asset` async task configuration storage.
   - `docs/api/api-asset-management.yaml` using the API template for `ApiAssetController.java` async task configuration request/response fields.
   - `docs/api/unified-access.yaml` using the API template for `UnifiedAccessController.java` task query entry.
   - API Catalog, Unified Access, and Observability design docs under `docs/design/aehter-api-hub/`.
2. Add nullable async task configuration persistence so existing assets remain synchronous by default.
3. Extend asset domain/application validation for optional async task config and publishing completeness.
4. Extend Unified Access task query command/model/controller/delegate path.
5. Extend forwarding URL resolution and tests for `{taskId}` template rendering, passthrough, failure classification, proxy routing, and redaction.
6. Extend Observability recording/tests for the task query access channel.
7. Rollback strategy: leave `async_task_config` null or disabled for all assets; existing synchronous Unified Access behavior remains unchanged.

## Open Questions

- Should phase one allow only `GET` task query, or allow `GET/POST` because some providers query tasks with a JSON body?
- Should task query auth be strictly same-as-submit in phase one, or allow a separate auth override inside async task config?
- Should `taskId` accept a conservative platform pattern only, or should the pattern be configurable per asset?
