# Aether API Hub API Subscription领域设计文档

## 1. 领域定位

API Subscription 负责维护“当前用户是否拥有某个已发布 API 资产的使用资格”。

它补齐的不是支付交易能力，而是 API 市场的一期最小闭环：

- 用户在 API 市场发现已发布 API
- 用户一键订阅 API
- 用户在控制台查看已订阅 API
- 用户携带 API Key 通过 Unified Access 调用 API
- Unified Access 在转发前校验订阅资格

本领域不改变 API Catalog 的资产所有权模型，也不把 Consumer 暴露为用户需要理解和操作的显式概念。

## 2. 统一语言

- API 订阅：当前用户对某个已发布 API 资产的使用资格记录。
- 订阅用户：发起订阅的当前登录用户。
- 订阅 Consumer：订阅用户在调用链路中的内部 Consumer 映射。
- 目标 API：被订阅和调用的 API 资产，以 `apiCode` 作为业务标识。
- 所有者访问：API 资产所有者访问自己发布的 API，不需要额外订阅记录。
- 订阅状态：`ACTIVE` 表示有效订阅，`CANCELLED` 表示已取消订阅。
- 访问状态：控制台展示用状态，包含 `SUBSCRIBED`、`NOT_SUBSCRIBED`、`OWNER`。

## 3. 聚合设计

### 3.1 聚合根

`ApiSubscriptionAggregate` 是本领域的核心聚合根。

它维护：

- 订阅标识：`subscriptionId`
- 订阅用户：`subscriberUserId`
- 订阅 Consumer：`subscriberConsumerId`
- 目标 API：`apiCode`
- 资产所有者快照：`assetOwnerUserId`
- 资产名称快照：`assetName`
- 订阅状态：`ACTIVE / CANCELLED`
- 创建、更新、取消、删除与版本信息

### 3.2 为什么这样设计

订阅是用户和 API 资产之间的使用关系，不属于 API Catalog 的资产生命周期，也不属于 Consumer & Auth 的 API Key 生命周期。

将订阅独立建模可以避免两个问题：

- 不把“谁能使用某个 API”的规则塞进 API Key。
- 不把“市场使用关系”混入 API 资产自身的发布、下架、删除规则。

一期只保留最小聚合，不引入订单、套餐、价格、审批、配额和结算模型。

## 4. 核心规则

- 只能订阅 `PUBLISHED` 且未删除的 API 资产。
- 用户不能为自己拥有的 API 资产创建订阅记录。
- 同一个 Consumer 对同一个 `apiCode` 只能存在一个有效订阅。
- 重复订阅已有效订阅的 API 时，返回已有订阅，不创建重复记录。
- 取消订阅只影响订阅资格，不删除 API 资产，不影响 API Key。
- 已取消订阅不能继续作为 Unified Access 的有效调用资格。
- Consumer 仍然是内部概念，订阅接口只表达“当前用户订阅 API”。

## 5. 业务能力边界

本领域负责：

- 当前用户订阅已发布 API
- 当前用户查询自己的订阅列表
- 当前用户查询某个 API 的订阅状态
- 当前用户取消自己的订阅
- 为 Unified Access 提供订阅资格判断

本领域不负责：

- API 资产创建、发布、下架、删除
- API Key 生成、启用、停用、吊销
- Consumer 显式注册和显式管理
- 支付、购买、套餐、审批、配额、计费、结算
- 上游 API 转发和响应透传
- 调用日志查询

## 6. 业务接口映射

本领域对应 `ApiSubscriptionController.java`，接口统一走当前用户语义：

- `POST /api/v1/current-user/api-subscriptions`
- `GET /api/v1/current-user/api-subscriptions`
- `GET /api/v1/current-user/api-subscriptions/status?apiCode={apiCode}`
- `PATCH /api/v1/current-user/api-subscriptions/{subscriptionId}/cancel`

接口契约文件：

- `docs/api/api-subscription.yaml`

存储契约文件：

- `docs/sql/api_subscription.sql`

## 7. 对其他领域的影响

### 7.1 对 API Catalog

API Catalog 仍然只负责 API 资产主数据和生命周期。

订阅领域只读取已发布且未删除的资产快照，不修改资产状态。

### 7.2 对 Consumer & Auth

Consumer 仍然隐式存在。

订阅创建时，如果当前用户尚未拥有内部 Consumer，应用层可以复用现有规则自动创建用户到 Consumer 的映射。

### 7.3 对 Unified Access

Unified Access 在完成 API Key 校验、目标 API 解析之后，必须在转发上游之前校验：

- 调用方是 API 资产所有者；或
- 调用方 Consumer 对目标 `apiCode` 存在 `ACTIVE` 订阅。

若不满足，返回平台侧 403 错误，不请求上游。

### 7.4 对 Observability

一期调用日志无需强制记录订阅 ID。

后续如果需要做订阅维度统计，可在当前订阅模型基础上扩展日志快照。

## 8. 本期结论

API Subscription 是 Aether API Hub 一期市场闭环的必要补齐点。

它采用轻量授权关系模型，优先保证“发现 -> 订阅 -> 调用”的主链路成立，同时避免过早引入交易系统复杂度。
