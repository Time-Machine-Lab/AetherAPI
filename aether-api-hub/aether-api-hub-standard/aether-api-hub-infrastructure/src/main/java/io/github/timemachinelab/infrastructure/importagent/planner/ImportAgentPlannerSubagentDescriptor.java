package io.github.timemachinelab.infrastructure.importagent.planner;

record ImportAgentPlannerSubagentDescriptor(
        String name,
        ImportAgentPlannerSubagentRole role,
        int order,
        ImportAgentPlannerSubagent subagent) {
}