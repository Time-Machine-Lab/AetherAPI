## ADDED Requirements

### Requirement: Platform administrators MUST manage proxy profiles

The system SHALL provide administrator-facing proxy profile management APIs mapped to `PlatformProxyProfileController.java`. These APIs MUST allow administrator-capable console sessions to create, update, list, inspect, enable, disable, and soft-delete platform proxy profiles while rejecting normal current-user sessions.

#### Scenario: Administrator creates a proxy profile

- **WHEN** an administrator-capable console session submits a valid proxy profile with protocol, host, port, and optional credentials
- **THEN** the system persists the proxy profile in `platform_proxy_profile` and returns a successful platform business response

#### Scenario: Normal user cannot manage proxy profiles

- **WHEN** a console session without administrator capability calls a proxy profile management endpoint
- **THEN** the system rejects the request before mutating proxy profile data

#### Scenario: Deleted profile is hidden from active management reads

- **WHEN** a platform proxy profile is soft-deleted
- **THEN** normal list and detail management reads no longer expose it as an active profile

### Requirement: Proxy profile secrets MUST be protected

The system SHALL prevent proxy credentials from being exposed through non-administrator surfaces and SHALL redact sensitive credential values from administrator-facing responses unless a command explicitly accepts new credential input.

#### Scenario: Profile response redacts credentials

- **WHEN** an administrator queries a proxy profile that has username or password credentials
- **THEN** the response identifies that credentials exist but does not return the raw password value

#### Scenario: Asset owner APIs do not expose proxy secrets

- **WHEN** an API asset owner queries their current-user asset detail
- **THEN** the response does not include proxy host, proxy port, proxy username, or proxy password values

### Requirement: Platform administrators MUST bind proxy profiles to API assets

The system SHALL allow administrator-capable console sessions to bind or unbind one enabled proxy profile to one API asset. The `api_asset` table MUST store only an optional `proxy_profile_id` reference for this binding.

#### Scenario: Administrator binds enabled profile to asset

- **WHEN** an administrator binds an enabled non-deleted proxy profile to an existing non-deleted API asset
- **THEN** the asset stores the profile reference as `proxy_profile_id`

#### Scenario: Administrator unbinds asset proxy profile

- **WHEN** an administrator removes proxy binding from an API asset
- **THEN** the asset `proxy_profile_id` becomes null and future Unified Access calls use direct forwarding unless another binding is applied

#### Scenario: Disabled profile cannot be newly bound

- **WHEN** an administrator attempts to bind a disabled or deleted proxy profile to an API asset
- **THEN** the system rejects the binding and leaves the asset binding unchanged

### Requirement: Proxy profile docs MUST remain authoritative

The system SHALL define proxy profile storage and API contracts in the repository root `docs/` authority documents before implementation.

#### Scenario: Proxy profile API maps to one controller

- **WHEN** the proxy profile management contract is generated
- **THEN** it is stored as `docs/api/platform-proxy-profile.yaml` and maps one-to-one to `PlatformProxyProfileController.java`

#### Scenario: Proxy profile SQL maps to one table

- **WHEN** the proxy profile table design is generated
- **THEN** it is stored as `docs/sql/platform_proxy_profile.sql` and describes only table `platform_proxy_profile`
