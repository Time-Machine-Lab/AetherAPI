## Why

When a Unified Access target is enabled with an invalid upstream URL such as one without a URI scheme, the JDK HTTP client raises `IllegalArgumentException` before the outbound call can be executed. That failure currently sits outside the documented Unified Access failure taxonomy, so callers cannot rely on a stable execution-failure contract.

## What Changes

- Define malformed configured upstream URIs and other request-construction transport failures as Unified Access upstream execution failures instead of platform pre-forward failures.
- Update the top-level authority contract `docs/api/unified-access.yaml` for `UnifiedAccessController.java` so upstream execution failure and timeout responses are documented explicitly. The authority doc update must be generated through `tml-docs-spec-generate` with the API template.
- Keep the change limited to failure classification and contract clarity for Unified Access execution; it does not add new routes, change target-resolution rules, or modify database structures.

## Capabilities

### New Capabilities
- `unified-access-upstream-execution-failure-classification`: Define how Unified Access classifies malformed upstream target URIs and other request-construction transport failures, and how the controller contract exposes execution-failure responses.

### Modified Capabilities
- None.

## Impact

- Affected authority docs: `docs/api/unified-access.yaml`
- Affected code: Unified Access forwarding implementation and tests in `aether-api-hub-standard` infrastructure and any adapter/application response mapping that depends on execution-failure semantics
- Database impact: none; no `docs/sql/` changes are expected
- Boundary note: this change must stay separate from API key validation, target resolution, and asset-management proposals because the target API has already been resolved when this failure occurs
