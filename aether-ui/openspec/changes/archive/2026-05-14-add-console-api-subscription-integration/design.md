## Context

The backend archived change `2026-05-02-add-api-subscription-foundation` introduced API Subscription as a lightweight current-user entitlement. The authority contract is already present in `docs/api/api-subscription.yaml`, and the domain design says subscription is not payment, not approval, not Consumer management, and not API Key management. It only answers whether the current user can use a published API asset.

`aether-console` currently has the marketplace browse page, the owner asset workspace, credential management, and Unified Access playground. The marketplace detail already exposes a disabled `subscriptionUnavailable` message, so the product slot exists but is not backed by the new contract. Unified Access also currently recognizes platform failures, but its frontend type union does not include the new `SUBSCRIPTION_REQUIRED` failure type from `docs/api/unified-access.yaml`.

Implementation must follow the shared frontend spec: Vue 3 + TypeScript + Vite, file routes, API calls through the unified axios layer, business orchestration in composables/features, no naked page requests, and all visible copy through i18n. Visual and interaction treatment must follow `aether-console/DESIGN.md`: actions use buttons, read-only subscription states use status tags, system feedback uses state blocks or notice-style feedback, and workspace rows align with the existing console rhythm.

## Goals / Non-Goals

**Goals:**

- Add a typed subscription API module that maps exactly to `docs/api/api-subscription.yaml`.
- Let users subscribe to published marketplace assets, see current status, see owner access, and cancel their own active subscriptions.
- Provide a protected current-user subscription list experience inside the existing console shell.
- Make the marketplace and Unified Access playground subscription-aware without inventing new backend fields.
- Treat `API_SUBSCRIPTION_REQUIRED` / `SUBSCRIPTION_REQUIRED` as a specific, actionable platform failure.
- Update mocks, i18n, and tests so local flows verify status, subscribe, cancel, owner access, and missing-subscription behavior.

**Non-Goals:**

- No backend API, SQL, or authority-doc changes.
- No payment, plan, order, billing, approval, quota, settlement, purchase language, or Consumer management UI.
- No new global Store as the default orchestration point.
- No redesign of the console visual system or shared frontend architecture.
- No attempt to auto-create API Keys or choose credentials as part of subscribing.

## Decisions

### 1. Introduce a dedicated subscription API/domain slice

Create a new frontend slice such as `src/api/subscription/subscription.api.ts`, `subscription.dto.ts`, and `subscription.types.ts` for the four current-user subscription endpoints.

Why this over placing subscription functions in catalog or credential modules:

- The backend design treats API Subscription as its own domain, separate from API Catalog and Consumer & Auth.
- The DTOs contain entitlement states (`ACTIVE`, `CANCELLED`, `OWNER`, `SUBSCRIBED`, `NOT_SUBSCRIBED`) that should not leak into asset publication or API Key enablement types.
- Tests can assert that subscription calls use `v1/current-user/api-subscriptions` and do not reuse discovery or credential endpoints.

### 2. Keep orchestration in composables, not page components or Store

Add composables such as `useApiSubscriptionStatus` and `useApiSubscriptionWorkspace` to load status by `apiCode`, subscribe, list subscriptions, and cancel. Pages and feature components consume these composables and render states.

Why this over page-local requests:

- The shared frontend spec requires page components to orchestrate and compose rather than issue naked requests.
- Marketplace detail and playground both need subscription status; a composable avoids duplicating status transitions.
- A global Store would be heavier than the first-phase need because subscription state is route/context scoped and can be refreshed from the authority endpoints.

### 3. Surface subscription status in marketplace detail first

The marketplace detail panel is the primary subscribe entry. When a detail is selected, the frontend queries status with `apiCode` and renders one of these states:

- `OWNER`: read-only owner-access status, no subscribe button.
- `SUBSCRIBED` or response `ACTIVE`: subscribed status with optional cancel entry when a `subscriptionId` is available.
- `NOT_SUBSCRIBED`: subscribe action.
- request loading/error: standard console state feedback.

Why this over a standalone subscription page only:

- The backend domain defines subscription as part of the discover -> subscribe -> call loop.
- The existing marketplace detail already has the visual slot and context needed to decide the next action.
- Users should not have to leave the asset they are evaluating before subscribing.

### 4. Add a protected "my subscriptions" section using existing console workspace patterns

Use the current console shell and workspace row patterns for the list of current-user subscriptions. The list should display only fields present in `ApiSubscriptionResp`: `apiCode`, `assetName`, `subscriptionStatus`, owner snapshot when available, timestamps, and cancellation status. Cancel actions are available only for active, cancellable records.

Why this over adding a separate public marketplace route:

- Subscription list is current-user scoped and requires bearer authentication.
- It belongs next to credentials, owned assets, playground, and logs in the protected console.
- Existing workspace list and status feedback patterns already cover loading, empty, error, and row actions.

### 5. Make Unified Access subscription-aware without blocking manual tests

The playground should continue to allow manual `apiCode` and API Key input, but when a selected marketplace asset has `NOT_SUBSCRIBED` status, the UI must clearly show that Unified Access may reject the call until subscription is active. If an invocation returns `failureType: SUBSCRIPTION_REQUIRED`, the result panel should show a specific subscription-required state with `apiCode` and `traceId` when present.

Why this over disabling all invocation buttons when not subscribed:

- Manual `apiCode` entry is needed for gray-box integration and owner-access cases.
- The backend remains the authority for entitlement enforcement.
- A specific failure display is more reliable than trying to fully predict every caller/API Key relationship on the client.

### 6. Keep cancellation conservative

Cancellation uses `PATCH /current-user/api-subscriptions/{subscriptionId}/cancel` and refreshes both the list and selected asset status after success. Owner access is never cancelled, and missing `subscriptionId` means the UI must not render a cancel action.

Why this over optimistic local-only mutation:

- The backend controls whether a subscription is still active and cancellable.
- The response can update timestamps and status, so the UI should use returned data and refresh dependent views.
- This avoids showing a cancelled entitlement as callable after server rejection.

## Risks / Trade-offs

- [Risk] The same asset can appear in marketplace, subscription list, and playground with stale status. -> Mitigation: refresh status after subscribe/cancel and reload status when selected `apiCode` changes.
- [Risk] Users may confuse subscription with purchase. -> Mitigation: i18n copy must use usage-entitlement language and avoid payment, order, plan, or billing terms.
- [Risk] Owner access can look like an active subscription. -> Mitigation: render owner access as a read-only status, not as a subscription row action, and do not require `subscriptionId`.
- [Risk] Manual playground calls can still fail after the UI indicates subscribed. -> Mitigation: always treat Unified Access response as authoritative and render `SUBSCRIPTION_REQUIRED` distinctly.
- [Risk] Existing mocks may allow calls without subscription. -> Mitigation: update local mocks and tests to include active, cancelled, owner, and missing-subscription paths.

## Migration Plan

1. Add typed subscription DTOs, types, API functions, and local mock handlers.
2. Add subscription composables for status, subscribe/list/cancel orchestration.
3. Replace marketplace `subscriptionUnavailable` placeholder with contract-backed status and actions.
4. Add current-user subscription list/cancel UI in the protected console shell.
5. Extend Unified Access failure types and result rendering for `SUBSCRIPTION_REQUIRED`.
6. Update i18n and tests for API mapping, composable state transitions, marketplace actions, list cancellation, and playground failure rendering.
7. Run the `aether-console` quality gates: tests, type-check, lint/format checks, and build where available.

Rollback strategy: remove this frontend integration or hide its entry points while keeping the backend subscription contract. Do not point users back to calling unsubscribed APIs as an intended flow, because backend Unified Access now enforces entitlement.

## Open Questions

- Whether the "my subscriptions" entry should live as a new console sidebar item or as a section under the existing workspace route can be decided during implementation by following the current console navigation density.
- If the backend later returns richer asset display fields for subscriptions, the frontend should map them in the subscription API adapter rather than infer them from discovery data.
