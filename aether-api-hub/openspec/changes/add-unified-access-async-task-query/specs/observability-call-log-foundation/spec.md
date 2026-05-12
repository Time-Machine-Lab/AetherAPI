## ADDED Requirements

### Requirement: Async task query invocations MUST be recorded as platform call logs
The system SHALL record completed Unified Access async task query invocations as platform call logs while keeping them distinguishable from normal submit/access invocations.

#### Scenario: Record successful task query invocation
- **WHEN** a Unified Access async task query completes successfully
- **THEN** the system records one call log with the target API snapshot, caller snapshot, invocation time, duration, success result classification, and a distinguishable task query access channel or result context

#### Scenario: Record failed task query invocation
- **WHEN** a Unified Access async task query completes with a platform-side or upstream failure result
- **THEN** the system records one call log with the failure classification, error summary, and a distinguishable task query access channel or result context

#### Scenario: Do not require task lifecycle storage
- **WHEN** the system records an async task query call log
- **THEN** it does not require a platform async task table, task state history, or cached upstream result
