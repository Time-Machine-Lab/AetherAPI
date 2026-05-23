package io.github.timemachinelab.infrastructure.importagent.planner.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentCategoryCandidateModel;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        ImportAgentLlmPlannerProperties properties = properties(false, false);
        OpenAiCompatibleImportAgentPlannerProvider provider = new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties);

        assertFalse(provider.supports(request()));

        properties.setEnabled(true);
        assertTrue(provider.supports(request()));
    }

    @Test
    @DisplayName("provider should run the declared eight-stage LLM control loop")
    void shouldRunDeclaredEightStageControlLoop() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> goal = response(content("{\"goal\":\"import weather\"}"));
        HttpResponse<String> observation = response(tool("extract_import_facts", "{\"assetFacts\":[{\"apiCode\":\"weather-tool\"}]}"));
        HttpResponse<String> state = response(tool("fill_import_slots", "{\"assetPlans\":[{\"apiCode\":\"weather-tool\"}]}"));
        HttpResponse<String> gaps = response(content("{\"missingFields\":[]}"));
        HttpResponse<String> control = response(content("{\"decision\":\"synthesize\"}"));
        HttpResponse<String> synthesis = response(content("{\"summary\":\"draft\",\"assetPlans\":[{\"apiCode\":\"weather-tool\"}]}"));
        HttpResponse<String> review = response(content("{\"summary\":\"reviewed\",\"assetPlans\":[{\"apiCode\":\"weather-tool\"}]}"));
        HttpResponse<String> finalPlan = response(tool("submit_import_plan",
                "{\"summary\":\"ready\",\"categoryPlans\":[{\"categoryCode\":\"tools\",\"action\":\"USE_EXISTING\"}],"
                        + "\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"assetName\":\"Weather Tool\","
                        + "\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"GET\","
                        + "\"upstreamUrl\":\"https://upstream.example.com/weather\","
                        + "\"authScheme\":\"NONE\",\"publishAfterImport\":true}]}"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(goal, observation, state, gaps, control, synthesis, review, finalPlan);

        OpenAiCompatibleImportAgentPlannerProvider provider =
                new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties(true, true));

        var result = provider.plan(request());

        assertTrue(result.getPlan().isExecutable());
        assertEquals("ready", result.getPlan().getSummary());
        assertEquals("weather-tool", result.getPlan().getAssetPlans().get(0).getApiCode());
        verify(httpClient, times(8)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("provider should keep missing execution fields non-executable")
    void shouldKeepMissingExecutionFieldsNonExecutable() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> empty = response(content("{}"));
        HttpResponse<String> finalPlan = response(content("{\"summary\":\"draft\",\"assetPlans\":[{\"apiCode\":\"weather-tool\",\"publishAfterImport\":true}]}"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(empty, empty, empty, empty, empty, empty, empty, finalPlan);

        OpenAiCompatibleImportAgentPlannerProvider provider =
                new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties(true, false));

        var result = provider.plan(request());

        assertFalse(result.getPlan().isExecutable());
        assertTrue(result.getPlan().getClarificationQuestions().stream()
                .anyMatch(question -> question.contains("资产名称")));
    }

    @Test
    @DisplayName("provider should include final plan tool and category candidates in request body")
    void shouldBuildFinalPlanRequestBody() throws Exception {
        OpenAiCompatibleImportAgentPlannerProvider provider =
                new OpenAiCompatibleImportAgentPlannerProvider(mock(HttpClient.class), properties(true, true));

        Method method = OpenAiCompatibleImportAgentPlannerProvider.class.getDeclaredMethod("buildRequestBody", ImportAgentPlannerRequest.class);
        method.setAccessible(true);
        String requestBody = (String) method.invoke(provider, requestWithCategories());

        assertTrue(requestBody.contains("submit_import_plan"));
        assertTrue(requestBody.contains("availableCategoriesJson"));
        assertTrue(requestBody.contains("\\\"categoryCode\\\":\\\"video\\\""));
        assertTrue(requestBody.contains("LLM 负责目标捕获"));
    }

    @Test
    @DisplayName("provider should include response body when request is rejected")
    void shouldIncludeResponseBodyWhenRequestIsRejected() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn("{\"error\":{\"message\":\"unsupported parameter\"}}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        OpenAiCompatibleImportAgentPlannerProvider provider =
                new OpenAiCompatibleImportAgentPlannerProvider(httpClient, properties(true, true));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> provider.plan(request()));

        assertTrue(exception.getMessage().contains("status 400"));
        assertTrue(exception.getMessage().contains("unsupported parameter"));
    }

    private ImportAgentPlannerRequest request() {
        return new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "please continue",
                null,
                2,
                List.of());
    }

    private ImportAgentPlannerRequest requestWithCategories() {
        return new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "summary",
                "import weather api",
                "please continue",
                null,
                2,
                List.of(),
                List.of(new ImportAgentCategoryCandidateModel("video", "Video", "ENABLED")));
    }

    private ImportAgentLlmPlannerProperties properties(boolean enabled, boolean toolCallingEnabled) {
        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(enabled);
        properties.setToolCallingEnabled(toolCallingEnabled);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setEndpointPath("/chat/completions");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        return properties;
    }

    private HttpResponse<String> response(String body) {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(body);
        return response;
    }

    private String content(String content) throws Exception {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.putArray("choices").addObject().putObject("message").put("content", content);
        return OBJECT_MAPPER.writeValueAsString(root);
    }

    private String tool(String toolName, String arguments) throws Exception {
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
