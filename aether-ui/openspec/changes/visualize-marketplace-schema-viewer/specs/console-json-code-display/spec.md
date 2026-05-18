## ADDED Requirements

### Requirement: Console MUST provide visual JSON Schema inspection on top of code display fallback

When the console needs to present contract-backed JSON Schema content, it MUST provide a visual inspection mode in addition to the existing raw JSON/code fallback so users can understand schema structure without reading raw source first.

#### Scenario: User inspects a supported schema structure

- **WHEN** the frontend receives a valid JSON Schema containing common keywords such as `type`, `properties`, `required`, `items`, `enum`, `description`, `format`, `default`, or `nullable`
- **THEN** the console MUST render a visual schema tree instead of only a raw JSON code block
- **THEN** each visible node MUST expose readable metadata for field name, type, and required state when applicable

#### Scenario: User expands and collapses nested schema fields

- **WHEN** the visual schema tree contains nested objects or arrays
- **THEN** the console MUST allow users to expand and collapse nested nodes
- **THEN** the initial presentation MUST remain readable without forcing the user to scan the entire schema at once

#### Scenario: Schema contains enum values or item shape

- **WHEN** a schema node declares enum values or array item structure
- **THEN** the visual schema inspection mode MUST make those values visible without requiring the user to parse the raw JSON source manually

#### Scenario: Schema cannot be fully visualized

- **WHEN** the schema content is invalid JSON or uses unsupported constructs
- **THEN** the console MUST preserve raw JSON/text inspection and copy behavior through the existing code display fallback
- **THEN** the visual viewer MUST NOT block the user from seeing the original source content