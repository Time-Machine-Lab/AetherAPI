## Why

Observability 一期只需要先把 API 调用事实稳定记录下来，支撑开发者控制台后续的日志查看，并为后续统计、计费、风控预留统一事实来源。当前项目虽然已有 Unified Access 与 Consumer/Auth 设计，但还缺少一份落到顶层文档与代码实现层面的最小调用日志基础能力提案。

## What Changes

- 新增最小 API 调用日志写入能力，在 Unified Access 完成一次调用后记录平台侧调用事实。
- 定义最小调用日志范围：调用主体快照、目标 API 快照、请求时间、耗时、调用结果、错误摘要，以及 AI 场景的预留扩展字段。
- 明确一期不记录完整原始请求体与响应体，不建设告警、监控大盘、计费结算等复杂观测能力。
- 新增顶层权威 SQL 文档 `docs/sql/api_call_log.sql`，并要求后续使用 `tml-docs-spec-generate` 的 SQL 模板生成或更新。
- 保持 Consumer 为隐式内部概念，不新增任何面向前端的 Consumer 显式业务接口。

## Capabilities

### New Capabilities
- `observability-call-log-foundation`: 建立 API 调用日志的最小事实模型、写入规则与持久化边界。

### Modified Capabilities

None.

## Impact

- 影响 `docs/design/aehter-api-hub/Aether API Hub Observability领域设计文档.md` 对应的一期观测实现落地。
- 新增顶层 SQL 权威文档 `docs/sql/api_call_log.sql`，文件名与表名保持一致。
- 影响 Unified Access 调用完成后的日志写入集成点，以及 Observability 领域的 domain / application / infrastructure 实现。
- 不新增业务接口文档，不修改现有 `docs/api/` 文件。
