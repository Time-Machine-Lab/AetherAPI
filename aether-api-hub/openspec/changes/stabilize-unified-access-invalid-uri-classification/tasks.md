## 1. Authority Contract

- [ ] 1.1 Update `docs/api/unified-access.yaml` for `UnifiedAccessController.java` to document `502` upstream execution failure and `504` upstream timeout responses using `tml-docs-spec-generate`

## 2. Backend Failure Classification

- [ ] 2.1 Keep `JdkUnifiedAccessDownstreamProxyPort` responsible for translating malformed upstream URI construction failures into `UnifiedAccessProxyResponseModel.upstreamFailure(...)`
- [ ] 2.2 Ensure existing Unified Access response mapping continues to surface the execution-failure payload and status without reclassifying the failure as a platform pre-forward error

## 3. Verification

- [ ] 3.1 Add or update backend tests covering malformed upstream URLs returning `502` with `UPSTREAM_EXECUTION_FAILURE`
- [ ] 3.2 Verify timeout and other transport-failure paths remain classified as execution outcomes and do not regress into uncategorized server errors
