## 1. Authority Alignment

- [x] 1.1 Read `docs/spec/` development rules together with `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`, `docs/api/api-import-agent.yaml`, and `docs/sql/api_import_agent_*.sql`, then confirm this change does not require new controller endpoints, response fields, or authority-table changes.
- [x] 1.2 Update the OpenSpec `api-import-agent-session` capability requirements to describe planner-internal subagent orchestration while preserving the existing session/run public contract.

## 2. Internal Subagent Skeleton

- [x] 2.1 Introduce a planner-internal subagent SPI in `aether-api-hub-infrastructure` for narrow planner roles such as document facts, auth recognition, async-pattern recognition, review, and clarification strategy.
- [x] 2.2 Add a planner subagent orchestrator that invokes registered internal subagents in deterministic order and keeps subagent collaboration inside the infrastructure planner layer.
- [x] 2.3 Keep `ApiImportAgentPlannerPort` and `ProviderBackedApiImportAgentPlanner` returning a single final planner result so service and adapter layers do not need to understand multiple planner roles.

## 3. Merge And Review Rules

- [x] 3.1 Implement merge rules so high-confidence facts from internal subagents can be combined into one candidate draft plan without silently overwriting stronger existing evidence from `currentPlan`, `documentSummary`, or recent turns.
- [x] 3.2 Add review rules so conflicting or low-confidence subagent outputs are downgraded to clarification, omission, or existing-value preservation instead of flowing straight into the final draft plan.
- [x] 3.3 Reuse existing deterministic normalization and validation paths in `ImportAgentPlannerJsonSupport` instead of duplicating final-plan fixup logic inside each subagent.

## 4. Planner Provider Integration

- [x] 4.1 Refactor `OpenAiCompatibleImportAgentPlannerProvider` so staged tool-calling can delegate specialized reasoning slices to the internal subagent orchestrator while still producing one unified plan candidate.
- [x] 4.2 Preserve the current tool-calling flow, non-tool-calling fallback behavior, and confirmation-before-execution semantics while subagent orchestration is introduced.
- [x] 4.3 Ensure non-critical subagent failure degrades to a safe unified planner result or targeted clarification instead of failing the whole session-planning request.

## 5. Verification

- [x] 5.1 Add focused tests for subagent registration, deterministic ordering, and orchestrator merge behavior.
- [x] 5.2 Add regression tests covering conflicting subagent outputs, low-confidence field suppression, and fallback-to-clarification behavior.
- [x] 5.3 Update planner provider tests to prove service-facing behavior still returns one unified plan result after internal subagents are introduced.
- [x] 5.4 Run relevant planner, service, and adapter Maven tests with Java 17 and record any environment-related gaps outside this change.