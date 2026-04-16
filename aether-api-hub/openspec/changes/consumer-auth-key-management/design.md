## Context

`Consumer & Auth` 领域设计文档已经明确了两条关键约束：

- `Consumer` 是长期内部化的调用身份，不作为用户显式操作对象暴露。
- 当前用户真正感知和操作的是 `API Key`，并且一个用户默认对应一个内部 `Consumer`，一个 `Consumer` 可持有多个 `API Key`。

当前仓库在 `docs/api/` 与 `docs/sql/` 中只有 Catalog 领域的权威文档，尚未定义 Consumer/Auth 的接口契约与存储设计；同时前端需要联调开发者控制台中的凭证管理能力。因此，这个 change 既要补齐顶层 API/SQL 文档，也要给 DDD 分层下的凭证管理代码提供稳定实现边界。

## Goals / Non-Goals

**Goals:**

- 建立“当前用户 -> 隐式 Consumer -> 多个 API Key”的稳定写模型。
- 提供当前用户可直接使用的 API Key 生命周期能力，包括创建、掩码查询、详情查看、停用、启用、吊销与过期信息展示。
- 保证 API Key 明文只在签发时返回一次，系统内部只保存安全指纹、掩码与必要管理信息。
- 明确并先行生成 `docs/sql/consumer-api-key-management.sql` 与 `docs/api/consumer-api-key-management.yaml`；执行时必须使用 `tml-docs-spec-generate`，SQL 文档使用 SQL 模板，API 文档使用 API 模板。
- 保持 DDD 边界清晰：聚合负责凭证规则，应用服务负责用例编排，adapter 只做请求响应转换。

**Non-Goals:**

- 不新增任何显式 `Consumer` 页面、Controller、DTO 或用户先注册 Consumer 的流程。
- 不实现统一接入的凭证校验与调用上下文透传，这部分由 `consumer-auth-unified-access-auth` change 负责。
- 不实现调用日志查询、限额、计费、RBAC 或完整账号体系。
- 不引入新的外部基础设施依赖，继续沿用 Java + Spring Boot + Maven + MyBatis-Plus + MySQL。

## Decisions

### 1. 对外只暴露“当前用户 API Key 管理”，不暴露显式 Consumer 操作

控制台接口全部围绕当前登录用户展开，接口语义只包含“创建我的 API Key”“查看我的 API Key”“停用/启用/吊销我的 API Key”。用户请求中不出现 `consumerId`、`consumerCode` 一类字段，`Consumer` 由系统在应用服务内部自动确保存在并完成绑定。

备选方案是显式提供 `RegisterConsumer`、`ListConsumers`、`IssueKeyForConsumer` 等接口，但这会直接违背领域文档中“Consumer 长期内部化”的原则，并增加前端和用户的理解负担，因此不采用。

### 2. 凭证生命周期由独立 `ApiCredentialAggregate` 负责，Consumer 只承担归属与可用性边界

`ApiCredentialAggregate` 负责明文签发、指纹保存、状态流转、过期策略和最近使用快照；`ConsumerAggregate` 只负责调用身份存在性、归属与整体可用性。应用服务在签发凭证时先确保用户对应 Consumer 存在，再由凭证聚合完成签发。

备选方案是把所有 Key 都挂到 `ConsumerAggregate` 中统一管理，但这会让单个 Consumer 聚合承载高频状态变化和多 Key 集合，不利于后续多 Key 并发更新，也不利于将来扩展轮换、用途标记和细粒度治理。

### 3. 顶层 SQL/API 文档必须先于代码实现

由于本提案同时引入新的存储结构和新的前端联调接口，`docs/sql/consumer-api-key-management.sql` 与 `docs/api/consumer-api-key-management.yaml` 必须先作为权威设计产物生成并评审，通过后再开始编码。任务执行时必须调用 `tml-docs-spec-generate` 技能生成这两份文档。

备选方案是先写代码再反推文档，但这与 `config.yaml` 中“docs/ 为唯一真理源”的规则冲突，也会让前后端联调缺少稳定契约，因此不采用。

### 4. 当前用户接口采用稳定的 Command / Query / Req / Resp 边界

对外接口采用 `Req / Resp` 命名，对内应用层采用 `Command / Query / Result / Model` 命名，符合现有开发规范。控制器只负责身份获取、参数转换和 `TML-SDK Result` 包装，不承载凭证签发、状态判断和安全规则。

备选方案是直接在 Controller 中组合用户信息、生成 Key 并调用 Mapper 落库，但这会违反项目对 DDD 分层和控制器职责的约束，因此不采用。

### 5. 多 Key 允许存在，但一期不额外引入“应用”“项目”显式模型

一期默认支持“一个用户多个 API Key”，为多环境、轮换和不同用途预留空间；但这些 Key 仍然统一归属于同一个隐式 Consumer。接口层可支持 Key 名称、用途说明、可选过期时间这类轻量管理信息，不引入新的显式应用主体模型。

备选方案一是限制一个用户只能有一个 Key，但这会过早堵死轮换和分环境使用场景；备选方案二是同时引入“应用”或“项目”模型，但这会让当前领域和页面复杂度明显上升，因此都不采用。

## Risks / Trade-offs

- [Risk] 允许一个用户持有多个 Key 后，前端会希望追加更多用途字段。→ Mitigation：一期只保留最小管理信息，超出当前文档约束的字段通过后续 change 再扩展。
- [Risk] 如果 SQL 文档没有先定义好状态字段、指纹字段和最近使用字段，后续实现会反复返工。→ Mitigation：把 `docs/sql/` 与 `docs/api/` 生成列为最高优先级任务，且在代码任务前完成。
- [Risk] 把 Consumer 隐藏后，研发实现时可能偷懒直接把 Key 绑到用户表。→ Mitigation：在设计与 specs 中明确要求保留独立 Consumer 内部模型与映射关系。

## Migration Plan

1. 使用 `tml-docs-spec-generate` 生成并评审 `docs/sql/consumer-api-key-management.sql`。
2. 使用 `tml-docs-spec-generate` 生成并评审 `docs/api/consumer-api-key-management.yaml`。
3. 在 `domain` 中落地 `ConsumerAggregate`、`ApiCredentialAggregate`、值对象与仓储端口。
4. 在 `service` 中实现当前用户凭证管理用例与隐式 Consumer 自动确保逻辑。
5. 在 `infrastructure` 中完成 MyBatis-Plus 持久化对象、Mapper 与仓储适配。
6. 在 `adapter/api` 中补齐控制台联调接口、`Req/Resp` DTO 与 `TML-SDK Result` 返回。
7. 增加聚合测试、应用服务测试和接口测试，覆盖一次性明文展示、掩码查询、状态流转和多 Key 场景。

## Open Questions

- 一期是否需要限制“单个用户最多可持有的启用中 API Key 数量”；当前设计默认不限制，但实现时可通过配置项预留。
- 一期是否允许用户在创建 Key 时不传过期时间；当前设计默认允许，并由领域层视为“永不过期”策略。
