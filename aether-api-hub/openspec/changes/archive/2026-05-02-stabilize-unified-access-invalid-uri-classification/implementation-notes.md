## Implementation Notes

- `docs/api/unified-access.yaml` now documents `502` upstream execution failure and `504` upstream timeout responses for `UnifiedAccessController.java`.
- `JdkUnifiedAccessDownstreamProxyPort` already owns malformed URI construction classification through `IllegalArgumentException` handling and returns `UnifiedAccessProxyResponseModel.upstreamFailure(...)`.
- Added adapter delegate coverage to prove execution-failure status and payload are surfaced as-is instead of being reclassified or wrapped.

## Verification

Focused backend tests passed with Maven 3.9.9 and JDK 17.0.4:

```powershell
mvn -pl aether-api-hub-standard/aether-api-hub-service,aether-api-hub-standard/aether-api-hub-adapter,aether-api-hub-standard/aether-api-hub-infrastructure -am "-Dtest=UnifiedAccessApplicationServiceTest,UnifiedAccessWebDelegateTest,JdkUnifiedAccessDownstreamProxyPortTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: BUILD SUCCESS. Covered service execution outcome handling, adapter response mapping, and infrastructure downstream proxy failure classification.
