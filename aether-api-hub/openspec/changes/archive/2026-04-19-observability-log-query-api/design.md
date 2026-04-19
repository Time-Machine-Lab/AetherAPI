## Context

Observability 一期除了记录调用事实，还必须为开发者控制台提供最小 API 日志查看能力，否则前端无法完成“API 调用 / API 日志”联调。根据现有架构与领域设计，该能力应定位为 Observability 的读侧能力，只面向当前登录用户查看自己的调用记录，不延伸为报表平台或运维后台。

当前仓库还没有与该能力对应的顶层 API 契约文档，因此一旦进入实现，必须先通过 `tml-docs-spec-generate` 使用 API 模板生成或更新 `docs/api/api-call-log.yaml`，并确保它只映射到单一 `ApiCallLogController.java`。本 change 依赖调用日志基础表与写侧能力，但不新增新的顶层 SQL 文档。

## Goals / Non-Goals

**Goals:**

- 为开发者控制台提供最小 API 日志查询接口，覆盖日志列表、日志详情与按 API / 时间范围筛选。
- 保持对外业务流程简单，接口只面向当前用户视角，不显式暴露 Consumer 概念。
- 明确 `docs/api/api-call-log.yaml` 是唯一顶层接口契约，并与 `ApiCallLogController.java` 一一映射。
- 复用 Observability 写侧沉淀的数据模型，不额外引入统计表、报表表或复杂查询引擎。

**Non-Goals:**

- 不提供全局监控看板、趋势图、聚合统计、告警规则与告警推送。
- 不提供管理员专用日志后台接口；当前只有 API 市场与开发者控制台两个用户视角。
- 不提供 Consumer 注册、启停、管理等显式业务接口。
- 不在一期引入全文检索、原始报文查看或复杂导出能力。

## Decisions

### 1. 对外只暴露当前用户视角的日志查询，不暴露显式 Consumer 标识

Consumer 在系统内部是调用主体身份模型，但对外始终保持隐式概念。因此查询接口不要求前端传入 `consumerId`，而是由认证上下文在应用层解析当前用户对应的调用主体范围，再执行日志查询。这样可以保持平台使用流程简单，也与之前 Consumer/Auth 领域设计一致。

备选方案是直接在查询接口中显式透出 `consumerId` 过滤，但这会把内部身份概念抬升为前端必须理解的业务对象，不符合既定产品方向，因此不采用。

### 2. 一期接口收敛为单一 `ApiCallLogController`

根据 `config.yaml` 规则，`docs/api/` 必须一份 YAML 对应一个 Controller，因此本 change 只生成一份 `docs/api/api-call-log.yaml`，统一映射到 `ApiCallLogController.java`。列表、详情与筛选都属于同一控制器责任范围，避免为了“接口分类”额外拆出多个 Controller。

备选方案是把列表接口和详情接口拆进不同 API 文档或不同 Controller，但这会让前端联调面与顶层文档都变得分散，且没有明显收益，因此不采用。

### 3. 查询模型优先满足控制台浏览，不提前引入分析型读模型

一期的核心是“能看日志”，因此读侧只需要稳定支持分页列表、基础筛选和详情展示。实现上可以直接基于 `api_call_log` 表建立查询仓储或读模型，不提前拆出专用统计表、物化视图或 ES 检索模型。

备选方案是一次性做面向分析的宽表、聚合表或搜索引擎接入，但这会明显超出一期范围，也会增加部署与维护复杂度，因此不采用。

### 4. 业务接口返回使用 TML-SDK Result，代理调用原始响应不在本 change 范围内

Unified Access 对外转发成功时按上游原样返回，这与平台业务接口不同。开发者控制台日志查询属于平台内部业务接口，因此 Controller 返回必须遵循现有开发规范，统一使用 TML-SDK Result 包装响应。这样可以保持后台业务接口风格一致，也符合既有约束。

备选方案是让日志查询接口也直接返回裸数据结构，但这会与当前后端业务接口规范冲突，因此不采用。

## Risks / Trade-offs

- [Risk] 直接基于 `api_call_log` 表查询，后续随着数据量增长可能面临性能压力。 → Mitigation：一期先控制筛选条件和排序方式，后续再通过独立 change 演进索引或读模型。
- [Risk] 当前用户视角查询如果身份映射不清晰，可能出现越权或漏数。 → Mitigation：查询入口统一依赖认证上下文解析，禁止由前端直接指定调用主体。
- [Risk] 日志详情字段过少可能影响排障。 → Mitigation：一期先围绕控制台必需字段交付，后续再按明确需求追加详情能力。

## Migration Plan

1. 使用 `tml-docs-spec-generate` 的 API 模板生成或更新 `docs/api/api-call-log.yaml`，并映射到 `ApiCallLogController.java`。
2. 基于 `api_call_log` 实现分页查询、详情查询与基础筛选读模型。
3. 在 adapter/api 中实现 `ApiCallLogController` 及对应 `Req / Resp` DTO，统一使用 TML-SDK Result 返回。
4. 增加权限范围、分页过滤、详情查看等测试，确保不显式暴露 Consumer 概念。

## Open Questions

- 日志详情是否在一期展示上游状态码之外的错误码摘要；当前建议保留最小错误摘要字段即可。
- 控制台是否需要 API 名称与 API 编码双筛选；当前建议至少支持 API 标识与时间范围，避免前端联调受阻。
