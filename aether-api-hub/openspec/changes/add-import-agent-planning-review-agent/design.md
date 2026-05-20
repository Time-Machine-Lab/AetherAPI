## 上下文

当前 Import Agent 的规划链路大致分为三层：

1. `OpenAiCompatibleImportAgentPlannerProvider` 负责 staged tool-calling 和主候选计划生成。
2. `ImportAgentPlannerSubagentOrchestrator` 负责内部 subagent 协作，对候选计划做事实补充、冲突审查和澄清降级。
3. `ImportAgentPlannerJsonSupport` 负责计划 JSON 的最终装配、补齐、归一化和 executable 判定。

问题在于第 2 层和第 3 层的边界仍然交叉。第 2 层已经有 review / clarification 语义，但第 3 层还在做部分语义性补救，例如：

- 自动折叠 submit/query 资产并推断 async 查询配置
- 根据缺失字段推断 async authMode
- 兼容畸形 authMode / authScheme 取值并把它们转换成可执行配置

这些逻辑虽然提高了短期容错，但也让 planner 输出的“真实来源”不再清晰：某些字段究竟是模型结构化输出、current plan 继承，还是后处理猜测，调试时需要跨越多个阶段追溯。

本次设计的目标是重新明确这两层的职责：

- 第 2 层负责“智能检查与降级”，即对候选计划做结构审查、冲突识别和 clarification 决策。
- 第 3 层负责“确定性守卫”，即解析、合并、最小兼容归一化和最终 executable gate。

## 目标 / 非目标

**目标：**

- 在规划阶段引入专门检查 agent，使关键字段检查和澄清降级在 planner 内部完成，而不是依赖后处理补救。
- 缩减 `ImportAgentPlannerJsonSupport` 中的智能补救逻辑，保留确定性 parse / merge / validate 职责。
- 保持当前 Import Agent 的会话、确认、执行闭环不变，不要求 service / adapter 理解多 agent runtime。
- 让 planner 日志、thinking 摘要和后续 debug 更准确地反映问题发生在“规划阶段”还是“守卫阶段”。

**非目标：**

- 不引入新的通用 agent runtime 或外部多 agent framework。
- 不修改 `ApiImportAgentController` 的对外接口、不新增公共 REST endpoint。
- 不新增 planner 相关数据库表或会话持久化字段。
- 不一次性移除所有兼容性归一化；低风险格式兼容仍可保留为 guardrail。

## 设计决策

### 决策 1：新增专门检查 agent，而不是继续扩张 JSON support 的补救逻辑

规划阶段新增一个专门检查 agent，职责是对候选计划做结构一致性审查。它重点关注：

- `authScheme` / `authConfig` 是否自洽
- `asyncTaskConfig` 是否完整，query 配置是否可执行
- `AI_API` 且可发布资产是否具备显式 `aiProfile.provider` 和 `aiProfile.model`
- candidate plan 中是否存在应转为 clarification 的冲突或缺失

不采用“继续在 `ImportAgentPlannerJsonSupport` 中增加更多补救规则”的原因：这会让后处理进一步演化成 inference engine，弱化规划阶段的责任边界。

### 决策 2：第 3 层后处理只保留确定性守卫，不再承担高风险字段补救主路径

`ImportAgentPlannerJsonSupport` 未来保留的能力包括：

- 结构化 JSON 解析
- current plan 字段合并
- 低风险格式兼容，例如 schema alias、已知 enum alias
- 最终 `clarificationQuestions` 汇总与 `executable` 判定

下列高风险补救不再作为第 3 层主路径：

- 仅凭后处理自动把 query 资产折叠成 submit 资产并产出可执行 async 配置
- 在缺失结构化证据时自动推断 `asyncTaskConfig.authMode` / `authScheme` / `authConfig`
- 通过补救让关键字段缺失的计划进入 executable 状态

### 决策 3：review agent 仍然是 planner 内部角色，不向上层泄漏多 agent 契约

新检查 agent 仍通过现有 planner-internal subagent 体系接入，或者作为 provider 内的 review stage 接入，但不改变 `ApiImportAgentPlannerPort` 的单一输出契约。

不采用“让应用层显式感知 review agent 结果”的原因：当前 service 和 adapter 层的 owner-scoped session 模型已经稳定，没有必要为内部检查角色扩大对外模型。

### 决策 4：异步 query 资产折叠语义保留，但决策前移

“异步 submit/query 接口最终折叠为单个资产的 `asyncTaskConfig`”这一业务语义保留；变化在于折叠应尽量由规划阶段的结构化候选计划和 review agent 决策完成，而不是在最终 guardrail 阶段被动补救。

### 决策 5：clarification 必须成为缺失关键字段时的优先出口

当关键字段缺失或存在冲突时，系统优先返回 targeted clarificationQuestions，而不是进入“先补救再执行”的路径。这样会增加部分追问轮次，但换来更稳定的 executable 判定和更清晰的调试边界。

## 实现锚点

本次变更建议优先围绕现有 3 个实现锚点推进，而不是先做大范围物理拆分：

### 锚点 1：`OpenAiCompatibleImportAgentPlannerProvider`

这个类当前已经负责三段规划：

- `EXTRACT_FACTS`
- `FILL_SLOTS`
- `SUBMIT_PLAN`

并在 `plan(...)` 中串起 subagent orchestrator 和 `ImportAgentPlannerJsonSupport.buildPlan(...)`。本次设计建议保持这个总编排入口不变，只在候选计划生成后插入更明确的 review 语义，避免把职责再分散到 service 层。

建议落地方式：

- 保持 `plan(...)` 仍是唯一总入口。
- `executePlanningStage(...)` 继续只负责 tool-calling stages，不承担结构 review。
- 候选 `planSource` 生成后，再把 review agent 作为 planner-internal review 步骤接入，然后才进入 `ImportAgentPlannerJsonSupport.buildPlan(...)`。

### 锚点 2：`ImportAgentPlannerSubagentOrchestrator` / `PlanReviewPlannerSubagent`

当前 orchestrator 已经是 planner 内部角色编排中心，`PlanReviewPlannerSubagent` 也已经存在，因此本次无需新造第二套 agent runtime。更合适的方向是把“专门检查 agent”明确化为 review 角色升级，而不是新增独立框架层。

建议落地方式：

- 让 `PlanReviewPlannerSubagent` 成为本次 change 的主要承载点，显式承担高风险字段检查职责。
- 如果现有 review agent 语义过宽，可以在不改外层契约的前提下，拆成更聚焦的内部 review subagent，但仍由 `ImportAgentPlannerSubagentOrchestrator.orchestrate(...)` 串联。
- review 输出应优先表现为：
	- 对 candidate plan 的结构 patch
	- 追加的 clarificationQuestions
	- review thinking / debug 摘要

### 锚点 3：`ImportAgentPlannerJsonSupport`

这个类当前既做 parse / merge / validate，也做一部分智能补救，是边界收缩的直接目标。后续实现应优先收缩以下方法的职责：

- `normalizeDraft(...)`
- `reconcileMissingSlots(...)`
- `normalizeAsyncTaskQueryAssets(...)`
- `mergeAsyncTaskConfig(...)`
- `resolveAsyncTaskQueryUrlTemplate(...)`
- `resolveAsyncTaskAuthMode(...)`
- `resolveAsyncTaskAuthScheme(...)`
- `resolveAsyncTaskAuthConfig(...)`
- `normalizeAsyncTaskConfig(...)`

建议保留的能力：

- `buildPlan(...)` 的整体装配入口
- current plan merge
- 低风险 alias normalize
- clarification 汇总与 executable gate

建议迁出的能力：

- 依赖语义猜测的 async query/submit 合并
- 缺失结构化证据时的 async auth 推断
- 会把关键字段缺失计划补成 executable 的逻辑

## 推荐实现顺序

### 步骤 1：先收紧行为定义，再动类拆分

第一轮实现优先完成行为迁移，而不是马上把 `ImportAgentPlannerJsonSupport` 拆成多个新类。这样可以先证明边界变更是正确的，再决定是否做物理重构。

### 步骤 2：先让 review agent 接住关键字段检查，再删除第 3 层补救

推荐顺序是：

1. 先增强 review agent 的检查职责和输出结构。
2. 让缺失字段的路径稳定降级为 clarification。
3. 再删掉第 3 层对应的补救逻辑。

不建议反过来先删补救逻辑，因为那样会在中间态制造一段“没人负责关键字段检查”的空窗期。

### 步骤 3：测试先改成新语义，再收缩守卫实现

本次 change 的核心回归不是“字段还能不能被补出来”，而是“字段缺失时是否稳定进入 clarification”。因此测试要先表达这个新语义，避免实现过程中被旧断言牵着走。

## 测试策略

建议把测试分为 3 层：

### 1. review agent / orchestrator 层

- 缺失 `authScheme` 或 `authConfig` 时，review 会追加 targeted clarification。
- 缺失 `asyncTaskConfig.queryUrlTemplate` / `authMode` 时，review 会保持非 executable。
- `AI_API` 可发布资产缺失 `aiProfile.provider` 或 `aiProfile.model` 时，review 会阻止可执行化。

### 2. `ImportAgentPlannerJsonSupport` 守卫层

- 已知低风险 alias 仍能被归一化。
- 仅有自由文本线索而无结构化字段时，不再被补齐成 executable。
- current plan partial merge 仍保持确定性，不因 review 前移而失效。

### 3. provider/service/adapter 窄集成层

- `OpenAiCompatibleImportAgentPlannerProvider.plan(...)` 在 review 后仍能产出稳定的 `ImportAgentPlannerResult`。
- `ApiImportAgentApplicationService` 在 clarification 路径上仍能正确生成 turn message。
- web delegate / stream 路径在 planner 非 executable 时不发生契约回归。

## 风险与权衡

- [规划阶段会更频繁地产生 clarification] -> 这是预期内代价；通过 review agent 产出更聚焦的字段级追问，控制用户感知成本。
- [某些历史依赖后处理补救的 planner 输出会退化为非 executable] -> 先保留低风险兼容归一化，只移除高风险字段补救主路径，降低回归面。
- [review 逻辑前移后，provider / subagent / json support 三层职责重排会影响现有测试] -> 先在现有类中完成边界收缩和测试重写，再视需要做物理拆分类重构。
- [thinking stream 方案与 review agent 同时推进时可能产生重叠日志] -> review agent 的 thinking 摘要应只描述检查结论，不重复 provider 的 stage status。

## 迁移计划

1. 在现有 planner 内部角色体系中引入专门检查 agent，并让其接在候选计划生成之后。
2. 收缩 `ImportAgentPlannerJsonSupport` 的智能补救逻辑，把高风险字段补救迁出第 3 层。
3. 更新相关回归测试，使关键字段缺失时表现为 clarification，而不是后处理自动可执行化。
4. 保持现有 `ApiImportAgentPlannerPort`、会话持久化和执行闭环不变，避免扩大联动面。
