package io.github.timemachinelab.infrastructure.importagent.planner;

record ImportAgentPlanningToolDescriptor(
        String name,
        PlannerStage stage,
        int order,
        ImportAgentPlanningTool tool) {
}