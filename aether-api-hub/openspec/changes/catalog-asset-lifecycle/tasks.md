## 1. Authority Documents

- [ ] 1.1 Use `tml-docs-spec-generate` with the SQL template to create or update the Catalog asset lifecycle design file under `docs/sql/`.
- [ ] 1.2 Use `tml-docs-spec-generate` with the API template to create or update the Catalog asset management contract file under `docs/api/`.

## 2. Domain Model Foundation

- [ ] 2.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and define `ApiAssetAggregate`, its value objects, repository ports, and category validity dependency aligned with the approved docs.
- [ ] 2.2 Implement aggregate rules for draft registration, immutable `API Code`, activation completeness checks, AI profile requirements, and critical configuration revalidation.

## 3. Application and Persistence

- [ ] 3.1 Implement application services for register, revise, enable, disable, and attach AI capability profile use cases.
- [ ] 3.2 Implement MyBatis-Plus persistence objects, repository adapters, and mappings for asset status, upstream configuration, examples, and AI metadata aligned with `docs/sql/`.

## 4. Adapter and Verification

- [ ] 4.1 Implement asset management DTOs and adapter endpoints aligned with `docs/api/`, keeping controller logic limited to request/response conversion.
- [ ] 4.2 Add tests for duplicate `API Code` rejection, draft saves with optional examples, activation validation failures, AI asset profile gating, and revalidation after critical config changes.
