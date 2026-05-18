package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rule-based import agent planner.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RuleBasedApiImportAgentPlanner implements ImportAgentPlannerProvider {

    @Override
    public boolean supports(ImportAgentPlannerRequest request) {
        return true;
    }

    @Override
    public ImportAgentPlannerResult plan(ImportAgentPlannerRequest request) {
        var sourceNode = ImportAgentPlannerJsonSupport.parseJsonCandidate(request.getLatestUserMessage());
        if (sourceNode == null) {
            sourceNode = ImportAgentPlannerJsonSupport.parseJsonCandidate(request.getDocumentSummary());
        }
        var plan = ImportAgentPlannerJsonSupport.buildPlan(request, sourceNode);
        return new ImportAgentPlannerResult(plan, ImportAgentPlannerJsonSupport.buildAgentMessage("Rule planner", plan));
    }
}