## 1. Discovery Contract

- [x] 1.1 Add `asyncTaskConfig` to `docs/api/api-catalog-discovery.yaml`.

## 2. Backend Projection

- [x] 2.1 Carry async task config through Discovery query record, SQL projection, service model, and response model.
- [x] 2.2 Add focused tests for Discovery async task config mapping.

## 3. Verification

- [x] 3.1 Run targeted backend tests for the affected Discovery projection. Blocked locally because the workspace Java runtime is 11 while the Maven target is Java 17.
