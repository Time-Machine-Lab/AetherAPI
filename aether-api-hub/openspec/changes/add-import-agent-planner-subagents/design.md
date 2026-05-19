## Context

当前 Import Agent planner 对应用层暴露的是单一 `ApiImportAgentPlannerPort`，应用服务只接收一次 `plan(...)` 调用并消费一个最终 `ImportAgentPlannerResult`。这条主链路已经稳定，也符合当前 owner-scoped 会话模型：每轮对话只推进一个 current plan version，不引入平行会话或并行执行编排。

另一方面，当前 planner 内部虽然已经采用 staged tool-calling，但仍然主要依赖单个 provider 在单个上下文中完成全部推理。文档事实抽取、鉴权识别、异步 submit/query 识别、AI profile 推断、缺槽审查和澄清策略仍然共享同一轮主要推理上下文。对于文档较长、字段耦合较多的导入任务，这会继续增加局部遗漏和低确定性字段污染最终计划的风险。

因此，这次 change 不打算在应用层暴露多 agent 概念，也不打算引入自治循环，而是把“子 agent”限定为 planner 内部的窄职责协作者：多个内部角色可以各自完成专门任务，由 orchestrator 合并结果、执行审查，最终仍然只向 service 层返回一个 draft import plan。

## Goals / Non-Goals

**Goals:**

- 在不改变 `ApiImportAgentPlannerPort` 对外契约的前提下，为 planner 增加内部子 agent 协作能力。
- 把事实抽取、鉴权判断、异步模式识别、计划审查和澄清生成拆分给窄职责内部角色，降低单一上下文负担。
- 为内部子 agent 结果定义确定性的 merge / review 规则，优先保留高确定性字段并阻止冲突结果直接进入最终计划。
- 保持现有 session / run API、现有 plan version 推进方式、现有执行确认门禁和现有 authority docs 不变。
- 保留单 planner 结果模型，使应用层与 adapter 层不感知内部多角色协作细节。

**Non-Goals:**

- 不把子 agent 做成对外可见的业务主链路或独立会话实体。
- 不为每个子 agent 新增独立持久化轨迹、独立控制接口或独立执行结果表。
- 不引入通用自治式 multi-agent runtime、循环规划器或跨轮自驱任务执行。
- 不修改 `docs/api/api-import-agent.yaml`、`docs/sql/api_import_agent_*.sql` 顶层文档。

## Decisions

### Decision 1: 子 agent 只作为 planner 内部协作者存在

子 agent 的实现边界限定在 infrastructure planner 内部。应用层仍然只依赖单一 `ApiImportAgentPlannerPort`，service 和 adapter 继续只消费一个最终 `ImportAgentPlannerResult`。

备选方案：在 service 层显式引入多 agent 编排概念，让应用服务感知多个 planner 角色。

不采用原因：这会污染当前单会话、单计划版本推进模型，并把本应属于 planner 内部实现细节的职责扩散到应用层。

### Decision 2: 采用窄职责子 agent，而不是一个通用 agent framework

第一版子 agent 仅服务 Import Agent planner，建议至少覆盖以下角色：

- `DocumentFactsSubagent`：抽取 API 事实、分类线索、请求方式和上游 URL
- `AuthSubagent`：识别 `authScheme`、`authConfig` 及相关高确定性鉴权事实
- `AsyncPatternSubagent`：识别 submit/query 异步模式并折叠到 `asyncTaskConfig`
- `PlanReviewSubagent`：对候选计划做缺槽、冲突和低确定性字段审查
- `ClarificationSubagent`：在仍有缺口时生成澄清项或澄清策略

备选方案：直接引入一个可扩展到任意业务场景的通用 agent runtime。

不采用原因：当前目标非常具体，通用运行时会显著抬高复杂度和测试面，且超出当前业务收益。

### Decision 3: orchestrator 负责顺序编排与结果合并，仍只生成一个最终 draft plan

在 provider 内部增加子 agent orchestrator，由它负责：

1. 依次调用内部子 agent；
2. 保存中间事实快照；
3. 对多个子 agent 输出执行 merge；
4. 调用 review 子 agent 或等价审查逻辑；
5. 构造最终提交给 `ImportAgentPlannerJsonSupport` 的统一 plan candidate。

最终仍然只产出一个 `ImportAgentPlanModel`，不向应用层返回多份子结果。

备选方案：让每个子 agent 各自返回独立 partial plan，再由 service 层决定如何采纳。

不采用原因：应用层不应承接 planner 内部推理冲突；最终计划归并应在 planner 内部完成。

### Decision 4: merge / review 规则优先于自由拼接

多个子 agent 的输出不允许无约束拼接进入最终计划。orchestrator 必须应用统一规则：

- 高确定性事实优先于低确定性推断；
- currentPlan 已确认的字段优先于新的弱证据覆盖；
- 冲突字段必须降级为待审查或 clarification，而不是静默覆盖；
- review 结果可以阻止候选字段进入最终 plan。

备选方案：简单按“后写覆盖前写”合并子 agent 结果。

不采用原因：这会把子 agent 输出冲突转化为随机行为，无法满足 planner 稳定性目标。

### Decision 5: 子 agent 可以复用现有 tool-calling 与 deterministic reconcile，而不是另起体系

内部子 agent 应优先复用现有阶段工具、现有 request context 和 `ImportAgentPlannerJsonSupport` 的 deterministic reconcile / validate 能力。子 agent 的引入是对当前 staged tool-calling 的细化，不是替换现有 planner contract。

备选方案：让每个子 agent 完全绕开现有 tool-calling 和 JsonSupport，自行生成最终字段。

不采用原因：这会重复已有的 normalize / validate 逻辑，并扩大行为漂移面。

### Decision 6: 保留当前 fallback 语义，子 agent 失败不能破坏现有单 planner 结果契约

如果内部某个子 agent 失败、超时或返回不可用结果，planner 需要回退到保守路径：要么忽略该子结果继续生成统一 draft plan，要么回退为 clarification，而不是让整个会话规划链路不可用。

备选方案：任一子 agent 失败即整个 planning request 失败。

不采用原因：当前 import-agent 会话更重视可继续推进和可澄清性，而不是要求所有内部角色全成功才返回结果。

## Risks / Trade-offs

- [内部调用轮次增加，token 和延迟上升] → 将第一版子 agent 控制在少量高价值角色，并优先复用已有上下文与 staged tool-calling。
- [结果合并规则变复杂] → 在 orchestrator 中集中实现 merge / review 规则，并用 focused tests 锁定冲突处理行为。
- [过度演进成通用多 agent 框架] → 明确只服务 Import Agent planner，不新增对外会话、持久化或自治运行时能力。
- [某个子 agent 误判导致计划偏移] → 引入 review 子 agent 或等价审查步骤，并让低确定性结果回退为 clarification。
- [service 层被迫感知内部多角色细节] → 坚持单一 `ApiImportAgentPlannerPort` 和单一最终 plan 结果，避免向上层泄漏内部协作结构。