## ADDED Requirements

### Requirement: Unified Access playground SHALL generate copyable curl commands
`aether-console` SHALL allow users to generate and copy curl commands from the current Unified Access playground invocation form using the documented `docs/api/unified-access.yaml` contract.

#### Scenario: Generate command from current invocation fields
- **WHEN** the playground has a valid `apiCode`, HTTP method, API Key, optional request headers, and a selected curl format
- **THEN** the generated command MUST target the documented Unified Access invocation path for that `apiCode`
- **THEN** the command MUST include the selected HTTP method
- **THEN** the command MUST include `X-Aether-Api-Key` with the API Key value currently entered by the user
- **THEN** the command MUST include only optional request headers currently present in the playground form

#### Scenario: Manual apiCode remains supported
- **WHEN** the user manually enters an `apiCode` without selecting a discovery target
- **THEN** the copy-curl action MUST generate the command from that manual `apiCode`
- **THEN** the action MUST NOT require a discovery target selection

#### Scenario: Missing required command input
- **WHEN** `apiCode` or API Key is empty
- **THEN** the playground MUST prevent copying a ready-to-run curl command
- **THEN** the playground MUST render internationalized feedback that identifies the missing command input

### Requirement: Curl generation SHALL support Linux and Windows formats
The Unified Access playground SHALL offer distinct Linux/macOS and Windows curl command formats and SHALL let users copy either format intentionally.

#### Scenario: Linux format is selected
- **WHEN** the user selects the Linux/macOS curl format
- **THEN** the generated command MUST use shell-safe quoting and line continuation suitable for POSIX-style shells
- **THEN** the copy action MUST copy the Linux/macOS formatted command

#### Scenario: Windows format is selected
- **WHEN** the user selects the Windows curl format
- **THEN** the generated command MUST use Windows command-line-safe quoting and line continuation
- **THEN** the copy action MUST copy the Windows formatted command

#### Scenario: User switches format
- **WHEN** the user switches between Linux/macOS and Windows formats
- **THEN** the command preview MUST update without changing `apiCode`, API Key, headers, request body, target assist, subscription guidance, or invocation result state

### Requirement: Curl command body behavior MUST match playground invocation semantics
The generated curl command MUST follow the same request-body inclusion rules as the Unified Access playground invocation flow.

#### Scenario: Body-capable method has request body
- **WHEN** the selected method is `POST`, `PUT`, or `PATCH` and the request body field contains content
- **THEN** the generated command MUST include a JSON content-type header when the playground invocation would send JSON
- **THEN** the generated command MUST include the request body payload using the selected shell format's safe quoting rules

#### Scenario: No-body method is selected
- **WHEN** the selected method is `GET` or `DELETE`
- **THEN** the generated command MUST omit request body payload flags
- **THEN** the generated command MUST NOT copy stale request body content that the invocation flow would not send

#### Scenario: Body-capable method has empty request body
- **WHEN** the selected method is `POST`, `PUT`, or `PATCH` and the request body field is empty
- **THEN** the generated command MUST omit payload content unless the existing invocation flow sends an explicit empty payload

### Requirement: Copy-curl UI MUST preserve credential and subscription boundaries
The copy-curl UI SHALL remain a frontend convenience and MUST NOT alter Unified Access credentials, account subscription state, or console session behavior.

#### Scenario: Subscription status is unavailable or unsubscribed
- **WHEN** subscription guidance is loading, failed, unsubscribed, or unknown
- **THEN** the copy-curl UI MUST remain available when required command inputs are present
- **THEN** the UI MUST NOT imply that copying a command grants subscription, owner access, or API Key access

#### Scenario: API Key is not recoverable
- **WHEN** the API Key field is empty
- **THEN** the copy-curl UI MUST NOT retrieve, infer, or reveal an API Key from account APIs, subscription APIs, local history, or previous command previews

#### Scenario: Clipboard copy succeeds or fails
- **WHEN** the user copies a curl command
- **THEN** the playground MUST show internationalized success feedback if the clipboard write succeeds
- **THEN** the playground MUST show internationalized failure feedback and keep the command preview inspectable if the clipboard write fails
