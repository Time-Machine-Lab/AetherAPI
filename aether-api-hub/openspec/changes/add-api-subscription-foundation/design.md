## Context

Aether API Hub currently has three relevant capabilities:

- API Catalog owns API asset metadata and lifecycle. Published, non-deleted assets can appear in the marketplace and be resolved by Unified Access.
- Consumer & Auth owns internal Consumer identity and API Key validation. Consumer remains an implicit internal concept.
- Unified Access validates API Key, resolves target API asset, then forwards the request to upstream while preserving upstream success responses.

The missing link is the usage entitlement between the current user and a published API asset. Without it, any valid API Key can call any published API asset, so the marketplace lacks the explicit discover -> subscribe -> call loop.

This change introduces lightweight subscription as a current-user scoped entitlement record, not as payment or purchase.

## Goals / Non-Goals

**Goals:**

- Add a minimal API subscription domain for current-user subscriptions to published API assets.
- Provide frontend-ready business APIs through `ApiSubscriptionController.java`.
- Add `api_subscription` as the single subscription table.
- Enforce subscription entitlement in Unified Access before upstream forwarding.
- Keep API owners able to call their own published assets without manually subscribing.
- Preserve existing boundary rules: Controller returns DTO/Result, application services orchestrate, domain owns rules, infrastructure persists.

**Non-Goals:**

- No paid purchase, pricing plan, order, billing, settlement, quota, approval, or subscription package model.
- No explicit Consumer registration or Consumer management UI/API.
- No change to API Key creation semantics except reusing the existing implicit Consumer mapping.
- No wrapping of successful Unified Access upstream responses with TML-SDK Result.

## Decisions

### Decision 1: Model subscription as a lightweight entitlement

Use a new `ApiSubscriptionAggregate` to represent the relationship:

- `subscriptionId`
- `subscriberUserId`
- `subscriberConsumerId`
- `apiCode`
- `assetOwnerUserId` snapshot
- `assetName` snapshot for console display
- `status`: `ACTIVE` / `CANCELLED`
- timestamps and soft-delete fields

Rationale: this gives the platform a durable usage relationship without prematurely introducing commerce concepts.

Alternative considered: treat subscription as a frontend-only bookmark. Rejected because Unified Access would still have no authoritative entitlement check.

### Decision 2: Keep Consumer implicit

Subscription APIs are current-user scoped. When a user subscribes, the application layer ensures the existing user -> Consumer mapping exists, but does not expose Consumer as a user-facing concept.

Rationale: this matches the existing Consumer & Auth decision that users should understand "my account and my API Keys", not "register another Consumer".

Alternative considered: add Consumer selection to subscription. Rejected because it adds a confusing business layer and conflicts with the current one-user-one-consumer first-phase model.

### Decision 3: Enforce entitlement in Unified Access

Unified Access MUST check entitlement after API Key validation and target asset resolution, before upstream forwarding:

```text
API Key valid
-> target asset published and not deleted
-> caller owns target asset OR caller has ACTIVE subscription
-> forward upstream
```

Rationale: this creates the real marketplace usage loop and prevents a valid API Key from becoming global access to every published API.

Alternative considered: only show subscriptions in console but do not enforce them. Rejected because it would not fix the core authorization gap.

### Decision 4: Owner access is implicit

The asset owner can call their own published asset without creating an `api_subscription` row. Subscription status APIs should expose this as an owner-access state so the frontend can avoid showing a misleading Subscribe button.

Rationale: a publisher should not need to subscribe to their own asset.

Alternative considered: auto-create subscription on publish. Rejected because it pollutes subscription records with ownership facts already represented by API Catalog.

### Decision 5: Top-level docs must be updated before code

The implementation must first update authoritative docs:

- `docs/api/api-subscription.yaml` -> `ApiSubscriptionController.java`
- `docs/sql/api_subscription.sql` -> table `api_subscription`
- `docs/api/unified-access.yaml` -> add 403 subscription-required failure
- Domain design document under `docs/design/aehter-api-hub/`

Any new `docs/api/` and `docs/sql/` files must be generated with `tml-docs-spec-generate` using the API and SQL templates.

## Risks / Trade-offs

- [Risk] Existing callers may currently rely on "valid API Key can call every published API" behavior. -> Mitigation: document this as an intentional authorization tightening and ensure frontend subscribes before invoking.
- [Risk] Asset ownership and subscription status can diverge if asset owner changes in the future. -> Mitigation: first phase treats owner change as out of scope; store owner snapshot for display and always resolve current asset owner from API Catalog for enforcement.
- [Risk] Cancelling subscription while calls are in flight may produce timing differences. -> Mitigation: entitlement is checked at request start; no attempt is made to interrupt in-flight upstream calls.
- [Risk] Additional query in Unified Access can add latency. -> Mitigation: check by indexed `(subscriber_consumer_id, api_code, status)` and keep the first implementation database-backed before adding cache.

## Migration Plan

1. Generate/update top-level docs using the required templates.
2. Add `api_subscription` DDL and indexes.
3. Implement subscription domain, repository port, persistence adapter, application service, and controller.
4. Extend Unified Access with a subscription entitlement port and pre-forward check.
5. Add tests for subscribe/list/status/cancel and for Unified Access subscribed, owner, and missing-subscription paths.
6. Rollback strategy: disable the Unified Access entitlement check while retaining subscription records if enforcement causes unexpected integration failures.

## Open Questions

- The first implementation should return a clear owner status from the status query, for example `OWNER`, but the exact frontend display wording can stay outside backend scope.
- If future paid subscriptions arrive, this lightweight `api_subscription` record should become the entitlement output of a purchase/approval process rather than being replaced immediately.
