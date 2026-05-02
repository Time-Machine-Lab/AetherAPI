## Why

BUG-004 reports that streaming requests are easy to time out. The archived upstream-proxy work established a minimum streaming-capable boundary, but it does not prove that long-lived AI streaming responses have appropriate timeout behavior, flushing behavior, and error classification.

## What Changes

- Stabilize Unified Access behavior for streaming-capable upstream targets, especially long-lived AI API responses.
- Separate normal request timeout expectations from streaming response lifetime expectations where the current implementation treats both too similarly.
- Ensure streaming responses are passed through without buffering the full response body.
- Ensure stream setup failures and mid-stream failures are classified predictably and logged without replacing successful partial passthrough semantics with generic server errors.
- Update `docs/api/unified-access.yaml` with `tml-docs-spec-generate` using the API template if streaming behavior or timeout outcomes need clearer client-facing documentation.
- No database change is expected.

## Capabilities

### New Capabilities
- `unified-access-streaming-timeout-stability`: Stabilize timeout and passthrough behavior for streaming-capable Unified Access upstream calls.

### Modified Capabilities
- None.

## Impact

- Affected authority docs: `docs/api/unified-access.yaml` if streaming/timeout response semantics require clarification
- Affected code: `JdkUnifiedAccessDownstreamProxyPort`, `UnifiedAccessWebDelegate`, streaming response mapping, timeout configuration, and related tests
- Boundary note: this change does not implement full AI Gateway routing, token accounting, or model streaming protocol transformation
- Database impact: none
