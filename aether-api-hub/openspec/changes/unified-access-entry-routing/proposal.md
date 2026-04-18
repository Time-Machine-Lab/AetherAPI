## Why

`Unified Access` 是 Aether API Hub 一期主链路中真正把“API 资源”和“调用身份”连接成“可执行调用”的中间层。当前仓库已经完成 `API Catalog` 与 `Consumer & Auth` 的领域设计，但仍缺少统一入口、目标 API 匹配、平台前置错误分类与调用入口契约，因此需要先落下统一接入的入口与路由边界。

## What Changes

- 新增统一调用入口能力，覆盖统一访问路径、API 标识解析、目标 API 匹配、前置身份校验接入和平台前置错误分类。
- 新增统一接入入口的业务接口契约，明确统一入口在成功转发前需要消费哪些入参，以及在鉴权失败、目标不存在、目标不可用等前置失败场景下返回什么平台错误。
- 明确需要新增 `docs/api/unified-access.yaml` 作为 `UnifiedAccessController.java` 的单一接口契约文件；该文档后续必须使用 `tml-docs-spec-generate` 的 API 模板生成。
- 本提案不新增独立 SQL 顶层文件，因为统一入口与目标匹配本身不拥有新的主存储表结构，而是消费 `API Catalog` 与 `Consumer & Auth` 已存在的主数据。
- 约束并发边界：本提案只负责入口、目标匹配、调用前置校验和平台错误边界，不负责真实上游转发执行、成功响应原样回传细节和流式透传实现，这些由独立 change 处理。

## Capabilities

### New Capabilities
- `unified-access-entry-routing`: 定义统一调用入口、目标 API 匹配、前置鉴权接入和平台前置错误返回行为。

### Modified Capabilities
- None.

## Impact

- 受影响文档：[Aether API Hub架构设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub架构设计文档.md)、[Aether API Hub Unified Access领域设计文档](D:/Code/Project/Github/AetherAPI/docs/design/aehter-api-hub/Aether API Hub Unified Access领域设计文档.md)、后续新增的 `docs/api/unified-access.yaml`。
- 受影响代码：`aether-api-hub-standard` 中与统一入口、应用服务、目标匹配和错误返回相关的 `api`、`adapter`、`service` 模块。
- 边界冲突提示：本提案依赖 `API Catalog` 的启用资产快照与 `Consumer & Auth` 的凭证校验能力，但不得反向修改这两个领域的主数据结构；同时不得在此提案中引入显式 Consumer 业务接口或额外页面业务。
