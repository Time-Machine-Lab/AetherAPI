## Why

Unified Access currently forwards outbound requests directly with one platform HTTP client, so the platform cannot govern egress network paths for upstream APIs that require a controlled proxy. Proxy host, credentials, and routing policy are platform operations concerns and should be maintained by platform administrators instead of API asset owners or API consumers.

## What Changes

- Add lightweight platform proxy profiles managed by platform administrators.
- Allow administrators to bind one proxy profile to an API asset while keeping proxy host, port, and credentials hidden from normal asset owners and consumers.
- Extend API Catalog persistence so `api_asset` stores only an optional `proxy_profile_id` reference, not proxy secrets.
- Extend Unified Access target snapshots so the forwarding boundary can resolve whether a call should go direct or through the bound platform proxy profile.
- Keep API asset owners responsible for business upstream settings such as `upstreamUrl`, request method, and upstream auth, but not for platform egress proxy nodes.
- Keep API consumers fully unaware of proxy configuration; Unified Access success and failure response semantics remain unchanged.
- Do not add a full role/permission system, approval workflow, proxy health dashboard, quota, load balancing, or multi-proxy routing policy in this change.

## Capabilities

### New Capabilities

- `platform-proxy-profile-management`: platform administrator management of proxy profiles and API asset proxy-profile binding.
- `unified-access-platform-proxy-routing`: Unified Access uses platform proxy profile binding during upstream forwarding while preserving existing passthrough and failure-classification semantics.

### Modified Capabilities

- None.

## Impact

- Top-level docs:
  - Add `docs/sql/platform_proxy_profile.sql`, mapped one-to-one to table `platform_proxy_profile`.
  - Update `docs/sql/api-asset.sql` to add optional `proxy_profile_id` on table `api_asset`.
  - Add `docs/api/platform-proxy-profile.yaml`, mapped one-to-one to `PlatformProxyProfileController.java`.
  - Update the API Catalog and Unified Access design documents under `docs/design/aehter-api-hub/` to describe administrator-owned egress proxy governance.
  - Any new or updated `docs/api/` and `docs/sql/` files must be generated with `tml-docs-spec-generate`; SQL files use the SQL template and API files use the API template.
- Backend code:
  - Add a small platform proxy profile domain/application/infrastructure/adapter slice.
  - Extend API Catalog persistence, aggregate reconstitution, query records, and runtime snapshots with optional proxy-profile binding.
  - Extend Unified Access forwarding infrastructure to choose a direct or proxied HTTP client from a proxy-aware resolver/factory.
  - Ensure proxy credentials are stored and exposed only through administrator-facing commands/responses with redaction where appropriate.
- API behavior:
  - Platform administrator APIs return TML-SDK `Result` like other console business APIs.
  - Current-user asset owner APIs do not expose proxy credentials and should not allow normal users to edit proxy binding.
  - Unified Access success responses remain upstream passthrough responses and must not be wrapped by TML-SDK `Result`.
  - Proxy connection, authentication, DNS, and timeout failures are execution-stage failures after target resolution, not platform pre-forward rejections.
