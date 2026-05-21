package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;
import io.github.timemachinelab.service.port.out.ApiImportAgentReplyPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * OpenAI-compatible streamed reply generator for import-agent chat responses.
 */
@Component
public class OpenAiCompatibleImportAgentReplyPort implements ApiImportAgentReplyPort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_ENDPOINT_PATH = "/chat/completions";
    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一个 API 导入助手，负责回复操作员。
            请使用用户的语言回复。
            你会收到最新用户消息和最终导入计划。
            如果 clarificationQuestions 非空，请自然、清晰地逐条提出这些问题。
            如果计划可执行，请总结关键导入决策，并请用户确认计划。
            不要输出 JSON 或 markdown 代码围栏。
            回复要简洁但具体。
            """;

    private final HttpClient httpClient;
    private final ImportAgentLlmPlannerProperties properties;

    public OpenAiCompatibleImportAgentReplyPort(HttpClient httpClient, ImportAgentLlmPlannerProperties properties) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null");
        this.properties = Objects.requireNonNull(properties, "LLM planner properties must not be null");
    }

    @Override
    public String streamReply(
            ImportAgentPlannerRequest request,
            ImportAgentPlanModel plan,
            ImportAgentStreamEmitter streamEmitter) {
        ImportAgentStreamEmitter stream = streamEmitter == null ? ImportAgentStreamEmitter.noop() : streamEmitter;
        if (!properties.isEnabled()
                || isBlank(properties.getBaseUrl())
                || isBlank(properties.getApiKey())
                || isBlank(properties.getModel())) {
            throw new IllegalStateException("Import agent reply streaming is not configured");
        }
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(buildEndpointUri())
                    .header("Authorization", "Bearer " + properties.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .timeout(resolveRequestTimeout())
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(request, plan), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Import agent reply request failed with status " + response.statusCode());
            }

            StringBuilder reply = new StringBuilder();
            try (Stream<String> lines = response.body()) {
                lines.forEach(line -> consumeLine(line, reply, stream));
            }
            String finalReply = reply.toString().trim();
            if (finalReply.isBlank()) {
                throw new IllegalStateException("Import agent reply stream returned no textual content");
            }
            return finalReply;
        } catch (IOException ex) {
            throw new IllegalStateException("Import agent reply request failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Import agent reply request interrupted", ex);
        }
    }

    private void consumeLine(String line, StringBuilder reply, ImportAgentStreamEmitter streamEmitter) {
        if (line == null) {
            return;
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith("data:")) {
            return;
        }
        String data = trimmed.substring(5).trim();
        if (data.isBlank() || "[DONE]".equals(data)) {
            return;
        }
        try {
            JsonNode event = OBJECT_MAPPER.readTree(data);
            JsonNode choices = event.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return;
            }
            JsonNode delta = choices.get(0).path("delta");
            String content = delta.path("content").asText(null);
            if (content == null || content.isEmpty()) {
                return;
            }
            reply.append(content);
            streamEmitter.message(ImportAgentActorType.AGENT, content);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse import agent reply stream chunk", ex);
        }
    }

    private URI buildEndpointUri() {
        String baseUrl = properties.getBaseUrl().trim();
        String endpointPath = hasText(properties.getEndpointPath()) ? properties.getEndpointPath().trim() : DEFAULT_ENDPOINT_PATH;
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
        return URI.create(normalizedBaseUrl + normalizedPath);
    }

    private Duration resolveRequestTimeout() {
        Integer requestTimeoutSeconds = properties.getRequestTimeoutSeconds();
        if (requestTimeoutSeconds == null || requestTimeoutSeconds <= 0) {
            return Duration.ofSeconds(30);
        }
        return Duration.ofSeconds(requestTimeoutSeconds);
    }

    private String buildRequestBody(ImportAgentPlannerRequest request, ImportAgentPlanModel plan) throws IOException {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", properties.getModel().trim());
        root.put("stream", true);
        if (properties.getTemperature() != null) {
            root.put("temperature", properties.getTemperature());
        }
        if (properties.getMaxCompletionTokens() != null && properties.getMaxCompletionTokens() > 0) {
            root.put("max_completion_tokens", properties.getMaxCompletionTokens());
        }
        ArrayNode messages = root.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", hasText(properties.getSystemPrompt()) ? properties.getSystemPrompt().trim() : DEFAULT_SYSTEM_PROMPT);
        messages.addObject()
                .put("role", "user")
                .put("content", buildUserPrompt(request, plan));
        return OBJECT_MAPPER.writeValueAsString(root);
    }

    private String buildUserPrompt(ImportAgentPlannerRequest request, ImportAgentPlanModel plan) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于最终导入计划回复操作员。\n");
        appendField(prompt, "documentSource", request.getDocumentSource());
        appendField(prompt, "documentSummary", request.getDocumentSummary());
        appendField(prompt, "importIntent", request.getImportIntent());
        appendField(prompt, "latestUserMessage", request.getLatestUserMessage());
        prompt.append("finalPlanJson:\n").append(OBJECT_MAPPER.writeValueAsString(plan)).append("\n");
        prompt.append("如果 clarificationQuestions 存在，请自然地提出这些问题；否则总结计划并请求用户确认。\n");
        return prompt.toString();
    }

    private void appendField(StringBuilder prompt, String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return;
        }
        prompt.append(fieldName).append(":\n").append(fieldValue.trim()).append("\n");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
