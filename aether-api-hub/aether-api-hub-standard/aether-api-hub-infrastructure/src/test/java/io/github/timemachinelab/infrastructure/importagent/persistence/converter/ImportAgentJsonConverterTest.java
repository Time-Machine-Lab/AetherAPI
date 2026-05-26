package io.github.timemachinelab.infrastructure.importagent.persistence.converter;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAssetPlanAction;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.UpstreamRequestHeaderModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentJsonConverterTest {

    @Test
    @DisplayName("计划 JSON 应完整往返上游请求头")
    void shouldRoundTripUpstreamRequestHeaders() {
        ImportAgentPlanModel plan = new ImportAgentPlanModel(
                2,
                true,
                "ready",
                List.of(),
                List.of(),
                List.of(),
                List.of(new ImportAssetPlanModel(
                        "chat-completion",
                        "Chat Completion",
                        AssetType.STANDARD_API,
                        "tools",
                        RequestMethod.POST,
                        "https://upstream.example.com/chat",
                        AuthScheme.NONE,
                        null,
                        List.of(new UpstreamRequestHeaderModel("OpenAI-Beta", "assistants=v2")),
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        null,
                        null
                ))
        );

        String json = ImportAgentJsonConverter.serializePlan(plan);
        ImportAgentPlanModel restored = ImportAgentJsonConverter.deserializePlan(json);

        assertTrue(json.contains("\"upstreamRequestHeaders\""));
        assertTrue(json.contains("\"action\":\"UPSERT\""));
        assertEquals("OpenAI-Beta", restored.getAssetPlans().get(0).getUpstreamRequestHeaders().get(0).getName());
        assertEquals("assistants=v2", restored.getAssetPlans().get(0).getUpstreamRequestHeaders().get(0).getValue());
    }

    @Test
    @DisplayName("旧计划 JSON 缺失 action 时应按 UPSERT 兼容解析")
    void shouldDefaultMissingAssetActionToUpsertForStoredPlanJson() {
        ImportAgentPlanModel restored = ImportAgentJsonConverter.deserializePlan("""
                {
                  "version": 1,
                  "executable": true,
                  "summary": "ready",
                  "clarificationQuestions": [],
                  "clarificationItems": [],
                  "categoryPlans": [],
                  "assetPlans": [
                    {
                      "apiCode": "weather-tool",
                      "assetName": "Weather Tool",
                      "assetType": "STANDARD_API",
                      "publishAfterImport": false
                    }
                  ]
                }
                """);

        assertEquals(ImportAssetPlanAction.UPSERT, restored.getAssetPlans().get(0).getAction());
    }
}
