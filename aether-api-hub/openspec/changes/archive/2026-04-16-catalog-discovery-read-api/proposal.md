## Why

架构设计把“API 市场浏览与接入”列为第一阶段核心场景，但当前仓库还没有任何 Catalog 查询能力。把已启用资产的列表和详情查询单独拆成只读提案，可以在不打断写模型收敛的前提下并行推进 API Marketplace 所需能力，并明显减少多人同时修改同一聚合实现的概率。

## What Changes

- 新增 Catalog 只读发现能力，覆盖已启用 API 资产的列表查询、详情查询，以及普通 API 与 AI API 的展示差异化输出。
- 为该能力新增独立 spec，并约束查询范围仅面向已启用资产，不承载分类管理、资产写入、调用鉴权或统一转发。
- 明确后续需要补齐 `docs/api/` 与 `docs/sql/` 下的 Catalog 查询权威文档；这些文件属于顶层设计产物，生成时必须使用 `tml-docs-spec-generate` 技能，其中 API 文档使用 API 模板，SQL 文档使用 SQL 模板。
- 约束并发开发边界：该提案优先采用独立查询模型或查询服务实现，只消费 Catalog 主数据，不改写资产状态流转规则。

## Capabilities

### New Capabilities
- `catalog-discovery-read-api`: 定义面向 API 市场和内部开发者浏览场景的已启用资产列表与详情读取行为。

### Modified Capabilities
- None.

## Impact

- 受影响文档：`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`、后续新增的 `docs/api/` 与 `docs/sql/` Catalog 查询文档。
- 受影响代码：`aether-api-hub-standard` 中面向查询的 `api`、`adapter`、`service`、`infrastructure` 模块，以及必要的只读模型。
- 潜在边界冲突：该提案依赖资产和分类主数据存在，但不应反向承载写模型规则；若第一期需要简化，可优先做最小列表与详情查询，不在此 change 内引入搜索、排序治理或消费方鉴权能力。
