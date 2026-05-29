## Why

当前自动导入计划已经有 `assetPlans[].categoryCode`，但 planner 不知道系统里有哪些可用分类，也没有专门角色判断 API 资产应该归入哪个分类。结果是用户需要手动补分类编码，或者模型凭空生成分类，体验和数据质量都不稳定。

顶层文档已确认：`docs/api/api-category-lifecycle.yaml` 提供分类查询与有效性语义，`docs/sql/api-category.sql` 定义分类主数据，`docs/api/api-import-agent.yaml` 已暴露导入计划中的分类计划和资产分类字段。本变更只增强 Import Agent 内部规划能力，不新增公共 API 字段，不修改数据库结构。

## What Changes

- 新增 Import Agent 内部分类选择子 agent，用于根据可用分类列表和 API 资产语义自动填写 `assetPlans[].categoryCode`。
- 在应用服务层查询启用状态的分类候选，并通过 `ImportAgentPlannerRequest` 传给 planner，避免 infrastructure planner 直接访问数据库。
- 调整 planner prompt，让模型优先从候选分类中选择，不再凭空生成分类编码。
- 子 agent 校验并补齐分类计划：高置信度时使用已有分类并生成 `categoryPlans[].action = USE_EXISTING`；低置信度时生成中文追问。
- 保留现有 `CREATE_IF_MISSING` 执行能力，但自动分类默认不创建新分类，除非用户明确要求。
- 与已有 schema、异步任务、AI capability tags 等子 agent 协同，确保分类选择发生在计划审查之前。

## Capabilities

### New Capabilities

- `api-import-agent-category-classification-subagent`: Import Agent 根据系统启用分类和 API 资产信息自动选择、校验、补齐分类编码的内部规划能力。

### Modified Capabilities

- 无。

## Impact

- 后端 service：`ApiImportAgentApplicationService` 需要在创建会话和追加对话时查询启用分类，并构造带候选分类的 planner request。
- 后端 service model：`ImportAgentPlannerRequest` 需要携带轻量分类候选列表。
- 后端 infrastructure planner：新增分类选择 role、子 agent、registry 注册、prompt 上下文和审查/澄清规则。
- 测试：需要覆盖候选分类注入、自动选择、保留已有有效分类、无候选/低置信度追问、禁用/不存在分类不被自动使用等用例。
- API/SQL 文档：不新增接口、不改字段、不改表结构；继续以 `docs/api/api-category-lifecycle.yaml`、`docs/api/api-import-agent.yaml` 和 `docs/sql/api-category.sql` 作为权威契约。
