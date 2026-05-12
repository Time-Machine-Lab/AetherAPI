## Context

The current asset edit drawer supports base metadata, upstream URL/auth, examples, and AI profile binding. The frontend already maps `asyncTaskConfig` in asset detail responses, but `ReviseAssetBody` and the edit form do not expose it. The backend contract treats `asyncTaskConfig: null` as no task query channel and a non-null object as the task query configuration consumed by Unified Access.

## Goals / Non-Goals

**Goals:**

- Let asset owners enable/disable async task query configuration from the existing asset edit drawer.
- Submit all known async task config fields: `enabled`, `queryMethod`, `queryUrlTemplate`, `authMode`, `authScheme`, `authConfig`, `statusPath`, `resultPath`, and `errorPath`.
- Keep override auth fields visually scoped to `authMode = OVERRIDE`.
- Validate that enabled task query has a URL template before saving.

**Non-Goals:**

- No automatic upstream task testing from the edit drawer.
- No status-path normalization or JSONPath validation.
- No changes to platform task storage, polling, or callbacks.
- No backend API changes.

## Decisions

### 1. Place async config in the asset edit drawer

The configuration belongs to API Catalog asset ownership, so the existing edit drawer is the correct location.

### 2. Disable by sending null

When the form toggle is off, the frontend sends `asyncTaskConfig: null`, matching the contract meaning that the asset does not declare a task query channel.

### 3. Default enabled config to GET and SAME_AS_SUBMIT

The backend contract recommends GET and defaults auth mode to SAME_AS_SUBMIT. The UI uses those defaults to reduce required input while still exposing override fields when needed.

## Risks / Trade-offs

- [Risk] The URL template may omit `{taskId}`. -> Mitigation: helper copy calls out the placeholder; backend remains the authority for stricter validation.
- [Risk] Users may confuse upstream auth override with API Key auth. -> Mitigation: labels explicitly say these fields are upstream task query auth, not `X-Aether-Api-Key`.
