## 1. Authority Context

- [x] 1.1 Confirm no backend or top-level API contract update is needed.

## 2. Playground State And Actions

- [x] 2.1 Add task id state, query loading state, result source state, and computed task query availability to `useUnifiedAccessPlayground`.
- [x] 2.2 Add a `queryTask` action that calls `queryUnifiedAccessTask` and writes into the existing result state.
- [x] 2.3 Reset task query state with the existing form/result reset helpers.

## 3. Playground UI

- [x] 3.1 Add a task query section to `UnifiedAccessPlayground.vue` with task id input, query button, loading state, and concise helper copy.
- [x] 3.2 Update response heading/copy to distinguish normal invocation from task query result.
- [x] 3.3 Add localized labels and failure guidance for task query.

## 4. Verification

- [x] 4.1 Add/update composable tests for task query success, missing fields, reset behavior, and task-query-specific platform failure.
- [x] 4.2 Run targeted Playground and Unified Access tests.
- [x] 4.3 Run `aether-console` type checking.
