## Context

The current API Import Agent is already modeled as an owner-scoped session, turn, plan confirmation, and run workflow. The authoritative API contract is `docs/api/api-import-agent.yaml`, mapped to `ApiImportAgentController.java`. The authoritative persistence contracts are `docs/sql/api_import_agent_session.sql`, `docs/sql/api_import_agent_turn.sql`, and `docs/sql/api_import_agent_run.sql`.

The existing design in `docs/design/aehter-api-hub/Aether API Hub API Catalog领域设计文档.md` requires Import Agent to stay in front of API Catalog as a controlled orchestration layer. Planner output is a plan snapshot, not a direct write path. Executor must continue to reuse `CategoryUseCase` and `ApiAssetUseCase`, and Discovery / Unified Access must continue to consume only API Catalog assets.

The problem is inside the planner implementation: the current `infrastructure.importagent.planner` package mixes remote LLM calls, tool schema construction, local heuristic subagents, JSON parsing, draft merging, validation, defaulting, and stream event generation. Several subagents now contain business-looking heuristics such as category scoring, async pair guessing, schema repair, and clarification mutation. This makes it hard to know whether a planning result came from the LLM, a local subagent, a parser fallback, or a validator.

This change does not require API or SQL contract changes. If implementation later discovers that `docs/api/` or `docs/sql/` must change, those top-level documents must be regenerated through `tml-docs-spec-generate` before code changes proceed.

## Goals / Non-Goals

**Goals:**

- Make the Import Agent planning workflow LLM-driven from observation through final plan synthesis.
- Replace Java heuristic subagents with declaration-only agent specifications containing role, prompt, allowed tools, expected output contract, and order.
- Move tool definitions into a dedicated tool package and make them declarative where possible.
- Reorganize planner code so LLM provider adapters, orchestration runtime, agent declarations, tools, and boundary contracts have clear package boundaries.
- Preserve minimal platform-owned safety checks: parseability, structural contract compatibility, owner-scoped access control, explicit plan confirmation, and deterministic execution.
- Preserve current API and SQL contracts unless a later implementation finding proves a top-level contract update is required.

**Non-Goals:**

- No new HTTP endpoint, table, column, or public response field is introduced by this change.
- No direct LLM write access to categories, assets, AI profiles, or publish actions.
- No removal of owner-scoped access control.
- No removal of explicit user confirmation before execution.
- No general-purpose multi-agent framework outside API Import Agent.
- No raw chain-of-thought streaming; stream events remain safe process summaries.

## Decisions

### Decision 1: Use a declaration-driven LLM orchestration runtime

The planner will be reorganized around a runtime that executes ordered `AgentSpec` declarations. Each agent declaration contains a stable name, role, prompt template, allowed tool names, output mode, and stage metadata for safe stream summaries.

The runtime, not each agent class, owns prompt assembly, provider calls, tool wiring, response parsing, retries if supported, and stream event emission. Agent declaration classes must not contain local correction logic or mutation heuristics.

Alternative considered: Keep the existing subagent classes and delete only the worst heuristics.

Why not: That leaves the ambiguous ownership problem in place. A future reader would still have to inspect every subagent method to know whether planning is model-driven or Java-driven.

### Decision 2: Model the workflow as a control loop

The planner will follow a cybernetic control flow:

1. Goal agent: capture the user's desired import outcome and constraints.
2. Observation agent: gather supplied document summary, current plan, recent turns, and platform facts through tools.
3. State estimation agent: identify API assets, auth, request/response examples, async task behavior, AI profile hints, and uncertainty.
4. Gap comparator agent: compare the estimated state with the minimum executable plan contract.
5. Plan controller agent: decide whether to ask clarification questions or continue to plan synthesis.
6. Plan synthesis agent: generate the full import plan JSON.
7. Plan review agent: ask the LLM to review consistency, risk, missing fields, and user intent alignment.
8. Final plan agent: produce the final plan payload through `submit_import_plan`.

This keeps the LLM in charge of planning while still making the feedback loop explicit and observable.

Alternative considered: Single prompt that directly emits the final plan.

Why not: A single prompt is simpler but makes it harder to inspect failure points, stream meaningful progress, and tune feedback stages independently.

### Decision 3: Keep a thin platform boundary validator

The final LLM result must pass a minimal boundary validator before it becomes an `ImportAgentPlanModel`. The validator is not a heuristic planner. It only verifies that:

- A JSON object was returned.
- The payload can be mapped to the Import Agent plan model.
- Required collection fields are represented consistently.
- Enum-like values are either recognized or left as clarification needs rather than silently rewritten into risky values.
- The plan's executable flag is consistent with missing required execution fields.

The validator must not perform category ranking, async pair discovery, schema invention, or auth guessing.

Alternative considered: Trust the LLM output completely.

Why not: The platform still needs a typed plan snapshot before it can persist, display, confirm, and execute safely.

### Decision 4: Preserve confirmation and deterministic execution

Execution remains gated by `confirmed_plan_version`. `startRun` must continue to reject unconfirmed or non-executable plan versions. Once confirmed, the executor continues to call existing application services in fixed order: ensure category, register or revise asset, attach AI profile, and publish when requested.

Alternative considered: Auto-execute when the final LLM plan is executable.

Why not: Import Agent can publish user-owned assets and configure upstream credentials. Human confirmation is the control loop's final actuator approval and must remain explicit.

### Decision 5: Separate packages by responsibility

The planner package should be split into responsibility-focused packages, for example:

- `planner.llm`: OpenAI-compatible provider and streamed reply adapter.
- `planner.orchestration`: runtime, agent executor, orchestration context, stage result model.
- `planner.agents`: declaration-only agent specs.
- `planner.tools`: tool declarations and tool registry.
- `planner.contract`: final plan parser, model mapper, and boundary validator.
- `planner.stream`: safe stage summary helpers if needed.

Exact class names may vary during implementation, but the package boundaries must preserve the same ownership.

Alternative considered: Keep all files in the existing planner package and rely on naming.

Why not: The current package already demonstrates that naming alone is not enough to prevent mixed responsibilities.

## Risks / Trade-offs

- [LLM output becomes more central] -> Keep typed final plan parsing, minimal boundary validation, tests for malformed provider output, and explicit confirmation.
- [More LLM calls can increase latency and cost] -> Make stages configurable and allow a compact orchestration mode while keeping the same agent contract.
- [Removing local heuristics may reduce auto-fill quality at first] -> Shift that behavior into prompts and tools, then measure plan completeness through tests and sample import cases.
- [Tool-calling provider compatibility varies] -> Keep OpenAI-compatible payload handling isolated in `planner.llm` and preserve content fallback where compatible with the selected output mode.
- [Stream events may expose too much internal detail] -> Continue emitting only safe status and thinking summaries, never raw prompts, raw provider payloads, credentials, or chain-of-thought.

## Migration Plan

1. Introduce new package structure and declaration/runtime types alongside the existing planner.
2. Implement the LLM orchestration runtime and map it behind the existing `ApiImportAgentPlannerPort`.
3. Port existing tools into the dedicated tool package and reduce them to declarative tool definitions plus execution callbacks where needed.
4. Replace heuristic subagents with declaration-only agent specs.
5. Introduce the thin final plan boundary validator and remove Java heuristic repair paths.
6. Update tests to verify planner flow, package boundaries, malformed output handling, confirmation gating, and deterministic execution.
7. Remove obsolete planner classes after the new provider path is covered by tests.

Rollback strategy: keep the public port interface stable. If the new orchestration fails in deployment, configuration can route back to the previous provider implementation until the obsolete implementation is removed.

## Open Questions

- Should the first implementation always run all agent stages, or should compact mode skip directly from observation to plan synthesis for simple requests?
- Should agent declarations be Java records/classes only, or should prompts move to resource files for easier non-code iteration?
- Should plan review failure produce clarification questions, a non-executable plan, or a hard planner error when the LLM identifies high-risk ambiguity?
