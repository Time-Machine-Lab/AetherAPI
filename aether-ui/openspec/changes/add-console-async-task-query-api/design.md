## Context

Unified Access successful responses are passthrough upstream responses, not TML-SDK result wrappers. The existing `aether-console` Unified Access API layer already reflects this by using a standalone axios instance with `responseType: 'arraybuffer'`, `validateStatus: () => true`, and `X-Aether-Api-Key` instead of the console bearer token. The new task query endpoint has the same runtime response contract but uses `GET v1/access/{apiCode}/tasks/{taskId}`.

The backend authority documents also expose async task metadata on API assets: asset detail can include `asyncTaskConfig`, and asset list summaries can include `asyncTaskQueryEnabled`. `aether-console` currently drops these fields during DTO mapping, so later UI/composable code cannot tell whether an asset has a task query channel.

Implementation must stay within the existing frontend layering rules: request details belong in `src/api`, orchestration belongs in composables when needed, page components do not issue naked requests, visible UI copy uses i18n, and visual rules in `DESIGN.md` are only relevant if a later UI change is proposed.

## Goals / Non-Goals

**Goals:**

- Add a `queryUnifiedAccessTask` API function that calls the backend task query endpoint using the same Unified Access response parsing as submit/invoke.
- Keep task query responses passthrough-first for JSON, text, event-stream, and binary payloads.
- Classify platform failures from task query responses with the same frontend result shape as normal Unified Access calls.
- Add typed async task metadata to catalog DTO/domain mappings for current-user asset detail and asset summary list data.
- Cover the API layer and mapping behavior with Vitest tests.

**Non-Goals:**

- No new page, form, route, or polling UI.
- No platform task center, task list, task ownership cache, callback, or scheduler.
- No backend API, SQL, or authority-doc updates.
- No change to console session authentication.
- No attempt to normalize upstream task statuses into a first-party task model.

## Decisions

### 1. Share Unified Access response parsing

Refactor the existing response parsing in `unified-access.api.ts` into an internal helper used by both `invokeUnifiedAccess` and `queryUnifiedAccessTask`.

Why this over duplicating parsing in the task function:

- Both endpoints intentionally share passthrough semantics.
- Duplicated parsing can drift on platform failure handling, event-stream text classification, or binary fallback.
- Tests can assert endpoint-specific request construction while relying on one parser.

### 2. Keep task query as a plain API function

Expose `queryUnifiedAccessTask(apiCode, taskId, apiKey)` from the existing Unified Access API module.

Why this over adding a composable now:

- The user's requested scope is a frontend interface/API, not a page flow.
- Polling cadence, task-id capture, and result presentation are UI/product decisions that should remain outside this narrow change.
- A plain function is reusable by future composables or UI without committing to orchestration behavior today.

### 3. Encode both path parameters

Use `encodeURIComponent` for both `apiCode` and `taskId`.

Why this over interpolating raw values:

- Existing `invokeUnifiedAccess` already encodes `apiCode`.
- Upstream task ids can contain characters that are unsafe in URL paths.
- The backend endpoint treats `taskId` as a path variable, so frontend code must preserve it as one segment.

### 4. Map async asset metadata without creating UI behavior

Add `AsyncTaskConfig`/`AsyncTaskConfigDto` and `asyncTaskQueryEnabled` to catalog domain mappings.

Why this over leaving metadata out until UI work:

- The API contract already exposes these fields.
- Frontend callers need a typed way to determine whether the task query API is relevant for a selected asset.
- Mapping metadata is API-layer work and does not force any visual or interaction changes.

## Risks / Trade-offs

- [Risk] Task query success payloads vary by upstream provider. -> Mitigation: preserve the existing passthrough result shape instead of normalizing provider-specific status, result, or error fields.
- [Risk] A future UI may need polling and cancellation semantics. -> Mitigation: keep this change API-only so those decisions can be proposed separately.
- [Risk] Task ids may contain slashes or reserved characters. -> Mitigation: encode `taskId` before placing it in the route.
- [Risk] Platform failure DTOs may expand beyond the current type union. -> Mitigation: include task-query and subscription-aware failure types in the frontend union while still requiring the platform `failureType` marker.

## Migration Plan

1. Add task query function and shared response parsing in the Unified Access API module.
2. Add async task metadata DTO/types and catalog mappings.
3. Add focused API and mapping tests.
4. Run targeted Vitest tests plus TypeScript checking for `aether-console`.

Rollback strategy: remove the new frontend exports and mapping fields. Existing Unified Access invocation behavior remains unchanged.
