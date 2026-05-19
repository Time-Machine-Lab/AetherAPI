package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Shared mutable context for internal planner subagent orchestration.
 */
public final class ImportAgentPlannerSubagentContext {

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
        clarificationQuestions.add(question.trim());
    }

    public void addFailure(String failureMessage) {
        if (failureMessage == null || failureMessage.isBlank()) {
            return;
        }
        failures.add(failureMessage.trim());
    }

    public List<String> clarificationQuestions() {
        return List.copyOf(clarificationQuestions);
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }
}