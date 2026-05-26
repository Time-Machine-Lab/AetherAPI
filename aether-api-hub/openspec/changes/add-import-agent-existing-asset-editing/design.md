## Context

现有 API Catalog 顶层设计已经明确：API 资产是当前用户拥有的市场资产，`ApiAssetController.java` 通过 `docs/api/api-asset-management.yaml` 提供 owner-scoped 的资产列表、详情、修改、发布、下架和删除能力；资产主表由 `docs/sql/api-asset.sql` 承载，`apiCode` 全局唯一且创建后不可修改。

现有 API Import Agent 顶层设计也已明确：Import Agent 是 `文档理解 -> 结构化计划 -> 显式确认 -> 确定性执行` 的前置编排入口。Planner 只生成计划快照，Executor 必须复用 `CategoryUseCase`、`ApiAssetUseCase` 等既有应用服务完成真实写操作，不能绕过 API Catalog 自行写库。

当前实现已经在执行阶段通过 `ApiAssetUseCase.getAssetByCode(ownerUserId, apiCode)` 判断资产是否存在：存在则 `reviseAsset`，不存在则 `registerAsset + reviseAsset`。这说明底层修改通道已经存在，但 Planner 缺少当前用户已有资产上下文，计划模型也缺少显式动作语义，因此无法安全表达“只修改已有资产”。

本变更涉及 `docs/api/api-import-agent.yaml` 的响应契约扩展，该文件仍一对一映射 `ApiImportAgentController.java`。后续更新该权威 API 文档时必须使用 `tml-docs-spec-generate` 技能的 API 生成模板。本变更不新增或修改数据库表结构，因此不更新 `docs/sql/`。

## Goals / Non-Goals

**Goals:**

- 让 Import Agent 在规划阶段获得当前用户已有资产候选与目标资产安全摘要。
- 让资产计划显式表达 `CREATE`、`UPDATE_EXISTING`、`UPSERT` 三种动作。
- 让已有资产修改采用补丁语义：只修改计划明确声明的字段，未声明字段由执行阶段读取当前资产后保持不变。
- 继续通过显式确认门禁和既有 `ApiAssetUseCase` 执行真实资产修改。
- 避免向 LLM 上下文、thinking 事件、普通 Agent 回复中泄露 `authConfig` 等敏感配置明文。

**Non-Goals:**

- 不新增资产管理 Controller，不改变 `ApiAssetController.java` 的 owner-scoped CRUD 契约。
- 不让 Planner 或工具调用直接写入 API Catalog。
- 不新增 Import Agent 表或 API Catalog 表。
- 不改变 Discovery、Unified Access、Observability 的读模型边界。
- 不引入后台队列、异步补偿事务或跨会话协作编辑。

## Decisions

### Decision 1: 规划前由应用服务注入当前用户资产上下文

`ApiImportAgentApplicationService` 在创建会话和追加轮次时，除加载可用分类候选外，还应通过 `ApiAssetUseCase.listAssets` 加载当前用户资产候选。候选信息只包含规划所需的安全字段，例如 `apiCode`、`assetName`、`assetType`、`categoryCode`、`status`、`requestMethod`、`updatedAt`、是否存在异步任务配置、是否存在 AI 能力档案。

当用户输入、当前计划或澄清答案明确指向某个 `apiCode` 时，应用服务可通过 `ApiAssetUseCase.getAssetByCode(ownerUserId, apiCode)` 加载该资产的详情摘要。该摘要仍必须脱敏：可以暴露 `authScheme` 和“authConfig 已配置/未配置”的布尔状态，但不得把 `authConfig` 明文传给 Planner、thinking 或 Agent 回复。

备选方案：让 LLM 通过 tool calling 动态查询资产。

不采用原因：当前 Import Agent 的生产路径仍以结构化计划为中心，tool-calling 开关并非默认启用；资产查询涉及 owner 权限和敏感字段脱敏，放在确定性的应用服务中更容易保证边界一致。

### Decision 2: 资产计划使用显式动作枚举

新增 `ImportAssetPlanAction`：

- `CREATE`：只允许创建新资产；如果 `apiCode` 已存在则执行失败。
- `UPDATE_EXISTING`：只允许修改当前用户已有资产；如果资产不存在或不属于当前用户则执行失败。
- `UPSERT`：保留当前兼容行为，存在则修改，不存在则创建。

Planner 必须在每个 `assetPlan` 中输出动作。若用户意图无法判断是创建还是修改，计划必须保持不可执行并生成澄清项，而不是默认 upsert。

备选方案：继续由 Executor 通过 `apiCode` 是否存在隐式判断。

不采用原因：隐式判断无法防止用户本意是修改已有资产却因 `apiCode` 写错而创建新资产，也无法让用户在确认前看清楚本次操作会创建还是修改。

### Decision 3: 已有资产修改采用 `changedFields` 补丁掩码

为 `ImportAssetPlanModel` 增加字段变更掩码，例如 `changedFields`。`UPDATE_EXISTING` 计划中只有出现在 `changedFields` 的字段才会写入 `ReviseApiAssetCommand` 的 `isXxxSet=true`；未出现在掩码中的字段必须保持当前资产值不变。

如果用户明确要求清空某个可空字段，计划可以将该字段值设为 `null`，并把字段名放入 `changedFields`，由执行阶段按“显式清空”处理。这样可以区分“未提及字段”和“明确清空字段”。

备选方案：让 Planner 输出合并后的完整资产快照。

不采用原因：完整快照容易在多轮对话中清空用户未重述的字段，也会诱导系统把敏感配置明文放进 Planner 上下文。

### Decision 4: Executor 按动作执行并继续复用 Catalog 应用服务

执行阶段按 `ImportAssetPlanAction` 分支：

- `CREATE`：先确认当前用户不可见该 `apiCode` 对应资产且全局未冲突，再调用 `registerAsset` 和必要的 `reviseAsset`。
- `UPDATE_EXISTING`：先调用 `getAssetByCode(ownerUserId, apiCode)`，成功后按 `changedFields` 构造 `ReviseApiAssetCommand`，必要时调用 `attachAiCapabilityProfile` 和 `publishAsset`。
- `UPSERT`：保留当前存在则修改、不存在则创建的兼容逻辑，但仍按补丁掩码控制写入字段。

如果修改已发布资产的关键上游配置，状态回退到 `UNPUBLISHED` 的规则继续由 `ApiAssetAggregate` 与 `ApiAssetUseCase` 承担，Import Agent 不重复实现生命周期规则。

备选方案：在 Import Agent 中新增专用资产写入逻辑。

不采用原因：会复制 API Catalog 的 owner 校验、发布校验、关键配置状态迁移和软删除规则，破坏当前 DDD 分层。

### Decision 5: 顶层契约只更新 Import Agent API 文档

本变更需要更新 `docs/api/api-import-agent.yaml`，补充：

- `ImportAssetPlanResp.action`
- `ImportAssetPlanResp.changedFields`
- 可选的已有资产上下文摘要响应，例如 `existingAssetContext` 或 `matchedExistingAsset`
- `ImportStepResultResp.stepType` 中与显式动作相关的枚举值

`docs/api/api-asset-management.yaml` 不需要新增接口；Import Agent 内部复用已有资产管理用例即可。`docs/sql/` 不需要更新，因为计划动作和补丁掩码存放在既有 `plan_snapshot_json` 与 `step_results_json` 中。

## Risks / Trade-offs

- [LLM 错误选择动作] -> 通过动作枚举、不可执行澄清、用户确认门禁和执行阶段存在性校验共同拦截。
- [敏感配置泄露到上下文] -> 资产上下文脱敏，`authConfig` 只暴露配置状态；thinking 和回复继续禁止输出敏感值。
- [补丁掩码与字段值不一致] -> Parser 和 validator 必须校验 `changedFields` 中的字段名合法；执行阶段只信任白名单字段。
- [现有 upsert 行为被调用方依赖] -> 保留 `UPSERT` 作为兼容动作，但默认规划策略不应在意图不明确时自动选择它。
- [计划 JSON 变更影响旧会话] -> Parser 对缺失 `action` 的旧计划可按兼容规则解释为 `UPSERT`，但新计划必须输出动作。

## Migration Plan

1. 使用 `tml-docs-spec-generate` 的 API 生成模板更新 `docs/api/api-import-agent.yaml`。
2. 扩展 Import Agent service model 与 API DTO 映射，保持旧计划缺失动作时的兼容解析。
3. 注入 owner-scoped 资产候选与脱敏详情摘要。
4. 更新 Planner prompt、tool schema、JSON parser、validator、merger。
5. 更新 Executor 动作分支与步骤结果。
6. 补齐单元测试和 Web delegate 测试。
7. 回滚时可停止输出新动作字段，并让旧 Executor upsert 逻辑继续处理既有计划；由于无数据库结构变更，不需要数据迁移回滚。

## Open Questions

- 资产候选列表默认加载数量建议先限制为 50 或 100；如果用户资产规模变大，后续是否需要引入关键词预筛选或专用检索工具。
- 已有资产详情摘要中是否允许暴露 `upstreamUrl` 完整值；若后续认为 URL 也可能包含敏感查询参数，应在注入前做查询参数脱敏。
