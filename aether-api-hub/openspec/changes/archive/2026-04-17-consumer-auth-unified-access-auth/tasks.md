## 1. Preconditions

- [x] 1.1 Read `docs/spec/Aether API HUB 鍚庣浠ｇ爜寮€鍙戣鑼冩枃妗?md` and confirm the DDD layering, naming, and error-handling constraints before implementation.
- [x] 1.2 Reuse and verify the approved Consumer/Auth SQL design from `consumer-auth-key-management`; if validation requires missing persistence fields, update that authority document first instead of creating a parallel storage design.

## 2. Validation Core

- [x] 2.1 Implement the credential validation application service and domain collaboration that resolve key fingerprints, check credential lifecycle state, check Consumer availability, and build a structured `Consumer Context`.
- [x] 2.2 Define the internal validation result model and categorized failure reasons used by unified access, without introducing a new user-facing HTTP validation interface.

## 3. Persistence and Access Integration

- [x] 3.1 Implement repository support for loading credentials by fingerprint, loading the owning Consumer, and updating last-used snapshots in alignment with the approved SQL fields.
- [x] 3.2 Integrate the validation service into the unified access calling path so downstream routing and logging can consume `Consumer Context` and classified failure results.

## 4. Verification

- [x] 4.1 Add tests for successful validation, credential-not-found, disabled, revoked, expired, and Consumer-unavailable scenarios.
- [x] 4.2 Add verification for last-used snapshot updates on matched credentials and confirm that adapters do not carry credential lifecycle rules directly.
