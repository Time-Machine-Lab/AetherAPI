## Context

API Catalog currently models asset configuration with a flat top-level field set plus a few dedicated value objects such as `UpstreamEndpointConfig`, `AiCapabilityProfile`, and `AsyncTaskConfig`. That model works for current scope, but each new platform first-class field still requires broad edits across contract DTOs, service commands, aggregate state, persistence entities, converters, and detail responses.

The current discussion established two constraints for this change:

- Existing asset fields must stay in place. This change must not migrate or rename `aiProfile`, `asyncTaskConfig`, example snapshots, JSON Schema snapshots, or any current top-level field.
- Future platform features still need a governed additive entry point so they can land without further top-level DTO growth.

The authority boundary also remains the same as current API Catalog work:

- Owner-scoped asset management is the only external write contract for this change.
- `docs/api/api-asset-management.yaml` remains the authority file for `ApiAssetController.java`.
- `docs/sql/api-asset.sql` remains the authority file for table `api_asset`.
- Discovery and Unified Access are intentionally not changed to consume generic extension blocks yet.

## Goals / Non-Goals

**Goals:**

- Add explicit future-feature extension blocks to API assets without moving existing first-class fields.
- Keep owner-scoped asset management as the only way to write the extension blocks.
- Allow future nested config inside an extension block to grow with fewer top-level field changes.
- Preserve current publish readiness, Discovery, Unified Access, and proxy-routing behavior.

**Non-Goals:**

- No migration of current top-level fields into `capabilityExtensions`, `policyExtensions`, or `metadataExtensions`.
- No generic marketplace or consumer exposure of extension blocks in Discovery in this change.
- No Unified Access forwarding, publish validation, or routing behavior change based on extension blocks.
- No dynamic schema registry, block-specific validation DSL, or per-feature execution logic in this change.

## Decisions

### 1. Add extension blocks as additive owner-scoped fields

The change adds three optional block fields on API assets:

- `capabilityExtensions`
- `policyExtensions`
- `metadataExtensions`

These are additive and nullable. Existing asset fields remain the authoritative place for current features. For example, `AiCapabilityProfile` continues to live in its existing first-class field path rather than being moved into `capabilityExtensions`.

This is the key boundary that keeps the change low-risk: future work gets a new landing zone, but current stable behavior is left untouched.

### 2. Store block content as opaque JSON objects while persisting raw text

The owner-scoped API contract should expose the extension blocks as nullable JSON objects, not as arbitrary strings. This gives future nested fields a structured home and avoids introducing more top-level DTO properties later.

Persistence still stores the blocks as nullable text snapshots on `api_asset`, following the same pragmatic storage pattern already used for JSON-like asset data such as `auth_config` and `async_task_config`.

Consequences:

- The API contract can evolve nested block content without introducing new top-level request/response fields.
- The database change remains additive and low-risk.
- The backend must preserve unknown nested keys rather than attempting to normalize them away.

### 3. Keep generic extension blocks owner-scoped only in phase one

Generic extension blocks are only readable and writable through owner asset management in this change. Discovery detail, Discovery list, and Unified Access do not expose or consume them.

This avoids prematurely leaking internal or owner-only configuration before individual future features define which sub-blocks, if any, belong in published or runtime-facing contracts.

### 4. Extension blocks do not participate in publish readiness by default

Current publish validation continues to depend only on existing first-class asset rules: asset name, category validity, upstream completeness, and AI profile when `assetType == AI_API`.

Adding or revising `capabilityExtensions`, `policyExtensions`, or `metadataExtensions` does not make a draft asset publishable and does not force a published asset to unpublish unless a future dedicated change explicitly adds such coupling.

This keeps the change infrastructure-focused rather than altering marketplace behavior.

### 5. Future features must claim typed sub-blocks instead of adding new top-level fields first

After this change, a new first-class platform feature should default to one of the extension blocks unless it genuinely changes core asset identity or publish rules.

Examples:

- AI testing configuration should prefer a typed object under `capabilityExtensions.testConfig`.
- AI document ingestion hints should prefer a typed object under `capabilityExtensions.docIngestionConfig` or `metadataExtensions.sourceTrace`, depending on runtime ownership.
- Future rate limits or pricing knobs should prefer `policyExtensions`.

This decision is what turns the new blocks into a real change-cost reducer rather than just three new fields.

## Risks / Trade-offs

- [Risk] Generic extension blocks can become a dumping ground. -> Mitigation: keep them owner-scoped first, preserve explicit sub-block naming, and require future changes to claim typed sub-blocks in specs and design docs.
- [Risk] Opaque JSON reduces immediate backend validation. -> Mitigation: phase one validates nullable object shape and round-trip behavior only; future features add typed validation when they claim a sub-block.
- [Risk] Branch conflicts with `add-api-asset-json-schemas` because both touch `docs/api/api-asset-management.yaml` and `docs/sql/api-asset.sql`. -> Mitigation: sequence or merge carefully and keep this proposal explicitly additive to existing fields.
- [Risk] Teams may assume existing fields should be backfilled into the new blocks. -> Mitigation: make non-migration an explicit design rule and repeat it in proposal, specs, and tasks.

## Open Questions

- None for this foundation change. Feature-specific sub-block schemas such as `testConfig` or `docIngestionConfig` should be proposed in later changes.