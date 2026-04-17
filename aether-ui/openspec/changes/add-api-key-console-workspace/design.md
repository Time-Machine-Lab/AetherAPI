## Context

`aether-console` already serves as the single frontend surface for API Hub marketplace browsing and protected workspace operations, and its sidebar reserves a `credentials` entry that is not yet backed by real functionality. At the same time, the backend authority documents now establish a clear first-phase rule set: users manage their own API Keys, `Consumer` remains an internal model, API Key plaintext is revealed only once, and `lastUsedSnapshot` is updated by unified access rather than by the page itself.

The frontend therefore needs a design that fits the existing console shell, `axios`-based API layer, i18n rules, and `aether-console/DESIGN.md` semantics, while staying inside the current contract boundary of `docs/api/api-credential.yaml`. The design must not synthesize undocumented unified-access request headers, hidden storage fields, or a separate `Consumer` UI.

## Goals / Non-Goals

**Goals:**

- Add a credential-management workspace inside `aether-console` that lets the current user create, inspect, and operate on API Keys through `docs/api/api-credential.yaml`.
- Reflect credential lifecycle information in a developer-friendly way, including masked key display, expiration, status, and `lastUsedSnapshot`.
- Provide a one-time plaintext reveal flow and persistent security guidance that make the backend one-time-secret rule obvious to users.
- Reuse the existing console shell, semantic roles, API layer, route hash navigation, and i18n structure without adding a new app or a parallel admin experience.
- Keep frontend behavior aligned with the hidden-`Consumer` product model by explaining the concept indirectly instead of introducing explicit `Consumer` creation or management.

**Non-Goals:**

- Do not create any explicit `Consumer` page, route, controller-facing concept, or user-editable `Consumer` workflow.
- Do not add usage, billing, orders, API call logs, or observability pages that are only placeholder navigation items today.
- Do not change `docs/api/api-credential.yaml`, invent a new unified-access HTTP calling contract, or introduce new backend endpoints in this change.
- Do not add a new global business store for credential orchestration; keep asynchronous composition in `api` modules and page-scoped composition logic.

## Decisions

### 1. Place the new experience inside the existing `console-workspace` credential anchor

- Decision: implement the first version of API Key management under the existing `console-workspace` route and `#credentials` anchor instead of creating a separate route tree or a second console application.
- Rationale: `console-shell.ts` already models `credentials` as an operations-side workspace section, and the current product structure keeps marketplace browsing plus protected operations inside one `aether-console` shell.
- Alternative considered: create a dedicated credential route family or move the feature to `aether-admin-console`.
- Why not: a separate route family would add navigation and state duplication before the workflow is proven, and `aether-admin-console` is outside the current target product direction.

### 2. Keep data access in a dedicated credential API module plus page-scoped composition logic

- Decision: add a credential-focused API module under `src/api`, plus page-scoped composition / helpers for filtering, refresh, action state, and one-time reveal handling.
- Rationale: the frontend specification requires pages to assemble state and interactions without issuing raw requests, while stores remain reserved for globally shared state such as authentication.
- Alternative considered: place all request logic directly inside the workspace page or add a new Pinia store for credentials.
- Why not: page-layer raw requests violate the documented layering rules, and a global store would over-centralize a workflow whose state is local to the credential workspace.

### 3. Use a split workspace surface: list, detail, and create/reveal flow

- Decision: model the workspace as a credential list with status filters, a detail surface for the selected key, and a create flow that ends with a one-time plaintext reveal state plus security notice.
- Rationale: the API contract naturally separates list, detail, and lifecycle actions, while the console design system already supports surface cards, status badges, notices, and selected detail panels.
- Alternative considered: a table-only layout or a multi-page wizard.
- Why not: a table-only layout makes one-time reveal and detail states harder to stage clearly, while a multi-page wizard adds routing complexity that the current shell does not need.

### 4. Treat backend fields as the single source of truth for status and recent usage

- Decision: render credential status, expiration, revocation, and `lastUsedSnapshot` strictly from the API response, limiting the client to formatting and empty-state presentation.
- Rationale: `consumer-auth-unified-access-auth` makes recent-usage snapshots a backend responsibility, and the frontend should not infer auth success, synthesize security events, or fabricate hidden fields.
- Alternative considered: derive extra usage status locally or cache pseudo-usage history in the browser.
- Why not: that would create a second, non-authoritative audit trail and break the rule that authority lives in backend contracts and domain documents.

### 5. Keep guidance explicit but bounded by authority documents

- Decision: include a credential guidance block in the workspace that explains the hidden-`Consumer` model, one-time secret handling, and where to look for official calling details, while refusing to invent executable unified-access examples that are not yet documented.
- Rationale: developers need onboarding help, but the archived unified-access backend proposal explicitly avoids adding a new user-facing HTTP auth interface in this phase.
- Alternative considered: generate a full request-example panel with assumed headers and endpoint shapes.
- Why not: that would exceed the current authority documents and risk teaching an interface contract that the backend has not published.

## Risks / Trade-offs

- [Risk] Backend delivery may lag behind the frontend proposal, especially for `lastUsedSnapshot` updates or lifecycle action readiness. -> Mitigation: keep the spec bound to `docs/api/api-credential.yaml` and define empty / unavailable states for snapshot fields instead of inventing fallback data.
- [Risk] Users may expect to retrieve the full API Key again after leaving the create flow. -> Mitigation: make one-time reveal a dedicated success state with persistent warning copy and ensure all later surfaces only render `maskedKey`.
- [Risk] The existing workspace already hosts catalog management, so adding credentials could make the route feel crowded. -> Mitigation: keep the credential experience anchored to its own section and reuse the existing sidebar hash navigation instead of interleaving operations into catalog panels.
- [Risk] Guidance content can drift into undocumented auth behavior. -> Mitigation: restrict guidance to concepts already present in authority docs and treat any new example or auth-contract text as a prerequisite docs update.

## Migration Plan

1. Confirm `docs/api/api-credential.yaml`, the Consumer & Auth domain document, and `aether-console/DESIGN.md` are still the governing authority before implementation begins.
2. Add credential API modules, DTO mapping, and page-scoped orchestration in `aether-console`, keeping the page layer free of raw requests.
3. Introduce the credential workspace section, create / reveal flow, lifecycle actions, and i18n copy.
4. Add guidance and empty / loading / error states for unavailable usage snapshots and non-documented auth details.
5. Verify the workspace through tests plus `lint`, `type-check`, and `build` before enabling broader follow-up work such as usage or observability pages.

## Open Questions

- Should the first frontend implementation default the credentials section to a list-first layout on desktop and a stacked list-detail flow on mobile, or should both viewports use the same linear layout?
- If backend later publishes a dedicated unified-access calling contract, should that guidance stay embedded in the credential workspace or move to the existing `docs` navigation area while keeping the workspace focused on lifecycle management?
