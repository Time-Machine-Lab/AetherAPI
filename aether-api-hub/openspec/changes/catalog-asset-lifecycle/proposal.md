## Why

Catalog 领域设计已经明确 API 资产是整个 Hub 的主数据底座，没有资产注册、配置编辑、启停和 AI 元数据绑定，后续的展示、统一接入和日志都只能依赖硬编码。把资产生命周期单独拆成一份 change，可以把最核心的写模型独立推进，同时避免和分类管理、公共查询接口在同一批文件上频繁冲突。

## What Changes

- 新增 API 资产生命周期能力，覆盖草稿注册、基础信息编辑、上游端点配置、鉴权方案配置、示例快照维护、启用、停用，以及 AI API 的能力档案绑定与校验。
- 为该能力新增独立 spec，并明确第一期仍遵守 Catalog 领域设计中的单主分类、配置驱动接入和 AI-ready 约束。
- 明确后续需要补齐 `docs/api/` 与 `docs/sql/` 下的 Catalog 资产管理权威文档；这些文件属于顶层设计产物，生成时必须使用 `tml-docs-spec-generate` 技能，其中 API 文档使用 API 模板，SQL 文档使用 SQL 模板。
- 约束并发开发边界：该提案只负责资产写模型与管理入口，不包含分类字典管理、市场只读列表/详情页查询，也不包含 Unified Access 转发逻辑。

## Capabilities

### New Capabilities
- `catalog-asset-lifecycle`: 定义 API 资产在 Catalog 领域中的注册、编辑、启停与 AI 能力档案约束。

### Modified Capabilities
- None.

## Impact

- 受影响文档：`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`、后续新增的 `docs/api/` 与 `docs/sql/` Catalog 资产管理文档。
- 受影响代码：`aether-api-hub-standard` 中与资产写模型相关的 `api`、`adapter`、`service`、`domain`、`infrastructure` 模块。
- 潜在边界冲突：该提案依赖分类有效性校验，但应通过分类查询端口或仓储完成；面向市场的资产列表与详情查询需要在独立读模型 change 中实现，避免同一控制器和 DTO 在多任务间反复改动。
