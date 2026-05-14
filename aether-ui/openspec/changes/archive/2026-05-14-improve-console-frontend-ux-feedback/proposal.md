## Why

近期试用反馈表明，`aether-console` 已经具备 API 市场、开发者控制台、资产管理、API Key、统一调用和调用日志等核心页面，但在“发布、理解、调用、排查”这条主链路上的前端体验仍不够清晰、稳定和高效。

现在需要优先改进这些前端体验问题，因为 AetherAPI 的 MVP 价值依赖开发者能快速发布 API、看懂平台调用方式，并在调用失败时完成自助诊断。

## What Changes

- 优化当前用户 API 资产工作台：新建草稿、编辑资产、绑定 AI 能力配置改为模态框或抽屉等聚焦式流程，避免页面跳到下方编辑。
- 修复绑定 AI 能力配置时清空资产基础配置的问题，避免接口返回的局部数据覆盖用户已填写的表单状态。
- 为请求模板、请求示例、响应示例、上游地址、鉴权方式、AI 能力字段补充字段说明。
- 在资产详情、市场详情和 Playground 跳转路径中展示并支持复制平台统一调用地址，即 `/api/v1/access/{apiCode}`。
- 优化 API 市场卡片和资产列表行：增加图标化元信息、状态/类型/方法标签、更多可扫读字段和清晰操作入口。
- 为请求示例、响应示例、Header、Payload、统一调用结果等 JSON/代码内容提供格式化展示、复制能力和更清晰的代码块样式。
- 优化调用日志页面：在现有接口字段范围内提升详情展示，并明确标识当前接口契约尚未提供的请求体、响应体、上游地址、Header 等诊断字段。
- 让资产上架流程中的分类/类型依赖更可见，避免“上架要求填写类型，但类型管理入口隐藏”的体验断层。
- 本次前端变更不实现 API 订阅功能，因为目前没有对应的订阅 API 契约和完整产品流程。

## Capabilities

### New Capabilities

- `console-asset-workspace-usability`：覆盖资产工作台的模态框/抽屉式新建与编辑、表单状态保护、字段说明、分类依赖可见性和资产列表增强。
- `console-marketplace-call-guidance`：覆盖 API 市场卡片信息增强、平台调用地址展示与复制、从市场到 Playground 的调用引导。
- `console-json-code-display`：覆盖 JSON/代码内容的统一展示、格式化、复制和契约边界处理。

### Modified Capabilities

- `console-api-call-log-pages`：改进调用日志详情展示，明确当前接口契约可展示字段与尚未暴露诊断字段的边界。

## Impact

- 影响应用：`aether-ui/aether-console`。
- 可能影响的前端区域：
  - `src/pages/workspace.vue`
  - `src/pages/index.vue`
  - `src/pages/playground.vue`
  - `src/features/credential/*`
  - `src/features/api-call-log/*`
  - `src/features/unified-access/*`
  - `src/features/catalog/*`
  - `src/composables/useWorkspaceCatalog.ts`
  - `src/composables/useCatalogDiscovery.ts`
  - `src/composables/useUnifiedAccessPlayground.ts`
  - `src/api/catalog/*`
  - `src/api/api-call-log/*`
  - `src/api/unified-access/*`
  - `src/components/ui/*`
  - `src/locales/zh-CN/*`
  - `src/locales/en-US/*`
- 继续沿用的接口契约：
  - `../docs/api/api-asset-management.yaml`
  - `../docs/api/api-catalog-discovery.yaml`
  - `../docs/api/unified-access.yaml`
  - `../docs/api/api-call-log.yaml`
- 如需展示真实的调用日志请求体、响应体、上游地址、请求 Header、响应 Header 等诊断数据，必须先更新 `../docs/api/api-call-log.yaml` 并完成后端日志快照能力。
- 不在本次范围内：
  - 后端资产删除持久化问题。
  - HTTPS 上游 502。
  - 流式请求超时。
  - 真实 API 订阅功能。
