## ADDED Requirements

### Requirement: Workspace SHALL maintain API asset JSON schemas

The console asset workspace SHALL allow asset owners to edit, clear, save, and inspect nullable request/response JSON Schema fields using the documented current-user asset contract.

#### Scenario: Existing schemas are loaded into the edit form

- **WHEN** an asset detail response includes `requestJsonSchema` or `responseJsonSchema`
- **THEN** opening the asset editor MUST prefill the corresponding schema fields

#### Scenario: Schema fields are saved

- **WHEN** the user saves an owned asset with request or response schema content
- **THEN** the revise asset request MUST include `requestJsonSchema` and `responseJsonSchema` with the normalized values

#### Scenario: Blank schema fields are cleared

- **WHEN** the user clears either schema field and saves the asset
- **THEN** the revise asset request MUST send null for the cleared schema field

#### Scenario: Owner detail displays schemas

- **WHEN** an owned asset detail contains schema fields
- **THEN** the workspace MUST display them through the reusable JSON schema display component
