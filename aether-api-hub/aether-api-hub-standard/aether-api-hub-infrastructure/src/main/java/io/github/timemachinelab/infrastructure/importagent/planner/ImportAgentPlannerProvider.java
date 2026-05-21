package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;

/**
 * Internal planner provider SPI for import agent planning.
 */
public interface ImportAgentPlannerProvider {

    boolean supports(ImportAgentPlannerRequest request);

    ImportAgentPlannerResult plan(ImportAgentPlannerRequest request);

    default ImportAgentPlannerResult plan(ImportAgentPlannerRequest request, ImportAgentStreamEmitter streamEmitter) {
        return plan(request);
    }
}
