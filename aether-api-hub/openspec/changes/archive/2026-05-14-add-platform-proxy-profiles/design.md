## Context

Aether API Hub already separates API Catalog, Consumer/Auth, Unified Access, and Observability. API Catalog owns API asset metadata and lifecycle, while Unified Access consumes a target API snapshot and delegates outbound HTTP execution to `JdkUnifiedAccessDownstreamProxyPort`.

The current forwarding path uses one platform-level JDK `HttpClient`. That is sufficient for direct outbound calls, but it does not let platform operations govern which upstream APIs must leave through a controlled HTTP proxy. The existing phase-one product also states that administrators and normal users work in the same console product, but the current backend has only a lightweight console session role string, not a full permission system.

This change introduces platform administrator owned proxy profiles as a lightweight egress governance capability. It intentionally keeps proxy node details and credentials outside the API asset owner workflow.

## Goals / Non-Goals

**Goals:**

- Add a platform proxy profile model maintained through administrator-facing console APIs.
- Store proxy secrets in a dedicated `platform_proxy_profile` table.
- Store only an optional `proxy_profile_id` binding on `api_asset`.
- Keep current-user API asset owner APIs from editing or exposing proxy credentials.
- Resolve proxy binding into Unified Access runtime snapshots and choose a direct or proxied HTTP client at the infrastructure forwarding boundary.
- Preserve existing Unified Access passthrough success semantics and execution-failure classification.
- Update authoritative `docs/` artifacts before implementation.

**Non-Goals:**

- No full RBAC or separate admin-user domain.
- No proxy pool, weighted routing, failover, health checking, retry orchestration, quota, or audit dashboard.
- No user-facing asset-owner proxy editing.
- No consumer-visible proxy behavior.
- No change to upstream authentication schemes beyond carrying proxy routing separately from upstream auth.

## Decisions

### Decision 1: Use a dedicated platform proxy profile table

Create `platform_proxy_profile` as the authoritative storage for platform-managed proxy profiles. A profile should include stable identity, display name, protocol, host, port, enabled status, optional credentials, timestamps, soft-delete marker, and version.

Rationale: proxy host and credentials are platform operations data, not API asset business metadata. Keeping them in a dedicated table avoids leaking secrets through asset owner reads and leaves room for future profile reuse.

Alternative considered: store proxy host/port/credentials directly on `api_asset`. Rejected because it lets asset owner data and platform egress secrets share one lifecycle and makes redaction harder.

### Decision 2: Bind assets by optional `proxy_profile_id`

Extend `api_asset` with nullable `proxy_profile_id`. `NULL` means direct outbound forwarding. A non-null value means Unified Access should use the referenced platform proxy profile if it is active and enabled.

Rationale: this is the lightest version of scheme B: reusable administrator-maintained profiles with per-asset binding, without introducing a policy engine.

Alternative considered: a single global proxy setting. Rejected because upstream APIs often need different egress routes and the user explicitly selected the lightweight profile-binding model.

### Decision 3: Add one administrator-facing controller contract

Add `docs/api/platform-proxy-profile.yaml`, mapped one-to-one to `PlatformProxyProfileController.java`. It should cover profile create/update/list/detail/enable/disable/delete and bind/unbind profile for an API asset.

Rationale: OpenSpec project rules require each API contract file to map to one Controller. Keeping profile management and asset binding together is acceptable because both belong to the same platform proxy profile capability.

Alternative considered: update `api-asset-management.yaml` with proxy binding endpoints. Rejected because current asset management is owner-scoped and must not become the surface for platform-only proxy governance.

### Decision 4: Use minimal administrator role enforcement

Require a valid console session whose role is treated as administrator-capable before calling proxy profile APIs. Because the project does not yet have full RBAC, this change should use a small role check at the application or web-delegate boundary and keep it replaceable.

Rationale: the phase-one docs say the same console product hosts administrator and normal-user work. This change needs enough protection for proxy secrets without forcing a full permission system.

Alternative considered: leave endpoints protected only by login. Rejected because any logged-in asset owner could then alter platform egress routing.

### Decision 5: Keep proxy routing in Unified Access infrastructure

The application service should resolve target asset and proxy binding into a runtime snapshot. The infrastructure forwarding port should use a proxy-aware `HttpClient` resolver or factory to select direct or proxied execution.

Rationale: JDK `HttpClient` proxy selection is client-level configuration. Building proxy handling inside request construction would fight the JDK model and blur infrastructure concerns into application services.

Alternative considered: create one `HttpClient` per request inline. Rejected because it can waste resources and complicate streaming calls. A resolver can cache clients by sanitized proxy profile identity.

### Decision 6: Proxy failures remain execution-stage outcomes

If target resolution succeeds but proxy connection, proxy authentication, DNS, TLS, or timeout fails during outbound execution, Unified Access MUST return the existing upstream execution failure or timeout outcome family.

Rationale: the request has already passed platform pre-forward validation and reached the outbound execution boundary. This aligns with existing HTTPS forwarding stability behavior.

Alternative considered: reject disabled or missing proxy profiles as target-unavailable pre-forward failures. Partially rejected: a missing or disabled bound profile can be treated as target unavailable before execution, but runtime transport failures through an enabled profile belong to execution outcomes.

## Risks / Trade-offs

- [Risk] The current console role model is only configuration-backed and not full RBAC. -> Mitigation: keep administrator checks explicit and isolated so a future permission system can replace them.
- [Risk] Proxy credentials may leak through API responses or error details. -> Mitigation: redact credentials in administrator responses and execution failure payloads, and never expose them through current-user asset APIs.
- [Risk] Caching proxied `HttpClient` instances can retain stale proxy credentials after update. -> Mitigation: key clients by profile id plus version or updated timestamp and evict/replace on profile changes.
- [Risk] Binding an asset to a disabled or deleted profile can make a published asset unusable. -> Mitigation: reject binding to disabled/deleted profiles and define runtime behavior for profiles disabled after binding.
- [Risk] Adding `proxy_profile_id` to `api_asset` changes asset persistence. -> Mitigation: keep the column nullable with direct-forwarding default behavior for all existing rows.

## Migration Plan

1. Generate or update authoritative docs first:
   - `docs/sql/platform_proxy_profile.sql` using the SQL template for table `platform_proxy_profile`.
   - `docs/sql/api-asset.sql` using the SQL template update for `api_asset.proxy_profile_id`.
   - `docs/api/platform-proxy-profile.yaml` using the API template for `PlatformProxyProfileController.java`.
   - API Catalog and Unified Access design docs under `docs/design/aehter-api-hub/`.
2. Add database migration-compatible nullable fields and new table.
3. Implement platform proxy profile domain/application/persistence/adapter slices.
4. Extend API asset persistence and runtime snapshot mapping with proxy profile binding.
5. Add proxy-aware HTTP client resolution in the Unified Access infrastructure forwarding boundary.
6. Add tests for administrator management, binding behavior, redaction, direct fallback, proxied forwarding, and failure classification.
7. Rollback strategy: leave `proxy_profile_id` null for all assets or clear bindings; Unified Access then continues direct forwarding.

## Open Questions

- Which role values should be administrator-capable in the first implementation: only `OWNER`, or both `OWNER` and `ADMIN` if the frontend already uses those labels?
- Should disabling a proxy profile immediately block bound assets with target unavailable, or should binding validation prevent disable while active bindings exist?
- Should profile credentials be encrypted at rest now, or stored as sensitive text with a follow-up encryption change?
