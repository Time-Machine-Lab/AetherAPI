package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentReplyPort replyPort = new OpenAiCompatibleImportAgentReplyPort(httpClient, properties);

        List<String> deltas = new ArrayList<>();
        String result = replyPort.streamReply(
                new ImportAgentPlannerRequest("source", "summary", "intent", "message", null, 1, List.of()),
                new ImportAgentPlanModel(1, true, "ready", List.of(), List.of(), List.of()),
                deltas::add
        );

        assertEquals(List.of("Hello ", "world"), deltas);
        assertEquals("Hello world", result);
    }

    @Test
    @DisplayName("reply port should build default reply prompts in Chinese")
    void shouldBuildDefaultReplyPromptsInChinese() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        ImportAgentLlmPlannerProperties properties = new ImportAgentLlmPlannerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setApiKey("sk-test");
        properties.setModel("gpt-4.1-mini");
        OpenAiCompatibleImportAgentReplyPort replyPort = new OpenAiCompatibleImportAgentReplyPort(httpClient, properties);

        Method method = OpenAiCompatibleImportAgentReplyPort.class.getDeclaredMethod(
                "buildRequestBody",
                ImportAgentPlannerRequest.class,
                ImportAgentPlanModel.class
        );
        method.setAccessible(true);
        String requestBody = (String) method.invoke(
                replyPort,
                new ImportAgentPlannerRequest("source", "summary", "intent", "message", null, 1, List.of()),
                new ImportAgentPlanModel(1, true, "ready", List.of(), List.of(), List.of())
        );

        assertTrue(requestBody.contains("你是一个 API 导入助手"));
        assertTrue(requestBody.contains("请基于最终导入计划回复操作员"));
        assertTrue(requestBody.contains("如果 clarificationQuestions 存在"));
        assertFalse(requestBody.contains("You are an API import assistant"));
        assertFalse(requestBody.contains("Respond to the operator"));
        assertFalse(requestBody.contains("If clarificationQuestions exists"));
    }
}
