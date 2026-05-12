## Context

`aether-console` 首页已经承载 API 市场浏览：列表通过 Discovery 列表契约加载已发布资产，点击卡片后按需调用 `GET /api/v1/discovery/assets/{apiCode}` 展示详情。详情面板目前展示名称、描述、发布者、分类、平台统一调用地址、订阅状态、Playground 入口、请求方法、鉴权方式、请求模板、请求示例、响应示例和 AI 能力。

本变更必须保持市场只读边界：文档导出只能消费 `../docs/api/api-catalog-discovery.yaml` 已公开的 Discovery 字段，以及既有 `/api/v1/access/{apiCode}` 平台统一调用地址推导。不得读取或导出上游地址、内部鉴权配置、平台代理配置、未契约化参数 schema 或状态码说明。

前端实现继续遵守 `../docs/spec/AetherAPI 前端技术栈与开发规范文档.md` 和 `aether-console/DESIGN.md`：页面层负责编排，API 请求走 `src/api`，批量导出状态和文档生成逻辑沉到同域 composable / feature helper，用户可见文案走 i18n，交互控件使用既有 Button、Card、StateBlock、DisplayTag、MethodTag 等体系。

## Goals / Non-Goals

**Goals:**

- 支持从当前市场详情导出单个 API 的 Markdown 文档文件。
- 支持在市场列表中多选 API，并导出一个合并 Markdown 文件。
- 批量导出时逐个加载详情，成功项进入文档正文，失败项列在文档顶部。
- 保持卡片点击查看详情与复选框多选导出两条交互路径互不干扰。
- 用可测试的纯函数生成 Markdown 内容和文件名，便于覆盖字段缺失、AI API、标准 API、局部失败等场景。

**Non-Goals:**

- 不生成 PDF、Word、ZIP 或多文件包。
- 不生成完整 OpenAPI 文档片段，因为现有 Discovery 契约没有参数 schema、响应 schema、状态码矩阵等信息。
- 不新增后端导出接口，不修改 `../docs/api/api-catalog-discovery.yaml`。
- 不导出资产工作台内部字段、上游地址、authConfig、平台代理配置或用户私有凭证。
- 不改变市场搜索、详情加载、订阅和 Playground 跳转的既有语义。

## Decisions

### 1. Use frontend Markdown generation

Decision: implement a small catalog document export helper that accepts `DiscoveryAssetDetail` values and produces Markdown text plus deterministic file names.

Rationale: all exportable content already exists in the Discovery detail response or can be derived from the public Unified Access path. A backend export endpoint would add avoidable contract surface without providing new data.

Alternative considered: add a backend document export API.

Why not: it would require a new API contract and would duplicate formatting logic that is currently presentation-oriented and frontend-specific.

### 2. Export one Markdown file for both single and multi-select

Decision: single detail export downloads `aetherapi-{apiCode}-doc.md`; multi-select export downloads `aetherapi-market-docs-{YYYY-MM-DD}.md`.

Rationale: Markdown is easy to inspect, paste into docs, review in source control, and generate without external dependencies. A single merged file for multi-select preserves order and avoids browser download spam.

Alternative considered: download one file per selected API.

Why not: multiple browser downloads are noisy, harder to track, and offer less room to summarize partial failures.

### 3. Keep selection separate from detail navigation

Decision: add an independent checkbox-style control on each market card. Clicking the card still selects the detail; clicking the checkbox only toggles export selection and must stop event propagation.

Rationale: the current page uses card click for detail browsing. Overloading the selected detail state for export would make it impossible to prepare a batch while inspecting another API.

Alternative considered: use the current selected detail as the only export target.

Why not: it does not support the requested multi-select export and weakens the existing browsing flow.

### 4. Batch export is best-effort

Decision: when exporting multiple APIs, load details for selected `apiCode` values, collect successes and failures, then download a Markdown file if at least one detail loads successfully. Failed items are listed near the top of the file with their `apiCode` and a generic localized failure reason.

Rationale: partial export protects the user's successful work and makes failures visible in the exported artifact itself.

Alternative considered: fail the entire export if any selected detail fails.

Why not: one transient detail failure would block all usable documentation and create more retry work.

### 5. Use existing browser download pattern without new dependency

Decision: create a `Blob` with `text/markdown;charset=utf-8`, use `URL.createObjectURL`, click a temporary anchor, then revoke the object URL. Keep this in a small utility or feature helper so it can be tested or reused.

Rationale: `UnifiedAccessPlayground.vue` already uses this browser-native download pattern for binary responses, and Markdown download does not require a library.

Alternative considered: install a file download dependency.

Why not: the native browser API is sufficient and keeps the change small.

## Risks / Trade-offs

- [Risk] Large selections can trigger many detail requests. -> Mitigation: disable export actions while exporting and run requests through a controlled batch path. If implementation chooses parallel loading, keep result ordering tied to the selected list.
- [Risk] Users may expect complete OpenAPI docs. -> Mitigation: label the action as API document export and include only contract-backed sections; do not name it OpenAPI export.
- [Risk] Exported Markdown may include empty sections. -> Mitigation: omit optional sections when the Discovery field is absent, except for a concise summary that can show unavailable values.
- [Risk] Checkbox interaction may conflict with card selection. -> Mitigation: stop propagation on checkbox clicks and provide clear selected-for-export visual state distinct from current detail selection.
- [Risk] Partial failures can be overlooked. -> Mitigation: put failure summary at the top of the exported Markdown and keep UI feedback after export.

## Migration Plan

No data migration or backend rollout is required. The feature can ship as a frontend-only enhancement behind the new market export UI. Rollback is limited to removing the export controls and helper usage; existing market browsing remains unchanged.

## Open Questions

- None. The user confirmed single export should download a file and batch export should preserve successful items while listing failures at the top.
