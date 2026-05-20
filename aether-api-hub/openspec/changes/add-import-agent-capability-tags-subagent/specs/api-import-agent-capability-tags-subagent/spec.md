## ADDED Requirements

### Requirement: Capability tags subagent shall generate AI tags

Import Agent planner SHALL include an internal capability tags subagent that generates non-empty `assetPlans[].aiProfile.capabilityTags` for AI API assets when provider/model or other plan evidence is sufficient.

#### Scenario: Generate tags from text-to-video model evidence
- **WHEN** an AI API asset has `aiProfile.provider` set to `dashscope`, `aiProfile.model` set to `happyhorse-1.0-t2v`, and empty or missing `capabilityTags`
- **THEN** the planner SHALL populate `capabilityTags` with non-empty normalized tags that include a video generation capability such as `text-to-video` or `video-generation`

#### Scenario: Generate tags from asset and URL evidence
- **WHEN** an AI API asset has an asset name, API code, upstream URL, request schema, or response schema that clearly indicates an AI capability
- **THEN** the capability tags subagent SHALL infer stable lowercase tags from that evidence without requiring user input

### Requirement: Capability tags shall be normalized and preserved

Import Agent planner SHALL normalize generated AI capability tags and SHALL preserve existing non-empty tags unless a conflict requires clarification.

#### Scenario: Normalize generated tags
- **WHEN** the capability tags subagent generates tags containing uppercase letters, whitespace, underscores, or duplicate values
- **THEN** the planner SHALL normalize them to stable lowercase hyphenated strings, remove empty values, and de-duplicate them

#### Scenario: Preserve existing tags
- **WHEN** an AI API asset already has non-empty `aiProfile.capabilityTags`
- **THEN** the planner SHALL preserve the existing tags and MUST NOT replace them with lower-confidence inferred tags

### Requirement: Missing capability tags shall block executable AI plans

Import Agent planner SHALL NOT mark an AI API import plan executable when `aiProfile.provider` and `aiProfile.model` are present but `aiProfile.capabilityTags` is empty after capability tag generation.

#### Scenario: Ask for capability when tags cannot be inferred
- **WHEN** an AI API asset has provider and model but the planner cannot infer any capability tag from the available evidence
- **THEN** the plan SHALL remain non-executable and include a Chinese clarification question or structured clarification item asking the user to confirm the AI capability category

#### Scenario: Avoid domain exception during run
- **WHEN** a user starts an Import Agent run for an AI API asset whose planned `capabilityTags` are empty
- **THEN** the service SHALL avoid passing empty tags into `AiCapabilityProfile.of(...)` and SHALL return a readable Import Agent failure or clarification path instead of exposing `AI capability tags must not be empty` as the primary user-facing failure

### Requirement: AI profile generation shall require tags

Import Agent LLM planning instructions and tool schemas SHALL instruct model outputs to include non-empty `capabilityTags` whenever they output an AI profile with provider and model.

#### Scenario: Tool-calling planner emits AI profile
- **WHEN** the tool-calling planner generates or patches `assetPlans[].aiProfile.provider` and `assetPlans[].aiProfile.model`
- **THEN** the tool schema and prompt SHALL make `capabilityTags` part of the expected AI profile output and the backend subagent SHALL fill any remaining omission when possible
