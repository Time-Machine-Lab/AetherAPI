package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
                + "\"content\":\"{\\\"summary\\\":\\\"ready\\\",\\\"assetPlans\\\":[{\\\"apiCode\\\":\\\"weather-forecast\\\",\\\"assetName\\\":\\\"Weather Forecast\\\",\\\"assetType\\\":\\\"AI_API\\\",\\\"categoryCode\\\":\\\"tools\\\",\\\"requestMethod\\\":\\\"GET\\\",\\\"upstreamUrl\\\":\\\"https://upstream.example.com/weather\\\",\\\"authScheme\\\":\\\"HEADER_TOKEN\\\",\\\"publishAfterImport\\\":true,\\\"aiProfile\\\":{\\\"provider\\\":\\\"OpenAI\\\",\\\"model\\\":\\\"gpt-4.1\\\",\\\"streamingSupported\\\":true,\\\"capabilityTags\\\":[\\\"chat\\\"]}}]}\""
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
                        + "{\"summary\":\"ready\",\"assetPlans\":[{\"apiCode\":\"video-package-submit\",\"assetName\":\"Video Package Submit\",\"assetType\":\"STANDARD_API\",\"categoryCode\":\"tools\",\"requestMethod\":\"POST\",\"upstreamUrl\":\"https://upstream.example.com/video/package/submit\",\"authScheme\":\"HEADER_TOKEN\",\"publishAfterImport\":true}]}"
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
}