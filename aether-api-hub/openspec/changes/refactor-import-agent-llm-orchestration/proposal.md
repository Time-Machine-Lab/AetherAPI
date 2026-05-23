## Why

The current API Import Agent planner mixes LLM prompts, tool schemas, local heuristic subagents, JSON normalization, validation, and execution gating in one planner package. This makes the agent workflow hard to reason about and causes planning behavior to be split between the model and scattered Java heuristics.

We should refactor the planner into a declaration-driven LLM orchestration flow: the LLM owns plan generation and review, while the platform keeps only minimal contract validation, owner-scoped access control, explicit user confirmation, and deterministic execution through existing Catalog services.

## What Changes

- Replace local heuristic planner subagents with declaration-only LLM agent specifications that define prompt, role, allowed tools, expected output, and orchestration order.
- Reorganize Import Agent planner files so LLM runtime, agent declarations, tools, contracts, and execution boundary code live in separate packages.
- Move planning responsibilities such as fact extraction, gap comparison, plan synthesis, and plan review into the LLM orchestration sequence instead of Java subagent methods.
- Keep a minimal platform boundary validator that verifies the final plan is parseable, structurally compatible with `ImportAgentPlanModel`, and safe to hand to the existing confirmation and execution flow.
- Preserve the explicit plan confirmation gate before any category, asset, AI profile, or publish action is executed.
- Preserve deterministic execution through existing `CategoryUseCase` and `ApiAssetUseCase`; the LLM must not directly create categories, write assets, attach AI profiles, or publish assets.
- Remove or replace current hard-coded heuristic behaviors such as category scoring, async pair guessing, ad hoc schema repair, and subagent-side plan mutation when they are not part of the minimal boundary contract.
- Keep the existing `docs/api/api-import-agent.yaml` and `docs/sql/api_import_agent_*.sql` contracts unchanged unless implementation discovers a required API or persistence contract change.

## Capabilities

### New Capabilities

- `api-import-agent-llm-orchestration`: Defines the LLM-driven planning workflow, declarative agent/tool model, minimal boundary validation, and preserved confirmation/execution boundaries for API Import Agent.

### Modified Capabilities

- None. The current authoritative API and SQL contracts remain compatible with the new internal orchestration design.

## Impact

- Affected backend code:
  - `aether-api-hub-standard/aether-api-hub-infrastructure/.../importagent/planner`
  - `ApiImportAgentPlannerPort` implementations and related tests
  - Import Agent stream thinking event generation
  - Planner configuration under `aether.import-agent.llm.*`
- Existing API contract:
  - `docs/api/api-import-agent.yaml` remains the authoritative contract for `ApiImportAgentController.java`.
  - No new endpoint is proposed.
- Existing SQL contracts:
  - `docs/sql/api_import_agent_session.sql`
  - `docs/sql/api_import_agent_turn.sql`
  - `docs/sql/api_import_agent_run.sql`
  - No new table or column is proposed.
- Boundary considerations:
  - The proposal intentionally keeps API Catalog as the authoritative asset write model.
  - The Import Agent remains a planning and orchestration layer, not a parallel asset write path.
  - Discovery and Unified Access continue to consume only published API Catalog assets, not Import Agent session or run metadata.
