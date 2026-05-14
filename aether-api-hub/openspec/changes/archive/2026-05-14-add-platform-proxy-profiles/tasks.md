## 1. Authority Docs First

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` before implementation and confirm DDD/module dependency constraints.
- [x] 1.2 Use `tml-docs-spec-generate` with the SQL template to create `docs/sql/platform_proxy_profile.sql`, mapped one-to-one to table `platform_proxy_profile`.
- [x] 1.3 Use `tml-docs-spec-generate` with the SQL template to update `docs/sql/api-asset.sql` for table `api_asset` with nullable `proxy_profile_id`.
- [x] 1.4 Use `tml-docs-spec-generate` with the API template to create `docs/api/platform-proxy-profile.yaml`, mapped one-to-one to `PlatformProxyProfileController.java`.
- [x] 1.5 Use `tml-docs-spec-generate` with the domain design template to update API Catalog and Unified Access design docs under `docs/design/aehter-api-hub/` with platform administrator proxy-profile governance.

## 2. Proxy Profile Domain And Application

- [x] 2.1 Add platform proxy profile domain model/value objects/status rules for profile identity, protocol, endpoint, credentials, enabled state, soft delete, and version.
- [x] 2.2 Add domain/application rules for create, update, enable, disable, soft delete, credential redaction, and rejecting invalid host/port/protocol inputs.
- [x] 2.3 Add repository ports and application commands/queries/results for proxy profile list/detail/create/update/enable/disable/delete.
- [x] 2.4 Add administrator-capability enforcement for proxy profile use cases without introducing a full RBAC system.

## 3. Persistence And Administrator API Adapter

- [x] 3.1 Add `platform_proxy_profile` persistence entity/mapper/repository adapter aligned 100% with `docs/sql/platform_proxy_profile.sql`.
- [x] 3.2 Extend `api_asset` persistence entity/converter/repository/query records for nullable `proxy_profile_id` aligned 100% with `docs/sql/api-asset.sql`.
- [x] 3.3 Add `PlatformProxyProfileController.java`, web delegate, Req/Resp DTOs, and error mapping aligned 100% with `docs/api/platform-proxy-profile.yaml`.
- [x] 3.4 Ensure administrator-facing responses redact proxy credentials and normal current-user asset APIs do not expose proxy host, port, username, or password.

## 4. Asset Binding And Runtime Snapshot

- [x] 4.1 Implement administrator bind/unbind use cases for assigning one enabled non-deleted proxy profile to one non-deleted API asset.
- [x] 4.2 Reject binding disabled/deleted profiles and preserve existing binding when validation fails.
- [x] 4.3 Extend `TargetApiSnapshotModel` and Unified Access target snapshot mapping with optional proxy profile reference and resolved enabled profile details needed by infrastructure.
- [x] 4.4 Define runtime behavior for assets bound to disabled, deleted, or missing profiles and ensure Unified Access does not silently fall back to direct forwarding in those cases.

## 5. Unified Access Proxy Routing

- [x] 5.1 Add a proxy-aware HTTP client resolver/factory that can return direct or proxied JDK `HttpClient` instances and cache safely by proxy profile identity/version.
- [x] 5.2 Update `JdkUnifiedAccessDownstreamProxyPort` to execute outbound requests with the resolved direct/proxied client while preserving request construction, streaming behavior, and response passthrough.
- [x] 5.3 Classify proxy connection/authentication/DNS/TLS/request execution failures as upstream execution failures and proxy timeouts as upstream timeouts.
- [x] 5.4 Sanitize execution failure diagnostics so proxy credentials, upstream auth values, and caller API keys cannot leak.

## 6. Verification

- [x] 6.1 Add domain/application tests for proxy profile lifecycle, admin-only enforcement, redaction, and invalid input rejection.
- [x] 6.2 Add persistence/API adapter tests for `platform_proxy_profile`, `api_asset.proxy_profile_id`, and `PlatformProxyProfileController` contract behavior.
- [x] 6.3 Add Unified Access tests for direct forwarding without binding, proxied forwarding with binding, disabled/deleted bound profile blocking, proxy timeout, proxy execution failure, streaming preservation, and secret redaction.
- [x] 6.4 Run relevant Maven tests for affected modules and record any environment-related gaps.
- [x] 6.5 Run `openspec status --change add-platform-proxy-profiles` and confirm the change is apply-ready.
