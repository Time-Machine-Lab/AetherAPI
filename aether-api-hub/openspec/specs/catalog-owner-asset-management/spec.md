# catalog-owner-asset-management Specification

## Purpose

Define owner-scoped API asset management so authenticated console users can create, revise, publish, unpublish, and delete only their own marketplace assets.
## Requirements
### Requirement: Current authenticated users MUST create owned API assets
The system MUST allow the current authenticated console user to create an API asset that is owned by that user and starts in draft state. When a backend import execution creates assets on behalf of the current authenticated user, it MUST preserve the same ownership and draft-first semantics instead of bypassing them.

#### Scenario: Create an owned draft asset
- **WHEN** the current authenticated user submits a valid create-asset request
- **THEN** the system creates a new draft API asset owned by that user

#### Scenario: Reject duplicate API code across the marketplace
- **WHEN** the current authenticated user submits a create-asset request whose `apiCode` already exists for another asset
- **THEN** the system rejects the request instead of creating a second asset with the same code

#### Scenario: Import execution creates an owned draft asset
- **WHEN** a confirmed import execution creates a new asset for the current authenticated user
- **THEN** the system creates that asset as owned by the current authenticated user and applies the same draft-first ownership rules as the direct asset management flow

### Requirement: Current authenticated users MUST query only their own asset workspace
The system MUST provide owner-scoped asset workspace queries for the current authenticated user and MUST NOT expose another user's write-model asset workspace through those APIs.

#### Scenario: List the current user's assets
- **WHEN** the current authenticated user requests their asset workspace list
- **THEN** the system returns only assets owned by that user

#### Scenario: View detail of an owned asset
- **WHEN** the current authenticated user requests the detail of an asset they own
- **THEN** the system returns that owned asset's workspace detail

#### Scenario: Reject another user's asset workspace access
- **WHEN** the current authenticated user requests asset workspace detail for an asset owned by another user
- **THEN** the system rejects or hides that asset instead of exposing another user's write-model asset data

### Requirement: Owners MUST maintain configuration only for their own assets
The system MUST allow an asset owner to revise the configuration of their own asset, including AI capability metadata when applicable, and MUST keep ownership checks outside controllers and persistence adapters. Import execution that revises an existing owned asset MUST reuse the same ownership and lifecycle validation instead of applying a parallel write path.

#### Scenario: Owner revises an owned draft asset
- **WHEN** an asset owner updates the configuration of their own draft asset
- **THEN** the system persists the updated asset configuration successfully

#### Scenario: Owner updates AI capability metadata for an owned AI asset
- **WHEN** an asset owner updates AI capability metadata for their own `AI_API` asset
- **THEN** the system stores the AI capability metadata as part of that owned asset

#### Scenario: Non-owner cannot revise asset configuration
- **WHEN** a user attempts to revise an asset owned by another user
- **THEN** the system rejects the request instead of applying the configuration change

#### Scenario: Import execution reuses owner-scoped revision rules
- **WHEN** a confirmed import execution revises an existing asset for the current authenticated user
- **THEN** the system applies the same owner-scoped validation, AI profile rules, and lifecycle checks as the direct owner asset revision flow

### Requirement: Owners MUST publish and unpublish their own assets through validation
The system MUST allow an asset owner to publish and unpublish their own assets, and publication MUST succeed only when the asset satisfies the required marketplace completeness rules.

#### Scenario: Publish a complete owned asset
- **WHEN** an asset owner publishes an owned asset whose required configuration is complete
- **THEN** the system marks the asset as published

#### Scenario: Reject publication of an incomplete owned asset
- **WHEN** an asset owner attempts to publish an owned asset whose required configuration is incomplete
- **THEN** the system rejects the publish request

#### Scenario: Critical revision withdraws a previously published asset
- **WHEN** an asset owner changes a published asset's critical upstream configuration
- **THEN** the system transitions that asset out of published state until the owner republishes it

#### Scenario: Owner unpublishes an owned asset
- **WHEN** an asset owner unpublishes their own published asset
- **THEN** the system removes that asset from the marketplace callable set

### Requirement: Owners MUST be able to soft-delete their own assets
The system MUST allow an asset owner to soft-delete their own asset so it is no longer discoverable or callable while preserving historical identity for governance and logs.

#### Scenario: Owner soft-deletes an asset
- **WHEN** an asset owner deletes their own asset
- **THEN** the system marks the asset deleted instead of exposing it as an active marketplace asset

#### Scenario: Deleted asset is absent from owner active workspace list
- **WHEN** the current user requests their active asset workspace list after deleting one of their assets
- **THEN** the deleted asset is not returned as an active asset

### Requirement: Asset management APIs MUST map to one controller authority file
The system MUST define the owner-scoped asset management contract in `docs/api/api-asset-management.yaml`, and that authority file SHALL map one-to-one to `ApiAssetController.java`. Any required asset schema updates MUST be reflected in `docs/sql/api-asset.sql`.

#### Scenario: Generate asset management authority files
- **WHEN** the project updates the owner-scoped asset management contract or the `api_asset` table structure
- **THEN** it updates `docs/api/api-asset-management.yaml` and `docs/sql/api-asset.sql` with `tml-docs-spec-generate` before code implementation

### Requirement: 所有者资产管理 MUST 维护上游请求头

系统 MUST 允许当前认证资产所有者为自有 API 资产创建、修订、清空和读取 nullable 上游请求头配置。该行为的权威文件是 `docs/api/api-asset-management.yaml`（对应 `ApiAssetController.java`）和现有 `docs/sql/api-asset.sql`（对应 `api_asset` 表）。

#### Scenario: 创建带上游请求头的资产

- **WHEN** 当前认证用户提交包含 `upstreamRequestHeaders` 的创建资产请求
- **THEN** 系统将这些请求头条目保存到新建的草稿 API 资产上

#### Scenario: 修订上游请求头

- **WHEN** 资产所有者使用新的 `upstreamRequestHeaders` 列表修订自有资产
- **THEN** 系统持久化该资产修订后的请求头配置

#### Scenario: 清空上游请求头

- **WHEN** 资产所有者使用 null 或空上游请求头配置修订自有资产
- **THEN** 系统按照文档化 API 契约，将该资产请求头配置保存为对应的 null 或空状态

#### Scenario: 在所有者详情中返回上游请求头

- **WHEN** 当前认证用户请求自有 API 资产详情
- **THEN** owner-scoped 资产响应包含 nullable `upstreamRequestHeaders`

#### Scenario: 拒绝不安全请求头名

- **WHEN** 资产所有者尝试保存平台、传输、鉴权或内容语义保留的请求头名
- **THEN** 系统以客户端可见的校验失败拒绝该配置，并且不持久化不安全请求头

#### Scenario: 代码实现前更新权威文档

- **WHEN** 项目实现 API 资产上游请求头存储能力
- **THEN** 必须先更新 `docs/api/api-asset-management.yaml` 和 `docs/sql/api-asset.sql`，再实现后端代码

### Requirement: Owner asset management MUST accept and return asset extension blocks

The system MUST allow the current authenticated asset owner to create, revise, clear, and read nullable `capabilityExtensions`, `policyExtensions`, and `metadataExtensions` fields for an owned API asset through the owner-scoped asset management contract.

#### Scenario: Create asset with extension blocks

- **WHEN** the current authenticated user submits a create-asset request with one or more non-null extension blocks
- **THEN** the system stores those blocks on the created draft asset

#### Scenario: Revise extension blocks on an owned asset

- **WHEN** an asset owner revises an owned asset with updated extension block content
- **THEN** the system persists the revised extension block content for that asset

#### Scenario: Clear extension blocks on an owned asset

- **WHEN** an asset owner revises an owned asset with null values for one or more extension blocks
- **THEN** the system stores those extension blocks as null instead of retaining stale content

#### Scenario: Return extension blocks in owner detail

- **WHEN** the current authenticated user requests detail for an owned API asset
- **THEN** the owner-scoped asset response includes nullable `capabilityExtensions`, `policyExtensions`, and `metadataExtensions`

### Requirement: Asset management authority MUST document extension block fields without removing existing fields

The system MUST update the owner-scoped asset management authority document to add the extension block fields while preserving the existing top-level field contract.

#### Scenario: Generate owner asset management authority doc

- **WHEN** the project updates owner asset management for this change
- **THEN** it updates `docs/api/api-asset-management.yaml`, mapped one-to-one to `ApiAssetController.java`
- **AND** it adds the extension block request and response fields without removing existing asset fields from the contract

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

