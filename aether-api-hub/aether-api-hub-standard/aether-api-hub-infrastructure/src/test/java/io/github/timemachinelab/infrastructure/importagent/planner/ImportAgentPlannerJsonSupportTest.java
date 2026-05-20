package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlannerJsonSupportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("buildPlan should preserve current plan fields but keep missing auth config non-executable")
    void shouldPreserveCurrentPlanFieldsButKeepMissingAuthConfigNonExecutable() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
                1,
                false,
                "draft",
                List.of("请补充上游鉴权信息"),
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

        assertFalse(result.isExecutable());
        assertEquals(null, result.getAssetPlans().get(0).getAuthConfig());
        assertEquals(RequestMethod.GET, result.getAssetPlans().get(0).getRequestMethod());
        assertEquals("https://provider.example.com/weather", result.getAssetPlans().get(0).getUpstreamUrl());
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("上游鉴权信息")));
    }

    @Test
    @DisplayName("buildPlan should keep async query and ai profile missing when only free text evidence exists")
    void shouldKeepAsyncQueryAndAiProfileMissingWhenOnlyFreeTextEvidenceExists() {
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

        assertFalse(result.isExecutable());
        assertEquals(null, result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryMethod());
        assertEquals(null, result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryUrlTemplate());
        assertEquals(null, result.getAssetPlans().get(0).getAiProfile());
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("任务查询方法")));
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("任务查询 URL 模板")));
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("AI 能力信息")));
        assertTrue(result.getClarificationItems().stream().anyMatch(item ->
                "queryMethod".equals(item.getFieldKey())
                        && "/assetPlans/0/asyncTaskConfig/queryMethod".equals(item.getTargetPath())
                        && item.getOptions().stream().anyMatch(option -> "GET".equals(option.getValue()))));
    }

    @Test
    @DisplayName("buildPlan should recommend provider-model apiCode when AI profile is known")
    void shouldRecommendProviderModelApiCodeWhenAiProfileKnown() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("assetName", "HappyHorse 文生视频");
        assetNode.put("assetType", "AI_API");
        assetNode.put("categoryCode", "video");
        assetNode.put("requestMethod", "POST");
        assetNode.put("upstreamUrl", "https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        assetNode.putObject("aiProfile")
                .put("provider", "DashScope")
                .put("model", "happyhorse-1.0-t2v")
                .put("streamingSupported", false);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertFalse(result.isExecutable());
        ImportAgentClarificationItemModel apiCodeItem = result.getClarificationItems().stream()
                .filter(item -> "apiCode".equals(item.getFieldKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(apiCodeItem);
        assertEquals("dashscope-happyhorse-1-0-t2v", apiCodeItem.getDefaultValue());
        assertEquals("CURRENT_PLAN", apiCodeItem.getDefaultSource());
        assertEquals("HIGH", apiCodeItem.getDefaultConfidence());
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
    @DisplayName("buildPlan should normalize object schema fields and drop invalid schema text")
    void shouldNormalizeObjectSchemaFieldsAndDropInvalidSchemaText() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "catalog-search");
        assetNode.put("assetName", "Catalog Search");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "catalog");
        assetNode.put("requestMethod", "GET");
        assetNode.put("upstreamUrl", "https://provider.example.com/catalog/search");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        assetNode.putObject("requestJsonSchema")
                .put("type", "object")
                .putObject("properties")
                .putObject("query")
                .put("type", "string");
        assetNode.put("responseJsonSchema", "响应内容包含列表");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}",
                result.getAssetPlans().get(0).getRequestJsonSchema());
        assertEquals(null, result.getAssetPlans().get(0).getResponseJsonSchema());
    }

    @Test
    @DisplayName("buildPlan should preserve current valid schema when planner output is invalid")
    void shouldPreserveCurrentValidSchemaWhenPlannerOutputIsInvalid() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
                1,
                true,
                "ready",
                List.of(),
                List.of(new ImportCategoryPlanModel("catalog", "Catalog", ImportCategoryPlanAction.CREATE_IF_MISSING)),
                List.of(new ImportAssetPlanModel(
                        "catalog-search",
                        "Catalog Search",
                        AssetType.STANDARD_API,
                        "catalog",
                        RequestMethod.GET,
                        "https://provider.example.com/catalog/search",
                        AuthScheme.NONE,
                        null,
                        null,
                        null,
                        null,
                        "{\"type\":\"object\"}",
                        "{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\"}}}",
                        true,
                        null,
                        null)));
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "catalog-search");
        assetNode.put("assetName", "Catalog Search");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "catalog");
        assetNode.put("requestMethod", "GET");
        assetNode.put("upstreamUrl", "https://provider.example.com/catalog/search");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        assetNode.put("requestJsonSchema", "not-json");
        assetNode.put("responseJsonSchema", "also-not-json");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(new ImportAgentPlannerRequest(
                "https://docs.example.com/catalog",
                "catalog api summary",
                "import catalog api",
                "continue",
                currentPlan,
                2,
                List.of()), source);

        assertTrue(result.isExecutable());
        assertEquals("{\"type\":\"object\"}", result.getAssetPlans().get(0).getRequestJsonSchema());
        assertEquals("{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\"}}}",
                result.getAssetPlans().get(0).getResponseJsonSchema());
    }

    @Test
    @DisplayName("buildPlan should normalize assetType aliases from planner output")
    void shouldNormalizeAssetTypeAliasesFromPlannerOutput() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "dashscope-video");
        assetNode.put("assetName", "DashScope Video");
        assetNode.put("assetType", "API");
        assetNode.put("categoryCode", "video");
        assetNode.put("requestMethod", "POST");
        assetNode.put("upstreamUrl", "https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis");
        assetNode.put("authScheme", "HEADER_TOKEN");
        assetNode.put("authConfig", "Authorization: Bearer test-token");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals(AssetType.STANDARD_API, result.getAssetPlans().get(0).getAssetType());
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
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("任务查询鉴权信息")));
        assertTrue(result.getClarificationItems().stream().anyMatch(item ->
                "authConfig".equals(item.getFieldKey())
                        && "/assetPlans/0/asyncTaskConfig/authConfig".equals(item.getTargetPath())));
    }

    @Test
    @DisplayName("buildPlan should normalize async task id placeholder aliases")
    void shouldNormalizeAsyncTaskIdPlaceholderAliases() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "video-submit");
        assetNode.put("assetName", "Video Submit");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "video");
        assetNode.put("requestMethod", "POST");
        assetNode.put("upstreamUrl", "https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        assetNode.putObject("asyncTaskConfig")
                .put("enabled", true)
                .put("queryMethod", "GET")
                .put("queryUrlTemplate", "https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}")
                .put("authMode", "SAME_AS_SUBMIT");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals("https://dashscope.aliyuncs.com/api/v1/tasks/{taskId}",
                result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryUrlTemplate());
        assertTrue(result.getClarificationQuestions().isEmpty());
    }

    @Test
    @DisplayName("buildPlan should keep async query assets separate instead of folding them into submit config")
    void shouldKeepAsyncQueryAssetsSeparateInsteadOfFoldingThem() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();

        ObjectNode submitAsset = source.putArray("assetPlans").addObject();
        submitAsset.put("apiCode", "image-submit");
        submitAsset.put("assetName", "Image Submit");
        submitAsset.put("assetType", "STANDARD_API");
        submitAsset.put("categoryCode", "image");
        submitAsset.put("requestMethod", "POST");
        submitAsset.put("upstreamUrl", "https://provider.example.com/v1/image/submit");
        submitAsset.put("authScheme", "NONE");
        submitAsset.put("publishAfterImport", true);

        ObjectNode queryAsset = source.withArray("assetPlans").addObject();
        queryAsset.put("apiCode", "image-status");
        queryAsset.put("assetName", "Image Status");
        queryAsset.put("assetType", "STANDARD_API");
        queryAsset.put("categoryCode", "image");
        queryAsset.put("requestMethod", "GET");
        queryAsset.put("upstreamUrl", "https://provider.example.com/v1/image/status");
        queryAsset.put("authScheme", "NONE");
        queryAsset.put("publishAfterImport", false);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals(2, result.getAssetPlans().size());
        assertEquals(null, result.getAssetPlans().get(0).getAsyncTaskConfig());
        assertEquals("image-status", result.getAssetPlans().get(1).getApiCode());
    }

    @Test
    @DisplayName("buildPlan should fold async query assets with task placeholder into submit config")
    void shouldFoldAsyncQueryAssetsWithTaskPlaceholder() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();

        ObjectNode submitAsset = source.putArray("assetPlans").addObject();
        submitAsset.put("apiCode", "image-submit");
        submitAsset.put("assetName", "Image Submit");
        submitAsset.put("assetType", "STANDARD_API");
        submitAsset.put("categoryCode", "image");
        submitAsset.put("requestMethod", "POST");
        submitAsset.put("upstreamUrl", "https://provider.example.com/v1/image/submit");
        submitAsset.put("authScheme", "NONE");
        submitAsset.put("publishAfterImport", true);

        ObjectNode queryAsset = source.withArray("assetPlans").addObject();
        queryAsset.put("apiCode", "image-status");
        queryAsset.put("assetName", "Image Status");
        queryAsset.put("assetType", "STANDARD_API");
        queryAsset.put("categoryCode", "image");
        queryAsset.put("requestMethod", "GET");
        queryAsset.put("upstreamUrl", "https://provider.example.com/v1/image/tasks/{task_id}/status");
        queryAsset.put("authScheme", "NONE");
        queryAsset.put("publishAfterImport", false);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals(1, result.getAssetPlans().size());
        assertEquals("image-submit", result.getAssetPlans().get(0).getApiCode());
        assertEquals("https://provider.example.com/v1/image/tasks/{taskId}/status",
                result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryUrlTemplate());
        assertEquals("SAME_AS_SUBMIT", result.getAssetPlans().get(0).getAsyncTaskConfig().getAuthMode());
    }

    @Test
    @DisplayName("buildPlan should provide default metadata for structured clarifications")
    void shouldProvideDefaultMetadataForStructuredClarifications() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "catalog-create");
        assetNode.put("assetName", "Catalog Create");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "catalog");
        assetNode.put("upstreamUrl", "https://provider.example.com/catalog/create");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertFalse(result.isExecutable());
        assertEquals("requestMethod", result.getClarificationItems().get(0).getFieldKey());
        assertEquals("POST", result.getClarificationItems().get(0).getDefaultValue());
        assertEquals("INFERRED_FROM_URL", result.getClarificationItems().get(0).getDefaultSource());
    }

    @Test
    @DisplayName("buildPlan should keep malformed async authMode non-executable instead of inferring override credentials")
    void shouldKeepMalformedAsyncAuthModeNonExecutable() {
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
                .put("authMode", "HEADER_TOKEN")
                .put("authConfig", "Authorization: Bearer upstream-token");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertFalse(result.isExecutable());
        assertEquals("HEADER_TOKEN", result.getAssetPlans().get(0).getAsyncTaskConfig().getAuthMode());
        assertEquals(null, result.getAssetPlans().get(0).getAsyncTaskConfig().getAuthScheme());
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("任务查询鉴权模式")));
    }

    @Test
    @DisplayName("buildPlan should normalize malformed asset authScheme aliases")
    void shouldNormalizeMalformedAssetAuthSchemeAlias() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "weather-tool");
        assetNode.put("assetName", "Weather Tool");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "tools");
        assetNode.put("requestMethod", "GET");
        assetNode.put("upstreamUrl", "https://provider.example.com/weather");
        assetNode.put("authScheme", "BEARER_TOKEN");
        assetNode.put("authConfig", "Authorization: Bearer upstream-token");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), source);

        assertTrue(result.isExecutable());
        assertEquals(AuthScheme.HEADER_TOKEN, result.getAssetPlans().get(0).getAuthScheme());
    }

        @Test
        @DisplayName("buildPlan should preserve current structured fields during partial updates without reinterpreting prose")
        void shouldPreserveCurrentStructuredFieldsDuringPartialUpdatesWithoutReinterpretingProse() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
            1,
            false,
            "draft",
            List.of("请确认任务查询鉴权方案"),
            List.of(new ImportCategoryPlanModel("video", "Video", ImportCategoryPlanAction.CREATE_IF_MISSING)),
            List.of(new ImportAssetPlanModel(
                "video-submit",
                "Video Submit",
                AssetType.STANDARD_API,
                "video",
                RequestMethod.POST,
                "https://provider.example.com/v1/video/submit",
                AuthScheme.HEADER_TOKEN,
                "Authorization: Bearer upstream-token",
                null,
                null,
                null,
                null,
                null,
                true,
                new AsyncTaskConfigModel(
                    true,
                    "GET",
                    "https://provider.example.com/v1/tasks/{taskId}",
                    "OVERRIDE",
                    "HEADER_TOKEN",
                    "Authorization: Bearer upstream-task-token",
                    null,
                    null,
                    null),
                new io.github.timemachinelab.service.model.ImportAiProfileModel("OpenAI", "gpt-4.1", true, List.of("video"))
            )));

        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "video-submit");
        assetNode.put("assetName", "Video Submit");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlannerRequest request = new ImportAgentPlannerRequest(
            "https://docs.example.com/video",
            "这里提到另一个示例 Authorization: Bearer unrelated-token 和一个无关 URL https://other.example.com/tasks/123",
            "import video api",
            "继续",
            currentPlan,
            2,
            List.of());

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request, source);

        assertTrue(result.isExecutable());
        assertEquals(RequestMethod.POST, result.getAssetPlans().get(0).getRequestMethod());
        assertEquals("https://provider.example.com/v1/video/submit", result.getAssetPlans().get(0).getUpstreamUrl());
        assertEquals("Authorization: Bearer upstream-token", result.getAssetPlans().get(0).getAuthConfig());
        assertEquals("GET", result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryMethod());
        assertEquals("https://provider.example.com/v1/tasks/{taskId}", result.getAssetPlans().get(0).getAsyncTaskConfig().getQueryUrlTemplate());
        assertEquals("Authorization: Bearer upstream-task-token", result.getAssetPlans().get(0).getAsyncTaskConfig().getAuthConfig());
        assertEquals("OpenAI", result.getAssetPlans().get(0).getAiProfile().getProvider());
        assertEquals("gpt-4.1", result.getAssetPlans().get(0).getAiProfile().getModel());
        }

    @Test
    @DisplayName("buildPlan should update anonymous current asset instead of keeping a duplicate")
    void shouldUpdateAnonymousCurrentAssetInsteadOfKeepingDuplicate() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
                1,
                false,
                "draft",
                List.of("API code (apiCode): Provide the unique API code for this asset."),
                List.of(),
                List.of(new ImportAssetPlanModel(
                        null,
                        "Weather Forecast",
                        AssetType.STANDARD_API,
                        "tools",
                        RequestMethod.GET,
                        "https://provider.example.com/weather",
                        AuthScheme.NONE,
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
        assetNode.put("apiCode", "weather-forecast");
        assetNode.put("assetName", "Weather Forecast");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "weather api summary",
                "import weather api",
                "weather-forecast",
                currentPlan,
                2,
                List.of()), source);

        assertEquals(1, result.getAssetPlans().size());
        assertEquals("weather-forecast", result.getAssetPlans().get(0).getApiCode());
        assertEquals("https://provider.example.com/weather", result.getAssetPlans().get(0).getUpstreamUrl());
    }

    @Test
    @DisplayName("buildPlan should merge compatible current asset when planner changes apiCode")
    void shouldMergeCompatibleCurrentAssetWhenPlannerChangesApiCode() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
                1,
                false,
                "draft",
                List.of("请补充上游鉴权信息"),
                List.of(new ImportCategoryPlanModel("video", "Video", ImportCategoryPlanAction.CREATE_IF_MISSING)),
                List.of(new ImportAssetPlanModel(
                        "ALI-HAPPYHORSE-T2V",
                        "HappyHorse 文生视频",
                        AssetType.AI_API,
                        "video",
                        RequestMethod.POST,
                        "https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis",
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
        assetNode.put("apiCode", "ALIYUN-BAILIAN-HAPPYHORSE-1.0-T2V");
        assetNode.put("assetName", "HappyHorse 文生视频");
        assetNode.put("assetType", "AI_API");
        assetNode.put("categoryCode", "video");
        assetNode.put("requestMethod", "POST");
        assetNode.put("upstreamUrl", "https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis");
        assetNode.put("authScheme", "HEADER_TOKEN");
        assetNode.put("authConfig", "Authorization: Bearer upstream-token");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(new ImportAgentPlannerRequest(
                "https://help.aliyun.com/zh/model-studio/happyhorse",
                "HappyHorse 文生视频接口",
                "导入 HappyHorse 文生视频",
                "鉴权使用 Authorization: Bearer upstream-token",
                currentPlan,
                2,
                List.of()), source);

        assertEquals(1, result.getAssetPlans().size());
        assertEquals("ALIYUN-BAILIAN-HAPPYHORSE-1.0-T2V", result.getAssetPlans().get(0).getApiCode());
        assertEquals("Authorization: Bearer upstream-token", result.getAssetPlans().get(0).getAuthConfig());
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
