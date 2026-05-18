package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * OpenAI-compatible import agent planner provider.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenAiCompatibleImportAgentPlannerProvider implements ImportAgentPlannerProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_ENDPOINT_PATH = "/chat/completions";
    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are an API import planning assistant.
            Return only one raw JSON object and no markdown fences.
            The JSON object may contain these fields: summary, clarificationQuestions, categoryPlans, assetPlans.
            categoryPlans is an array of objects with categoryCode, categoryName, action where action is USE_EXISTING or CREATE_IF_MISSING.
            assetPlans is an array of objects with apiCode, assetName, assetType, categoryCode, requestMethod, upstreamUrl, authScheme, authConfig, requestTemplate, requestExample, responseExample, requestJsonSchema, responseJsonSchema, publishAfterImport, aiProfile.
            assetType must be STANDARD_API or AI_API.
            requestMethod must be GET, POST, PUT, PATCH, or DELETE.
            authScheme must be NONE, HEADER_TOKEN, or QUERY_TOKEN.
            aiProfile, when present, must contain provider, model, streamingSupported, capabilityTags.
            Keep fields absent instead of fabricating values you do not know.
            """;

    private final HttpClient httpClient;
    private final ImportAgentLlmPlannerProperties properties;

    public OpenAiCompatibleImportAgentPlannerProvider(
            HttpClient httpClient,
            ImportAgentLlmPlannerProperties properties) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null");
        this.properties = Objects.requireNonNull(properties, "LLM planner properties must not be null");
    }

    @Override
    public boolean supports(ImportAgentPlannerRequest request) {
        return properties.isEnabled()
                && hasText(properties.getBaseUrl())
                && hasText(properties.getApiKey())
                && hasText(properties.getModel());
    }

    @Override
    public ImportAgentPlannerResult plan(ImportAgentPlannerRequest request) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(buildEndpointUri())
                    .header("Authorization", "Bearer " + properties.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .timeout(resolveRequestTimeout())
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(request), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("LLM planner request failed with status " + response.statusCode());
            }
            JsonNode payload = OBJECT_MAPPER.readTree(response.body());
            JsonNode planSource = ImportAgentPlannerJsonSupport.parseJsonCandidate(extractContent(payload));
            if (planSource == null) {
                throw new IllegalStateException("LLM planner returned non-JSON plan content");
            }
            ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(request, planSource);
            return new ImportAgentPlannerResult(plan, ImportAgentPlannerJsonSupport.buildAgentMessage("LLM planner", plan));
        } catch (IOException ex) {
            throw new IllegalStateException("LLM planner request failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM planner request interrupted", ex);
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

    private String buildRequestBody(ImportAgentPlannerRequest request) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", properties.getModel().trim());
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
                .put("content", buildUserPrompt(request));
        return root.toString();
    }

    private String buildUserPrompt(ImportAgentPlannerRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Prepare an import plan for the current request.\n");
        appendField(prompt, "documentSource", request.getDocumentSource());
        appendField(prompt, "documentSummary", request.getDocumentSummary());
        appendField(prompt, "importIntent", request.getImportIntent());
        appendField(prompt, "latestUserMessage", request.getLatestUserMessage());
        prompt.append("nextPlanVersion: ").append(request.getNextPlanVersion()).append("\n");
        if (request.getCurrentPlan() != null) {
            prompt.append("currentPlanSummary: ").append(request.getCurrentPlan().getSummary()).append("\n");
        }
        prompt.append("Return only raw JSON.\n");
        return prompt.toString();
    }

    private void appendField(StringBuilder prompt, String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return;
        }
        prompt.append(fieldName).append(":\n").append(fieldValue.trim()).append("\n");
    }

    private String extractContent(JsonNode payload) {
        JsonNode choices = payload.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("LLM planner response is missing choices");
        }
        JsonNode contentNode = choices.get(0).path("message").path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (contentNode.isArray()) {
            StringBuilder text = new StringBuilder();
            for (JsonNode item : contentNode) {
                if (item.path("type").asText().equals("text") && item.path("text").isTextual()) {
                    text.append(item.path("text").asText());
                }
            }
            if (text.length() > 0) {
                return text.toString();
            }
        }
        throw new IllegalStateException("LLM planner response content is not textual");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}