package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentRegistry;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentSpec;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerOutputMode;
import io.github.timemachinelab.infrastructure.importagent.planner.stream.ImportAgentPlannerStreamSummaries;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Executes the declarative import-agent planner control loop.
 */
@Component
public class ImportAgentPlannerRuntime {

    private final ImportAgentPlannerAgentRegistry agentRegistry;

    public ImportAgentPlannerRuntime(ImportAgentPlannerAgentRegistry agentRegistry) {
        this.agentRegistry = Objects.requireNonNull(agentRegistry, "Agent registry must not be null");
    }

    public ImportAgentPlannerRuntime() {
        this(new ImportAgentPlannerAgentRegistry());
    }

    public ImportAgentPlannerRuntimeResult run(
            ImportAgentPlannerRequest request,
            ImportAgentStreamEmitter streamEmitter,
            ImportAgentPlannerAgentInvoker invoker) throws IOException, InterruptedException {
        Objects.requireNonNull(request, "Planner request must not be null");
        Objects.requireNonNull(invoker, "Planner agent invoker must not be null");
        ImportAgentStreamEmitter stream = streamEmitter == null ? ImportAgentStreamEmitter.noop() : streamEmitter;
        ImportAgentPlannerContext context = new ImportAgentPlannerContext(request);
        JsonNode finalPlanSource = null;
        for (ImportAgentPlannerAgentSpec agent : agentRegistry.getAgents()) {
            ImportAgentPlannerStreamSummaries.emitStart(stream, agent);
            ImportAgentPlannerStageResult result = invoker.invoke(agent, context);
            context.addStageResult(result);
            ImportAgentPlannerStreamSummaries.emitComplete(stream, agent, result.output());
            if (agent.outputMode() == ImportAgentPlannerOutputMode.FINAL_PLAN) {
                finalPlanSource = result.output();
            }
        }
        return new ImportAgentPlannerRuntimeResult(context.getStageResults(), finalPlanSource);
    }
}
