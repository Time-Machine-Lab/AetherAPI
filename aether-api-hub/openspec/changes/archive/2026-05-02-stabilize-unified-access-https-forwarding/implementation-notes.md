## Implementation Notes

- Reproduced representative HTTPS forwarding outcomes in focused infrastructure tests:
  - Successful HTTPS target construction and response mapping through a controlled `HttpClient` equivalent.
  - HTTPS connection failure against an unused local port, classified as `502` / `UPSTREAM_EXECUTION_FAILURE`.
  - Timeout behavior remains classified as `504` / `UPSTREAM_TIMEOUT`.
- `stabilize-unified-access-invalid-uri-classification` has been archived under `openspec/changes/archive/2026-05-02-stabilize-unified-access-invalid-uri-classification`; its `502` and `504` documentation expectations are already reflected in `docs/api/unified-access.yaml`.
- `JdkUnifiedAccessDownstreamProxyPort` remains the boundary that normalizes HTTPS transport/request-execution failures into `UnifiedAccessProxyResponseModel` outcomes.
- Execution failure `detail` is now defensively sanitized to avoid leaking caller API keys, Authorization header values, upstream auth config, or upstream token values if a low-level exception message includes them.

## Follow-up Notes

- This change does not disable TLS certificate validation and does not add a trust-all client.
- Local self-signed TLS success testing is intentionally not introduced here to avoid brittle test-only certificate generation and JVM export flags. The successful HTTPS path is covered by asserting an `https` outbound `HttpRequest` through a controlled `HttpClient`, while real transport failures are covered through JDK `HttpClient` execution.
- Redirect policy remains unchanged. If product expectations require automatic redirect following, create a separate Unified Access redirect policy change.

## Verification

Focused infrastructure tests passed with Maven 3.9.9 and JDK 17.0.4:

```powershell
mvn -pl aether-api-hub-standard/aether-api-hub-infrastructure -am "-Dtest=JdkUnifiedAccessDownstreamProxyPortTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: BUILD SUCCESS. `JdkUnifiedAccessDownstreamProxyPortTest` ran 8 tests with 0 failures.

Affected service and adapter tests passed:

```powershell
mvn -pl aether-api-hub-standard/aether-api-hub-service,aether-api-hub-standard/aether-api-hub-adapter -am "-Dtest=UnifiedAccessApplicationServiceTest,UnifiedAccessWebDelegateTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: BUILD SUCCESS. `UnifiedAccessApplicationServiceTest` ran 9 tests and `UnifiedAccessWebDelegateTest` ran 4 tests with 0 failures.
