## Why

当前 Import Agent 的规划链路仍然允许“自由文本/宽松 JSON -> 后端补猜 -> 再校验”的工作方式。`ImportAgentPlannerJsonSupport` 同时承担 JSON 提取、旧计划合并、自由文本补槽、异步任务折叠和执行前校验，导致执行关键字段的来源不稳定：`authScheme`、`authConfig`、`requestMethod`、`upstreamUrl`、`asyncTaskConfig.authMode`、`asyncTaskConfig.queryUrlTemplate` 等字段既可能来自 planner 的结构化输出，也可能被后端从会话文本、文档摘要或兼容别名中推断出来。

这种模式的问题不是单点 bug，而是输入协议本身过宽。只要 planner 把枚举写错位、遗漏高风险字段，或把字段落到自由文本里，后端就会尝试“理解模型真正想表达什么”。结果是：缺失信息没有在规划阶段被明确暴露，而是可能被隐式修复、隐式猜测，直到后续 session 展示、确认或执行阶段才暴露为运行时异常或行为偏差。

已审阅的顶层权威文档表明，本次问题集中在后端内部 planner contract 和计划装配逻辑，而不是当前用户 Import Agent 的外部 HTTP 契约或表结构：

- `docs/api/api-import-agent.yaml`
- `docs/api/api-asset-management.yaml`
- `docs/spec/Aether API HUB 后端代码开发规范文档.md`

因此需要单独发起一个 change，把 Import Agent planner 的主路径收紧为“强约束结构化输出 + 明确澄清”，并把自由文本推断降级为兼容/低风险兜底，而不是继续扩张 `ImportAgentPlannerJsonSupport` 中的猜测逻辑。

## What Changes

- 收紧 Import Agent planner 的结构化输出合同，对执行关键字段引入更严格的 enum、required、conditional rules 和 undeclared-field rejection。
- 将规划装配链路明确分成“结构化解析、旧计划合并、异步任务归一化、执行前校验”四个阶段，减少单个支持类同时承担解析和猜测职责。
- 把高风险字段的自由文本推断降级为非主路径，不再依赖会话文本或文档摘要自动补齐可执行计划所需的 `auth*`、`requestMethod`、`upstreamUrl`、`asyncTaskConfig.*` 等字段。
- 保留当前导入会话、确认、执行与 SSE 接口语义，不新增对外 API，也不修改现有表结构。

## Capabilities

### Modified Capabilities
- `api-import-agent-session`: 收紧 planner draft 的输入合同，要求执行关键字段必须来自声明过的结构化 planner 输出或确定性的 current-plan merge；缺失时必须进入 clarificationQuestions 并保持计划不可执行。

## Impact

- Reviewed authority docs:
  - `docs/api/api-import-agent.yaml`
  - `docs/api/api-asset-management.yaml`
  - `docs/spec/Aether API HUB 后端代码开发规范文档.md`
- Top-level docs impact:
  - 当前提案默认不引入新的 Controller 契约或数据库表结构，因此预计无需修改 `docs/api/` 或 `docs/sql/` 顶层权威文档。
  - 如果设计阶段确认需要暴露新的 planner 诊断字段或持久化新的 planning metadata，必须先补充对应顶层文档后再进入实现。
- Affected backend modules:
  - `aether-api-hub-infrastructure`: planner provider、tool schema、planner JSON support / normalization 逻辑
  - `aether-api-hub-service`: planner session orchestration、plan merge 与 validation 边界
- Runtime impact:
  - Import Agent 可能更频繁返回 clarificationQuestions，因为缺失的执行关键字段将不再被自由文本猜测自动补齐。
  - `aether.import-agent.llm.tool-calling-enabled` 继续作为 feature gate；本 change 重点是提高 planner 输出确定性，而不是扩展外部接口。