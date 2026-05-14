## Context

The current `console-workspace` page already contains category management plus an asset-management card, but that card only supports "load by apiCode", "register new asset", and "recent assets". The console therefore lacks the browse-first workflow operators expect when revisiting previously created assets.

The target experience must remain inside `aether-console` and follow the existing design hierarchy documented in `aether-console/DESIGN.md`: workspace cards, row rhythm, surface semantics, i18n-backed user text, and the existing protected-shell layout. The frontend also must not invent its own contract; it depends on the updated backend authority document at `../docs/api/api-asset-management.yaml`.

## Goals / Non-Goals

**Goals:**
- Add a workspace asset list panel that lets operators browse existing assets without entering an `apiCode` first.
- Support the same basic filters and paging semantics that the management list API standardizes.
- Reuse the existing single-asset detail snapshot panel so list selection and direct code lookup converge on one current-asset detail surface.
- Keep all user-facing copy in the existing i18n system and preserve console visual alignment rules.

**Non-Goals:**
- No redesign of the public discovery market page.
- No new application shell, independent route tree, or dashboard-style analytics view.
- No bulk actions, drag-and-drop organization, or advanced faceted filtering in this change.
- No divergence from the backend authority contract.

## Decisions

### 1. Keep the feature inside `console-workspace`
The asset list will be added to the existing workspace composition rather than opening a separate route. This keeps navigation, auth, and layout behavior aligned with the current console shell.

Alternative considered: add a new asset-list page or route.
Why not: the current shell already routes asset management to `console-workspace`, and a new page would add navigation complexity without solving the core gap.

### 2. Add a dedicated list API wrapper and workspace state branch
The frontend should introduce a list API function plus workspace state for page/filter/list selection, instead of overloading `getAsset` and the current single-code workflow.

Alternative considered: fetch all assets through repeated detail calls or repurpose discovery APIs.
Why not: discovery omits draft and disabled assets, and N+1 detail fetches would produce the wrong semantics and weaker performance.

### 3. Let list selection drive the existing current-asset detail card
Selecting a row from the asset list should hydrate the same `currentAsset` detail area used by direct code lookup and post-registration flows.

Alternative considered: build a second parallel detail component for list items.
Why not: that would duplicate state and create inconsistent asset-management interactions.

### 4. Follow existing workspace visual grammar instead of inventing a new table system
The list should use the console's existing card/row/action vocabulary from `DESIGN.md`, which favors aligned rows and card sections over a dense enterprise table.

Alternative considered: introduce a data-grid or table library.
Why not: the current console visual language does not use that pattern, and the feature only needs moderate browsing density.

## Risks / Trade-offs

- [Risk] The workspace page becomes visually crowded once category management, asset list, register form, and detail state all coexist -> Mitigation: keep the list scoped to the asset-management column and reuse the existing detail panel rather than adding more peer sections.
- [Risk] Backend contract timing may block frontend implementation -> Mitigation: make the updated `../docs/api/api-asset-management.yaml` an explicit prerequisite in tasks.
- [Risk] Direct code lookup and list selection can diverge in behavior -> Mitigation: route both flows through shared `currentAsset` state and shared error/loading handling.
- [Risk] A row-based card list may become less scalable than a dense table if the dataset grows substantially -> Mitigation: keep pagination and lightweight filters in scope now; reconsider a denser presentation only if future requirements demand it.
