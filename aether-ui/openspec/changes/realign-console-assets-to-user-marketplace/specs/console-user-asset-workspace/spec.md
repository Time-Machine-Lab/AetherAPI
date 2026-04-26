## ADDED Requirements

### Requirement: Workspace SHALL use current-user asset endpoints
The console asset workspace SHALL call the owner-scoped asset management contract under `v1/current-user/assets` for asset list, create, detail, revise, publish, unpublish, delete, and AI profile maintenance.

#### Scenario: List current-user assets
- **WHEN** the workspace loads or refreshes the asset list
- **THEN** the frontend calls `GET v1/current-user/assets` with documented filters and does not call `GET v1/assets`

#### Scenario: Load current-user asset detail
- **WHEN** a user selects or searches for an owned asset by `apiCode`
- **THEN** the frontend calls `GET v1/current-user/assets/{apiCode}` and maps the response into the workspace asset detail model

#### Scenario: Create current-user asset draft
- **WHEN** a user submits a valid asset creation form
- **THEN** the frontend calls `POST v1/current-user/assets` and displays the returned draft asset

### Requirement: Workspace SHALL use publication lifecycle states
The console asset workspace SHALL represent asset lifecycle state as `DRAFT`, `PUBLISHED`, or `UNPUBLISHED` and SHALL NOT use `ENABLED` or `DISABLED` for asset status.

#### Scenario: Filter by publication state
- **WHEN** a user opens the asset status filter
- **THEN** the available asset filters include all, `DRAFT`, `PUBLISHED`, and `UNPUBLISHED`

#### Scenario: Render publication state labels
- **WHEN** an asset row or detail panel renders a status badge
- **THEN** the badge text and visual treatment reflect draft, published, or unpublished semantics

#### Scenario: Preserve non-asset enablement language
- **WHEN** the workspace renders category or credential status
- **THEN** the frontend continues to use enablement language for those non-asset domains

### Requirement: Workspace SHALL expose publish and unpublish actions
The console asset workspace SHALL allow the current user to publish and unpublish owned assets using the backend publication endpoints.

#### Scenario: Publish an owned asset
- **WHEN** the current asset is not published and the user activates the publish action
- **THEN** the frontend calls `PATCH v1/current-user/assets/{apiCode}/publish` and renders the returned asset state

#### Scenario: Unpublish an owned asset
- **WHEN** the current asset is published and the user activates the unpublish action
- **THEN** the frontend calls `PATCH v1/current-user/assets/{apiCode}/unpublish` and renders the returned asset state

#### Scenario: Publish validation fails
- **WHEN** the publish endpoint rejects the asset because required configuration is incomplete
- **THEN** the workspace shows i18n-backed error feedback without clearing the current asset detail

### Requirement: Workspace SHALL support owner asset revision and AI profile maintenance
The console asset workspace SHALL revise owned asset configuration and maintain AI capability metadata through the documented current-user contract.

#### Scenario: Save asset configuration
- **WHEN** the user saves changes to an owned asset configuration form
- **THEN** the frontend calls `PUT v1/current-user/assets/{apiCode}` with documented fields and renders the returned asset

#### Scenario: Critical revision withdraws published asset
- **WHEN** a saved revision returns an asset status of `UNPUBLISHED`
- **THEN** the workspace updates the status immediately and does not keep displaying the old published state

#### Scenario: Save AI capability profile
- **WHEN** the user saves AI metadata for an `AI_API` asset
- **THEN** the frontend calls `PUT v1/current-user/assets/{apiCode}/ai-profile` using `provider`, `model`, `streamingSupported`, and `capabilityTags`

### Requirement: Workspace SHALL support owner soft delete
The console asset workspace SHALL expose a delete operation for the current asset and remove deleted assets from the active workspace list after a successful response.

#### Scenario: Delete owned asset
- **WHEN** the user confirms deletion of the current asset
- **THEN** the frontend calls `DELETE v1/current-user/assets/{apiCode}` and renders the returned deleted asset state or clears the current selection according to existing workspace feedback patterns

#### Scenario: Refresh after deletion
- **WHEN** an asset delete succeeds
- **THEN** the frontend refreshes or updates the workspace list so the deleted asset is not shown as an active owned asset

### Requirement: Workspace SHALL preserve current-user ownership language
The workspace copy SHALL describe the asset list and detail surfaces as the current user's asset workspace, not a global registered asset management list.

#### Scenario: Render workspace description
- **WHEN** the asset workspace header and empty states render
- **THEN** the visible copy communicates that users are managing their own API assets

#### Scenario: Another user's asset is unavailable
- **WHEN** the detail endpoint returns not found for an `apiCode`
- **THEN** the workspace presents the same unavailable-asset feedback without implying global asset visibility
