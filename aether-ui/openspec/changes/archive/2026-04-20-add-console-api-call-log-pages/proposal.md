## Why

后端已归档提案 `2026-04-19-observability-call-log-foundation` 与 `2026-04-19-observability-log-query-api` 已经分别补齐了调用日志事实写入能力与当前用户日志查询契约，仓库根目录也已经存在权威接口文档 `docs/api/api-call-log.yaml`。但 `aether-console` 当前仍没有承接这些能力的前端入口，导致控制台里的 API 调用日志链路停留在“后端可查、前端不可见”的状态，开发者无法在控制台内完成日志列表、基础筛选和详情排查。

与此同时，现有前端归档提案已经明确把 `usage`、`billing`、`orders` 与 observability 页面保留为后续独立交付范围，`console-workspace` 目前也只对 `#credentials` 做了实际分区渲染。因此需要新增一份前端配套提案，在不发明新后端契约、不突破 `aether-console/DESIGN.md` 和前端统一分层规范的前提下，把 API 调用日志查询能力真正接入控制台。

## What Changes

- 在 `aether-console` 中新增“API 调用日志”前端能力，为当前登录用户提供调用日志列表、按目标 API 与时间范围筛选、分页浏览与单条详情查看。
- 复用根目录权威契约 `docs/api/api-call-log.yaml`，在前端新增对应的 API 封装、DTO/类型映射、页面级状态编排与国际化文案，不新增任何后端接口，也不扩展未文档化字段。
- 将日志能力落在现有控制台壳层与受保护工作区中，优先复用现有 `console-workspace` 的 hash 导航、卡片语义、状态反馈与列表/详情布局约束，而不是新开独立应用或额外路由体系。
- 为日志列表、详情、筛选、空态、错误态与越权/不存在反馈定义清晰的前端呈现边界，确保结果类型、错误摘要、AI 扩展字段等展示严格以后端返回为唯一事实来源。
- 明确本次变更不包含日志统计图表、聚合分析、管理员视角日志后台、完整原始请求/响应报文查看，也不顺带补齐 `usage`、`orders`、`billing` 等其他占位导航项。

## Capabilities

### New Capabilities

- `console-api-call-log-pages`: 定义 `aether-console` 中当前用户 API 调用日志的导航入口、分页列表、基础筛选、详情查看以及相关状态反馈，严格以 `docs/api/api-call-log.yaml` 为唯一契约来源。

### Modified Capabilities

- 无。

## Impact

- 受影响应用：`aether-console`
- 依赖的权威契约：`../docs/api/api-call-log.yaml`
- 参考的权威设计与规范：`../docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、`../docs/design/**/*.md`、`aether-console/DESIGN.md`
- 预期影响的前端区域：`src/pages`、`src/features`、`src/api`、`src/locales`、控制台导航配置与工作区 hash 分区编排
- 当前提案阶段不要求新增或修改根目录 `docs/api/`、统一前端规范文档或 `aether-console/DESIGN.md`；如果实现阶段发现现有权威文档不足以覆盖新增视觉/交互模式，必须先同步这些顶层文档再进入代码实现
