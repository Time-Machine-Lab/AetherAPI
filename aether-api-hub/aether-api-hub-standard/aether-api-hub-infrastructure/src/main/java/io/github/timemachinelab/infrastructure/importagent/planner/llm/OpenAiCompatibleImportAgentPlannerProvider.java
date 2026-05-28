package io.github.timemachinelab.infrastructure.importagent.planner.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentRegistry;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerAgentSpec;
import io.github.timemachinelab.infrastructure.importagent.planner.agents.ImportAgentPlannerOutputMode;
import io.github.timemachinelab.infrastructure.importagent.planner.contract.ImportAgentPlannerJsonSupport;
import io.github.timemachinelab.infrastructure.importagent.planner.orchestration.ImportAgentPlannerContext;
import io.github.timemachinelab.infrastructure.importagent.planner.orchestration.ImportAgentPlannerProvider;
import io.github.timemachinelab.infrastructure.importagent.planner.orchestration.ImportAgentPlannerRuntime;
import io.github.timemachinelab.infrastructure.importagent.planner.orchestration.ImportAgentPlannerRuntimeResult;
import io.github.timemachinelab.infrastructure.importagent.planner.orchestration.ImportAgentPlannerStageResult;
import io.github.timemachinelab.infrastructure.importagent.planner.stream.ImportAgentPlannerStreamSummaries;
import io.github.timemachinelab.infrastructure.importagent.planner.tools.ImportAgentPlanningToolDescriptor;
import io.github.timemachinelab.infrastructure.importagent.planner.tools.ImportAgentPlanningToolRegistry;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Objects;

/**
 * OpenAI-compatible import-agent planner provider.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenAiCompatibleImportAgentPlannerProvider implements ImportAgentPlannerProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleImportAgentPlannerProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_ENDPOINT_PATH = "/chat/completions";
    private static final int ERROR_BODY_LOG_LIMIT = 1200;
    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是 API 导入 Agent 的规划器。
            LLM 负责目标捕获、观察、状态估计、差距比较、计划控制、计划生成、计划审查和最终计划提交。
            平台代码只会解析并校验最终计划边界，不会替你修复、排序、猜测或编造规划值。
            不要暴露凭据、原始思维链或供应商原始载荷。
            不要执行目录写入。最终计划必须等待用户显式确认后才能执行。
            """;

    private final HttpClient httpClient;
    private final ImportAgentLlmPlannerProperties properties;
    private final ImportAgentPlanningToolRegistry toolRegistry;
    private final ImportAgentPlannerRuntime runtime;

    @Autowired
    public OpenAiCompatibleImportAgentPlannerProvider(
            HttpClient httpClient,
            ImportAgentLlmPlannerProperties properties,
            ImportAgentPlanningToolRegistry toolRegistry,
            ImportAgentPlannerRuntime runtime) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP 客户端不能为空");
        this.properties = Objects.requireNonNull(properties, "LLM 规划配置不能为空");
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "规划工具注册表不能为空");
        this.runtime = Objects.requireNonNull(runtime, "规划运行时不能为空");
    }

    OpenAiCompatibleImportAgentPlannerProvider(
            HttpClient httpClient,
            ImportAgentLlmPlannerProperties properties) {
        this(
                httpClient,
            properties,
            ImportAgentPlanningToolRegistry.defaultRegistry(),
            new ImportAgentPlannerRuntime());
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
        return plan(request, ImportAgentStreamEmitter.noop());
    }

    @Override
    public ImportAgentPlannerResult plan(ImportAgentPlannerRequest request, ImportAgentStreamEmitter streamEmitter) {
        ImportAgentStreamEmitter stream = streamEmitter == null ? ImportAgentStreamEmitter.noop() : streamEmitter;
        try {
            log.info("Import-agent planner start: nextPlanVersion={}, toolCallingEnabled={}, hasCurrentPlan={}, turns={}",
                    request.getNextPlanVersion(),
                    properties.isToolCallingEnabled(),
                    request.getCurrentPlan() != null,
                    request.getTurns().size());
            stream.thinking("planner", "规划已开始", "API 导入 Agent 正在运行 LLM 控制环。");
            ImportAgentPlannerRuntimeResult runtimeResult = runtime.run(
                    request,
                    stream,
                    (agent, context) -> executeAgentStage(request, agent, context));
            JsonNode planSource = runtimeResult.finalPlanSource();
            if (planSource == null || !planSource.isObject()) {
                throw new IllegalStateException("LLM 规划器返回的最终计划内容不是 JSON 对象");
            }
            log.debug("Import-agent planner final candidate: {}", ImportAgentPlannerStreamSummaries.summarize(planSource));
            ImportAgentPlanModel plan = ImportAgentPlannerJsonSupport.buildPlan(request, planSource);
            log.info("Import-agent planner complete: executable={}, categoryPlans={}, assetPlans={}, clarificationQuestions={}",
                    plan.isExecutable(),
                    plan.getCategoryPlans().size(),
                    plan.getAssetPlans().size(),
                    plan.getClarificationQuestions().size());
            stream.thinking(
                    "planner.complete",
                    "规划已完成",
                    "最终计划已准备好。",
                    ImportAgentPlannerStreamSummaries.summarize(planSource));
            return new ImportAgentPlannerResult(plan, ImportAgentPlannerJsonSupport.buildAgentMessage("LLM 规划器", plan));
        } catch (IOException ex) {
            log.error("Import-agent planner IO failure", ex);
            throw new IllegalStateException("LLM 规划请求失败", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Import-agent planner interrupted", ex);
            throw new IllegalStateException("LLM 规划请求被中断", ex);
        } catch (RuntimeException ex) {
            log.error("Import-agent planner failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private ImportAgentPlannerStageResult executeAgentStage(
            ImportAgentPlannerRequest request,
            ImportAgentPlannerAgentSpec agent,
            ImportAgentPlannerContext context) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(buildEndpointUri())
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .header("Content-Type", "application/json")
                .timeout(resolveRequestTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(
                        buildRequestBody(request, agent, context),
                        StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorBody = summarizeErrorBody(response.body());
            log.warn("Import-agent planner stage failed: agent={}, status={}, body={}", agent.name(), response.statusCode(), errorBody);
            throw new IllegalStateException("LLM 规划请求失败，状态码：" + response.statusCode() + "，响应：" + errorBody);
        }
        JsonNode payload = OBJECT_MAPPER.readTree(response.body());
        JsonNode stageOutput = extractStageOutput(payload, agent);
        log.debug("Import-agent planner stage complete: agent={}, output={}",
                agent.name(), ImportAgentPlannerStreamSummaries.summarize(stageOutput));
        return new ImportAgentPlannerStageResult(agent, stageOutput);
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
        ImportAgentPlannerAgentSpec finalAgent = new ImportAgentPlannerAgentRegistry().getAgent("final_plan");
        return buildRequestBody(request, finalAgent, new ImportAgentPlannerContext(request));
    }

    private String buildRequestBody(
            ImportAgentPlannerRequest request,
            ImportAgentPlannerAgentSpec agent,
            ImportAgentPlannerContext context) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", properties.getModel().trim());
        if (properties.getTemperature() != null) {
            root.put("temperature", properties.getTemperature());
        }
        if (properties.getMaxCompletionTokens() != null && properties.getMaxCompletionTokens() > 0) {
            root.put(resolveMaxTokensParameterName(), properties.getMaxCompletionTokens());
        }
        ArrayNode messages = root.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", hasText(properties.getSystemPrompt()) ? properties.getSystemPrompt().trim() : DEFAULT_SYSTEM_PROMPT);
        messages.addObject()
                .put("role", "user")
                .put("content", buildUserPrompt(request, agent, context));
        if (properties.isToolCallingEnabled() && !agent.allowedTools().isEmpty()) {
            root.set("tools", toolRegistry.buildTools(agent.allowedTools(), OBJECT_MAPPER));
            if (isToolChoiceEnabled() && agent.allowedTools().size() == 1) {
                root.putObject("tool_choice")
                        .put("type", "function")
                        .putObject("function")
                        .put("name", agent.allowedTools().get(0));
            }
        }
        return root.toString();
    }

    private String buildUserPrompt(
            ImportAgentPlannerRequest request,
            ImportAgentPlannerAgentSpec agent,
            ImportAgentPlannerContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("运行 API 导入 Agent 规划阶段：").append(agent.name()).append("\n");
        prompt.append("角色：").append(agent.role().name()).append("\n");
        prompt.append("期望输出模式：").append(agent.outputMode().name()).append("\n");
        prompt.append(agent.promptTemplate().trim()).append("\n");
        appendField(prompt, "documentSource", request.getDocumentSource());
        appendField(prompt, "documentSummary", request.getDocumentSummary());
        appendField(prompt, "importIntent", request.getImportIntent());
        appendField(prompt, "latestUserMessage", request.getLatestUserMessage());
        prompt.append("nextPlanVersion: ").append(request.getNextPlanVersion()).append("\n");
        if (!request.getAvailableCategories().isEmpty()) {
            prompt.append("availableCategoriesJson:\n").append(serializeAvailableCategories(request)).append("\n");
        }
        if (!request.getExistingAssetCandidates().isEmpty()) {
            prompt.append("existingAssetCandidatesJson:\n").append(serializeExistingAssetCandidates(request)).append("\n");
        }
        if (!request.getTargetExistingAssets().isEmpty()) {
            prompt.append("targetExistingAssetsJson:\n").append(serializeTargetExistingAssets(request)).append("\n");
        }
        if (!request.getTurns().isEmpty()) {
            prompt.append("recentTurnsJson:\n").append(serializeTurns(request)).append("\n");
        }
        if (request.getCurrentPlan() != null) {
            prompt.append("currentPlanJson:\n").append(serializeCurrentPlan(request)).append("\n");
        }
        if (context != null && !context.getStageResults().isEmpty()) {
            prompt.append("priorStageOutputsJson:\n").append(serializeStageResults(context.getStageResults())).append("\n");
        }
        if (!agent.allowedTools().isEmpty()) {
            prompt.append("允许使用的工具：").append(String.join(", ", agent.allowedTools())).append("\n");
            for (ImportAgentPlanningToolDescriptor descriptor : toolRegistry.getTools(agent.allowedTools())) {
                String instruction = descriptor.tool().stagePromptInstruction();
                if (hasText(instruction)) {
                    prompt.append(instruction).append("\n");
                }
            }
        }
        prompt.append("请返回结构化 JSON。最终阶段请返回完整最终计划 JSON，或调用 submit_import_plan。\n");
        return prompt.toString();
    }

    private String serializeCurrentPlan(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getCurrentPlan());
        } catch (IOException ex) {
            throw new IllegalStateException("序列化当前导入计划失败", ex);
        }
    }

    private String serializeAvailableCategories(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getAvailableCategories());
        } catch (IOException ex) {
            throw new IllegalStateException("序列化可用分类失败", ex);
        }
    }

    private String serializeExistingAssetCandidates(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getExistingAssetCandidates());
        } catch (IOException ex) {
            throw new IllegalStateException("序列化现有资产候选失败", ex);
        }
    }

    private String serializeTargetExistingAssets(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getTargetExistingAssets());
        } catch (IOException ex) {
            throw new IllegalStateException("序列化目标现有资产失败", ex);
        }
    }

    private String serializeTurns(ImportAgentPlannerRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request.getTurns());
        } catch (IOException ex) {
            throw new IllegalStateException("序列化导入 Agent 对话记录失败", ex);
        }
    }

    private String serializeStageResults(List<ImportAgentPlannerStageResult> stageResults) {
        ArrayNode array = OBJECT_MAPPER.createArrayNode();
        for (ImportAgentPlannerStageResult stageResult : stageResults) {
            ObjectNode node = array.addObject();
            node.put("agent", stageResult.agent().name());
            node.put("role", stageResult.agent().role().name());
            node.set("output", stageResult.output());
        }
        return array.toString();
    }

    private void appendField(StringBuilder prompt, String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return;
        }
        prompt.append(fieldName).append(":\n").append(fieldValue.trim()).append("\n");
    }

    private JsonNode extractStageOutput(JsonNode payload, ImportAgentPlannerAgentSpec agent) {
        String expectedToolName = agent.allowedTools().size() == 1 ? agent.allowedTools().get(0) : null;
        JsonNode toolArguments = extractToolArguments(payload, expectedToolName);
        if (toolArguments != null) {
            return toolArguments;
        }
        String content = extractContent(payload, agent.outputMode() == ImportAgentPlannerOutputMode.FINAL_PLAN);
        JsonNode parsed = ImportAgentPlannerJsonSupport.parseJsonCandidate(content);
        if (parsed != null) {
            return parsed;
        }
        if (agent.outputMode() == ImportAgentPlannerOutputMode.FINAL_PLAN) {
            return null;
        }
        ObjectNode note = OBJECT_MAPPER.createObjectNode();
        note.put("message", content == null ? "" : content);
        return note;
    }

    private JsonNode extractToolArguments(JsonNode payload, String expectedToolName) {
        JsonNode choices = payload.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("LLM 规划响应缺少 choices 字段");
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
            throw new IllegalStateException("LLM 规划响应缺少 choices 字段");
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
        throw new IllegalStateException("LLM 规划响应内容不是文本");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveMaxTokensParameterName() {
        if (hasText(properties.getMaxTokensParameterName())) {
            String configured = properties.getMaxTokensParameterName().trim();
            if ("max_tokens".equals(configured) || "max_completion_tokens".equals(configured)) {
                return configured;
            }
            log.warn("Unsupported import-agent max tokens parameter name: {}, fallback to provider default", configured);
        }
        if (isDeepSeekPlanner()) {
            return "max_tokens";
        }
        return "max_completion_tokens";
    }

    private boolean isDeepSeekPlanner() {
        String baseUrl = properties.getBaseUrl();
        String model = properties.getModel();
        return (baseUrl != null && baseUrl.toLowerCase().contains("deepseek"))
                || (model != null && model.toLowerCase().startsWith("deepseek-"));
    }

    private boolean isToolChoiceEnabled() {
        if (properties.getToolChoiceEnabled() != null) {
            return properties.getToolChoiceEnabled();
        }
        return !isDeepSeekPlanner();
    }

    private String summarizeErrorBody(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= ERROR_BODY_LOG_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, ERROR_BODY_LOG_LIMIT) + "...";
    }
}
