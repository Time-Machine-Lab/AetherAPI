## ADDED Requirements

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