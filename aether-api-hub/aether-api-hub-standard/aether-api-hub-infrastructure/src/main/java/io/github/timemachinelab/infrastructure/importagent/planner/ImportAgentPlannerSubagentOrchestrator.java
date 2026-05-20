package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Orchestrates internal planner subagents and returns one unified plan candidate.
 */
@Component
public class ImportAgentPlannerSubagentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ImportAgentPlannerSubagentOrchestrator.class);
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
        log.debug("Import-agent subagent orchestration start: extractedFacts={}, slotPatches={}, planSource={}",
                summarizeNode(extractedFacts),
                summarizeNode(slotPatches),
                summarizeNode(planSource));
        ImportAgentPlannerSubagentContext context = new ImportAgentPlannerSubagentContext(request, extractedFacts, slotPatches);
        ObjectNode candidatePlan = planSource != null && planSource.isObject()
                ? ((ObjectNode) planSource).deepCopy()
                : OBJECT_MAPPER.createObjectNode();
        for (ImportAgentPlannerSubagentDescriptor descriptor : subagentRegistry.getSubagents()) {
            int beforeClarificationCount = context.clarificationCount();
            int beforeFailureCount = context.failureCount();
            int beforeAssetPlanCount = sizeOfArray(candidatePlan, "assetPlans");
            log.debug("Import-agent subagent start: name={}, role={}, order={}",
                    descriptor.name(), descriptor.role(), descriptor.order());
            try {
                descriptor.subagent().contribute(context, candidatePlan);
            } catch (RuntimeException ex) {
                log.warn("Import-agent subagent failed: name={}, role={}, message={}",
                        descriptor.name(), descriptor.role(), ex.getMessage(), ex);
                context.addFailure(descriptor.name() + ": " + ex.getMessage());
            }
            log.debug("Import-agent subagent complete: name={}, assetPlansDelta={}, clarificationDelta={}, failureDelta={}",
                    descriptor.name(),
                    sizeOfArray(candidatePlan, "assetPlans") - beforeAssetPlanCount,
                    context.clarificationCount() - beforeClarificationCount,
                    context.failureCount() - beforeFailureCount);
        }
        appendClarificationQuestions(candidatePlan, context);
        log.debug("Import-agent subagent orchestration complete: candidatePlan={}", summarizeNode(candidatePlan));
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

    private String summarizeNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        }
        if (node.isArray()) {
            return "array(size=" + node.size() + ")";
        }
        if (!node.isObject()) {
            return node.getNodeType().name().toLowerCase();
        }
        return "object(assetPlans=" + sizeOfArray(node, "assetPlans")
                + ", clarificationQuestions=" + sizeOfArray(node, "clarificationQuestions")
                + ", assetFacts=" + sizeOfArray(node, "assetFacts")
                + ", authHints=" + sizeOfArray(node, "authHints")
                + ", asyncHints=" + sizeOfArray(node, "asyncHints")
                + ")";
    }

    private int sizeOfArray(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        return child.isArray() ? child.size() : 0;
    }
}