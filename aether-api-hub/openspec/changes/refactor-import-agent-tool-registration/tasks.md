## 1. Authority Alignment

- [x] 1.1 Read `docs/spec/` development rules together with `docs/design/aehter-api-hub/Aether API Hub架构设计文档.md`, then confirm this refactor does not require new controller endpoints, API YAML updates, or SQL authority-table changes.
- [x] 1.2 Update the OpenSpec `api-import-agent-session` capability requirements to describe registry-based planner tool registration without changing the existing session/run public contract.

## 2. Planner Tool Registration Skeleton

- [x] 2.1 Introduce a planning-tool SPI in `aether-api-hub-infrastructure` so each planner tool can live in its own class and expose its own schema definition.
- [x] 2.2 Add a planner-tool registration annotation that declares stable metadata such as tool name, planner stage, and order.
- [x] 2.3 Implement a Spring-managed planner-tool registry that collects registered tool beans, groups them by stage, sorts them deterministically, and fails fast on duplicate or incomplete metadata.
- [x] 2.4 Keep planner output normalization, slot reconciliation, and validation in `ImportAgentPlannerJsonSupport` instead of moving those behaviors into the registry or individual tool classes.

## 3. Provider Refactor

- [x] 3.1 Refactor `OpenAiCompatibleImportAgentPlannerProvider` to load stage-specific tools from the registry instead of building `extract_import_facts`, `fill_import_slots`, and `submit_import_plan` schemas inline.
- [x] 3.2 Preserve the current staged orchestration, request assembly, response parsing, and `aether.import-agent.llm.tool-calling-enabled` fallback semantics while moving tool definitions out of the provider.
- [x] 3.3 Add consistency protection so tool registration metadata and the emitted function schema name cannot silently drift apart.

## 4. Verification

- [x] 4.1 Add focused tests for individual planner-tool schema definitions and for registry startup validation of duplicate names, missing metadata, and deterministic stage ordering.
- [x] 4.2 Update provider tests to prove stage-specific tool lookup still drives the same extract, fill, and submit orchestration semantics after the registration refactor.
- [x] 4.3 Add regression coverage proving the non-tool-calling fallback path still works when `aether.import-agent.llm.tool-calling-enabled` is disabled.
- [x] 4.4 Run relevant Maven tests for the planner slice using Java 17 and record any environment-related gaps that remain outside this change.