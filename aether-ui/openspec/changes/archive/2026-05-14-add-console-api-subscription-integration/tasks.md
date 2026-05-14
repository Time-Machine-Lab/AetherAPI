## 1. Authority Context

- [x] 1.1 Re-read `docs/spec/AetherAPI 前端技术栈与开发规范文档.md`, `aether-ui/aether-console/DESIGN.md`, `docs/api/api-subscription.yaml`, `docs/api/unified-access.yaml`, and the API Subscription domain design before coding.
- [x] 1.2 Confirm no top-level `docs/api/*.yaml`, frontend stack spec, or `aether-console/DESIGN.md` updates are required because existing authority docs already define the needed contracts and visual rules.

## 2. Subscription API Layer

- [x] 2.1 Add `src/api/subscription` DTOs and domain types for subscription responses, status responses, page responses, and request bodies.
- [x] 2.2 Implement typed API functions for subscribe, list current-user subscriptions, get status by `apiCode`, and cancel by `subscriptionId` through the unified axios instance.
- [x] 2.3 Add subscription mock handlers for active, cancelled, owner access, not subscribed, subscribe idempotency, and cancel responses.
- [x] 2.4 Add API-layer tests that verify endpoint paths, request payloads, response mapping, and error propagation.

## 3. Subscription Orchestration

- [x] 3.1 Add composable logic for selected-asset subscription status loading and refresh on `apiCode` changes.
- [x] 3.2 Add composable logic for subscribe and cancel flows, including loading, success, error, and stale-status refresh behavior.
- [x] 3.3 Add composable logic for current-user subscription list loading, empty state, error state, pagination parameters if needed, and row cancellation.
- [x] 3.4 Add composable tests for subscribed, not subscribed, owner access, duplicate subscribe, cancelled, and failed request scenarios.

## 4. Marketplace And Subscription Workspace UI

- [x] 4.1 Replace marketplace detail `subscriptionUnavailable` placeholder with contract-backed status tags, subscribe action, owner-access state, loading state, and error state.
- [x] 4.2 Add or place the protected "my subscriptions" console experience using existing workspace/list row patterns and current-user subscription data.
- [x] 4.3 Wire cancel actions only for active rows with a `subscriptionId`, and refresh list plus selected marketplace status after cancellation.
- [x] 4.4 Update console navigation or workspace anchors only if needed, preserving existing console shell density and route guard behavior.

## 5. Unified Access Subscription Awareness

- [x] 5.1 Extend Unified Access frontend failure types to include `SUBSCRIPTION_REQUIRED` and stable code handling for `API_SUBSCRIPTION_REQUIRED`.
- [x] 5.2 Update the playground result panel to render subscription-required failures as a specific actionable state while preserving upstream passthrough success rendering.
- [x] 5.3 Refresh marketplace-to-playground and target-selection guidance so subscribed, not subscribed, and owner-access statuses are reflected without blocking manual invocation.
- [x] 5.4 Add tests for subscription-required failure rendering, subscribed target guidance, owner-access guidance, and unresolved manual `apiCode` fallback.

## 6. I18n, Visual Semantics, And Verification

- [x] 6.1 Add `zh-CN` and `en-US` i18n keys for subscription states, actions, empty states, errors, and playground subscription-required guidance.
- [x] 6.2 Ensure all subscription UI follows `aether-console/DESIGN.md` semantic roles: actions as buttons, read-only states as status tags, and errors/empty/loading as state feedback.
- [x] 6.3 Run the relevant `aether-console` test suite.
- [x] 6.4 Run `type-check`, lint/format checks, and build for `aether-console` where available.
