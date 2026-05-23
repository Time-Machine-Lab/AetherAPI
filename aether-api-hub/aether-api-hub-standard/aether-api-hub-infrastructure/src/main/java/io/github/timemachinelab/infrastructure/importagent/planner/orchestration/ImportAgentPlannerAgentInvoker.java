package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentSpec;

import java.io.IOException;

/**
 * Provider-specific invocation hook for one planner agent stage.
 */
@FunctionalInterface
public interface ImportAgentPlannerAgentInvoker {

    ImportAgentPlannerStageResult invoke(
            ImportAgentPlannerAgentSpec agent,
            ImportAgentPlannerContext context) throws IOException, InterruptedException;
}
