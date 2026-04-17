## ADDED Requirements

### Requirement: Design authority MUST be synchronized before implementation
The project SHALL update `aether-console/DESIGN.md` to define the refreshed control-panel hierarchy for shell search, page search, notice banners, list/detail composition, and workspace alignment before implementation code for this change begins.

#### Scenario: Design rules are available before coding
- **WHEN** implementation work for the refreshed console visuals is started
- **THEN** `aether-console/DESIGN.md` MUST already describe the hierarchy and alignment rules introduced by this change

### Requirement: Marketplace browsing hierarchy MUST remain visually unambiguous
The API marketplace page SHALL differentiate shell-level utilities, page-level browsing controls, asset list cards, and the selected detail panel so that users can identify the primary browsing action and current reading focus without relying on hover alone.

#### Scenario: Page search is presented as the primary browsing control
- **WHEN** the marketplace page renders its shell header and page-local search on desktop layouts
- **THEN** the page-local search MUST carry the stronger content emphasis and the shell-level search MUST appear visually subordinate

#### Scenario: List and detail regions feel structurally aligned
- **WHEN** asset cards and the detail panel are displayed side by side
- **THEN** the two regions MUST follow aligned section edges, consistent elevation rules, and stable empty/loading/error states without one control appearing to protrude beyond its section

### Requirement: Workspace management surfaces MUST share a consistent alignment grid
The category management and API asset management panels SHALL use consistent control heights, horizontal baselines, row padding, and action spacing across create, query, inline-edit, and recent-item flows.

#### Scenario: Rename mode preserves the row rhythm
- **WHEN** a category row enters rename mode
- **THEN** the inline input and its adjacent actions MUST align to the row baseline and preserve the same minimum interaction height class as neighboring controls

#### Scenario: Search and action rows align across workspace panels
- **WHEN** the workspace page displays category creation and asset lookup rows in the same viewport
- **THEN** the inputs and buttons across those rows MUST follow the same alignment, spacing, and height rules
