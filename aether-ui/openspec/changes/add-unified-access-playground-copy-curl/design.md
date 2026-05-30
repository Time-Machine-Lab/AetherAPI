## Context

The Unified Access playground in `aether-console` already owns the form state for `apiCode`, HTTP method, API Key, optional headers, request body, target assist, subscription guidance, and invocation results. The backend contract is already defined by `docs/api/unified-access.yaml`: invocations use `/api/v1/access/{apiCode}`, task queries use the documented task path, and authentication for invocation is the `X-Aether-Api-Key` header rather than the console bearer session.

This change is frontend-only. It must follow the existing Vue 3 + TypeScript + Vite stack, keep orchestration inside composables or feature utilities, keep user-visible copy in i18n resources, and render commands through the console's established code-display/action patterns.

## Goals / Non-Goals

**Goals:**
- Generate deterministic curl commands from the current playground state.
- Support a Linux/macOS shell format and a Windows command format.
- Keep generated commands aligned with `docs/api/unified-access.yaml`, including method, path, API Key header, optional headers, and body behavior.
- Provide copy actions and feedback without changing invocation execution semantics.
- Cover escaping and body-inclusion rules with focused unit tests.

**Non-Goals:**
- No backend API changes and no changes to `docs/api/unified-access.yaml`.
- No API Key lookup, reveal, storage, or automatic credential management.
- No replacement of the existing playground invocation flow.
- No promise that a copied command bypasses subscription or owner-access enforcement.

## Decisions

### 1. Generate curl from normalized playground state in frontend logic

Decision: implement curl construction as a small typed utility or composable used by the playground composable/page, rather than placing string assembly directly in the Vue template.

Rationale: the front-end specification requires pages to orchestrate UI while domain logic lives in composables, features, or utilities. Curl generation has enough escaping and conditional behavior to merit focused tests.

Alternative considered: assemble command text inline in the page. This is simpler initially but makes escaping and Windows/Linux differences harder to test and maintain.

### 2. Treat shell format as an explicit option

Decision: expose two generated variants: `linux` and `windows`. The Linux/macOS variant uses backslash line continuation and single-quote-oriented escaping. The Windows variant uses caret line continuation and double-quote-oriented escaping suitable for common Windows command prompts.

Rationale: the user specifically needs both Windows and Linux formats, and making the format explicit avoids guessing from browser platform. Users may copy a command for a different environment than the one running the console.

Alternative considered: auto-detect the user's platform. Detection is brittle, can be misleading for WSL or remote terminals, and does not satisfy cross-environment handoff well.

### 3. Respect request-body rules already used by invocation

Decision: include `--data` only for methods whose playground invocation path allows a body, and omit the body for `GET` and `DELETE` even if stale body text exists in local state.

Rationale: the archived playground spec already requires no-body methods to hide or disable request body editing and block body payload submission. Copied commands must match that behavior.

Alternative considered: always include the visible body field when non-empty. That would drift from the invocation semantics and could generate commands the playground itself would not send.

### 4. Keep credentials user-controlled and visible only as current form input

Decision: the command uses the API Key value currently present in the form and never attempts to retrieve, persist, or infer a key from subscription state or account APIs.

Rationale: the existing API Key security model treats complete API keys as sensitive and not recoverable from the platform after creation. Copy-curl must not weaken that boundary.

Alternative considered: generate a placeholder key whenever the field is empty. A placeholder is useful as feedback, but the primary copy action should be disabled or clearly invalid until required command inputs are present so users do not copy a command that looks ready but cannot run.

### 5. Render commands as code display with adjacent actions

Decision: show generated command previews in the playground using the existing code-display style and action buttons, with format selection close to the preview.

Rationale: `aether-console/DESIGN.md` defines code/data display through `CodeBlock`-style surfaces and copy feedback through action semantics. This keeps the feature consistent with JSON/result rendering.

Alternative considered: provide only two copy buttons without preview. That is compact, but users often need to inspect the URL, headers, and body before copying terminal commands containing credentials.

## Risks / Trade-offs

- [Shell escaping edge cases] Complex header/body strings can contain shell-sensitive characters. -> Centralize escaping in a tested formatter and prefer conservative quoting rules for each format.
- [Credential exposure in previews] Showing the full API Key inside a curl preview makes sensitive data more visible. -> Render the preview only from the user's current explicit input, keep it scoped to the playground, and avoid persisting command text.
- [Base URL ambiguity] Frontend environments can run behind different hosts or proxies. -> Build commands from the same configured Unified Access base path/origin used by the standalone Unified Access client, without hardcoding production domains.
- [Clipboard API failure] Browser permissions or insecure contexts can block clipboard writes. -> Show internationalized failure feedback and keep the preview selectable so users can manually copy.
