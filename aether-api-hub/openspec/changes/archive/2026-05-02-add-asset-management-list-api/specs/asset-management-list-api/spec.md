## ADDED Requirements

### Requirement: Management asset list MUST expose paged asset browsing across lifecycle states
The system MUST provide a management-facing asset list endpoint under the asset-management controller contract so operators can browse existing API assets without knowing an `apiCode` in advance. The list MUST include assets in `DRAFT`, `ENABLED`, and `DISABLED` states, unlike discovery APIs which expose enabled assets only.

#### Scenario: Return a mixed lifecycle management page
- **WHEN** a management asset list request is executed and the catalog contains draft, enabled, and disabled assets
- **THEN** the system returns a paged list that may include all three lifecycle states

#### Scenario: Keep management list separate from discovery exposure rules
- **WHEN** a draft or disabled asset exists in the write model
- **THEN** the management asset list MAY return it
- **THEN** the discovery-market list behavior remains unchanged

### Requirement: Management asset list MUST support basic management filters
The system MUST allow operators to narrow the management asset list by page, size, asset status, category code, and keyword. The keyword filter MUST be defined by the management contract and MUST NOT rely on undocumented query parameters.

#### Scenario: Filter by asset status
- **WHEN** the caller provides a valid asset status filter
- **THEN** the list returns only assets in that status

#### Scenario: Filter by category
- **WHEN** the caller provides a category code filter
- **THEN** the list returns only assets that belong to that category code

#### Scenario: Filter by keyword
- **WHEN** the caller provides a keyword filter
- **THEN** the list applies the documented keyword matching rule to supported management fields

#### Scenario: Reject undocumented or invalid paging semantics
- **WHEN** the caller provides invalid paging or filter parameters
- **THEN** the system returns a contract-aligned validation failure

### Requirement: Management asset list MUST return operator-readable summary fields
The management asset list MUST return enough summary information for workspace browsing without requiring a detail fetch for every row. At minimum, each item MUST expose the asset identifier and management-visible lifecycle context defined by the contract.

#### Scenario: Return asset summary with lifecycle context
- **WHEN** an asset appears in the management list
- **THEN** the item includes its `apiCode`, asset display name field, asset type, lifecycle status, and last-updated timestamp

#### Scenario: Return category context when available
- **WHEN** a listed asset is associated with a category
- **THEN** the item includes category code
- **THEN** the item includes category name when the category can be resolved through the current category data
