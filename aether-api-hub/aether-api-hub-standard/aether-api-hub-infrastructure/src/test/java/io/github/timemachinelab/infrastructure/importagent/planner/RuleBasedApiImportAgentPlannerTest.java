package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedApiImportAgentPlannerTest {

    private final RuleBasedApiImportAgentPlanner planner = new RuleBasedApiImportAgentPlanner();

    @Test
    @DisplayName("planner should request clarification when no asset plans are provided")
    void shouldRequestClarificationWhenAssetPlansMissing() {
        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "{}",
                "import weather api",
                "please import it",
                null,
                1,
                List.of()
        ));

        assertFalse(result.getPlan().isExecutable());
        assertEquals(1, result.getPlan().getVersion());
        assertEquals(List.of("Provide JSON with assetPlans or assets to continue."), result.getPlan().getClarificationQuestions());
    }

    @Test
    @DisplayName("planner should parse executable JSON asset plan and auto-fill missing category plan")
    void shouldParseExecutablePlanFromJson() {
        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
                null,
                """
                        {
                          "assetPlans": [
                            {
                              "apiCode": "weather-forecast",
                              "assetName": "Weather Forecast",
                              "assetType": "AI_API",
                              "categoryCode": "tools",
                              "requestMethod": "GET",
                              "upstreamUrl": "https://upstream.example.com/weather",
                              "authScheme": "HEADER_TOKEN",
                              "authConfig": "Authorization: Bearer upstream-token",
                              "publishAfterImport": true,
                              "aiProfile": {
                                "provider": "OpenAI",
                                "model": "gpt-4.1",
                                "streamingSupported": true,
                                "capabilityTags": ["chat"]
                              }
                            }
                          ]
                        }
                        """,
                "import weather api",
                "please continue",
                null,
                2,
                List.of()
        ));

        assertTrue(result.getPlan().isExecutable());
        assertEquals(2, result.getPlan().getVersion());
        assertEquals(1, result.getPlan().getCategoryPlans().size());
        assertEquals("tools", result.getPlan().getCategoryPlans().get(0).getCategoryCode());
        assertEquals("weather-forecast", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("OpenAI", result.getPlan().getAssetPlans().get(0).getAiProfile().getProvider());
        assertTrue(result.getAgentMessage().contains("Ready for confirmation"));
    }
}