package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentSpec;

/**
 * Result of one LLM planner stage.
 */
public record ImportAgentPlannerStageResult(
        ImportAgentPlannerAgentSpec agent,
        JsonNode output) {
}
