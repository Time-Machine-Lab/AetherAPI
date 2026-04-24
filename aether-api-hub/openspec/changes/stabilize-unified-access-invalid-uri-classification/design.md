## Context

Unified Access already distinguishes platform pre-forward failures from upstream execution outcomes in code and in archived change history, but the active main specs do not yet capture the specific case where request construction fails before `HttpClient.send` starts. The current controller authority contract in `docs/api/unified-access.yaml` also documents only success and platform pre-forward failures, leaving `502`/`504` execution outcomes underspecified even though runtime behavior already returns them.

This change stays inside the existing DDD boundaries: `UnifiedAccessController` keeps request parsing and delegation only, the application service keeps target resolution and orchestration, and the infrastructure forwarding port remains responsible for translating transport-layer execution problems into execution outcomes.

## Goals / Non-Goals

**Goals:**

- Define invalid configured upstream URIs as Unified Access execution failures, not as target-resolution or generic server failures.
- Update the single-controller authority contract `docs/api/unified-access.yaml` before code implementation so `502` and `504` execution outcomes are documented consistently.
- Keep the runtime fix localized to the downstream proxy implementation and its tests.

**Non-Goals:**

- Redesign Unified Access route structure, API key validation, or target-availability rules.
- Introduce new SQL documents, storage fields, or observability schema changes.
- Add generic upstream URL validation to asset creation or enable flows in this change.

## Decisions

### 1. Treat request-construction URI failures as execution failures

If Unified Access has already resolved a target API and begins constructing the outbound request, failures caused by malformed configured upstream URLs are treated as upstream execution failures. This keeps them distinct from platform pre-forward failures such as invalid API code, invalid credential, or target unavailability.

The alternative would be to classify malformed upstream URLs as platform-side configuration failures. We are not choosing that path here because the runtime already crossed target resolution and is operating inside the downstream forwarding boundary; reclassifying it as a pre-forward failure would blur the existing Unified Access failure taxonomy.

### 2. Keep the runtime fix inside the downstream proxy port

The transport adapter should catch `IllegalArgumentException` raised during URI creation or request construction and translate it into `UnifiedAccessProxyResponseModel.upstreamFailure(...)`, alongside the existing timeout and I/O execution-failure branches. This preserves controller and application-service simplicity and keeps transport normalization where it belongs.

The alternative would be to add defensive `try/catch` logic higher in the application or controller layer. We are not choosing that because it would leak HTTP-client construction details outside the infrastructure boundary.

### 3. Document execution-failure responses in the Unified Access authority contract

Because this behavior is client-visible, `docs/api/unified-access.yaml` must explicitly describe execution-failure responses for `UnifiedAccessController.java`, including `502` upstream execution failure and `504` upstream timeout outcomes. Per project rules, that authority doc update must be generated through `tml-docs-spec-generate` using the API template before implementation tasks are considered complete.

The alternative would be to leave the authority contract unchanged and treat this as implementation-only hardening. We are not choosing that because callers already see these HTTP statuses, and undocumented visible behavior is precisely what caused the current ambiguity.

## Risks / Trade-offs

- [Risk] Malformed upstream URLs are configuration defects, so classifying them as execution failures may look less precise than a configuration-specific platform error. -> Mitigation: keep the detail message in the execution-failure payload and preserve the distinction that the failure happened inside the forwarding boundary.
- [Risk] Updating `docs/api/unified-access.yaml` may reveal that other existing execution outcomes are also underdocumented. -> Mitigation: scope this change to the currently implemented `502` and `504` outcomes without expanding into a broader Unified Access contract redesign.
- [Risk] Future asset-management validation may choose to reject invalid upstream URLs earlier, creating overlap with this runtime protection. -> Mitigation: treat this change as runtime hardening that remains valuable even if earlier validation is added later.

## Migration Plan

1. Update `docs/api/unified-access.yaml` with explicit execution-failure and timeout responses for `UnifiedAccessController.java` using `tml-docs-spec-generate`.
2. Keep the downstream proxy implementation responsible for converting malformed URI construction failures into `UPSTREAM_EXECUTION_FAILURE` outcomes.
3. Add or update focused tests for malformed upstream URLs, existing timeout behavior, and any affected controller/application response expectations.
4. Verify Unified Access callers receive `502` or `504` execution outcomes instead of uncategorized server errors for forwarding-stage failures.

## Open Questions

- None for this scoped fix.
