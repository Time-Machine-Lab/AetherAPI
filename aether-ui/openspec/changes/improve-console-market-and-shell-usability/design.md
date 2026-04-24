## Context

The current `aether-console` implementation already exposes the affected experiences, but each one is under-specified at the UI level:

- `src/pages/index.vue` renders API Market cards with very little visible summary information and a detail panel that still treats `exampleSnapshot` as a single raw blob.
- `../docs/api/api-catalog-discovery.yaml` already defines richer discovery detail semantics, including `requestMethod` and a structured `exampleSnapshot` object with request/response snapshots, so the current frontend model is drifting from the authority document.
- `src/pages/workspace.vue` uses placeholder-only inputs across registration, asset configuration, and AI profile editing, which makes prefilled values hard to interpret.
- `src/layouts/ConsoleLayout.vue` places the sidebar inside the normal page flow, so the navigation column scrolls away and has no collapse affordance.

This is a cross-cutting frontend change touching page composition, layout behavior, API-model mapping, i18n copy, and interaction feedback. It benefits from a design document before implementation so the team can keep the solution aligned with the existing console shell and design system.

## Goals / Non-Goals

**Goals:**
- Make the API Market page feel information-complete without introducing undocumented backend fields.
- Render discovery detail data according to `api-catalog-discovery.yaml`, including explicit request-method and separated request/response example presentation when available.
- Make asset-management forms understandable when fields are empty or already populated by keeping labels visible.
- Keep the left navigation available during page scroll and support a shell-level collapsed mode that still preserves icon-based navigation.
- Preserve the current retained console route structure and navigation grouping.

**Non-Goals:**
- No backend API redesign or new business endpoints.
- No full console visual redesign beyond the requested usability fixes.
- No replacement of the current layout system with a mobile drawer or a new navigation architecture.
- No expansion of asset-management business scope beyond clearer form presentation.

## Decisions

### 1. Treat `api-catalog-discovery.yaml` as the authority for market detail presentation
The market-detail implementation should align its DTO/type mapping and render logic to the discovery authority document instead of continuing to rely on an outdated frontend-only shape.

Alternative considered: keep the current frontend shape and just add more decorative UI around it.
Why not: that would leave the request-method visibility problem unresolved and would continue to treat `exampleSnapshot` incorrectly as a single blob instead of the documented request/response snapshot structure.

### 2. Increase API Market density by reusing existing metadata, not by inventing new summaries
The list cards should become denser through better use of already documented summary fields and clearer visual grouping, while the detail panel should carry the richer contract-backed metadata.

Alternative considered: add custom derived marketing copy or undocumented teaser fields to each card.
Why not: the request is about usability and information density, not fabricated content, and the authority contract already provides enough structure to improve the experience.

### 3. Use persistent field labels for workspace forms while keeping placeholders as secondary hints
Asset-management forms should expose visible labels above or alongside controls so operators can identify prefilled values without clearing inputs.

Alternative considered: keep placeholders only and add more section descriptions.
Why not: placeholders disappear as soon as a value exists, which is exactly the failure mode the user reported.

### 4. Make sidebar usability a shell concern with local UI state
Sticky behavior and collapse/expand should be handled in `ConsoleLayout.vue`, with the sidebar remaining independently scrollable and the collapsed state managed locally inside the shell.

Alternative considered: solve the issue per page by changing page-level layout spacing.
Why not: the problem belongs to the shell itself and would otherwise need to be duplicated across multiple pages.

### 5. Keep the collapse behavior additive instead of reworking navigation semantics
Collapsed mode should reduce width and hide verbose text while preserving icon affordance, active state, and the existing route/hash model.

Alternative considered: replace the sidebar with a different navigation component when collapsed.
Why not: that would turn a usability enhancement into a structural redesign and create unnecessary implementation risk.

## Risks / Trade-offs

- [Risk] Runtime discovery payloads may still differ from `api-catalog-discovery.yaml` even though the authority doc already defines richer fields -> Mitigation: implementation must verify contract alignment first and treat authority drift as a prerequisite issue instead of silently inventing adapters.
- [Risk] Adding more market metadata can make cards feel crowded on smaller breakpoints -> Mitigation: keep dense metadata lightweight on cards and reserve the richest field presentation for the sticky detail panel.
- [Risk] More visible labels in workspace forms can increase vertical space and visual weight -> Mitigation: use the existing field styling and hierarchy from `DESIGN.md`, keeping labels compact and secondary to section titles.
- [Risk] Sidebar collapse can reduce discoverability if text disappears completely -> Mitigation: retain icons, active-state emphasis, and a clear toggle affordance, while keeping expanded mode as the default starting state.
