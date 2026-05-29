# console-user-asset-workspace Specification

## Purpose
TBD - created by archiving change realign-console-assets-to-user-marketplace. Update Purpose after archive.
## Requirements
### Requirement: Workspace SHALL use current-user asset endpoints
The console asset workspace SHALL call the owner-scoped asset management contract under `v1/current-user/assets` for asset list, create, detail, revise, publish, unpublish, delete, and AI profile maintenance.

#### Scenario: List current-user assets
- **WHEN** the workspace loads or refreshes the asset list
- **THEN** the frontend calls `GET v1/current-user/assets` with documented filters and does not call `GET v1/assets`

#### Scenario: Load current-user asset detail
- **WHEN** a user selects or searches for an owned asset by `apiCode`
- **THEN** the frontend calls `GET v1/current-user/assets/{apiCode}` and maps the response into the workspace asset detail model

#### Scenario: Create current-user asset draft
- **WHEN** a user submits a valid asset creation form
- **THEN** the frontend calls `POST v1/current-user/assets` and displays the returned draft asset

### Requirement: Workspace SHALL use publication lifecycle states
The console asset workspace SHALL represent asset lifecycle state as `DRAFT`, `PUBLISHED`, or `UNPUBLISHED` and SHALL NOT use `ENABLED` or `DISABLED` for asset status.

#### Scenario: Filter by publication state
- **WHEN** a user opens the asset status filter
- **THEN** the available asset filters include all, `DRAFT`, `PUBLISHED`, and `UNPUBLISHED`

#### Scenario: Render publication state labels
- **WHEN** an asset row or detail panel renders a status badge
- **THEN** the badge text and visual treatment reflect draft, published, or unpublished semantics

#### Scenario: Preserve non-asset enablement language
- **WHEN** the workspace renders category or credential status
- **THEN** the frontend continues to use enablement language for those non-asset domains

### Requirement: Workspace SHALL expose publish and unpublish actions
The console asset workspace SHALL allow the current user to publish and unpublish owned assets using the backend publication endpoints.

#### Scenario: Publish an owned asset
- **WHEN** the current asset is not published and the user activates the publish action
- **THEN** the frontend calls `PATCH v1/current-user/assets/{apiCode}/publish` and renders the returned asset state

#### Scenario: Unpublish an owned asset
- **WHEN** the current asset is published and the user activates the unpublish action
- **THEN** the frontend calls `PATCH v1/current-user/assets/{apiCode}/unpublish` and renders the returned asset state

#### Scenario: Publish validation fails
- **WHEN** the publish endpoint rejects the asset because required configuration is incomplete
- **THEN** the workspace shows i18n-backed error feedback without clearing the current asset detail

### Requirement: Workspace SHALL support owner asset revision and AI profile maintenance
The console asset workspace SHALL revise owned asset configuration and maintain AI capability metadata through the documented current-user contract.

#### Scenario: Save asset configuration
- **WHEN** the user saves changes to an owned asset configuration form
- **THEN** the frontend calls `PUT v1/current-user/assets/{apiCode}` with documented fields and renders the returned asset

#### Scenario: Critical revision withdraws published asset
- **WHEN** a saved revision returns an asset status of `UNPUBLISHED`
- **THEN** the workspace updates the status immediately and does not keep displaying the old published state

#### Scenario: Save AI capability profile
- **WHEN** the user saves AI metadata for an `AI_API` asset
- **THEN** the frontend calls `PUT v1/current-user/assets/{apiCode}/ai-profile` using `provider`, `model`, `streamingSupported`, and `capabilityTags`

### Requirement: Workspace SHALL support owner soft delete
The console asset workspace SHALL expose a delete operation for the current asset and remove deleted assets from the active workspace list after a successful response.

#### Scenario: Delete owned asset
- **WHEN** the user confirms deletion of the current asset
- **THEN** the frontend calls `DELETE v1/current-user/assets/{apiCode}` and renders the returned deleted asset state or clears the current selection according to existing workspace feedback patterns

#### Scenario: Refresh after deletion
- **WHEN** an asset delete succeeds
- **THEN** the frontend refreshes or updates the workspace list so the deleted asset is not shown as an active owned asset

### Requirement: Workspace SHALL preserve current-user ownership language
The workspace copy SHALL describe the asset list and detail surfaces as the current user's asset workspace, not a global registered asset management list.

#### Scenario: Render workspace description
- **WHEN** the asset workspace header and empty states render
- **THEN** the visible copy communicates that users are managing their own API assets

#### Scenario: Another user's asset is unavailable
- **WHEN** the detail endpoint returns not found for an `apiCode`
- **THEN** the workspace presents the same unavailable-asset feedback without implying global asset visibility

### Requirement: 工作台 MUST 维护上游请求头

控制台资产工作台 MUST 允许资产所有者基于已记录的当前用户资产契约编辑、清空、保存和查看可为空的上游请求头配置。

#### Scenario: 现有请求头加载到编辑表单

- **WHEN** 资产详情响应包含 `upstreamRequestHeaders`
- **THEN** 打开资产编辑器时必须预填对应的请求头 name/value 行

#### Scenario: 保存请求头行

- **WHEN** 用户保存包含上游请求头行的自有资产
- **THEN** 修订资产请求必须包含 `upstreamRequestHeaders`，并携带规范化后的 name/value 条目

#### Scenario: 空白或已移除请求头会被清空

- **WHEN** 用户移除所有上游请求头行并保存资产
- **THEN** 修订资产请求必须发送契约中声明的空值或 null，以清空上游请求头

#### Scenario: 所有者详情展示请求头

- **WHEN** 自有资产详情包含上游请求头
- **THEN** 工作台必须将其作为仅所有者可见的上游配置展示，并在视觉上与 `authConfig` 分离

#### Scenario: 市场页面不暴露所有者请求头

- **WHEN** 用户查看已发布市场详情或导出市场文档
- **THEN** 控制台不得展示或推断仅所有者可见的上游请求头，除非未来 Discovery 显式暴露浏览安全的契约

### Requirement: 工作台 MUST 分离上游鉴权和额外请求头

控制台资产工作台 MUST 将 `authScheme` / `authConfig` 编辑与固定非鉴权上游请求头分离。

#### Scenario: 请求头编辑器不替代鉴权配置

- **WHEN** 资产使用 `HEADER_TOKEN` 或 `QUERY_TOKEN` 鉴权
- **THEN** 鉴权配置仍保留在现有鉴权字段中，不得移动到 `upstreamRequestHeaders`

#### Scenario: 保留请求头依赖后端校验

- **WHEN** 后端因请求头名称保留或不安全而拒绝保存上游请求头
- **THEN** 工作台必须展示现有规范化错误状态，不得静默改写用户配置

### Requirement: Workspace SHALL maintain API asset JSON schemas

The console asset workspace SHALL allow asset owners to edit, clear, save, and inspect nullable request/response JSON Schema fields using the documented current-user asset contract.

#### Scenario: Existing schemas are loaded into the edit form

- **WHEN** an asset detail response includes `requestJsonSchema` or `responseJsonSchema`
- **THEN** opening the asset editor MUST prefill the corresponding schema fields

#### Scenario: Schema fields are saved

- **WHEN** the user saves an owned asset with request or response schema content
- **THEN** the revise asset request MUST include `requestJsonSchema` and `responseJsonSchema` with the normalized values

#### Scenario: Blank schema fields are cleared

- **WHEN** the user clears either schema field and saves the asset
- **THEN** the revise asset request MUST send null for the cleared schema field

#### Scenario: Owner detail displays schemas

- **WHEN** an owned asset detail contains schema fields
- **THEN** the workspace MUST display them through the reusable JSON schema display component

