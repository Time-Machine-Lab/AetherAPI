package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiCompatibleImportAgentPlannerProviderTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("supports should require enabled flag and essential credentials")
    void shouldRequireEnabledFlagAndCredentials() {
        HttpClient httpClient = mock(HttpClient.class);
        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(false);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        assertFalse(provider.supports(request()));

        properties.setEnabled(true);
        assertTrue(provider.supports(request()));
    }

    @Test
    @DisplayName("provider should translate OpenAI-compatible response into executable import plan")
    void shouldTranslateOpenAiResponseIntoPlan() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{"
                + "\"choices\":[{"
                + "\"message\":{"
                + "\"content\":\"{\\\"summary\\\":\\\"ready\\\",\\\"assetPlans\\\":[{\\\"apiCode\\\":\\\"weather-forecast\\\",\\\"assetName\\\":\\\"Weather Forecast\\\",\\\"assetType\\\":\\\"AI_API\\\",\\\"categoryCode\\\":\\\"tools\\\",\\\"requestMethod\\\":\\\"GET\\\",\\\"upstreamUrl\\\":\\\"https://upstream.example.com/weather\\\",\\\"authScheme\\\":\\\"HEADER_TOKEN\\\",\\\"authConfig\\\":\\\"Authorization: Bearer upstream-token\\\",\\\"publishAfterImport\\\":true,\\\"aiProfile\\\":{\\\"provider\\\":\\\"OpenAI\\\",\\\"model\\\":\\\"gpt-4.1\\\",\\\"streamingSupported\\\":true,\\\"capabilityTags\\\":[\\\"chat\\\"]}}]}\""
                + "}}]}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        var result = provider.plan(request());

        assertTrue(result.getPlan().isExecutable());
        assertEquals("ready", result.getPlan().getSummary());
        assertEquals("weather-forecast", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("Authorization: Bearer upstream-token", result.getPlan().getAssetPlans().get(0).getAuthConfig());
        assertEquals("tools", result.getPlan().getCategoryPlans().get(0).getCategoryCode());
        assertEquals("OpenAI", result.getPlan().getAssetPlans().get(0).getAiProfile().getProvider());
    }

    @Test
    @DisplayName("provider should extract JSON plan from prose-wrapped response content")
    void shouldExtractJsonPlanFromProseWrappedResponse() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(responseBodyWithContent(
                "Here is the plan you requested:\n```json\n"
                        + "{\"summary\":\"ready\",\"assetPlans\":[{\"apiCode\":\"video-package-submit\",\"assetName\":\"Video Package Submit\",\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"POST\",\"upstreamUrl\":\"https://upstream.example.com/video/package/submit\",\"authScheme\":\"HEADER_TOKEN\",\"authConfig\":\"Authorization: Bearer upstream-token\",\"publishAfterImport\":true}]}"
                        + "\n```"
        ));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        var result = provider.plan(request());

        assertTrue(result.getPlan().isExecutable());
        assertEquals("video-package-submit", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("POST", result.getPlan().getAssetPlans().get(0).getRequestMethod().name());
    }

    @Test
    @DisplayName("provider should read plan from tool call arguments when tool calling response is returned")
    void shouldReadPlanFromToolCallArguments() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(responseBodyWithToolCallArguments(
                "submit_import_plan",
                "{\"summary\":\"ready\",\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\",\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"GET\",\"upstreamUrl\":\"https://upstream.example.com/weather\",\"authScheme\":\"HEADER_TOKEN\",\"authConfig\":\"Authorization: Bearer upstream-token\",\"publishAfterImport\":true}]}"
        ));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setToolCallingEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        var result = provider.plan(request());

        assertTrue(result.getPlan().isExecutable());
        assertEquals("weather-tool", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("Authorization: Bearer upstream-token", result.getPlan().getAssetPlans().get(0).getAuthConfig());
    }

        @Test
        @DisplayName("provider should orchestrate extract fill and submit stages when tool calling is enabled")
        void shouldOrchestrateExtractFillAndSubmitStages() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> extractResponse = mock(HttpResponse.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> fillResponse = mock(HttpResponse.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> submitResponse = mock(HttpResponse.class);
        when(extractResponse.statusCode()).thenReturn(200);
        when(fillResponse.statusCode()).thenReturn(200);
        when(submitResponse.statusCode()).thenReturn(200);
        when(extractResponse.body()).thenReturn(responseBodyWithToolCallArguments(
            "extract_import_facts",
            "{\"assetFacts\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\",\"assetType\":\"STANDARD_API\"}],\"authHints\":[{\"apiCode\":\"weather-tool\",\"authScheme\":\"HEADER_TOKEN\"}]}"));
        when(fillResponse.body()).thenReturn(responseBodyWithToolCallArguments(
            "fill_import_slots",
            "{\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"authConfig\":\"Authorization: Bearer upstream-token\"}],\"remainingMissingSlots\":[]}"));
        when(submitResponse.body()).thenReturn(responseBodyWithToolCallArguments(
            "submit_import_plan",
            "{\"summary\":\"ready\",\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\",\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"GET\",\"upstreamUrl\":\"https://upstream.example.com/weather\",\"authScheme\":\"HEADER_TOKEN\",\"authConfig\":\"Authorization: Bearer upstream-token\",\"publishAfterImport\":true}]}"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(extractResponse, fillResponse, submitResponse);

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setToolCallingEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        var result = provider.plan(request());

        assertTrue(result.getPlan().isExecutable());
        assertEquals("weather-tool", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("Authorization: Bearer upstream-token", result.getPlan().getAssetPlans().get(0).getAuthConfig());
        verify(httpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }

        @Test
        @DisplayName("provider should merge internal subagent outputs into one unified plan result")
        void shouldMergeInternalSubagentOutputsIntoUnifiedPlanResult() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> extractResponse = mock(HttpResponse.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> fillResponse = mock(HttpResponse.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> submitResponse = mock(HttpResponse.class);
        when(extractResponse.statusCode()).thenReturn(200);
        when(fillResponse.statusCode()).thenReturn(200);
        when(submitResponse.statusCode()).thenReturn(200);
        when(extractResponse.body()).thenReturn(responseBodyWithToolCallArguments(
            "extract_import_facts",
            "{\"assetFacts\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\",\"assetType\":\"STANDARD_API\",\"requestMethod\":\"GET\",\"upstreamUrl\":\"https://upstream.example.com/weather\"}],\"authHints\":[{\"apiCode\":\"weather-tool\",\"authScheme\":\"HEADER_TOKEN\"}]}"));
        when(fillResponse.body()).thenReturn(responseBodyWithToolCallArguments(
            "fill_import_slots",
            "{\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"authConfig\":\"Authorization: Bearer upstream-token\"}],\"remainingMissingSlots\":[]}"));
        when(submitResponse.body()).thenReturn(responseBodyWithToolCallArguments(
            "submit_import_plan",
            "{\"summary\":\"ready\",\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\",\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"GET\",\"upstreamUrl\":\"https://upstream.example.com/weather\",\"publishAfterImport\":true}]}"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(extractResponse, fillResponse, submitResponse);

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setToolCallingEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        var result = provider.plan(request());

        assertTrue(result.getPlan().isExecutable());
        assertEquals("HEADER_TOKEN", result.getPlan().getAssetPlans().get(0).getAuthScheme().name());
        assertEquals("Authorization: Bearer upstream-token", result.getPlan().getAssetPlans().get(0).getAuthConfig());
        }

    @Test
    @DisplayName("provider should reject asset plan when auth scheme lacks security config")
    void shouldRejectPlanWhenAuthSchemeLacksSecurityConfig() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(responseBodyWithContent(
                "{\"summary\":\"auth described in prose only\",\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\",\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"GET\",\"upstreamUrl\":\"https://upstream.example.com/weather\",\"authScheme\":\"HEADER_TOKEN\",\"publishAfterImport\":false}]}"
        ));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        var result = provider.plan(request());

        assertFalse(result.getPlan().isExecutable());
        assertTrue(result.getPlan().getClarificationQuestions().stream().anyMatch(question -> question.contains("authConfig")));
    }

    @Test
    @DisplayName("provider should build default planning prompts in Chinese")
    void shouldBuildDefaultPlanningPromptsInChinese() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setToolCallingEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        Method method = OpenAiCompatibleImportAgentPlannerProvider.class.getDeclaredMethod("buildRequestBody", ImportAgentPlannerRequest.class);
        method.setAccessible(true);
        String requestBody = (String) method.invoke(provider, request());

        assertTrue(requestBody.contains("你是一个 API 导入规划助手。"));
        assertTrue(requestBody.contains("信息不足时，请保留已有计划数据"));
        assertTrue(requestBody.contains("请为当前请求准备 API 导入计划"));
        assertTrue(requestBody.contains("不能确认时请追问"));
        assertTrue(requestBody.contains("除非正在调用规划工具，否则只返回原始 JSON"));
        assertTrue(requestBody.contains("提交当前 API 导入请求的完整导入计划"));
        assertTrue(requestBody.contains("查询接口必须并入提交接口的 asyncTaskConfig"));
        assertTrue(requestBody.contains("请把答案写回 currentPlanJson"));
        assertTrue(requestBody.contains("不要假设后端会从自由文本自动补齐"));
        assertTrue(requestBody.contains("asyncTaskConfig.authMode"));
        assertTrue(requestBody.contains("aiProfile.provider"));
        assertFalse(requestBody.contains("UPSTREAM_API_KEY"));
        assertFalse(requestBody.contains("When information is missing"));
        assertFalse(requestBody.contains("Prepare an import plan"));
        assertFalse(requestBody.contains("Return only raw JSON"));
        assertFalse(requestBody.contains("Submit the full import plan"));
    }

    private ImportAgentPlannerRequest request() {
        return new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "please continue",
                null,
                2,
                List.of()
        );
    }

    private String responseBodyWithContent(String content) throws Exception {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        ObjectNode message = root.putArray("choices").addObject().putObject("message");
        message.put("content", content);
        return OBJECT_MAPPER.writeValueAsString(root);
    }

    private String responseBodyWithToolCallArguments(String toolName, String arguments) throws Exception {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        ObjectNode message = root.putArray("choices").addObject().putObject("message");
        ObjectNode toolCall = message.putArray("tool_calls").addObject();
        toolCall.put("type", "function");
        toolCall.putObject("function")
                .put("name", toolName)
                .put("arguments", arguments);
        return OBJECT_MAPPER.writeValueAsString(root);
    }
}
