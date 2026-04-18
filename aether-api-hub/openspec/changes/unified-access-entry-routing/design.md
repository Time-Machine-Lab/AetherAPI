## Context

`Unified Access` 领域文档已经明确，统一接入层在一期要解决的是“请求如何进入平台、如何完成前置身份校验、如何匹配目标 API、如何分类平台前置失败”，而不是一次性做成全功能网关。当前仓库已经有：

- `API Catalog` 相关主数据与查询文档
- `Consumer & Auth` 相关隐式 Consumer 与 API Key 能力

但仍缺少统一入口这一层的正式契约和实现边界。尤其考虑到前端或联调方需要知道“统一调用入口长什么样、平台前置失败怎么返回、成功场景与失败场景边界如何划分”，这一部分必须先固定下来。

## Goals / Non-Goals

**Goals:**

- 定义单一 `UnifiedAccessController` 对应的统一调用入口契约。
- 明确统一调用入口如何解析 API 标识、如何接入 `Consumer & Auth` 校验、如何解析目标 API 快照。
- 明确平台前置失败的错误分类与返回边界，例如鉴权失败、目标不存在、目标不可用。
- 先生成 `docs/api/unified-access.yaml` 顶层接口契约文档，并要求后续使用 `tml-docs-spec-generate` 的 API 模板生成。
- 保持入口能力和转发执行能力分离，让后续 `unified-access-upstream-proxy` change 可以并行推进而不争抢同一份 API 契约文件。

**Non-Goals:**

- 不实现真实上游 HTTP 调用执行、成功响应原样回传和流式透传细节。
- 不新增任何显式 Consumer 相关接口、DTO 或页面。
- 不新增独立 SQL 顶层文件，因为当前入口与目标匹配不引入新的主存储表。
- 不实现日志查询、限流、重试、熔断、智能路由等重网关能力。

## Decisions

### 1. 统一入口使用单一 `UnifiedAccessController` 契约承载

根据 `config.yaml` 的规则，`docs/api/` 必须遵守“一份 YAML 对应一个 Controller 文件”。因此本提案只生成一份 `docs/api/unified-access.yaml`，并显式映射到单一 `UnifiedAccessController.java`。该契约只描述统一入口请求、平台前置失败返回和成功返回的边界，而不拆成多个 demand-level API 文件。

备选方案是按“目标匹配”“调用入口”“前置校验”再拆多份 YAML，但这些最终都映射到同一个入口控制器，会违反当前文档命名与映射规则，因此不采用。

### 2. 入口层负责“接收、校验前置条件、解析目标”，不负责真实转发执行

统一入口应用服务负责：

- 接收请求
- 提取 API Key
- 调用 `Consumer & Auth` 解析 `Consumer Context`
- 调用 `API Catalog` 解析目标 API 快照
- 对平台前置失败进行统一分类

真实的上游请求组装和发送，由 `unified-access-upstream-proxy` 这份 change 承担。这样入口层可以更纯粹地收敛领域规则和错误边界，也能让两个 change 的代码与任务不重叠。

备选方案是把入口匹配和转发执行一次性放进同一个 change，但那会让同一份接口契约、应用服务和调用执行代码全部耦合在一起，降低多 AI 并行的可操作性，因此不采用。

### 3. 平台前置失败与上游业务成功必须明确分层

本提案只定义平台前置失败：

- 凭证无效
- 凭证不可用
- 目标 API 不存在
- 目标 API 不可调用

这些失败由平台统一返回；而上游成功响应如何原样返回，由下一份转发提案继续约束。这样能避免在当前阶段把“平台错误响应规范”和“上游业务成功响应规范”混到一份设计里。

备选方案是当前 design 就把成功返回细节一起写死，但这会与下一份提案产生明显交叉，因此不采用。

### 4. 统一入口默认按 `{baseUrl}/{apiCode}` 形式建模，但保留 API 顶层文档最终钉死权

领域文档已经使用 `{aetherApiBaseUrl}/{apiCode}` 作为说明性示例，本提案沿用这一表达来定义统一入口。但最终是否还需要前缀段、版本段或其他固定路径，仍应由 `docs/api/unified-access.yaml` 作为权威契约钉死。

备选方案是此刻直接在 design 中写死完整 URL 细节，但那会让领域设计与 API 契约层界限变模糊，因此不采用。

## Risks / Trade-offs

- [Risk] 如果统一入口路径规则迟迟不定，前端联调和后续转发执行都会反复调整。→ Mitigation：把 `docs/api/unified-access.yaml` 放到首要任务，尽快冻结入口契约。
- [Risk] 入口层与转发层拆开后，需要额外维护一个内部调用边界。→ Mitigation：通过清晰的应用服务接口和目标快照模型来约束职责，不让两边直接互相侵入。
- [Risk] 平台前置失败和上游失败容易在实现中混淆。→ Mitigation：在 specs 里明确只把“转发前失败”放到本提案，真正上游失败留给下一份提案。

## Migration Plan

1. 使用 `tml-docs-spec-generate` 生成并评审 `docs/api/unified-access.yaml`，映射到 `UnifiedAccessController.java`。
2. 在 `service` 中实现统一入口应用服务，接入 `Consumer & Auth` 和 `API Catalog` 的调用边界。
3. 在 `adapter/api` 中实现统一入口 Controller 和请求解析逻辑。
4. 定义目标 API 快照与平台前置失败结果模型，为下一份转发提案提供稳定输入。
5. 增加测试，覆盖鉴权失败、目标不存在、目标不可用和成功进入转发前阶段。

## Open Questions

- 最终统一入口路径是否需要固定前缀，如 `/access/{apiCode}`，还是直接使用 `/{apiCode}`；当前 design 保留给 API 文档最终确定。
- 平台前置失败是否统一为同一种错误响应结构，还是按现有错误码体系分层返回；当前建议按现有错误码体系对齐，但不在 design 中写死字段细节。
