## Why

The backend and frontend API layer now support Unified Access async task queries, but `aether-console` users still cannot query a returned task id from the Playground. The page needs a visible task-query workflow so async APIs can be submitted and checked without leaving the console.

## What Changes

- Add a task query section to the Unified Access Playground page.
- Let users enter a task id and query `GET /api/v1/access/{apiCode}/tasks/{taskId}` using the current `apiCode` and API Key.
- Reuse the existing response panel for task query results, including JSON/text/binary passthrough rendering and platform failure display.
- Surface task-query-specific failures such as `ASYNC_TASK_QUERY_UNAVAILABLE` and `INVALID_TASK_ID` with localized guidance.
- Preserve manual invocation behavior and do not introduce automatic polling, task lists, callbacks, or a platform task center.

## Capabilities

### New Capabilities

- `console-unified-access-async-task-query-playground`: user-facing Playground behavior for manually querying Unified Access async task status/results.

### Modified Capabilities

- None. Existing active specs are not changed.

## Impact

- Affected app: `aether-console`.
- Backend authority dependencies: `docs/api/unified-access.yaml` and the Unified Access domain design.
- Frontend dependencies: existing `queryUnifiedAccessTask` API layer, `useUnifiedAccessPlayground`, `UnifiedAccessPlayground.vue`, i18n locale files, and related tests.
- Contract changes: none expected.
