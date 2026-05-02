# api-subscription-management Specification

## Purpose

Define API subscription management requirements for authenticated users to subscribe to published API assets, query subscription lists and status, cancel subscriptions, and maintain explicit top-level contracts.

## Requirements

### Requirement: Current user can subscribe to a published API asset
The system SHALL allow an authenticated current user to create an active subscription to a published, non-deleted API asset that is not owned by the current user.

#### Scenario: Subscribe to published asset
- **WHEN** an authenticated user requests subscription to a published, non-deleted API asset owned by another user
- **THEN** the system creates an ACTIVE subscription linked to the current user, the implicit Consumer, and the target apiCode

#### Scenario: Reject unavailable asset subscription
- **WHEN** an authenticated user requests subscription to a missing, deleted, draft, or unpublished API asset
- **THEN** the system rejects the request and does not create a subscription record

#### Scenario: Reject owner subscription creation
- **WHEN** an authenticated user requests subscription to an API asset they own
- **THEN** the system does not create an api_subscription record and reports that owner access already applies

### Requirement: Subscription creation is idempotent for active subscriptions
The system SHALL prevent duplicate active subscriptions for the same current user Consumer and apiCode.

#### Scenario: Subscribe twice to same asset
- **WHEN** an authenticated user subscribes to an API asset that already has an ACTIVE subscription for their implicit Consumer
- **THEN** the system returns the existing active subscription instead of creating a duplicate record

### Requirement: Current user can query subscription list
The system SHALL provide a current-user scoped subscription list containing only the authenticated user's non-deleted subscription records and minimal API asset display information.

#### Scenario: List my subscriptions
- **WHEN** an authenticated user requests their subscription list
- **THEN** the system returns only subscriptions linked to that user's implicit Consumer

#### Scenario: Exclude other users subscriptions
- **WHEN** an authenticated user requests their subscription list
- **THEN** subscriptions owned by other users or other Consumers MUST NOT be returned

### Requirement: Current user can query subscription status by apiCode
The system SHALL provide a current-user scoped status query for a target apiCode.

#### Scenario: Active subscription status
- **WHEN** the current user has an ACTIVE subscription to the target apiCode
- **THEN** the system returns a status indicating the API is subscribed and callable subject to API Key validation

#### Scenario: Owner access status
- **WHEN** the current user owns the target API asset
- **THEN** the system returns a status indicating owner access without requiring an api_subscription record

#### Scenario: Not subscribed status
- **WHEN** the current user does not own the target API asset and has no ACTIVE subscription
- **THEN** the system returns a status indicating the API is not subscribed

### Requirement: Current user can cancel an active subscription
The system SHALL allow an authenticated current user to cancel their own active subscription without deleting API assets or API Keys.

#### Scenario: Cancel active subscription
- **WHEN** an authenticated user cancels an ACTIVE subscription linked to their implicit Consumer
- **THEN** the system marks the subscription as CANCELLED and future entitlement checks MUST treat it as inactive

#### Scenario: Reject cancel for other user subscription
- **WHEN** an authenticated user attempts to cancel a subscription not linked to their implicit Consumer
- **THEN** the system rejects the operation

### Requirement: Subscription top-level contracts are explicit
The system SHALL maintain top-level subscription API and SQL contracts before implementing code.

#### Scenario: API contract mapping
- **WHEN** subscription APIs are added
- **THEN** `docs/api/api-subscription.yaml` MUST map one-to-one to `ApiSubscriptionController.java`

#### Scenario: SQL contract mapping
- **WHEN** subscription storage is added
- **THEN** `docs/sql/api_subscription.sql` MUST describe only the `api_subscription` table
