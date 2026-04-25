## Context

The current catalog design and implementation assume that API assets are registered and enabled by internal maintainers, then exposed for other developers to browse and call. That assumption appears in the top-level architecture and planning docs, in the catalog domain doc, in `docs/api/api-asset-management.yaml`, in `docs/sql/api-asset.sql`, and in the current `ApiAssetController` write contract.

The historical change `add-asset-management-list-api` has already pushed part of the old model into real code and contract files: a global `GET /api/v1/assets` list endpoint, `ListApiAssetReq`, `ApiAssetPageResp`, `ApiAssetPageResult`, and tests around `DRAFT` / `ENABLED` / `DISABLED` management filtering. These artifacts must be migrated as part of this marketplace realignment rather than treated as neutral baseline behavior.

The confirmed product direction is different: every authenticated user can publish and share API assets, and every user can also consume API assets through the same marketplace. The platform's role is to normalize asset management, discovery, and unified access, not to act as the default publisher. This change therefore touches both top-level language and concrete write-model behavior.

Constraints and existing anchors:

- `docs/` remains the single source of truth. Any API or schema change must first land in `docs/design/`, `docs/api/`, and `docs/sql/`.
- `docs/api/api-asset-management.yaml` must continue to map one-to-one to `ApiAssetController.java`.
- `docs/api/api-catalog-discovery.yaml` must continue to map one-to-one to `CatalogDiscoveryController.java`.
- `docs/sql/api-asset.sql` remains the only authority file for the `api_asset` table.
- Console business APIs already have current-user token resolution through `console-session-auth`; asset management should reuse that chain instead of inventing a second console identity flow.
- Unified Access still authenticates consumer calls with API Key; successful upstream passthrough does not use TML Result wrapping.

Stakeholders:

- Asset publishers who create and maintain their own assets
- Asset consumers who browse and call published assets
- Console frontend that needs owner-scoped asset management APIs
- Unified access and discovery modules that depend on consistent asset publication semantics

## Goals / Non-Goals

**Goals:**

- Reframe the asset domain as a user-owned marketplace asset domain across top-level documentation and OpenSpec requirements.
- Introduce owner-scoped asset write behavior for create, edit, publish, unpublish, delete, and owner-scoped listing.
- Migrate the already-developed historical asset list code path into that owner-scoped workspace model instead of leaving a legacy global management surface in place.
- Make discovery and unified access depend on marketplace publication state rather than the old platform enablement wording.
- Preserve DDD boundaries by keeping ownership and publication rules in domain/application services, not in controllers or mappers.
- Update authority docs first, then align code and tests.

**Non-Goals:**

- Introducing explicit Consumer registration or management to end users
- Building subscription commerce, billing, settlement, review, moderation, or provider revenue-sharing
- Replacing API Key auth for unified access with console bearer tokens
- Adding owner-side analytics or publisher console reports to Observability in this change
- Building a separate provider portal or management backend

## Decisions

### 1. Model assets as user-owned marketplace resources

The write model will move from "platform-maintained asset" to "current-user-owned asset". `api_asset` must carry ownership fields needed to enforce lifecycle rules without depending on an unstated external user directory.

Chosen approach:

- Add ownership fields to `api_asset`, at minimum `owner_user_id`
- Add a publisher display snapshot field if discovery needs publisher-facing display data without a separate profile lookup
- Keep `api_code` globally unique across the marketplace

Why this over a separate ownership table:

- The current model has a single `api_asset` authority file and no existing publisher table
- Ownership is intrinsic to the aggregate lifecycle, not an optional relation
- It keeps the phase-one change smaller and reduces cross-module joins

### 2. Replace platform enablement semantics with publication semantics

The current `ENABLED` / `DISABLED` wording is tied to a platform-operator mental model. The marketplace model needs publication language.

Chosen approach:

- Replace asset exposure semantics with `DRAFT`, `PUBLISHED`, and `UNPUBLISHED`
- Continue using `is_deleted` for soft deletion instead of introducing a second delete-like status
- Discovery and unified access treat only `PUBLISHED` and non-deleted assets as market-visible or callable
- If a published asset's critical upstream configuration changes, the asset falls back to `UNPUBLISHED` and requires republish validation

Why this over keeping `ENABLED` and reinterpreting it:

- Reusing `ENABLED` keeps the old platform-operator mental model alive
- Publication language better matches user-facing sharing behavior
- It makes top-level docs, UI copy, and future marketplace capabilities more coherent

### 3. Move owner write APIs under current-user scope while keeping one controller contract

Current asset write APIs are global `/api/v1/assets` operations with no explicit owner scope. That does not align with how other console business APIs are scoped.

Chosen approach:

- Rework `ApiAssetController` into a current-user-scoped contract, for example under `/api/v1/current-user/assets`
- Keep all asset write behavior in the same controller and same authority file `docs/api/api-asset-management.yaml`
- Replace the historical global list behavior from `add-asset-management-list-api` with owner-scoped list and detail queries to support the developer console's "my assets" workflow
- Use the existing console session token resolution chain to derive current user identity

Why this over keeping global endpoints:

- Current-user scoping matches existing API key and log query console APIs
- It avoids a hidden admin-style global write contract
- It makes ownership enforcement visible in the API contract and easier for frontend integration

### 4. Treat the historical management list implementation as migration scope

The old list capability is already merged into authority docs and backend code. Leaving it untouched would keep a platform-management surface alive even after the top-level product semantics are corrected.

Chosen approach:

- Refactor the existing list-query DTOs and controller wiring instead of creating a second parallel asset list flow
- Rename or reshape request/response objects where needed so their semantics become owner-scoped workspace models
- Update tests that currently assert `DRAFT` / `ENABLED` / `DISABLED` global management behavior so they instead assert owner isolation plus `DRAFT` / `PUBLISHED` / `UNPUBLISHED` semantics

Why this over temporarily keeping the old list API:

- A temporary global list API would immediately violate the new product model
- It would let frontend or other AI workers keep integrating against the wrong boundary
- The old query code already sits in the same controller family, so coexistence would increase confusion more than it would reduce migration risk

### 5. Keep discovery public/read-only but enrich it with marketplace publisher context

Discovery is a marketplace browse surface, not a write console. It should remain read-only and hide upstream secrets, but it now needs marketplace semantics.

Chosen approach:

- `CatalogDiscoveryController` continues to expose only browse-safe data
- Discovery list/detail return only published assets
- Discovery can expose minimal publisher summary such as publisher display name snapshot
- Upstream URL, auth config, and write-model-only data stay hidden

Why this over merging discovery with owner console reads:

- Marketplace browsing and owner workspace management are different bounded use cases
- Read-only public discovery should stay stable for future UI and public-market use
- It preserves the current split between `ApiAssetController` and `CatalogDiscoveryController`

### 6. Boundary modules consume corrected asset semantics, but do not absorb asset ownership rules

This change touches multiple modules, but ownership rules still belong to catalog.

Chosen approach:

- Unified Access changes only its target-eligibility rule from "enabled asset" to "published asset"
- Observability continues to log against the corrected asset identity but keeps its current consumer-scoped query model
- Console auth remains unchanged and only provides current-user identity to asset APIs

Why this over pushing owner rules into access or logs:

- It keeps bounded contexts clear
- It avoids duplicating ownership logic outside catalog
- It limits the scope of change in auth and observability

### 7. Update authority docs before code and generate them with the required skill

Per project rules, doc authority changes must lead implementation.

Chosen approach:

- First update:
  - `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`
  - `docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md`
  - `docs/AetherAPI 第一期项目规划书.md`
  - `docs/api/api-asset-management.yaml`
  - `docs/api/api-catalog-discovery.yaml`
  - `docs/sql/api-asset.sql`
- Generate or refresh `docs/api/*.yaml` and `docs/sql/*.sql` with `tml-docs-spec-generate`
- Only after those docs are aligned, update DTOs, controllers, domain models, repositories, and tests

Why this over code-first refactoring:

- The current project explicitly treats docs as the authority layer
- This change is as much a semantics correction as an implementation change
- Frontend and other AI workers need the corrected authority docs for coordinated implementation

## Risks / Trade-offs

- [Legacy asset rows have no owner] -> Add a one-time migration/backfill step before enforcing non-null ownership in environments with preexisting data; in low-volume dev environments, backfill can use the bootstrap console user.
- [Endpoint semantics become breaking for existing clients] -> Mark the change as breaking in proposal/specs, update frontend integration against the new authority docs first, and keep the controller mapping stable to one file.
- [Historical list API is already merged and may be used by frontend or tests] -> Explicitly migrate the existing list API code path, DTOs, and regression tests inside this change instead of treating them as untouched baseline behavior.
- [Publisher display data may drift if user profile changes] -> Treat publisher display fields as snapshots for marketplace display and refresh them during owner edits/publish where appropriate.
- [Published asset edits may surprise users when assets become unpublished] -> Document the republish rule clearly in API contract and console UX copy; keep the fallback limited to critical configuration changes.
- [Boundary modules may continue using old `enabled` wording] -> Include explicit boundary tasks for discovery, unified access, tests, and error copy review.

## Migration Plan

1. Rewrite the top-level docs to adopt the user-owned marketplace language.
2. Update `docs/sql/api-asset.sql` and the asset/discovery API contracts with `tml-docs-spec-generate`.
3. Refactor the already-added list contract and code path from `add-asset-management-list-api` so it becomes the owner workspace list instead of a global management list.
4. Refactor catalog domain/application/infrastructure code to add owner scoping and publication semantics.
5. Update discovery and unified-access read behavior to consume `PUBLISHED` assets only.
6. Backfill legacy asset ownership if any existing rows are present.
7. Run regression tests for asset management, discovery, unified access, and current-user console flows.

Rollback strategy:

- Revert the doc authority files and code changes together as one change set
- If schema migration has already run, map `PUBLISHED` back to `ENABLED` and `UNPUBLISHED` back to `DISABLED` only in controlled rollback scenarios

## Open Questions

- No blocking open questions for proposal stage. If legacy seeded assets already exist outside local development, the exact owner backfill source should be confirmed before apply.
