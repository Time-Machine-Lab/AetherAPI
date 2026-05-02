## Why

BUG-003 reports that Unified Access may return `502` when forwarding to HTTPS upstream APIs. The existing `stabilize-unified-access-invalid-uri-classification` change only covers malformed configured URIs; it does not fully diagnose real HTTPS forwarding failures such as TLS handshake problems, redirects, connection failures, or header handling issues.

## What Changes

- Add a focused HTTPS upstream forwarding stability investigation and hardening change for Unified Access.
- Preserve the existing failure taxonomy: target resolution and credential failures remain platform pre-forward failures, while HTTPS transport/TLS/request-execution failures remain upstream execution outcomes.
- Ensure HTTPS upstream failures return stable, documented execution responses with enough diagnostic detail for developers without leaking secrets.
- Add regression coverage for successful HTTPS forwarding where practical and for representative HTTPS failure classifications such as TLS handshake/connect failures.
- Update `docs/api/unified-access.yaml` with `tml-docs-spec-generate` using the API template if the `502` / `504` execution response contract is still incomplete.
- No database change is expected.

## Capabilities

### New Capabilities
- `unified-access-https-forwarding-stability`: Stabilize HTTPS upstream forwarding behavior and execution-failure classification for Unified Access.

### Modified Capabilities
- None.

## Impact

- Affected authority docs: `docs/api/unified-access.yaml`
- Affected code: `JdkUnifiedAccessDownstreamProxyPort`, Unified Access response mapping, tests for infrastructure/application/adapter boundaries
- Related existing change: `stabilize-unified-access-invalid-uri-classification` should be completed or folded into this verification flow before archive, because both touch execution-failure documentation for `UnifiedAccessController.java`
- Database impact: none
