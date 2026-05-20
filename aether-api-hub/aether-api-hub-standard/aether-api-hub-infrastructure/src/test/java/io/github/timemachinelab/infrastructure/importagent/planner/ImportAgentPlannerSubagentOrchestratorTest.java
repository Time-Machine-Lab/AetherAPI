package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
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
    @DisplayName("orchestrator should apply examples from extracted facts and generate schemas")
    void shouldApplyExamplesFromFactsAndGenerateSchemas() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

        ObjectNode extractedFacts = OBJECT_MAPPER.createObjectNode();
        extractedFacts.putArray("assetFacts")
                .addObject()
                .put("apiCode", "catalog-search")
                .put("requestExample", "{\"query\":\"bike\",\"limit\":10}")
                .put("responseExample", "{\"items\":[{\"id\":\"sku-1\"}],\"total\":1}");
        ObjectNode planSource = executableStandardPlan("catalog-search");

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), extractedFacts, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertTrue(plan.isExecutable());
        assertEquals("{\"query\":\"bike\",\"limit\":10}",
                candidate.path("assetPlans").get(0).path("requestExample").asText());
        assertEquals("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"},\"limit\":{\"type\":\"integer\"}},\"required\":[\"query\",\"limit\"]}",
                candidate.path("assetPlans").get(0).path("requestJsonSchema").asText());
    }

    @Test
    @DisplayName("orchestrator should generate examples from schema hints when examples are missing")
    void shouldGenerateExamplesFromSchemaHints() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

        ObjectNode extractedFacts = OBJECT_MAPPER.createObjectNode();
        ObjectNode schemaHint = extractedFacts.putArray("schemaHints").addObject();
        schemaHint.put("apiCode", "video-submit");
        schemaHint.put("requestJsonSchema", """
                {
                  "type": "object",
                  "properties": {
                    "model": { "type": "string" },
                    "prompt": { "type": "string" }
                  }
                }
                """);
        schemaHint.put("responseJsonSchema", """
                {
                  "type": "object",
                  "properties": {
                    "task_id": { "type": "string" },
                    "status": { "type": "string", "enum": ["PENDING", "SUCCEEDED"] }
                  }
                }
                """);
        ObjectNode planSource = executableStandardPlan("video-submit");

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), extractedFacts, null, planSource);

        assertEquals("{\"model\":\"example-model\",\"prompt\":\"example-prompt\"}",
                candidate.path("assetPlans").get(0).path("requestExample").asText());
        assertEquals("{\"task_id\":\"example-task_id\",\"status\":\"PENDING\"}",
                candidate.path("assetPlans").get(0).path("responseExample").asText());
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
        assertTrue(plan.getClarificationQuestions().stream().anyMatch(question -> question.contains("上游鉴权方式")));
        assertEquals("HEADER_TOKEN", candidate.path("assetPlans").get(0).path("authScheme").asText());
                assertTrue(candidate.path("reviewDiagnostics").isArray());
    }

        @Test
        @DisplayName("orchestrator should add clarification for async pair and missing ai profile")
        void shouldAddClarificationForAsyncPairAndMissingAiProfile() throws Exception {
                ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

                ObjectNode planSource = OBJECT_MAPPER.createObjectNode();
                planSource.put("summary", "draft");
                planSource.putArray("assetPlans")
                                .addObject()
                                .put("apiCode", "video-submit")
                                .put("assetName", "Video Submit")
                                .put("assetType", "AI_API")
                                .put("categoryCode", "video")
                                .put("requestMethod", "POST")
                                .put("upstreamUrl", "https://upstream.example.com/video/submit")
                                .put("authScheme", "NONE")
                                .put("publishAfterImport", true);
                planSource.withArray("assetPlans")
                                .addObject()
                                .put("apiCode", "video-status")
                                .put("assetName", "Video Status")
                                .put("assetType", "STANDARD_API")
                                .put("categoryCode", "video")
                                .put("requestMethod", "GET")
                                .put("upstreamUrl", "https://upstream.example.com/video/tasks/{taskId}")
                                .put("authScheme", "NONE")
                                .put("publishAfterImport", false);

                ObjectNode candidate = orchestrator.orchestrate(baseRequest(), null, null, planSource);
                ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

                assertFalse(plan.isExecutable());
                assertTrue(plan.getClarificationQuestions().stream().anyMatch(question -> question.contains("AI 能力信息")));
                assertTrue(plan.getClarificationQuestions().stream().anyMatch(question -> question.contains("任务查询方法")));
                assertEquals("plan_review", candidate.path("reviewDiagnostics").get(0).path("subagent").asText());
                assertTrue(candidate.path("reviewDiagnostics").get(0).path("structurePatch").isObject());
                assertTrue(candidate.path("reviewDiagnostics").get(0).path("clarificationQuestionsDelta").isArray());
                assertTrue(candidate.path("reviewDiagnostics").get(0).path("clarificationQuestionsDelta").toString().contains("任务查询方法"));
        assertTrue(candidate.path("reviewDiagnostics").get(0).path("summary").asText().contains("clarificationDelta="));
        }

    @Test
    @DisplayName("orchestrator should normalize async task id placeholder aliases before review")
    void shouldNormalizeAsyncTaskIdPlaceholderAliasesBeforeReview() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

        ObjectNode planSource = OBJECT_MAPPER.createObjectNode();
        planSource.put("summary", "draft");
        ObjectNode assetNode = planSource.putArray("assetPlans").addObject();
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

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), null, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertTrue(plan.isExecutable());
        assertEquals("https://dashscope.aliyuncs.com/api/v1/tasks/{taskId}",
                candidate.path("assetPlans").get(0).path("asyncTaskConfig").path("queryUrlTemplate").asText());
        assertTrue(plan.getClarificationQuestions().isEmpty());
    }

    @Test
    @DisplayName("orchestrator should apply schema hints without creating extra assets")
    void shouldApplySchemaHintsWithoutCreatingExtraAssets() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();

        ObjectNode extractedFacts = OBJECT_MAPPER.createObjectNode();
        extractedFacts.putArray("schemaHints")
                .addObject()
                .put("apiCode", "catalog-search")
                .put("requestJsonSchema", "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}")
                .put("responseJsonSchema", "{\"type\":\"object\"}");

        ObjectNode planSource = executableStandardPlan("catalog-search");

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), extractedFacts, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertTrue(plan.isExecutable());
        assertEquals(1, candidate.path("assetPlans").size());
        assertEquals("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}",
                candidate.path("assetPlans").get(0).path("requestJsonSchema").asText());
        assertEquals("{\"type\":\"object\"}",
                candidate.path("assetPlans").get(0).path("responseJsonSchema").asText());
    }

    @Test
    @DisplayName("orchestrator should infer schemas from request and response examples")
    void shouldInferSchemasFromExamples() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();
        ObjectNode planSource = executableStandardPlan("catalog-search");
        ObjectNode assetNode = (ObjectNode) planSource.path("assetPlans").get(0);
        assetNode.put("requestExample", "{\"query\":\"bike\",\"limit\":10}");
        assetNode.put("responseExample", "{\"items\":[{\"id\":\"sku-1\"}],\"total\":1}");

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), null, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertTrue(plan.isExecutable());
        assertEquals("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"},\"limit\":{\"type\":\"integer\"}},\"required\":[\"query\",\"limit\"]}",
                candidate.path("assetPlans").get(0).path("requestJsonSchema").asText());
        assertTrue(candidate.path("assetPlans").get(0).path("responseJsonSchema").asText().contains("\"items\":{\"type\":\"array\""));
    }

    @Test
    @DisplayName("orchestrator should reject invalid schema text during plan review")
    void shouldRejectInvalidSchemaTextDuringReview() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();
        ObjectNode planSource = executableStandardPlan("catalog-search");
        ((ObjectNode) planSource.path("assetPlans").get(0)).put("requestJsonSchema", "请求体包含 query");

        ObjectNode candidate = orchestrator.orchestrate(baseRequest(), null, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(baseRequest(), candidate);

        assertFalse(plan.isExecutable());
        assertTrue(candidate.path("assetPlans").get(0).path("requestJsonSchema").isMissingNode());
        assertTrue(plan.getClarificationQuestions().stream().anyMatch(question -> question.contains("请求体 Schema")));
        assertEquals("plan_review", candidate.path("reviewDiagnostics").get(0).path("subagent").asText());
        assertTrue(candidate.path("reviewDiagnostics").get(0).path("structurePatch").toString().contains("assetPlans"));
    }

    @Test
    @DisplayName("orchestrator should keep current valid schema when new schema is invalid")
    void shouldKeepCurrentValidSchemaWhenNewSchemaIsInvalid() throws Exception {
        ImportAgentPlannerSubagentOrchestrator orchestrator = ImportAgentPlannerSubagentOrchestrator.defaultOrchestrator();
        ObjectNode planSource = executableStandardPlan("catalog-search");
        ((ObjectNode) planSource.path("assetPlans").get(0)).put("requestJsonSchema", "not-json");

        ObjectNode candidate = orchestrator.orchestrate(requestWithCurrentSchemaPlan(), null, null, planSource);
        ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(requestWithCurrentSchemaPlan(), candidate);

        assertTrue(plan.isExecutable());
        assertEquals("{\"type\":\"object\"}", candidate.path("assetPlans").get(0).path("requestJsonSchema").asText());
        assertEquals("plan_review", candidate.path("reviewDiagnostics").get(0).path("subagent").asText());
    }

    private ObjectNode executableStandardPlan(String apiCode) {
        ObjectNode planSource = OBJECT_MAPPER.createObjectNode();
        planSource.put("summary", "draft");
        ObjectNode assetNode = planSource.putArray("assetPlans").addObject();
        assetNode.put("apiCode", apiCode);
        assetNode.put("assetName", "Catalog Search");
        assetNode.put("assetType", "STANDARD_API");
        assetNode.put("categoryCode", "catalog");
        assetNode.put("requestMethod", "GET");
        assetNode.put("upstreamUrl", "https://upstream.example.com/catalog/search");
        assetNode.put("authScheme", "NONE");
        assetNode.put("publishAfterImport", true);
        return planSource;
    }

    private ImportAgentPlannerRequest requestWithCurrentSchemaPlan() {
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
                        "https://upstream.example.com/catalog/search",
                        AuthScheme.NONE,
                        null,
                        null,
                        null,
                        null,
                        "{\"type\":\"object\"}",
                        null,
                        true,
                        null,
                        null)));
        return new ImportAgentPlannerRequest(
                "https://docs.example.com/catalog",
                "summary",
                "import catalog api",
                "please continue",
                currentPlan,
                2,
                List.of());
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
