package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.port.out.ApiImportAgentPlannerPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Planner port implementation backed by ordered internal providers.
 */
@Component
public class ProviderBackedApiImportAgentPlanner implements ApiImportAgentPlannerPort {

    private final List<ImportAgentPlannerProvider> plannerProviders;

    public ProviderBackedApiImportAgentPlanner(
            List<ImportAgentPlannerProvider> plannerProviders) {
        this.plannerProviders = plannerProviders == null ? List.of() : List.copyOf(plannerProviders);
    }

    @Override
    public ImportAgentPlannerResult plan(ImportAgentPlannerRequest request) {
        boolean matched = false;
        for (ImportAgentPlannerProvider provider : plannerProviders) {
            if (!provider.supports(request)) {
                continue;
            }
            matched = true;
            try {
                return provider.plan(request);
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Import agent planner provider failed", ex);
            }
        }
        if (!matched) {
            throw new IllegalStateException("No import agent planner provider matched request");
        }
        throw new IllegalStateException("No import agent planner provider produced a plan");
    }
}
