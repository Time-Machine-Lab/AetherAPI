## ADDED Requirements

### Requirement: Unified Access MUST apply bound platform proxy profiles

When a resolved target API asset has a platform proxy profile binding, Unified Access SHALL route the outbound upstream request through that proxy profile. When no proxy profile is bound, Unified Access SHALL preserve the existing direct outbound forwarding behavior.

#### Scenario: Bound profile routes outbound request through proxy

- **WHEN** Unified Access resolves a published target API asset with an enabled platform proxy profile binding
- **THEN** the outbound upstream request is executed with a proxied HTTP client configured from that platform proxy profile

#### Scenario: Missing binding uses direct forwarding

- **WHEN** Unified Access resolves a published target API asset without a proxy profile binding
- **THEN** the outbound upstream request is executed with the direct HTTP client behavior used before this change

### Requirement: Unified Access MUST keep proxy details internal

Unified Access SHALL keep proxy profile host, port, username, password, and internal profile identifiers out of API consumer responses and forwarded upstream request headers unless the proxy protocol itself requires them at the transport layer.

#### Scenario: Consumer success response has no proxy metadata

- **WHEN** a proxied Unified Access invocation succeeds
- **THEN** the API consumer receives the upstream success status, body, and allowed response headers without proxy profile metadata

#### Scenario: Upstream request does not receive platform proxy metadata

- **WHEN** Unified Access forwards a request through a platform proxy profile
- **THEN** the target upstream API does not receive internal proxy profile identifiers as application-level headers

### Requirement: Proxy execution failures MUST use existing execution outcomes

If target resolution succeeds and outbound execution fails because of proxy connection, proxy authentication, DNS, TLS, request execution, or timeout behavior, Unified Access SHALL classify the failure as an upstream execution failure or upstream timeout according to the existing execution outcome model.

#### Scenario: Proxy transport failure is upstream execution failure

- **WHEN** Unified Access resolves a target API and the configured proxy cannot establish the upstream request
- **THEN** the system returns an upstream execution failure outcome instead of a platform pre-forward rejection

#### Scenario: Proxy timeout is upstream timeout

- **WHEN** Unified Access resolves a target API and the proxied outbound request times out
- **THEN** the system returns an upstream timeout outcome

#### Scenario: Failure detail redacts proxy secrets

- **WHEN** a proxied outbound execution failure response includes diagnostic detail
- **THEN** the response does not include proxy passwords, proxy authorization values, upstream authorization tokens, or caller API keys

### Requirement: Disabled bound profiles MUST prevent unsafe forwarding

If an API asset remains bound to a proxy profile that becomes disabled or deleted after binding, Unified Access SHALL avoid silently falling back to direct outbound forwarding.

#### Scenario: Disabled bound profile blocks forwarding

- **WHEN** Unified Access resolves a target API whose stored `proxy_profile_id` points to a disabled profile
- **THEN** the system does not send the request directly to upstream as a fallback

#### Scenario: Deleted bound profile blocks forwarding

- **WHEN** Unified Access resolves a target API whose stored `proxy_profile_id` points to a deleted or missing profile
- **THEN** the system does not send the request directly to upstream as a fallback
