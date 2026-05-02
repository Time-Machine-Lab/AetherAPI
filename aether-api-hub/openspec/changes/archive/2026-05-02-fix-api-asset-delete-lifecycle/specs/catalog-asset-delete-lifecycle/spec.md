## ADDED Requirements

### Requirement: Owner asset deletion MUST persist as a soft delete
The system MUST persist a delete request for an owner-scoped API asset by marking the `api_asset` row deleted through the `is_deleted` lifecycle marker defined in `docs/sql/api-asset.sql`.

#### Scenario: Delete a draft asset
- **WHEN** the owner deletes a draft API asset through `DELETE /api/v1/current-user/assets/{apiCode}`
- **THEN** the asset is persisted with `is_deleted = true`
- **THEN** a subsequent active lookup for that asset does not return it

#### Scenario: Delete an unpublished asset
- **WHEN** the owner deletes an unpublished API asset through `DELETE /api/v1/current-user/assets/{apiCode}`
- **THEN** the asset is persisted with `is_deleted = true`
- **THEN** a subsequent active lookup for that asset does not return it

#### Scenario: Delete a published asset
- **WHEN** the owner deletes a published API asset through `DELETE /api/v1/current-user/assets/{apiCode}`
- **THEN** the asset is persisted with `is_deleted = true`
- **THEN** the asset is no longer an active marketplace or invocation target

### Requirement: Delete MUST NOT be modeled as unpublish
The system MUST keep publication state and deletion state separate. It MUST NOT satisfy a delete request by only changing a published asset to `UNPUBLISHED` while leaving it active.

#### Scenario: Published asset delete is not only status demotion
- **WHEN** the owner deletes a published API asset
- **THEN** the asset's active lifecycle is removed by the deletion marker
- **THEN** the asset does not reappear in the current-user active asset list after refresh

### Requirement: Deleted assets MUST be hidden from active reads
The system MUST exclude deleted API assets from all active read paths covered by API Catalog, Catalog Discovery, and Unified Access.

#### Scenario: Current-user list hides deleted asset
- **WHEN** an owner deletes an API asset and then reloads the current-user asset list
- **THEN** the deleted asset is not included in the list response

#### Scenario: Current-user detail hides deleted asset
- **WHEN** an owner deletes an API asset and then requests its current-user detail by `apiCode`
- **THEN** the system returns the same not-found behavior used for missing or non-owned assets

#### Scenario: Discovery hides deleted published asset
- **WHEN** a published API asset is deleted and a marketplace discovery list or detail request is executed
- **THEN** the deleted asset is not exposed through Catalog Discovery

#### Scenario: Unified Access rejects deleted target asset
- **WHEN** a unified access invocation targets an API asset that has been deleted
- **THEN** the target resolution fails as if the asset is not an active callable target

### Requirement: Deleted assets MUST reject further owner lifecycle operations
The system MUST prevent further active owner lifecycle operations on a deleted API asset.

#### Scenario: Deleted asset cannot be republished
- **WHEN** an owner deletes an API asset and then attempts to publish it again
- **THEN** the system rejects the operation with the same business error category used for unavailable assets

#### Scenario: Deleted asset cannot be revised
- **WHEN** an owner deletes an API asset and then attempts to revise its configuration
- **THEN** the system rejects the operation with the same business error category used for unavailable assets
