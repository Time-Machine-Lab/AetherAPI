## ADDED Requirements

### Requirement: Action and status components MUST use distinct semantic treatments
The console UI SHALL render badges, tags, toggle states, and actionable buttons with distinct visual treatments so users can separate informational state from clickable commands at a glance.

#### Scenario: Status labels are not mistaken for actions
- **WHEN** category status, asset type, or other state labels are displayed beside actionable controls
- **THEN** the status element MUST use a different fill, border, or elevation treatment from neighboring buttons

#### Scenario: Secondary and disabled actions remain visibly actionable or unavailable
- **WHEN** rename, cancel, enable, disable, or outline-style actions are rendered
- **THEN** each action MUST retain clear affordance cues in default, hover, focus, and disabled states instead of collapsing into undifferentiated text

### Requirement: Notice banners MUST use dedicated feedback styling
The console shell SHALL present top notices with a dedicated banner pattern rather than reusing pill-like utility, badge, or navigation treatments.

#### Scenario: Notice area is visually distinct from chips and badges
- **WHEN** a console notice is shown beneath the shell utilities
- **THEN** it MUST use a banner-level container treatment that is visually distinct from badges, chips, and navigation pills

### Requirement: Inputs and inline edit fields MUST preserve comfortable interaction size
The console UI SHALL use consistent minimum heights and focus treatments for standard inputs and inline edit fields across marketplace and workspace flows unless the design explicitly defines a dedicated search variant.

#### Scenario: Rename input remains usable and visible
- **WHEN** a user activates category rename
- **THEN** the inline input MUST preserve the same minimum interaction-height category as the surrounding control system and show a visible focus treatment

#### Scenario: Search and form fields share consistent state semantics
- **WHEN** users interact with search fields and registration fields in the console
- **THEN** those fields MUST use consistent placeholder, focus, and disabled-state semantics unless the design defines an intentional search-specific exception
