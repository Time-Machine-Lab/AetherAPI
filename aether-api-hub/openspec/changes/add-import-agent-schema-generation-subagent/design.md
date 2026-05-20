## Context

API Catalog 领域已经把 `requestJsonSchema` 和 `responseJsonSchema` 作为 API 资产的一等快照字段保存：`docs/sql/api-asset.sql` 中有 `request_json_schema` 与 `response_json_schema`，`docs/api/api-asset-management.yaml` 和 `docs/api/api-import-agent.yaml` 也已经暴露对应字段。Import Agent 的定位仍然是 API Catalog 前置编排入口，不替代 API Catalog 主模型。

当前 Import Agent planner 有分阶段 tool-calling 和内部 subagent 编排：`document_facts`、`auth_recognition`、`async_pattern`、`plan_review`、`clarification_strategy`。`extract_import_facts` 工具里虽然定义了 `schemaHints`，但它只是 LLM 输出的事实容器；现有代码没有任何 subagent 消费该字段，也没有对 `requestJsonSchema` / `responseJsonSchema` 做 JSON 可解析性检查。

因此，非法 schema 字符串会沿着 `assetPlans[].requestJsonSchema` / `responseJsonSchema` 原样进入计划，再在确认执行时被写入 API 资产。本设计在 planner 基础设施层修复这个问题，不引入新的数据库字段，也不修改公共 Controller 契约。

## Goals / Non-Goals

**Goals:**

- 新增专职 `schema_generation` 子 agent，负责请求/响应 JSON Schema 的生成、补齐、规范化和低置信度过滤。
- 明确 `schemaHints` 只是输入来源之一，而不是已存在的 Schema 生成能力。
- 保证进入最终 Import Agent draft plan 的 `requestJsonSchema` / `responseJsonSchema` 要么为空，要么是可解析的 JSON 对象字符串。
- 对非法、Markdown、自然语言、示例正文等非 Schema 内容进行丢弃或转为中文澄清问题，避免污染可执行计划。
- 保持现有 API/SQL 契约不变，继续复用 `requestJsonSchema` / `responseJsonSchema` 字段。

**Non-Goals:**

- 不新增运行时请求/响应校验。
- 不引入 JSON Schema dialect 版本协商、schema registry 或兼容性分析。
- 不要求所有导入资产必须拥有 schema；缺失 schema 不应单独阻止导入执行，除非后续产品规则另行定义。
- 不新增公共 API 字段或数据库字段。

## Decisions

### 决策 1：新增 `schema_generation` 子 agent，而不是扩展 `document_facts`

`document_facts` 适合提取高确定性的资产身份、方法、地址和分类等事实。JSON Schema 生成需要额外处理示例推断、字段类型归纳、对象/字符串归一化和非法内容过滤，职责明显更重。

新增 `schema_generation` 子 agent 可以保持职责清晰：

- 输入：`extractedFacts.schemaHints`、`slotPatches.assetPlans`、`planSource.assetPlans`、当前计划、文档摘要、最近 turn 中的 request/response 示例。
- 输出：仅更新匹配资产的 `requestJsonSchema` / `responseJsonSchema`，不创建资产、不决定鉴权、不折叠异步任务。
- 顺序：建议在 `async_pattern` 之后、`plan_review` 之前执行，例如 order 35。这样 review 能看到规范化后的 schema 字段。

备选方案是把 schema 逻辑塞进 `plan_review`。拒绝原因是 review 应该偏向审查和降级，而不是承担生成职责。

### 决策 2：Schema 字段在 planner 内部允许对象或字符串，最终计划只保留字符串

现有后端契约把 `requestJsonSchema` / `responseJsonSchema` 定义为字符串快照，所以最终 `ImportAssetPlanModel` 仍保持字符串字段。

但 planner/subagent 内部可以接受两类输入：

- JSON object：直接校验后序列化为紧凑 JSON 字符串。
- JSON string：先 trim，再用 `ObjectMapper.readTree` 解析，解析结果必须是 object。

解析失败、解析为 array/string/number、空对象以外的明显非 schema 文本，都不得进入最终计划。

### 决策 3：只做轻量 JSON Schema 形态校验，不引入完整 dialect 校验

本阶段的目标是防止非法字符串和自然语言污染资产快照，不是实现完整 JSON Schema 引擎。

最低校验规则：

- 必须是 JSON object。
- 如果存在 `type`，其值必须是 JSON Schema 常见类型之一或类型数组。
- 允许 `$schema`、`title`、`description`、`properties`、`required`、`items`、`enum`、`oneOf`、`anyOf`、`allOf`、`additionalProperties` 等常见关键字。
- 如果文档只有示例对象，可生成以 `type: object`、`properties`、可选 `required` 为主的基础 schema。

备选方案是引入 JSON Schema validator 依赖。暂不采用，因为历史设计已明确 schema 快照是可选 JSON 文本，不做运行时 dialect 约束；增加依赖会扩大范围。

### 决策 4：非法 schema 不应让计划失败，而应降级为空或中文澄清

Schema 是增强资产可消费性的元数据，不是当前导入执行的硬门槛。若 schema 证据不足，子 agent 应清空低置信度字段并保留资产导入能力。

当 planner 明确尝试填入 schema 但内容非法时，可追加中文澄清问题，例如：

- “请求体 Schema 需要是合法 JSON 对象，请提供请求示例或字段说明。”
- “响应体 Schema 需要是合法 JSON 对象，请提供响应示例或字段说明。”

如果已有当前计划中存在合法 schema，而新输出非法，应保留当前合法值并记录 review diagnostics。

### 决策 5：API/SQL 顶层文档无需更新

本变更不新增公共请求/响应字段，不改 `api_asset` 表结构。现有权威文件已经覆盖相关字段：

- `docs/api/api-import-agent.yaml`：Import Agent 计划响应中的 schema 字段。
- `docs/api/api-asset-management.yaml`：资产创建、修订、响应中的 schema 字段。
- `docs/sql/api-asset.sql`：`request_json_schema`、`response_json_schema` 字段。

因此实现任务应先确认这些权威文档仍然适用，再进入代码实现；无需调用 `tml-docs-spec-generate` 生成新的 API/SQL 文档。

## Risks / Trade-offs

- [Risk] 轻量校验可能接受语义不完整但语法合法的 JSON Schema。 -> Mitigation：本阶段目标是防止非法字符串；完整 dialect 校验留作后续独立变更。
- [Risk] 从示例推断 schema 可能过度泛化或误判 required 字段。 -> Mitigation：仅在证据明确时生成基础 schema；不确定时保留为空或追问。
- [Risk] 子 agent 与 LLM submit 阶段都可能写 schema，产生冲突。 -> Mitigation：`schema_generation` 统一规范化，`plan_review` 保留合法旧值并丢弃非法新值。
- [Risk] 已有历史 session 中可能保存非法 schema。 -> Mitigation：本变更处理新一轮 planning 与执行前计划规范化；历史数据清理不在本范围内。

## Migration Plan

1. 在实现前确认 `docs/api/api-import-agent.yaml`、`docs/api/api-asset-management.yaml`、`docs/sql/api-asset.sql` 的现有字段仍是权威契约。
2. 新增 `SCHEMA_GENERATION` subagent role 和 `SchemaGenerationPlannerSubagent`，注册到 planner subagent registry。
3. 增加共享 Schema 规范化工具方法，供子 agent、draft parser/normalizer 或 application service 执行前复用。
4. 增加 plan review 对 schema 字段的保护：非法新值不得覆盖当前合法值。
5. 补充 planner/subagent/service 聚焦测试。
6. 可回滚方式：从 registry 移除 `schema_generation` 子 agent；现有字段仍按旧逻辑透传。

## Open Questions

- 是否需要在 UI 上专门提示“Schema 是可选增强信息”，避免用户误以为必须补齐后才能导入？
- 从示例推断 schema 时，是否默认把示例中出现的字段都放入 `required`，还是只生成 `properties`？
