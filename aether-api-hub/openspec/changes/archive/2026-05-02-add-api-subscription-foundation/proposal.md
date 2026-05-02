## Why

Current API marketplace flow allows users to discover published assets and call them with an API Key, but it does not record whether the caller has subscribed to or been authorized to use a specific asset. This leaves the first-phase marketplace loop incomplete: discover -> subscribe -> call -> observe.

## What Changes

- Add a lightweight API subscription capability for current users to subscribe to published API assets.
- Add current-user subscription APIs for frontend integration: create subscription, list my subscriptions, query subscription status by `apiCode`, and cancel subscription.
- Add an `api_subscription` table as the authoritative subscription relationship between a user/consumer and an API asset.
- Require Unified Access to verify active subscription entitlement before forwarding a non-owner call.
- Allow an API asset owner to call their own published asset without manually subscribing.
- Keep `Consumer` as an internal concept; do not expose Consumer registration or Consumer management APIs.
- Do not implement payment, plans, approval workflow, quota, billing, settlement, or purchase semantics in this change.

## Capabilities

### New Capabilities

- `api-subscription-management`: current-user API subscription lifecycle and query capability.
- `unified-access-subscription-enforcement`: Unified Access must enforce active subscription entitlement before forwarding calls.

### Modified Capabilities

- None.

## Impact

- Top-level docs:
  - Add `docs/api/api-subscription.yaml`, mapped one-to-one to `ApiSubscriptionController.java`.
  - Add `docs/sql/api_subscription.sql`, mapped one-to-one to table `api_subscription`.
  - Update `docs/api/unified-access.yaml` to document subscription-required failure behavior.
  - Add or update the corresponding domain design document under `docs/design/aehter-api-hub/`.
  - Any new `docs/api/` and `docs/sql/` files must be generated with `tml-docs-spec-generate` using the API/SQL templates.
- Backend code:
  - Add subscription domain/application/infrastructure/adapter slices following existing DDD boundaries.
  - Add read ports to resolve published API asset snapshots and current-user consumer mapping without leaking domain models across adapters.
  - Extend Unified Access pre-forward validation with subscription entitlement checks.
- API behavior:
  - Platform business APIs continue to return TML-SDK `Result`.
  - Unified Access success responses continue to passthrough upstream responses and must not be wrapped by TML-SDK `Result`.
  - Unauthorized subscription failures are platform-side pre-forward failures and should be returned before any upstream request is made.
