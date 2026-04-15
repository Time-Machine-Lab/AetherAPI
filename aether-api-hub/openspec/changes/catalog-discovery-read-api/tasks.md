## 1. Authority Documents

- [ ] 1.1 Use `tml-docs-spec-generate` with the SQL template to create or update the Catalog discovery query design file under `docs/sql/`.
- [ ] 1.2 Use `tml-docs-spec-generate` with the API template to create or update the Catalog discovery list/detail contract file under `docs/api/`.

## 2. Query Contracts

- [ ] 2.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and define list/detail query use cases and result models for enabled asset discovery.
- [ ] 2.2 Define query-side ports and mappings so AI-specific detail fields are exposed without leaking write-model internals.

## 3. Query Implementation

- [ ] 3.1 Implement query services and infrastructure queries that return only enabled assets with category and asset type summaries.
- [ ] 3.2 Implement enabled asset detail retrieval that keeps normal API detail readable and enriches AI assets with provider, model, streaming capability, and capability tags.

## 4. Adapter and Verification

- [ ] 4.1 Implement discovery DTOs and adapter endpoints for the list and detail APIs aligned with `docs/api/`.
- [ ] 4.2 Add tests for enabled-only filtering, non-enabled detail rejection, optional example snapshot handling, and AI metadata exposure in detail responses.
