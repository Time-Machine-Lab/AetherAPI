package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Result of the full planner control-loop runtime.
 */
public record ImportAgentPlannerRuntimeResult(
        List<ImportAgentPlannerStageResult> stageResults,
        JsonNode finalPlanSource) {

    public ImportAgentPlannerRuntimeResult {
        stageResults = stageResults == null ? List.of() : List.copyOf(stageResults);
    }
}
