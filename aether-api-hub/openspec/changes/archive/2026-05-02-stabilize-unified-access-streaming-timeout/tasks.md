## 1. Reproduction And Contract Check

- [x] 1.1 Reproduce BUG-004 with a controlled streaming upstream or a real AI streaming API and record whether timeout occurs before response setup, during stream transfer, or in adapter response writing.
- [x] 1.2 Review `docs/api/unified-access.yaml` and update it with `tml-docs-spec-generate` using the API template if streaming timeout or success semantics are missing or unclear.
- [x] 1.3 Confirm no `docs/sql/` update is required because this change does not add storage fields.

## 2. Streaming Timeout Stabilization

- [x] 2.1 Add tests for streaming-capable targets proving the response uses streaming passthrough and does not buffer the full body.
- [x] 2.2 Add tests for long-lived streaming responses that remain active within the configured streaming budget.
- [x] 2.3 Add tests for stream setup timeout classification.
- [x] 2.4 Adjust `JdkUnifiedAccessDownstreamProxyPort`, timeout configuration, or `UnifiedAccessWebDelegate` streaming mapping if tests show ordinary timeout behavior is breaking valid streams.

## 3. Verification

- [x] 3.1 Run focused Unified Access infrastructure tests.
- [x] 3.2 Run affected adapter tests for `StreamingResponseBody` behavior.
- [x] 3.3 Verify successful streaming responses remain unwrapped and platform failure responses remain classified.
