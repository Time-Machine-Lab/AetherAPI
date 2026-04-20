## ADDED Requirements

### Requirement: Internal maintainers can manage category lifecycle
The system SHALL allow internal maintainers to create API categories with a stable `CategoryCode`, rename the category display name, and enable or disable the category within the API Catalog bounded context.

#### Scenario: Create a new category
- **WHEN** an internal maintainer creates a category with a new `CategoryCode` and category name
- **THEN** the system creates a managed category that can be referenced by API assets

#### Scenario: Reject duplicate category code
- **WHEN** an internal maintainer creates a category with a `CategoryCode` that already exists
- **THEN** the system rejects the creation request

#### Scenario: Rename a category without changing its code
- **WHEN** an internal maintainer renames an existing category
- **THEN** the system updates the category display name and keeps the original `CategoryCode` unchanged

#### Scenario: Disable a category
- **WHEN** an internal maintainer disables an existing category
- **THEN** the system marks the category as unavailable for new asset assignments

### Requirement: Only enabled categories are valid for new asset assignment
The system SHALL treat only enabled API categories as valid classification targets for new API asset registration or reassignment.

#### Scenario: Reject assignment to a disabled category
- **WHEN** downstream asset management attempts to assign a new or revised API asset to a disabled category
- **THEN** the system rejects the assignment as invalid

#### Scenario: Reject assignment to a missing category
- **WHEN** downstream asset management attempts to assign a new or revised API asset to a category that does not exist
- **THEN** the system rejects the assignment as invalid
