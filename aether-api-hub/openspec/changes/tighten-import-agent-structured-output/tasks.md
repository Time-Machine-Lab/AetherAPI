## 1. Tighten Planner Contract

- [x] 1.1 Update Import Agent planner tool/response schema so execution-critical fields use explicit enum and conditional constraints, and undeclared fields are rejected or ignored.
- [x] 1.2 Align planner prompts/provider behavior with the constrained schema so missing execution-critical fields become clarificationQuestions instead of implicit free-text plan state.

## 2. Refactor Planning Assembly Boundaries

- [x] 2.1 Reorganize Import Agent plan assembly into explicit parse, merge, async-normalize, and validate phases without crossing existing DDD module boundaries.
- [x] 2.2 Remove free-text-driven promotion of execution-critical fields so only declared structured fields plus deterministic current-plan merge can make a plan executable.

## 3. Reduce High-Risk Inference

- [x] 3.1 Demote automatic inference for auth and async configs to compatibility-only normalization instead of primary executable-plan assembly.
- [x] 3.2 Ensure publishable AI asset drafts require explicit structured `aiProfile.provider` and `aiProfile.model` instead of relying on free-text inference.

## 4. Verify Planner Behavior

- [x] 4.1 Add regression tests for schema drift / wrong-field enum values and missing execution-critical fields so malformed planner output stays non-executable with targeted clarification questions.
- [x] 4.2 Add regression tests for partial structured updates preserving current-plan values without re-inferring unrelated prose.