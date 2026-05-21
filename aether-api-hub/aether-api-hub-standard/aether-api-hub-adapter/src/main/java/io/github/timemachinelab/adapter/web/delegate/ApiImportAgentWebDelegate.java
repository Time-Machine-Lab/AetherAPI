package io.github.timemachinelab.adapter.web.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.api.req.AppendImportAgentTurnReq;
import io.github.timemachinelab.api.req.ConfirmImportAgentPlanReq;
import io.github.timemachinelab.api.req.CreateImportAgentSessionReq;
import io.github.timemachinelab.api.req.StartImportAgentRunReq;
import io.github.timemachinelab.api.resp.ApiImportAgentRunResp;
import io.github.timemachinelab.api.resp.ApiImportAgentSessionResp;
import io.github.timemachinelab.api.resp.AsyncTaskConfigResp;
import io.github.timemachinelab.api.resp.ImportAgentClarificationItemResp;
import io.github.timemachinelab.api.resp.ImportAgentClarificationOptionResp;
import io.github.timemachinelab.api.resp.ImportAgentPlanResp;
import io.github.timemachinelab.api.resp.ImportAgentTurnResp;
import io.github.timemachinelab.api.resp.ImportAiProfileResp;
import io.github.timemachinelab.api.resp.ImportAssetPlanResp;
import io.github.timemachinelab.api.resp.ImportCategoryPlanResp;
import io.github.timemachinelab.api.resp.ImportStepResultResp;
import io.github.timemachinelab.adapter.web.config.ImportAgentStreamProperties;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskAuthMode;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.AppendImportAgentTurnCommand;
import io.github.timemachinelab.service.model.ConfirmImportAgentPlanCommand;
import io.github.timemachinelab.service.model.CreateImportAgentSessionCommand;
import io.github.timemachinelab.service.model.ImportAgentClarificationAnswerModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationOptionModel;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;
import io.github.timemachinelab.service.model.ImportAgentStreamEvent;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.StartImportAgentRunCommand;
import io.github.timemachinelab.service.port.in.ApiImportAgentUseCase;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Import agent web delegate.
 */
@Component
public class ApiImportAgentWebDelegate {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String STREAM_CONTENT_TYPE = "text/event-stream";
    private final ApiImportAgentUseCase apiImportAgentUseCase;
    private final ImportAgentStreamProperties streamProperties;

    public ApiImportAgentWebDelegate(
            ApiImportAgentUseCase apiImportAgentUseCase,
            ImportAgentStreamProperties streamProperties) {
        this.apiImportAgentUseCase = apiImportAgentUseCase;
        this.streamProperties = streamProperties;
    }

    public ApiImportAgentSessionResp createSession(String currentUserId, String publisherDisplayName, CreateImportAgentSessionReq req) {
        return toSessionResp(apiImportAgentUseCase.createSession(new CreateImportAgentSessionCommand(
                currentUserId,
                publisherDisplayName,
                req.getDocumentSource(),
                req.getDocumentSummary(),
                req.getImportIntent()
        )));
    }

    public ApiImportAgentSessionResp getSession(String currentUserId, String sessionId) {
        return toSessionResp(apiImportAgentUseCase.getSession(currentUserId, sessionId));
    }

    public ApiImportAgentSessionResp appendTurn(String currentUserId, String sessionId, AppendImportAgentTurnReq req) {
        return toSessionResp(apiImportAgentUseCase.appendTurn(new AppendImportAgentTurnCommand(
                currentUserId,
                sessionId,
                req.getMessage(),
                toClarificationAnswers(req)
        )));
    }

    public void createSessionStreamToResponse(
            String currentUserId,
            String publisherDisplayName,
            CreateImportAgentSessionReq req,
            HttpServletRequest request,
            HttpServletResponse response) {
        streamSessionToResponse(
                request,
                response,
            streamEmitter -> toSessionResp(apiImportAgentUseCase.createSession(
                new CreateImportAgentSessionCommand(
                    currentUserId,
                    publisherDisplayName,
                    req.getDocumentSource(),
                    req.getDocumentSummary(),
                    req.getImportIntent()
                ),
                streamEmitter
            ))
        );
    }

    public void appendTurnStreamToResponse(
            String currentUserId,
            String sessionId,
            AppendImportAgentTurnReq req,
            HttpServletRequest request,
            HttpServletResponse response) {
        streamSessionToResponse(
                request,
                response,
            streamEmitter -> toSessionResp(apiImportAgentUseCase.appendTurn(
                new AppendImportAgentTurnCommand(
                    currentUserId,
                    sessionId,
                    req.getMessage(),
                    toClarificationAnswers(req)
                ),
                streamEmitter
            ))
        );
    }

    public ApiImportAgentSessionResp confirmPlan(String currentUserId, String sessionId, ConfirmImportAgentPlanReq req) {
        return toSessionResp(apiImportAgentUseCase.confirmPlan(new ConfirmImportAgentPlanCommand(
                currentUserId,
                sessionId,
                req.getPlanVersion()
        )));
    }

    public ApiImportAgentRunResp startRun(String currentUserId, String publisherDisplayName, String sessionId, StartImportAgentRunReq req) {
        return toRunResp(apiImportAgentUseCase.startRun(new StartImportAgentRunCommand(
                currentUserId,
                publisherDisplayName,
                sessionId,
                req.getPlanVersion()
        )));
    }

    public ApiImportAgentRunResp getRun(String currentUserId, String runId) {
        return toRunResp(apiImportAgentUseCase.getRun(currentUserId, runId));
    }

    private void streamSessionToResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            StreamingSessionOperation responseSupplier) {
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(resolveStreamTimeoutMillis());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(STREAM_CONTENT_TYPE);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        CompletableFuture.runAsync(() -> writeSessionStream(asyncContext, response, responseSupplier));
    }

    private void writeSessionStream(
            AsyncContext asyncContext,
            HttpServletResponse response,
            StreamingSessionOperation responseSupplier) {
        try {
            OutputStream outputStream = response.getOutputStream();
            ApiImportAgentSessionResp session = responseSupplier.execute(event -> {
                try {
                    writeSseEvent(outputStream, event);
                } catch (IOException ex) {
                    throw new IllegalStateException("Failed to stream import agent event", ex);
                }
            });
            writeSseEvent(outputStream, ImportAgentStreamEvent.session(session));
            writeSseEvent(outputStream, ImportAgentStreamEvent.done("completed"));
            outputStream.flush();
        } catch (Exception ex) {
            try {
                OutputStream outputStream = response.getOutputStream();
                writeSseEvent(outputStream, ImportAgentStreamEvent.error(
                        "IMPORT_AGENT_STREAM_FAILED",
                        buildErrorMessage(ex)
                ));
                outputStream.flush();
            } catch (IOException ignored) {
                // Response already failed; nothing else to do.
            }
        } finally {
                asyncContext.complete();
        }
    }

    private void writeSseEvent(OutputStream outputStream, String eventName, Object payload) throws IOException {
        outputStream.write(("event: " + eventName + "\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("data: " + OBJECT_MAPPER.writeValueAsString(payload) + "\n\n").getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private void writeSseEvent(OutputStream outputStream, ImportAgentStreamEvent event) throws IOException {
        writeSseEvent(outputStream, event.getEventName(), event.getPayload());
    }

    private long resolveStreamTimeoutMillis() {
        Integer timeoutSeconds = streamProperties.getTimeoutSeconds();
        if (timeoutSeconds == null || timeoutSeconds <= 0) {
            return 180_000L;
        }
        return timeoutSeconds * 1000L;
    }

    private String buildErrorMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "导入代理流式响应失败。"
                : ex.getMessage();
    }

    @FunctionalInterface
    private interface StreamingSessionOperation {
        ApiImportAgentSessionResp execute(ImportAgentStreamEmitter streamEmitter);
    }

    private ApiImportAgentSessionResp toSessionResp(ApiImportAgentSessionModel model) {
        return new ApiImportAgentSessionResp(
                model.getSessionId(),
                model.getStatus().name(),
                model.getDocumentSource(),
                model.getDocumentSummary(),
                model.getImportIntent(),
                model.getPublisherDisplayName(),
                model.getCurrentPlanVersion(),
                model.getConfirmedPlanVersion(),
                model.getLatestRunId(),
                toPlanResp(model.getCurrentPlan()),
                model.getTurns().stream().map(turn -> new ImportAgentTurnResp(
                        turn.getTurnId(),
                        turn.getActorType().name(),
                        turn.getMessage(),
                        turn.getPlanVersion(),
                        turn.getCreatedAt()
                )).toList(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    private ImportAgentPlanResp toPlanResp(ImportAgentPlanModel model) {
        if (model == null) {
            return null;
        }
        return new ImportAgentPlanResp(
                model.getVersion(),
                model.isExecutable(),
                model.getSummary(),
                model.getClarificationQuestions(),
                model.getClarificationItems().stream().map(this::toClarificationItemResp).toList(),
                model.getCategoryPlans().stream().map(this::toCategoryPlanResp).toList(),
                model.getAssetPlans().stream().map(this::toAssetPlanResp).toList()
        );
    }

    private List<ImportAgentClarificationAnswerModel> toClarificationAnswers(AppendImportAgentTurnReq req) {
        if (req.getClarificationAnswers() == null) {
            return List.of();
        }
        return req.getClarificationAnswers().stream()
                .map(answer -> new ImportAgentClarificationAnswerModel(
                        answer.getClarificationId(),
                        answer.getTargetPath(),
                        answer.getFieldKey(),
                        answer.getValue()))
                .toList();
    }

    private ImportAgentClarificationItemResp toClarificationItemResp(ImportAgentClarificationItemModel model) {
        return new ImportAgentClarificationItemResp(
                model.getId(),
                model.getTargetPath(),
                model.getFieldKey(),
                model.getLabel(),
                model.getDescription(),
                model.getInputType(),
                model.isRequired(),
                model.getOptions().stream().map(this::toClarificationOptionResp).toList(),
                model.getCurrentValue(),
                model.getDefaultValue(),
                model.getDefaultLabel(),
                model.getDefaultSource(),
                model.getDefaultConfidence()
        );
    }

    private ImportAgentClarificationOptionResp toClarificationOptionResp(ImportAgentClarificationOptionModel model) {
        return new ImportAgentClarificationOptionResp(model.getValue(), model.getLabel());
    }

    private ImportCategoryPlanResp toCategoryPlanResp(ImportCategoryPlanModel model) {
        return new ImportCategoryPlanResp(model.getCategoryCode(), model.getCategoryName(), model.getAction().name());
    }

    private ImportAssetPlanResp toAssetPlanResp(ImportAssetPlanModel model) {
        return new ImportAssetPlanResp(
                model.getApiCode(),
                model.getAssetName(),
                model.getAssetType() == null ? null : model.getAssetType().name(),
                model.getCategoryCode(),
                model.getRequestMethod() == null ? null : model.getRequestMethod().name(),
                model.getUpstreamUrl(),
                model.getAuthScheme() == null ? null : model.getAuthScheme().name(),
                model.getAuthConfig(),
                model.getRequestTemplate(),
                model.getRequestExample(),
                model.getResponseExample(),
                model.getRequestJsonSchema(),
                model.getResponseJsonSchema(),
                model.isPublishAfterImport(),
                toAsyncTaskConfigResp(model.getAsyncTaskConfig()),
                toAiProfileResp(model.getAiProfile())
        );
    }

    private AsyncTaskConfigResp toAsyncTaskConfigResp(AsyncTaskConfigModel model) {
        if (model == null) {
            return null;
        }
        AsyncTaskAuthMode authMode = resolveAsyncTaskAuthMode(model);
        AuthScheme authScheme = resolveAsyncTaskAuthScheme(model, authMode);
        return new AsyncTaskConfigResp(
                model.getEnabled(),
                model.getQueryMethod() == null ? null : RequestMethod.valueOf(model.getQueryMethod()),
                model.getQueryUrlTemplate(),
                authMode,
                authScheme,
                model.getAuthConfig(),
                model.getStatusPath(),
                model.getResultPath(),
                model.getErrorPath()
        );
    }

    private AsyncTaskAuthMode resolveAsyncTaskAuthMode(AsyncTaskConfigModel model) {
        String authMode = normalizeEnumText(model.getAuthMode());
        if ("SAME_AS_SUBMIT".equals(authMode) || "OVERRIDE".equals(authMode)) {
            return AsyncTaskAuthMode.valueOf(authMode);
        }
        if (resolveAuthScheme(model.getAuthMode()) != null
                || resolveAuthScheme(model.getAuthScheme()) != null
                || hasText(model.getAuthConfig())) {
            return AsyncTaskAuthMode.OVERRIDE;
        }
        return null;
    }

    private AuthScheme resolveAsyncTaskAuthScheme(AsyncTaskConfigModel model, AsyncTaskAuthMode authMode) {
        AuthScheme authScheme = resolveAuthScheme(model.getAuthScheme());
        if (authScheme != null) {
            return authScheme;
        }
        authScheme = resolveAuthScheme(model.getAuthMode());
        if (authScheme != null) {
            return authScheme;
        }
        if (authMode == AsyncTaskAuthMode.OVERRIDE && hasText(model.getAuthConfig())) {
            return model.getAuthConfig().contains(":") ? AuthScheme.HEADER_TOKEN : AuthScheme.QUERY_TOKEN;
        }
        return null;
    }

    private AuthScheme resolveAuthScheme(String value) {
        String normalized = normalizeEnumText(value);
        if (normalized == null) {
            return null;
        }
        try {
            return AuthScheme.fromToken(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeEnumText(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ImportAiProfileResp toAiProfileResp(ImportAiProfileModel model) {
        if (model == null) {
            return null;
        }
        return new ImportAiProfileResp(
                model.getProvider(),
                model.getModel(),
                model.isStreamingSupported(),
                model.getCapabilityTags()
        );
    }

    private ApiImportAgentRunResp toRunResp(ApiImportAgentRunModel model) {
        return new ApiImportAgentRunResp(
                model.getRunId(),
                model.getSessionId(),
                model.getPlanVersion(),
                model.getStatus().name(),
                model.getSummary(),
                model.getFailureReason(),
                model.getAffectedApiCodes(),
                model.getStepResults().stream().map(this::toStepResultResp).toList(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    private ImportStepResultResp toStepResultResp(ImportStepResultModel model) {
        return new ImportStepResultResp(
                model.getStepType().name(),
                model.getTargetRef(),
                model.getStatus().name(),
                model.getMessage()
        );
    }
}
