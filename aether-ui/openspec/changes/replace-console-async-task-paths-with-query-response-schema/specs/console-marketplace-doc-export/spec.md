## MODIFIED Requirements

### Requirement: Exported API docs MUST include async task query information

When Discovery detail contains enabled async task query configuration, console Marketplace Markdown export MUST include an async task query section based on the current Discovery contract.

#### Scenario: Asset declares async task query configuration

- **WHEN** a Marketplace detail has `asyncTaskConfig.enabled` set to true
- **THEN** the exported Markdown MUST include the platform task query endpoint `/api/v1/access/{apiCode}/tasks/{taskId}`
- **THEN** the exported Markdown MUST include query method, auth mode, auth scheme when available, and task query response schema when available
- **THEN** the exported Markdown MUST NOT include private auth override payloads
- **THEN** the exported Markdown MUST NOT include `statusPath`, `resultPath`, or `errorPath`

#### Scenario: Asset omits task query response schema

- **WHEN** a Marketplace detail has enabled async task configuration without `queryResponseJsonSchema`
- **THEN** the exported Markdown MUST still include the task query endpoint and forwarding metadata
- **AND** the exported Markdown MUST mark the task query response schema as unavailable or omit that optional schema subsection

## REMOVED Requirements

### Requirement: Exported API docs MUST show async task response structure from configured paths

**Reason**: The backend async task contract no longer exposes `statusPath`, `resultPath`, or `errorPath`; task-query response shape is described by `queryResponseJsonSchema`.

**Migration**: Export the task query response schema snapshot when `asyncTaskConfig.queryResponseJsonSchema` is present. Do not generate response examples from legacy path fields.
