## ADDED Requirements

### Requirement: Marketplace detail SHALL display published API JSON schemas

The console marketplace detail SHALL display request/response JSON Schema fields returned by the published Discovery detail contract without inventing schemas from examples or templates.

#### Scenario: Discovery detail includes schemas

- **WHEN** a selected marketplace asset detail includes `requestJsonSchema` or `responseJsonSchema`
- **THEN** the marketplace detail panel MUST display each available schema through the reusable JSON schema display component

#### Scenario: Discovery detail omits schemas

- **WHEN** a selected marketplace asset detail has null or absent schema fields
- **THEN** the marketplace detail panel MUST render a stable unavailable state or omit the schema section according to existing detail layout patterns

#### Scenario: Schemas are not inferred

- **WHEN** Discovery detail includes examples or request templates but no schema fields
- **THEN** the frontend MUST NOT derive or display a fake JSON Schema from those examples or templates
