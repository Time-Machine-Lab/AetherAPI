package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Declares one planner tool schema exposed to the LLM provider.
 */
public interface ImportAgentPlanningTool {

    ObjectNode buildDefinition(ObjectMapper objectMapper, String toolName);

    default String stagePromptInstruction() {
        return "";
    }

    default boolean requiresStrictContentFallback() {
        return false;
    }
}