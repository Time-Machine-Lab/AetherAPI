package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
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
            ObjectNode beforeCandidatePlan = descriptor.role() == ImportAgentPlannerSubagentRole.PLAN_REVIEW
                ? candidatePlan.deepCopy()
                : null;
            List<String> beforeClarifications = context.clarificationQuestions();
            List<String> beforeFailures = context.failures();
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
            if (descriptor.role() == ImportAgentPlannerSubagentRole.PLAN_REVIEW && beforeCandidatePlan != null) {
                appendReviewDiagnostics(candidatePlan, descriptor, beforeCandidatePlan, beforeClarifications, beforeFailures, context);
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

    private void appendReviewDiagnostics(
            ObjectNode candidatePlan,
            ImportAgentPlannerSubagentDescriptor descriptor,
            ObjectNode beforeCandidatePlan,
            List<String> beforeClarifications,
            List<String> beforeFailures,
            ImportAgentPlannerSubagentContext context) {
        List<String> clarificationDelta = difference(beforeClarifications, context.clarificationQuestions());
        List<String> failureDelta = difference(beforeFailures, context.failures());
        ObjectNode structurePatch = buildStructurePatch(beforeCandidatePlan, candidatePlan);

        ObjectNode diagnostics = OBJECT_MAPPER.createObjectNode();
        diagnostics.put("subagent", descriptor.name());
        diagnostics.put("role", descriptor.role().name());
        diagnostics.set("structurePatch", structurePatch);

        ArrayNode clarificationDeltaNode = diagnostics.putArray("clarificationQuestionsDelta");
        for (String question : clarificationDelta) {
            clarificationDeltaNode.add(question);
        }

        ArrayNode failureDeltaNode = diagnostics.putArray("failureDelta");
        for (String failure : failureDelta) {
            failureDeltaNode.add(failure);
        }

        String summary = "review=" + descriptor.name()
                + ", structurePatchFields=" + structurePatch.size()
                + ", clarificationDelta=" + clarificationDelta.size()
                + ", failureDelta=" + failureDelta.size();
        diagnostics.put("summary", summary);
        ImportAgentPlannerSubagentSupport.ensureArray(candidatePlan, "reviewDiagnostics").add(diagnostics);
        log.debug("Import-agent review summary: {}", summary);
    }

    private List<String> difference(List<String> before, List<String> after) {
        LinkedHashSet<String> baseline = new LinkedHashSet<>(before);
        return after.stream()
                .filter(value -> !baseline.contains(value))
                .toList();
    }

    private ObjectNode buildStructurePatch(ObjectNode before, ObjectNode after) {
        ObjectNode patch = OBJECT_MAPPER.createObjectNode();
        LinkedHashSet<String> fieldNames = new LinkedHashSet<>();
        before.fieldNames().forEachRemaining(fieldNames::add);
        after.fieldNames().forEachRemaining(fieldNames::add);
        fieldNames.remove("reviewDiagnostics");
        for (String fieldName : fieldNames) {
            JsonNode beforeNode = before.get(fieldName);
            JsonNode afterNode = after.get(fieldName);
            if (Objects.equals(beforeNode, afterNode)) {
                continue;
            }
            if (afterNode == null) {
                patch.putNull(fieldName);
                continue;
            }
            patch.set(fieldName, afterNode.deepCopy());
        }
        return patch;
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