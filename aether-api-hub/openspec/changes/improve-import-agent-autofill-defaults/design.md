## Context

Import Agent 当前已经有 `document_facts`、`auth_recognition`、`async_pattern`、`schema_generation`、`plan_review`、`clarification_strategy` 等内部 subagent，并通过 `docs/api/api-import-agent.yaml` 暴露结构化计划、结构化澄清项和资产计划字段。现有契约已经包含 `requestExample`、`responseExample`、`requestJsonSchema`、`responseJsonSchema`、`asyncTaskConfig`，因此主动补全不需要新增资产字段或数据库字段。

体验问题在于：planner 即使有足够证据，也容易把可推断内容转成问题交给用户；结构化澄清项只有 `currentValue`，无法表达“这是 agent 根据文档推断出的推荐默认值，用户可直接确认”。因此本设计把“主动补全”和“带默认值的提问”作为同一条规划链路处理：先尽力确定性补全，仍不确定时再用结构化问题携带推荐默认值。

## Goals / Non-Goals

**Goals:**

- 在证据足够时主动生成请求示例、响应示例、请求/响应 JSON Schema 与异步任务配置。
- 在必须提问时，为结构化澄清项提供可选推荐默认值、默认值来源和置信度，让前端可展示“推荐采用”的体验。
- 保证默认值只是建议，不等同于用户已确认答案；后端仍通过 `clarificationAnswers` 接收用户确认后的值。
- 先更新 `docs/api/api-import-agent.yaml`，再调整 DTO、service model、adapter 映射和 planner 实现。
- 保持旧客户端兼容，新增响应字段均为 nullable/optional。

**Non-Goals:**

- 不新增 SQL 字段，不修改 `docs/sql/`。
- 不做密钥脱敏、密钥托管或凭证安全策略变更。
- 不引入完整 JSON Schema dialect 校验。
- 不改变 Unified Access 运行时调用、异步查询执行语义或 API 资产主数据模型。
- 不让后端代表用户自动提交澄清答案。

## Decisions

### 决策 1：先主动补全，再结构化追问

planner 应按“证据收集 -> 主动补全 -> 审查降级 -> 澄清生成”的顺序运行。示例和 Schema 生成优先消费文档中的 request/response 示例、字段定义、OpenAPI 片段、历史当前计划和 `schemaHints`；异步任务配置优先消费 submit/query 接口配对、任务 ID 字段、任务状态/结果路径和查询 URL 模板。

备选方案是继续把缺失字段全部交给 `clarification_strategy` 提问。拒绝原因是这会把 agent 能完成的机械推断转嫁给用户，正是当前体验差的核心。

### 决策 2：默认值作为澄清项元数据，而不是写入答案

在 `ImportAgentClarificationItemResp` 中新增可选字段：

- `defaultValue`：推荐提交值，字符串表示，格式与 `clarificationAnswers[].value` 一致。
- `defaultLabel`：展示标签，适合 SELECT/BOOLEAN 等控件。
- `defaultSource`：默认值来源，如 `DOCUMENT`、`INFERRED_FROM_URL`、`CURRENT_PLAN`、`AGENT_HEURISTIC`。
- `defaultConfidence`：`HIGH`、`MEDIUM`、`LOW`，供前端决定视觉强调程度。

`currentValue` 继续表示当前计划已有值；`defaultValue` 只表示尚未确认的推荐值。这样可以兼容旧客户端，也能避免默认值被误认为用户已确认。

### 决策 3：默认值必须来自可解释证据

后端不得为了让表单看起来完整而编造默认值。允许的来源包括：

- 文档中明确给出的字段、示例、URL、Header/Query 名称或枚举值。
- 当前计划已有合法值。
- submit/query 接口之间可稳定匹配出的异步任务配置。
- 常见但低风险的结构推断，例如从响应示例推断 `statusPath`、`resultPath`。

对于凭证值、密钥值、租户私有参数值等高风险信息，不生成默认值；只提示用户提供业务信息或凭证来源。

### 决策 4：异步任务配置生成保持可审查、可回退

`async_pattern` 可以在证据足够时直接生成 `asyncTaskConfig`，包括 `enabled`、`queryMethod`、`queryUrlTemplate`、`authMode`、`statusPath`、`resultPath`、`errorPath`。当只缺少少量字段时，不应拆出第二个资产或卡住，而应在同一资产上给出带默认值的结构化问题。

`plan_review` 继续负责校验：查询 URL 模板必须包含 `{taskId}`，`authMode` 必须是 `SAME_AS_SUBMIT` 或 `OVERRIDE`，非法配置应降级为问题而不是写入可执行计划。

### 决策 5：公共契约只增量，不破坏

本变更涉及接口响应字段增量，必须先使用 `tml-docs-spec-generate` 更新 `docs/api/api-import-agent.yaml`，目标 Controller 仍为 `ApiImportAgentController.java`。不需要 SQL 文档，因为没有表结构变化。

## Risks / Trade-offs

- [Risk] 默认值被用户误以为已自动生效。 -> Mitigation：后端只返回 `defaultValue`，不把它写成 `currentValue`；前端必须用户确认后才提交。
- [Risk] agent 过度推断导致错误配置。 -> Mitigation：按来源和置信度分级；高风险字段不默认；review 子 agent 继续拦截非法异步配置和 schema。
- [Risk] API 字段增量造成前端类型不同步。 -> Mitigation：任务中把 `docs/api/api-import-agent.yaml` 更新放在第一步，并要求前后端 DTO/types 对齐。
- [Risk] 生成示例可能与真实上游要求不完全一致。 -> Mitigation：示例生成只作为资产文档增强；不作为运行时校验或强制执行依据。

## Migration Plan

1. 使用 `tml-docs-spec-generate` 更新 `docs/api/api-import-agent.yaml`，为 `ImportAgentClarificationItemResp` 增加可选默认值字段。
2. 更新后端 DTO/service model/adapter 映射，确保旧响应字段兼容。
3. 增强示例、Schema、异步任务配置的 planner/subagent 生成逻辑。
4. 增加 plan review 与 clarification strategy 测试，验证默认值不会自动成为用户答案。
5. 回滚方式：保留 API 可选字段但停止填充默认值，或在 planner 中关闭新增主动补全逻辑。

## Open Questions

- `defaultConfidence` 是否需要数值化为 0-1，还是枚举足够？本提案建议先用枚举，避免前端解释复杂度过高。
- 是否需要记录默认值来源的更细粒度证据片段？本提案暂不暴露 provider 原始 payload，只返回来源类别。
