## 1. Authority Documents

- [x] 1.1 Use `tml-docs-spec-generate` with the SQL template to create or update the Catalog discovery query design file under `docs/sql/`.
- [x] 1.2 Use `tml-docs-spec-generate` with the API template to create or update the Catalog discovery list/detail contract file under `docs/api/`.

## 2. Query Contracts

- [x] 2.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and define list/detail query use cases and result models for enabled asset discovery.
- [x] 2.2 Define query-side ports and mappings so AI-specific detail fields are exposed without leaking write-model internals.

## 3. Query Implementation

- [x] 3.1 Implement query services and infrastructure queries that return only enabled assets with category and asset type summaries.
- [x] 3.2 Implement enabled asset detail retrieval that keeps normal API detail readable and enriches AI assets with provider, model, streaming capability, and capability tags.

## 4. Adapter and Verification

- [x] 4.1 Implement discovery DTOs and adapter endpoints for the list and detail APIs aligned with `docs/api/`.
- [x] 4.2 Add tests for enabled-only filtering, non-enabled detail rejection, optional example snapshot handling, and AI metadata exposure in detail responses.
