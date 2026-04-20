## 1. API Authority Documents

- [x] 1.1 Use `tml-docs-spec-generate` to update or realign the repo-root `docs/api/` authority file mapped to `CategoryController.java`, and record parameter-binding-related `400` scenarios.
- [x] 1.2 Use `tml-docs-spec-generate` to update or realign the repo-root `docs/api/` authority file mapped to `ApiAssetController.java`, and record invalid-request behavior for asset detail binding.
- [x] 1.3 Use `tml-docs-spec-generate` to update `docs/api/api-credential.yaml` mapped to `ApiCredentialController.java`, and record current-user API key list/detail invalid-request behavior.
- [x] 1.4 If the same binding-risk pattern is confirmed for `CatalogDiscoveryController.java` or `ApiCallLogController.java`, update or add their repo-root `docs/api/` authority documents before code changes.

## 2. Regression Coverage

- [x] 2.1 Add backend Web-layer tests that reproduce current binding failures for category list/detail, asset detail, and current-user API key list endpoints.
- [x] 2.2 Add exception-handling tests that verify framework-level binding failures map to the correct interface-family error code and no longer expose compiler or reflection hints.
- [x] 2.3 Scan other controllers that use the same implicit `@PathVariable` / `@RequestParam` style and add at least one regression case for each high-risk endpoint kept in scope.

## 3. Parameter Binding Stability Fix

- [x] 3.1 Adjust the parent Maven compiler configuration to emit Java parameter metadata, and update in-scope controllers to use explicit `@PathVariable` / `@RequestParam` names where binding currently depends on implicit inference.
- [x] 3.2 Update global exception handling so framework-level binding failures for category, asset, credential, discovery, and call-log endpoints no longer collapse into `CATEGORY_CODE_INVALID`.
- [x] 3.3 Ensure binding-failure responses use sanitized client-facing messages and do not leak `-parameters`, reflection metadata, or similar internal diagnostics.

## 4. Verification

- [x] 4.1 Run the targeted backend test suite covering in-scope controllers and the global exception handler.
- [x] 4.2 Verify in a packaged artifact or equivalent smoke path that the documented endpoints no longer regress to framework-level `400` binding failures during frontend联调.
