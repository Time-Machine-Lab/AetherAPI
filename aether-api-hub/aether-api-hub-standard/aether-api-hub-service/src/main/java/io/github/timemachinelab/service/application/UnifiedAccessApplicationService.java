package io.github.timemachinelab.service.application;

import io.github.timemachinelab.api.error.CatalogErrorCodes;
import io.github.timemachinelab.api.error.ConsumerAuthErrorCodes;
import io.github.timemachinelab.api.error.UnifiedAccessErrorCodes;
import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.service.model.ConsumerContextModel;
import io.github.timemachinelab.service.model.CredentialValidationResult;
import io.github.timemachinelab.service.model.PlatformPreForwardFailureType;
import io.github.timemachinelab.service.model.RecordApiCallLogCommand;
import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.TargetApiSnapshotModel;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessExecutionOutcomeType;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureException;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.model.ValidateApiCredentialCommand;
import io.github.timemachinelab.service.port.in.CredentialValidationUseCase;
import io.github.timemachinelab.service.port.in.ObservabilityUseCase;
import io.github.timemachinelab.service.port.in.UnifiedAccessUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiSubscriptionEntitlementPort;
import io.github.timemachinelab.service.port.out.UnifiedAccessDownstreamProxyPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Unified access application service that resolves caller identity and target API snapshot.
 */
public class UnifiedAccessApplicationService implements UnifiedAccessUseCase {

    private static final System.Logger log = System.getLogger(UnifiedAccessApplicationService.class.getName());
    private static final String DEFAULT_ACCESS_CHANNEL = "UNIFIED_ACCESS";

    private final CredentialValidationUseCase credentialValidationUseCase;
    private final ApiAssetRepositoryPort apiAssetRepositoryPort;
    private final ApiSubscriptionEntitlementPort apiSubscriptionEntitlementPort;
    private final UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;
    private final UnifiedAccessDownstreamProxyPort unifiedAccessDownstreamProxyPort;
    private final ObservabilityUseCase observabilityUseCase;

    public UnifiedAccessApplicationService(
            CredentialValidationUseCase credentialValidationUseCase,
            ApiAssetRepositoryPort apiAssetRepositoryPort,
            ApiSubscriptionEntitlementPort apiSubscriptionEntitlementPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort,
            UnifiedAccessDownstreamProxyPort unifiedAccessDownstreamProxyPort,
            ObservabilityUseCase observabilityUseCase) {
        this.credentialValidationUseCase = credentialValidationUseCase;
        this.apiAssetRepositoryPort = apiAssetRepositoryPort;
        this.apiSubscriptionEntitlementPort = apiSubscriptionEntitlementPort;
        this.userConsumerMappingRepositoryPort = userConsumerMappingRepositoryPort;
        this.unifiedAccessDownstreamProxyPort = unifiedAccessDownstreamProxyPort;
        this.observabilityUseCase = observabilityUseCase;
    }

    @Override
    public UnifiedAccessProxyResponseModel invoke(ResolveUnifiedAccessInvocationCommand command) {
        Instant invocationStartedAt = Instant.now();
        try {
            UnifiedAccessInvocationModel invocation = resolveInvocation(command);
            UnifiedAccessProxyResponseModel response = unifiedAccessDownstreamProxyPort.forward(invocation);
            observabilityUseCase.recordApiCallLog(toCompletionLogCommand(invocation, response, invocationStartedAt));
            return response;
        } catch (UnifiedAccessPlatformFailureException ex) {
            recordPlatformFailureBestEffort(command, ex.getFailure(), invocationStartedAt);
            throw ex;
        }
    }

    @Override
    public UnifiedAccessInvocationModel resolveInvocation(ResolveUnifiedAccessInvocationCommand command) {
        Objects.requireNonNull(command, "Unified access invocation command must not be null");

        String httpMethod = normalizeHttpMethod(command.getHttpMethod());
        String accessChannel = normalizeAccessChannel(command.getAccessChannel());
        String requestedApiCode = normalizeRequestedApiCode(command.getApiCode());
        String plaintextApiKey = normalizePlaintextApiKey(command.getPlaintextApiKey(), requestedApiCode);

        CredentialValidationResult validationResult = credentialValidationUseCase.validateApiCredential(
                new ValidateApiCredentialCommand(plaintextApiKey, accessChannel)
        );
        if (!validationResult.isValid()) {
            throw platformFailure(
                    mapCredentialFailureCode(validationResult.getFailureReason()),
                    defaultFailureMessage(validationResult),
                    PlatformPreForwardFailureType.INVALID_CREDENTIAL,
                    requestedApiCode,
                    401
            );
        }

        ConsumerContextModel consumerContext = validationResult.getConsumerContext();
        if (consumerContext == null) {
            throw new IllegalStateException("Consumer context must be present after successful credential validation");
        }

        ApiCode apiCode = parseApiCode(requestedApiCode, consumerContext);
        ApiAssetAggregate targetApi = apiAssetRepositoryPort.findByCode(apiCode)
                .orElseThrow(() -> platformFailure(
                        CatalogErrorCodes.ASSET_NOT_FOUND,
                        "Asset not found: " + apiCode.getValue(),
                        PlatformPreForwardFailureType.TARGET_NOT_FOUND,
                        apiCode.getValue(),
                        404,
                        consumerContext
                ));
        ensureTargetAvailable(targetApi, apiCode.getValue(), consumerContext);
        ensureSubscriptionEntitlement(targetApi, apiCode, consumerContext);

        return new UnifiedAccessInvocationModel(
                consumerContext,
                toTargetApiSnapshot(targetApi),
                httpMethod,
                command.getHeaders(),
                command.getQueryParameters(),
                command.getRequestBody(),
                command.getContentType(),
                accessChannel
        );
    }

    private void ensureTargetAvailable(ApiAssetAggregate targetApi, String apiCode, ConsumerContextModel consumerContext) {
        if (targetApi.getStatus() != AssetStatus.PUBLISHED) {
            throw platformFailure(
                    UnifiedAccessErrorCodes.TARGET_API_UNAVAILABLE,
                    "Target API is unavailable: " + apiCode,
                    PlatformPreForwardFailureType.TARGET_UNAVAILABLE,
                    apiCode,
                    503,
                    consumerContext
            );
        }

        UpstreamEndpointConfig upstreamConfig = targetApi.getUpstreamConfig();
        if (upstreamConfig == null || !upstreamConfig.isComplete()) {
            throw platformFailure(
                    UnifiedAccessErrorCodes.TARGET_API_UNAVAILABLE,
                    "Target API is unavailable: " + apiCode,
                    PlatformPreForwardFailureType.TARGET_UNAVAILABLE,
                    apiCode,
                    503,
                    consumerContext
            );
        }
    }

    private void ensureSubscriptionEntitlement(
            ApiAssetAggregate targetApi,
            ApiCode apiCode,
            ConsumerContextModel consumerContext) {
        ConsumerId consumerId = ConsumerId.of(consumerContext.getConsumerId());
        UserConsumerMapping mapping = userConsumerMappingRepositoryPort.findActiveByConsumerId(consumerId).orElse(null);
        if (mapping != null && targetApi.getOwnerUserId().equals(mapping.getUserId())) {
            return;
        }
        if (apiSubscriptionEntitlementPort.hasActiveSubscription(consumerId, apiCode)) {
            return;
        }
        throw platformFailure(
                UnifiedAccessErrorCodes.API_SUBSCRIPTION_REQUIRED,
                "API subscription is required before calling: " + apiCode.getValue(),
                PlatformPreForwardFailureType.SUBSCRIPTION_REQUIRED,
                apiCode.getValue(),
                403,
                consumerContext
        );
    }

    private TargetApiSnapshotModel toTargetApiSnapshot(ApiAssetAggregate targetApi) {
        UpstreamEndpointConfig upstreamConfig = targetApi.getUpstreamConfig();
        boolean streamingSupported = targetApi.getAiCapabilityProfile() != null
                && targetApi.getAiCapabilityProfile().isStreamingSupported();
        return new TargetApiSnapshotModel(
                targetApi.getId().getValue(),
                targetApi.getCode().getValue(),
                targetApi.getName(),
                targetApi.getType().name(),
                upstreamConfig.getRequestMethod().name(),
                upstreamConfig.getUpstreamUrl(),
                upstreamConfig.getAuthScheme().name(),
                upstreamConfig.getAuthConfig(),
                streamingSupported,
                targetApi.getAiCapabilityProfile() == null ? null : targetApi.getAiCapabilityProfile().getProvider(),
                targetApi.getAiCapabilityProfile() == null ? null : targetApi.getAiCapabilityProfile().getModel()
        );
    }

    private ApiCode parseApiCode(String requestedApiCode, ConsumerContextModel consumerContext) {
        try {
            return ApiCode.of(requestedApiCode);
        } catch (IllegalArgumentException ex) {
            throw platformFailure(
                    CatalogErrorCodes.API_CODE_INVALID,
                    ex.getMessage(),
                    PlatformPreForwardFailureType.INVALID_API_CODE,
                    requestedApiCode,
                    400,
                    consumerContext
            );
        }
    }

    private String normalizeRequestedApiCode(String apiCode) {
        if (apiCode == null || apiCode.isBlank()) {
            throw platformFailure(
                    CatalogErrorCodes.API_CODE_INVALID,
                    "API code must not be blank",
                    PlatformPreForwardFailureType.INVALID_API_CODE,
                    apiCode,
                    400
            );
        }
        return apiCode.trim();
    }

    private String normalizePlaintextApiKey(String plaintextApiKey, String requestedApiCode) {
        if (plaintextApiKey == null || plaintextApiKey.isBlank()) {
            throw platformFailure(
                    ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID,
                    "API credential is required",
                    PlatformPreForwardFailureType.INVALID_CREDENTIAL,
                    requestedApiCode,
                    401
            );
        }
        return plaintextApiKey.trim();
    }

    private String mapCredentialFailureCode(CredentialValidationFailureReason failureReason) {
        if (failureReason == null) {
            return ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID;
        }
        return switch (failureReason) {
            case CREDENTIAL_NOT_FOUND -> ConsumerAuthErrorCodes.API_CREDENTIAL_NOT_FOUND;
            case CREDENTIAL_DISABLED -> ConsumerAuthErrorCodes.API_CREDENTIAL_INVALID;
            case CREDENTIAL_REVOKED -> ConsumerAuthErrorCodes.API_CREDENTIAL_REVOKED;
            case CREDENTIAL_EXPIRED -> ConsumerAuthErrorCodes.API_CREDENTIAL_EXPIRED;
            case CONSUMER_UNAVAILABLE -> ConsumerAuthErrorCodes.CONSUMER_UNAVAILABLE;
        };
    }

    private String defaultFailureMessage(CredentialValidationResult validationResult) {
        String failureMessage = validationResult.getFailureMessage();
        if (failureMessage != null && !failureMessage.isBlank()) {
            return failureMessage;
        }
        return "API credential is invalid";
    }

    private UnifiedAccessPlatformFailureException platformFailure(
            String code,
            String message,
            PlatformPreForwardFailureType failureType,
            String apiCode,
            int httpStatus) {
        return platformFailure(code, message, failureType, apiCode, httpStatus, null);
    }

    private UnifiedAccessPlatformFailureException platformFailure(
            String code,
            String message,
            PlatformPreForwardFailureType failureType,
            String apiCode,
            int httpStatus,
            ConsumerContextModel consumerContext) {
        return new UnifiedAccessPlatformFailureException(
                new UnifiedAccessPlatformFailureModel(code, message, failureType, apiCode, httpStatus, consumerContext)
        );
    }

    private String normalizeHttpMethod(String httpMethod) {
        if (httpMethod == null || httpMethod.isBlank()) {
            throw new IllegalArgumentException("HTTP method must not be blank");
        }
        return httpMethod.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeAccessChannel(String accessChannel) {
        if (accessChannel == null || accessChannel.isBlank()) {
            return DEFAULT_ACCESS_CHANNEL;
        }
        return accessChannel.trim().toUpperCase(Locale.ROOT);
    }

    private RecordApiCallLogCommand toCompletionLogCommand(
            UnifiedAccessInvocationModel invocation,
            UnifiedAccessProxyResponseModel response,
            Instant invocationStartedAt) {
        UnifiedAccessExecutionOutcomeType outcomeType = response.getOutcomeType();
        boolean success = outcomeType == UnifiedAccessExecutionOutcomeType.SUCCESS;
        String resultType = success ? "SUCCESS" : outcomeType.name();
        String errorType = success ? null : outcomeType.name();
        String errorSummary = success ? null : defaultFailureSummary(response);

        return new RecordApiCallLogCommand(
                invocation.getConsumerContext().getConsumerId(),
                invocation.getConsumerContext().getConsumerCode(),
                invocation.getConsumerContext().getConsumerName(),
                invocation.getConsumerContext().getConsumerType(),
                invocation.getConsumerContext().getCredentialId(),
                invocation.getConsumerContext().getCredentialCode(),
                invocation.getConsumerContext().getCredentialStatus(),
                invocation.getAccessChannel(),
                invocation.getTargetApi().getAssetId(),
                invocation.getTargetApi().getApiCode(),
                invocation.getTargetApi().getAssetName(),
                invocation.getTargetApi().getAssetType(),
                invocation.getHttpMethod(),
                invocationStartedAt,
                computeDurationMillis(invocationStartedAt),
                resultType,
                success,
                response.getStatusCode(),
                null,
                errorType,
                errorSummary,
                invocation.getTargetApi().getAiProvider(),
                invocation.getTargetApi().getAiModel(),
                resolveAiStreaming(invocation.getTargetApi()),
                null,
                null
        );
    }

    private RecordApiCallLogCommand toPlatformFailureLogCommand(
            ResolveUnifiedAccessInvocationCommand command,
            UnifiedAccessPlatformFailureModel failure,
            Instant invocationStartedAt) {
        String requestedApiCode = command == null ? null : command.getApiCode();
        ConsumerContextModel consumerContext = failure.getConsumerContext();
        return new RecordApiCallLogCommand(
                consumerContext == null ? null : consumerContext.getConsumerId(),
                consumerContext == null ? null : consumerContext.getConsumerCode(),
                consumerContext == null ? null : consumerContext.getConsumerName(),
                consumerContext == null ? null : consumerContext.getConsumerType(),
                consumerContext == null ? null : consumerContext.getCredentialId(),
                consumerContext == null ? null : consumerContext.getCredentialCode(),
                consumerContext == null ? null : consumerContext.getCredentialStatus(),
                normalizeAccessChannel(command == null ? null : command.getAccessChannel()),
                null,
                requestedApiCode == null || requestedApiCode.isBlank() ? failure.getApiCode() : requestedApiCode.trim(),
                null,
                null,
                normalizeHttpMethod(command == null ? null : command.getHttpMethod()),
                invocationStartedAt,
                computeDurationMillis(invocationStartedAt),
                failure.getFailureType().name(),
                false,
                failure.getHttpStatus(),
                failure.getCode(),
                failure.getFailureType().name(),
                failure.getMessage(),
                null,
                null,
                null,
                null,
                null
        );
    }

    private void recordPlatformFailureBestEffort(
            ResolveUnifiedAccessInvocationCommand command,
            UnifiedAccessPlatformFailureModel failure,
            Instant invocationStartedAt) {
        try {
            observabilityUseCase.recordApiCallLog(toPlatformFailureLogCommand(command, failure, invocationStartedAt));
        } catch (RuntimeException logException) {
            log.log(
                    System.Logger.Level.WARNING,
                    "Unified access platform failure log persistence failed; preserving original failure response. apiCode={0}, failureType={1}",
                    failure.getApiCode(),
                    failure.getFailureType(),
                    logException
            );
        }
    }

    private long computeDurationMillis(Instant invocationStartedAt) {
        return Math.max(0L, Duration.between(invocationStartedAt, Instant.now()).toMillis());
    }

    private String defaultFailureSummary(UnifiedAccessProxyResponseModel response) {
        if (response.getMessage() != null && !response.getMessage().isBlank()) {
            return response.getMessage();
        }
        return "Unified access invocation finished with result " + response.getOutcomeType().name();
    }

    private Boolean resolveAiStreaming(TargetApiSnapshotModel targetApi) {
        if (targetApi.getAiProvider() == null && targetApi.getAiModel() == null) {
            return null;
        }
        return targetApi.isStreamingSupported();
    }
}
