## Why

当前 API Import Agent 已能把用户输入整理为导入计划，并在执行阶段复用 API Catalog 的资产应用服务完成真实写入；但 Planner 规划时看不到当前用户已有资产，只能围绕“导入新资产”生成计划，导致“修改已有资产”依赖用户手动提供完整 `apiCode` 与字段快照，且无法清晰表达“只允许更新已有资产”还是“存在则更新、不存在则创建”。

随着 API 资产导入能力进入可持续维护阶段，Agent 需要从一次性导入入口升级为受控的资产维护入口：先查询当前用户资产上下文，再生成可确认、可审计、可回放的编辑计划。

## What Changes

- 为 API Import Agent 新增 owner-scoped 已有资产上下文能力：规划前可读取当前用户资产候选列表，并在用户明确指定资产时读取该资产详情快照。
- 扩展导入计划中的资产计划语义，新增显式动作：创建新资产、修改已有资产、存在则更新否则创建。
- 修改执行语义：`UPDATE_EXISTING` 只能修改当前用户已有资产；`CREATE` 遇到已存在 `apiCode` 必须失败；`UPSERT` 才保留当前“存在则修改、不存在则创建”的兼容行为。
- 保持 Import Agent 的编排边界：Planner 只生成结构化计划，不直接写 API Catalog；Executor 继续复用 `ApiAssetUseCase` 与 `CategoryUseCase`。
- 更新 `docs/api/api-import-agent.yaml` 作为 `ApiImportAgentController.java` 的权威 API 契约，补充资产计划动作、目标资产上下文与执行步骤枚举。该文件后续必须使用 `tml-docs-spec-generate` 的 API 生成模板更新。
- 不新增或修改数据库表结构；`api_import_agent_session.plan_snapshot_json` 与 `api_import_agent_run.step_results_json` 继续承载计划快照和执行投影，因此本变更不需要更新 `docs/sql/`。

## Capabilities

### New Capabilities

- `api-import-agent-existing-asset-editing`: 定义 API Import Agent 查询当前用户已有资产、生成显式资产编辑计划、确认后安全修改已有资产的行为。

### Modified Capabilities

- 无。`catalog-owner-asset-management` 已提供当前用户资产查询与修改能力，本变更只规定 Import Agent 如何消费这些既有能力，不改变 Catalog 资产管理本身的需求边界。

## Impact

- API 契约：更新 `docs/api/api-import-agent.yaml`，仍一对一映射 `ApiImportAgentController.java`。
- 服务模型：扩展 Import Agent 计划模型、资产计划响应模型、步骤类型枚举和 Planner 请求上下文。
- 应用服务：`ApiImportAgentApplicationService` 在规划前加载当前用户资产候选与目标详情；执行阶段按显式动作分支处理创建、修改与 upsert。
- Planner/工具契约：更新 LLM planner prompt、tool schema、JSON parser、draft validator 和 plan merger，确保已有资产编辑保持字段补丁语义，未被用户要求修改的字段不被清空。
- 测试：覆盖已有资产候选加载、目标资产详情注入、`CREATE` 冲突、`UPDATE_EXISTING` 不存在/非 owner 拒绝、`UPSERT` 兼容、发布后关键配置修改退回 `UNPUBLISHED` 等场景。
