## ADDED Requirements

### Requirement: Console workspace MUST provide a managed asset browsing entry
`aether-console` MUST provide a managed asset list inside the protected `console-workspace` experience so operators can browse existing API assets without entering an `apiCode` first.

#### Scenario: Operator opens the asset management area
- **WHEN** an authenticated operator enters the asset management section of `console-workspace`
- **THEN** the page renders a management asset list region in the existing workspace flow

#### Scenario: Asset browsing remains inside the existing shell
- **WHEN** the operator uses the asset browsing capability
- **THEN** the experience stays inside the existing console shell and workspace route structure

### Requirement: Console workspace MUST align list filters and paging with the backend management contract
The asset list UI MUST use only the page, size, and documented management filters defined by the updated `../docs/api/api-asset-management.yaml` authority contract. The frontend MUST NOT invent extra request parameters or depend on undocumented response fields.

#### Scenario: Operator filters the asset list
- **WHEN** the operator applies one or more supported management filters
- **THEN** the frontend issues the management list request using only documented filter fields

#### Scenario: Operator changes pages
- **WHEN** the operator navigates to another page of assets
- **THEN** the frontend refreshes the list using the contract-defined paging fields

#### Scenario: Backend contract is not yet available
- **WHEN** the frontend implementation has not yet received the updated management list contract
- **THEN** the asset list capability MUST be treated as blocked rather than inferred from undocumented backend behavior

### Requirement: Console workspace MUST connect list selection to asset management detail behavior
The asset list MUST support selecting a listed asset so the operator can continue management work through the existing asset detail surface and lifecycle actions.

#### Scenario: Operator selects an asset from the list
- **WHEN** the operator selects a row or card from the asset list
- **THEN** the workspace loads that asset into the current asset detail state

#### Scenario: Direct code lookup and list selection converge on shared detail state
- **WHEN** the operator loads an asset by `apiCode` or selects it from the list
- **THEN** the workspace presents the same current asset detail surface and follow-up management actions

### Requirement: Console workspace MUST provide i18n-backed loading, empty, and error feedback for asset browsing
The asset list experience MUST surface loading, empty, and error states using the existing i18n system and console workspace presentation rules.

#### Scenario: Asset list request is loading
- **WHEN** the management asset list request is in progress
- **THEN** the workspace shows a loading state in the asset list region

#### Scenario: Asset list returns no items
- **WHEN** the management asset list returns an empty result
- **THEN** the workspace shows an explicit empty state in the asset list region

#### Scenario: Asset list request fails
- **WHEN** the management asset list request fails
- **THEN** the workspace shows an error state without hard-coded final user text outside the i18n system
