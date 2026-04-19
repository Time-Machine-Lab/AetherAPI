## ADDED Requirements

### Requirement: Unified access invocations MUST be recorded as platform call logs
The system MUST record one platform call log for each completed unified access invocation so that downstream log query capabilities have a stable fact source.

#### Scenario: Record a successful invocation
- **WHEN** a unified access invocation completes successfully
- **THEN** the system records one call log with the target API snapshot, caller snapshot, invocation time, duration, and success result classification

#### Scenario: Record a failed invocation
- **WHEN** a unified access invocation completes with a platform-side or upstream failure result
- **THEN** the system records one call log with the failure classification and error summary for that invocation

### Requirement: Call log storage MUST map to a single authority SQL document
The system MUST define the call log storage structure in a single top-level SQL authority document whose file name matches the table name exactly.

#### Scenario: Generate the call log SQL authority file
- **WHEN** the project generates or updates the top-level call log table design
- **THEN** it uses `tml-docs-spec-generate` with the SQL template to maintain `docs/sql/api_call_log.sql`

### Requirement: Call logs MUST capture only minimal phase-one facts
The system MUST store only the minimal fields required for phase-one log viewing, including caller identity snapshot, target API snapshot, invocation timing, duration, result classification, and error summary.

#### Scenario: Persist minimal facts without full payloads
- **WHEN** the system writes a call log in phase one
- **THEN** it stores summary fields needed for log viewing and does not require full raw request or response payload persistence

### Requirement: Call logs MUST reserve nullable AI extension fields
The system MUST allow phase-one call logs to reserve nullable fields for AI-related invocation metadata without making AI observability a separate explicit workflow.

#### Scenario: Write a normal API call without AI metadata
- **WHEN** a standard API invocation does not carry AI-specific metadata
- **THEN** the call log is stored successfully with AI extension fields left empty

#### Scenario: Write an AI-related API call with reserved metadata
- **WHEN** an invocation includes AI-related observability metadata that fits the reserved phase-one fields
- **THEN** the call log stores that metadata without changing the main logging workflow
