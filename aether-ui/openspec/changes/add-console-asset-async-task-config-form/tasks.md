## 1. Authority Context

- [x] 1.1 Confirm `docs/api/api-asset-management.yaml` already defines request/response `asyncTaskConfig`.

## 2. API And Composable

- [x] 2.1 Add `asyncTaskConfig` to `ReviseAssetBody` and `reviseAsset` request mapping.
- [x] 2.2 Add async task form state and sync it from selected asset detail.
- [x] 2.3 Save enabled async config and send `null` when disabled.
- [x] 2.4 Add validation for enabled config without query URL template.

## 3. UI And I18n

- [x] 3.1 Add async task query form fields to the asset edit drawer.
- [x] 3.2 Add zh-CN and en-US labels, hints, and validation copy.

## 4. Verification

- [x] 4.1 Add/update API and workspace composable tests.
- [x] 4.2 Run targeted catalog/workspace tests.
- [x] 4.3 Run `aether-console` type checking and formatting/lint checks.
