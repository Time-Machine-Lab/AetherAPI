package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.domain.importagent.model.ImportAgentDomainException;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.ApiAssetPageResult;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.ApiAssetSummaryModel;
import io.github.timemachinelab.service.model.AsyncTaskConfigModel;
import io.github.timemachinelab.service.model.AppendImportAgentTurnCommand;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.ConfirmImportAgentPlanCommand;
import io.github.timemachinelab.service.model.CreateCategoryCommand;
import io.github.timemachinelab.service.model.CreateImportAgentSessionCommand;
import io.github.timemachinelab.service.model.CategoryModel;
import io.github.timemachinelab.service.model.CategoryPageResult;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentCategoryCandidateModel;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.model.ImportAgentStreamEmitter;
import io.github.timemachinelab.service.model.ImportAgentRunStatus;
import io.github.timemachinelab.service.model.ImportAgentSessionStatus;
import io.github.timemachinelab.service.model.ImportAgentStepType;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationAnswerModel;
import io.github.timemachinelab.service.model.ImportAgentClarificationItemModel;
import io.github.timemachinelab.service.model.ImportAssetPlanAction;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ImportExistingAssetSummaryModel;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.ImportStepResultStatus;
import io.github.timemachinelab.service.model.ListApiAssetQuery;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.model.StartImportAgentRunCommand;
import io.github.timemachinelab.service.model.UpstreamRequestHeaderModel;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.in.ApiImportAgentUseCase;
import io.github.timemachinelab.service.port.in.CategoryUseCase;
import io.github.timemachinelab.service.port.out.ApiImportAgentPlannerPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentReplyPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentRunRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentSessionRepositoryPort;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Import agent application service.
 */
public class ApiImportAgentApplicationService implements ApiImportAgentUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ApiImportAgentSessionRepositoryPort sessionRepositoryPort;
    private final ApiImportAgentRunRepositoryPort runRepositoryPort;
    private final ApiImportAgentPlannerPort plannerPort;
    private final ApiImportAgentReplyPort replyPort;
    private final CategoryUseCase categoryUseCase;
    private final ApiAssetUseCase apiAssetUseCase;

    public ApiImportAgentApplicationService(
            ApiImportAgentSessionRepositoryPort sessionRepositoryPort,
            ApiImportAgentRunRepositoryPort runRepositoryPort,
            ApiImportAgentPlannerPort plannerPort,
            ApiImportAgentReplyPort replyPort,
            CategoryUseCase categoryUseCase,
            ApiAssetUseCase apiAssetUseCase) {
        this.sessionRepositoryPort = sessionRepositoryPort;
        this.runRepositoryPort = runRepositoryPort;
        this.plannerPort = plannerPort;
        this.replyPort = replyPort;
        this.categoryUseCase = categoryUseCase;
        this.apiAssetUseCase = apiAssetUseCase;
    }

    @Override
    public ApiImportAgentSessionModel createSession(CreateImportAgentSessionCommand command) {
        return createSession(command, null);
    }

    @Override
    public ApiImportAgentSessionModel createSession(CreateImportAgentSessionCommand command, ImportAgentStreamEmitter streamEmitter) {
        ImportAgentStreamEmitter stream = ImportAgentStreamEmitter.withSequence(streamEmitter);
        String ownerUserId = normalizeRequired(command.getOwnerUserId(), "当前用户 ID");
        String now = now();
        String sessionId = UUID.randomUUID().toString();
        String publisherDisplayName = normalizePublisherDisplayName(command.getPublisherDisplayName(), ownerUserId);
        String initialMessage = normalizeRequired(command.getImportIntent(), "导入意图");
        stream.status("planning", "正在规划导入对话。");
        stream.thinking("planning", "开始规划", "正在读取导入意图和文档信息，准备生成导入计划。");
        ImportAgentTurnModel userTurn = new ImportAgentTurnModel(
                UUID.randomUUID().toString(),
                sessionId,
                1,
                ImportAgentActorType.USER,
                initialMessage,
                null,
                now
        );
        ImportAgentPlannerRequest plannerRequest = new ImportAgentPlannerRequest(
                normalizeOptional(command.getDocumentSource()),
                normalizeOptional(command.getDocumentSummary()),
                initialMessage,
                initialMessage,
                null,
                1,
                List.of(userTurn),
                loadEnabledCategoryCandidates(stream),
                loadExistingAssetCandidates(ownerUserId, stream),
                loadTargetExistingAssets(ownerUserId, initialMessage, null, stream)
        );
        ImportAgentPlannerResult plannerResult = plannerPort.plan(plannerRequest, stream);
        ImportAgentPlanModel plan = enrichPlanWithExistingAssets(plannerResult.getPlan(), plannerRequest.getTargetExistingAssets());
        String agentMessage = resolveAgentMessage(
                plannerRequest,
                plan,
                plannerResult.getAgentMessage(),
                stream
        );
        ApiImportAgentSessionModel session = new ApiImportAgentSessionModel(
                sessionId,
                ownerUserId,
                resolveSessionStatus(plan),
                normalizeOptional(command.getDocumentSource()),
                normalizeOptional(command.getDocumentSummary()),
                initialMessage,
                publisherDisplayName,
                plan == null ? null : plan.getVersion(),
                null,
                null,
                null,
                plan,
                List.of(),
                now,
                now
        );
        ImportAgentTurnModel agentTurn = new ImportAgentTurnModel(
                UUID.randomUUID().toString(),
                sessionId,
                2,
                ImportAgentActorType.AGENT,
            agentMessage,
                plan == null ? null : plan.getVersion(),
                now
        );
        sessionRepositoryPort.saveSession(session);
        sessionRepositoryPort.saveTurn(userTurn);
        sessionRepositoryPort.saveTurn(agentTurn);
        return getSession(ownerUserId, sessionId);
    }

    @Override
    public ApiImportAgentSessionModel getSession(String ownerUserId, String sessionId) {
        ApiImportAgentSessionModel session = loadOwnedSession(ownerUserId, sessionId);
        return new ApiImportAgentSessionModel(
                session.getSessionId(),
                session.getOwnerUserId(),
                session.getStatus(),
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                session.getPublisherDisplayName(),
                session.getCurrentPlanVersion(),
                session.getConfirmedPlanVersion(),
                session.getLatestRunId(),
                session.getLatestConfirmedAt(),
                session.getCurrentPlan(),
                sessionRepositoryPort.listTurns(sessionId),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    @Override
    public ApiImportAgentSessionModel appendTurn(AppendImportAgentTurnCommand command) {
        return appendTurn(command, null);
    }

    @Override
    public ApiImportAgentSessionModel appendTurn(AppendImportAgentTurnCommand command, ImportAgentStreamEmitter streamEmitter) {
        ImportAgentStreamEmitter stream = ImportAgentStreamEmitter.withSequence(streamEmitter);
        ApiImportAgentSessionModel session = loadOwnedSession(command.getOwnerUserId(), command.getSessionId());
        String now = now();
        List<ImportAgentClarificationAnswerModel> clarificationAnswers = normalizeClarificationAnswers(command.getClarificationAnswers());
        String message = normalizeAppendTurnMessage(command.getMessage(), session.getCurrentPlan(), clarificationAnswers);
        ImportAgentPlanModel refinedCurrentPlan = applyClarificationAnswers(session.getCurrentPlan(), clarificationAnswers);
        int nextUserTurnIndex = sessionRepositoryPort.countTurns(session.getSessionId()) + 1;
        ImportAgentTurnModel userTurn = new ImportAgentTurnModel(
                UUID.randomUUID().toString(),
                session.getSessionId(),
                nextUserTurnIndex,
                ImportAgentActorType.USER,
                message,
                session.getCurrentPlanVersion(),
                now
        );
        List<ImportAgentTurnModel> plannerTurns = new ArrayList<>(sessionRepositoryPort.listTurns(session.getSessionId()));
        plannerTurns.add(userTurn);
        int nextPlanVersion = session.getCurrentPlanVersion() == null ? 1 : session.getCurrentPlanVersion() + 1;
        stream.status("planning", "正在更新导入计划。");
        stream.thinking("planning", "更新计划", "正在合并本轮补充信息，并重新检查导入计划。");
        ImportAgentPlannerRequest plannerRequest = new ImportAgentPlannerRequest(
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                message,
                refinedCurrentPlan,
                nextPlanVersion,
                plannerTurns,
                loadEnabledCategoryCandidates(stream),
                loadExistingAssetCandidates(session.getOwnerUserId(), stream),
                loadTargetExistingAssets(session.getOwnerUserId(), message, refinedCurrentPlan, stream)
        );
        ImportAgentPlannerResult plannerResult = plannerPort.plan(plannerRequest, stream);
        ImportAgentPlanModel newPlan = enrichPlanWithExistingAssets(plannerResult.getPlan(), plannerRequest.getTargetExistingAssets());
        String agentMessage = resolveAgentMessage(
                plannerRequest,
                newPlan,
                plannerResult.getAgentMessage(),
                stream
        );
        ApiImportAgentSessionModel updatedSession = new ApiImportAgentSessionModel(
                session.getSessionId(),
                session.getOwnerUserId(),
                resolveSessionStatus(newPlan),
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                session.getPublisherDisplayName(),
                newPlan == null ? null : newPlan.getVersion(),
                null,
                session.getLatestRunId(),
                null,
                newPlan,
                List.of(),
                session.getCreatedAt(),
                now
        );
        ImportAgentTurnModel agentTurn = new ImportAgentTurnModel(
                UUID.randomUUID().toString(),
                session.getSessionId(),
                nextUserTurnIndex + 1,
                ImportAgentActorType.AGENT,
            agentMessage,
                newPlan == null ? null : newPlan.getVersion(),
                now
        );
        sessionRepositoryPort.saveSession(updatedSession);
        sessionRepositoryPort.saveTurn(userTurn);
        sessionRepositoryPort.saveTurn(agentTurn);
        return getSession(command.getOwnerUserId(), command.getSessionId());
    }

    private List<ImportAgentCategoryCandidateModel> loadEnabledCategoryCandidates(ImportAgentStreamEmitter stream) {
        try {
            CategoryPageResult pageResult = categoryUseCase.listCategories(CategoryStatus.ENABLED, 1, 100);
            if (pageResult == null || pageResult.getItems() == null || pageResult.getItems().isEmpty()) {
                return List.of();
            }
            List<ImportAgentCategoryCandidateModel> candidates = new ArrayList<>();
            for (CategoryModel category : pageResult.getItems()) {
                if (category == null || category.getCode() == null || category.getCode().isBlank()) {
                    continue;
                }
                candidates.add(new ImportAgentCategoryCandidateModel(
                        category.getCode(),
                        category.getName(),
                        category.getStatus()));
            }
            return List.copyOf(candidates);
        } catch (RuntimeException ex) {
            if (stream != null) {
                stream.thinking("category_candidates", "分类候选读取失败", "暂时无法读取可用分类，导入规划会继续进行，并在需要分类时向用户确认。");
            }
            return List.of();
        }
    }

    private List<ImportExistingAssetSummaryModel> loadExistingAssetCandidates(String ownerUserId, ImportAgentStreamEmitter stream) {
        try {
            ApiAssetPageResult pageResult = apiAssetUseCase.listAssets(new ListApiAssetQuery(ownerUserId, null, null, null, 1, 100));
            if (pageResult == null || pageResult.getItems() == null || pageResult.getItems().isEmpty()) {
                return List.of();
            }
            List<ImportExistingAssetSummaryModel> candidates = new ArrayList<>();
            for (ApiAssetSummaryModel asset : pageResult.getItems()) {
                if (asset == null || asset.getApiCode() == null || asset.getApiCode().isBlank()) {
                    continue;
                }
                candidates.add(new ImportExistingAssetSummaryModel(
                        asset.getApiCode(),
                        asset.getAssetName(),
                        asset.getAssetType(),
                        asset.getCategoryCode(),
                        asset.getStatus(),
                        null,
                        null,
                        null,
                        false,
                        asset.isAsyncTaskQueryEnabled(),
                        false,
                        asset.getUpdatedAt()
                ));
            }
            return List.copyOf(candidates);
        } catch (RuntimeException ex) {
            if (stream != null) {
                stream.thinking("asset_candidates", "Asset candidates unavailable",
                        "Unable to load current-user asset candidates; planning will continue and ask for clarification if needed.");
            }
            return List.of();
        }
    }

    private List<ImportExistingAssetSummaryModel> loadTargetExistingAssets(
            String ownerUserId,
            String latestMessage,
            ImportAgentPlanModel currentPlan,
            ImportAgentStreamEmitter stream) {
        LinkedHashSet<String> candidateCodes = new LinkedHashSet<>();
        if (currentPlan != null) {
            for (ImportAssetPlanModel assetPlan : currentPlan.getAssetPlans()) {
                if (assetPlan != null && hasText(assetPlan.getApiCode())) {
                    candidateCodes.add(assetPlan.getApiCode().trim());
                }
            }
        }
        for (ImportExistingAssetSummaryModel candidate : loadExistingAssetCandidates(ownerUserId, null)) {
            if (candidate != null
                    && hasText(candidate.getApiCode())
                    && latestMessage != null
                    && latestMessage.contains(candidate.getApiCode())) {
                candidateCodes.add(candidate.getApiCode());
            }
        }
        if (candidateCodes.isEmpty()) {
            return List.of();
        }
        List<ImportExistingAssetSummaryModel> targets = new ArrayList<>();
        for (String apiCode : candidateCodes) {
            try {
                ImportExistingAssetSummaryModel target = toSafeExistingAssetSummary(apiAssetUseCase.getAssetByCode(ownerUserId, apiCode));
                if (target != null) {
                    targets.add(target);
                }
            } catch (RuntimeException ex) {
                if (stream != null) {
                    stream.thinking("asset_detail", "Target asset detail skipped",
                            "A target API code could not be loaded for the current user; its asset context was skipped.");
                }
            }
        }
        return List.copyOf(targets);
    }

    private ImportExistingAssetSummaryModel toSafeExistingAssetSummary(ApiAssetModel asset) {
        if (asset == null) {
            return null;
        }
        return new ImportExistingAssetSummaryModel(
                asset.getApiCode(),
                asset.getAssetName(),
                asset.getAssetType(),
                asset.getCategoryCode(),
                asset.getStatus(),
                asset.getRequestMethod(),
                asset.getUpstreamUrl(),
                asset.getAuthScheme(),
                hasText(asset.getAuthConfig()),
                asset.getAsyncTaskConfig() != null && Boolean.TRUE.equals(asset.getAsyncTaskConfig().getEnabled()),
                hasText(asset.getAiProvider()) || hasText(asset.getAiModel()),
                asset.getUpdatedAt()
        );
    }

    private ImportAgentPlanModel enrichPlanWithExistingAssets(
            ImportAgentPlanModel plan,
            List<ImportExistingAssetSummaryModel> targetExistingAssets) {
        if (plan == null || targetExistingAssets == null || targetExistingAssets.isEmpty()) {
            return plan;
        }
        List<ImportAssetPlanModel> enrichedAssets = new ArrayList<>();
        for (ImportAssetPlanModel assetPlan : plan.getAssetPlans()) {
            enrichedAssets.add(copyAssetPlanWithMatchedExistingAsset(
                    assetPlan,
                    findExistingAssetSummary(targetExistingAssets, assetPlan.getApiCode())));
        }
        return new ImportAgentPlanModel(
                plan.getVersion(),
                plan.isExecutable(),
                plan.getSummary(),
                plan.getClarificationQuestions(),
                plan.getClarificationItems(),
                plan.getCategoryPlans(),
                enrichedAssets
        );
    }

    private ImportExistingAssetSummaryModel findExistingAssetSummary(
            List<ImportExistingAssetSummaryModel> targetExistingAssets,
            String apiCode) {
        if (!hasText(apiCode)) {
            return null;
        }
        for (ImportExistingAssetSummaryModel target : targetExistingAssets) {
            if (target != null && apiCode.equals(target.getApiCode())) {
                return target;
            }
        }
        return null;
    }

    private ImportAssetPlanModel copyAssetPlanWithMatchedExistingAsset(
            ImportAssetPlanModel current,
            ImportExistingAssetSummaryModel matchedExistingAsset) {
        if (current == null || matchedExistingAsset == null) {
            return current;
        }
        return new ImportAssetPlanModel(
                current.getAction(),
                current.getApiCode(),
                matchedExistingAsset,
                current.getAssetName(),
                current.getAssetType(),
                current.getCategoryCode(),
                current.getRequestMethod(),
                current.getUpstreamUrl(),
                current.getAuthScheme(),
                current.getAuthConfig(),
                current.getUpstreamRequestHeaders(),
                current.getRequestTemplate(),
                current.getRequestExample(),
                current.getResponseExample(),
                current.getRequestJsonSchema(),
                current.getResponseJsonSchema(),
                current.isPublishAfterImport(),
                current.getChangedFields(),
                current.getAsyncTaskConfig(),
                current.getAiProfile()
        );
    }

    private String resolveAgentMessage(
            ImportAgentPlannerRequest request,
            ImportAgentPlanModel plan,
            String fallbackMessage,
            ImportAgentStreamEmitter streamEmitter) {
        if (streamEmitter == null || replyPort == null || plan == null) {
            return fallbackMessage;
        }
        try {
            streamEmitter.status("replying", "正在准备助手回复。");
            String streamedMessage = replyPort.streamReply(request, plan, streamEmitter);
            if (streamedMessage != null && !streamedMessage.isBlank()) {
                return streamedMessage;
            }
        } catch (RuntimeException ignored) {
            // Fall through to the existing deterministic message when streaming reply generation fails.
        }
        if (fallbackMessage != null && !fallbackMessage.isBlank()) {
            streamEmitter.message(ImportAgentActorType.AGENT, fallbackMessage);
        }
        return fallbackMessage;
    }

    @Override
    public ApiImportAgentSessionModel confirmPlan(ConfirmImportAgentPlanCommand command) {
        ApiImportAgentSessionModel session = loadOwnedSession(command.getOwnerUserId(), command.getSessionId());
        ImportAgentPlanModel plan = normalizePlan(session.getCurrentPlan());
        if (plan == null || plan.getVersion() == null || plan.getVersion() != command.getPlanVersion()) {
            throw new ImportAgentDomainException("导入计划版本不匹配");
        }
        if (!plan.isExecutable()) {
            throw new ImportAgentDomainException("导入计划暂不可执行");
        }
        String now = now();
        sessionRepositoryPort.saveSession(new ApiImportAgentSessionModel(
                session.getSessionId(),
                session.getOwnerUserId(),
                ImportAgentSessionStatus.CONFIRMED,
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                session.getPublisherDisplayName(),
                session.getCurrentPlanVersion(),
                command.getPlanVersion(),
                session.getLatestRunId(),
                now,
                plan,
                List.of(),
                session.getCreatedAt(),
                now
        ));
        return getSession(command.getOwnerUserId(), command.getSessionId());
    }

    @Override
    public ApiImportAgentRunModel startRun(StartImportAgentRunCommand command) {
        ApiImportAgentSessionModel session = loadOwnedSession(command.getOwnerUserId(), command.getSessionId());
        ImportAgentPlanModel plan = session.getCurrentPlan();
        if (plan == null || !plan.isExecutable()) {
            throw new ImportAgentDomainException("导入计划暂不可执行");
        }
        if (!Objects.equals(session.getCurrentPlanVersion(), command.getPlanVersion())
                || !Objects.equals(session.getConfirmedPlanVersion(), command.getPlanVersion())) {
            throw new ImportAgentDomainException("请先确认导入计划");
        }

        String now = now();
        String runId = UUID.randomUUID().toString();
        sessionRepositoryPort.saveSession(new ApiImportAgentSessionModel(
                session.getSessionId(),
                session.getOwnerUserId(),
                ImportAgentSessionStatus.EXECUTING,
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                session.getPublisherDisplayName(),
                session.getCurrentPlanVersion(),
                session.getConfirmedPlanVersion(),
                runId,
                session.getLatestConfirmedAt(),
                plan,
                List.of(),
                session.getCreatedAt(),
                now
        ));

        ApiImportAgentRunModel runningRun = new ApiImportAgentRunModel(
                runId,
                session.getSessionId(),
                session.getOwnerUserId(),
                command.getPlanVersion(),
                ImportAgentRunStatus.RUNNING,
                null,
                null,
                List.of(),
                List.of(),
                now,
                now
        );
        runRepositoryPort.saveRun(runningRun);

        List<ImportStepResultModel> stepResults = new ArrayList<>();
        LinkedHashSet<String> affectedApiCodes = new LinkedHashSet<>();
        String failureReason = null;
        boolean anySuccess = false;

        try {
            for (ImportCategoryPlanModel categoryPlan : plan.getCategoryPlans()) {
                ImportStepResultModel stepResult = ensureCategory(categoryPlan);
                stepResults.add(stepResult);
                if (stepResult.getStatus() == ImportStepResultStatus.FAILED) {
                    failureReason = stepResult.getMessage();
                    break;
                }
                anySuccess = true;
            }
            if (failureReason == null) {
                for (ImportAssetPlanModel assetPlan : plan.getAssetPlans()) {
                    List<ImportStepResultModel> assetSteps = applyAssetPlan(command, assetPlan);
                    for (ImportStepResultModel assetStep : assetSteps) {
                        stepResults.add(assetStep);
                        if (assetStep.getStatus() == ImportStepResultStatus.FAILED) {
                            failureReason = assetStep.getMessage();
                            break;
                        }
                        anySuccess = true;
                    }
                    if (assetPlan.getApiCode() != null) {
                        affectedApiCodes.add(assetPlan.getApiCode());
                    }
                    if (failureReason != null) {
                        break;
                    }
                }
            }
        } catch (RuntimeException ex) {
            failureReason = ex.getMessage();
        }

        ImportAgentRunStatus finalStatus = resolveRunStatus(failureReason, anySuccess);
        String finishedAt = now();
        ApiImportAgentRunModel finalRun = new ApiImportAgentRunModel(
                runId,
                session.getSessionId(),
                session.getOwnerUserId(),
                command.getPlanVersion(),
                finalStatus,
                buildRunSummary(plan, finalStatus, affectedApiCodes.size()),
                failureReason,
                new ArrayList<>(affectedApiCodes),
                stepResults,
                runningRun.getCreatedAt(),
                finishedAt
        );
        runRepositoryPort.saveRun(finalRun);
        sessionRepositoryPort.saveSession(new ApiImportAgentSessionModel(
                session.getSessionId(),
                session.getOwnerUserId(),
                finalStatus == ImportAgentRunStatus.SUCCEEDED ? ImportAgentSessionStatus.COMPLETED : ImportAgentSessionStatus.FAILED,
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                session.getPublisherDisplayName(),
                session.getCurrentPlanVersion(),
                session.getConfirmedPlanVersion(),
                runId,
                session.getLatestConfirmedAt(),
                session.getCurrentPlan(),
                List.of(),
                session.getCreatedAt(),
                finishedAt
        ));
        return finalRun;
    }

    @Override
    public ApiImportAgentRunModel getRun(String ownerUserId, String runId) {
        return runRepositoryPort.findOwnedRun(normalizeRequired(ownerUserId, "当前用户 ID"), normalizeRequired(runId, "运行 ID"))
                .orElseThrow(() -> new ImportAgentDomainException("未找到导入运行：" + runId));
    }

    private ApiImportAgentSessionModel loadOwnedSession(String ownerUserId, String sessionId) {
        ApiImportAgentSessionModel session = sessionRepositoryPort.findOwnedSession(
                        normalizeRequired(ownerUserId, "当前用户 ID"),
                        normalizeRequired(sessionId, "会话 ID"))
                .orElseThrow(() -> new ImportAgentDomainException("未找到导入会话：" + sessionId));
        return normalizeSessionPlan(session);
    }

    private ApiImportAgentSessionModel normalizeSessionPlan(ApiImportAgentSessionModel session) {
        if (session == null) {
            return null;
        }
        return new ApiImportAgentSessionModel(
                session.getSessionId(),
                session.getOwnerUserId(),
                session.getStatus(),
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                session.getPublisherDisplayName(),
                session.getCurrentPlanVersion(),
                session.getConfirmedPlanVersion(),
                session.getLatestRunId(),
                session.getLatestConfirmedAt(),
                normalizePlan(session.getCurrentPlan()),
                session.getTurns(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private ImportAgentPlanModel normalizePlan(ImportAgentPlanModel plan) {
        if (plan == null) {
            return null;
        }
        return new ImportAgentPlanModel(
                plan.getVersion(),
                plan.isExecutable(),
                plan.getSummary(),
                plan.getClarificationQuestions(),
                plan.getClarificationItems(),
                plan.getCategoryPlans(),
                plan.getAssetPlans().stream().map(this::normalizeAssetPlan).toList()
        );
    }

    private List<ImportAgentClarificationAnswerModel> normalizeClarificationAnswers(
            List<ImportAgentClarificationAnswerModel> clarificationAnswers) {
        if (clarificationAnswers == null || clarificationAnswers.isEmpty()) {
            return List.of();
        }
        List<ImportAgentClarificationAnswerModel> normalized = new ArrayList<>();
        for (ImportAgentClarificationAnswerModel answer : clarificationAnswers) {
            if (answer == null) {
                continue;
            }
            String value = normalizeOptional(answer.getValue());
            if (value == null) {
                continue;
            }
            normalized.add(new ImportAgentClarificationAnswerModel(
                    normalizeOptional(answer.getClarificationId()),
                    normalizeOptional(answer.getTargetPath()),
                    normalizeOptional(answer.getFieldKey()),
                    value));
        }
        return List.copyOf(normalized);
    }

    private String normalizeAppendTurnMessage(
            String rawMessage,
            ImportAgentPlanModel currentPlan,
            List<ImportAgentClarificationAnswerModel> clarificationAnswers) {
        String message = normalizeOptional(rawMessage);
        if (message != null) {
            return message;
        }
        if (clarificationAnswers != null && !clarificationAnswers.isEmpty()) {
            return buildClarificationAnswerMessage(currentPlan, clarificationAnswers);
        }
        return normalizeRequired(rawMessage, "对话消息");
    }

    private String buildClarificationAnswerMessage(
            ImportAgentPlanModel currentPlan,
            List<ImportAgentClarificationAnswerModel> clarificationAnswers) {
        List<String> lines = new ArrayList<>();
        lines.add("用户已补充以下澄清信息，请据此更新导入计划：");
        for (ImportAgentClarificationAnswerModel answer : clarificationAnswers) {
            String label = answer.getFieldKey();
            if (currentPlan != null) {
                try {
                    ImportAgentClarificationItemModel item = resolveClarificationItem(currentPlan, answer);
                    label = firstText(item.getLabel(), item.getFieldKey(), label);
                } catch (IllegalArgumentException ignored) {
                    label = firstText(answer.getFieldKey(), answer.getTargetPath(), "澄清信息");
                }
            }
            String answerValue = isUpstreamRequestHeaderValueAnswer(answer) ? "[REDACTED]" : answer.getValue();
            lines.add("- " + label + "：" + answerValue);
        }
        return String.join("\n", lines);
    }

    private boolean isUpstreamRequestHeaderValueAnswer(ImportAgentClarificationAnswerModel answer) {
        return answer != null
                && "value".equals(answer.getFieldKey())
                && answer.getTargetPath() != null
                && answer.getTargetPath().contains("/upstreamRequestHeaders/");
    }

    private ImportAgentPlanModel applyClarificationAnswers(
            ImportAgentPlanModel currentPlan,
            List<ImportAgentClarificationAnswerModel> clarificationAnswers) {
        if (currentPlan == null || clarificationAnswers == null || clarificationAnswers.isEmpty()) {
            return currentPlan;
        }
        List<ImportCategoryPlanModel> categoryPlans = new ArrayList<>(currentPlan.getCategoryPlans());
        List<ImportAssetPlanModel> assetPlans = new ArrayList<>(currentPlan.getAssetPlans());
        for (ImportAgentClarificationAnswerModel answer : clarificationAnswers) {
            ImportAgentClarificationItemModel item = resolveClarificationItem(currentPlan, answer);
            String targetPath = firstText(answer.getTargetPath(), item.getTargetPath());
            String fieldKey = firstText(answer.getFieldKey(), item.getFieldKey());
            if (targetPath == null || fieldKey == null) {
                throw new IllegalArgumentException("Clarification answer must include targetPath and fieldKey.");
            }
            if (targetPath.startsWith("/assetPlans/")) {
                applyAssetClarificationAnswer(assetPlans, targetPath, fieldKey, answer.getValue());
                continue;
            }
            if (targetPath.startsWith("/categoryPlans/")) {
                applyCategoryClarificationAnswer(categoryPlans, targetPath, fieldKey, answer.getValue());
                continue;
            }
            throw new IllegalArgumentException("Unsupported clarification targetPath: " + targetPath);
        }
        return new ImportAgentPlanModel(
                currentPlan.getVersion(),
                currentPlan.isExecutable(),
                currentPlan.getSummary(),
                currentPlan.getClarificationQuestions(),
                currentPlan.getClarificationItems(),
                categoryPlans,
                assetPlans);
    }

    private ImportAgentClarificationItemModel resolveClarificationItem(
            ImportAgentPlanModel currentPlan,
            ImportAgentClarificationAnswerModel answer) {
        for (ImportAgentClarificationItemModel item : currentPlan.getClarificationItems()) {
            if (answer.getClarificationId() != null && answer.getClarificationId().equals(item.getId())) {
                return item;
            }
        }
        for (ImportAgentClarificationItemModel item : currentPlan.getClarificationItems()) {
            boolean targetMatches = answer.getTargetPath() == null || answer.getTargetPath().equals(item.getTargetPath());
            boolean fieldMatches = answer.getFieldKey() == null || answer.getFieldKey().equals(item.getFieldKey());
            if (targetMatches && fieldMatches) {
                return item;
            }
        }
        if (currentPlan.getClarificationItems().isEmpty()
                && answer.getTargetPath() != null
                && answer.getFieldKey() != null) {
            return new ImportAgentClarificationItemModel(
                    answer.getClarificationId(),
                    answer.getTargetPath(),
                    answer.getFieldKey(),
                    answer.getFieldKey(),
                    null,
                    "TEXT",
                    true,
                    List.of(),
                    null);
        }
        throw new IllegalArgumentException("Clarification answer does not match the current plan.");
    }

    private void applyAssetClarificationAnswer(
            List<ImportAssetPlanModel> assetPlans,
            String targetPath,
            String fieldKey,
            String value) {
        int assetIndex = parseIndexedPath(targetPath, "assetPlans");
        if (assetIndex < 0 || assetIndex >= assetPlans.size()) {
            throw new IllegalArgumentException("Clarification target asset does not exist: " + targetPath);
        }
        ImportAssetPlanModel current = assetPlans.get(assetIndex);
        if (targetPath.startsWith("/assetPlans/" + assetIndex + "/asyncTaskConfig")) {
            assetPlans.set(assetIndex, copyAssetPlanWithAsyncTaskConfig(
                    current,
                    applyAsyncTaskClarificationAnswer(current.getAsyncTaskConfig(), fieldKey, value)));
            return;
        }
        if (targetPath.startsWith("/assetPlans/" + assetIndex + "/upstreamRequestHeaders")) {
            assetPlans.set(assetIndex, copyAssetPlanWithHeaderField(current, targetPath, fieldKey, value));
            return;
        }
        assetPlans.set(assetIndex, copyAssetPlanWithField(current, fieldKey, value));
    }

    private void applyCategoryClarificationAnswer(
            List<ImportCategoryPlanModel> categoryPlans,
            String targetPath,
            String fieldKey,
            String value) {
        int categoryIndex = parseIndexedPath(targetPath, "categoryPlans");
        if (categoryIndex < 0 || categoryIndex >= categoryPlans.size()) {
            throw new IllegalArgumentException("Clarification target category does not exist: " + targetPath);
        }
        ImportCategoryPlanModel current = categoryPlans.get(categoryIndex);
        categoryPlans.set(categoryIndex, new ImportCategoryPlanModel(
                "categoryCode".equals(fieldKey) ? value : current.getCategoryCode(),
                "categoryName".equals(fieldKey) ? value : current.getCategoryName(),
                "action".equals(fieldKey) ? enumValue(ImportCategoryPlanAction.class, value, current.getAction()) : current.getAction()
        ));
    }

    private int parseIndexedPath(String targetPath, String collectionName) {
        String prefix = "/" + collectionName + "/";
        if (targetPath == null || !targetPath.startsWith(prefix)) {
            return -1;
        }
        String remainder = targetPath.substring(prefix.length());
        int slashIndex = remainder.indexOf('/');
        String indexText = slashIndex < 0 ? remainder : remainder.substring(0, slashIndex);
        try {
            return Integer.parseInt(indexText);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private ImportAssetPlanModel copyAssetPlanWithField(
            ImportAssetPlanModel current,
            String fieldKey,
            String value) {
        return new ImportAssetPlanModel(
                "action".equals(fieldKey) ? enumValue(ImportAssetPlanAction.class, value, current.getAction()) : current.getAction(),
                "apiCode".equals(fieldKey) ? value : current.getApiCode(),
                current.getMatchedExistingAsset(),
                "assetName".equals(fieldKey) ? value : current.getAssetName(),
                "assetType".equals(fieldKey) ? enumValue(AssetType.class, value, current.getAssetType()) : current.getAssetType(),
                "categoryCode".equals(fieldKey) ? value : current.getCategoryCode(),
                "requestMethod".equals(fieldKey) ? enumValue(RequestMethod.class, value, current.getRequestMethod()) : current.getRequestMethod(),
                "upstreamUrl".equals(fieldKey) ? value : current.getUpstreamUrl(),
                "authScheme".equals(fieldKey) ? resolveAuthScheme(value) : current.getAuthScheme(),
                "authConfig".equals(fieldKey) ? value : current.getAuthConfig(),
                current.getUpstreamRequestHeaders(),
                "requestTemplate".equals(fieldKey) ? value : current.getRequestTemplate(),
                "requestExample".equals(fieldKey) ? value : current.getRequestExample(),
                "responseExample".equals(fieldKey) ? value : current.getResponseExample(),
                "requestJsonSchema".equals(fieldKey) ? value : current.getRequestJsonSchema(),
                "responseJsonSchema".equals(fieldKey) ? value : current.getResponseJsonSchema(),
                "publishAfterImport".equals(fieldKey) ? Boolean.parseBoolean(value) : current.isPublishAfterImport(),
                mergeChangedFields(current.getChangedFields(), fieldKey),
                current.getAsyncTaskConfig(),
                current.getAiProfile()
        );
    }

    private ImportAssetPlanModel copyAssetPlanWithAsyncTaskConfig(
            ImportAssetPlanModel current,
            AsyncTaskConfigModel asyncTaskConfig) {
        return new ImportAssetPlanModel(
                current.getAction(),
                current.getApiCode(),
                current.getMatchedExistingAsset(),
                current.getAssetName(),
                current.getAssetType(),
                current.getCategoryCode(),
                current.getRequestMethod(),
                current.getUpstreamUrl(),
                current.getAuthScheme(),
                current.getAuthConfig(),
                current.getUpstreamRequestHeaders(),
                current.getRequestTemplate(),
                current.getRequestExample(),
                current.getResponseExample(),
                current.getRequestJsonSchema(),
                current.getResponseJsonSchema(),
                current.isPublishAfterImport(),
                mergeChangedFields(current.getChangedFields(), "asyncTaskConfig"),
                asyncTaskConfig,
                current.getAiProfile()
        );
    }

    private ImportAssetPlanModel copyAssetPlanWithHeaderField(
            ImportAssetPlanModel current,
            String targetPath,
            String fieldKey,
            String value) {
        int headerIndex = parseHeaderIndex(targetPath);
        if (headerIndex < 0) {
            throw new IllegalArgumentException("Clarification target header does not exist: " + targetPath);
        }
        List<UpstreamRequestHeaderModel> headers = new ArrayList<>(
                current.getUpstreamRequestHeaders() == null ? List.of() : current.getUpstreamRequestHeaders());
        while (headers.size() <= headerIndex) {
            headers.add(new UpstreamRequestHeaderModel(null, null));
        }
        UpstreamRequestHeaderModel currentHeader = headers.get(headerIndex);
        headers.set(headerIndex, new UpstreamRequestHeaderModel(
                "name".equals(fieldKey) ? value : currentHeader.getName(),
                "value".equals(fieldKey) ? value : currentHeader.getValue()
        ));
        return new ImportAssetPlanModel(
                current.getAction(),
                current.getApiCode(),
                current.getMatchedExistingAsset(),
                current.getAssetName(),
                current.getAssetType(),
                current.getCategoryCode(),
                current.getRequestMethod(),
                current.getUpstreamUrl(),
                current.getAuthScheme(),
                current.getAuthConfig(),
                headers,
                current.getRequestTemplate(),
                current.getRequestExample(),
                current.getResponseExample(),
                current.getRequestJsonSchema(),
                current.getResponseJsonSchema(),
                current.isPublishAfterImport(),
                mergeChangedFields(current.getChangedFields(), "upstreamRequestHeaders"),
                current.getAsyncTaskConfig(),
                current.getAiProfile()
        );
    }

    private List<String> mergeChangedFields(List<String> changedFields, String fieldKey) {
        if (!hasText(fieldKey)) {
            return changedFields;
        }
        if ("action".equals(fieldKey) || "apiCode".equals(fieldKey)) {
            return changedFields;
        }
        LinkedHashSet<String> fields = new LinkedHashSet<>(changedFields == null ? List.of() : changedFields);
        fields.add(fieldKey);
        return List.copyOf(fields);
    }

    private int parseHeaderIndex(String targetPath) {
        String marker = "/upstreamRequestHeaders/";
        int markerIndex = targetPath == null ? -1 : targetPath.indexOf(marker);
        if (markerIndex < 0) {
            return -1;
        }
        String remainder = targetPath.substring(markerIndex + marker.length());
        int slashIndex = remainder.indexOf('/');
        String indexText = slashIndex < 0 ? remainder : remainder.substring(0, slashIndex);
        try {
            return Integer.parseInt(indexText);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private AsyncTaskConfigModel applyAsyncTaskClarificationAnswer(
            AsyncTaskConfigModel current,
            String fieldKey,
            String value) {
        AsyncTaskConfigModel safeCurrent = current == null
                ? new AsyncTaskConfigModel(null, null, null, null, null, null, null)
                : current;
        return new AsyncTaskConfigModel(
                "enabled".equals(fieldKey) ? Boolean.parseBoolean(value) : safeCurrent.getEnabled(),
                "queryMethod".equals(fieldKey) ? value : safeCurrent.getQueryMethod(),
                "queryUrlTemplate".equals(fieldKey) ? value : safeCurrent.getQueryUrlTemplate(),
                "authMode".equals(fieldKey) ? value : safeCurrent.getAuthMode(),
                "authScheme".equals(fieldKey) ? value : safeCurrent.getAuthScheme(),
                "authConfig".equals(fieldKey) ? value : safeCurrent.getAuthConfig(),
                "queryResponseJsonSchema".equals(fieldKey) ? value : safeCurrent.getQueryResponseJsonSchema()
        );
    }

    private <T extends Enum<T>> T enumValue(Class<T> type, String value, T defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(type, value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    private ImportAssetPlanModel normalizeAssetPlan(ImportAssetPlanModel assetPlan) {
        if (assetPlan == null) {
            return null;
        }
        return new ImportAssetPlanModel(
                assetPlan.getAction(),
                assetPlan.getApiCode(),
                assetPlan.getMatchedExistingAsset(),
                assetPlan.getAssetName(),
                assetPlan.getAssetType(),
                assetPlan.getCategoryCode(),
                assetPlan.getRequestMethod(),
                assetPlan.getUpstreamUrl(),
                assetPlan.getAuthScheme(),
                assetPlan.getAuthConfig(),
                assetPlan.getUpstreamRequestHeaders(),
                assetPlan.getRequestTemplate(),
                assetPlan.getRequestExample(),
                assetPlan.getResponseExample(),
                normalizeJsonObjectSnapshot(assetPlan.getRequestJsonSchema()),
                normalizeJsonObjectSnapshot(assetPlan.getResponseJsonSchema()),
                assetPlan.isPublishAfterImport(),
                assetPlan.getChangedFields(),
                normalizeAsyncTaskConfig(assetPlan.getAsyncTaskConfig()),
                assetPlan.getAiProfile()
        );
    }

    private AsyncTaskConfigModel normalizeAsyncTaskConfig(AsyncTaskConfigModel config) {
        if (config == null) {
            return null;
        }
        String authScheme = normalizeAsyncTaskAuthScheme(config.getAuthMode(), config.getAuthScheme(), config.getAuthConfig());
        String authMode = normalizeAsyncTaskAuthMode(config.getAuthMode(), authScheme, config.getAuthConfig());
        return new AsyncTaskConfigModel(
                config.getEnabled(),
                config.getQueryMethod(),
                normalizeAsyncTaskQueryUrlTemplate(config.getQueryUrlTemplate()),
                authMode,
                authScheme,
                config.getAuthConfig(),
                normalizeJsonObjectSnapshot(config.getQueryResponseJsonSchema())
        );
    }

    private String normalizeAsyncTaskQueryUrlTemplate(String queryUrlTemplate) {
        if (queryUrlTemplate == null) {
            return null;
        }
        String normalized = queryUrlTemplate.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return normalized
                .replace("{task_id}", "{taskId}")
                .replace("{taskID}", "{taskId}")
                .replace("{task-id}", "{taskId}");
    }

    private String normalizeJsonObjectSnapshot(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank() || !trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return null;
        }
        return JsonObjectSyntax.isObject(trimmed) ? trimmed : null;
    }

    private String normalizeAsyncTaskAuthMode(String authMode, String authScheme, String authConfig) {
        String normalized = normalizeEnumText(authMode);
        if ("SAME_AS_SUBMIT".equals(normalized) || "OVERRIDE".equals(normalized)) {
            return normalized;
        }
        if (authScheme != null || (authConfig != null && !authConfig.isBlank())) {
            return "OVERRIDE";
        }
        return null;
    }

    private String normalizeAsyncTaskAuthScheme(String authMode, String authScheme, String authConfig) {
        AuthScheme normalizedScheme = resolveAuthScheme(authScheme);
        if (normalizedScheme != null) {
            return normalizedScheme.name();
        }
        normalizedScheme = resolveAuthScheme(authMode);
        if (normalizedScheme != null) {
            return normalizedScheme.name();
        }
        if (authConfig == null || authConfig.isBlank()) {
            return null;
        }
        return authConfig.contains(":") ? AuthScheme.HEADER_TOKEN.name() : AuthScheme.QUERY_TOKEN.name();
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
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private ImportAgentSessionStatus resolveSessionStatus(ImportAgentPlanModel plan) {
        if (plan == null) {
            return ImportAgentSessionStatus.WAITING_FOR_PLAN;
        }
        return plan.isExecutable()
                ? ImportAgentSessionStatus.WAITING_FOR_CONFIRMATION
                : ImportAgentSessionStatus.WAITING_FOR_CLARIFICATION;
    }

    private List<ImportStepResultModel> applyAssetPlan(StartImportAgentRunCommand command, ImportAssetPlanModel assetPlan) {
        List<ImportStepResultModel> steps = new ArrayList<>();
        try {
            ImportAssetPlanAction action = assetPlan.getAction() == null ? ImportAssetPlanAction.UPSERT : assetPlan.getAction();
            ApiAssetModel existing = findOwnedAsset(command.getOwnerUserId(), assetPlan.getApiCode());
            if (action == ImportAssetPlanAction.CREATE) {
                if (existing != null) {
                    steps.add(failedStep(ImportAgentStepType.CREATE_ASSET, assetPlan.getApiCode(), "资产已存在"));
                    return steps;
                }
                registerAsset(command, assetPlan);
                apiAssetUseCase.reviseAsset(buildAssetRevisionCommand(command, assetPlan));
                steps.add(successStep(ImportAgentStepType.CREATE_ASSET, assetPlan.getApiCode(), "资产草稿已创建"));
            } else if (action == ImportAssetPlanAction.UPDATE_EXISTING) {
                if (existing == null) {
                    steps.add(failedStep(ImportAgentStepType.UPDATE_EXISTING_ASSET, assetPlan.getApiCode(), "当前用户资产不存在"));
                    return steps;
                }
                apiAssetUseCase.reviseAsset(buildAssetRevisionCommand(command, assetPlan));
                steps.add(successStep(ImportAgentStepType.UPDATE_EXISTING_ASSET, assetPlan.getApiCode(), "资产配置已更新"));
            } else {
                if (existing == null) {
                    registerAsset(command, assetPlan);
                }
                apiAssetUseCase.reviseAsset(buildAssetRevisionCommand(command, assetPlan));
                steps.add(successStep(ImportAgentStepType.UPSERT_ASSET, assetPlan.getApiCode(),
                        existing == null ? "资产草稿已创建" : "资产配置已更新"));
            }
            if (assetPlan.getAiProfile() != null && shouldApplyField(assetPlan, "aiProfile", assetPlan.getAiProfile())) {
                apiAssetUseCase.attachAiCapabilityProfile(new AttachAiCapabilityProfileCommand(
                        command.getOwnerUserId(),
                        normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                        assetPlan.getApiCode(),
                        assetPlan.getAiProfile().getProvider(),
                        assetPlan.getAiProfile().getModel(),
                        assetPlan.getAiProfile().isStreamingSupported(),
                        assetPlan.getAiProfile().getCapabilityTags()
                ));
                steps.add(successStep(ImportAgentStepType.ATTACH_AI_PROFILE, assetPlan.getApiCode(), "AI 能力配置已关联"));
            }
            if (assetPlan.isPublishAfterImport()) {
                apiAssetUseCase.publishAsset(
                        command.getOwnerUserId(),
                        normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                        assetPlan.getApiCode()
                );
                steps.add(successStep(ImportAgentStepType.PUBLISH_ASSET, assetPlan.getApiCode(), "资产已发布"));
            }
            return steps;
        } catch (RuntimeException ex) {
            steps.add(failedStep(resolveAssetStepType(assetPlan), assetPlan.getApiCode(), ex.getMessage()));
            return steps;
        }
    }

    private ApiAssetModel findOwnedAsset(String ownerUserId, String apiCode) {
        try {
            return apiAssetUseCase.getAssetByCode(ownerUserId, apiCode);
        } catch (AssetDomainException ex) {
            return null;
        }
    }

    private void registerAsset(StartImportAgentRunCommand command, ImportAssetPlanModel assetPlan) {
        apiAssetUseCase.registerAsset(new RegisterApiAssetCommand(
                command.getOwnerUserId(),
                normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                assetPlan.getApiCode(),
                assetPlan.getAssetType(),
                assetPlan.getAssetName(),
                assetPlan.getRequestJsonSchema(),
                assetPlan.getResponseJsonSchema(),
                assetPlan.getAsyncTaskConfig(),
                assetPlan.getUpstreamRequestHeaders(),
                null,
                null,
                null
        ));
    }

    private ReviseApiAssetCommand buildAssetRevisionCommand(StartImportAgentRunCommand command, ImportAssetPlanModel assetPlan) {
        return new ReviseApiAssetCommand(
                command.getOwnerUserId(),
                normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                assetPlan.getApiCode(),
                assetPlan.getAssetName(),
                shouldApplyField(assetPlan, "assetName", assetPlan.getAssetName()),
                assetPlan.getAssetType(),
                shouldApplyField(assetPlan, "assetType", assetPlan.getAssetType()),
                assetPlan.getCategoryCode(),
                shouldApplyField(assetPlan, "categoryCode", assetPlan.getCategoryCode()),
                assetPlan.getRequestMethod(),
                shouldApplyField(assetPlan, "requestMethod", assetPlan.getRequestMethod()),
                assetPlan.getUpstreamUrl(),
                shouldApplyField(assetPlan, "upstreamUrl", assetPlan.getUpstreamUrl()),
                assetPlan.getAuthScheme(),
                shouldApplyField(assetPlan, "authScheme", assetPlan.getAuthScheme()),
                assetPlan.getAuthConfig(),
                shouldApplyField(assetPlan, "authConfig", assetPlan.getAuthConfig()),
                assetPlan.getUpstreamRequestHeaders(),
                shouldApplyField(assetPlan, "upstreamRequestHeaders", assetPlan.getUpstreamRequestHeaders()),
                assetPlan.getRequestTemplate(),
                shouldApplyField(assetPlan, "requestTemplate", assetPlan.getRequestTemplate()),
                assetPlan.getRequestExample(),
                shouldApplyField(assetPlan, "requestExample", assetPlan.getRequestExample()),
                assetPlan.getResponseExample(),
                shouldApplyField(assetPlan, "responseExample", assetPlan.getResponseExample()),
                assetPlan.getRequestJsonSchema(),
                shouldApplyField(assetPlan, "requestJsonSchema", assetPlan.getRequestJsonSchema()),
                assetPlan.getResponseJsonSchema(),
                shouldApplyField(assetPlan, "responseJsonSchema", assetPlan.getResponseJsonSchema()),
                assetPlan.getAsyncTaskConfig(),
                shouldApplyField(assetPlan, "asyncTaskConfig", assetPlan.getAsyncTaskConfig()),
                null,
                false,
                null,
                false,
                null,
                false
        );
    }

    private boolean shouldApplyField(ImportAssetPlanModel assetPlan, String fieldName, Object fieldValue) {
        List<String> changedFields = assetPlan.getChangedFields();
        if (changedFields != null && !changedFields.isEmpty()) {
            return changedFields.stream().anyMatch(changedField -> matchesChangedField(changedField, fieldName));
        }
        return fieldValue != null;
    }

    private boolean matchesChangedField(String changedField, String fieldName) {
        if (!hasText(changedField) || !hasText(fieldName)) {
            return false;
        }
        String normalized = changedField.trim();
        return normalized.equals(fieldName)
                || normalized.startsWith(fieldName + ".")
                || normalized.startsWith(fieldName + "/");
    }

    private ImportAgentStepType resolveAssetStepType(ImportAssetPlanModel assetPlan) {
        ImportAssetPlanAction action = assetPlan.getAction() == null ? ImportAssetPlanAction.UPSERT : assetPlan.getAction();
        if (action == ImportAssetPlanAction.CREATE) {
            return ImportAgentStepType.CREATE_ASSET;
        }
        if (action == ImportAssetPlanAction.UPDATE_EXISTING) {
            return ImportAgentStepType.UPDATE_EXISTING_ASSET;
        }
        return ImportAgentStepType.UPSERT_ASSET;
    }

    private ImportStepResultModel ensureCategory(ImportCategoryPlanModel categoryPlan) {
        if (categoryPlan.getCategoryCode() == null || categoryPlan.getCategoryCode().isBlank()) {
            return failedStep(ImportAgentStepType.ENSURE_CATEGORY, null, "分类编码不能为空");
        }
        try {
            categoryUseCase.getCategoryByCode(categoryPlan.getCategoryCode());
            return successStep(ImportAgentStepType.ENSURE_CATEGORY, categoryPlan.getCategoryCode(), "分类已存在");
        } catch (RuntimeException ex) {
            if (categoryPlan.getAction() != ImportCategoryPlanAction.CREATE_IF_MISSING) {
                return failedStep(ImportAgentStepType.ENSURE_CATEGORY, categoryPlan.getCategoryCode(), ex.getMessage());
            }
            try {
                categoryUseCase.createCategory(new CreateCategoryCommand(
                        categoryPlan.getCategoryCode(),
                        categoryPlan.getCategoryName() == null || categoryPlan.getCategoryName().isBlank()
                                ? categoryPlan.getCategoryCode()
                                : categoryPlan.getCategoryName()
                ));
                return successStep(ImportAgentStepType.ENSURE_CATEGORY, categoryPlan.getCategoryCode(), "分类已创建");
            } catch (RuntimeException createEx) {
                return failedStep(ImportAgentStepType.ENSURE_CATEGORY, categoryPlan.getCategoryCode(), createEx.getMessage());
            }
        }
    }

    private ImportAgentRunStatus resolveRunStatus(String failureReason, boolean anySuccess) {
        if (failureReason == null) {
            return ImportAgentRunStatus.SUCCEEDED;
        }
        return anySuccess ? ImportAgentRunStatus.PARTIALLY_FAILED : ImportAgentRunStatus.FAILED;
    }

    private String buildRunSummary(ImportAgentPlanModel plan, ImportAgentRunStatus status, int affectedCount) {
        return "导入运行状态：" + status.name() + "，计划版本：" + plan.getVersion() + "，影响资产数：" + affectedCount;
    }

    private ImportStepResultModel successStep(ImportAgentStepType stepType, String targetRef, String message) {
        return new ImportStepResultModel(stepType, targetRef, ImportStepResultStatus.SUCCEEDED, message);
    }

    private ImportStepResultModel failedStep(ImportAgentStepType stepType, String targetRef, String message) {
        return new ImportStepResultModel(stepType, targetRef, ImportStepResultStatus.FAILED, message);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizePublisherDisplayName(String value, String ownerUserId) {
        return value == null || value.isBlank() ? ownerUserId : value.trim();
    }

    private String now() {
        return TIME_FORMATTER.format(Instant.now());
    }

    private static final class JsonObjectSyntax {

        private final String value;

        private JsonObjectSyntax(String value) {
            this.value = value;
        }

        static boolean isObject(String value) {
            JsonObjectSyntax parser = new JsonObjectSyntax(value);
            int end = parser.parseValue(parser.skipWhitespace(0));
            return end > 0
                    && parser.value.charAt(parser.skipWhitespace(0)) == '{'
                    && parser.skipWhitespace(end) == parser.value.length();
        }

        private int parseValue(int index) {
            if (index >= value.length()) {
                return -1;
            }
            char current = value.charAt(index);
            if (current == '{') {
                return parseObject(index);
            }
            if (current == '[') {
                return parseArray(index);
            }
            if (current == '"') {
                return parseString(index);
            }
            if (current == '-' || Character.isDigit(current)) {
                return parseNumber(index);
            }
            if (value.startsWith("true", index)) {
                return index + 4;
            }
            if (value.startsWith("false", index)) {
                return index + 5;
            }
            if (value.startsWith("null", index)) {
                return index + 4;
            }
            return -1;
        }

        private int parseObject(int index) {
            int cursor = skipWhitespace(index + 1);
            if (cursor < value.length() && value.charAt(cursor) == '}') {
                return cursor + 1;
            }
            while (cursor < value.length()) {
                if (value.charAt(cursor) != '"') {
                    return -1;
                }
                cursor = skipWhitespace(parseString(cursor));
                if (cursor < 0 || cursor >= value.length() || value.charAt(cursor) != ':') {
                    return -1;
                }
                cursor = skipWhitespace(parseValue(skipWhitespace(cursor + 1)));
                if (cursor < 0 || cursor >= value.length()) {
                    return -1;
                }
                char separator = value.charAt(cursor);
                if (separator == '}') {
                    return cursor + 1;
                }
                if (separator != ',') {
                    return -1;
                }
                cursor = skipWhitespace(cursor + 1);
            }
            return -1;
        }

        private int parseArray(int index) {
            int cursor = skipWhitespace(index + 1);
            if (cursor < value.length() && value.charAt(cursor) == ']') {
                return cursor + 1;
            }
            while (cursor < value.length()) {
                cursor = skipWhitespace(parseValue(cursor));
                if (cursor < 0 || cursor >= value.length()) {
                    return -1;
                }
                char separator = value.charAt(cursor);
                if (separator == ']') {
                    return cursor + 1;
                }
                if (separator != ',') {
                    return -1;
                }
                cursor = skipWhitespace(cursor + 1);
            }
            return -1;
        }

        private int parseString(int index) {
            int cursor = index + 1;
            while (cursor < value.length()) {
                char current = value.charAt(cursor);
                if (current == '"') {
                    return cursor + 1;
                }
                if (current == '\\') {
                    cursor += 2;
                    continue;
                }
                if (current < 0x20) {
                    return -1;
                }
                cursor += 1;
            }
            return -1;
        }

        private int parseNumber(int index) {
            int cursor = index;
            if (cursor < value.length() && value.charAt(cursor) == '-') {
                cursor += 1;
            }
            if (cursor >= value.length() || !Character.isDigit(value.charAt(cursor))) {
                return -1;
            }
            if (value.charAt(cursor) == '0') {
                cursor += 1;
            } else {
                while (cursor < value.length() && Character.isDigit(value.charAt(cursor))) {
                    cursor += 1;
                }
            }
            if (cursor < value.length() && value.charAt(cursor) == '.') {
                cursor += 1;
                if (cursor >= value.length() || !Character.isDigit(value.charAt(cursor))) {
                    return -1;
                }
                while (cursor < value.length() && Character.isDigit(value.charAt(cursor))) {
                    cursor += 1;
                }
            }
            if (cursor < value.length() && (value.charAt(cursor) == 'e' || value.charAt(cursor) == 'E')) {
                cursor += 1;
                if (cursor < value.length() && (value.charAt(cursor) == '+' || value.charAt(cursor) == '-')) {
                    cursor += 1;
                }
                if (cursor >= value.length() || !Character.isDigit(value.charAt(cursor))) {
                    return -1;
                }
                while (cursor < value.length() && Character.isDigit(value.charAt(cursor))) {
                    cursor += 1;
                }
            }
            return cursor;
        }

        private int skipWhitespace(int index) {
            int cursor = index;
            while (cursor >= 0 && cursor < value.length() && Character.isWhitespace(value.charAt(cursor))) {
                cursor += 1;
            }
            return cursor;
        }
    }
}
