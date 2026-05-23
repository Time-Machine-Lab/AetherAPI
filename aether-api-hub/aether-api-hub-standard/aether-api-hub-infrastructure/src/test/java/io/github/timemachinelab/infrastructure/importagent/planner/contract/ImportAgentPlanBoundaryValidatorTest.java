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
