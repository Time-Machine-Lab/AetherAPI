# Aether API Hub API Catalog领域设计文档

## 1. 领域定位

API Catalog 是 Aether API Hub 中负责“API 资产主数据与生命周期”的核心领域。

它解决的问题不是“平台录入了哪些 API”，而是：

- 哪个用户发布了哪些 API 资产
- 这些资产当前是否具备发布条件
- 哪些资产可以进入市场被浏览
- 哪些资产可以被 Unified Access 调用

API Catalog 是用户赋能型市场模型，不是平台维护型目录模型。

## 2. 统一语言

- API 资产：用户发布到平台中的一个 API 单元，可以是普通 API，也可以是 AI API
- 资产所有者：创建并维护该 API 资产的当前用户
- 发布：使资产进入市场可见与可调用状态
- 下架：将已发布资产移出市场和调用集合，但保留资产本身
- 删除：软删除资产，使其不再出现在工作台有效列表、市场和统一调用中
- 发布者快照：用于市场展示的发布者名称快照，不依赖额外用户查询链路

## 3. 聚合设计

### 3.1 聚合根

`ApiAssetAggregate` 是本领域唯一核心聚合根。

它负责维护：

- 资产标识：`apiCode`
- 所有者：`ownerUserId`
- 发布者展示快照：`publisherDisplayName`
- 资产类型：普通 API / AI API
- 资产配置：分类、上游地址、鉴权方案、示例
- 资产状态：`DRAFT / PUBLISHED / UNPUBLISHED`
- 删除状态

### 3.2 为什么这样设计

这样设计的原因很直接：

- 资产所有权是生命周期规则的一部分，不是附属关系
- 发布、下架、修改、删除都围绕单个资产展开，天然适合一个聚合根收口
- 如果把 owner 放到外部关系表，会让“是否允许当前用户操作该资产”分散到多个地方判断
- 第一阶段目标是快速落地，不需要为了像 DDD 而额外制造 Provider、Publisher 等空壳聚合

### 3.3 值对象

- `ApiCode`
- `CategoryRef`
- `UpstreamEndpointConfig`
- `ExampleSnapshot`
- `AiCapabilityProfile`

这些对象只表达资产内部语义，不单独承担跨聚合生命周期。

## 4. 生命周期规则

### 4.1 状态

- `DRAFT`：草稿，可持续编辑，尚未进入市场
- `PUBLISHED`：已发布，可被市场浏览与 Unified Access 调用
- `UNPUBLISHED`：已下架，不再对外可见，但可继续维护后重新发布

删除由 `isDeleted` 表达，不与状态枚举混用。

### 4.2 状态迁移

- 新建资产默认进入 `DRAFT`
- 配置完整后可从 `DRAFT` 发布为 `PUBLISHED`
- 已发布资产可主动下架为 `UNPUBLISHED`
- `UNPUBLISHED` 资产可重新发布
- 已发布资产若修改关键配置，自动回退为 `UNPUBLISHED`
- 删除后的资产不再允许继续操作

### 4.3 发布校验

发布前至少满足：

- 资产名称存在
- 分类有效
- 上游请求方法存在
- 上游地址存在
- 上游鉴权配置完整
- 若是 `AI_API`，必须具备 AI 能力档案

## 5. 领域约束

- `apiCode` 全局唯一，创建后不可修改
- 资产只能由所有者本人操作
- Controller、Mapper、适配器中不写所有权规则
- Discovery 与 Unified Access 只能消费“已发布且未删除”的资产
- 发布者信息只保留最小展示快照，不引入额外用户查询依赖

## 6. 业务能力边界

### 6.1 本领域负责

- 当前用户创建资产草稿
- 当前用户查看自己的资产工作台列表
- 当前用户查看自己的资产详情
- 当前用户修改自己的资产配置
- 当前用户维护 AI 能力档案
- 当前用户发布、下架、删除自己的资产
- 提供市场发现与统一调用所需资产主数据

### 6.2 本领域不负责

- API Key 生成与生命周期
- Consumer 显式管理
- 用户登录
- 统一调用代理实现
- 日志查询视图

## 7. 业务接口映射

本领域对应 `ApiAssetController.java`，接口统一走当前用户工作台语义：

- `GET /api/v1/current-user/assets`
- `POST /api/v1/current-user/assets`
- `GET /api/v1/current-user/assets/{apiCode}`
- `PUT /api/v1/current-user/assets/{apiCode}`
- `PUT /api/v1/current-user/assets/{apiCode}/ai-profile`
- `PATCH /api/v1/current-user/assets/{apiCode}/publish`
- `PATCH /api/v1/current-user/assets/{apiCode}/unpublish`
- `DELETE /api/v1/current-user/assets/{apiCode}`

市场发现仍由 `CatalogDiscoveryController.java` 提供只读接口，不和资产工作台混合。

## 8. 对其他领域的影响

### 8.1 对 Discovery

Discovery 只能返回：

- `PUBLISHED`
- `isDeleted = false`

并附带最小发布者展示信息。

### 8.2 对 Unified Access

Unified Access 解析目标资产时，只允许命中：

- `PUBLISHED`
- `isDeleted = false`
- 上游配置完整

### 8.3 对 Observability

调用日志仍按 Consumer 调用链路记录，但目标资产标识必须来自新的市场资产语义。

## 9. 本期设计结论

API Catalog 本期不再采用“平台录入、平台启停”的旧模型，而是切换为“当前用户拥有、当前用户发布、市场统一消费”的新模型。

这套模型的价值是：

- 与产品理念一致
- 与控制台当前用户接口一致
- 与 API 市场浏览链路一致
- 与 Unified Access 可调用集合一致
- 避免后续继续在错误语义上迭代
