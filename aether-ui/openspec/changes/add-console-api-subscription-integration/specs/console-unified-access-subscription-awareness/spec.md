## ADDED Requirements

### Requirement: Unified Access playground MUST understand subscription-required platform failures
`aether-console` SHALL treat Unified Access platform failures with `failureType: SUBSCRIPTION_REQUIRED` or `code: API_SUBSCRIPTION_REQUIRED` as a specific subscription-required state.

#### Scenario: Unified Access returns subscription required
- **WHEN** a Unified Access invocation returns a platform failure with `failureType` equal to `SUBSCRIPTION_REQUIRED`
- **THEN** the playground MUST render a subscription-required failure state instead of a generic invocation error
- **THEN** the playground MUST display available contract fields such as `apiCode` and `traceId` without inventing missing data

#### Scenario: Unified Access returns stable subscription code
- **WHEN** a Unified Access invocation returns `code` equal to `API_SUBSCRIPTION_REQUIRED`
- **THEN** the playground MUST render copy that tells the user the target API requires an active subscription or owner access
- **THEN** the copy MUST be internationalized

#### Scenario: Unified Access succeeds after entitlement check
- **WHEN** a Unified Access invocation succeeds
- **THEN** the playground MUST preserve existing upstream passthrough response rendering
- **THEN** the playground MUST NOT wrap or reinterpret successful upstream responses as subscription API responses

### Requirement: Marketplace-to-playground flow MUST carry subscription awareness
`aether-console` SHALL keep the marketplace "open playground" flow aware of the selected asset's subscription status while still allowing manual integration testing.

#### Scenario: User opens playground from a subscribed asset
- **WHEN** a user opens the playground from a marketplace asset whose current-user status is subscribed
- **THEN** the playground MUST prefill or preserve the selected `apiCode`
- **THEN** the playground MUST NOT show a subscription-required warning before the user invokes the API

#### Scenario: User opens playground from an owner-access asset
- **WHEN** a user opens the playground from a marketplace asset whose current-user status is owner access
- **THEN** the playground MUST show owner access as a read-only status when that status is available
- **THEN** the playground MUST NOT ask the user to subscribe to their own asset

#### Scenario: User opens playground from an unsubscribed asset
- **WHEN** a user opens the playground from a marketplace asset whose current-user status is not subscribed
- **THEN** the playground MUST keep manual invocation possible
- **THEN** the playground MUST show guidance that Unified Access may reject the call until the user subscribes or has owner access

### Requirement: Playground target guidance MUST refresh subscription status by apiCode
`aether-console` SHALL query subscription status when the playground has a selected or manually entered `apiCode` that maps to a published marketplace asset.

#### Scenario: User selects a target asset in the playground
- **WHEN** the user selects a published marketplace target from the playground target list
- **THEN** the console MUST query or refresh the current-user subscription status for that target `apiCode`
- **THEN** the guidance panel MUST render subscribed, not subscribed, owner access, loading, or error state according to the response

#### Scenario: User manually edits apiCode
- **WHEN** the user manually edits the playground `apiCode`
- **THEN** the console MAY attempt to load marketplace detail and subscription status for that `apiCode`
- **THEN** if the target cannot be resolved by discovery, the console MUST keep manual invocation available and MUST NOT infer a subscription status

#### Scenario: Subscription status request fails in playground
- **WHEN** the playground cannot load subscription status for the selected `apiCode`
- **THEN** the playground MUST show a non-blocking internationalized status error
- **THEN** the invocation form MUST remain usable because Unified Access remains the enforcement authority

### Requirement: Subscription-aware Unified Access UI MUST preserve credential boundaries
`aether-console` SHALL clearly distinguish bearer-authenticated subscription management APIs from `X-Aether-Api-Key` Unified Access invocation credentials.

#### Scenario: User sees subscription guidance near API Key input
- **WHEN** the playground renders subscription-aware guidance near the API Key input or invocation controls
- **THEN** the guidance MUST distinguish account subscription status from the API Key used for Unified Access invocation
- **THEN** the guidance MUST NOT imply that subscribing creates, reveals, selects, enables, or cancels an API Key

#### Scenario: User subscribes then invokes manually
- **WHEN** a user subscribes to an API and then invokes it from the playground
- **THEN** the subscription operation MUST use the current-user bearer-authenticated API layer
- **THEN** the invocation MUST continue to use `X-Aether-Api-Key` according to `docs/api/unified-access.yaml`
