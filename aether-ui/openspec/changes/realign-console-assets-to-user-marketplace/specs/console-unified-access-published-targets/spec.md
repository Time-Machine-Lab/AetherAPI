## ADDED Requirements

### Requirement: Unified Access target assist SHALL use published assets
The Unified Access playground target-assist UI SHALL use discovery assets as published callable marketplace targets and SHALL NOT describe them as enabled assets.

#### Scenario: Load target candidates
- **WHEN** the playground loads target candidates from discovery
- **THEN** the frontend treats returned assets as published marketplace targets

#### Scenario: Render target assist copy
- **WHEN** the playground renders guidance, loading, empty, or picker text for target assets
- **THEN** the visible copy refers to published assets or marketplace assets instead of enabled assets

### Requirement: Unified Access manual target entry SHALL remain available
The Unified Access playground SHALL continue to allow manual `apiCode` entry even when discovery returns no published marketplace assets.

#### Scenario: No published targets returned
- **WHEN** discovery returns an empty item list for target assist
- **THEN** the playground shows the empty target-assist state and keeps manual `apiCode` input usable

#### Scenario: User selects a discovery target
- **WHEN** the user selects a published asset from target assist
- **THEN** the playground fills the target `apiCode` without changing API Key handling or request execution semantics

### Requirement: Unified Access errors SHALL stay scoped to invocation
The Unified Access playground SHALL keep invocation errors separate from console session errors and SHALL NOT introduce owner workspace rules into the playground.

#### Scenario: Published target invocation fails
- **WHEN** a Unified Access call against a selected target fails
- **THEN** the playground renders the standardized invocation error feedback without changing console authentication state

#### Scenario: Discovery target assist fails
- **WHEN** the discovery request used for target assist fails
- **THEN** the playground shows target-assist error feedback while preserving manual invocation controls
