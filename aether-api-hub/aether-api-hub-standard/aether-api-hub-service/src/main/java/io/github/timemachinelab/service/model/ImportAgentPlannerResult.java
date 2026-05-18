package io.github.timemachinelab.service.model;

/**
 * Import agent planner result.
 */
public class ImportAgentPlannerResult {

    private final ImportAgentPlanModel plan;
    private final String agentMessage;

    public ImportAgentPlannerResult(ImportAgentPlanModel plan, String agentMessage) {
        this.plan = plan;
        this.agentMessage = agentMessage;
    }

    public ImportAgentPlanModel getPlan() {
        return plan;
    }

    public String getAgentMessage() {
        return agentMessage;
    }
}