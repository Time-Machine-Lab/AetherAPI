## ADDED Requirements

### Requirement: Unified Access requires subscription entitlement before forwarding
The system SHALL verify subscription entitlement before forwarding a Unified Access request to upstream when the caller is not the owner of the target API asset.

#### Scenario: Forward subscribed caller request
- **WHEN** a request has a valid API Key, resolves to a published non-deleted API asset, and the caller has an ACTIVE subscription to the target apiCode
- **THEN** Unified Access forwards the request to the upstream API

#### Scenario: Forward owner request without subscription record
- **WHEN** a request has a valid API Key and the caller owns the published non-deleted target API asset
- **THEN** Unified Access forwards the request without requiring an api_subscription record

#### Scenario: Reject missing subscription before upstream
- **WHEN** a request has a valid API Key and resolves to a published non-deleted API asset but the caller is not the owner and has no ACTIVE subscription
- **THEN** Unified Access returns a platform-side authorization failure and MUST NOT send any request to upstream

#### Scenario: Reject cancelled subscription before upstream
- **WHEN** a request has a valid API Key and resolves to a published non-deleted API asset but the caller's subscription is CANCELLED
- **THEN** Unified Access returns a platform-side authorization failure and MUST NOT send any request to upstream

### Requirement: Subscription failure is documented in Unified Access API contract
The system SHALL document subscription entitlement failure as a Unified Access pre-forward platform failure.

#### Scenario: Missing subscription response contract
- **WHEN** Unified Access rejects a call because subscription entitlement is missing
- **THEN** `docs/api/unified-access.yaml` documents a 403 response with a stable business code such as `API_SUBSCRIPTION_REQUIRED`

### Requirement: Unified Access success response remains upstream passthrough
The system SHALL preserve existing Unified Access success response semantics after subscription enforcement is added.

#### Scenario: Successful call response passthrough
- **WHEN** a Unified Access request passes API Key validation, target resolution, subscription entitlement, and upstream invocation succeeds
- **THEN** the response preserves upstream status/body semantics and is not wrapped by TML-SDK Result
