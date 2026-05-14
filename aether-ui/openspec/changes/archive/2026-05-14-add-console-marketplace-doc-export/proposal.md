## Why

API 市场详情已经能展示调用地址、鉴权方式、请求模板和示例，但用户在评估或转交接口时仍需要手动复制多个区域再整理成文档，效率低且容易遗漏字段。

本次变更将市场详情中的契约字段生成可下载的 Markdown 文档，让单个 API 和多个 API 的文档导出都能在现有 Discovery 能力范围内完成。

## What Changes

- 在 `aether-console` API 市场详情面板提供“导出 API 文档”操作，当前选中 API 将导出为单个 Markdown 文件。
- 在 API 市场列表卡片上增加独立多选入口，保持“点击卡片查看详情”和“勾选用于导出”的交互分离。
- 在多选后展示批量导出工具条，支持查看已选数量、清空选择，并将选中 API 导出为一个合并 Markdown 文件。
- 批量导出按选中 `apiCode` 逐个加载 Discovery 详情，成功项写入文档正文；失败项不阻断导出，统一列在文件顶部。
- 文档内容仅来自 `docs/api/api-catalog-discovery.yaml` 已定义的 Discovery 详情字段，以及既有 `/api/v1/access/{apiCode}` 平台统一调用地址推导；不导出上游地址，不发明参数、响应 schema 或状态码字段。
- 复用现有 Vue 3、TypeScript、Tailwind、shadcn-vue、lucide 图标、i18n、API 层和代码展示/复制相关工具模式，不新增后端接口或第三方依赖。

## Capabilities

### New Capabilities

- `console-marketplace-doc-export`: 覆盖 API 市场详情单个 Markdown 文档导出、市场列表多选导出、批量详情加载、局部失败记录和导出文件内容边界。

### Modified Capabilities

- None.

## Impact

- Affected frontend code:
  - `aether-console/src/pages/index.vue`
  - `aether-console/src/composables/useCatalogDiscovery.ts` 或新增同域 composable
  - `aether-console/src/api/catalog/discovery.api.ts`
  - `aether-console/src/features/catalog/*`
  - `aether-console/src/utils/*`
  - `aether-console/src/locales/zh-CN/common.ts`
  - `aether-console/src/locales/en-US/common.ts`
- Affected specs:
  - 新增 `console-marketplace-doc-export`
- Affected API contracts:
  - 继续消费 `../docs/api/api-catalog-discovery.yaml`
  - 不需要新增或更新后端 API 契约
- Affected design constraints:
  - 遵循 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`
  - 遵循 `aether-console/DESIGN.md` 中的市场页布局、语义角色、按钮、状态反馈和卡片交互规则
- Risks:
  - 多选导出需要对多个详情接口发起请求，必须展示导出中的禁用/加载状态，避免重复触发。
  - 如果部分详情加载失败，导出文件必须保留成功项并在顶部列出失败项，避免用户误以为全部成功。
