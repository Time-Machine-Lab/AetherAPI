## 1. Contract And Baseline Review

- [x] 1.1 Read `docs/spec/` backend development conventions before implementation and record any constraints that affect planner refactoring.
- [x] 1.2 Reconfirm `docs/api/api-import-agent.yaml` still matches `ApiImportAgentController.java` and that no endpoint, request, or response change is required.
- [x] 1.3 Reconfirm `docs/sql/api_import_agent_session.sql`, `docs/sql/api_import_agent_turn.sql`, and `docs/sql/api_import_agent_run.sql` need no table or column changes.
- [x] 1.4 Review existing Import Agent planner tests and identify assertions that cover current heuristic subagents, tool calling, structured output, reply streaming, and confirmation gating.

## 2. Package Structure

- [x] 2.1 Create dedicated planner subpackages for LLM adapters, orchestration runtime, agent declarations, tool declarations, contract validation, and stream helpers.
- [x] 2.2 Move `OpenAiCompatibleImportAgentPlannerProvider` and `OpenAiCompatibleImportAgentReplyPort` into the LLM adapter package while preserving Spring wiring.
- [x] 2.3 Move planning tool registry and tool definitions into the tool package without changing externally visible behavior.
- [x] 2.4 Move final plan parsing, mapping, and boundary validation support into the contract package.
- [x] 2.5 Remove obsolete flat-package imports after the new package structure compiles.

## 3. Declarative Agent Model

- [x] 3.1 Define an agent declaration model containing agent name, role, order, prompt template, allowed tools, output mode, and stream stage metadata.
- [x] 3.2 Add declaration-only agents for goal capture, observation, state estimation, gap comparison, plan control, plan synthesis, plan review, and final plan submission.
- [x] 3.3 Ensure agent declaration classes contain no Java heuristic plan mutation, ranking, guessing, schema invention, or field repair logic.
- [x] 3.4 Add tests proving agent declarations expose only the tools declared for that agent.

## 4. LLM Orchestration Runtime

- [x] 4.1 Implement an orchestration runtime that executes agent declarations in order and passes stage outputs forward as structured context.
- [x] 4.2 Adapt the OpenAI-compatible provider so each agent stage can build its request body from the common runtime context.
- [x] 4.3 Support final plan submission through the declared `submit_import_plan` tool or compatible structured content fallback.
- [x] 4.4 Emit safe status and thinking stream events for each orchestration stage without exposing raw prompts, raw provider payloads, credentials, stack traces, or chain-of-thought.
- [x] 4.5 Keep a compact failure path that converts provider errors into planner failures without creating or executing a run.

## 5. Boundary Validation And Execution Safety

- [x] 5.1 Implement the thin final plan boundary validator for JSON parseability, model mapping, collection consistency, enum compatibility, and executable-state consistency.
- [x] 5.2 Remove category scoring, async pair guessing, schema invention, auth guessing, and ad hoc plan repair from platform-side subagents.
- [x] 5.3 Ensure missing execution fields produce non-executable plans or clarification needs instead of Java-invented values.
- [x] 5.4 Verify `confirmPlan` still requires the current executable plan version.
- [x] 5.5 Verify `startRun` still rejects unconfirmed or mismatched plan versions before any category, asset, AI profile, or publish action.
- [x] 5.6 Verify executor still uses `CategoryUseCase` and `ApiAssetUseCase` rather than any LLM tool write path.

## 6. Tests

- [x] 6.1 Add unit tests for the declarative agent registry and orchestration order.
- [x] 6.2 Add unit tests for per-agent tool exposure.
- [x] 6.3 Add provider tests for multi-stage LLM request construction and final plan extraction.
- [x] 6.4 Add boundary validator tests for valid final plans, malformed JSON, unsupported enum values, and missing execution fields.
- [x] 6.5 Update application service tests to prove planning persists session and turn history without executing writes.
- [x] 6.6 Update streaming tests to prove safe stage summaries and streamed assistant messages still work.
- [x] 6.7 Keep or add regression tests proving unconfirmed plans cannot start runs.

## 7. Cleanup And Verification

- [x] 7.1 Delete replaced heuristic subagent classes and unused planner support methods.
- [x] 7.2 Remove or update tests that asserted legacy heuristic behavior instead of desired LLM orchestration behavior.
- [x] 7.3 Run targeted Import Agent unit tests.
- [x] 7.4 Run the backend module test suite required by the repository conventions.
- [x] 7.5 Run `openspec status --change refactor-import-agent-llm-orchestration` and confirm the change is apply-ready.
