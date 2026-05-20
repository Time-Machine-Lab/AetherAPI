## ADDED Requirements

### Requirement: Console Import Agent MUST render clarification defaults

The console Import Agent workspace MUST render optional recommended default metadata on structured clarification items when the backend provides it.

#### Scenario: Clarification item shows a recommended value

- **WHEN** the current plan contains a structured clarification item with `defaultValue`
- **THEN** the workspace shows the recommended value near the corresponding input control
- **AND** the workspace allows the user to adopt or edit that value before submitting an answer

#### Scenario: Default metadata is absent

- **WHEN** a structured clarification item does not include `defaultValue`
- **THEN** the workspace keeps the existing manual input or selection behavior
- **AND** the workspace does not show empty default-value affordances

### Requirement: Console MUST NOT silently submit defaults

The console MUST treat recommended defaults as suggestions until the user explicitly submits them through the Import Agent turn flow.

#### Scenario: Page loads with default values

- **WHEN** the workspace receives clarification items with default values
- **THEN** the workspace does not automatically call the append-turn API
- **AND** no `clarificationAnswers` payload is sent until the user submits the clarification form

#### Scenario: User adopts a default value

- **WHEN** the user adopts a recommended default value and submits the clarification form
- **THEN** the workspace sends a `clarificationAnswers` item containing the adopted value
- **AND** the answer references the original clarification item by `clarificationId` or by its target path and field key

### Requirement: Console MUST present default source and confidence as secondary context

The console MUST present default source and confidence metadata as secondary helper context when those fields are present, without replacing the primary field label or description.

#### Scenario: Source and confidence are present

- **WHEN** a clarification item includes `defaultSource` or `defaultConfidence`
- **THEN** the workspace presents localized helper text or compact labels for those values
- **AND** the primary field label, description, input, and submit action remain visible and usable

#### Scenario: Long default value is displayed safely

- **WHEN** a default value is long JSON, URL, multiline text, or schema-like content
- **THEN** the workspace displays it without overflowing the plan card
- **AND** the user can edit or replace the value before submission

### Requirement: Console MUST remain compatible with old Import Agent responses

The console MUST continue working with Import Agent responses that do not include the new default metadata fields.

#### Scenario: Old response payload is loaded

- **WHEN** the API response contains `clarificationItems` with only the previously supported fields
- **THEN** the workspace renders the existing structured clarification controls
- **AND** no runtime error occurs because default metadata fields are missing

#### Scenario: DTO mapping preserves optional fields

- **WHEN** the API response includes `defaultValue`, `defaultLabel`, `defaultSource`, or `defaultConfidence`
- **THEN** the frontend API mapping preserves those fields in the domain type consumed by the workspace
- **AND** the append-turn request format remains unchanged
