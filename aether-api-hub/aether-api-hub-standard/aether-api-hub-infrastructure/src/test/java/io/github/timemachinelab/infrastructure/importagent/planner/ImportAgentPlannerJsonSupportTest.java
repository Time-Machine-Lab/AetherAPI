package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlannerJsonSupportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("buildPlan should recover authConfig from latest user answer and preserve current plan fields")
    void shouldRecoverAuthConfigFromLatestUserAnswerAndPreserveCurrentPlanFields() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
                1,
                false,
                "draft",
                List.of("请补充 authConfig"),
                List.of(new ImportCategoryPlanModel("tools", "Tools", ImportCategoryPlanAction.CREATE_IF_MISSING)),
                List.of(new ImportAssetPlanModel(
                        "weather-tool",
                        "Weather Tool",
                        AssetType.STANDARD_API,
                        "tools",
                        RequestMethod.GET,
                        "https://provider.example.com/weather",
                        AuthScheme.HEADER_TOKEN,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        null,
                        null)));

        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "weather-tool");
        assetNode.put("assetName", "Weather Tool");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("authScheme", "HEADER_TOKEN");

        ImportAgentPlannerRequest request = new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "weather api summary",
                "import weather api",
                "请使用 Authorization: Bearer upstream-token",
                currentPlan,
                2,
                List.of(new ImportAgentTurnModel("t1", "s1", 1, ImportAgentActorType.USER, "Authorization: Bearer upstream-token", 1, "2026-05-19T10:00:00Z")));

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request, source);

        assertTrue(result.isExecutable());
        assertEquals("Authorization: Bearer upstream-token", result.getAssetPlans().get(0).getAuthConfig());
        assertEquals(RequestMethod.GET, result.getAssetPlans().get(0).getRequestMethod());
        assertEquals("https://provider.example.com/weather", result.getAssetPlans().get(0).getUpstreamUrl());
    }

    @Test
    @DisplayName("buildPlan should recover async query fields and aiProfile from document summary")
    void shouldRecoverAsyncQueryFieldsAndAiProfileFromDocumentSummary() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "video-submit");
        assetNode.put("assetName", "Video Submit");
        assetNode.put("assetType", "AI_API");
        assetNode.put("categoryCode", "video");
        assetNode.put("requestMethod", "POST");
        assetNode.put("upstreamUrl", "https://provider.example.com/v1/video/submit");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        assetNode.putObject("asyncTaskConfig").put("enabled", true);

        ImportAgentPlannerRequest request = new ImportAgentPlannerRequest(
                "https://docs.example.com/video",
                "提交任务后通过 GET https://provider.example.com/v1/tasks/{task_id} 查询结果。模型为 gpt-4.1，提供商 OpenAI。",
                "import video api",
                "继续",
                null,
                1,
                List.of());

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request, source);

        assertTrue(result.isExecutable());
        assertEquals("GET", result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryMethod());
        assertEquals("https://provider.example.com/v1/tasks/{taskId}", result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryUrlTemplate());
        assertEquals("OpenAI", result.getAssetPlans().get(0).getAiProfile().getProvider());
        assertEquals("gpt-4.1", result.getAssetPlans().get(0).getAiProfile().getModel());
    }

    @Test
    @DisplayName("buildPlan should normalize schema aliases and structured authConfig objects")
    void shouldNormalizeSchemaAliasesAndStructuredAuthConfigObjects() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "catalog-search");
        assetNode.put("assetName", "Catalog Search");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "catalog");
        assetNode.put("requestMethod", "GET");
        assetNode.put("upstreamUrl", "https://provider.example.com/catalog/search");
        assetNode.put("authScheme", "HEADER_TOKEN");
        assetNode.put("publishAfterImport", true);
        assetNode.putObject("authConfig")
                .put("headerName", "Authorization")
                .put("value", "Bearer test-token");
        assetNode.put("requestSchema", "{\"type\":\"object\"}");
        assetNode.put("outputSchema", "{\"type\":\"object\"}");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals("Authorization: Bearer test-token", result.getAssetPlans().get(0).getAuthConfig());
        assertEquals("{\"type\":\"object\"}", result.getAssetPlans().get(0).getRequestJsonSchema());
        assertEquals("{\"type\":\"object\"}", result.getAssetPlans().get(0).getResponseJsonSchema());
    }

    @Test
    @DisplayName("buildPlan should keep async override plan non-executable when override credentials are still missing")
    void shouldRequireAsyncOverrideCredentialsWhenStillMissing() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "task-submit");
        assetNode.put("assetName", "Task Submit");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "tools");
        assetNode.put("requestMethod", "POST");
        assetNode.put("upstreamUrl", "https://provider.example.com/task/submit");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        assetNode.putObject("asyncTaskConfig")
                .put("enabled", true)
                .put("queryMethod", "GET")
                .put("queryUrlTemplate", "https://provider.example.com/task/{taskId}")
                .put("authMode", "OVERRIDE");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertFalse(result.isExecutable());
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("asyncTaskConfig.authConfig")));
    }

    private ImportAgentPlannerRequest baseRequest() {
        return new ImportAgentPlannerRequest(
                "https://docs.example.com/base",
                "summary",
                "import api",
                "continue",
                null,
                1,
                List.of());
    }
}