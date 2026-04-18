## 1. Authority Documents

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and confirm DDD layering, naming, `TML-SDK Result` usage boundaries, and the "one YAML maps to one Controller" rule before implementation.
- [x] 1.2 Use `tml-docs-spec-generate` with the API template to create or update `docs/api/unified-access.yaml`, and explicitly map it to `UnifiedAccessController.java`.

## 2. Entry and Routing Core

- [x] 2.1 Implement the unified access entry application service that accepts the invocation request, resolves `Consumer Context` through `Consumer & Auth`, and resolves the target API snapshot through `API Catalog`.
- [x] 2.2 Implement the platform pre-forward failure model and categorized error handling for invalid credentials, unknown API identifiers, and unavailable target APIs.

## 3. Adapter Integration

- [x] 3.1 Implement `UnifiedAccessController` and its request/response DTO boundary aligned with `docs/api/unified-access.yaml`, keeping controller logic limited to request parsing and delegation.
- [x] 3.2 Integrate the entry layer with the downstream proxy execution boundary so only successfully resolved invocations proceed to upstream forwarding.

## 4. Verification

- [x] 4.1 Add tests for target API resolution, credential validation handoff, unknown API identifier rejection, and unavailable target rejection before forwarding.
- [x] 4.2 Verify that this change does not introduce explicit Consumer business interfaces, duplicate upstream execution logic, or extra API contract files beyond `docs/api/unified-access.yaml`.
