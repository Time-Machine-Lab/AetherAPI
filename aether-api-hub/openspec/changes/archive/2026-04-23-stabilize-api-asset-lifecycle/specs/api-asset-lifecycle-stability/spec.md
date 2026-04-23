## ADDED Requirements

### Requirement: Asset revision MUST update draft assets without default server errors
The system MUST allow a draft API asset to be revised with category, upstream endpoint, auth scheme, examples, and display fields according to `docs/api/api-asset-management.yaml`. Valid revision requests MUST return the updated asset response and MUST NOT return a Spring default `500` response.

#### Scenario: Revise draft asset with category and upstream config
- **WHEN** a client registers a draft asset and then revises it with valid category code, request method, upstream URL, and auth scheme
- **THEN** the system returns the revised asset detail and persists the revised fields

#### Scenario: Revise draft asset with partial valid fields
- **WHEN** a client revises a draft asset with a valid partial update supported by the revision contract
- **THEN** the system merges the provided fields with the existing asset state and returns the revised asset detail

### Requirement: Asset lifecycle failures MUST return business errors
The system MUST classify expected asset lifecycle failures as business errors instead of leaking default server error responses.

#### Scenario: Revision target is missing
- **WHEN** a client revises an asset code that does not exist
- **THEN** the system returns an asset-not-found business error

#### Scenario: Enable incomplete asset
- **WHEN** a client enables an asset that lacks category or upstream configuration
- **THEN** the system returns the existing activation-incomplete business error

### Requirement: Enabled revised assets MUST become discoverable
The system MUST allow a successfully revised and enabled asset to appear in the API marketplace discovery list.

#### Scenario: Revised asset appears in discovery
- **WHEN** a draft asset is revised with complete valid configuration and then enabled
- **THEN** the asset appears in `GET /discovery/assets`

#### Scenario: Draft asset remains hidden
- **WHEN** an asset remains in draft status after revision failure
- **THEN** the asset does not appear in `GET /discovery/assets`
