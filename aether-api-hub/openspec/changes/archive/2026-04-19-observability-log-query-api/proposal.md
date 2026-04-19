## Why

Observability 一期除了要“记下来”，还需要给开发者控制台提供可联调、可查看的最小 API 日志查询接口，否则前端无法承接“API 调用 / API 日志”页面。本提案聚焦最小查询闭环，不引入监控平台化或复杂分析能力。

## What Changes

- 新增开发者控制台使用的最小 API 日志查询能力，支持当前登录用户查看自己的 API 调用日志。
- 提供最小查询范围：日志列表、日志详情、按 API 与时间范围筛选。
- 新增顶层权威接口文档 `docs/api/api-call-log.yaml`，并要求后续使用 `tml-docs-spec-generate` 的 API 模板生成或更新；该文件必须与 `ApiCallLogController.java` 一一映射。
- 查询接口仅暴露平台业务视角，不显式暴露 Consumer 注册、启停、管理等内部概念。
- 复用调用日志基础表与读模型，不额外引入复杂报表、聚合统计、告警接口。

## Capabilities

### New Capabilities
- `observability-log-query-api`: 提供面向开发者控制台的最小 API 日志查询接口与查询约束。

### Modified Capabilities

None.

## Impact

- 影响 `docs/design/aehter-api-hub/Aether API Hub Observability领域设计文档.md` 中开发者控制台日志查看能力的落地。
- 新增顶层 API 权威文档 `docs/api/api-call-log.yaml`，文件名与 `ApiCallLogController.java` 保持直接映射。
- 影响 Observability 领域 read model、application query service、adapter controller 与前端联调契约。
- 依赖调用日志基础能力提供稳定的日志数据来源，不新增新的数据库表设计文档。
