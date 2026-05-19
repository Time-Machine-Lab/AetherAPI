package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import org.springframework.beans.factory.annotation.Autowired;
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
            你是一个 API 导入规划助手。
            如果提供了 currentPlanJson，请将其视为当前基线，并在应用最新用户消息后返回完整更新后的 JSON 计划，而不是只返回增量字段。
            如果用户提供的信息不足，请先保留已有计划并尝试自动补槽，再使用用户的语言在 clarificationQuestions 中提出简洁、具体的追问。
            当最新用户消息已经回答了缺失项时，请优先把答案写回对应字段，而不是重复追问。
            如果上游 API 是“提交任务后通过任务 ID 查询结果”的异步模式，请将查询信息并入提交资产的 asyncTaskConfig，不要拆成独立资产。
            只根据已提供的文档、当前计划和对话内容作答；不要编造未知值、默认密钥、环境变量名或平台特例。
            """;

    private final HttpClient httpClient;
    private final ImportAgentLlmPlannerProperties properties;
    private final ImportAgentPlanningToolRegistry toolRegistry;

    @Autowired
    public OpenAiCompatibleImportAgentPlannerProvider(
            HttpClient httpClient,
            ImportAgentLlmPlannerProperties properties,
            ImportAgentPlanningToolRegistry toolRegistry) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null");
        this.properties = Objects.requireNonNull(properties, "LLM planner properties must not be null");
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "Planning tool registry must not be null");
    }

    OpenAiCompatibleImportAgentPlannerProvider(
            HttpClient httpClient,
            ImportAgentLlmPlannerProperties properties) {
        this(httpClient, properties, ImportAgentPlanningToolRegistry.defaultRegistry());
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
            JsonNode planSource;
            if (properties.isToolCallingEnabled()) {
                JsonNode extractedFacts = executePlanningStage(request, PlannerStage.EXTRACT_FACTS, null, null);
                JsonNode slotPatches = executePlanningStage(request, PlannerStage.FILL_SLOTS, extractedFacts, null);
                planSource = executePlanningStage(request, PlannerStage.SUBMIT_PLAN, extractedFacts, slotPatches);
            } else {
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
                planSource = extractPlanSource(payload, null, true);
            }
            if (planSource == null) {
                throw new IllegalStateException("LLM planner returned non-JSON plan content");
            }
            ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(request, planSource);
            return new ImportAgentPlannerResult(plan, ImportAgentPlannerJsonSupport.buildAgentMessage("LLM 规划器", plan));
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
        return buildRequestBody(request, PlannerStage.SUBMIT_PLAN, null, null);
    }

    private String buildRequestBody(
            ImportAgentPlannerRequest request,
            PlannerStage stage,
            JsonNode extractedFacts,
            JsonNode slotPatches) {
        ImportAgentPlanningToolDescriptor primaryTool = toolRegistry.primaryTool(stage);
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
                .put("content", buildUserPrompt(request, stage, primaryTool, extractedFacts, slotPatches));
        if (properties.isToolCallingEnabled()) {
            root.set("tools", toolRegistry.buildTools(stage, OBJECT_MAPPER));
            root.putObject("tool_choice")
                .put("type", "function")
                .putObject("function")
                .put("name", primaryTool.name());
        }
        return root.toString();
    }

    private String buildUserPrompt(
            ImportAgentPlannerRequest request,
            PlannerStage stage,
            ImportAgentPlanningToolDescriptor primaryTool,
            JsonNode extractedFacts,
            JsonNode slotPatches) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为当前请求准备 API 导入计划。\n");
        prompt.append("当前阶段: ").append(stage.getStageLabel()).append("\n");
        appendField(prompt, "documentSource", request.getDocumentSource());
        appendField(prompt, "documentSummary", request.getDocumentSummary());
        appendField(prompt, "importIntent", request.getImportIntent());
        appendField(prompt, "latestUserMessage", request.getLatestUserMessage());
        prompt.append("nextPlanVersion: ").append(request.getNextPlanVersion()).append("\n");
        if (!request.getTurns().isEmpty()) {
            prompt.append("recentTurnsJson:\n").append(serializeTurns(request)).append("\n");
        }
        if (request.getCurrentPlan() != null) {
            prompt.append("currentPlanSummary: ").append(request.getCurrentPlan().getSummary()).append("\n");
            prompt.append("currentPlanJson:\n").append(serializeCurrentPlan(request)).append("\n");
        }
        if (extractedFacts != null) {
            prompt.append("extractedFactsJson:\n").append(extractedFacts.toString()).append("\n");
        }
        if (slotPatches != null) {
            prompt.append("slotPatchesJson:\n").append(slotPatches.toString()).append("\n");
        }
        prompt.append("信息不足时，请保留已有计划数据，并返回自然对话式的 clarificationQuestions，不要返回空洞的通用占位问题。\n");
        prompt.append("异步任务模式下，查询接口必须并入提交接口的 asyncTaskConfig，不要作为独立资产导入。\n");
        prompt.append("需要发布且使用鉴权的资产必须有明确 authConfig；不能确认时请追问，不要编造默认密钥、环境变量名或平台特例。\n");
        prompt.append("如果资产使用 HEADER_TOKEN 或 QUERY_TOKEN，必须把安全配置写进 assetPlans[].authConfig；不要只在计划摘要或回复文案中描述。\n");
        prompt.append("如果用户正在回答某个缺失项，请把答案写回 currentPlanJson 对应字段，并返回更新后的完整计划。\n");
        if (primaryTool != null) {
            String stageInstruction = primaryTool.tool().stagePromptInstruction();
            if (hasText(stageInstruction)) {
                prompt.append(stageInstruction).append("\n");
            }
        }
        prompt.append("除非正在调用规划工具，否则只返回原始 JSON。\n");
        return prompt.toString();
    }

    private String serializeCurrentPlan(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getCurrentPlan());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize current import plan", ex);
        }
    }

    private String serializeTurns(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getTurns());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize import agent turns", ex);
        }
    }

    private void appendField(StringBuilder prompt, String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return;
        }
        prompt.append(fieldName).append(":\n").append(fieldValue.trim()).append("\n");
    }

    private JsonNode executePlanningStage(
            ImportAgentPlannerRequest request,
            PlannerStage stage,
            JsonNode extractedFacts,
            JsonNode slotPatches) throws IOException, InterruptedException {
        ImportAgentPlanningToolDescriptor primaryTool = toolRegistry.primaryTool(stage);
        HttpRequest httpRequest = HttpRequest.newBuilder(buildEndpointUri())
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .header("Content-Type", "application/json")
                .timeout(resolveRequestTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(
                        buildRequestBody(request, stage, extractedFacts, slotPatches),
                        StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("LLM planner request failed with status " + response.statusCode());
        }
        JsonNode payload = OBJECT_MAPPER.readTree(response.body());
        return extractPlanSource(payload, primaryTool.name(), primaryTool.tool().requiresStrictContentFallback());
    }

    private JsonNode extractPlanSource(JsonNode payload, String expectedToolName, boolean strict) {
        JsonNode toolArguments = extractToolArguments(payload, expectedToolName);
        if (toolArguments != null) {
            return toolArguments;
        }
        String content = extractContent(payload, strict);
        if (content == null) {
            return null;
        }
        return ImportAgentPlannerJsonSupport.parseJsonCandidate(content);
    }

    private JsonNode extractToolArguments(JsonNode payload, String expectedToolName) {
        JsonNode choices = payload.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("LLM planner response is missing choices");
        }
        JsonNode messageNode = choices.get(0).path("message");
        JsonNode toolCalls = messageNode.path("tool_calls");
        if (toolCalls.isArray()) {
            for (JsonNode toolCall : toolCalls) {
                if (!"function".equals(toolCall.path("type").asText())) {
                    continue;
                }
                JsonNode functionNode = toolCall.path("function");
                String toolName = functionNode.path("name").asText();
                if (expectedToolName != null && !expectedToolName.equals(toolName)) {
                    continue;
                }
                JsonNode parsed = ImportAgentPlannerJsonSupport.parseJsonCandidate(functionNode.path("arguments").asText(null));
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        JsonNode functionCall = messageNode.path("function_call");
        if (!functionCall.isMissingNode()) {
            String toolName = functionCall.path("name").asText();
            if (expectedToolName != null && !expectedToolName.equals(toolName)) {
                return null;
            }
            return ImportAgentPlannerJsonSupport.parseJsonCandidate(functionCall.path("arguments").asText(null));
        }
        return null;
    }

    private String extractContent(JsonNode payload, boolean strict) {
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
        if (!strict) {
            return null;
        }
        throw new IllegalStateException("LLM planner response content is not textual");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
