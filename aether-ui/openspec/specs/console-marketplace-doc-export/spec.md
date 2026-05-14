# console-marketplace-doc-export Specification

## Purpose
TBD - created by archiving change add-console-marketplace-doc-export. Update Purpose after archive.
## Requirements
### Requirement: 市场详情 MUST 支持导出当前 API 文档文件
`aether-console` API 市场 MUST allow users to export the currently loaded marketplace asset detail as a Markdown document file, using only contract-backed Discovery detail fields and the platform Unified Access address.

#### Scenario: 用户导出当前 API 文档
- **WHEN** 用户打开一个市场资产详情，且详情已成功加载
- **THEN** 详情面板 MUST 展示导出 API 文档的操作入口
- **THEN** 用户触发导出时，系统 MUST 下载一个 Markdown 文件
- **THEN** 文件名 MUST include the current asset `apiCode`

#### Scenario: 当前详情尚不可导出
- **WHEN** 市场详情为空、正在加载或加载失败
- **THEN** 系统 MUST NOT 提供可触发的当前 API 文档导出操作

### Requirement: 导出文档内容 MUST 受 Discovery 契约约束
导出的 Markdown 文档 MUST be generated only from `docs/api/api-catalog-discovery.yaml` Discovery detail fields and the documented platform Unified Access path `/api/v1/access/{apiCode}`.

#### Scenario: 详情字段完整
- **WHEN** Discovery detail contains `apiCode`, display name, asset type, category, publisher, published time, description, request method, auth scheme, request template, request example, response example, and AI capability profile
- **THEN** exported Markdown MUST include sections for basic information, platform Unified Access address, request method and auth scheme, request template, request example, response example, and AI capability

#### Scenario: 可选字段缺失
- **WHEN** a Discovery detail optional field is absent or empty
- **THEN** exported Markdown MUST omit that optional section or mark the value as unavailable
- **THEN** exported Markdown MUST NOT infer unavailable fields from unrelated data

#### Scenario: 保护内部字段
- **WHEN** generating exported Markdown
- **THEN** exported Markdown MUST NOT include upstream URL, `authConfig`, platform proxy configuration, private credentials, non-contract parameter schema, non-contract response schema, or invented status code documentation

### Requirement: 市场列表 MUST 支持独立多选导出
API 市场列表 MUST support selecting multiple visible assets for document export without changing the existing card-click detail browsing behavior.

#### Scenario: 用户勾选市场卡片
- **WHEN** 用户点击某个市场卡片的导出选择控件
- **THEN** 系统 MUST toggle that asset in the export selection
- **THEN** 系统 MUST NOT change the currently selected detail because of that checkbox click

#### Scenario: 用户点击市场卡片正文
- **WHEN** 用户点击某个市场卡片正文区域
- **THEN** 系统 MUST preserve the existing behavior of selecting and loading that asset detail
- **THEN** 系统 MUST NOT toggle the export selection because of the card body click

#### Scenario: 存在导出选择
- **WHEN** one or more assets are selected for export
- **THEN** 系统 MUST show a batch export surface with the selected count, a clear selection action, and an export action

### Requirement: 多选导出 MUST 合并成功项并列出失败项
When exporting multiple selected marketplace assets, `aether-console` MUST load each selected asset detail, generate one merged Markdown file for successfully loaded details, and list failed detail loads at the top of the file.

#### Scenario: 多选导出全部成功
- **WHEN** 用户选择多个 API 并触发导出
- **THEN** 系统 MUST request Discovery detail for each selected `apiCode`
- **THEN** 系统 MUST download one merged Markdown file containing one document section per successfully loaded API
- **THEN** the merged file name MUST include the market docs export date

#### Scenario: 多选导出部分失败
- **WHEN** 用户选择多个 API 并触发导出, and at least one selected detail loads successfully while another selected detail fails
- **THEN** 系统 MUST still download a merged Markdown file for successful details
- **THEN** the top of the file MUST list failed `apiCode` values with a failure note
- **THEN** failed items MUST NOT appear as successful API document sections

#### Scenario: 多选导出全部失败
- **WHEN** 用户选择多个 API 并触发导出, and all selected detail requests fail
- **THEN** 系统 MUST NOT download an empty success document
- **THEN** 系统 MUST show an internationalized export failure state or feedback

### Requirement: 导出状态 MUST 防止重复触发
API documentation export actions MUST provide stable loading, disabled, success, and failure feedback using existing console visual and i18n patterns.

#### Scenario: 导出进行中
- **WHEN** a single or batch export is in progress
- **THEN** related export actions MUST be disabled
- **THEN** 系统 MUST communicate that export is in progress

#### Scenario: 导出完成
- **WHEN** a Markdown file download is triggered successfully
- **THEN** 系统 MUST provide internationalized success feedback

#### Scenario: 导出失败
- **WHEN** Markdown generation or file download cannot be completed
- **THEN** 系统 MUST provide internationalized failure feedback without clearing the user's current market selection

### Requirement: Exported API docs MUST include async task query information

When Discovery detail contains enabled async task query configuration, console Marketplace Markdown export MUST include an async task query section.

#### Scenario: Asset declares async task query configuration

- **WHEN** a Marketplace detail has `asyncTaskConfig.enabled` set to true
- **THEN** the exported Markdown MUST include the platform task query endpoint `/api/v1/access/{apiCode}/tasks/{taskId}`
- **THEN** the exported Markdown MUST include query method, auth mode, auth scheme when available, and status/result/error paths
- **THEN** the exported Markdown MUST NOT include private auth override payloads

### Requirement: Exported API docs MUST show async task response structure from configured paths

When async task status/result/error paths are configured, console Marketplace Markdown export MUST generate a best-effort JSON response structure from those paths.

#### Scenario: Simple JSONPath fields are configured

- **WHEN** `statusPath`, `resultPath`, and `errorPath` use simple JSONPath object syntax
- **THEN** the exported Markdown MUST include a JSON response structure containing all configured path leaves

#### Scenario: Path syntax cannot be converted

- **WHEN** a configured path cannot be converted into a simple JSON object structure
- **THEN** the exported Markdown MUST still include the raw configured path value
- **THEN** the exported Markdown MUST NOT invent a structure for that unsupported path

