## ADDED Requirements

### Requirement: Console MUST provide current-user API subscription status for marketplace assets
`aether-console` SHALL query the current user's subscription status for a selected published marketplace asset by using `docs/api/api-subscription.yaml` and the selected asset `apiCode`.

#### Scenario: User selects a published marketplace asset
- **WHEN** an authenticated user selects a marketplace asset with an `apiCode`
- **THEN** the console MUST request the current-user subscription status for that `apiCode`
- **THEN** the console MUST render the status as subscribed, not subscribed, owner access, loading, or error using existing console status feedback patterns

#### Scenario: User owns the selected marketplace asset
- **WHEN** the status response indicates `OWNER` access
- **THEN** the console MUST show a read-only owner-access state
- **THEN** the console MUST NOT render a subscribe or cancel subscription action for that asset

#### Scenario: User is not subscribed to the selected asset
- **WHEN** the status response indicates `NOT_SUBSCRIBED`
- **THEN** the console MUST show a subscribe action for the selected published asset
- **THEN** the action MUST use the selected asset `apiCode` and MUST NOT require the user to choose or manage a Consumer

### Requirement: Console MUST allow current users to subscribe to published API assets
`aether-console` SHALL allow authenticated users to create a subscription to a published marketplace asset through the current-user subscription creation endpoint.

#### Scenario: Subscribe to a published asset
- **WHEN** an authenticated user chooses to subscribe to a selected marketplace asset
- **THEN** the console MUST call the current-user subscription creation endpoint with only the target `apiCode`
- **THEN** the console MUST refresh the selected asset subscription status after the operation succeeds
- **THEN** the console MUST show the returned active subscription or owner-access state without inventing additional fields

#### Scenario: Subscription creation fails
- **WHEN** the subscription creation endpoint returns a contract error
- **THEN** the console MUST show an internationalized error state
- **THEN** the console MUST preserve the selected marketplace asset context so the user can retry or inspect the asset

#### Scenario: Duplicate active subscription
- **WHEN** the backend returns an existing active subscription for a repeated subscription request
- **THEN** the console MUST render the asset as already subscribed
- **THEN** the console MUST NOT create duplicate local subscription rows for the same `apiCode`

### Requirement: Console MUST provide a current-user subscription list
`aether-console` SHALL provide a protected current-user view of API subscriptions using only the authenticated user's subscription list endpoint.

#### Scenario: User opens the subscription list
- **WHEN** an authenticated user opens the console subscription list experience
- **THEN** the console MUST request `GET /api/v1/current-user/api-subscriptions`
- **THEN** the console MUST render only the returned current-user subscription records
- **THEN** each row MUST display available contract fields such as `apiCode`, `assetName`, `subscriptionStatus`, timestamps, and owner snapshot information when present

#### Scenario: Subscription list is empty
- **WHEN** the current-user subscription list returns no records
- **THEN** the console MUST show an internationalized empty state
- **THEN** the empty state MUST NOT imply payment, purchase, plan, approval, or billing concepts

#### Scenario: Subscription list request fails
- **WHEN** the current-user subscription list request fails
- **THEN** the console MUST show an internationalized error state
- **THEN** the console MUST NOT expose records from other users or cached stale rows as authoritative data

### Requirement: Console MUST allow current users to cancel active subscriptions
`aether-console` SHALL allow authenticated users to cancel their own active subscription when the API contract returns a cancellable `subscriptionId`.

#### Scenario: Cancel active subscription
- **WHEN** an authenticated user cancels an active subscription row
- **THEN** the console MUST call `PATCH /api/v1/current-user/api-subscriptions/{subscriptionId}/cancel`
- **THEN** the console MUST refresh the subscription list and any selected marketplace status for the same `apiCode`
- **THEN** the console MUST render the returned subscription as cancelled or inactive according to the contract response

#### Scenario: Cancel action is not available
- **WHEN** a subscription row is already cancelled or lacks a `subscriptionId`
- **THEN** the console MUST NOT render an enabled cancel action for that row

#### Scenario: Cancel request is rejected
- **WHEN** the cancel endpoint rejects the request
- **THEN** the console MUST show an internationalized error state
- **THEN** the console MUST keep the previous row visible until fresh server data is loaded

### Requirement: Console subscription copy and UI semantics MUST stay within lightweight entitlement scope
`aether-console` SHALL describe API subscription as current-user usage entitlement and MUST NOT present it as payment, purchase, approval, quota, billing, settlement, plan, or Consumer management.

#### Scenario: User sees subscription actions and states
- **WHEN** the console renders subscription buttons, status tags, empty states, or errors
- **THEN** all visible copy MUST come from the existing i18n system
- **THEN** the copy MUST avoid payment, purchase, billing, plan, approval, quota, settlement, and Consumer-management language

#### Scenario: User interacts with subscription controls
- **WHEN** the user subscribes, views, or cancels a subscription
- **THEN** the console MUST use existing action, status, field, surface, and state-feedback semantics from `aether-console/DESIGN.md`
- **THEN** read-only states MUST NOT look like clickable actions
