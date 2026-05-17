## ADDED Requirements

### Requirement: Owner asset management MUST store request and response JSON schemas

The system MUST allow the current authenticated asset owner to create, revise, clear, and read nullable request/response JSON Schema snapshots for an owned API asset. The authority files for this behavior are `docs/api/api-asset-management.yaml` for `ApiAssetController.java` and the existing `docs/sql/api-asset.sql` table document for `api_asset`.

#### Scenario: Create asset with schema snapshots

- **WHEN** the current authenticated user submits a create-asset request with `requestJsonSchema` and `responseJsonSchema`
- **THEN** the system stores those values on the created draft API asset

#### Scenario: Revise asset schema snapshots

- **WHEN** an asset owner revises an owned asset with new `requestJsonSchema` or `responseJsonSchema` values
- **THEN** the system persists the revised schema snapshots for that asset

#### Scenario: Clear asset schema snapshots

- **WHEN** an asset owner revises an owned asset with null schema values
- **THEN** the system stores the corresponding request or response schema snapshot as null

#### Scenario: Return schema snapshots in owner detail

- **WHEN** the current authenticated user requests detail for an owned API asset
- **THEN** the owner-scoped asset response includes nullable `requestJsonSchema` and `responseJsonSchema`

#### Scenario: Update authority documents before code

- **WHEN** the project implements request/response JSON Schema storage for API assets
- **THEN** it updates `docs/api/api-asset-management.yaml` and `docs/sql/api-asset.sql` before backend code implementation
