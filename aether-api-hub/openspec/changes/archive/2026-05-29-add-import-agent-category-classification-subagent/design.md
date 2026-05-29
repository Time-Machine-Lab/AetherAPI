## Context

API Catalog 领域文档将分类、上游配置、鉴权方案和示例归入资产配置范畴，并约束 Import Agent 的 planner 只负责生成结构化导入计划，真实写操作由 executor 复用 `CategoryUseCase`、`ApiAssetUseCase` 等确定性应用服务完成。

当前代码里，导入计划模型已有 `ImportAssetPlanModel.categoryCode` 和 `ImportCategoryPlanModel`，执行阶段也能通过 `ensureCategory` 处理分类计划。但 planner request 只包含文档、意图、当前计划和对话轮次，不包含可用分类候选；现有子 agent 也只覆盖文档事实、鉴权、异步任务、schema、计划审查和澄清策略，没有分类选择角色。

分类查询能力已经存在于 `CategoryUseCase.listCategories(status, page, size)`，顶层契约也说明只有 `ENABLED` 分类可被新资产引用。因此分类候选应由 service 层查询并传入 planner，而不是让 infrastructure planner 直接查询数据库。

## Goals / Non-Goals

**Goals:**

- 在信息足够时自动填写 `assetPlans[].categoryCode`，减少用户手动输入分类编码。
- 自动选择必须基于系统已有启用分类，避免模型编造分类编码。
- 在分类选择不确定时输出中文追问，并尽量给出候选默认值。
- 保持 DDD 分层边界：应用服务查询分类候选，planner 子 agent 只消费候选并生成计划。
- 与现有 schema generation、capability tags、plan review 和 clarification strategy 子 agent 协同。

**Non-Goals:**

- 不新增分类管理 API，不修改 `docs/api/api-category-lifecycle.yaml`。
- 不修改分类表结构，不新增分类字典表，不修改 `docs/sql/api-category.sql`。
- 不把分类查询逻辑放进 infrastructure planner 子 agent。
- 不默认创建新分类；`CREATE_IF_MISSING` 仅在用户明确表达要创建新分类时使用。
- 不解决价格配置、鉴权配置、schema 生成等其它自动导入能力。

## Decisions

### 决策 1：由 service 层注入启用分类候选

`ApiImportAgentApplicationService` 在 `createSession` 和 `appendTurn` 调用 planner 前，通过 `CategoryUseCase.listCategories(CategoryStatus.ENABLED, 1, 100)` 获取可用分类候选，并放入 `ImportAgentPlannerRequest`。

候选模型建议只包含 `categoryCode`、`categoryName`、`status` 三类 planner 必要信息。即使当前只查询 `ENABLED`，保留 `status` 也便于测试和后续防御式校验。

替代方案是让 `CategoryClassificationPlannerSubagent` 注入 repository 或 use case 自行查询。拒绝该方案，因为 planner infrastructure 会越过应用服务边界，测试也会变得更重。

### 决策 2：新增独立 `CATEGORY_CLASSIFICATION` 子 agent

新增 `ImportAgentPlannerSubagentRole.CATEGORY_CLASSIFICATION` 和 `CategoryClassificationPlannerSubagent`，职责限定为：

- 消费 `ImportAgentPlannerRequest.availableCategories`。
- 为缺失 `categoryCode` 的资产选择最合适的启用分类。
- 校验已有 `categoryCode` 是否在候选分类中；有效则保留，无效则按置信度替换或追问。
- 同步补齐 `categoryPlans`，对已存在分类使用 `USE_EXISTING`。
- 记录中文澄清问题，不直接执行创建分类、资产注册或发布。

子 agent 排序建议位于 `SCHEMA_GENERATION` 之后、`PLAN_REVIEW` 之前。这样分类判断可以利用请求/响应示例、schema、异步配置和 AI profile；计划审查仍能在最后检查分类是否完整。若 capability tags 子 agent 同时存在，推荐顺序为 schema generation -> category classification -> capability tags -> plan review，或 schema generation -> capability tags -> category classification -> plan review，具体以两个子 agent 是否互相消费字段为准，但二者都必须早于 plan review。

### 决策 3：分类选择采用候选约束加置信度策略

分类选择不能自由生成编码，只能从 `availableCategories` 中选择。推荐策略：

- 明确命中：资产名、apiCode、URL、模型名、文档摘要或 schema 字段与分类 code/name 有明显语义匹配时自动填写。
- 领域启发：AI 对话/补全/embedding/rerank 优先匹配 LLM 或 AI 相关分类；图片生成匹配 image；视频生成匹配 video；语音相关匹配 speech/audio。
- 已有值优先：当前计划或用户回答中的有效分类优先保留。
- 低置信度：不随意填默认值，生成中文追问，并可列出 2-3 个候选分类作为建议。
- 无候选分类：保留非可执行计划，提示用户先创建或启用分类。

### 决策 4：计划可执行性由 plan review 兜底

分类子 agent 负责补齐和提出追问，`PlanReviewPlannerSubagent` 应继续兜底检查：发布或导入资产时，若 `categoryCode` 为空、未出现在启用候选中，或对应 `categoryPlans` 缺失，应保持计划不可执行并追加中文澄清项。

执行阶段已有 `ensureCategory`，但它不应成为主要体验路径。执行前仍可保留防御性校验，避免无效分类进入资产修订。

### 决策 5：不更新 API/SQL 顶层文档

本变更只在后端内部 planner request 增加候选上下文，不改变 HTTP 请求/响应契约，也不改变分类表结构。`docs/api/api-import-agent.yaml` 已有导入计划分类字段，`docs/api/api-category-lifecycle.yaml` 已有分类查询与校验语义，`docs/sql/api-category.sql` 已有分类主数据定义，因此无需使用 `tml-docs-spec-generate` 生成新的 API/SQL 文档。

## Risks / Trade-offs

- [Risk] 分类语义匹配不一定符合运营预期 -> Mitigation：只从启用分类中选择，保留已有有效值，低置信度时追问而不是强填。
- [Risk] 分类列表超过 100 条时候选不完整 -> Mitigation：首版沿用现有 `listCategories` 最大页大小；后续如分类规模扩大，可增加专门的轻量查询用例或分页聚合策略。
- [Risk] 与 capability tags 子 agent 排序冲突 -> Mitigation：两者职责独立，均早于 plan review；实现时通过 registry order 和测试固定预期顺序。
- [Risk] service 层每轮都查分类带来额外开销 -> Mitigation：分类列表小且已有分页上限；后续可在应用服务层增加短时缓存，但不在本提案范围内。

## Migration Plan

1. 实现前确认 `docs/api/api-category-lifecycle.yaml`、`docs/api/api-import-agent.yaml`、`docs/sql/api-category.sql` 与当前代码字段一致。
2. 新增 planner request 分类候选模型，并在应用服务层注入启用分类。
3. 新增分类选择子 agent、角色和 registry 注册。
4. 更新 prompt、plan review 和澄清策略，确保分类候选被正确使用。
5. 补充单元测试和 Import Agent 聚焦测试。
6. 回滚方式：从 registry 移除分类子 agent，并停止向 planner request 注入候选；不涉及数据迁移。

## Open Questions

- 默认候选上限是否长期保持 100 条，还是后续需要专门的“全部启用分类轻量查询”用例？
- 当所有候选都低置信度时，是否允许选择一个平台默认分类，还是必须追问？本提案建议必须追问。
- 如果用户明确说“没有合适分类，请创建一个”，是否沿用现有 `CREATE_IF_MISSING`，还是要求前端先进入分类管理流程？本提案暂时保留现有能力，但不主动触发。
