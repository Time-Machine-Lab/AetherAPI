package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.AppendImportAgentTurnReq;
import io.github.timemachinelab.api.req.ConfirmImportAgentPlanReq;
import io.github.timemachinelab.api.req.CreateImportAgentSessionReq;
import io.github.timemachinelab.api.req.StartImportAgentRunReq;
import io.github.timemachinelab.api.resp.ApiImportAgentRunResp;
import io.github.timemachinelab.api.resp.ApiImportAgentSessionResp;
import io.github.timemachinelab.api.resp.ImportAgentPlanResp;
import io.github.timemachinelab.api.resp.ImportAgentTurnResp;
import io.github.timemachinelab.api.resp.ImportAiProfileResp;
import io.github.timemachinelab.api.resp.ImportAssetPlanResp;
import io.github.timemachinelab.api.resp.ImportCategoryPlanResp;
import io.github.timemachinelab.api.resp.ImportStepResultResp;
import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.AppendImportAgentTurnCommand;
import io.github.timemachinelab.service.model.ConfirmImportAgentPlanCommand;
import io.github.timemachinelab.service.model.CreateImportAgentSessionCommand;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAiProfileModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.StartImportAgentRunCommand;
import io.github.timemachinelab.service.port.in.ApiImportAgentUseCase;
import org.springframework.stereotype.Component;

/**
 * Import agent web delegate.
 */
@Component
public class ApiImportAgentWebDelegate {

    private final ApiImportAgentUseCase apiImportAgentUseCase;

    public ApiImportAgentWebDelegate(ApiImportAgentUseCase apiImportAgentUseCase) {
        this.apiImportAgentUseCase = apiImportAgentUseCase;
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
                req.getMessage()
        )));
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
                model.getCategoryPlans().stream().map(this::toCategoryPlanResp).toList(),
                model.getAssetPlans().stream().map(this::toAssetPlanResp).toList()
        );
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
                toAiProfileResp(model.getAiProfile())
        );
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