## Why

Current Aether API Hub documents and asset-management code model API assets as platform-maintained catalog entries that are created and enabled by internal operators. That conflicts with the confirmed product direction that everyone can share APIs and everyone can use APIs on the same marketplace, so the asset domain must be realigned before more catalog, discovery, and access behavior is built on the wrong ownership model.

## What Changes

- Revise the top-level product and domain documents so `API Asset` is defined as a user-owned marketplace asset instead of a platform-owned catalog entry, and so the platform is positioned as the manager of marketplace rules and unified access rather than the default publisher.
- Introduce owner-scoped asset lifecycle behavior for the asset write model: authenticated users create their own assets, maintain upstream configuration, publish or unpublish them, soft-delete them, and query their own asset list from the developer console.
- Fold in and supersede the already-developed legacy management list behavior from `add-asset-management-list-api`, so the existing list endpoint, DTOs, query flow, and tests are migrated from platform-management semantics to current-user asset workspace semantics instead of being preserved as legacy behavior.
- **BREAKING** Replace the current exposure semantics centered on `ENABLED` / `DISABLED` as the discovery and invocation gate with marketplace publication semantics centered on user-owned publishability. Discovery and unified access must only expose assets that are published and not deleted.
- **BREAKING** Replace the current global management endpoint family centered on `/api/v1/assets` with owner-scoped asset workspace semantics, so the historical list API and related management flows no longer behave as cross-user management surfaces.
- Update discovery behavior so the marketplace reads only published assets and can show publisher-facing summary fields needed by the product, while still keeping write-model internals and upstream secrets hidden.
- Update the top-level authority documents before implementation: `docs/design/aehter-api-hub/*.md`, `docs/api/api-asset-management.yaml`, `docs/api/api-catalog-discovery.yaml`, and `docs/sql/api-asset.sql`. Per project rules, these authority files must be generated or refreshed with `tml-docs-spec-generate` using the matching API or SQL template.
- Refactor the `ApiAssetController` line and related domain/service/infrastructure code so asset ownership, publish state, current-user scoping, and the already-added management list query path are enforced in the application layer and not implemented ad hoc in controllers or mappers.
- Align boundary modules with the corrected asset semantics: discovery reads marketplace-published assets, unified access resolves only published targets, and observability remains consumer-scoped while continuing to log calls against the corrected asset identity.

## Capabilities

### New Capabilities
- `catalog-owner-asset-management`: Authenticated users manage the lifecycle of their own API marketplace assets, including create, edit, publish, unpublish, delete, and owner-scoped asset listing.

### Modified Capabilities
- `catalog-discovery-read-api`: Discovery requirements change from exposing platform-enabled assets to exposing marketplace-published user assets, and the read model must support publisher-facing discovery fields without leaking write-model internals.

## Impact

- Top-level docs: `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`, `docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`, and `docs/AetherAPI 第一期项目规划书.md`
- API contracts: `docs/api/api-asset-management.yaml` mapped to `ApiAssetController.java`; `docs/api/api-catalog-discovery.yaml` mapped to `CatalogDiscoveryController.java`
- SQL authority file: `docs/sql/api-asset.sql`
- Code paths: asset controller, delegates, API DTOs, domain aggregate/repository, application service/commands/models, persistence entity/converter/repository, discovery read model, unified-access target resolution, and related tests
- Historical code impact: the already-added artifacts from `add-asset-management-list-api` such as `ListApiAssetReq`, `ApiAssetPageResp`, `ApiAssetPageResult`, the `GET /api/v1/assets` list flow, and related tests must be rewritten to match owner-scoped marketplace semantics instead of preserved as-is
- No new infrastructure dependency is expected, but this change alters business semantics shared by catalog, discovery, unified access, and console current-user flows
