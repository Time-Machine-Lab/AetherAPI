## Context

The Unified Access Playground already collects `apiCode`, API Key, request payload, optional headers, and renders a passthrough response. Async task query needs almost the same credentials and result handling, but it uses `taskId` instead of method/body/headers. The existing API-layer function `queryUnifiedAccessTask(apiCode, taskId, apiKey)` keeps the request contract out of the page.

## Goals / Non-Goals

**Goals:**

- Add manual task id input and query action in the Playground.
- Use the current `apiCode` and API Key for task query, matching backend authentication.
- Render task query results through the existing result panel.
- Add loading, disabled, reset, and i18n states for task query.
- Add composable tests for task query success, required fields, and platform failures.

**Non-Goals:**

- No automatic polling.
- No task history or task list.
- No storage of task ids or task ownership.
- No backend API changes.
- No redesign of the Playground.

## Decisions

### 1. Keep task query inside the existing Playground

Task query belongs next to Unified Access invocation because users receive task ids from async invocation responses and immediately need to inspect them.

### 2. Reuse the existing result state

Both invoke and task query return `UnifiedAccessResult`, so one response panel can render both. A small `resultSource` state distinguishes whether the latest response came from invocation or task query for headings and copy.

### 3. Do not infer task availability as a hard gate

The UI will allow manual task queries when `apiCode`, API Key, and task id are present. Backend remains the authority and returns `ASYNC_TASK_QUERY_UNAVAILABLE` when the selected API has no configured task query channel.

## Risks / Trade-offs

- [Risk] Users may expect polling. -> Mitigation: provide a one-shot query action only and keep polling as a future proposal.
- [Risk] Task ids may be copied from arbitrary providers. -> Mitigation: send them through the API layer, which URL-encodes path values.
- [Risk] The selected asset may not expose task metadata in discovery. -> Mitigation: do not block manual query based on metadata; rely on backend failure classification.
