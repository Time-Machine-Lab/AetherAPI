# api-import-agent-session Specification

## Purpose
TBD - created by archiving change refactor-import-agent-tool-registration. Update Purpose after archive.
## Requirements
### Requirement: Planner tool definitions MUST be registered as stage-scoped components before tool-calling orchestration runs
When Import Agent tool calling is enabled, the planner MUST obtain available planning tools from a registry built from registered tool components, instead of depending on provider-local hardcoded tool definitions.

#### Scenario: Build stage-specific tool lists from registered planner tools
- **WHEN** the planner enters a stage such as `EXTRACT_FACTS`, `FILL_SLOTS`, or `SUBMIT_PLAN`
- **THEN** it MUST load that stage's tools from the registered planning-tool set in stable order instead of hand-assembling the tool list inside the provider

#### Scenario: Reject duplicate tool names during startup
- **WHEN** two registered planning tools declare the same tool name
- **THEN** the planner configuration MUST fail fast during registry initialization instead of leaving the duplicate unresolved until a planning request runs

#### Scenario: Reject missing stage metadata during startup
- **WHEN** a registered planning tool is missing required registration metadata such as tool name or stage
- **THEN** the planner configuration MUST fail fast during startup instead of accepting an incomplete tool definition

### Requirement: Planner tool schema ownership MUST be independent from provider orchestration
The system MUST allow each planning tool to own and evolve its schema definition independently while keeping `OpenAiCompatibleImportAgentPlannerProvider` focused on staged orchestration, request assembly, LLM invocation, and response parsing.

#### Scenario: Change a single tool schema without redesigning provider-local tool builders
- **WHEN** a planner tool needs to add, remove, or tighten fields in its function schema
- **THEN** that schema change MUST be localized to the tool definition and registry path instead of requiring provider-local hardcoded builder methods for every tool

#### Scenario: Keep deterministic plan reconciliation outside tool registration
- **WHEN** the planner receives tool-calling output that still needs merge, normalize, or validation work
- **THEN** deterministic reconciliation MUST remain in the existing plan-normalization path instead of being moved into individual tool registration components

### Requirement: Planner tool registration MUST preserve the current external planner contract
The system MUST preserve the current staged planner behavior, current feature gate, and current draft-plan contract while refactoring tool registration internals.

#### Scenario: Preserve staged tool-calling behavior after registration refactor
- **WHEN** tool calling is enabled for Import Agent planning
- **THEN** the planner MUST continue to expose the same stage-aligned tools and produce the same draft-plan contract semantics as before the registration refactor

#### Scenario: Preserve the non-tool-calling fallback path
- **WHEN** `aether.import-agent.llm.tool-calling-enabled` is disabled
- **THEN** the planner MUST continue to use the existing non-tool-calling fallback path without requiring registered planner tools to execute that request

### Requirement: Current authenticated users MUST manage owned import agent sessions
The system MUST provide owner-scoped Import Agent session APIs in `docs/api/api-import-agent.yaml`, mapped one-to-one to `ApiImportAgentController.java`, so the current authenticated user can create, view, and continue only their own import sessions.

#### Scenario: Create an owned import session
- **WHEN** the current authenticated user submits a valid import-session creation request with `importIntent` and optional `documentSource` or `documentSummary`
- **THEN** the system creates a new import session owned by that user in a non-executing state

#### Scenario: View an owned import session
- **WHEN** the current authenticated user requests a session detail they own
- **THEN** the system returns that session's current status, latest full plan snapshot, and visible turn history summary

#### Scenario: Reject access to another user's import session
- **WHEN** the current authenticated user requests a session owned by another user
- **THEN** the system rejects or hides that session instead of exposing another user's import workspace data

### Requirement: Import agent sessions MUST persist multi-turn planning history
The system MUST persist import session state, user/agent turn history, and current plan version in authority tables `docs/sql/api_import_agent_session.sql` and `docs/sql/api_import_agent_turn.sql`, generated with `tml-docs-spec-generate` before implementation.

#### Scenario: Append a new user turn to an owned session
- **WHEN** the current authenticated user sends follow-up constraints or clarifications to an owned import session
- **THEN** the system stores a new turn linked to that session and advances the session context version

#### Scenario: Planner updates the current structured plan
- **WHEN** the planner produces a new structured import plan for the current session
- **THEN** the system stores the new full plan snapshot on the session without mutating API Catalog assets yet

### Requirement: Current authenticated users MUST access streamed planning endpoints for session create and append flows
The system MUST provide stream variants of import-session creation and turn append in `docs/api/api-import-agent.yaml`, mapped to `ApiImportAgentController.java`, so the current authenticated user can receive planning status, assistant deltas, and the final session snapshot without changing owner-scoped semantics.

#### Scenario: Create an owned import session through SSE
- **WHEN** the current authenticated user calls the stream create-session endpoint with a valid request
- **THEN** the system emits planning status events, assistant message deltas when available, and the final owned session snapshot

#### Scenario: Append a user turn through SSE
- **WHEN** the current authenticated user calls the stream append-turn endpoint for a session they own
- **THEN** the system emits planning status events, assistant message deltas when available, and the refreshed owned session snapshot

### Requirement: Planner output MUST remain non-mutating until user confirmation
The system MUST treat planner output as a draft import plan and MUST NOT create categories, assets, AI profiles, or publish actions before the current user explicitly confirms the target plan version.

#### Scenario: Planner produces a draft plan from document input
- **WHEN** the planner successfully interprets the current session input
- **THEN** the system returns a structured draft plan describing candidate categories, assets, AI profile actions, and pending clarifications without executing write operations

#### Scenario: Planner requests clarification instead of executing
- **WHEN** the planner determines the current input is insufficient to produce a safe executable plan
- **THEN** the system keeps the session in a waiting state and returns follow-up questions or missing fields instead of creating an execution batch

#### Scenario: Reject execution before confirmation
- **WHEN** the current authenticated user tries to execute a plan version that has not been explicitly confirmed
- **THEN** the system rejects the request instead of starting import execution

