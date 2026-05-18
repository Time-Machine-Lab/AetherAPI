## ADDED Requirements

### Requirement: Current authenticated users MUST manage owned import agent sessions
The system MUST provide owner-scoped Import Agent session APIs in `docs/api/api-import-agent.yaml`, mapped one-to-one to `ApiImportAgentController.java`, so the current authenticated user can create, view, and continue only their own import sessions.

#### Scenario: Create an owned import session
- **WHEN** the current authenticated user submits a valid import-session creation request with a document source or import intent
- **THEN** the system creates a new import session owned by that user in a non-executing state

#### Scenario: View an owned import session
- **WHEN** the current authenticated user requests a session detail they own
- **THEN** the system returns that session's current status, latest plan snapshot reference, and visible turn history summary

#### Scenario: Reject access to another user's import session
- **WHEN** the current authenticated user requests a session owned by another user
- **THEN** the system rejects or hides that session instead of exposing another user's import workspace data

### Requirement: Import agent sessions MUST persist multi-turn planning history
The system MUST persist import session state, user/agent turn history, and current plan version in authority tables `docs/sql/api_import_agent_session.sql` and `docs/sql/api_import_agent_turn.sql`, generated with `tml-docs-spec-generate` before implementation.

#### Scenario: Append a new user turn to an owned session
- **WHEN** the current authenticated user sends follow-up constraints or clarifications to an owned import session
- **THEN** the system stores a new turn linked to that session and advances the session context version

#### Scenario: Planner updates the current structured plan
- **WHEN** the planner produces a new structured import plan for the current session
- **THEN** the system stores the new plan snapshot reference on the session without mutating API Catalog assets yet

### Requirement: Planner output MUST remain non-mutating until user confirmation
The system MUST treat planner output as a draft import plan and MUST NOT create categories, assets, AI profiles, or publish actions before the current user explicitly confirms the target plan version.

#### Scenario: Planner produces a draft plan from document input
- **WHEN** the planner successfully interprets the current session input
- **THEN** the system returns a structured draft plan describing candidate categories, assets, AI profile actions, and pending clarifications without executing write operations

#### Scenario: Planner requests clarification instead of executing
- **WHEN** the planner determines the current input is insufficient to produce a safe executable plan
- **THEN** the system keeps the session in a waiting state and returns follow-up questions or missing fields instead of creating an execution batch

#### Scenario: Reject execution before confirmation
- **WHEN** the current authenticated user tries to execute a plan version that has not been explicitly confirmed
- **THEN** the system rejects the request instead of starting import execution