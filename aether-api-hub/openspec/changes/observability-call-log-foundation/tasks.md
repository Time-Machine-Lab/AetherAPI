## 1. Authority Documents

- [ ] 1.1 Read `docs/spec/` development rules and confirm the DDD layering, naming, and top-level document-first workflow before implementation.
- [ ] 1.2 Use `tml-docs-spec-generate` with the SQL template to create or update `docs/sql/api_call_log.sql`, and keep the file name exactly aligned to table `api_call_log`.

## 2. Call Log Write Model

- [ ] 2.1 Implement the Observability call log aggregate, command model, and repository port for the phase-one call fact boundary.
- [ ] 2.2 Implement the persistence model and repository adapter aligned exactly with `docs/sql/api_call_log.sql`, including the minimal fact fields and nullable AI extension fields.

## 3. Unified Access Integration

- [ ] 3.1 Integrate Unified Access completion handling with the Observability application service so each completed invocation produces one call log write request.
- [ ] 3.2 Implement result classification and error-summary mapping so successful and failed invocations are recorded with a stable platform log shape.

## 4. Verification

- [ ] 4.1 Add tests for successful invocation logging, failure invocation logging, and empty AI extension field handling.
- [ ] 4.2 Verify that this change does not introduce new business APIs, explicit Consumer management behavior, or raw full request/response payload persistence.
