## ADDED Requirements

### Requirement: Planner Receives Enabled Category Candidates

Import Agent planning SHALL receive the current enabled API category candidates before generating or refining an import plan.

#### Scenario: New session planning has category candidates

- **WHEN** a user starts an Import Agent session
- **THEN** the application service SHALL query enabled categories and include them in the planner request

#### Scenario: Follow-up planning has refreshed category candidates

- **WHEN** a user appends a follow-up message to an Import Agent session
- **THEN** the application service SHALL query enabled categories again and include them in the planner request

#### Scenario: Planner does not access category persistence directly

- **WHEN** planner subagents classify an asset
- **THEN** they SHALL use category candidates from the planner request instead of querying database repositories or category use cases directly

### Requirement: Category Classification Subagent Selects Existing Categories

Import Agent SHALL provide a dedicated category classification subagent that fills missing `assetPlans[].categoryCode` only from enabled category candidates.

#### Scenario: Missing category is inferred from asset evidence

- **WHEN** an asset plan has no `categoryCode` and its asset name, API code, URL, AI profile, examples, or schemas match an enabled category
- **THEN** the category classification subagent SHALL set `assetPlans[].categoryCode` to the matched category code

#### Scenario: Existing valid category is preserved

- **WHEN** an asset plan already has a `categoryCode` that exists in the enabled category candidates
- **THEN** the category classification subagent SHALL preserve that category code

#### Scenario: Invalid category is not silently accepted

- **WHEN** an asset plan has a `categoryCode` that is not present in the enabled category candidates
- **THEN** the category classification subagent SHALL replace it only when another enabled category has high-confidence evidence, otherwise it SHALL add a Chinese clarification question

#### Scenario: No enabled categories are available

- **WHEN** the planner request contains no enabled category candidates
- **THEN** the category classification subagent SHALL not invent a category code and SHALL add a Chinese clarification question or blocking review note

### Requirement: Category Plans Are Kept Consistent

Import Agent planning SHALL keep `categoryPlans` consistent with automatically selected asset category codes.

#### Scenario: Selected existing category creates use-existing plan

- **WHEN** a category code is selected from enabled category candidates for any asset plan
- **THEN** the plan SHALL contain a matching `categoryPlans[]` item with `action` set to `USE_EXISTING`

#### Scenario: Duplicate category plans are avoided

- **WHEN** multiple asset plans select the same enabled category
- **THEN** the planner SHALL keep only one category plan for that category code

#### Scenario: New category creation is not automatic

- **WHEN** category classification cannot match an enabled category
- **THEN** the planner SHALL not create a `CREATE_IF_MISSING` category plan unless the user explicitly requested creating a new category

### Requirement: Low Confidence Classification Produces Chinese Clarification

Import Agent SHALL ask the user in Chinese when classification cannot be determined confidently from available categories and asset evidence.

#### Scenario: Multiple likely categories exist

- **WHEN** more than one enabled category could plausibly match an asset and no candidate is clearly best
- **THEN** the planner SHALL keep the plan non-executable and ask the user in Chinese to confirm the category, preferably listing the strongest candidate defaults

#### Scenario: Category is required before execution

- **WHEN** an asset plan remains without a valid enabled `categoryCode`
- **THEN** plan review SHALL prevent the plan from becoming executable and include a Chinese clarification item

### Requirement: Classification Integrates With Existing Planner Subagents

Import Agent category classification SHALL run as part of the existing planner subagent orchestration before final plan review.

#### Scenario: Classification consumes generated evidence

- **WHEN** schema generation or other earlier subagents add request examples, response examples, JSON schemas, async task configuration, or AI profile fields
- **THEN** category classification SHALL be able to use those fields as evidence

#### Scenario: Plan review validates classification result

- **WHEN** category classification completes
- **THEN** plan review SHALL validate that required asset category codes are present, enabled, and reflected by category plans
