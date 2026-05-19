package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Orchestrates internal planner subagents and returns one unified plan candidate.
 */
@Component
public class ImportAgentPlannerSubagentOrchestrator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ImportAgentPlannerSubagentRegistry subagentRegistry;

    public ImportAgentPlannerSubagentOrchestrator(ImportAgentPlannerSubagentRegistry subagentRegistry) {
        this.subagentRegistry = Objects.requireNonNull(subagentRegistry, "Planner subagent registry must not be null");
    }

    static ImportAgentPlannerSubagentOrchestrator defaultOrchestrator() {
        return new ImportAgentPlannerSubagentOrchestrator(ImportAgentPlannerSubagentRegistry.defaultRegistry());
    }

    public ObjectNode orchestrate(
            ImportAgentPlannerRequest request,
            JsonNode extractedFacts,
            JsonNode slotPatches,
            JsonNode planSource) {
        ImportAgentPlannerSubagentContext context = new ImportAgentPlannerSubagentContext(request, extractedFacts, slotPatches);
        ObjectNode candidatePlan = planSource != null && planSource.isObject()
                ? ((ObjectNode) planSource).deepCopy()
                : OBJECT_MAPPER.createObjectNode();
        for (ImportAgentPlannerSubagentDescriptor descriptor : subagentRegistry.getSubagents()) {
            try {
                descriptor.subagent().contribute(context, candidatePlan);
            } catch (RuntimeException ex) {
                context.addFailure(descriptor.name() + ": " + ex.getMessage());
            }
        }
        appendClarificationQuestions(candidatePlan, context);
        return candidatePlan;
    }

    private void appendClarificationQuestions(ObjectNode candidatePlan, ImportAgentPlannerSubagentContext context) {
        ArrayNode questions = ImportAgentPlannerSubagentSupport.ensureArray(candidatePlan, "clarificationQuestions");
        for (String question : context.clarificationQuestions()) {
            if (!containsText(questions, question)) {
                questions.add(question);
            }
        }
        for (String failure : context.failures()) {
            String safeQuestion = "内部规划审查未完全完成，请确认关键缺失字段后再继续：" + failure;
            if (!containsText(questions, safeQuestion)) {
                questions.add(safeQuestion);
            }
        }
    }

    private boolean containsText(ArrayNode questions, String expected) {
        for (JsonNode questionNode : questions) {
            if (expected.equals(questionNode.asText(null))) {
                return true;
            }
        }
        return false;
    }
}