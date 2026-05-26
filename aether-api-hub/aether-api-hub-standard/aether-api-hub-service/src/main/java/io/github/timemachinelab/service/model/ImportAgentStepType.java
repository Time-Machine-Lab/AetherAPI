package io.github.timemachinelab.service.model;

/**
 * Import step type.
 */
public enum ImportAgentStepType {
    ENSURE_CATEGORY,
    RESOLVE_EXISTING_ASSET,
    REGISTER_ASSET,
    REVISE_ASSET,
    CREATE_ASSET,
    UPDATE_EXISTING_ASSET,
    UPSERT_ASSET,
    ATTACH_AI_PROFILE,
    PUBLISH_ASSET
}
