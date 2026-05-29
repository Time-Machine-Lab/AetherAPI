## 1. Authority Docs First

- [x] 1.1 Read `docs/spec/Aether API HUB 后端代码开发规范文档.md` and confirm module boundary and DDD constraints before implementation.
- [x] 1.2 Use `tml-docs-spec-generate` with the API template to create `docs/api/api-import-agent.yaml`, mapped one-to-one to `ApiImportAgentController.java`, covering session create/detail/turn/confirm/execute/run-query APIs.
- [x] 1.3 Use `tml-docs-spec-generate` with the SQL template to create `docs/sql/api_import_agent_session.sql` for the import session authority table.
- [x] 1.4 Use `tml-docs-spec-generate` with the SQL template to create `docs/sql/api_import_agent_turn.sql` for the import turn authority table.
- [x] 1.5 Use `tml-docs-spec-generate` with the SQL template to create `docs/sql/api_import_agent_run.sql` for the import execution run authority table.
- [x] 1.6 Update the relevant top-level design document under `docs/design/aehter-api-hub/` to describe Planner/Executor separation, explicit confirmation gating, and reuse of API Catalog write rules.

## 2. Session And Planning Workflow

- [x] 2.1 Add owner-scoped API DTOs and response models for import session creation, session detail, turn append, plan confirmation, and run detail flows.
- [x] 2.2 Add `ApiImportAgentController` and its Web delegate, mapped only to `docs/api/api-import-agent.yaml`.
- [x] 2.3 Implement import session application service commands/models for session creation, turn append, plan refresh, and plan confirmation.
- [x] 2.4 Introduce a Planner port and a minimal planner orchestration layer that converts session context into a structured import plan snapshot without mutating API Catalog directly.
- [x] 2.5 Implement session and turn persistence so each owned session stores current plan version, latest status, and turn history references.

## 3. Deterministic Execution Orchestration

- [x] 3.1 Implement execution-batch application logic that starts only from an explicitly confirmed plan version.
- [x] 3.2 Orchestrate category ensure, asset register/revise, AI profile attach, and publish steps by reusing existing deterministic application services instead of direct persistence writes.
- [x] 3.3 Persist execution batch summaries, step results, affected asset identifiers, and failure reasons in the import run repository.
- [x] 3.4 Ensure execution batches preserve current-user ownership semantics and draft/publish lifecycle validation from API Catalog.

## 4. Boundary Protections

- [x] 4.1 Confirm Import Agent session and run metadata do not appear in Discovery list/detail responses.
- [x] 4.2 Confirm Unified Access invocation resolution and downstream execution do not depend on Import Agent session or run tables.
- [x] 4.3 Confirm Planner output cannot trigger category or asset writes before explicit user confirmation.
- [x] 4.4 Confirm Import Agent execution does not create a parallel asset write path outside existing owner-scoped catalog rules.

## 5. Verification

- [x] 5.1 Add focused adapter tests for session create/detail/turn/confirm/execute/run-query request binding and response mapping.
- [x] 5.2 Add service tests for planner gating, confirmation gating, execution orchestration, and failure recording.
- [x] 5.3 Add persistence tests for session, turn, and run round-trips, including plan-version progression and step-result storage.
- [x] 5.4 Add regression tests proving Discovery and Unified Access behavior remain unchanged when imported assets carry Import Agent history.
- [x] 5.5 Run relevant Maven tests for affected modules and record any environment-related gaps.
- [x] 5.6 Run `openspec status --change add-api-import-agent-workflow` and confirm the change is apply-ready.