## 1. 权威对齐

- [ ] 1.1 重新核对 `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/api/api-import-agent.yaml` 和后端开发规范，确认本次 change 只影响内部 planner 组织，不引入新接口和新表结构。
- [ ] 1.2 若设计阶段需要新增 review 诊断字段或 stream 事件，再单独回到顶层 `docs/api/` 权威文档更新后推进实现。

## 2. 规划阶段新增专门检查 agent

- [x] 2.1 设计并接入一个专门检查 planner candidate 的 review agent，聚焦 auth、async、AI profile 和 executable 关键字段一致性。
- [x] 2.2 让 review agent 在缺失或冲突时优先产出 targeted clarificationQuestions，而不是依赖第 3 层后处理补救。
- [x] 2.3 保持 review agent 仍为 planner 内部角色，不改变 `ApiImportAgentPlannerPort` 的单一结果契约。
- [x] 2.4 明确 `OpenAiCompatibleImportAgentPlannerProvider.plan(...)` 中的接入点：保持 staged tool-calling 不变，在 `SUBMIT_PLAN` 产出 candidate 后执行 review，再进入 `ImportAgentPlannerJsonSupport.buildPlan(...)`。
- [x] 2.5 优先复用并收紧 `PlanReviewPlannerSubagent` 职责；仅当单类职责仍然过宽时，再拆出更聚焦的内部 review subagent。
- [x] 2.6 约定 review 输出格式至少覆盖：结构 patch、clarificationQuestions 增量、review 摘要日志，避免再次把检查结论藏回自由文本。

## 3. 缩减第 3 层智能补救

- [x] 3.1 收缩 `ImportAgentPlannerJsonSupport`，保留 parse / merge / minimal normalize / validate 职责，移除高风险字段的主路径智能补救。
- [x] 3.2 让 submit/query 折叠、async 鉴权判断等高风险决策前移到规划阶段；guardrail 只做确定性守卫，不负责把缺失计划补成 executable。
- [x] 3.3 保留低风险格式兼容，如 schema alias 和已知 enum alias，但不得再借此隐式补齐缺失关键字段。
- [x] 3.4 优先清理下列方法中的高风险补救职责：`normalizeDraft(...)`、`reconcileMissingSlots(...)`、`normalizeAsyncTaskQueryAssets(...)`、`mergeAsyncTaskConfig(...)`、`resolveAsyncTaskQueryUrlTemplate(...)`、`resolveAsyncTaskAuthMode(...)`、`resolveAsyncTaskAuthScheme(...)`、`resolveAsyncTaskAuthConfig(...)`、`normalizeAsyncTaskConfig(...)`。
- [x] 3.5 保留 `buildPlan(...)` 作为最终装配入口，但确保 executable 只由结构化字段、current plan merge 和 clarification gate 共同决定，而不是由后处理猜测触发。

## 4. 回归验证

- [x] 4.1 更新 infrastructure / service 测试，覆盖“关键字段缺失 => clarification”而不是“后处理补救成 executable”的新行为。
- [x] 4.2 覆盖 review agent 对 auth、async、AI profile 冲突的降级行为，以及 current plan partial merge 仍保持确定性的回归。
- [x] 4.3 使用 Java 17 运行 import-agent 相关的 infrastructure、service、adapter 窄范围测试，确认边界收缩未破坏现有主链路。
- [x] 4.4 优先补齐 review/orchestrator 单测，再调整 `ImportAgentPlannerJsonSupportTest`，最后回归 `OpenAiCompatibleImportAgentPlannerProviderTest`、`ApiImportAgentApplicationServiceTest`、`ApiImportAgentWebDelegateTest`。

## 5. 实施顺序

- [x] 5.1 先改测试断言和 review agent 行为定义，建立“缺关键字段即 clarification”的新基线。
- [x] 5.2 再删除第 3 层对应智能补救，避免出现关键字段无人检查的中间态。
- [x] 5.3 若行为收敛后 `ImportAgentPlannerJsonSupport` 仍明显过大，再单独提出 parser / merger / validator 物理拆分类重构。
