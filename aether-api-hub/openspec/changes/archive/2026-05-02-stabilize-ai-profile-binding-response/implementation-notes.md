## Implementation Notes

- `docs/api/api-asset-management.yaml` already documents `PUT /current-user/assets/{apiCode}/ai-profile` as returning a full `ApiAssetResp`.
- `docs/sql/api-asset.sql` already contains normal asset configuration fields and AI capability fields for `api_asset`; no schema change is required.
- Backend aggregate, application service, persistence converter, and delegate mapping already preserve and return complete asset configuration during AI profile binding.
- This change adds regression coverage to lock that behavior down. If the console still clears form fields after these tests pass in a Maven-enabled environment, the remaining issue is likely frontend state replacement rather than backend response truncation.

## Verification

Focused backend tests passed with Maven 3.9.9 and JDK 17.0.4:

```powershell
mvn -pl aether-api-hub-standard/aether-api-hub-domain,aether-api-hub-standard/aether-api-hub-service,aether-api-hub-standard/aether-api-hub-infrastructure,aether-api-hub-standard/aether-api-hub-adapter -am "-Dtest=ApiAssetAggregateTest,ApiAssetApplicationServiceTest,MybatisApiAssetRepositoryTest,ApiAssetWebDelegateTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: BUILD SUCCESS. Focused test classes covered domain, service, adapter, and infrastructure asset behavior.
