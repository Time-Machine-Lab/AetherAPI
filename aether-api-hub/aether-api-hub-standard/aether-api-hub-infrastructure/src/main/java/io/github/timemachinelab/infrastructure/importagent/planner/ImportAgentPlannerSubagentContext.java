package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Shared mutable context for internal planner subagent orchestration.
 */
public final class ImportAgentPlannerSubagentContext {

    private static final Logger log = LoggerFactory.getLogger(ImportAgentPlannerSubagentContext.class);

    private final ImportAgentPlannerRequest request;
    private final JsonNode extractedFacts;
    private final JsonNode slotPatches;
    private final LinkedHashSet<String> clarificationQuestions = new LinkedHashSet<>();
    private final List<String> failures = new ArrayList<>();

    public ImportAgentPlannerSubagentContext(
            ImportAgentPlannerRequest request,
            JsonNode extractedFacts,
            JsonNode slotPatches) {
        this.request = Objects.requireNonNull(request, "Planner request must not be null");
        this.extractedFacts = extractedFacts;
        this.slotPatches = slotPatches;
    }

    public ImportAgentPlannerRequest getRequest() {
        return request;
    }

    public JsonNode getExtractedFacts() {
        return extractedFacts;
    }

    public JsonNode getSlotPatches() {
        return slotPatches;
    }

    public void addClarificationQuestion(String question) {
        if (question == null || question.isBlank()) {
            return;
        }
        String normalized = question.trim();
        if (clarificationQuestions.add(normalized)) {
            log.debug("Import-agent clarification added: {}", normalized);
        }
    }

    public void addFailure(String failureMessage) {
        if (failureMessage == null || failureMessage.isBlank()) {
            return;
        }
        String normalized = failureMessage.trim();
        failures.add(normalized);
        log.warn("Import-agent subagent failure recorded: {}", normalized);
    }

    public List<String> clarificationQuestions() {
        return List.copyOf(clarificationQuestions);
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }

    public int clarificationCount() {
        return clarificationQuestions.size();
    }

    public int failureCount() {
        return failures.size();
    }
}