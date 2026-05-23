package io.github.timemachinelab.infrastructure.importagent.planner.tools;

public record ImportAgentPlanningToolDescriptor(
        String name,
        PlannerStage stage,
        int order,
        ImportAgentPlanningTool tool) {
}
