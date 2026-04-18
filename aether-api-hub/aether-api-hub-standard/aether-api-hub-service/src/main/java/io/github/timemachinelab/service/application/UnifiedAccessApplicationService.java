package io.github.timemachinelab.service.application;

import io.github.timemachinelab.api.error.CatalogErrorCodes;
import io.github.timemachinelab.api.error.ConsumerAuthErrorCodes;
import io.github.timemachinelab.api.error.UnifiedAccessErrorCodes;
import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;
import io.github.timemachinelab.service.model.ConsumerContextModel;
import io.github.timemachinelab.service.model.CredentialValidationResult;
import io.github.timemachinelab.service.model.PlatformPreForwardFailureType;
import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.TargetApiSnapshotModel;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureException;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.model.ValidateApiCredentialCommand;
import io.github.timemachinelab.service.port.in.CredentialValidationUseCase;
import io.github.timemachinelab.service.port.in.UnifiedAccessUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.UnifiedAccessDownstreamProxyPort;

import java.util.Locale;
import java.util.Objects;

/**
 * Unified access application service that resolves caller identity and target API snapshot.
 */
public class UnifiedAccessApplicationService implements UnifiedAccessUseCase {

    private static final String DEFAULT_ACCESS_CHANNEL = "UNIFIED_ACCESS";

    private final CredentialValidationUseCase credentialValidationUseCase;
    private final ApiAssetRepositoryPort apiAssetRepositoryPort;
    private final UnifiedAccessDownstreamProxyPort unifiedAccessDownstreamProxyPort;

    public UnifiedAccessApplicationService(
            CredentialValidationUseCase credentialValidationUseCase,
            ApiAssetRepositoryPort apiAssetRepositoryPort,
            UnifiedAccessDownstreamProxyPort unifiedAccessDownstreamProxyPort) {
        this.credentialValidationUseCase = credentialValidationUseCase;
        this.apiAssetRepositoryPort = apiAssetRepositoryPort;
        this.unifiedAccessDownstreamProxyPort = unifiedAccessDownstreamProxyPort;
    }

    @Override
    public UnifiedAccessProxyResponseModel invoke(ResolveUnifiedAccessInvocationCommand command) {
        UnifiedAccessInvocationModel invocation = resolveInvocation(command);
        return unifiedAccessDownstreamProxyPort.forward(invocation);
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

        ApiCode apiCode = parseApiCode(requestedApiCode);
        ApiAssetAggregate targetApi = apiAssetRepositoryPort.findByCode(apiCode)
                .orElseThrow(() -> platformFailure(
                        CatalogErrorCodes.ASSET_NOT_FOUND,
                        "Asset not found: " + apiCode.getValue(),
                        PlatformPreForwardFailureType.TARGET_NOT_FOUND,
                        apiCode.getValue(),
                        404
                ));
        ensureTargetAvailable(targetApi, apiCode.getValue());

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

    private void ensureTargetAvailable(ApiAssetAggregate targetApi, String apiCode) {
        if (targetApi.getStatus() != AssetStatus.ENABLED) {
            throw platformFailure(
                    UnifiedAccessErrorCodes.TARGET_API_UNAVAILABLE,
                    "Target API is unavailable: " + apiCode,
                    PlatformPreForwardFailureType.TARGET_UNAVAILABLE,
                    apiCode,
                    503
            );
        }

        UpstreamEndpointConfig upstreamConfig = targetApi.getUpstreamConfig();
        if (upstreamConfig == null || !upstreamConfig.isComplete()) {
            throw platformFailure(
                    UnifiedAccessErrorCodes.TARGET_API_UNAVAILABLE,
                    "Target API is unavailable: " + apiCode,
                    PlatformPreForwardFailureType.TARGET_UNAVAILABLE,
                    apiCode,
                    503
            );
        }
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
                streamingSupported
        );
    }

    private ApiCode parseApiCode(String requestedApiCode) {
        try {
            return ApiCode.of(requestedApiCode);
        } catch (IllegalArgumentException ex) {
            throw platformFailure(
                    CatalogErrorCodes.API_CODE_INVALID,
                    ex.getMessage(),
                    PlatformPreForwardFailureType.INVALID_API_CODE,
                    requestedApiCode,
                    400
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
        return new UnifiedAccessPlatformFailureException(
                new UnifiedAccessPlatformFailureModel(code, message, failureType, apiCode, httpStatus)
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
}
