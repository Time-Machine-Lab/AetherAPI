package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;

/**
 * Import agent planner port.
 */
public interface ApiImportAgentPlannerPort {

    ImportAgentPlannerResult plan(ImportAgentPlannerRequest request);
}