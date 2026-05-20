## ADDED Requirements

### Requirement: Import Agent MUST proactively generate evidenced plan enrichments

The system MUST proactively generate request examples, response examples, request JSON Schema, response JSON Schema, and async task configuration when the available source material provides enough evidence. The system MUST prefer generating these fields over asking the user to manually provide backend configuration.

#### Scenario: Request and response evidence fills examples and schemas

- **WHEN** the import source contains request or response body examples, field definitions, OpenAPI schema fragments, or schema hints for an asset plan
- **THEN** the system generates or preserves `requestExample`, `responseExample`, `requestJsonSchema`, and `responseJsonSchema` for the matching asset plan when they can be represented safely
- **AND** generated schema values are valid JSON object strings or blank

#### Scenario: Insufficient evidence leaves optional enrichments blank

- **WHEN** the source material does not provide enough request or response shape evidence for an asset plan
- **THEN** the system leaves optional examples or schema fields blank
- **AND** the absence of those optional fields does not by itself make the import plan non-executable

#### Scenario: Agent does not ask for backend configuration when business information is enough

- **WHEN** required information can be derived from user-provided business details, examples, field descriptions, or documented upstream behavior
- **THEN** the system asks for that business information in Chinese only if needed
- **AND** the system does not ask the user to hand-write backend configuration objects

### Requirement: Import Agent MUST proactively assemble async task configuration

The system MUST detect submit/query async-task API patterns and assemble `asyncTaskConfig` on the submit asset when the source material provides enough evidence.

#### Scenario: Submit and query endpoints become one asset with async config

- **WHEN** the import source describes a submit endpoint and a matching task query endpoint
- **THEN** the system keeps the submit API as the primary asset plan
- **AND** the system folds the query endpoint into `asyncTaskConfig` instead of creating a second asset for the query endpoint
- **AND** the generated `queryUrlTemplate` contains the normalized `{taskId}` placeholder

#### Scenario: Partial async evidence creates targeted clarification

- **WHEN** the system can identify an async submit/query relationship but one or more required async fields remain uncertain
- **THEN** the system creates targeted Chinese clarification items for the uncertain fields
- **AND** the clarification items include recommended defaults when the system has a supported inference for those fields

### Requirement: Structured clarification items MUST support recommended defaults

The public Import Agent API MUST expose optional recommended default metadata on `ImportAgentClarificationItemResp` so guided clients can present a default answer without treating it as already confirmed.

#### Scenario: Clarification item includes default metadata

- **WHEN** the system asks a structured clarification question and has a recommended answer
- **THEN** the clarification item includes `defaultValue`
- **AND** the clarification item may include `defaultLabel`, `defaultSource`, and `defaultConfidence`
- **AND** `currentValue` remains reserved for values already present in the current plan

#### Scenario: User confirmation is still required

- **WHEN** a clarification item includes `defaultValue`
- **THEN** the system does not apply that value as a user answer until the client submits it through `clarificationAnswers`
- **AND** the refreshed plan reflects the value only after it is submitted as an answer or independently regenerated from stronger evidence

#### Scenario: Old clients remain compatible

- **WHEN** a client ignores the default metadata fields
- **THEN** the existing `clarificationQuestions`, `clarificationItems`, `currentValue`, and `clarificationAnswers` workflow continues to work
- **AND** no new required request field is introduced for Import Agent sessions or turns

### Requirement: Default values MUST be evidence-based and safe

The system MUST only return recommended defaults that are supported by source evidence or low-risk deterministic inference. The system MUST NOT generate secret values or private credential values as defaults.

#### Scenario: Safe defaults are returned with source and confidence

- **WHEN** a default value is derived from documented examples, URLs, current plan values, or deterministic async/schema inference
- **THEN** the system marks the default with a source category
- **AND** the system marks confidence as `HIGH`, `MEDIUM`, or `LOW`

#### Scenario: Secret-like values are not defaulted

- **WHEN** a field requires an actual credential, API key, token, tenant secret, or private account value
- **THEN** the system does not invent a default secret value
- **AND** the system asks the user for the relevant business information or credential source in Chinese
