## ADDED Requirements

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
