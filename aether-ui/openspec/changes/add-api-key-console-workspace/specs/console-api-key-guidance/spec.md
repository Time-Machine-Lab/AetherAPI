## ADDED Requirements

### Requirement: Console SHALL explain one-time secret handling and credential safety

The credential experience SHALL include visible guidance that API Key plaintext is revealed only once after creation and that later console surfaces provide masked display only.

#### Scenario: Showing the post-create reveal guidance

- **WHEN** a newly created API Key is returned by the create flow
- **THEN** the console presents a dedicated reveal state for the one-time plaintext secret
- **THEN** the same state includes security guidance telling the user to save the key now because later views will not show the full secret again

#### Scenario: Re-entering the credential workspace after creation

- **WHEN** the user returns to the workspace after the initial create success state has been dismissed
- **THEN** the console shows masked credential data only
- **THEN** the console keeps reminder copy that full secrets are not recoverable from the UI

### Requirement: Console SHALL provide bounded credential usage guidance

The console SHALL provide guidance about how API Keys fit into the current-user access model, and that guidance SHALL stay within authority documents that already exist for Consumer & Auth.

#### Scenario: Explaining the hidden Consumer model

- **WHEN** the workspace renders guidance content for the user
- **THEN** the console explains that API Keys are managed per signed-in user
- **THEN** the console avoids instructing the user to manually create or manage a separate `Consumer`

#### Scenario: Missing authority docs for executable calling examples

- **WHEN** the frontend lacks an approved authority document for a concrete unified-access request example
- **THEN** the console displays a bounded guidance message or links to the existing docs area
- **THEN** the console MUST NOT synthesize undocumented headers, endpoint paths, or request bodies as if they were official contract

### Requirement: Console SHALL localize all user-visible credential guidance and errors

All user-visible credential guidance, empty states, action feedback, and recoverable error prompts SHALL be delivered through the console's existing i18n structure instead of hard-coded page strings.

#### Scenario: Rendering guidance in different locales

- **WHEN** the user changes the console locale between `zh-CN` and `en-US`
- **THEN** credential guidance, status copy, and user-facing error prompts switch with the active locale
- **THEN** the workspace does not leave newly introduced credential strings outside the i18n resource structure
