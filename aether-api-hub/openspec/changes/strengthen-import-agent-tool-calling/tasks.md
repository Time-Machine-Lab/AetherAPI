## 1. Authority Alignment

- [x] 1.1 Read `docs/api/api-import-agent.yaml`, `docs/api/api-asset-management.yaml`, `docs/sql/api_import_agent_session.sql`, `docs/sql/api_import_agent_turn.sql`, and `docs/sql/api_import_agent_run.sql`, then confirm this change does not require new controller endpoints or new authority tables.
- [x] 1.2 Update the OpenSpec session capability requirements to describe staged tool calling, slot filling, and planner field normalization without changing the existing public contract.

## 2. Planner Tool-Calling Hardening

- [x] 2.1 Refactor `OpenAiCompatibleImportAgentPlannerProvider` to expose the staged planner tools `extract_import_facts`, `fill_import_slots`, and `submit_import_plan` instead of the current single loose submit tool.
- [x] 2.2 Add the strict schema matrix for root, category plan, asset plan, async-task config, and AI profile objects, including `required`, `enum`, `additionalProperties: false`, and conditional rules.
- [x] 2.3 Introduce planner-side slot-filling / reconciliation logic so missing fields are first recovered from `documentSummary`, `currentPlan`, and recent turns before user clarification is emitted.
- [x] 2.4 Keep `ImportAgentPlannerJsonSupport` as the deterministic normalization and fallback-validation layer instead of the sole place where required planning constraints are enforced.
- [x] 2.5 Preserve compatibility for vendors that only partially support JSON Schema conditionals by applying the same constraint matrix again during deterministic response reconciliation.

## 3. Skill Alignment

- [x] 3.1 Align planner output conventions with `.codex/skills/batch-import-api-skill/SKILL.md`, especially for `authConfig`, `requestJsonSchema`, `responseJsonSchema`, `assetType`, `aiProfile`, and async query folding rules.
- [x] 3.2 Ensure planner output never invents unsupported auth JSON structures such as `secretRef`, `headerName`, or `queryParamName` objects when the existing backend expects plain-string `authConfig`.

## 4. Verification

- [x] 4.1 Add focused planner-provider tests for missing authConfig, async query fields, AI profile fields, and partial patch preservation.
- [x] 4.2 Add tests proving planner slot-filling can recover missing values from currentPlan or latest user answers before returning clarificationQuestions.
- [x] 4.3 Add regression coverage for the non-tool-calling path so disabling `aether.import-agent.llm.tool-calling-enabled` does not break the existing planning workflow.
- [x] 4.4 Add tests for staged tool sequencing so extraction and slot-filling failures do not silently skip into a malformed final submit.
- [x] 4.5 Run relevant Maven tests for `aether-api-hub-standard` planner, service, and adapter slices and record any environment-related gaps.