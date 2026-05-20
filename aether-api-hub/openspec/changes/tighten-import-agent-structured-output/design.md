## Context

当前 Import Agent 的 planner 主链路在后端内部混合了四类不同性质的工作：

- 结构化结果解析
- current plan 字段合并
- 自由文本补槽和猜测
- 执行前归一化与校验

这让 `ImportAgentPlannerJsonSupport` 既像 parser，又像 merger，又像 inference engine，还像 validator。对于 `summary` 这类低风险字段，这种宽松处理问题不大；但对 `authScheme`、`authConfig`、`upstreamUrl`、`requestMethod`、`asyncTaskConfig.queryUrlTemplate`、`asyncTaskConfig.authMode` 等执行关键字段，宽松处理会把“缺失信息”悄悄转成“隐式猜测”，增加运行时偏差和异常概率。

本 change 的目标不是推翻当前 Import Agent 会话/确认/执行闭环，也不是新增公共接口，而是把 planner 主路径收紧为：planner 明确给出结构化结果，后端做有限的装配和校验，缺失时明确追问。

## Goals

- 让执行关键字段只来源于声明过的结构化 planner 输出或确定性的 current-plan merge。
- 让缺失执行关键字段在规划阶段就暴露为 clarificationQuestions，而不是在展示或执行阶段才暴露为异常。
- 保留当前 Import Agent 的对外 HTTP 契约、数据库结构和 feature gate。
- 把 planner 装配逻辑拆成清晰阶段，避免继续在单个支持类里堆积解析、推断和校验责任。

## Non-Goals

- 不新增或修改 `ApiImportAgentController` 的对外接口。
- 不新增 planner 相关数据库表或字段。
- 不一次性移除所有兼容归一化逻辑；低风险兼容路径仍可保留，但不再主导 executable 结果。
- 不改变现有“异步查询折叠进 submit asset 的 asyncTaskConfig”这一业务语义。

## Decisions

### 1. 收紧 planner 的结构化合同，而不是继续增强自由文本后处理

planner 输出必须被视为受约束的结构化 contract，而不是“自然语言 + 零散 JSON”的组合。执行关键字段必须使用明确字段名和合法枚举值，并受 required/conditional 规则约束；未知字段必须被拒绝或忽略，而不是自动进入计划状态。

这样做的原因是，问题根源在输入协议过宽，而不在于后端缺少更多正则。继续增强后处理只会扩大维护成本，无法从根上消除错位值、别名漂移和自由文本污染。

### 2. 明确拆分 planner 装配阶段

装配链路在逻辑上拆成以下阶段：

- `PlanDraftParser`: 只负责从结构化 planner 结果解码出 draft payload；嵌入文本 JSON 提取仅作为 legacy fallback。
- `PlanDraftMerger`: 只负责把新 draft 和 current plan 合并，保留已知字段，不做自由文本猜测。
- `AsyncTaskPlanNormalizer`: 只负责 submit/query 资产折叠、枚举归一化和 asyncTaskConfig 结构整形。
- `PlanValidator`: 只负责生成 clarificationQuestions、判定 executable 与默认摘要。

这样做不是强制要求一次性新增四个类，而是明确责任边界。即便第一阶段仍保留在单个文件中，也要按照这四段责任组织逻辑和测试。

### 3. 高风险字段不再允许自由文本主路径推断

以下字段被定义为执行关键字段：

- `requestMethod`
- `upstreamUrl`
- `authScheme`
- `authConfig`
- `asyncTaskConfig.queryMethod`
- `asyncTaskConfig.queryUrlTemplate`
- `asyncTaskConfig.authMode`
- `asyncTaskConfig.authScheme`
- `asyncTaskConfig.authConfig`
- 对 `AI_API` 且 `publishAfterImport = true` 的资产，`aiProfile.provider` 与 `aiProfile.model`

这些字段缺失时，planner 可以返回非 executable draft 和 clarificationQuestions，但后端不得仅凭自由文本把它们补成 executable state。

这样做的代价是澄清轮次可能增加，但这是可接受的，因为它把错误暴露提前到规划阶段，避免后续执行失败或脏状态持久化。

### 4. 确定性的 merge 和兼容性归一化仍然保留

以下行为仍然允许：

- partial structured patch 未包含的字段，继续沿用 current plan 已有值
- async submit/query 资产继续折叠为单个 asset 的 `asyncTaskConfig`
- 已知 enum alias 或大小写漂移可被归一化到合法值

这些行为的共同特点是：它们基于明确结构化字段或既有状态，不需要解释自由文本的业务含义。

### 5. 外部 API 和 SQL 契约保持不变

本 change 不修改 `docs/api/api-import-agent.yaml` 中的 current-user Import Agent 会话/确认/执行接口，也不修改 planner 相关表结构。行为变化限定在后端内部 planner contract、session planning 和 executable gate 上。

## Risks / Trade-offs

- Risk: 计划在短期内会更常进入 clarification，而不是被“自动修好”。
  - Mitigation: 把 clarificationQuestions 做成更聚焦的字段级问题，避免重复追问。

- Risk: 现有 provider 或 prompt 仍可能输出宽松 JSON，导致更多 draft 被判定为非 executable。
  - Mitigation: 保留 legacy fallback 作为兼容层，但它只用于解析/归一化，不再决定高风险字段的 executable 结果。

- Risk: 对 `ImportAgentPlannerJsonSupport` 的职责收缩会牵动现有测试和 provider 行为。
  - Mitigation: 先以阶段化重构推进，保持现有对外 API、feature gate 和 async folding 语义不变，并补充分层回归测试。