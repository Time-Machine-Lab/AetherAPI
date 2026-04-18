## 1. Preconditions

- [x] 1.1 Read `docs/spec/Aether API HUB 鍚庣浠ｇ爜寮€鍙戣鑼冩枃妗?md` and confirm the response-boundary rule: `TML-SDK Result` applies to platform management APIs, not to successful unified access upstream responses.
- [x] 1.2 Reuse the approved unified entry contract and invocation models from `unified-access-entry-routing` instead of creating new top-level API or SQL documents in this change.

## 2. Upstream Execution Core

- [x] 2.1 Implement the upstream proxy service that builds the upstream request from the resolved invocation, passes through query/body data, injects required upstream auth, and strips internal-only headers.
- [x] 2.2 Implement the execution outcome model for upstream success, upstream failure, and upstream timeout, keeping these outcomes distinct from platform pre-forward failures.

## 3. Response and Streaming

- [x] 3.1 Implement successful response pass-through so upstream status code, response body, and important response headers are preserved without wrapping the success payload in `TML-SDK Result`.
- [x] 3.2 Implement the minimum streaming-capable execution boundary for targets marked as streaming-capable so streaming responses are not forced into a non-streaming model.

## 4. Verification

- [x] 4.1 Add tests for successful upstream proxying, upstream timeout classification, upstream failure classification, and success response pass-through without `TML-SDK Result`.
- [x] 4.2 Add tests or verification notes for at least one streaming-capable AI API path, and confirm this change does not introduce duplicate entry routing logic or extra user-facing API contracts.
