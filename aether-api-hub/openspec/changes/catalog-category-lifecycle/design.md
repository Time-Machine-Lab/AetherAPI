## Context

`Aether API Hub API Catalog领域设计文档` 已经把 `ApiCategoryAggregate` 定义为 Catalog 领域内的轻量聚合根，承担分类命名与启停语义，并明确“被停用分类不得继续被新资产引用”。当前 `aether-api-hub` 仓库还是空骨架，`docs/api/` 和 `docs/sql/` 也只有占位文件，因此这份 change 需要先把分类主数据的边界和权威设计产物固定下来。

这项变更会跨越 `api`、`adapter`、`service`、`domain`、`infrastructure` 多个模块，但仍需遵守后端开发规范里的实用 DDD 分层：业务规则放在 `domain`，流程编排放在 `service`，HTTP 适配和 DTO 转换放在 `adapter`，持久化实现放在 `infrastructure`。

## Goals / Non-Goals

**Goals:**

- 为 API Catalog 建立独立的分类生命周期能力，覆盖创建、重命名、启用、停用和有效性判定。
- 让分类能力可以被后续资产写模型安全复用，而不是让资产模块直接持有分类落库细节。
- 在实现代码前明确需要补齐的 `docs/api/` 与 `docs/sql/` 权威文档，并要求后续使用 `tml-docs-spec-generate` 生成。

**Non-Goals:**

- 不在本 change 内实现 API 资产注册、启停、AI 能力档案绑定。
- 不在本 change 内实现 API 市场列表、详情等只读浏览接口。
- 不扩展为多级分类树、标签治理或搜索体系。

## Decisions

### 1. 使用轻量分类聚合，而不是静态枚举或硬编码字典

选择在 `domain` 中实现 `ApiCategoryAggregate`，只承载 `CategoryCode`、展示名称和启停状态等必要领域语义，并保持 `CategoryCode` 创建后不可变。这样与领域设计文档中的 `rename()`、`enable()`、`disable()` 行为保持一致，也能满足“被停用分类不得继续被新资产引用”的业务前提。

备选方案是先用枚举或配置文件硬编码分类，但这会让后续资产写模型、管理接口和持久化边界被迫返工，因此不采用。

### 2. 通过服务层端口暴露“分类有效性”能力，而不是让下游直接查库

分类管理用例由 `service` 层应用服务编排，并向后续 Catalog 资产能力暴露最小化的查询/校验端口，例如“根据 `CategoryCode` 判断是否存在且可用于新资产引用”。这样可以让资产 change 与本 change 并行推进，同时不破坏 `adapter -> service -> domain` 的依赖方向。

备选方案是在资产模块里直接复用分类 Mapper 或持久化对象，但这违反项目的模块宪法，也会让后续边界更难维护，因此不采用。

### 3. 先补齐顶层 API/SQL 设计文档，再进入代码实现

按照 `openspec/config.yaml` 的规则，这项变更涉及新增持久化和管理接口，就必须把 `docs/sql/` 和 `docs/api/` 视为权威设计产物。执行时应先用 `tml-docs-spec-generate` 生成 Catalog 分类管理相关的 SQL 文档和 API 文档，再据此落地代码与测试。

备选方案是先写代码、再倒推出文档，但这与仓库“`docs/` 是唯一真理”的约束冲突，因此不采用。

### 4. 第一阶段只做单层分类与启停约束，不引入树形层级

架构文档和领域设计文档都没有要求多级分类树或标签治理，本变更保持分类为单层主分类。这样能把 change 控制在最小闭环，并且减少与市场筛选、标签系统和 AI 能力标签的边界冲突。

备选方案是把分类与标签一起统一抽象，但这会扩大 scope，并和 `AI-ready Metadata` 中的能力标签产生概念重叠，因此不采用。

## Risks / Trade-offs

- [Risk] 分类 change 先落地后，短期内缺少资产模块消费方，价值不够直观。 → Mitigation：在设计里预留明确的有效性查询端口，并把该依赖关系写入资产生命周期 change。
- [Risk] 如果过早引入树形层级或复杂治理，会放大第一期实现复杂度。 → Mitigation：严格限制为单层分类与状态管理。
- [Risk] 若 `docs/api/` 与 `docs/sql/` 没先补齐，后续多 AI 并行实现容易各写各的契约。 → Mitigation：把文档生成列为实现任务的最高优先级，并要求使用 `tml-docs-spec-generate`。

## Migration Plan

1. 先生成并评审 Catalog 分类管理的 `docs/api/` 与 `docs/sql/` 顶层文档。
2. 在 `domain`、`service`、`infrastructure` 中落地分类聚合、仓储端口和持久化实现。
3. 在 `adapter` 与 `api` 中补齐管理接口契约和 DTO。
4. 通过应用服务测试与接口测试验证生命周期和有效性约束。

## Open Questions

- 分类创建后的默认状态应为启用还是草稿式禁用状态；领域设计文档尚未明确这一点。
- 第一阶段是否需要内置一批种子分类数据，还是完全由后台管理接口创建。
