## ADDED Requirements

### Requirement: Console sidebar MUST remain available during page scroll
The protected `aether-console` shell MUST keep the left navigation available while the main content scrolls, rather than allowing the sidebar to scroll away with the page body.

#### Scenario: User scrolls a long console page
- **WHEN** the current console page content exceeds the viewport height and the user scrolls downward
- **THEN** the left sidebar MUST remain available within the shell viewport
- **THEN** sidebar overflow MUST be handled within the navigation column instead of removing shell navigation from view

### Requirement: Console sidebar MUST support expand and collapse behavior
The protected console shell MUST provide a sidebar expand/collapse interaction so operators can choose between a full navigation rail and a compact navigation rail without losing access to retained routes.

#### Scenario: User collapses the sidebar
- **WHEN** the operator activates the sidebar collapse control
- **THEN** the shell MUST switch the sidebar to a compact state that still preserves icon-based access to the retained navigation entries
- **THEN** the shell MUST continue to indicate the active navigation destination clearly in the collapsed state

#### Scenario: User expands the sidebar again
- **WHEN** the operator activates the sidebar expand control from the compact state
- **THEN** the shell MUST restore the full navigation labels and grouping without changing the current route

### Requirement: Sidebar usability changes MUST preserve retained navigation behavior
Sticky and collapsible sidebar behavior MUST remain compatible with the existing retained route and hash model for `catalog-browse`, `catalog-manage`, `credentials`, `api-call-logs`, and `unified-access-playground`.

#### Scenario: User navigates while the sidebar is sticky or collapsed
- **WHEN** the operator changes routes or workspace hashes using the retained navigation entries
- **THEN** the shell MUST preserve the same retained destinations and active-state semantics that existed before the usability enhancement
- **THEN** the usability enhancement MUST NOT reintroduce temporarily hidden navigation entries as part of the collapse behavior
