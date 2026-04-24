## ADDED Requirements

### Requirement: Unified Access MUST classify malformed upstream target URIs as execution failures
When Unified Access has already resolved a target API and the outbound forwarding stage fails because the configured upstream URI cannot be constructed into a valid HTTP request target, the system MUST classify that result as an upstream execution failure instead of a platform pre-forward rejection.

#### Scenario: Missing URI scheme causes outbound request construction failure
- **WHEN** a Unified Access invocation reaches the downstream forwarding stage and the resolved target API contains an upstream URL without a valid URI scheme
- **THEN** the system returns an execution-failure response with HTTP status `502`
- **AND** the response uses the `UPSTREAM_EXECUTION_FAILURE` error code family rather than `INVALID_API_CODE`, `TARGET_NOT_FOUND`, or other platform pre-forward failure types

### Requirement: Unified Access controller contract MUST document execution-failure outcomes
The system MUST keep the top-level Unified Access authority contract aligned with client-visible execution outcomes exposed by `UnifiedAccessController.java`.

#### Scenario: Authority contract is updated for execution failures
- **WHEN** the Unified Access API authority document is generated or updated for this change
- **THEN** it uses a single `docs/api/unified-access.yaml` file mapped to `UnifiedAccessController.java`
- **AND** it explicitly documents `502` upstream execution failure and `504` upstream timeout responses
