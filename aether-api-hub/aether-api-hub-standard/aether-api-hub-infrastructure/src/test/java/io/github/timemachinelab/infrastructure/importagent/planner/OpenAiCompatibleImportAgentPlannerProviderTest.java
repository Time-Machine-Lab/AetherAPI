package io.github.timemachinelab.infrastructure.importagent.planner;

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
}