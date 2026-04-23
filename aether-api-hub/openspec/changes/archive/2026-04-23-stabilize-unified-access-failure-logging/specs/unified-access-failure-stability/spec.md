## ADDED Requirements

### Requirement: Unified Access target-not-found MUST return platform failure response
The system MUST return the platform failure response defined by `docs/api/unified-access.yaml` when a valid API Key calls a target API code that does not exist.

#### Scenario: Valid credential calls unknown API
- **WHEN** a caller invokes Unified Access with a valid enabled API Key and an unknown `apiCode`
- **THEN** the system returns a `TARGET_NOT_FOUND` platform failure response with code `ASSET_NOT_FOUND`

### Requirement: Unified Access controller binding MUST be stable
The Unified Access HTTP controller MUST bind path variables and request parameters without relying on compiler-retained Java parameter names.

#### Scenario: Path variable binds without reflection metadata
- **WHEN** a client calls `/api/v1/access/{apiCode}`
- **THEN** the controller binds the requested API code explicitly and reaches the unified access application flow

### Requirement: Platform failure response MUST not be overwritten by log persistence failures
The system MUST preserve the original Unified Access platform failure response even if best-effort call-log persistence fails.

#### Scenario: Log write fails during platform failure handling
- **WHEN** a platform pre-forward failure is produced and call-log persistence fails
- **THEN** the client still receives the original platform failure response rather than a default `500`
