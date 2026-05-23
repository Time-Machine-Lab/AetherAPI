package io.github.timemachinelab.infrastructure.importagent.planner.agents;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlannerAgentRegistryTest {

    @Test
    @DisplayName("registry should expose the cybernetic control-loop agents in order")
    void shouldExposeControlLoopAgentsInOrder() {
        ImportAgentPlannerAgentRegistry registry = new ImportAgentPlannerAgentRegistry();

        assertEquals(List.of(
                        "goal_capture",
                        "observation",
                        "state_estimation",
                        "gap_comparator",
                        "plan_controller",
                        "plan_synthesis",
                        "plan_review",
                        "final_plan"),
                registry.getAgents().stream().map(ImportAgentPlannerAgentSpec::name).toList());
    }

    @Test
    @DisplayName("agent declarations should expose only their declared tools")
    void shouldExposeOnlyDeclaredTools() {
        ImportAgentPlannerAgentRegistry registry = new ImportAgentPlannerAgentRegistry();

        assertEquals(List.of("extract_import_facts"), registry.getAgent("observation").allowedTools());
        assertEquals(List.of("fill_import_slots"), registry.getAgent("state_estimation").allowedTools());
        assertEquals(List.of("submit_import_plan"), registry.getAgent("final_plan").allowedTools());
        assertTrue(registry.getAgent("plan_review").allowedTools().isEmpty());
    }

    @Test
    @DisplayName("registry should reject duplicate agent names")
    void shouldRejectDuplicateNames() {
        ImportAgentPlannerAgentSpec left = new ImportAgentPlannerAgentSpec(
                "duplicate",
                ImportAgentPlannerAgentRole.GOAL_CAPTURE,
                1,
                "prompt",
                List.of(),
                ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                "stage",
                "title",
                "summary");
        ImportAgentPlannerAgentSpec right = new ImportAgentPlannerAgentSpec(
                "duplicate",
                ImportAgentPlannerAgentRole.OBSERVATION,
                2,
                "prompt",
                List.of(),
                ImportAgentPlannerOutputMode.STRUCTURED_NOTES,
                "stage",
                "title",
                "summary");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ImportAgentPlannerAgentRegistry(List.of(left, right)));

        assertTrue(exception.getMessage().contains("Duplicate import-agent planner agent name"));
    }
}
