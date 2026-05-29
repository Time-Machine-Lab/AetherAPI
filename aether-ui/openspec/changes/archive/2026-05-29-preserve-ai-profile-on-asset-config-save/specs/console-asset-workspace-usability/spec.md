## ADDED Requirements

### Requirement: Saving asset configuration MUST preserve AI capability edit state
The console asset workspace MUST preserve the current AI capability configuration state when saving non-AI asset configuration for an `AI_API` asset, even if the asset revision response does not include AI profile fields.

#### Scenario: User saves base configuration after loading AI capability
- **WHEN** the user has loaded an `AI_API` asset with AI capability configuration and saves base asset configuration
- **THEN** the workspace MUST keep the loaded AI provider, model, streaming flag, and capability tags in the current edit state
- **THEN** the workspace MUST render the saved base asset configuration from the revision response

#### Scenario: Revision response omits AI capability profile
- **WHEN** the asset revision request succeeds but the response omits `aiProfile` or `aiCapabilityProfile`
- **THEN** the workspace MUST NOT clear the current AI capability form values
- **THEN** the workspace MUST keep the current asset's AI capability metadata until a later explicit asset load or AI profile binding response replaces it
