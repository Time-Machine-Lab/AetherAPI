## Why

The Unified Access playground already lets users assemble and send an invocation, but users still need to manually translate the same inputs into terminal commands for local debugging or integration handoff. Adding copyable curl output closes that gap while staying within the existing `docs/api/unified-access.yaml` contract and the current console interaction model.

## What Changes

- Add a copy-curl capability to the `aether-console` Unified Access playground.
- Generate curl commands from the current playground form state, including `apiCode`, HTTP method, `X-Aether-Api-Key`, optional request headers, and request body only when the selected method supports a body.
- Support both Linux/macOS shell format and Windows command format so users can copy the command that matches their terminal environment.
- Provide internationalized labels, empty/invalid-state feedback, and copy-success or copy-failure feedback using existing console action, field, surface, and code-display patterns.
- Keep invocation execution, bearer-session authentication, subscription status behavior, and backend API contracts unchanged.

## Capabilities

### New Capabilities
- `console-unified-access-copy-curl`: Covers generation, display, and clipboard copying of Windows and Linux curl commands from the Unified Access playground.

### Modified Capabilities
- None.

## Impact

- Affected app: `aether-ui/aether-console`.
- Affected areas: Unified Access playground page/composable, curl formatting utility or composable logic, i18n resources, and focused Vitest coverage.
- API impact: no new backend endpoints and no changes to `docs/api/unified-access.yaml`; generated commands target the documented Unified Access paths and `X-Aether-Api-Key` header.
- Dependency impact: no new runtime dependency is expected; clipboard access should use browser Clipboard API with existing UI feedback fallback patterns where needed.
