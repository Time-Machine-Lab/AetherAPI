## ADDED Requirements

### Requirement: Marketplace detail SHALL expose request and response schema through a compact visual inspection workflow

The console marketplace detail SHALL keep request and response schema presentation compact in-page and provide a dedicated visual inspection surface for each available schema.

#### Scenario: Marketplace detail has request schema

- **WHEN** a selected marketplace asset detail includes `requestJsonSchema`
- **THEN** the detail panel MUST show a compact request-schema trigger or summary surface
- **THEN** activating that surface MUST open a dedicated schema inspection overlay for the request schema

#### Scenario: Marketplace detail has response schema

- **WHEN** a selected marketplace asset detail includes `responseJsonSchema`
- **THEN** the detail panel MUST show a compact response-schema trigger or summary surface
- **THEN** activating that surface MUST open a dedicated schema inspection overlay for the response schema

#### Scenario: Large schemas do not overwhelm the detail page

- **WHEN** the request or response schema is large or deeply nested
- **THEN** the marketplace detail page MUST NOT inline the entire visual schema tree in the normal page flow
- **THEN** the primary inspection experience MUST use a dedicated overlay such as a dialog or narrow-screen drawer/sheet

#### Scenario: Schema fields remain contract-backed only

- **WHEN** the selected marketplace detail has request templates or examples but no schema fields
- **THEN** the frontend MUST NOT derive a visual schema from those other fields
- **THEN** the schema inspection entry points MUST remain absent or disabled according to existing empty-state patterns