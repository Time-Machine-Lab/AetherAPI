## ADDED Requirements

### Requirement: Console SHALL provide a current-user API Key workspace

The `aether-console` protected workspace SHALL expose a credential-management section for the currently signed-in user through the existing `credentials` navigation entry, and it SHALL consume the current-user API Key contract from `docs/api/api-credential.yaml`.

#### Scenario: Entering the credential workspace

- **WHEN** an authenticated user opens the `credentials` section from the console sidebar
- **THEN** the console routes the user to the protected workspace credential area rather than a placeholder panel
- **THEN** the page loads data through a dedicated API-layer module instead of issuing raw requests from the page component

### Requirement: Console SHALL support masked list and detail browsing

The console SHALL let the current user browse masked API Keys, inspect a selected key, and see contract-backed lifecycle fields including status, expiration, revocation timestamp, creation timestamp, update timestamp, and `lastUsedSnapshot` when present.

#### Scenario: Listing current-user API Keys

- **WHEN** the credential workspace requests the current user's API Keys
- **THEN** the console renders paged items from the list response
- **THEN** each rendered item shows `maskedKey` and other non-secret fields only
- **THEN** the console MUST NOT expose `plaintextKey` on list or detail refresh flows

#### Scenario: Viewing a credential with no usage snapshot yet

- **WHEN** a credential response contains a null or empty `lastUsedSnapshot`
- **THEN** the console shows an explicit empty usage state
- **THEN** the console MUST NOT fabricate local usage history or inferred auth outcomes

### Requirement: Console SHALL support the current-user lifecycle actions defined by contract

The console SHALL let the current user create, enable, disable, and revoke API Keys using the operations defined in `docs/api/api-credential.yaml`, and it SHALL refresh the displayed credential state after each successful mutation.

#### Scenario: Creating an API Key

- **WHEN** the user submits a valid create form in the credential workspace
- **THEN** the console calls the create endpoint defined by the authority contract
- **THEN** the success state shows the returned `plaintextKey` exactly in the post-create reveal flow
- **THEN** subsequent list or detail reloads show only masked key data

#### Scenario: Mutating credential status

- **WHEN** the user triggers enable, disable, or revoke on a credential and the backend accepts the action
- **THEN** the console updates the visible status using the latest API response
- **THEN** unavailable lifecycle actions are not presented as if they were valid current-state operations

### Requirement: Console SHALL preserve the hidden-Consumer model in credential management

The credential workspace SHALL express the product as "my API Keys" and MUST NOT expose explicit `Consumer` identifiers, creation actions, or editable `Consumer` fields as part of the user workflow.

#### Scenario: Rendering credential ownership context

- **WHEN** the workspace renders credential data and helper copy
- **THEN** the user-facing interface talks about the current user's API Keys
- **THEN** the console does not render an explicit `Consumer` management form, table, or primary identifier as an operable object
