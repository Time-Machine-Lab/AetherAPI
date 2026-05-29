# console-published-marketplace-discovery Specification

## Purpose
TBD - created by archiving change realign-console-assets-to-user-marketplace. Update Purpose after archive.
## Requirements
### Requirement: Marketplace SHALL consume published discovery assets
The console marketplace browse page SHALL consume `docs/api/api-catalog-discovery.yaml` discovery responses as published marketplace assets and SHALL NOT present discovery results as enabled platform-managed assets.

#### Scenario: Load marketplace list
- **WHEN** the marketplace page loads or searches for assets
- **THEN** the frontend calls `GET v1/discovery/assets` and renders returned items as published marketplace assets

#### Scenario: Discovery list response has no page metadata
- **WHEN** the discovery list response contains `items` without pagination metadata
- **THEN** the frontend still renders the returned asset items without failing

### Requirement: Marketplace SHALL show publisher summary fields
The console marketplace SHALL map and render the minimal publisher summary fields returned by discovery list and detail responses.

#### Scenario: Render publisher on asset card
- **WHEN** a discovery list item contains `publisher.displayName`
- **THEN** the marketplace card makes that publisher display name available in the card model and visible UI

#### Scenario: Render publisher on detail
- **WHEN** a discovery detail response contains `publisher.displayName`
- **THEN** the detail panel renders the publisher display name without exposing owner workspace internals

#### Scenario: Publisher summary is absent
- **WHEN** a discovery item or detail has no publisher summary
- **THEN** the marketplace renders a stable fallback without throwing

### Requirement: Marketplace SHALL show publication time when provided
The console marketplace SHALL preserve `publishedAt` from discovery list and detail responses so users can understand marketplace freshness when the backend provides it.

#### Scenario: Render published time on list item
- **WHEN** a discovery list item contains `publishedAt`
- **THEN** the frontend maps the timestamp into the asset card model and renders it using existing date display conventions

#### Scenario: Render published time on detail
- **WHEN** a discovery detail response contains `publishedAt`
- **THEN** the detail panel renders the published timestamp with i18n-backed labeling

### Requirement: Marketplace SHALL hide write-model-only fields
The console marketplace SHALL NOT render upstream URL, auth configuration secrets, deletion state, or owner workspace-only data from discovery surfaces.

#### Scenario: Render normal API detail
- **WHEN** a published standard API detail is loaded
- **THEN** the detail panel shows browse-safe fields such as method, auth scheme, request template, and examples without showing upstream URL or auth config secrets

#### Scenario: Render AI API detail
- **WHEN** a published AI API detail contains AI capability metadata
- **THEN** the detail panel renders provider, model, streaming capability, and capability tags

#### Scenario: Discovery detail is unavailable
- **WHEN** the backend returns not found for an unpublished, draft, deleted, or unknown asset
- **THEN** the marketplace shows the existing detail error state and does not expose partial write-model data

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

