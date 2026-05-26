## 1. 权威文档与开发约束

- [x] 1.1 阅读并遵守 `docs/spec/Aether API HUB 后端代码开发规范文档.md`，确认后端分层、命名、测试和接口实现约束。
- [x] 1.2 使用 `tml-docs-spec-generate` 的 API 生成模板更新 `docs/api/api-import-agent.yaml`，该文件一对一映射 `ApiImportAgentController.java`。
- [x] 1.3 在 `docs/api/api-import-agent.yaml` 中补充 `ImportAssetPlanResp.action`、`ImportAssetPlanResp.changedFields`、已有资产上下文摘要响应字段和新的 `ImportStepResultResp.stepType` 枚举。
- [x] 1.4 确认本变更不涉及新增表或字段，不更新 `docs/sql/`；若实现中发现必须落库，先暂停代码实现并补充对应表名的 `docs/sql/<table>.sql` 权威文档。

## 2. 服务模型与 API DTO

- [x] 2.1 新增 `ImportAssetPlanAction` 服务枚举，包含 `CREATE`、`UPDATE_EXISTING`、`UPSERT`。
- [x] 2.2 扩展 `ImportAssetPlanModel`，增加 `action`、`changedFields` 以及必要的已有资产匹配摘要字段。
- [x] 2.3 扩展 Import Agent API 响应 DTO 与 Web delegate 映射，确保响应字段与 `docs/api/api-import-agent.yaml` 对齐。
- [x] 2.4 扩展 `ImportAgentStepType`，区分创建、更新、upsert 和已有资产上下文相关步骤结果。
- [x] 2.5 为旧计划 JSON 缺失 `action` 的情况提供兼容解析，默认按历史 upsert 行为解释，但新生成计划必须输出显式动作。

## 3. 已有资产上下文注入

- [x] 3.1 在 `ImportAgentPlannerRequest` 中增加当前用户资产候选列表与目标资产安全详情摘要。
- [x] 3.2 在 `ApiImportAgentApplicationService` 中通过 `ApiAssetUseCase.listAssets` 加载当前用户 owner-scoped 资产候选，并限制返回数量。
- [x] 3.3 当用户输入、当前计划或澄清答案明确包含 `apiCode` 时，通过 `ApiAssetUseCase.getAssetByCode` 加载当前用户拥有的目标资产详情摘要。
- [x] 3.4 对 Planner 上下文中的资产详情执行脱敏，禁止传入 `authConfig` 明文，只允许传入鉴权方案和是否已配置等安全信号。
- [x] 3.5 确保资产候选或详情加载失败不会泄露跨用户信息，并按现有会话错误处理方式返回可解释失败。

## 4. Planner 契约、解析与校验

- [x] 4.1 更新 planner prompt 和 tool schema，要求每个 `assetPlan` 输出显式 `action` 与合法 `changedFields`。
- [x] 4.2 更新 `ImportAgentPlanDraftParser`，解析 `action`、`changedFields` 和已有资产上下文相关字段。
- [x] 4.3 更新 `ImportAgentPlanDraftValidator`，当动作缺失、动作与上下文冲突或 `changedFields` 非法时生成澄清项或阻止计划执行。
- [x] 4.4 更新 plan merger，使多轮对话保留已有计划动作与补丁语义，未被用户重新声明的字段不被清空。
- [x] 4.5 更新 reply 生成约束，确认 Agent 回复和 thinking 摘要不得输出 `authConfig` 等敏感值。

## 5. 执行器语义

- [x] 5.1 重构 `ApiImportAgentApplicationService.applyAssetPlan`，按 `CREATE`、`UPDATE_EXISTING`、`UPSERT` 显式分支执行。
- [x] 5.2 实现 `CREATE` 冲突保护：目标 `apiCode` 已存在时记录失败步骤，不覆盖已有资产。
- [x] 5.3 实现 `UPDATE_EXISTING` 存在性保护：目标资产不存在或不属于当前用户时记录失败步骤，不创建新资产。
- [x] 5.4 根据 `changedFields` 构造 `ReviseApiAssetCommand` 的 `isXxxSet` 标志，支持未提及字段保留和显式 null 清空。
- [x] 5.5 继续通过 `ApiAssetUseCase.attachAiCapabilityProfile` 和 `publishAsset` 处理 AI 能力与发布动作，不在 Import Agent 中复制 Catalog 生命周期规则。

## 6. 测试与验证

- [x] 6.1 为 Planner 请求构建补充单元测试，覆盖只加载当前用户资产候选和目标详情脱敏。
- [x] 6.2 为 plan parser、validator、merger 补充单元测试，覆盖动作缺失、动作冲突、非法 `changedFields`、多轮补丁保留。
- [x] 6.3 为执行器补充单元测试，覆盖 `CREATE` 已存在失败、`UPDATE_EXISTING` 不存在失败、`UPDATE_EXISTING` 补丁保留字段、`UPSERT` 兼容行为。
- [x] 6.4 为 Web delegate/API 映射补充测试，确认响应字段与 `docs/api/api-import-agent.yaml` 对齐。
- [x] 6.5 运行后端相关 Maven 测试，至少覆盖 service、adapter、infrastructure 中 Import Agent 与 Api Asset 相关测试。
