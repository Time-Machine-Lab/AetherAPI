## ADDED Requirements

### Requirement: Confirmed import plans MUST execute through deterministic backend workflows
The system MUST execute a confirmed import plan by orchestrating deterministic category and asset application services, and MUST NOT allow the planner layer to write import results directly into persistence.

#### Scenario: Execute a confirmed import plan successfully
- **WHEN** the current authenticated user confirms a valid import plan version and starts execution
- **THEN** the system creates an execution batch and performs the planned category, asset, AI profile, and publish actions through deterministic backend workflows

#### Scenario: Import execution preserves owner attribution
- **WHEN** the system creates or revises assets during import execution
- **THEN** each affected asset is created or updated as owned by the current authenticated user instead of a platform-level shadow owner

#### Scenario: Import execution rejects non-owner execution request
- **WHEN** a user attempts to execute a plan for a session they do not own
- **THEN** the system rejects the request instead of starting the batch

### Requirement: Import execution MUST persist step-level run history
The system MUST persist execution batch state, step results, and failure summaries in `docs/sql/api_import_agent_run.sql`, generated with `tml-docs-spec-generate` before implementation, so users can inspect what happened after planner confirmation.

#### Scenario: Record successful execution steps
- **WHEN** an import batch completes category and asset steps successfully
- **THEN** the system stores the batch outcome, step history, and affected asset identifiers for later inspection

#### Scenario: Record partial failure without losing completed steps
- **WHEN** an import batch fails after some planned steps have already succeeded
- **THEN** the system stores the failed step, failure reason, and previously completed step results instead of collapsing the batch into an opaque error

#### Scenario: Query a prior execution batch
- **WHEN** the current authenticated user requests a batch they own
- **THEN** the system returns that batch's status, step outcomes, and asset/result summary

### Requirement: Import execution MUST NOT change Discovery or Unified Access read models directly
The system MUST rely on existing publish and asset lifecycle behavior to make imported assets discoverable or callable, and generic import-session or run metadata MUST NOT appear in Discovery or Unified Access responses.

#### Scenario: Unpublished imported asset remains absent from Discovery
- **WHEN** an import batch creates or revises an asset but does not publish it successfully
- **THEN** Discovery and Unified Access continue to exclude that asset according to existing published-only rules

#### Scenario: Import run metadata stays out of market and access responses
- **WHEN** an imported asset becomes discoverable or callable through existing publish behavior
- **THEN** Discovery and Unified Access responses still exclude import-session and execution-batch metadata