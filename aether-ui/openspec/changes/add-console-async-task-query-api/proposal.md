## Why

The backend now exposes Unified Access async task querying through `GET /api/v1/access/{apiCode}/tasks/{taskId}`, but `aether-console` only has a submit/invoke API client for `v1/access/{apiCode}`. Frontend code therefore has no typed, reusable channel for checking upstream async task status or results after a task-submitting API returns a task id.

## What Changes

- Add a typed `aether-console` API client function for querying Unified Access async tasks by `apiCode` and `taskId`.
- Reuse the existing standalone Unified Access axios instance behavior: `X-Aether-Api-Key` authentication, no console bearer-token substitution, no TML result normalization, binary-safe response handling, and platform failure classification.
- Extend Unified Access frontend failure typing so task-query-specific platform failures can be represented without collapsing into generic passthrough JSON.
- Map API asset async task metadata already defined by `docs/api/api-asset-management.yaml`, including detail `asyncTaskConfig` and list `asyncTaskQueryEnabled`, so frontend callers can discover whether task querying is configured.
- Add focused API-layer tests for task query request construction, passthrough response parsing, platform failures, and asset async metadata mapping.
- Do not add task list storage, polling orchestration, background scheduling, callbacks, or new page UI in this change.

## Capabilities

### New Capabilities

- `console-unified-access-async-task-query-api`: frontend API-layer behavior for querying Unified Access async tasks and exposing asset async task metadata in `aether-console`.

### Modified Capabilities

- None. The active baseline spec `console-api-call-log-pages` is not changed by this API-layer addition.

## Impact

- Affected app: `aether-console`.
- Backend authority dependencies: `docs/api/unified-access.yaml`, `docs/api/api-asset-management.yaml`, `docs/design/aehter-api-hub/Aether API Hub Unified Access领域设计文档.md`, and `docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`.
- Frontend authority dependencies: `docs/spec/AetherAPI 前端技术栈与开发规范文档.md` and `aether-ui/aether-console/DESIGN.md`.
- Likely frontend areas: `src/api/unified-access/*`, `src/api/catalog/*`, and related `*.spec.ts`.
- Contract changes: none expected. Existing authority docs already define the Unified Access task query endpoint and API asset async task fields required by this frontend proposal.
