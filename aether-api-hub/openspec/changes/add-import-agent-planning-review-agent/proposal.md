## 背景

当前 Import Agent 的规划主链路已经具备 staged tool-calling、内部 subagent 协作和结构化计划输出能力，但 planner 完成后的第 3 层后处理仍然承担了一部分“智能补救”职责。`ImportAgentPlannerJsonSupport` 除了解析结构化 JSON、合并 current plan、生成 clarificationQuestions 外，还会在部分场景下自动折叠异步 query 资产、推断 async 鉴权模式、兼容畸形枚举值，甚至把缺失信息转换成可执行配置。

这种设计的问题不在于某一个方法是否实现正确，而在于职责边界不清晰：规划阶段负责“提出候选计划”，后处理阶段本应只做确定性守卫和校验，却继续承担了语义性修复。结果是，当模型漏填关键字段时，系统会在 planner 结束后继续“猜模型真正想表达什么”，这会降低计划来源的可解释性，也让 review/debug 难以聚焦在规划阶段。

因此需要新增一个专门的规划检查 agent，把高风险结构检查、冲突识别和澄清降级前移到规划阶段，同时把第 3 层后处理收缩为薄守卫：只保留结构化解析、确定性合并、最小兼容归一化和 executable gate，不再承担智能补救主路径。

## 变更内容

- 在 Import Agent 的规划阶段新增专门检查 agent，用于审查候选计划中的 auth、async、AI profile 和执行关键字段是否完整、自洽、可发布。
- 将当前第 3 层后处理中的智能补救能力前移到规划阶段 review，减少 `ImportAgentPlannerJsonSupport` 对执行关键字段的语义性推断。
- 收缩 `ImportAgentPlannerJsonSupport` 的职责，使其主要负责结构化解析、current plan 合并、最小兼容归一化和最终校验，不再主动补齐高风险字段。
- 保持当前对外 HTTP 契约、SSE 事件集合和数据库表结构不变；本次 change 仅调整后端内部 planner 装配与 executable 判定路径。

## 能力变更

### 已修改能力

- `api-import-agent-session`
  - planner 必须在生成 draft plan 后，通过专门检查 agent 对执行关键字段进行结构一致性审查，并在缺失或冲突时优先降级为 clarificationQuestions。
  - planner 后处理阶段不得再作为高风险字段的主要补救路径；对 `auth*`、`asyncTaskConfig.*`、`aiProfile.*` 等执行关键字段，缺失时必须保持非 executable，而不是在后处理阶段隐式补齐。

## 影响范围

- 已审阅权威文档：
  - `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`
  - `docs/api/api-import-agent.yaml`
  - `docs/spec/Aether API HUB 后端代码开发规范文档.md`
- 顶层文档影响：
  - 当前提案默认不新增 Controller 接口，也不修改数据库表结构，预计无需修改 `docs/api/` 与 `docs/sql/` 顶层文档。
  - 若设计阶段决定新增 planner review 诊断字段或新 SSE 事件，则必须先更新对应顶层权威文档，再进入实现。
- 受影响模块：
  - `aether-api-hub-infrastructure`：planner provider、subagent orchestration、planner JSON support
  - `aether-api-hub-service`：规划结果的 executable gate 与 session message 生成路径
- 运行时影响：
  - 计划在关键字段缺失时更可能停留在 clarification，而不是被后处理隐式修复。
  - planner 阶段的日志和 thinking 摘要将更集中反映检查结果，便于调试。
