## 1. Top-Level Contracts

- [x] 1.1 Read `docs/spec/` backend development constitution and confirm DDD/module dependency constraints before code changes.
- [x] 1.2 Use `tml-docs-spec-generate` with the domain design template to create/update the API Subscription domain design document under `docs/design/aehter-api-hub/`.
- [x] 1.3 Use `tml-docs-spec-generate` with the API template to create `docs/api/api-subscription.yaml`, mapped one-to-one to `ApiSubscriptionController.java`.
- [x] 1.4 Use `tml-docs-spec-generate` with the SQL template to create `docs/sql/api_subscription.sql`, mapped one-to-one to table `api_subscription`.
- [x] 1.5 Use `tml-docs-spec-generate` with the API template to update `docs/api/unified-access.yaml` with the 403 subscription-required failure contract.

## 2. Subscription Domain And Application

- [x] 2.1 Add subscription domain model/value objects/status rules for `ApiSubscriptionAggregate`.
- [x] 2.2 Add domain rules for subscribing only to published non-deleted assets, preventing owner subscription records, idempotent active subscriptions, and cancelling active subscriptions.
- [x] 2.3 Add repository/read ports for subscription persistence, target asset lookup, and implicit current-user Consumer resolution.
- [x] 2.4 Add application commands/queries/results for create subscription, list current-user subscriptions, query status by apiCode, and cancel subscription.
- [x] 2.5 Implement subscription application service orchestration without exposing domain models to adapter DTOs.

## 3. Persistence And API Adapter

- [x] 3.1 Add `api_subscription` persistence entity/mapper aligned 100% with `docs/sql/api_subscription.sql`.
- [x] 3.2 Implement subscription repository adapter and indexes/query methods for current user, apiCode, status, and consumer lookup.
- [x] 3.3 Add `ApiSubscriptionController.java` and Req/Resp DTOs aligned 100% with `docs/api/api-subscription.yaml`.
- [x] 3.4 Ensure subscription business APIs return TML-SDK `Result` and use existing current-user context handling.

## 4. Unified Access Enforcement

- [x] 4.1 Add a subscription entitlement port for Unified Access to check owner access or ACTIVE subscription by consumer/apiCode.
- [x] 4.2 Integrate entitlement check after API Key validation and target asset resolution, before upstream forwarding.
- [x] 4.3 Return documented 403 platform-side failure when subscription is missing or cancelled, without calling upstream.
- [x] 4.4 Preserve Unified Access successful upstream passthrough response behavior and avoid TML-SDK Result wrapping for success payloads.

## 5. Verification

- [x] 5.1 Add tests for subscribe success, unavailable asset rejection, owner subscription rejection/status, duplicate subscribe idempotency, list isolation, status query, and cancel.
- [x] 5.2 Add Unified Access tests for active subscription forwarding, owner forwarding, missing subscription rejection, and cancelled subscription rejection.
- [x] 5.3 Run relevant Maven tests for affected modules and record any test gaps if local environment cannot execute them.
- [x] 5.4 Re-run `openspec status --change add-api-subscription-foundation` and confirm artifacts remain apply-ready.
