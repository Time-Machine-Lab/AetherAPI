## ADDED Requirements

### Requirement: Console MUST provide a reusable JSON schema display component

`aether-console` SHALL provide a reusable component for displaying nullable JSON Schema content in asset and marketplace surfaces, aligned with `aether-console/DESIGN.md` JSON/code display and state feedback rules.

#### Scenario: Valid JSON schema is displayed

- **WHEN** the component receives a valid JSON string
- **THEN** it formats the JSON and displays it through the console code display pattern
- **THEN** it provides a copy action using existing console copy feedback conventions

#### Scenario: Invalid JSON or plain text is displayed

- **WHEN** the component receives content that cannot be parsed as JSON
- **THEN** it displays the original content as plain text
- **THEN** it shows a non-blocking formatting fallback state

#### Scenario: Schema is absent

- **WHEN** the component receives null, undefined, or blank schema content
- **THEN** it renders a stable empty or unavailable state instead of an empty code block

#### Scenario: Component uses console visual system

- **WHEN** the component renders labels, code, copy action, or empty state
- **THEN** it uses existing console display components, Tailwind tokens, and i18n text instead of hard-coded visible copy
