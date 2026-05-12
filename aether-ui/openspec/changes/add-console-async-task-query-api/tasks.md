## 1. Authority Context

- [x] 1.1 Confirm `docs/api/unified-access.yaml` defines `GET /api/v1/access/{apiCode}/tasks/{taskId}` and existing passthrough response semantics.
- [x] 1.2 Confirm `docs/api/api-asset-management.yaml` already defines `asyncTaskConfig` and `asyncTaskQueryEnabled`, so no top-level API contract update is needed.

## 2. Unified Access API Layer

- [x] 2.1 Refactor Unified Access response parsing into a shared internal helper without changing existing invocation behavior.
- [x] 2.2 Add `queryUnifiedAccessTask(apiCode, taskId, apiKey)` using `GET v1/access/{apiCode}/tasks/{taskId}` with encoded path variables and `X-Aether-Api-Key`.
- [x] 2.3 Extend Unified Access platform failure types to include task-query-compatible failure values.
- [x] 2.4 Add or update API-layer tests for task query request construction, passthrough success parsing, and platform failure classification.

## 3. Catalog Async Metadata Mapping

- [x] 3.1 Add frontend DTO/domain types for asset `asyncTaskConfig` fields.
- [x] 3.2 Map asset detail `asyncTaskConfig` and asset summary `asyncTaskQueryEnabled` in the catalog API adapter.
- [x] 3.3 Add catalog API tests for async task config and task-query-enabled summary mapping.

## 4. Verification

- [x] 4.1 Run targeted `aether-console` Vitest specs for Unified Access and catalog API layers.
- [x] 4.2 Run `aether-console` type checking.
