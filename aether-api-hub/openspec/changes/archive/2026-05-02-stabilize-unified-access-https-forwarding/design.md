## Context

Unified Access currently uses JDK `HttpClient` for downstream forwarding and has an execution-outcome model for upstream success, upstream failure, and timeout. Real HTTPS APIs can fail for reasons that are not covered by malformed-URI handling: TLS handshake, certificate trust, unsupported protocol, redirects, connection refusal, DNS, and upstream 4xx/5xx responses.

This change should not make the platform silently trust invalid certificates. It should make HTTPS behavior predictable, diagnostics stable, and tests explicit.

## Goals / Non-Goals

**Goals:**

- Verify HTTPS upstream success behavior and representative failure behavior.
- Ensure HTTPS transport failures are classified as stable upstream execution failures.
- Ensure `docs/api/unified-access.yaml` documents visible `502` and `504` outcomes.
- Keep diagnostics useful but sanitized.

**Non-Goals:**

- Disabling TLS certificate validation.
- Adding a new gateway product or proxy infrastructure.
- Changing API Key authentication.
- Changing asset publication or target-resolution rules.
- Implementing full redirect policy redesign unless investigation proves it is the direct cause of the reported bug.

## Decisions

### 1. Keep HTTPS failures in the downstream proxy boundary

TLS, connection, and HTTP execution problems belong in the infrastructure forwarding port. Controllers and application services should receive normalized execution outcomes.

### 2. Do not bypass certificate validation

If a target HTTPS server has invalid or untrusted certificates, Unified Access should return a classified execution failure rather than weakening TLS validation globally.

### 3. Document visible execution outcomes

Callers see `502`/`504`, so the single authority file `docs/api/unified-access.yaml` must document these responses before implementation is considered complete.

## Risks / Trade-offs

- [Risk] Some HTTPS failures are caused by real upstream certificate problems. -> Mitigation: classify clearly and expose sanitized detail so users can fix their upstream configuration.
- [Risk] Redirect behavior may differ from user expectation. -> Mitigation: first test current behavior, then decide whether redirect following belongs in this fix or a separate proposal.
- [Risk] Local tests for TLS can become brittle. -> Mitigation: use controlled test servers or mocked `HttpClient` behavior where practical.

## Migration Plan

1. Complete or account for `stabilize-unified-access-invalid-uri-classification` so `502`/`504` contract work is not duplicated.
2. Add tests for HTTPS success and representative HTTPS execution failures.
3. Harden downstream proxy exception classification where gaps are found.
4. Update `docs/api/unified-access.yaml` if execution responses are still missing.
5. Run focused Unified Access infrastructure, service, and adapter tests.
