# api-import-agent-existing-asset-editing Specification

## Purpose
TBD - created by archiving change add-import-agent-existing-asset-editing. Update Purpose after archive.
## Requirements
### Requirement: Import Agent MUST load owner-scoped existing asset context
The system MUST provide API Import Agent planning with current-user-owned existing asset context and MUST NOT expose another user's asset workspace through that context.

#### Scenario: Load current user asset candidates for planning
- **WHEN** the current authenticated user creates or continues an Import Agent session
- **THEN** the planner receives a bounded list of asset candidates owned by the current user

#### Scenario: Hide other users' assets from planning
- **WHEN** another user owns an asset whose code or name is similar to the user's import intent
- **THEN** the planner context excludes that other user's asset

#### Scenario: Load target asset detail when current user identifies an owned asset
- **WHEN** the current user message or current plan identifies an existing owned `apiCode`
- **THEN** the planner receives a safe detail summary for that owned asset

### Requirement: Existing asset context MUST be safe for LLM planning
The system MUST provide only safe asset summary fields to the planner and MUST NOT expose sensitive owner-only secrets in planner prompts, thinking events, or assistant replies.

#### Scenario: Redact upstream auth configuration
- **WHEN** an existing owned asset has `authConfig` configured
- **THEN** the planner context indicates that auth configuration exists without including the auth value

#### Scenario: Avoid leaking secrets in streamed thinking
- **WHEN** Import Agent emits planning or thinking events while editing an existing asset
- **THEN** those events do not include `authConfig` plaintext or provider raw payloads

### Requirement: Import Agent asset plans MUST declare explicit asset actions
The system MUST require each Import Agent asset plan to declare whether it creates a new asset, updates an existing asset, or performs an explicit upsert.

#### Scenario: Plan update for an identified existing asset
- **WHEN** the current user asks to modify an owned existing asset
- **THEN** the generated asset plan uses `UPDATE_EXISTING` for that asset

#### Scenario: Plan creation for a new asset import
- **WHEN** the current user asks to import a new API asset
- **THEN** the generated asset plan uses `CREATE` unless the user explicitly asks for upsert behavior

#### Scenario: Ask for clarification when action is ambiguous
- **WHEN** the planner cannot determine whether the user intends to create a new asset or update an existing asset
- **THEN** the plan remains non-executable and includes a clarification item for the asset action

### Requirement: Existing asset edits MUST use patch semantics
The system MUST preserve existing asset fields that are not explicitly included in the Import Agent edit plan's changed-field set.

#### Scenario: Preserve unmentioned fields during existing asset edit
- **WHEN** an `UPDATE_EXISTING` plan changes only the asset name
- **THEN** the executor updates the asset name and preserves existing upstream URL, auth configuration, schemas, examples, async task configuration, fixed upstream headers, and AI capability metadata

#### Scenario: Explicitly clear a nullable field
- **WHEN** an `UPDATE_EXISTING` plan includes a nullable field in `changedFields` with a null value
- **THEN** the executor clears that field instead of treating it as omitted

#### Scenario: Reject invalid changed field
- **WHEN** an asset plan contains a field name in `changedFields` that is not supported by the asset edit contract
- **THEN** the plan remains non-executable or execution fails before applying any unsupported field update

### Requirement: Executor MUST enforce explicit asset action semantics
The system MUST execute Import Agent asset plans according to the declared asset action while reusing existing Catalog application services for real writes.

#### Scenario: Create fails when asset already exists
- **WHEN** a confirmed `CREATE` plan targets an `apiCode` that already exists
- **THEN** execution records a failed create step and does not overwrite the existing asset

#### Scenario: Update fails when asset does not exist
- **WHEN** a confirmed `UPDATE_EXISTING` plan targets an `apiCode` that is not owned by the current user or does not exist
- **THEN** execution records a failed update step and does not create a new asset

#### Scenario: Upsert preserves compatibility
- **WHEN** a confirmed `UPSERT` plan targets an `apiCode`
- **THEN** execution updates the owned asset if it exists or creates a new asset if it does not exist

#### Scenario: Published asset critical update follows Catalog lifecycle
- **WHEN** an Import Agent edit changes critical upstream configuration of a published owned asset
- **THEN** the asset lifecycle transition is handled by the existing Catalog asset application service rules

### Requirement: Import Agent API contract MUST expose existing asset edit planning state
The system MUST define the Import Agent existing-asset edit planning contract in `docs/api/api-import-agent.yaml`, and that authority file SHALL map one-to-one to `ApiImportAgentController.java`.

#### Scenario: Generate Import Agent authority file
- **WHEN** the project updates Import Agent asset plan action, changed-field, existing-asset context, or execution-step response schemas
- **THEN** it updates `docs/api/api-import-agent.yaml` with `tml-docs-spec-generate` using the API template before code implementation

#### Scenario: Avoid SQL authority changes
- **WHEN** the project implements explicit Import Agent asset edit planning without adding persistent columns or tables
- **THEN** it does not update `docs/sql/` authority files for this change

