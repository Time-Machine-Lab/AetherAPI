## Why

Import Agent 目前会把 `requestJsonSchema` 和 `responseJsonSchema` 当作普通字符串交给 LLM 直接生成，再由后端原样透传到资产计划和资产主数据中。实际使用中已经出现“请求体 schema 是不合法字符串”的问题，说明仅靠通用 planner 生成字段不足以保证可读、可解析、可复用的请求/响应契约快照。

现有 `schemaHints` 只是 `extract_import_facts` 工具中预留的事实提取字段，不是专门的 Schema 生成子 agent；当前代码也没有任何 subagent 消费 `schemaHints`。因此需要新增一个专职 Schema 生成与规范化子 agent，把请求示例、响应示例、文档字段说明和 `schemaHints` 汇总成合法 JSON Schema 字符串。

## What Changes

- 新增 Import Agent 内部 `schema_generation` 子 agent，专门生成、补齐和规范化 `assetPlans[].requestJsonSchema` 与 `assetPlans[].responseJsonSchema`。
- 将现有 `schemaHints` 定位为该子 agent 的可选输入来源之一，而不是把它视为已经存在的 Schema 生成能力。
- 在 planner 汇总阶段新增确定性 Schema 规范化规则：只接受可解析的 JSON 对象或 JSON 字符串；对象应序列化为紧凑 JSON 字符串；非法 JSON 或明显非 JSON Schema 的文本不得写入可执行计划。
- 当文档证据不足以生成合法 Schema 时，Import Agent 应保留 schema 字段为空，必要时输出中文澄清问题，而不是写入伪 JSON、Markdown、示例正文或自然语言说明。
- 不新增数据库字段，不新增 Import Agent 公共接口字段；继续使用现有 `requestJsonSchema` / `responseJsonSchema` 契约。
- 不引入运行时请求/响应校验，也不改变 Unified Access 调用行为。

## Capabilities

### New Capabilities

- `api-import-agent-schema-generation-subagent`: Import Agent 内部 Schema 生成子 agent、Schema 输入归一化、合法性检查和澄清行为。

### Modified Capabilities

- 无。当前已归档 OpenSpec specs 中尚无 Import Agent session 能力；本变更以新增能力描述 Import Agent 内部规划行为。

## Impact

- 权威文档：现有 `docs/api/api-import-agent.yaml` 已包含计划响应里的 `requestJsonSchema` / `responseJsonSchema` 字段；现有 `docs/api/api-asset-management.yaml` 和 `docs/sql/api-asset.sql` 已定义资产主数据中的 JSON Schema 快照字段。本提案不新增或修改这些顶层 API/SQL 契约。
- 后端基础设施 planner：影响 `ImportAgentPlannerSubagentRole`、`ImportAgentPlannerSubagentRegistry`、新增 Schema 子 agent、`ImportAgentPlannerJsonSupport` / draft parser 或 normalizer 中的 Schema 规范化逻辑。
- 后端测试：需要覆盖合法对象输入、合法字符串输入、非法字符串拒绝、`schemaHints` 被消费、无证据时不生成伪 schema、中文澄清问题等场景。
- 兼容性：对旧会话和旧客户端为 additive；已有合法 schema 字符串应继续保留。
