## Why

The backend subscription foundation is already archived and the authority contract `docs/api/api-subscription.yaml` now defines current-user subscription APIs, but `aether-console` still presents published marketplace assets with a disabled "subscription unavailable" state. The frontend must close the discover -> subscribe -> call loop so users can subscribe to published APIs before invoking them through Unified Access.

## What Changes

- Add current-user API subscription integration to `aether-console`, consuming `POST /api/v1/current-user/api-subscriptions`, `GET /api/v1/current-user/api-subscriptions`, `GET /api/v1/current-user/api-subscriptions/status`, and `PATCH /api/v1/current-user/api-subscriptions/{subscriptionId}/cancel`.
- Replace the marketplace detail placeholder `subscriptionUnavailable` with contract-backed subscription states and actions: subscribe, subscribed, owner access, not subscribed, cancelled, loading, and error.
- Add a protected "my subscriptions" console experience using the current-user list endpoint, with cancel support and status feedback that stays scoped to the authenticated user.
- Make Unified Access entry points subscription-aware: marketplace "open playground" and playground target guidance should surface whether the current user is subscribed, has owner access, or must subscribe first.
- Handle Unified Access 403 platform failures with `code: API_SUBSCRIPTION_REQUIRED` / `failureType: SUBSCRIPTION_REQUIRED` as a first-class frontend state instead of a generic invocation error.
- Update API DTOs, frontend domain types, composables, mocks, i18n, and tests to match `docs/api/api-subscription.yaml` and the existing `docs/api/unified-access.yaml` failure contract.
- Do not add payment, pricing plans, approval workflow, billing, settlement, quota, Consumer management, or new backend contract changes in this frontend proposal.

## Capabilities

### New Capabilities

- `console-api-subscription-marketplace`: `aether-console` marketplace and subscription workspace behavior for current-user API subscription status, creation, listing, and cancellation.
- `console-unified-access-subscription-awareness`: Unified Access playground and invocation feedback behavior when published API calls require an active subscription or owner access.

### Modified Capabilities

- None. The active baseline spec `console-api-call-log-pages` is not changed by subscription integration.

## Impact

- Affected app: `aether-console`.
- Backend authority dependencies: `docs/api/api-subscription.yaml`, `docs/api/unified-access.yaml`, `docs/api/api-catalog-discovery.yaml`, `docs/design/aehter-api-hub/Aether API Hub API Subscription领域设计文档.md`, and the archived backend change `aether-api-hub/openspec/changes/archive/2026-05-02-add-api-subscription-foundation`.
- Frontend authority dependencies: `docs/spec/AetherAPI 前端技术栈与开发规范文档.md` and `aether-ui/aether-console/DESIGN.md`.
- Likely frontend areas: `src/api/subscription/*`, `src/api/unified-access/*`, `src/composables/useCatalogDiscovery.ts`, a new subscription composable, `src/pages/index.vue`, `src/pages/workspace.vue` or a protected subscription section, `src/features/unified-access`, `src/features/console`, `src/locales/**/common.ts`, local mocks, and related `*.spec.ts`.
- Contract changes: none expected. Existing authority docs already define the API subscription and Unified Access failure contracts required by this proposal.
