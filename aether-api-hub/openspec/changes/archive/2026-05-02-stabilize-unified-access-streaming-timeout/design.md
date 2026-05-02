## Context

Unified Access has a streaming-capable path using JDK `HttpClient` with `BodyHandlers.ofInputStream()` and adapter-level `StreamingResponseBody`. That is the right minimum shape, but streaming APIs can remain open much longer than ordinary request/response APIs. A single short request timeout can make valid streams look like failures.

The platform should keep the implementation lightweight while making streaming behavior predictable enough for AI API demos and developer trust.

## Goals / Non-Goals

**Goals:**

- Define and verify timeout behavior for streaming-capable targets.
- Avoid buffering full streaming responses in memory.
- Preserve streaming response headers and body flow where possible.
- Classify stream setup timeout/failure predictably.

**Non-Goals:**

- Implementing provider-specific SSE parsing.
- Transforming upstream stream chunks.
- Adding billing/token accounting.
- Adding a new gateway dependency.
- Guaranteeing recovery after a client disconnect or mid-stream network break.

## Decisions

### 1. Treat streaming timeout policy separately from ordinary request timeout

Streaming targets need a longer setup/read budget than non-streaming calls. The implementation can start with configuration or target-derived behavior without introducing a new infrastructure dependency.

### 2. Keep streaming passthrough untransformed

Unified Access should pass the upstream stream through as bytes and headers. It should not parse SSE, modify chunks, or wrap successful streaming payloads in TML Result.

### 3. Test stream setup and long-lived response behavior

The fix should include tests that hold a stream open long enough to prove it is not forced into the ordinary short timeout path.

## Risks / Trade-offs

- [Risk] Longer streaming timeouts can tie up server resources. -> Mitigation: keep the scope to streaming-capable assets and make timeouts bounded/configurable.
- [Risk] Mid-stream failures may occur after response headers are committed. -> Mitigation: document and test setup failures separately from post-commit stream interruptions.
- [Risk] Different AI providers have different streaming formats. -> Mitigation: do byte passthrough only in this change.

## Migration Plan

1. Reproduce streaming timeout with a controlled streaming upstream.
2. Add tests for streaming setup, long-lived stream passthrough, and setup timeout classification.
3. Adjust timeout policy and adapter streaming behavior where tests expose gaps.
4. Update `docs/api/unified-access.yaml` only if visible client-facing behavior needs clarification.
5. Run focused Unified Access tests.
