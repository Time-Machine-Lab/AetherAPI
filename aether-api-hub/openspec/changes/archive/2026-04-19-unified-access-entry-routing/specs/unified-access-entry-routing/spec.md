## ADDED Requirements

### Requirement: Unified access entry MUST resolve a single target API before forwarding
The system MUST expose a unified access entry that resolves exactly one enabled target API snapshot before any upstream forwarding begins.

#### Scenario: Resolve target API by access identifier
- **WHEN** a caller requests the unified access entry with a valid API identifier
- **THEN** the system resolves exactly one target API snapshot for that invocation

#### Scenario: Reject unknown API identifier before forwarding
- **WHEN** a caller requests the unified access entry with an API identifier that does not map to an enabled API asset
- **THEN** the system returns a platform-side failure without attempting upstream forwarding

### Requirement: Unified access entry MUST validate caller identity before forwarding
The system MUST validate the caller's API key through `Consumer & Auth` and obtain a `Consumer Context` before the invocation can enter the upstream forwarding phase.

#### Scenario: Stop request when credential validation fails
- **WHEN** the caller submits a request with an invalid or unavailable API key
- **THEN** the system returns a platform-side failure and does not continue to target API forwarding

#### Scenario: Carry resolved Consumer context into invocation
- **WHEN** credential validation succeeds
- **THEN** the unified access entry attaches the resolved `Consumer Context` to the invocation before forwarding

### Requirement: Entry contract MUST stay mapped to a single controller
The system MUST define the unified access entry in one API contract file that maps to one controller implementation, instead of splitting one controller's contract across multiple YAML files.

#### Scenario: Unified access API contract maps to one controller
- **WHEN** the top-level API contract for unified access is generated
- **THEN** it is represented by a single `docs/api/unified-access.yaml` file mapped to `UnifiedAccessController.java`

### Requirement: Entry layer MUST classify platform pre-forward failures
The system MUST classify failures that happen before upstream forwarding begins so downstream components can distinguish platform-side rejection from upstream execution failure.

#### Scenario: Return platform failure for unavailable target
- **WHEN** the target API exists but is not callable because it is disabled or lacks required access configuration
- **THEN** the system returns a classified platform-side failure before upstream forwarding

#### Scenario: Keep pre-forward failure distinct from upstream failure
- **WHEN** the invocation fails before any upstream request is sent
- **THEN** the failure result is classified as a platform pre-forward failure instead of an upstream execution failure
