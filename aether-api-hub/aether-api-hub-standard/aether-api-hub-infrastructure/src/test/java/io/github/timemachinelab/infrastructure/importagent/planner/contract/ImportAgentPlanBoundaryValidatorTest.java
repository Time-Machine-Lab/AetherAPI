package io.github.timemachinelab.infrastructure.importagent.planner.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportAgentPlanBoundaryValidatorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("boundary should map a valid final plan")
    void shouldMapValidFinalPlan() {
        ObjectNode source = executablePlan();

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertTrue(result.isExecutable());
        assertEquals("ready", result.getSummary());
        assertEquals(ImportCategoryPlanAction.USE_EXISTING, result.getCategoryPlans().get(0).getAction());
        assertEquals(AuthScheme.NONE, result.getAssetPlans().get(0).getAuthScheme());
    }

    @Test
    @DisplayName("边界校验应将上游请求头解析为结构化计划字段")
    void shouldParseUpstreamRequestHeaders() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.putArray("upstreamRequestHeaders")
                .addObject()
                .put("name", "OpenAI-Beta")
                .put("value", "assistants=v2");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertTrue(result.isExecutable());
        assertEquals("OpenAI-Beta", result.getAssetPlans().get(0).getUpstreamRequestHeaders().get(0).getName());
        assertEquals("assistants=v2", result.getAssetPlans().get(0).getUpstreamRequestHeaders().get(0).getValue());
    }

    @Test
    @DisplayName("边界校验应为缺失的上游请求头值发起澄清")
    void shouldClarifyMissingUpstreamRequestHeaderValue() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.putArray("upstreamRequestHeaders")
                .addObject()
                .put("name", "OpenAI-Beta");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertFalse(result.isExecutable());
        assertTrue(result.getClarificationItems().stream().anyMatch(item ->
                "/assetPlans/0/upstreamRequestHeaders/0/value".equals(item.getTargetPath())
                        && "value".equals(item.getFieldKey())
        ));
    }

    @Test
    @DisplayName("边界校验应拒绝受保护的上游请求头名称")
    void shouldClarifyProtectedUpstreamRequestHeaderName() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.putArray("upstreamRequestHeaders")
                .addObject()
                .put("name", "Authorization")
                .put("value", "Bearer token");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertFalse(result.isExecutable());
        assertTrue(result.getClarificationItems().stream().anyMatch(item ->
                "/assetPlans/0/upstreamRequestHeaders/0/name".equals(item.getTargetPath())
                        && "name".equals(item.getFieldKey())
        ));
    }

    @Test
    @DisplayName("boundary should parse JSON objects from markdown-wrapped content")
    void shouldParseJsonCandidateFromMarkdown() {
        assertEquals("ready", ImportAgentPlannerJsonSupport.parseJsonCandidate("""
                结果如下：
                ```json
                {"summary":"ready","assetPlans":[]}
                ```
                """).path("summary").asText());
    }

    @Test
    @DisplayName("boundary should return null for malformed final content")
    void shouldReturnNullForMalformedContent() {
        assertNull(ImportAgentPlannerJsonSupport.parseJsonCandidate("not-json"));
    }

    @Test
    @DisplayName("unsupported enum values should become clarification needs instead of risky defaults")
    void shouldTreatUnsupportedEnumsAsClarificationNeeds() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.put("assetType", "UNKNOWN");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertFalse(result.isExecutable());
        assertNull(result.getAssetPlans().get(0).getAssetType());
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("资产类型")));
    }

    @Test
    @DisplayName("missing execution fields should keep a plan non-executable")
    void shouldKeepMissingExecutionFieldsNonExecutable() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "weather-tool");
        assetNode.put("publishAfterImport", true);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertFalse(result.isExecutable());
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("资产名称")));
        assertTrue(result.getClarificationQuestions().stream().anyMatch(question -> question.contains("请求方法")));
    }

    @Test
    @DisplayName("async query assets should stay separate unless the LLM final plan models asyncTaskConfig explicitly")
    void shouldNotFoldAsyncQueryAssets() {
        ObjectNode source = executablePlan();
        ObjectNode queryAsset = source.withArray("assetPlans").addObject();
        queryAsset.put("apiCode", "weather-status");
        queryAsset.put("assetName", "Weather Status");
        queryAsset.put("assetType", "STANDARD_API");
        queryAsset.put("categoryCode", "tools");
        queryAsset.put("requestMethod", "GET");
        queryAsset.put("upstreamUrl", "https://upstream.example.com/tasks/{taskId}");
        queryAsset.put("authScheme", "NONE");
        queryAsset.put("publishAfterImport", false);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertTrue(result.isExecutable());
        assertEquals(2, result.getAssetPlans().size());
        assertNull(result.getAssetPlans().get(0).getAsyncTaskConfig());
    }

    @Test
    @DisplayName("schema and example syntax normalization should remain boundary-only")
    void shouldNormalizeBoundarySyntaxOnly() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.putObject("requestJsonSchema")
                .put("type", "object")
                .putObject("properties")
                .putObject("query")
                .put("type", "string");
        assetNode.put("responseJsonSchema", "not-json");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertTrue(result.isExecutable());
        assertEquals("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}",
                result.getAssetPlans().get(0).getRequestJsonSchema());
        assertNull(result.getAssetPlans().get(0).getResponseJsonSchema());
    }

    @Test
    @DisplayName("auth config object should keep value prefix when normalized")
    void shouldNormalizeAuthConfigObjectWithValuePrefix() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.put("authScheme", "HEADER_TOKEN");
        assetNode.putObject("authConfig")
                .put("headerName", "Authorization")
                .put("valuePrefix", "Bearer ")
                .put("value", "upstream-token");

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertTrue(result.isExecutable());
        assertEquals("Authorization: Bearer upstream-token", result.getAssetPlans().get(0).getAuthConfig());
    }

    @Test
    @DisplayName("auth config JSON string should normalize to backend string format")
    void shouldNormalizeAuthConfigJsonString() {
        ObjectNode source = executablePlan();
        ObjectNode assetNode = (ObjectNode) source.withArray("assetPlans").get(0);
        assetNode.put("authScheme", "HEADER_TOKEN");
        assetNode.put("authConfig", """
                {"headerName":"Authorization","valuePrefix":"Bearer ","value":"upstream-token"}
                """);

        ImportAgentPlanModel result = ImportAgentPlannerJsonSupport.buildPlan(request(), source);

        assertTrue(result.isExecutable());
        assertEquals("Authorization: Bearer upstream-token", result.getAssetPlans().get(0).getAuthConfig());
    }

    private ObjectNode executablePlan() {
        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        source.put("summary", "ready");
        source.putArray("categoryPlans")
                .addObject()
                .put("categoryCode", "tools")
                .put("categoryName", "Tools")
                .put("action", "USE_EXISTING");
        ObjectNode assetNode = source.putArray("assetPlans").addObject();
        assetNode.put("apiCode", "weather-tool");
        assetNode.put("assetName", "Weather Tool");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "tools");
        assetNode.put("requestMethod", "GET");
        assetNode.put("upstreamUrl", "https://upstream.example.com/weather");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        return source;
    }

    private ImportAgentPlannerRequest request() {
        return new ImportAgentPlannerRequest("source", "summary", "intent", "message", null, 1, List.of());
    }
}
