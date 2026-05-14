# console-asset-workspace-layout Specification

## Purpose
TBD - created by archiving change improve-console-login-memory-and-asset-layout. Update Purpose after archive.
## Requirements
### Requirement: Asset workspace uses left summary and right list regions
The default API asset management workspace SHALL place recent assets in the upper-left region, the selected/API asset card in the lower-left region, and the API asset list in the right region on wide screens.

#### Scenario: Wide workspace layout
- **WHEN** an operator opens the default API asset management workspace on a wide viewport
- **THEN** recent assets appear above the selected/API asset card in the left region and the API asset list appears in the right region

#### Scenario: No recent assets
- **WHEN** an operator has no recent assets to display
- **THEN** the selected/API asset card remains in the left region and the API asset list remains in the right region without rendering an empty recent-assets card

#### Scenario: Narrow workspace layout
- **WHEN** the viewport cannot support the two-region layout without crowding content
- **THEN** the workspace collapses into a single column that preserves the order recent assets, selected/API asset card, then API asset list

### Requirement: Asset list owns the right-side browsing space
The API asset list SHALL use the right-side workspace region for filtering, browsing, pagination, row actions, and loading/empty/error feedback.

#### Scenario: Browsing assets
- **WHEN** the operator searches, filters, pages, selects, or edits from the API asset list
- **THEN** those controls and row actions remain available inside the right-side list region

#### Scenario: List state feedback
- **WHEN** the API asset list is loading, empty, or has an error
- **THEN** the state feedback appears inside the right-side list region using the existing console state style

### Requirement: Asset editing does not reserve blank workspace space
The asset management workspace SHALL rely on the existing right-side editor drawer for edit flow and MUST NOT keep an unused blank right-side page area for future editing content.

#### Scenario: Opening the asset editor
- **WHEN** the operator opens the editor for an asset
- **THEN** the editor opens in the existing drawer overlay while the page layout remains the recent/selected-left and list-right workspace composition

#### Scenario: Closing the asset editor
- **WHEN** the operator closes the asset editor
- **THEN** the workspace returns to the same recent/selected-left and list-right composition without exposing an empty reserved edit column

### Requirement: Workspace layout preserves existing asset management behavior
The layout change MUST preserve existing asset data, actions, and contract-backed fields from the asset management API.

#### Scenario: Existing asset actions
- **WHEN** an operator uses load, create, edit, publish, unpublish, delete, pagination, or recent-asset selection
- **THEN** the behavior remains consistent with the current API asset management workflow

#### Scenario: Existing visual system
- **WHEN** the workspace renders cards, rows, tags, buttons, fields, or state feedback
- **THEN** those elements continue to follow the existing `aether-console` console design system

