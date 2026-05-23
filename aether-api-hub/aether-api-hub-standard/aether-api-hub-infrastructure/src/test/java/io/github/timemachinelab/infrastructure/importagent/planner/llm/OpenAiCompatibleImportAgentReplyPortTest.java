package io.github.timemachinelab.infrastructure.importagent.planner.llm;

import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentStreamEvent;
import io.github.timemachinelab.service.model.ImportAgentStreamEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAiCompatibleImportAgentReplyPortTest {

    @Test
    @DisplayName("reply port should stream assistant deltas from SSE lines")
    void shouldStreamAssistantDeltas() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<Stream<String>> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(Stream.of(
                "data: {\"choices\":[{\"delta\":{\"content\":\"Hello \"}}]}",
                "data: {\"choices\":[{\"delta\":{\"content\":\"world\"}}]}",
                "data: [DONE]"
        ));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        OpenAiCompatibleImportAgentReplyPort replyPort = new OpenAiCompatibleImportAgentReplyPort(httpClient, properties());

        List<String> deltas = new ArrayList<>();
        String result = replyPort.streamReply(
                new ImportAgentPlannerRequest("source", "summary", "intent", "message", null, 1, List.of()),
                new ImportAgentPlanModel(1, true, "ready", List.of(), List.of(), List.of()),
                event -> collectMessageDelta(event, deltas));

        assertEquals(List.of("Hello ", "world"), deltas);
        assertEquals("Hello world", result);
    }

    @Test
    @DisplayName("reply port should build an OpenAI-compatible streaming request")
    void shouldBuildOpenAiCompatibleStreamingRequest() throws Exception {
        OpenAiCompatibleImportAgentReplyPort replyPort =
                new OpenAiCompatibleImportAgentReplyPort(mock(HttpClient.class), properties());

        Method method = OpenAiCompatibleImportAgentReplyPort.class.getDeclaredMethod(
                "buildRequestBody",
                ImportAgentPlannerRequest.class,
                ImportAgentPlanModel.class);
        method.setAccessible(true);
        String requestBody = (String) method.invoke(
                replyPort,
                new ImportAgentPlannerRequest("source", "summary", "intent", "message", null, 1, List.of()),
                new ImportAgentPlanModel(1, true, "ready", List.of(), List.of(), List.of()));

        assertTrue(requestBody.contains("\"model\":\"gpt-4.1-mini\""));
        assertTrue(requestBody.contains("\"stream\":true"));
        assertTrue(requestBody.contains("finalPlanJson"));
    }

    private void collectMessageDelta(ImportAgentStreamEvent event, List<String> deltas) {
        if (event.getType() != ImportAgentStreamEventType.MESSAGE) {
            return;
        }
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) event.getPayload();
        deltas.add((String) payload.get("delta"));
    }

    private ImportAgentLlmPlannerProperties properties() {
        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        return properties;
    }
}
