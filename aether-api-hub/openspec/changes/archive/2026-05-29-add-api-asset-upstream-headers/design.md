## 背景

API Catalog 当前使用 `requestMethod`、`upstreamUrl`、`authScheme` 和 `authConfig` 描述上游调用。Unified Access 会转发允许的调用方请求头、恢复 `Content-Type`，再根据 `authConfig` 应用上游鉴权。Import Agent 也只理解 `authConfig` 这一类上游凭证配置。

这个模型无法覆盖需要固定非鉴权请求头的上游服务，例如 beta 开关、异步提交标记、provider 版本选择器或租户路由提示。这些请求头会影响真实执行，不应被当作元数据扩展或公开 Discovery 字段。

## 目标 / 非目标

**目标：**

- 在 API 资产上存储 nullable、owner-scoped 的上游请求头。
- 允许所有者通过当前用户资产管理 API 创建、修订、清空和读取这些请求头。
- 在普通 Unified Access 提交转发时发送配置请求头。
- 将配置请求头与调用方透传请求头、上游鉴权配置分离。
- 扩展 Import Agent 计划，使请求头抽取与澄清使用结构化字段。

**非目标：**

- 不支持按消费者或按单次调用动态模板化请求头。
- 不在公开 Discovery 中暴露 owner 上游请求头。
- 本变更不新增请求头值加密或 secret vault，先沿用现有存储约定。
- 不允许任意覆盖受保护 HTTP/平台请求头。
- 不在缺少显式异步任务请求头建模的情况下，把 submit-only 请求头自动继承到异步任务查询。

## 决策

### 1. 将请求头建模为结构化条目

使用名为 `upstreamRequestHeaders` 的 nullable 字段，条目形态如下：

```json
[
  { "name": "X-DashScope-Async", "value": "enable" },
  { "name": "OpenAI-Beta", "value": "assistants=v2" }
]
```

这样可以避免多行字符串解析歧义，也便于后端在转发前校验 header name 和 value。

### 2. 在 `api_asset` 上以 nullable JSON text 存储

为 `api_asset` 增加 nullable text 列保存 JSON 快照。该方式与 `auth_config`、`async_task_config` 和扩展块等 JSON-like 字段保持一致，同时是向后兼容的增量改动。

备选方案：存入 `metadata_extensions`。该方案被拒绝，因为这些请求头影响实时上游执行，属于上游端点配置边界，而不是元数据。

### 3. `authConfig` 只负责鉴权

`authConfig` 继续表示 `HEADER_TOKEN` 或 `QUERY_TOKEN` 鉴权。`upstreamRequestHeaders` 只表达固定非鉴权请求头。后端必须拒绝或丢弃受保护名称，包括 `Authorization`、`Content-Type`、`Host`、`Content-Length`、`X-Aether-*` 和 hop-by-hop headers。

如果某个 provider 需要 authorization-like 的自定义凭证头，应优先使用当前支持的 `authScheme` / `authConfig`，或通过后续凭证模型提案处理。

### 4. 在调用方请求头过滤后应用配置请求头

Unified Access 继续沿用当前 allowlist 行为过滤调用方请求头。过滤后再应用资产配置请求头，作为稳定的所有者配置。配置请求头不得绕过受保护名称规则。若出现同名的允许请求头，资产配置值可以覆盖调用方值，以保持 owner 路由/版本要求确定。

### 5. 异步任务查询行为保持显式

普通 submit 转发使用 `upstreamRequestHeaders`。异步任务查询不得盲目继承 submit headers，因为部分 provider 使用 submit-only 请求头，例如 async mode 开关。如果后续需要任务查询请求头，应单独增加 `asyncTaskConfig.requestHeaders` 或显式继承模式。本提案保留该决策，避免意外泄漏。

### 6. 将 Import Agent 扩展为一等计划字段

Import Agent 的 planner schema、计划模型、持久化 JSON、引导式澄清和执行映射都使用 `assetPlans[].upstreamRequestHeaders`。Planner 指令必须明确说明，不要把额外固定请求头写入 `authConfig`。

流式摘要、思考事件、日志和 UI 面向用户的计划文本不得回显敏感形态的请求头值。请求头名通常可展示；值若看起来像 token、key、secret 或 bearer material，应进行脱敏。

## 风险 / 权衡

- [风险] 请求头值可能包含 secret，且以 JSON text 存储。-> 缓解：与现有 `authConfig` 约定保持一致，诊断脱敏，并将 vault 化作为后续安全提案。
- [风险] provider 可能要求 `Content-Type` 或 `Authorization` 变体。-> 缓解：这些继续走现有 body/content-type 和 auth 路径，不允许通用 header list 覆盖核心平台行为。
- [风险] 异步任务查询可能需要不同于提交请求的请求头。-> 缓解：默认不自动继承 submit headers；确有需求时再显式建模。
- [风险] Import Agent 过度推断请求头。-> 缓解：要求基于文档/示例证据；缺少值时创建结构化澄清，而不是编造 header value。

## 迁移计划

- 先更新权威文档：`docs/sql/api-asset.sql`、`docs/api/api-asset-management.yaml`、`docs/api/api-import-agent.yaml` 和相关设计文档。
- 增加 nullable 存储列，确保现有行仍有效。
- 部署后端代码，将缺失或空请求头配置视为无配置请求头。
- 现有资产在所有者或 Import Agent 计划设置 `upstreamRequestHeaders` 前，转发行为保持不变。

## 待确认问题

- 是否现在就为异步任务查询增加独立 `requestHeaders` 列表，还是等 provider 需求出现后再处理？
- 是否应将敏感请求头值与更广泛的 `authConfig` secret 存储提案一起做静态加密？
