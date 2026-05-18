## 1. Authority Docs First

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` before implementation and confirm DDD/module dependency constraints.
- [x] 1.2 Use `tml-docs-spec-generate` with the SQL template to update the existing authority file `docs/sql/api-asset.sql` for table `api_asset` with nullable storage for `capabilityExtensions`, `policyExtensions`, and `metadataExtensions`.
- [x] 1.3 Use `tml-docs-spec-generate` with the API template to update `docs/api/api-asset-management.yaml`, mapped one-to-one to `ApiAssetController.java`, so owner-scoped asset create/revise/detail contracts accept and return nullable extension blocks.
- [x] 1.4 Update the API Catalog design authority under `docs/design/aehter-api-hub/` to state that extension blocks are additive future-feature entry points and that existing first-class fields remain authoritative.

## 2. Owner Asset Management Extension Blocks

- [x] 2.1 Add owner-scoped request/response DTO support for nullable JSON object fields `capabilityExtensions`, `policyExtensions`, and `metadataExtensions` without removing or renaming any existing asset fields.
- [x] 2.2 Extend `ApiAssetWebDelegate` and asset service commands/models so the extension blocks round-trip through create, revise, and detail flows while preserving null-clear semantics.
- [x] 2.3 Extend `ApiAssetAggregate` so it stores the extension blocks additively and does not change current publish-readiness rules or existing first-class field behavior.

## 3. Persistence And Mapping

- [x] 3.1 Extend `api_asset` persistence with three nullable text columns for the extension blocks while keeping the existing columns unchanged.
- [x] 3.2 Extend persistence entities, converters, repositories, and owner detail mapping so unknown nested keys are preserved through save/load round-trips.
- [x] 3.3 Keep list-query projections unchanged unless a block field is explicitly required for list output; generic extension blocks should not be added to owner list or Discovery list summaries in this change.

## 4. Boundary Protections

- [x] 4.1 Confirm Discovery detail and Discovery list do not expose the generic extension blocks in this change.
- [x] 4.2 Confirm Unified Access and publish validation do not consume extension blocks in this change.
- [x] 4.3 Confirm no existing first-class field is migrated into the new blocks in this change, including AI profile, async task config, examples, and JSON Schema snapshots.

## 5. Verification

- [x] 5.1 Add focused API adapter tests for create/revise/detail flows that set, update, and clear the three extension blocks.
- [x] 5.2 Add aggregate/service tests proving extension block updates do not alter publish readiness or existing field behavior.
- [x] 5.3 Add persistence conversion tests proving the three blocks survive round-trips with nested JSON content and null values.
- [x] 5.4 Add regression coverage proving Discovery and Unified Access responses remain unchanged by generic extension block data.
- [x] 5.5 Run relevant Maven tests for affected modules and record any environment-related gaps.
- [x] 5.6 Run `openspec status --change add-api-asset-extension-blocks` and confirm the change is apply-ready.