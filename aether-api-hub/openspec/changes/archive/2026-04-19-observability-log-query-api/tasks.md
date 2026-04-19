## 1. Authority Documents

- [x] 1.1 Read `docs/spec/` development rules and confirm the Result usage boundary, DTO naming rules, and the "one YAML maps to one Controller" rule before implementation.
- [x] 1.2 Use `tml-docs-spec-generate` with the API template to create or update `docs/api/api-call-log.yaml`, and explicitly map it to `ApiCallLogController.java`.

## 2. Query Read Model

- [x] 2.1 Implement the call log query service and read repository for paged log listing, log detail lookup, and minimal API/time-range filtering.
- [x] 2.2 Implement current-user scope resolution so log queries are constrained by the authenticated user context without exposing explicit Consumer identifiers.

## 3. API Adapter

- [x] 3.1 Implement `ApiCallLogController` and its `Req / Resp` DTOs aligned with `docs/api/api-call-log.yaml`, keeping controller logic limited to request parsing and delegation.
- [x] 3.2 Return query results through TML-SDK Result and keep the API boundary focused on developer-console business interfaces only.

## 4. Verification

- [x] 4.1 Add tests for paged log listing, detail lookup, API filtering, time-range filtering, and out-of-scope access rejection.
- [x] 4.2 Verify that this change does not introduce admin-only APIs, explicit Consumer business APIs, or unnecessary analytics/reporting behavior.
