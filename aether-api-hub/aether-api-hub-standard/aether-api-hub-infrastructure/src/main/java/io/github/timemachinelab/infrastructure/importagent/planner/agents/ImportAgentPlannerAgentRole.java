package io.github.timemachinelab.infrastructure.importagent.planner.agents;

/**
 * Control-loop role for one import-agent planner stage.
 */
public enum ImportAgentPlannerAgentRole {
    GOAL_CAPTURE,
    OBSERVATION,
    STATE_ESTIMATION,
    GAP_COMPARISON,
    PLAN_CONTROL,
    PLAN_SYNTHESIS,
    PLAN_REVIEW,
    FINAL_PLAN
}
