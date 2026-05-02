## Context

`ApiAssetController` currently exposes register, detail, revise, enable, disable, and AI-profile binding operations, while management-side browsing is absent. The existing authority contract in `docs/api/api-asset-management.yaml` also stops at single-asset operations, and the console therefore falls back to "query by apiCode" instead of rendering a real asset list.

The implementation needs to stay within the current Aether API HUB DDD layering: controller/delegate at the adapter edge, application service orchestration in service, and MyBatis-backed query logic in infrastructure. Because this is an API contract change, `docs/api/api-asset-management.yaml` must be updated first through `tml-docs-spec-generate`; no `docs/sql/` change is planned because the query reads from the existing `api_asset` table.

## Goals / Non-Goals

**Goals:**
- Add a management-side `GET /assets` capability under the existing `ApiAssetController` contract.
- Support paged browsing of draft, enabled, and disabled assets with lightweight management filters.
- Reuse existing asset data and category join information so the frontend can render a useful management list without extra per-row fetches.
- Keep discovery-market behavior and write-model lifecycle rules unchanged.

**Non-Goals:**
- No new database tables, snapshots, or dedicated search engine.
- No redesign of discovery APIs or public market exposure rules.
- No bulk operations, sorting matrix, or advanced faceted search in this change.
- No mutation of asset lifecycle semantics beyond what existing write APIs already enforce.

## Decisions

### 1. Add the list endpoint to the existing asset-management authority contract
The new list operation will extend `docs/api/api-asset-management.yaml` and remain owned by `ApiAssetController`, instead of creating a new aggregate API document or a parallel controller.

Alternative considered: define a separate management query controller and a new API document.
Why not: the current controller already owns the management lifecycle contract, and the config rules explicitly prefer one YAML per controller mapping.

### 2. Keep query storage on the existing `api_asset` table
The list query will read from `api_asset`, optionally joining `api_category` for category display data. No `docs/sql/api-asset.sql` change is planned unless implementation reveals an unavoidable missing index.

Alternative considered: add a dedicated read model.
Why not: the management list is still simple enough for MyBatis query logic over the existing table, and introducing another store would add needless migration and consistency cost.

### 3. Use a dedicated query path instead of overloading single-asset repository methods
The endpoint should use an infrastructure query mapper or query port shaped for paging/filtering rather than forcing repository-style aggregate loading for every row.

Alternative considered: load all assets through repository methods and page in memory.
Why not: that would couple the management list to write aggregates, degrade query efficiency, and blur the read/query boundary.

### 4. Keep filters minimal and management-oriented
The list contract should cover page, size, status, category code, and keyword, which is enough to unblock the console asset workspace without prematurely locking in a richer search product surface.

Alternative considered: expose many optional filters immediately.
Why not: the current UI gap is simple list visibility, and over-expanding the contract would create more authority-doc and implementation churn than needed.

## Risks / Trade-offs

- [Risk] Keyword semantics become ambiguous between `apiCode` and asset name matching -> Mitigation: define keyword matching behavior explicitly in the spec and API contract.
- [Risk] The existing `api_asset` indexes may be enough for status/category filters but less ideal for broader keyword search -> Mitigation: keep keyword scope lightweight first and only update `docs/sql/api-asset.sql` if implementation proves an additional index is necessary.
- [Risk] Management and discovery list contracts drift toward duplicated fields -> Mitigation: keep discovery and management as separate capabilities with intentionally different audience boundaries, but reuse compatible field naming where possible.
- [Risk] Frontend may assume richer filters than the backend proposal standardizes -> Mitigation: make the backend proposal the source for the frontend proposal's dependency and keep the frontend scoped to these same filters.
