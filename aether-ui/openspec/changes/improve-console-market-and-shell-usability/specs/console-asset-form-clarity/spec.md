## ADDED Requirements

### Requirement: Asset-management forms MUST provide persistent field labels
`console-workspace` MUST provide visible labels for asset-management form controls so operators can identify each field even when the control already contains a value.

#### Scenario: User edits a prefilled asset configuration
- **WHEN** an operator opens an asset that already contains configuration values
- **THEN** each visible input, select, textarea, or comparable form control MUST still present a persistent field label
- **THEN** the operator MUST NOT need to clear a value just to understand what field is currently being edited

### Requirement: Form clarity rules MUST cover the retained asset-management flows
The persistent-label requirement MUST cover the retained asset-management forms, including asset lookup, asset registration, asset configuration, and AI profile editing where those controls are rendered.

#### Scenario: User switches between asset-management form sections
- **WHEN** the workspace renders the retained asset-management forms for lookup, registration, configuration, or AI profile editing
- **THEN** each rendered form section MUST follow a consistent label-and-field presentation pattern
- **THEN** placeholders MAY remain as secondary hints, but they MUST NOT be the only field identifier

### Requirement: Labeled forms MUST preserve existing field and surface grammar
The introduction of persistent labels MUST stay within the current console field styling, spacing rhythm, and i18n system rather than creating a separate visual form language.

#### Scenario: Workspace form labels are rendered
- **WHEN** the asset-management forms display visible field labels
- **THEN** those labels MUST use the existing console typography and field hierarchy from `aether-console/DESIGN.md`
- **THEN** all user-visible labels and helper text MUST continue to come from the existing i18n system
