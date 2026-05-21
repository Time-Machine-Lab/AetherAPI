package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;

/**
 * Import agent planner port.
 */
public interface ApiImportAgentPlannerPort {

    default ImportAgentPlannerResult plan(ImportAgentPlannerRequest request) {
        return plan(request, ImportAgentStreamEmitter.noop());
    }

    ImportAgentPlannerResult plan(ImportAgentPlannerRequest request, ImportAgentStreamEmitter streamEmitter);
}
