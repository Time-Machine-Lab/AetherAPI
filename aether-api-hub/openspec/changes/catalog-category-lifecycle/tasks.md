## 1. Authority Documents

- [x] 1.1 Use `tml-docs-spec-generate` with the SQL template to create or update the Catalog category lifecycle design file under `docs/sql/`. ✓ 已存在 `docs/sql/api-category-lifecycle.sql`，包含完整的 `api_category` 表结构设计，支持分类生命周期管理。
- [x] 1.2 Use `tml-docs-spec-generate` with the API template to create or update the Catalog category management contract file under `docs/api/`. ✓ 已存在 `docs/api/api-category-lifecycle.yaml`，定义了完整的分类管理 OpenAPI 接口（创建、重命名、启用、停用、分页查询、有效性校验）。

## 2. Domain and Service Contracts

- [x] 2.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and define the category aggregate, immutable `CategoryCode`, and service/domain ports for lifecycle management and validity lookup. ✓ 已创建：ApiCategoryAggregate, CategoryId, CategoryCode, CategoryStatus, CategoryDomainException, ApiCategoryRepository, CategoryUseCase, CategoryRepositoryPort, CategoryApplicationService
- [x] 2.2 Implement the category application services for create, rename, enable, disable, and downstream validity checks. ✓ 已实现完整的分类生命周期管理应用服务

## 3. Infrastructure and Adapter Implementation

- [ ] 3.1 Implement MyBatis-Plus persistence objects, mapper/repository adapters, and mappings for category lifecycle state aligned with `docs/sql/`.
- [ ] 3.2 Implement category management DTOs and adapter endpoints aligned with `docs/api/`, without leaking domain or persistence objects.

## 4. Verification

- [ ] 4.1 Add tests for duplicate `CategoryCode` rejection, rename behavior, and enable/disable lifecycle transitions.
- [ ] 4.2 Add tests for category validity lookup so disabled or missing categories are rejected for new asset assignment.
