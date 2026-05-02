## Why

BUG-002 shows that binding an AI capability profile may cause already-entered asset configuration to disappear from the edit form. The backend must guarantee that the AI-profile binding endpoint preserves existing asset configuration and returns a complete, stable asset response so the frontend does not lose user input when it refreshes form state from the response.

## What Changes

- Verify and harden `ApiAssetController` / `ApiAssetWebDelegate` / `ApiAssetApplicationService.attachAiCapabilityProfile(...)` so binding an AI profile updates only AI capability fields and publisher snapshot fields.
- Ensure the response returned by the AI-profile binding endpoint is a complete `ApiAssetResp`, including existing category, upstream config, auth config, request template, request example, response example, status, ownership-visible fields, and AI profile.
- Add regression tests proving that binding AI capability data does not clear non-AI asset configuration.
- If the current authority contract is ambiguous, update `docs/api/api-asset-management.yaml` with `tml-docs-spec-generate` using the API template to document that the AI-profile binding response returns a full asset representation.
- No SQL change is expected because the existing `api_asset` table already stores AI metadata and normal asset configuration fields.

## Capabilities

### New Capabilities
- `catalog-ai-profile-response-stability`: Ensure AI profile binding preserves existing asset configuration and returns a complete stable asset response.

### Modified Capabilities
- None.

## Impact

- Affected authority docs: `docs/api/api-asset-management.yaml` if response semantics need clarification.
- Affected code: `ApiAssetController.java`, `ApiAssetWebDelegate.java`, `ApiAssetApplicationService.java`, `ApiAssetAggregate.java`, `ApiAssetResp`, converter/persistence mapping, and related tests.
- Boundary note: if backend responses are already complete, the backend task should record that finding and hand off the remaining form-overwrite issue to the frontend without changing backend behavior unnecessarily.
