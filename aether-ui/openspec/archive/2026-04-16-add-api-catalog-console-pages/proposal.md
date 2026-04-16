## 背景

`Aether API Hub` 的一期文档已经明确了两类前端产品界面：面向开发者的 API 市场浏览界面，以及面向内部维护、运营人员的开发者控制台工作台。但 `aether-ui` 中的 `aether-console` 目前仍然是通用骨架页面，尚未承接 `API Catalog`、`Discovery` 与分类管理接口，导致现有后端契约无法被真实展示和验证。

补齐这一组页面后，可以将文档中已经定义好的“API 管理 + Hub 展示”主链路真正跑通，也能为后续统一接入、凭证说明和调用观测预留稳定的前端落点。

## 变更内容

- 在 `aether-console` 中新增 API 市场浏览能力，基于 Discovery 接口展示已启用 API 资产列表与详情。
- 在 `aether-console` 中将现有控制台导航、首页和工作台替换为 API Catalog 场景对应的信息架构、文案和浏览交互。
- 在 `aether-console` 的受保护工作台中新增 API 资产管理能力，支持按 `apiCode` 查询资产、草稿注册、修订、启用、停用与 AI 能力档案绑定。
- 在 `aether-console` 的受保护工作台中新增分类管理能力，支持分类分页查询、创建、重命名、启用与停用。
- 在 `aether-console` 内新增对应业务域的 API 模块、DTO、类型与页面状态编排，并接入现有统一 `axios` 实例、路由元信息和 i18n 体系。
- 保持现有 `docs/api/*.yaml` 契约不变；本次实现以前端对接既有接口为目标，不引入新的后端接口需求。

## 能力

### 新增能力

- `api-marketplace-browse`：在 `aether-console` 中提供 API 市场列表、详情与 AI 能力信息展示，消费 `api-catalog-discovery.yaml` 定义的只读浏览接口。
- `api-catalog-management`：在 `aether-console` 的受保护工作台中提供 API 资产与分类管理工作流，消费 `api-asset-management.yaml` 与 `api-category-lifecycle.yaml` 定义的管理接口。

### 修改的能力

无。

## 影响范围

- 受影响应用：`aether-console`
- 受影响前端区域：`src/pages`、`src/layouts`、`src/features`、`src/api`、`src/locales`
- 消费的后端契约：`docs/api/api-catalog-discovery.yaml`、`docs/api/api-asset-management.yaml`、`docs/api/api-category-lifecycle.yaml`
- 参考的权威文档：`docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`、`docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`、`docs/spec/AetherAPI 前端技术栈与开发规范文档.md`、目标应用 `DESIGN.md`
- 本提案不需要变更任何后端契约。
