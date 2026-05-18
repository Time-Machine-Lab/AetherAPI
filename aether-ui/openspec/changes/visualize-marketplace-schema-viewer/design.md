## Context

Marketplace detail currently renders request and response schema via `JsonSchemaViewer`, but that component is only a schema-labeled `CodeBlock` wrapper with empty-state handling. The existing `CodeBlock` rules in `aether-console/DESIGN.md` section 11 are still the right base for formatting, copy, and plain-text fallback, but they do not solve schema-specific comprehension for nested fields.

This proposal does not change the Discovery contract. It only changes how `requestJsonSchema` and `responseJsonSchema` are presented in `aether-console`. Because it introduces a new shared inspection pattern, `aether-console/DESIGN.md` should be updated first or alongside implementation.

## Goals / Non-Goals

**Goals:**

- Let marketplace users understand schema structure without reading raw JSON first.
- Keep the detail page compact while still making large schemas easy to inspect.
- Surface schema metadata that matters for integration work: field hierarchy, required state, type, enum values, item shape, description, and common hints such as `format`, `default`, or `nullable`.
- Preserve existing raw-text fallback and copy behavior when schema parsing is partial or impossible.

**Non-Goals:**

- No schema editing workflow.
- No backend schema validation or schema dialect normalization.
- No fully generic visual support for every JSON Schema keyword in the first iteration.
- No inference of schema from request examples or templates.

## Decisions

### 1. Use compact triggers plus an overlay viewer

Marketplace detail should keep only a compact schema summary surface inline, such as request/response cards with quick metadata and a “view schema” action. The full inspection experience should open in a desktop dialog and fall back to a full-height sheet/drawer on smaller screens.

Alternative considered: render the full tree inline. Rejected because marketplace detail already contains multiple dense sections and large schemas would dominate the page.

### 2. Model schema as a tree of display nodes

The viewer should parse common JSON Schema structure into a tree model: object properties, required lists, array `items`, scalar nodes, and common union constructs. Each node should expose a readable label row with field name/path, type badge, required badge, enum count or chips, and optional supporting text below.

### 3. Support progressive disclosure

Tree nodes should be expandable/collapsible so the viewer starts readable even for large schemas. Top-level object fields should be visible by default, while deeper nested nodes can be collapsed until needed.

### 4. Keep raw source as a fallback mode

The visual tree is best-effort. When the parser encounters unsupported keywords, ambiguous combinations, or invalid JSON, the user must still be able to inspect the raw schema text through the existing code-display pattern. This can be a raw-source tab, a secondary panel, or a toggle inside the overlay.

### 5. Scope the first iteration to marketplace detail

The component should be reusable, but the acceptance scope for this change is marketplace detail. Reuse in owner workspace can happen later once the interaction proves stable.

## Risks / Trade-offs

- [Risk] Large or deeply nested schemas can still overwhelm the viewer. -> Mitigation: use overlay presentation, default collapse for deeper nodes, and visible hierarchy indentation.
- [Risk] JSON Schema dialect differences create partial parsing. -> Mitigation: support the common keywords first and preserve a raw-source fallback for everything else.
- [Risk] The visual component drifts away from console display conventions. -> Mitigation: update `aether-console/DESIGN.md` and continue reusing `CodeBlock`, `StateBlock`, dialog, and existing badge/button patterns where appropriate.

## Open Questions

- None for proposal stage. Exact keyword coverage can be finalized during implementation, but required/type/enum/items/description support is mandatory for the first delivery.