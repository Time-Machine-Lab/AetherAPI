package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Internal narrow-role collaborator for planner orchestration.
 */
public interface ImportAgentPlannerSubagent {

    void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan);
}