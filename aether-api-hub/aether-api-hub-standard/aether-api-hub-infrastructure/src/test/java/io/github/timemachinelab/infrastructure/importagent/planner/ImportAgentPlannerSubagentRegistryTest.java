package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlannerSubagentRegistryTest {

    @Test
    @DisplayName("registry should order planner subagents deterministically")
    void shouldOrderPlannerSubagentsDeterministically() {
        ImportAgentPlannerSubagentRegistry registry = new ImportAgentPlannerSubagentRegistry(List.of(
                new OrderedSubagentLater(),
                new OrderedSubagentEarlier(),
                new PlanReviewPlannerSubagent()
        ));

        List<ImportAgentPlannerSubagentDescriptor> descriptors = registry.getSubagents();

        assertEquals(List.of("ordered-subagent-earlier", "ordered-subagent-later", "plan_review"),
                descriptors.stream().map(ImportAgentPlannerSubagentDescriptor::name).toList());
    }

    @Test
    @DisplayName("registry should reject duplicate planner subagent names")
    void shouldRejectDuplicatePlannerSubagentNames() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ImportAgentPlannerSubagentRegistry(List.of(
                        new DuplicatePlannerSubagentOne(),
                        new DuplicatePlannerSubagentTwo()
                )));

        assertTrue(exception.getMessage().contains("Duplicate import-agent planner subagent name"));
    }

    @Test
    @DisplayName("registry should reject planner subagent without annotation")
    void shouldRejectPlannerSubagentWithoutAnnotation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ImportAgentPlannerSubagentRegistry(List.of(new NoAnnotationPlannerSubagent())));

        assertTrue(exception.getMessage().contains("missing @ImportAgentPlannerSubagentSpec"));
    }

    @ImportAgentPlannerSubagentSpec(name = "ordered-subagent-later", role = ImportAgentPlannerSubagentRole.CLARIFICATION_STRATEGY, order = 20)
    private static final class OrderedSubagentLater implements ImportAgentPlannerSubagent {
        @Override
        public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        }
    }

    @ImportAgentPlannerSubagentSpec(name = "ordered-subagent-earlier", role = ImportAgentPlannerSubagentRole.DOCUMENT_FACTS, order = 1)
    private static final class OrderedSubagentEarlier implements ImportAgentPlannerSubagent {
        @Override
        public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        }
    }

    @ImportAgentPlannerSubagentSpec(name = "duplicate-subagent", role = ImportAgentPlannerSubagentRole.DOCUMENT_FACTS, order = 1)
    private static final class DuplicatePlannerSubagentOne implements ImportAgentPlannerSubagent {
        @Override
        public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        }
    }

    @ImportAgentPlannerSubagentSpec(name = "duplicate-subagent", role = ImportAgentPlannerSubagentRole.AUTH_RECOGNITION, order = 2)
    private static final class DuplicatePlannerSubagentTwo implements ImportAgentPlannerSubagent {
        @Override
        public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        }
    }

    private static final class NoAnnotationPlannerSubagent implements ImportAgentPlannerSubagent {
        @Override
        public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        }
    }
}