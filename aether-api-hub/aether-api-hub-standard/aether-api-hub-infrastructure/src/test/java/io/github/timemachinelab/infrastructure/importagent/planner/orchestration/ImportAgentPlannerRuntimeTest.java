package io.github.timemachinelab.infrastructure.importagent.planner.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentRegistry;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportAgentPlannerRuntimeTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("runtime should invoke declarations in order and pass prior outputs")
    void shouldInvokeDeclarationsInOrder() throws Exception {
        ImportAgentPlannerRuntime runtime = new ImportAgentPlannerRuntime(new ImportAgentPlannerAgentRegistry());
        List<String> names = new ArrayList<>();
        List<Integer> priorCounts = new ArrayList<>();

        ImportAgentPlannerRuntimeResult result = runtime.run(
                new ImportAgentPlannerRequest(null, null, "intent", "message", null, 1, List.of()),
                null,
                (agent, context) -> {
                    names.add(agent.name());
                    priorCounts.add(context.getStageResults().size());
                    return new ImportAgentPlannerStageResult(agent,
                            OBJECT_MAPPER.createObjectNode().put("agent", agent.name()));
                });

        assertEquals(List.of(
                        "goal_capture",
                        "observation",
                        "state_estimation",
                        "gap_comparator",
                        "plan_controller",
                        "plan_synthesis",
                        "plan_review",
                        "final_plan"),
                names);
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7), priorCounts);
        assertEquals("final_plan", result.finalPlanSource().path("agent").asText());
    }
}
