## 1. Authority Documents

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and confirm naming, DTO, Result, DDD boundary rules, and the “one table one SQL / one controller one YAML” document convention before implementation.
- [x] 1.2 Use `tml-docs-spec-generate` with the SQL template to create or update `docs/sql/api_credential.sql`, `docs/sql/consumer_identity.sql`, and `docs/sql/user_consumer_mapping.sql`, ensuring one `.sql` file maps to one table with the same name.
- [ ] 1.3 Use `tml-docs-spec-generate` with the API template to rename or update the credential management contract as `docs/api/api-credential.yaml`, and ensure it maps to a single `ApiCredentialController.java` rather than a demand-level aggregate file name.

## 2. Domain and Application

- [ ] 2.1 Implement `ConsumerAggregate`, `ApiCredentialAggregate`, related value objects, and repository ports aligned with the approved SQL document.
- [ ] 2.2 Implement application commands and queries for current-user key issuance, masked list/detail queries, enable, disable, revoke, and implicit Consumer ensure behavior without exposing explicit Consumer operations.

## 3. Persistence and Adapter

- [ ] 3.1 Implement MyBatis-Plus persistence objects, mapper logic, and repository adapters for `api_credential`, `consumer_identity`, and `user_consumer_mapping` aligned with their table-specific SQL documents.
- [ ] 3.2 Implement `ApiCredentialController` and its `Req / Resp` DTOs aligned with `docs/api/api-credential.yaml`, ensuring controller logic is limited to request/response conversion and TML-SDK `Result` wrapping.

## 4. Verification

- [ ] 4.1 Add tests for first key issuance auto-creating the internal Consumer, one-time plaintext return, masked queries, multi-key ownership, and independent credential state transitions.
- [ ] 4.2 Verify that no explicit Consumer-facing controller, DTO, or business interface was introduced, and document any remaining test gaps if full integration coverage cannot be completed.
