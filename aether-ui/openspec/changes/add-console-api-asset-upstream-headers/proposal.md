## 为什么

后端会允许 API 资产保存固定上游请求头，并在 Unified Access 转发给 provider 时携带。`aether-console` 需要将这个 owner-only 配置与 `authConfig` 明确区分；Import Agent 也需要展示并澄清计划中的请求头，而不是让用户编辑原始 JSON 或把请求头塞进鉴权字段。

## 变更内容

- 在 `../docs/api/api-asset-management.yaml` 暴露字段后，为当前用户资产管理 API 增加 `upstreamRequestHeaders` DTO/type 映射。
- 在所有者资产编辑器中增加固定上游请求头配置区，以结构化 name/value 行管理。
- 在 owner 资产详情中展示已保存的上游请求头，并注意敏感值展示。
- 除非后续后端契约显式暴露 browse-safe 数据，公开市场/Discovery 视图和市场文档导出不得展示上游请求头。
- 扩展 Import Agent 前端 DTO/types 和计划渲染，支持 `assetPlans[].upstreamRequestHeaders`。
- 允许指向上游请求头字段的引导式澄清项通过现有澄清答案流程渲染和提交。
- 补充 i18n 文案与重点测试，覆盖 API 映射、workspace 保存/清空、Import Agent 计划展示和澄清提交。

## 能力

### 新增能力

- `console-import-agent-upstream-headers`：`aether-console` Import Agent 工作流可以在生成的资产计划中展示并澄清结构化上游请求头。

### 修改能力

- `console-user-asset-workspace`：资产所有者可以编辑、清空、保存和查看自有 API 资产的上游请求头配置。
- `console-asset-auth-config`：上游鉴权配置继续与非鉴权固定上游请求头在资产编辑器中保持分离。

## 影响

- 权威依赖：
  - 前端实现前，后端 `../docs/api/api-asset-management.yaml` 必须在所有者资产请求/响应中暴露 `upstreamRequestHeaders`。
  - Import Agent 前端实现前，后端 `../docs/api/api-import-agent.yaml` 必须暴露 `assetPlans[].upstreamRequestHeaders` 和澄清路径。
  - `aether-console/DESIGN.md` 仍是表单布局、密集 workspace 控件和敏感配置展示规则的权威来源。
- 前端应用：`aether-console`。
- 前端区域：`src/api/catalog/*`、`src/api/import-agent/*`、`src/pages/workspace.vue`、`src/features/import-agent/ImportAgentWorkspace.vue`、locale 资源、mocks 和相关测试。
- 不新增路由，不新增全局状态库，不在页面层直接发起裸 HTTP 请求。
