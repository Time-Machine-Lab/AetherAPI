## ADDED Requirements

### Requirement: Sign-in form hydrates saved credentials
The console sign-in form SHALL read its remembered credential state from `localStorage` when the sign-in form is created.

#### Scenario: Saved account only
- **WHEN** the browser has a saved login account without a saved password
- **THEN** the sign-in form shows the saved account, leaves the password empty, and leaves remember password unchecked

#### Scenario: Saved account and password
- **WHEN** the browser has a saved login account and a saved password
- **THEN** the sign-in form shows both values and marks remember password as checked

#### Scenario: Missing or invalid saved data
- **WHEN** the browser has no saved credential state, malformed saved credential state, or unavailable storage
- **THEN** the sign-in form remains usable with empty credential fields and no blocking error

### Requirement: Successful sign-in persists account and optional password
The console sign-in form SHALL update remembered credential state only after a successful sign-in.

#### Scenario: Remember password is unchecked
- **WHEN** the operator signs in successfully with remember password unchecked
- **THEN** the system stores the login account for future form hydration and removes any previously saved password

#### Scenario: Remember password is checked
- **WHEN** the operator signs in successfully with remember password checked
- **THEN** the system stores the login account, stores the submitted password, and preserves the checked remember-password state for future form hydration

#### Scenario: Sign-in fails
- **WHEN** the sign-in request fails
- **THEN** the system does not overwrite the remembered credential state with the failed submission values

### Requirement: Credential memory does not change console authentication contract
The console sign-in memory behavior MUST remain a frontend-only form convenience and MUST NOT change the console-auth request, response, bearer-token storage, or redirect behavior.

#### Scenario: Sign-in request remains unchanged
- **WHEN** the operator submits the sign-in form
- **THEN** the system calls the existing sign-in flow with `loginName` and `password` only

#### Scenario: Existing session behavior remains unchanged
- **WHEN** sign-in succeeds and the console session is stored
- **THEN** the system continues to use the existing bearer-token session storage independently from remembered credential state
