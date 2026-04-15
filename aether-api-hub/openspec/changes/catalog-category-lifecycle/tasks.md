## 1. Authority Documents

- [x] 1.1 Use `tml-docs-spec-generate` with the SQL template to create or update the Catalog category lifecycle design file under `docs/sql/`.
- [x] 1.2 Use `tml-docs-spec-generate` with the API template to create or update the Catalog category management contract file under `docs/api/`.

## 2. Domain and Service Contracts

- [x] 2.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and define the category aggregate, immutable `CategoryCode`, and service/domain ports for lifecycle management and validity lookup.
- [x] 2.2 Implement the category application services for create, rename, enable, disable, and downstream validity checks.

## 3. Infrastructure and Adapter Implementation

- [x] 3.1 Implement MyBatis-Plus persistence objects, mapper/repository adapters, and mappings for category lifecycle state aligned with `docs/sql/`.
- [x] 3.2 Implement category management DTOs and adapter endpoints aligned with `docs/api/`, without leaking domain or persistence objects.

## 4. Verification

- [x] 4.1 Add tests for duplicate `CategoryCode` rejection, rename behavior, and enable/disable lifecycle transitions.
- [x] 4.2 Add tests for category validity lookup so disabled or missing categories are rejected for new asset assignment.
