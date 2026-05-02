## 1. Reproduction And Contract Check

- [x] 1.1 Reproduce BUG-003 with at least one HTTPS upstream target and record whether the failure is TLS, connection, redirect, request construction, upstream 4xx/5xx, or response mapping.
- [x] 1.2 Review `stabilize-unified-access-invalid-uri-classification` and either complete its unchecked tasks first or explicitly include its documentation expectations in this implementation.
- [x] 1.3 Update `docs/api/unified-access.yaml` with `tml-docs-spec-generate` using the API template if `502` upstream execution failure and `504` timeout responses are not fully documented.

## 2. HTTPS Forwarding Stabilization

- [x] 2.1 Add focused tests for successful HTTPS forwarding or a controlled equivalent that exercises the JDK HTTP client HTTPS path.
- [x] 2.2 Add focused tests for representative HTTPS execution failures and verify they map to stable upstream execution outcomes.
- [x] 2.3 Harden `JdkUnifiedAccessDownstreamProxyPort` exception mapping if TLS, connection, redirect, or request-execution failures escape the execution-outcome model.
- [x] 2.4 Verify failure payloads do not include caller API keys, upstream tokens, or raw auth config values.

## 3. Verification

- [x] 3.1 Run focused Unified Access infrastructure tests.
- [x] 3.2 Run affected Unified Access service and adapter tests.
- [x] 3.3 Record remaining unsupported HTTPS cases as explicit follow-up notes if they are outside this fix.
