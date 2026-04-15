## Context

`Aether API Hub API Catalog领域设计文档` 已经把 `ApiAssetAggregate` 定义为 Catalog 模块的核心聚合根，并给出了注册、编辑、启停、AI 能力档案绑定等业务规则。当前仓库没有任何 Catalog 代码、`docs/api/` 与 `docs/sql/` 也没有正式权威文档，因此这项 change 既要把资产写模型边界固定下来，也要为后续 `Unified Access` 和市场查询提供可靠主数据。

该 change 横跨 `api`、`adapter`、`service`、`domain`、`infrastructure`，并且会依赖分类有效性能力。实现时必须保持“核心规则归于聚合，跨聚合编排归于应用服务，技术细节落在基础设施”的分层约束。

## Goals / Non-Goals

**Goals:**

- 建立 API 资产的写模型闭环，覆盖草稿注册、配置编辑、启停和 AI 能力档案绑定。
- 让启用前完整性校验、AI 类型校验、关键配置变更再校验等规则收敛在资产聚合内部。
- 明确后续需要补齐的 `docs/api/` 与 `docs/sql/` 权威文档，并要求后续使用 `tml-docs-spec-generate` 生成。

**Non-Goals:**

- 不在本 change 内实现分类管理本身，只消费分类有效性能力。
- 不在本 change 内实现 API 市场列表、详情和搜索类查询接口。
- 不实现 Unified Access 的转发、Consumer/Auth 或 Observability。
- 不引入运行时强校验 DSL 或复杂脚本化路由模型。

## Decisions

### 1. 使用 `ApiAssetAggregate` 承载核心不变量，而不是平铺成多个 CRUD 表单

资产注册、启停、AI 元数据挂载和关键配置变更都存在明确的不变量，因此使用 `ApiAssetAggregate` 统一维护 `ApiCode`、资产类型、暴露状态、分类引用、上游配置、示例快照和 AI 能力档案。`ApiCode` 创建后保持稳定，不允许在后续编辑中被改写。

备选方案是把资产拆成多个独立 CRUD 服务分别更新，但那会把“启用前校验”“AI API 必须具备能力档案”等规则散落到多个层次里，因此不采用。

### 2. 将上游配置、示例快照和 AI 档案建模为聚合内部对象

沿用领域设计文档中的划分，在资产聚合内部维护 `UpstreamEndpointConfig`、`ExampleSnapshot`、`CategoryRef` 和 `AiCapabilityProfile`。这样可以把“示例快照仅用于展示”“AI 档案仅对 AI_API 生效”等规则留在领域模型里，而不是变成若干松散 DTO。

备选方案是把 AI 元数据再拆成单独 change 或独立技术表单，但这会让多个 change 高频修改同一批资产文件，和“并行开发尽量不相交”的目标相冲突，因此不采用。

### 3. 启用采用草稿优先和显式再校验策略

新资产默认进入草稿状态；只有在基础名称、分类、请求方式、上游地址、鉴权方案等最小必要信息齐全时才允许启用。对于 `AI_API`，启用前还必须附带 `AiCapabilityProfile`。若已启用资产修改了关键接入配置，则必须重新通过启用前校验后才能继续被视为启用资产。

备选方案是允许“创建即启用”或“关键配置变更后自动保持启用”，但这会削弱领域设计文档中定义的完整性和变更安全约束，因此不采用。

### 4. 通过服务层协调分类有效性、唯一性检查与事务边界

应用服务负责编排资产注册、编辑、启停用例，并通过端口完成 `ApiCode` 唯一性校验、分类有效性校验与资产持久化。这样既符合项目的模块依赖方向，也能让分类生命周期和资产生命周期在不同 change 中并行推进。

备选方案是让 Controller 或 Mapper 直接承担校验，但这违反后端开发规范中的分层禁令，因此不采用。

### 5. 顶层 API/SQL 文档必须先于代码收敛

这项变更涉及新的管理接口和持久化模型，后续执行时必须先使用 `tml-docs-spec-generate` 生成 Catalog 资产管理相关的 `docs/api/` 和 `docs/sql/` 文档，再进入代码实现。这些文档将作为 `api` DTO、错误码和 `infrastructure` 持久化对象的权威依据。

备选方案是让代码先行，但这会与仓库的 Single Source of Truth 规则冲突，因此不采用。

## Risks / Trade-offs

- [Risk] 资产写模型横跨多个模块，稍有不慎就会把业务规则泄漏到 adapter 或 infrastructure。 → Mitigation：在设计与任务里明确聚合、应用服务和持久化职责边界。
- [Risk] AI 元数据与普通资产配置共用同一聚合，初期实现会比纯 CRUD 更复杂。 → Mitigation：限制第一期只支持必要字段和单主分类，不做复杂标签治理。
- [Risk] “关键配置变更后如何保持启用状态”若不先讲清楚，后续实现可能产生分歧。 → Mitigation：在 spec 中仅约束必须重新通过启用前校验，把具体状态迁移策略作为 open question 保留。

## Migration Plan

1. 先生成并评审 Catalog 资产管理的 `docs/api/` 与 `docs/sql/` 顶层文档。
2. 在 `domain` 中落地资产聚合和值对象，在 `service` 中定义应用服务与端口。
3. 在 `infrastructure` 中实现仓储与持久化映射，在 `adapter`/`api` 中补齐管理接口与 DTO。
4. 增加聚合测试、应用服务测试和接口测试，覆盖唯一性、完整性、AI 校验与状态流转。

## Open Questions

- 第一阶段支持的 `AuthScheme` 枚举是否只包含文档举例的 Header Token、Query Token、无鉴权三类，还是还需要额外模式。
- 已启用资产修改关键配置后，是回退到草稿状态，还是进入一种“待重新启用”的中间状态；领域文档只要求重新校验，尚未固定状态模型。
- 示例快照在第一阶段是否只保留单份当前版本，还是需要保留历史版本。
