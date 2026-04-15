## Why

`API Catalog` 的领域设计已经把分类定义为资产引用合法性的前置条件，但当前仓库仍是空骨架，既没有分类生命周期实现，也没有对应的 `docs/api` 和 `docs/sql` 权威设计产物。先单独落地分类聚合，可以为后续资产写模型提供稳定边界，并把多 AI 并行开发时最容易互相踩到的基础字典逻辑提前拆开。

## What Changes

- 新增 API Catalog 分类生命周期能力，覆盖分类创建、重命名、启用、停用与有效性约束。
- 为该能力新增独立 spec，并把分类约束固定为 Catalog 领域内部规则，不延伸到资产注册、市场查询或统一调用链路。
- 明确后续需要补齐 `docs/api/` 与 `docs/sql/` 下的 Catalog 分类权威文档；这些文件属于顶层设计产物，生成时必须使用 `tml-docs-spec-generate` 技能，其中 API 文档使用 API 模板，SQL 文档使用 SQL 模板。
- 约束并发开发边界：该提案只负责分类聚合及其管理入口，不包含 API 资产写模型和面向市场的只读查询接口。

## Capabilities

### New Capabilities
- `catalog-category-lifecycle`: 定义 API 分类在 Catalog 领域中的创建、维护、状态流转与引用有效性规则。

### Modified Capabilities
- None.

## Impact

- 受影响文档：`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`、后续新增的 `docs/api/` 与 `docs/sql/` Catalog 分类文档。
- 受影响代码：`aether-api-hub-standard` 中与分类相关的 `api`、`adapter`、`service`、`domain`、`infrastructure` 模块。
- 潜在边界冲突：分类停用会影响资产注册合法性，但资产生命周期实现应通过端口依赖消费分类有效性，而不是在本提案中直接扩展到资产聚合实现。
