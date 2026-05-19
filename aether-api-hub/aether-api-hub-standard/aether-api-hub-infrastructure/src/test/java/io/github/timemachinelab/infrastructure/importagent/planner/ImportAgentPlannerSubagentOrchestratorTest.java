package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlannerSubagentOrchestratorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("orchestrator should merge subagent facts into one candidate plan")
    void shouldMergeSubagentFactsIntoOneCandidatePlan() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

        ObjectNode extractedFacts = OBJECT_MAPPER.createObjectNode();
        extractedFacts.putArray("assetFacts")
                .addObject()
                .put("apiCode", "weather-tool")
                .put("assetName", "Weather Tool")
                .put("assetType", "STANDARD_API")
                .put("requestMethod", "GET")
                .put("upstreamUrl", "https://upstream.example.com/weather");
        extractedFacts.putArray("authHints")
                .addObject()
                .put("apiCode", "weather-tool")
                .put("authScheme", "HEADER_TOKEN");

        ObjectNode slotPatches = OBJECT_MAPPER.createObjectNode();
        slotPatches.putArray("assetPlans")
                .addObject()
                .put("apiCode", "weather-tool")
                .put("authConfig", "Authorization: Bearer upstream-token");

        ObjectNode planSource = OBJECT_MAPPER.createObjectNode();
        planSource.put("summary", "draft");
        planSource.putArray("assetPlans")
                .addObject()
                .put("apiCode", "weather-tool")
                .put("assetName", "Weather Tool")
                .put("assetType", "STANDARD_API")
                .put("categoryCode", "tools")
                .put("requestMethod", "GET")
                .put("upstreamUrl", "https://upstream.example.com/weather")
                .put("publishAfterImport", true);

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), extractedFacts, slotPatches, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertTrue(plan.isExecutable());
        assertEquals("HEADER_TOKEN", candidate.path("assetPlans").get(0).path("authScheme").asText());
        assertEquals("Authorization: Bearer upstream-token", candidate.path("assetPlans").get(0).path("authConfig").asText());
    }

    @Test
    @DisplayName("orchestrator should downgrade conflicting subagent output into clarification")
    void shouldDowngradeConflictingSubagentOutputIntoClarification() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

        ObjectNode extractedFacts = OBJECT_MAPPER.createObjectNode();
        extractedFacts.putArray("authHints")
                .addObject()
                .put("apiCode", "weather-tool")
                .put("authScheme", "QUERY_TOKEN")
                .put("authConfig", "token=query-token");

        ObjectNode planSource = OBJECT_MAPPER.createObjectNode();
        planSource.put("summary", "draft");
        planSource.putArray("assetPlans")
                .addObject()
                .put("apiCode", "weather-tool")
                .put("assetName", "Weather Tool")
                .put("assetType", "STANDARD_API")
                .put("categoryCode", "tools")
                .put("requestMethod", "GET")
                .put("upstreamUrl", "https://upstream.example.com/weather")
                .put("authScheme", "HEADER_TOKEN")
                .put("authConfig", "Authorization: Bearer upstream-token")
                .put("publishAfterImport", true);

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), extractedFacts, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertFalse(plan.isExecutable());
        assertTrue(plan.getClarificationQuestions().stream().anyMatch(question -> question.contains("authScheme")));
        assertEquals("HEADER_TOKEN", candidate.path("assetPlans").get(0).path("authScheme").asText());
    }

    private ImportAgentPlannerRequest baseRequest() {
        return new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "please continue",
                null,
                2,
                List.of());
    }
}