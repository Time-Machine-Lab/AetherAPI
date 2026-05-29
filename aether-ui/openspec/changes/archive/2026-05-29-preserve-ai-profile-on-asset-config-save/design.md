## Context

`aether-console` keeps API asset editor state in `useWorkspaceCatalog`. Loading or opening an asset editor hydrates both `assetConfigForm` and `aiProfileForm` from `currentAsset`. AI profile binding already merges the binding response with the previous asset to avoid clearing unrelated base configuration when the backend response is partial.

The inverse path is less defensive: base asset configuration save calls `reviseAsset()` and then replaces `currentAsset` through `setCurrentAsset(updated)`. If a revision response omits AI profile fields, the subsequent form sync clears `aiProfileForm`, even though the existing API contract supports `ApiAssetResp.aiCapabilityProfile`.

## Goals / Non-Goals

**Goals:**

- Keep AI profile state visible and available after saving base asset configuration.
- Preserve the existing current-user asset API contract and API adapter boundaries.
- Add focused regression coverage around the composable state transition.

**Non-Goals:**

- No backend contract, persistence, or controller changes.
- No changes to the editor drawer layout, visual design, field copy, or i18n keys.
- No change to the two-button workflow for saving base configuration and binding AI capability configuration.

## Decisions

### Decision 1: Merge revision responses with the previous AI profile in the composable

Add a small merge path for base asset revision results that keeps `updated.aiProfile` when present and falls back to `previous.aiProfile` when the response omits it.

Rationale:

- The existing composable already owns edit-state orchestration and already uses a similar merge pattern for AI binding responses.
- Keeping the fix in the composable avoids inventing request-layer semantics or changing the API adapter contract.
- A missing AI profile in a response should be treated as absent data, not an instruction to clear the current editor state.

Alternative considered:

- Re-fetch asset detail after every base save. This would also restore the full state but adds an extra network round trip and still depends on timing; the current save response is already enough for the edited fields.

### Decision 2: Cover the partial-response case in unit tests

Add a regression test where an `AI_API` asset has an existing AI profile, `reviseAsset()` resolves with a base asset response that omits AI profile data, and `handleSaveAssetConfig()` keeps both `currentAsset.aiProfile` and `aiProfileForm`.

Rationale:

- The bug is a state-management regression, so the composable test is the narrowest useful coverage.
- The existing tests already cover the opposite direction: AI binding must not clear base configuration.

## Risks / Trade-offs

- [Risk] If the backend intentionally clears an AI profile by returning no profile, this merge would keep stale data in the editor. -> Mitigation: the current API has a dedicated AI profile binding endpoint and no documented clear operation in this flow; absence in a base revision response is not a clear signal.
- [Risk] A future explicit clear operation may need different semantics. -> Mitigation: that future change should expose an explicit response field or action and add a separate test.
