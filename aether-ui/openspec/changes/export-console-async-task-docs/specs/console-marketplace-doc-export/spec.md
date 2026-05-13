## ADDED Requirements

### Requirement: Exported API docs MUST include async task query information

When Discovery detail contains enabled async task query configuration, console Marketplace Markdown export MUST include an async task query section.

#### Scenario: Asset declares async task query configuration

- **WHEN** a Marketplace detail has `asyncTaskConfig.enabled` set to true
- **THEN** the exported Markdown MUST include the platform task query endpoint `/api/v1/access/{apiCode}/tasks/{taskId}`
- **THEN** the exported Markdown MUST include query method, auth mode, auth scheme when available, and status/result/error paths
- **THEN** the exported Markdown MUST NOT include private auth override payloads

### Requirement: Exported API docs MUST show async task response structure from configured paths

When async task status/result/error paths are configured, console Marketplace Markdown export MUST generate a best-effort JSON response structure from those paths.

#### Scenario: Simple JSONPath fields are configured

- **WHEN** `statusPath`, `resultPath`, and `errorPath` use simple JSONPath object syntax
- **THEN** the exported Markdown MUST include a JSON response structure containing all configured path leaves

#### Scenario: Path syntax cannot be converted

- **WHEN** a configured path cannot be converted into a simple JSON object structure
- **THEN** the exported Markdown MUST still include the raw configured path value
- **THEN** the exported Markdown MUST NOT invent a structure for that unsupported path
