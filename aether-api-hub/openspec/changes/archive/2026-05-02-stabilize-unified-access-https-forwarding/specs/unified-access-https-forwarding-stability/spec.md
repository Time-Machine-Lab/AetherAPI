## ADDED Requirements

### Requirement: HTTPS upstream forwarding MUST produce stable execution outcomes
The system MUST classify HTTPS upstream transport and TLS failures as Unified Access execution outcomes after target resolution succeeds.

#### Scenario: HTTPS transport failure is classified as upstream execution failure
- **WHEN** Unified Access resolves a published target API and HTTPS request execution fails before a successful upstream response is received
- **THEN** the system returns a stable upstream execution failure response instead of an uncategorized server error

#### Scenario: HTTPS timeout is classified as upstream timeout
- **WHEN** Unified Access resolves a published target API and the HTTPS upstream request times out
- **THEN** the system returns a stable upstream timeout response

### Requirement: HTTPS diagnostics MUST be sanitized
The system MUST expose enough diagnostic detail to identify the failure category while avoiding leakage of API keys, authorization headers, upstream tokens, or full secret configuration.

#### Scenario: Execution failure does not expose secrets
- **WHEN** an HTTPS upstream execution failure is returned
- **THEN** the response does not include caller API keys, upstream authorization tokens, or raw auth config values

### Requirement: HTTPS success MUST preserve upstream response semantics
The system MUST preserve successful HTTPS upstream status, response body, and allowed response headers without wrapping the success payload in TML Result.

#### Scenario: Successful HTTPS response is passed through
- **WHEN** Unified Access successfully invokes an HTTPS upstream API
- **THEN** the caller receives the upstream success response semantics according to the Unified Access passthrough contract
