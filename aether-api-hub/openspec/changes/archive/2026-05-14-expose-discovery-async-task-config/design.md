## Design

Discovery detail remains a read-only projection over published `api_asset` rows. The async task configuration is already stored as JSON in `api_asset.async_task_config`; this change threads that value through the existing query record, service detail model, and web response.

The response reuses the existing `AsyncTaskConfigResp` shape to avoid creating a second async task contract. Discovery sets `authConfig` to null so public marketplace detail does not expose credential material. Invalid or absent JSON is treated as no declared async task config for Discovery export purposes.

## Contract Boundary

`docs/api/api-catalog-discovery.yaml` is updated because it is the one-to-one top-level API contract for `CatalogDiscoveryController.java`.
