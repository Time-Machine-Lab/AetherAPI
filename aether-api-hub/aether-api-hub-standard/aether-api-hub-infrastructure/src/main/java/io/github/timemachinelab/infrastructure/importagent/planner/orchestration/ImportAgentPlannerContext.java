package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable runtime context passed across ordered planner agents.
 */
public class ImportAgentPlannerContext {

    private final ImportAgentPlannerRequest request;
    private final List<ImportAgentPlannerStageResult> stageResults = new ArrayList<>();

    public ImportAgentPlannerContext(ImportAgentPlannerRequest request) {
        this.request = request;
    }

    public ImportAgentPlannerRequest getRequest() {
        return request;
    }

    public List<ImportAgentPlannerStageResult> getStageResults() {
        return List.copyOf(stageResults);
    }

    public void addStageResult(ImportAgentPlannerStageResult result) {
        stageResults.add(result);
    }

    public JsonNode latestOutput() {
        if (stageResults.isEmpty()) {
            return null;
        }
        return stageResults.get(stageResults.size() - 1).output();
    }
}
