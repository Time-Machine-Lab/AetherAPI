# api-import-agent-llm-orchestration Specification

## Purpose
TBD - created by archiving change refactor-import-agent-llm-orchestration. Update Purpose after archive.
## Requirements
### Requirement: LLM-driven planning sequence
The system SHALL generate Import Agent plans through an ordered LLM orchestration sequence that covers goal capture, observation, state estimation, gap comparison, plan control, plan synthesis, plan review, and final plan submission.

#### Scenario: Planning starts from a new import session
- **WHEN** a current user creates an Import Agent session with an import intent and optional document context
- **THEN** the planner runs the LLM orchestration sequence and returns a session with a current plan version or a planner failure

#### Scenario: Planning refines an existing session
- **WHEN** a current user appends a turn or clarification answers to an existing Import Agent session
- **THEN** the planner includes the current plan, recent turns, and new user input in the LLM orchestration sequence before creating the next plan version

### Requirement: Declarative agent specifications
The system SHALL represent planner agents as declarations of role, prompt, allowed tools, expected output mode, and orchestration order. Agent declarations MUST NOT contain Java heuristic mutation, ranking, guessing, or repair logic.

#### Scenario: Agent declaration is loaded
- **WHEN** the planner builds the orchestration sequence
- **THEN** each stage is resolved from an agent declaration rather than from a class that mutates the plan with local heuristics

#### Scenario: Agent uses tools through runtime
- **WHEN** an agent stage needs platform facts or final plan submission
- **THEN** the orchestration runtime exposes only that agent's declared tools to the LLM provider

### Requirement: Dedicated planner package boundaries
The system SHALL separate LLM provider adapters, orchestration runtime, agent declarations, tool declarations, and final plan contract handling into dedicated planner packages.

#### Scenario: Planner code is organized by responsibility
- **WHEN** a developer inspects Import Agent planner source files
- **THEN** LLM adapter code, orchestration runtime code, agent declaration code, tool declaration code, and boundary contract code are located under separate package paths

### Requirement: Minimal final plan boundary validation
The system SHALL validate the final LLM plan only at the platform boundary for parseability, structural compatibility with the Import Agent plan model, executable-state consistency, and safe mapping into persisted plan snapshots.

#### Scenario: Final plan is structurally valid
- **WHEN** the LLM orchestration submits a final plan that can be parsed and mapped to the Import Agent plan model
- **THEN** the system persists the plan snapshot as the session's current plan version

#### Scenario: Final plan is malformed
- **WHEN** the LLM orchestration submits non-JSON content or a payload that cannot be mapped to the Import Agent plan model
- **THEN** the system rejects the planner result without executing any category, asset, AI profile, or publish action

#### Scenario: Final plan lacks execution fields
- **WHEN** the final plan is missing fields required for execution
- **THEN** the system returns a non-executable plan with clarification needs instead of silently inventing missing execution values

### Requirement: No heuristic plan mutation in platform subagents
The system SHALL remove platform-side heuristic plan mutation from Import Agent subagents, including category scoring, async pair guessing, schema invention, auth guessing, and ad hoc field repair.

#### Scenario: Ambiguous category remains an LLM or user decision
- **WHEN** the planner cannot confidently determine a category from the LLM-generated plan and available platform facts
- **THEN** the final plan remains non-executable or asks for clarification instead of applying Java category scoring

#### Scenario: Ambiguous async task shape remains unresolved
- **WHEN** the planner cannot confidently merge submit and query endpoints into an async task configuration
- **THEN** the final plan remains non-executable or asks for clarification instead of applying Java async pair guessing

### Requirement: Confirmation gate remains mandatory
The system SHALL require explicit confirmation of the current plan version before starting an Import Agent run.

#### Scenario: Start run without confirmation
- **WHEN** a current user requests a run for a plan version that is not the session's confirmed plan version
- **THEN** the system rejects the run request and performs no category, asset, AI profile, or publish action

#### Scenario: Start run after confirmation
- **WHEN** a current user confirms an executable plan version and then starts a run for that same version
- **THEN** the system starts deterministic execution through existing application services

### Requirement: LLM cannot directly execute catalog writes
The system SHALL prevent LLM agents and tools from directly creating categories, registering assets, revising assets, attaching AI profiles, or publishing assets. All such writes MUST remain in the deterministic executor after plan confirmation.

#### Scenario: Planner produces executable plan
- **WHEN** the LLM orchestration produces an executable final plan
- **THEN** the system saves the plan for confirmation and does not perform catalog writes during planning

#### Scenario: Executor applies confirmed plan
- **WHEN** a confirmed plan is executed
- **THEN** the executor uses existing `CategoryUseCase` and `ApiAssetUseCase` behavior rather than an LLM tool write path

### Requirement: Safe stream summaries
The system SHALL emit only safe Import Agent stream summaries during LLM orchestration and MUST NOT stream raw prompts, raw provider payloads, credentials, stack traces, or chain-of-thought.

#### Scenario: Streaming planning progress
- **WHEN** a streaming session or turn request runs the LLM orchestration sequence
- **THEN** the stream may emit status, thinking, message, session, error, and done events without exposing raw LLM internals

