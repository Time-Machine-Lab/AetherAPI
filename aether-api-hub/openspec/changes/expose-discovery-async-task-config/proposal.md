## Why

Marketplace API document export is backed by the Discovery detail contract, but Discovery detail does not currently expose an asset's async task query configuration. As a result, exported API docs cannot describe the platform task query channel even when the asset is configured for async task querying.

## What Changes

- Extend `CatalogDiscoveryController` detail response contract with nullable `asyncTaskConfig`.
- Populate the field from `api_asset.async_task_config` for published, non-deleted assets.
- Keep sensitive auth override material out of generated user documentation; the backend still returns the existing config shape for contract consistency.

## Non-Goals

- No new task lifecycle storage.
- No task status polling or cached task result API.
- No change to Unified Access task query execution behavior.
