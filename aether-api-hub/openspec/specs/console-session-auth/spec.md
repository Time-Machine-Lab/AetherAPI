# console-session-auth Specification

## Purpose
TBD - created by archiving change add-console-session-auth. Update Purpose after archive.
## Requirements
### Requirement: Console sign-in contract
The system MUST provide a console sign-in API contract in the repository root `docs/api/console-auth.yaml`, and that contract SHALL map one-to-one to `ConsoleAuthController.java`. A successful sign-in request MUST authenticate a backend-managed console login subject and return a backend-issued bearer token, token expiry information, and the current console user profile required by the console application.

#### Scenario: Console sign-in succeeds
- **WHEN** the client submits valid console sign-in credentials to the console sign-in endpoint
- **THEN** the system returns a successful response containing a backend-issued bearer token, token expiry information, and the authenticated console user profile

#### Scenario: Console sign-in fails
- **WHEN** the client submits invalid console sign-in credentials to the console sign-in endpoint
- **THEN** the system rejects the request with an authentication failure response and does not issue a bearer token

### Requirement: Console current-session query
The system MUST provide a current-session query endpoint in `ConsoleAuthController` so the console frontend can restore authenticated state after page refresh. The endpoint MUST require a valid backend-issued bearer token and MUST return the current console user profile associated with that token.

#### Scenario: Current session is returned
- **WHEN** the client calls the current-session endpoint with a valid backend-issued bearer token
- **THEN** the system returns the current console user profile associated with that authenticated session

#### Scenario: Current session query is rejected
- **WHEN** the client calls the current-session endpoint without a valid backend-issued bearer token
- **THEN** the system rejects the request with an authentication failure response

### Requirement: Protected console APIs resolve current user from console token
Protected console business APIs that rely on the current user context MUST resolve `Principal` from the backend-issued console bearer token before controller business logic executes. Missing or invalid console tokens MUST be rejected by the authentication chain instead of falling through to blank-principal business exceptions.

#### Scenario: Current-user API executes with authenticated principal
- **WHEN** the client calls a protected console API such as current-user API key management or current-user call log query with a valid console bearer token
- **THEN** the system establishes the authenticated `Principal` before invoking controller business logic and the API executes against that current user context

#### Scenario: Current-user API is blocked before business execution
- **WHEN** the client calls a protected console API without a valid console bearer token
- **THEN** the system rejects the request in the authentication chain and does not enter the controller business flow that depends on `Principal`

### Requirement: Console session auth remains separate from API consumer auth
Console session authentication MUST remain isolated from Unified Access API consumer authentication. Console bearer tokens SHALL authenticate console business interfaces only, and Unified Access consumer requests MUST continue to use API Key validation defined by the existing consumer authentication capability.

#### Scenario: Console token does not replace API Key auth
- **WHEN** a caller invokes Unified Access with only a console bearer token and no valid API Key
- **THEN** the system does not treat the console bearer token as a substitute for API consumer authentication

#### Scenario: API Key auth remains unchanged
- **WHEN** a caller invokes Unified Access with a valid API Key according to the existing consumer authentication rules
- **THEN** the request continues to follow the existing API consumer authentication flow regardless of whether console session authentication exists

