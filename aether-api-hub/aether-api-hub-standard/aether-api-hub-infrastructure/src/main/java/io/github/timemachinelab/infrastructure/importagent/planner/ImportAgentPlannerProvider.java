package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;

/**
 * Internal planner provider SPI for import agent planning.
 */
public interface ImportAgentPlannerProvider {

    boolean supports(ImportAgentPlannerRequest request);

    ImportAgentPlannerResult plan(ImportAgentPlannerRequest request);
}