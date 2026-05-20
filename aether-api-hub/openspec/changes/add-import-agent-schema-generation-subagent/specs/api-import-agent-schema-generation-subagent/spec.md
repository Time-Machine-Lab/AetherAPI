## ADDED Requirements

### Requirement: Import Agent MUST use a dedicated schema generation subagent

The system MUST include a dedicated internal Import Agent subagent for generating and normalizing request and response JSON Schema snapshots. This subagent MUST be separate from generic document fact extraction, auth recognition, async-pattern recognition, plan review, and clarification strategy.

#### Scenario: Schema subagent is part of planner orchestration

- **WHEN** Import Agent planner subagents are orchestrated for a planning request
- **THEN** the system includes a dedicated schema generation subagent in the internal orchestration
- **AND** the subagent updates only `requestJsonSchema` and `responseJsonSchema` fields for matching asset plans
- **AND** the subagent does not create new asset plans or change auth, async-task, AI profile, category, method, or upstream URL fields

#### Scenario: Existing schemaHints are treated as input only

- **WHEN** `extract_import_facts` returns `schemaHints`
- **THEN** the system treats those hints as optional input evidence for the schema generation subagent
- **AND** the system does not treat `schemaHints` alone as an already completed schema generation step

### Requirement: Generated request and response schemas MUST be valid JSON object strings

The system MUST ensure that Import Agent draft-plan `requestJsonSchema` and `responseJsonSchema` values are either blank or valid JSON object strings before they can be accepted into the refreshed plan.

#### Scenario: JSON object schema is serialized to a string

- **WHEN** the schema generation subagent or planner stage produces `requestJsonSchema` or `responseJsonSchema` as a JSON object
- **THEN** the system serializes that object into a JSON string in the draft plan
- **AND** the resulting string can be parsed back into a JSON object

#### Scenario: JSON string schema is accepted

- **WHEN** the schema generation subagent or planner stage produces `requestJsonSchema` or `responseJsonSchema` as a string containing a valid JSON object
- **THEN** the system preserves the schema as a normalized JSON object string
- **AND** the field remains available for the later asset registration or revision command

#### Scenario: Invalid schema text is rejected

- **WHEN** the schema generation subagent or planner stage produces Markdown, natural language, example payload text, malformed JSON, or a JSON value that is not an object for `requestJsonSchema` or `responseJsonSchema`
- **THEN** the system MUST NOT persist that value into the refreshed draft plan
- **AND** the system keeps any previously valid current-plan schema value for the same field when one exists

### Requirement: Schema generation MUST be evidence-based and optional

The system MUST generate request and response JSON Schema snapshots only from explicit evidence such as documented field definitions, request examples, response examples, existing current-plan schema values, or schema hints.

#### Scenario: Request and response examples produce basic schemas

- **WHEN** the source material contains a request example or response example with a JSON object body
- **THEN** the schema generation subagent may produce a basic JSON Schema object using `type`, `properties`, and related common JSON Schema keywords
- **AND** the generated schema is written to the matching `requestJsonSchema` or `responseJsonSchema` field as a valid JSON object string

#### Scenario: Insufficient evidence leaves schemas blank

- **WHEN** the source material does not contain enough request or response shape evidence for an asset
- **THEN** the system leaves the corresponding schema field blank
- **AND** the absence of that optional schema field does not by itself make the import plan non-executable

#### Scenario: Invalid attempted schema can trigger Chinese clarification

- **WHEN** the planner attempted to provide a schema but the system rejects it as invalid
- **THEN** the system may return a Chinese clarification question asking for a request example, response example, or field description
- **AND** the clarification MUST describe the needed business information rather than asking the user to hand-write backend configuration

### Requirement: Plan review MUST protect schema fields from invalid overwrites

The system MUST review generated schema fields before accepting a draft plan and MUST avoid replacing a valid existing schema with an invalid or lower-confidence value.

#### Scenario: Invalid new schema does not overwrite existing valid schema

- **WHEN** the current plan has a valid `requestJsonSchema` or `responseJsonSchema`
- **AND** a later planner or subagent output provides an invalid value for the same field
- **THEN** the system keeps the existing valid schema value
- **AND** the invalid value is omitted from the refreshed plan

#### Scenario: Review diagnostics record schema normalization

- **WHEN** plan review changes or rejects schema fields during subagent orchestration
- **THEN** the system records review diagnostics consistent with existing planner review diagnostics
- **AND** those diagnostics do not expose hidden provider payloads to public Import Agent API responses
