package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.catalog.model.AssetDomainException;
import io.github.timemachinelab.domain.importagent.model.ImportAgentDomainException;
import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.ApiAssetModel;
import io.github.timemachinelab.service.model.AppendImportAgentTurnCommand;
import io.github.timemachinelab.service.model.AttachAiCapabilityProfileCommand;
import io.github.timemachinelab.service.model.ConfirmImportAgentPlanCommand;
import io.github.timemachinelab.service.model.CreateCategoryCommand;
import io.github.timemachinelab.service.model.CreateImportAgentSessionCommand;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.model.ImportAgentRunStatus;
import io.github.timemachinelab.service.model.ImportAgentSessionStatus;
import io.github.timemachinelab.service.model.ImportAgentStepType;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import io.github.timemachinelab.service.model.ImportStepResultModel;
import io.github.timemachinelab.service.model.ImportStepResultStatus;
import io.github.timemachinelab.service.model.RegisterApiAssetCommand;
import io.github.timemachinelab.service.model.ReviseApiAssetCommand;
import io.github.timemachinelab.service.model.StartImportAgentRunCommand;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.in.ApiImportAgentUseCase;
import io.github.timemachinelab.service.port.in.CategoryUseCase;
import io.github.timemachinelab.service.port.out.ApiImportAgentPlannerPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentRunRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiImportAgentSessionRepositoryPort;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
    private final CategoryUseCase categoryUseCase;
    private final ApiAssetUseCase apiAssetUseCase;

    public ApiImportAgentApplicationService(
            ApiImportAgentSessionRepositoryPort sessionRepositoryPort,
            ApiImportAgentRunRepositoryPort runRepositoryPort,
            ApiImportAgentPlannerPort plannerPort,
            CategoryUseCase categoryUseCase,
            ApiAssetUseCase apiAssetUseCase) {
        this.sessionRepositoryPort = sessionRepositoryPort;
        this.runRepositoryPort = runRepositoryPort;
        this.plannerPort = plannerPort;
        this.categoryUseCase = categoryUseCase;
        this.apiAssetUseCase = apiAssetUseCase;
    }

    @Override
    public ApiImportAgentSessionModel createSession(CreateImportAgentSessionCommand command) {
        String ownerUserId = normalizeRequired(command.getOwnerUserId(), "Current user id");
        String now = now();
        String sessionId = UUID.randomUUID().toString();
        String publisherDisplayName = normalizePublisherDisplayName(command.getPublisherDisplayName(), ownerUserId);
        String initialMessage = normalizeRequired(command.getImportIntent(), "Import intent");
        ImportAgentTurnModel userTurn = new ImportAgentTurnModel(
                UUID.randomUUID().toString(),
                sessionId,
                1,
                ImportAgentActorType.USER,
                initialMessage,
                null,
                now
        );
        ImportAgentPlannerResult plannerResult = plannerPort.plan(new ImportAgentPlannerRequest(
                normalizeOptional(command.getDocumentSource()),
                normalizeOptional(command.getDocumentSummary()),
                initialMessage,
                initialMessage,
                null,
                1,
                List.of(userTurn)
        ));
        ImportAgentPlanModel plan = plannerResult.getPlan();
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
                plannerResult.getAgentMessage(),
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
        ApiImportAgentSessionModel session = loadOwnedSession(command.getOwnerUserId(), command.getSessionId());
        String now = now();
        String message = normalizeRequired(command.getMessage(), "Turn message");
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
        ImportAgentPlannerResult plannerResult = plannerPort.plan(new ImportAgentPlannerRequest(
                session.getDocumentSource(),
                session.getDocumentSummary(),
                session.getImportIntent(),
                message,
                session.getCurrentPlan(),
                nextPlanVersion,
                plannerTurns
        ));
        ImportAgentPlanModel newPlan = plannerResult.getPlan();
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
                plannerResult.getAgentMessage(),
                newPlan == null ? null : newPlan.getVersion(),
                now
        );
        sessionRepositoryPort.saveSession(updatedSession);
        sessionRepositoryPort.saveTurn(userTurn);
        sessionRepositoryPort.saveTurn(agentTurn);
        return getSession(command.getOwnerUserId(), command.getSessionId());
    }

    @Override
    public ApiImportAgentSessionModel confirmPlan(ConfirmImportAgentPlanCommand command) {
        ApiImportAgentSessionModel session = loadOwnedSession(command.getOwnerUserId(), command.getSessionId());
        ImportAgentPlanModel plan = session.getCurrentPlan();
        if (plan == null || plan.getVersion() == null || plan.getVersion() != command.getPlanVersion()) {
            throw new ImportAgentDomainException("Import plan version mismatch");
        }
        if (!plan.isExecutable()) {
            throw new ImportAgentDomainException("Import plan is not executable");
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
                session.getCurrentPlan(),
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
            throw new ImportAgentDomainException("Import plan is not executable");
        }
        if (!Objects.equals(session.getCurrentPlanVersion(), command.getPlanVersion())
                || !Objects.equals(session.getConfirmedPlanVersion(), command.getPlanVersion())) {
            throw new ImportAgentDomainException("Import plan confirmation required");
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
                session.getCurrentPlan(),
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
        return runRepositoryPort.findOwnedRun(normalizeRequired(ownerUserId, "Current user id"), normalizeRequired(runId, "Run id"))
                .orElseThrow(() -> new ImportAgentDomainException("Import run not found: " + runId));
    }

    private ApiImportAgentSessionModel loadOwnedSession(String ownerUserId, String sessionId) {
        return sessionRepositoryPort.findOwnedSession(
                        normalizeRequired(ownerUserId, "Current user id"),
                        normalizeRequired(sessionId, "Session id"))
                .orElseThrow(() -> new ImportAgentDomainException("Import session not found: " + sessionId));
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
            ApiAssetModel existing = null;
            try {
                existing = apiAssetUseCase.getAssetByCode(command.getOwnerUserId(), assetPlan.getApiCode());
            } catch (AssetDomainException ignored) {
                existing = null;
            }
            if (existing == null) {
                apiAssetUseCase.registerAsset(new RegisterApiAssetCommand(
                        command.getOwnerUserId(),
                        normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                        assetPlan.getApiCode(),
                        assetPlan.getAssetType(),
                        assetPlan.getAssetName(),
                        assetPlan.getRequestJsonSchema(),
                        assetPlan.getResponseJsonSchema(),
                        null,
                        null,
                        null,
                        null
                ));
                steps.add(successStep(ImportAgentStepType.REGISTER_ASSET, assetPlan.getApiCode(), "Asset draft created"));
            } else {
                apiAssetUseCase.reviseAsset(new ReviseApiAssetCommand(
                        command.getOwnerUserId(),
                        normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                        assetPlan.getApiCode(),
                        assetPlan.getAssetName(),
                        assetPlan.getAssetName() != null,
                        assetPlan.getAssetType(),
                        assetPlan.getAssetType() != null,
                        assetPlan.getCategoryCode(),
                        assetPlan.getCategoryCode() != null,
                        assetPlan.getRequestMethod(),
                        assetPlan.getRequestMethod() != null,
                        assetPlan.getUpstreamUrl(),
                        assetPlan.getUpstreamUrl() != null,
                        assetPlan.getAuthScheme(),
                        assetPlan.getAuthScheme() != null,
                        assetPlan.getAuthConfig(),
                        assetPlan.getAuthConfig() != null,
                        assetPlan.getRequestTemplate(),
                        assetPlan.getRequestTemplate() != null,
                        assetPlan.getRequestExample(),
                        assetPlan.getRequestExample() != null,
                        assetPlan.getResponseExample(),
                        assetPlan.getResponseExample() != null,
                        assetPlan.getRequestJsonSchema(),
                        assetPlan.getRequestJsonSchema() != null,
                        assetPlan.getResponseJsonSchema(),
                        assetPlan.getResponseJsonSchema() != null,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false
                ));
                steps.add(successStep(ImportAgentStepType.REVISE_ASSET, assetPlan.getApiCode(), "Asset configuration revised"));
            }

            if (assetPlan.getAiProfile() != null) {
                apiAssetUseCase.attachAiCapabilityProfile(new AttachAiCapabilityProfileCommand(
                        command.getOwnerUserId(),
                        normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                        assetPlan.getApiCode(),
                        assetPlan.getAiProfile().getProvider(),
                        assetPlan.getAiProfile().getModel(),
                        assetPlan.getAiProfile().isStreamingSupported(),
                        assetPlan.getAiProfile().getCapabilityTags()
                ));
                steps.add(successStep(ImportAgentStepType.ATTACH_AI_PROFILE, assetPlan.getApiCode(), "AI profile attached"));
            }
            if (assetPlan.isPublishAfterImport()) {
                apiAssetUseCase.publishAsset(
                        command.getOwnerUserId(),
                        normalizePublisherDisplayName(command.getPublisherDisplayName(), command.getOwnerUserId()),
                        assetPlan.getApiCode()
                );
                steps.add(successStep(ImportAgentStepType.PUBLISH_ASSET, assetPlan.getApiCode(), "Asset published"));
            }
            return steps;
        } catch (RuntimeException ex) {
            steps.add(failedStep(ImportAgentStepType.REVISE_ASSET, assetPlan.getApiCode(), ex.getMessage()));
            return steps;
        }
    }

    private ImportStepResultModel ensureCategory(ImportCategoryPlanModel categoryPlan) {
        if (categoryPlan.getCategoryCode() == null || categoryPlan.getCategoryCode().isBlank()) {
            return failedStep(ImportAgentStepType.ENSURE_CATEGORY, null, "Category code must not be blank");
        }
        try {
            categoryUseCase.getCategoryByCode(categoryPlan.getCategoryCode());
            return successStep(ImportAgentStepType.ENSURE_CATEGORY, categoryPlan.getCategoryCode(), "Category already exists");
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
                return successStep(ImportAgentStepType.ENSURE_CATEGORY, categoryPlan.getCategoryCode(), "Category created");
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
        return "Import run " + status.name() + " for plan version " + plan.getVersion() + ", affected assets: " + affectedCount;
    }

    private ImportStepResultModel successStep(ImportAgentStepType stepType, String targetRef, String message) {
        return new ImportStepResultModel(stepType, targetRef, ImportStepResultStatus.SUCCEEDED, message);
    }

    private ImportStepResultModel failedStep(ImportAgentStepType stepType, String targetRef, String message) {
        return new ImportStepResultModel(stepType, targetRef, ImportStepResultStatus.FAILED, message);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizePublisherDisplayName(String value, String ownerUserId) {
        return value == null || value.isBlank() ? ownerUserId : value.trim();
    }

    private String now() {
        return TIME_FORMATTER.format(Instant.now());
    }
}