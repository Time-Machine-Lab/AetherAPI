## Context

当前 Import Agent 的规划链路已经可用，但实现方式偏向“单次大模型生成 + 后置校验”：

- `OpenAiCompatibleImportAgentPlannerProvider` 只暴露一个 `submit_import_plan` 工具。
- tool schema 只有基础 properties 描述，缺少 `required`、`additionalProperties: false` 和基于 `publishAfterImport`、`authScheme`、`assetType`、`asyncTaskConfig.authMode` 的条件约束。
- `ImportAgentPlannerJsonSupport.validatePlan(...)` 负责事后发现缺失字段，但发现之后主要依赖 clarificationQuestions 回问用户。
- 当前 planner request 只有 `documentSource`、`documentSummary`、`importIntent`、`latestUserMessage`、`currentPlan` 和 `turns`，缺少中间抽取产物，导致“阅读文档、抽字段、识别异步模式、补齐计划”全部挤在一个回合内完成。

与之对照，`.codex/skills/batch-import-api-skill/SKILL.md` 已经沉淀出更稳定的导入原则：

- `authConfig` 必须是后端可直接消费的纯字符串，而不是自定义 JSON；
- `assetType` 必须显式提供，不做隐式推导；
- 异步任务查询接口应并入 `asyncTaskConfig`，而不是默认创建第二个资产；
- `requestJsonSchema` / `responseJsonSchema` 字段名必须与后端现有契约对齐。

因此，这个 change 的目标不是改 public API，而是把这些稳定规则下沉到 planner tool-calling 层，让模型更难漏参、更少重复追问。

## Goals / Non-Goals

**Goals:**

- 在不改变现有导入会话和执行批次公共接口的前提下，补强 planner 的 tool-calling 约束能力。
- 把当前“单次完整提交”的 planner 流程拆成更容易校验的 staged extraction / slot filling / final submit。
- 让 planner 输出遵循现有资产管理权威契约中的字段名和格式规则，尤其是 auth、async task、AI profile、JSON schema 快照相关字段。
- 在向用户发 clarificationQuestions 之前，优先利用现有文档、当前计划和最近 turns 自动补齐缺失槽位。

**Non-Goals:**

- 不新增新的 Import Agent Controller 路径。
- 不新增新的 Import Agent 权威表。
- 不在本 change 中引入通用多 Agent 框架或自治循环。
- 不在本 change 中调整执行阶段的分类、资产、发布编排顺序。

## Decisions

### Decision 1: 保持现有 public session/run API 和 SQL 表结构不变

当前 `docs/api/api-import-agent.yaml`、`docs/sql/api_import_agent_session.sql`、`docs/sql/api_import_agent_turn.sql`、`docs/sql/api_import_agent_run.sql` 已经覆盖会话、轮次和执行批次闭环。本 change 只补强 planner 内部行为，不额外引入新的 HTTP 资源或新的 authority tables。

备选方案：新增 planner diagnostics 或 missing-slots 响应字段。

不采用原因：本次目标是先提升现有规划链路的完整性；如果没有明确的前端消费需求，先不扩展 public contract，避免让 docs/api 先行膨胀。

### Decision 2: 用 staged tools 替代“单个最终提交工具”

planner tool-calling 将拆分为至少两个阶段：

- `extract_import_facts`：只负责从文档摘要、用户消息和 currentPlan 中提取事实，不直接产出最终可执行计划。
- `submit_import_plan`：只负责提交经过抽取和补槽后的最终计划。

如实现需要，可在两者之间增加 `fill_import_slots` 或等价的内部工具，用于把缺失字段补回现有资产计划，而不是反复让用户回答同一问题。

备选方案：继续保留单个 `submit_import_plan`，仅在 prompt 里补更多文字约束。

不采用原因：单次自由生成同时承担“理解、抽取、判断、装配”四类职责，漏字段是结构性问题，不是简单加长 prompt 能根治的问题。

#### Concrete tool plan

规划阶段按以下顺序执行：

1. `extract_import_facts`
	 - 输入：`documentSource`、`documentSummary`、`importIntent`、`latestUserMessage`、`currentPlan`、`recentTurns`
	 - 输出：
		 - `assetFacts[]`
		 - `categoryHints[]`
		 - `authHints[]`
		 - `asyncHints[]`
		 - `aiProfileHints[]`
		 - `schemaHints[]`
		 - `unresolvedQuestions[]`
	 - 要求：只提取事实，不生成最终 plan version。

2. `fill_import_slots`
	 - 输入：`currentPlan`、`assetFacts[]`、`authHints[]`、`asyncHints[]`、`aiProfileHints[]`、`schemaHints[]`
	 - 输出：
		 - `assetPatches[]`
		 - `categoryPatches[]`
		 - `remainingMissingSlots[]`
	 - 要求：
		 - 只能补齐高确定性字段；
		 - 不能删除当前 plan 中已有且未被明确推翻的字段；
		 - 不能在缺少证据时凭空生成默认 token、环境变量名或平台特例。

3. `submit_import_plan`
	 - 输入：最终 `categoryPlans[]`、`assetPlans[]`、`summary`、`clarificationQuestions[]`
	 - 输出：完整结构化 plan
	 - 要求：只提交最终 plan，不再承担抽取职责。

### Decision 3: 在 tool schema 层表达强约束，而不是只做后置 validate

新的 tool schema 必须补齐：

- `required`
- `additionalProperties: false`
- `enum`
- 针对 `publishAfterImport`、`authScheme`、`assetType`、`asyncTaskConfig.enabled`、`asyncTaskConfig.authMode` 的条件约束

`ImportAgentPlannerJsonSupport.validatePlan(...)` 仍保留，但职责转向兜底校验和生成澄清问题，而不是承担唯一约束来源。

备选方案：完全依赖 `validatePlan(...)` 做后置发现。

不采用原因：后置校验只能在模型已经漏字段之后回问用户，不能降低首轮输出缺失率。

#### Constraint matrix

`submit_import_plan` 的 schema 需要至少覆盖以下约束：

- root
	- `type: object`
	- `additionalProperties: false`
	- properties: `summary`, `clarificationQuestions`, `categoryPlans`, `assetPlans`

- `categoryPlans[].*`
	- `additionalProperties: false`
	- `required`: `categoryCode`, `action`
	- `action.enum`: `USE_EXISTING`, `CREATE_IF_MISSING`

- `assetPlans[].*`
	- `additionalProperties: false`
	- `required`: `apiCode`, `assetName`, `assetType`
	- `assetType.enum`: `STANDARD_API`, `AI_API`
	- `requestMethod.enum`: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`
	- `authScheme.enum`: `NONE`, `HEADER_TOKEN`, `QUERY_TOKEN`

- conditional rules for `assetPlans[]`
	- if `publishAfterImport = true`
		- require `categoryCode`, `requestMethod`, `upstreamUrl`, `authScheme`
	- if `authScheme in [HEADER_TOKEN, QUERY_TOKEN]`
		- require `authConfig`
	- if `assetType = AI_API` and `publishAfterImport = true`
		- require `aiProfile.provider`, `aiProfile.model`

- `asyncTaskConfig`
	- `additionalProperties: false`
	- if `enabled = true`
		- require `queryMethod`, `queryUrlTemplate`, `authMode`
	- `queryMethod.enum`: `GET`, `POST`
	- `authMode.enum`: `SAME_AS_SUBMIT`, `OVERRIDE`
	- if `authMode = OVERRIDE`
		- require `authScheme`, `authConfig`
	- if present, `queryUrlTemplate` must contain `{taskId}`

- `aiProfile`
	- `additionalProperties: false`
	- if present, `provider` and `model` are required

如果目标 OpenAI-compatible 厂商不支持完整 JSON Schema 条件表达，则 provider 需要在发送请求前生成最强可表达 schema，并在响应后用同一矩阵做 deterministic reconcile。

### Decision 4: planner 输出格式与 batch-import-api-skill 保持一致

planner 需要明确遵循现有批量导入 skill 已验证过的字段约定：

- `authConfig` 使用纯字符串格式
- `requestJsonSchema` / `responseJsonSchema` 使用现有后端字段名，不引入别名
- 查询接口默认折叠进 `asyncTaskConfig`
- `assetType` 显式提供
- `aiProfile` 至少对 `AI_API` 资产提供 `provider` 和 `model`

备选方案：让 planner 自行决定字段命名和序列化风格，再由后处理层尽量兼容。

不采用原因：这会继续放大 planner 与后端契约之间的隐式映射，维护成本高，且更容易产生参数遗漏或错误归类。

#### Format alignment rules

- `authConfig`
	- `HEADER_TOKEN` 采用 `Header-Name: value`
	- `QUERY_TOKEN` 采用 `paramName=value`
	- 不允许输出 `{"headerName":...,"secretRef":...}` 这类 JSON 结构
- `requestJsonSchema` / `responseJsonSchema`
	- 只允许使用当前后端字段名，不引入 `requestSchema`、`responseSchema`、`inputSchema`、`outputSchema` 等别名
- `asyncTaskConfig`
	- 查询接口默认并入提交接口，不默认创建第二个 publishable asset
- `aiProfile`
	- 对 `AI_API` 资产至少提供 `provider`、`model`
	- `capabilityTags` 和 `streamingSupported` 可在存在明确证据时补齐

### Decision 5: clarificationQuestions 只在自动补槽失败后才暴露给用户

planner 在生成 clarificationQuestions 前，应优先执行一轮内部补槽：

- 从 `documentSummary` 中再次查找 auth、query URL、AI model、schema 信息
- 从 `currentPlan` 中保留已有字段，避免部分 patch 覆盖时丢字段
- 从最近 turns 中识别“用户正在回答哪个缺口”并回写目标字段

备选方案：只要缺字段就立刻向用户追问。

不采用原因：现有文档里很多缺失字段其实已经在输入材料中出现，只是首轮没有被抽到；直接追问会显著拉低可用性。

#### Slot-filling order

在向用户发 clarificationQuestions 前，按以下顺序补槽：

1. 保留 `currentPlan` 中已有字段，避免部分 patch 覆盖时清空已有值。
2. 从 `latestUserMessage` 中匹配直接回答：
	- `Authorization: ...`
	- `key=...`
	- `无需鉴权`
	- `模型是 ...`
3. 从 `documentSummary` 中再次抽取：
	- `requestMethod`
	- `upstreamUrl`
	- `authScheme` / `authConfig`
	- `queryUrlTemplate`
	- `provider` / `model`
	- schema 样例
4. 对异步模式执行 submit/query fold：
	- 将 query endpoint 归并为 `asyncTaskConfig`
	- 归并后删去孤立 query asset
5. 只有 `remainingMissingSlots[]` 仍包含高优先级必填字段时，才生成用户可见 `clarificationQuestions`

高优先级缺失槽位包括：

- publishable asset 的 `authConfig`
- `asyncTaskConfig.enabled = true` 时的 `queryUrlTemplate`
- publishable `AI_API` 的 `aiProfile.provider` / `aiProfile.model`

## Risks / Trade-offs

- [Planner 实现复杂度上升] → staged tools 和 slot filling 会增加 provider 和 tests 复杂度，但这是换取稳定性的必要成本。
- [不同 OpenAI-compatible 厂商的 tool-calling 兼容性不一致] → 保留 `tool-calling-enabled` feature gate，并保留非 tool-calling fallback 路径作为兼容兜底。
- [自动补槽可能误填] → 只允许补齐高确定性字段，低确定性信息仍然回退为 clarificationQuestions。
- [工具数变多导致 prompt 体积上升] → 将 extraction tool 与 final submit tool 的职责拆开，减少每个工具单次承载的信息密度。

## Migration Plan

1. 先补充 OpenSpec 对 planner tool-calling 约束与自动补槽行为的要求，不改 public API / SQL authority docs。
2. 再在 infrastructure planner provider 中实现 staged tools、强 schema 约束和 slot-filling。
3. 最后补 focused tests，确认 tool-calling 开启与关闭两条路径都能稳定工作。

## Implementation Surface

- `OpenAiCompatibleImportAgentPlannerProvider`
	- 拆分工具定义
	- 组装 staged prompt
	- 解析 tool-call 响应
- `ImportAgentPlannerJsonSupport`
	- 保留 deterministic normalize / validate / async fold
	- 增加 slot reconciliation 所需的辅助方法
- planner tests
	- 增加 staged tool-calling、slot-filling 和 strict schema regression coverage