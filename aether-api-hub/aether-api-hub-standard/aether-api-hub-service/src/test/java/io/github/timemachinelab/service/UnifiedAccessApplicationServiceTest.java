package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.AiCapabilityProfile;
import io.github.timemachinelab.domain.catalog.model.ApiAssetAggregate;
import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.catalog.model.AssetId;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.domain.catalog.model.UpstreamEndpointConfig;
import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;
import io.github.timemachinelab.service.application.UnifiedAccessApplicationService;
import io.github.timemachinelab.service.model.ConsumerContextModel;
import io.github.timemachinelab.service.model.CredentialValidationResult;
import io.github.timemachinelab.service.model.PlatformPreForwardFailureType;
import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.UnifiedAccessExecutionOutcomeType;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessPlatformFailureException;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;
import io.github.timemachinelab.service.model.ValidateApiCredentialCommand;
import io.github.timemachinelab.service.port.in.CredentialValidationUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.UnifiedAccessDownstreamProxyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedAccessApplicationServiceTest {

    @Test
    @DisplayName("resolveInvocation returns consumer context and target snapshot for valid request")
    void shouldResolveInvocationForValidRequest() {
        InMemoryCredentialValidationUseCase credentialValidationUseCase = new InMemoryCredentialValidationUseCase(
                CredentialValidationResult.valid(new ConsumerContextModel(
                        "consumer-1",
                        "consumer_code_1",
                        "consumer-one",
                        "USER_ACCOUNT",
                        "credential-1",
                        "cred_code_1",
                        "ENABLED",
                        "ak_live",
                        "ak_live_****1234"
                ))
        );
        InMemoryApiAssetRepositoryPort apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        InMemoryUnifiedAccessDownstreamProxyPort downstreamProxyPort = new InMemoryUnifiedAccessDownstreamProxyPort();
        apiAssetRepositoryPort.save(enabledAsset("chat-completions", AssetType.AI_API, true));

        UnifiedAccessApplicationService service = new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                downstreamProxyPort
        );

        UnifiedAccessInvocationModel invocation = service.resolveInvocation(new ResolveUnifiedAccessInvocationCommand(
                "chat-completions",
                "ak_live_validation_key",
                "post",
                Map.of("X-Trace-Id", List.of("trace-1")),
                Map.of("stream", List.of("true")),
                "{\"message\":\"hello\"}".getBytes(),
                "application/json",
                null
        ));

        assertEquals("consumer-1", invocation.getConsumerContext().getConsumerId());
        assertEquals("chat-completions", invocation.getTargetApi().getApiCode());
        assertEquals("POST", invocation.getHttpMethod());
        assertEquals("UNIFIED_ACCESS", invocation.getAccessChannel());
        assertTrue(invocation.getTargetApi().isStreamingSupported());
        assertEquals("https://upstream.example.com/v1/chat-completions", invocation.getTargetApi().getUpstreamUrl());
        assertEquals(List.of("trace-1"), invocation.getHeaders().get("X-Trace-Id"));
        assertEquals(List.of("true"), invocation.getQueryParameters().get("stream"));
        assertArrayEquals("{\"message\":\"hello\"}".getBytes(), invocation.getRequestBody());
        assertEquals("ak_live_validation_key", credentialValidationUseCase.lastCommand.getPlaintextKey());
        assertEquals("UNIFIED_ACCESS", credentialValidationUseCase.lastCommand.getAccessChannel());
        assertEquals(1, credentialValidationUseCase.validationCount);
        assertEquals(1, apiAssetRepositoryPort.findByCodeCount);
        assertEquals(0, downstreamProxyPort.invocationCount);
    }

    @Test
    @DisplayName("invoke forwards only successfully resolved invocation to downstream proxy")
    void shouldForwardResolvedInvocationToDownstreamProxyBoundary() {
        InMemoryCredentialValidationUseCase credentialValidationUseCase = new InMemoryCredentialValidationUseCase(
                CredentialValidationResult.valid(new ConsumerContextModel(
                        "consumer-1",
                        "consumer_code_1",
                        "consumer-one",
                        "USER_ACCOUNT",
                        "credential-1",
                        "cred_code_1",
                        "ENABLED",
                        "ak_live",
                        "ak_live_****1234"
                ))
        );
        InMemoryApiAssetRepositoryPort apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        InMemoryUnifiedAccessDownstreamProxyPort downstreamProxyPort = new InMemoryUnifiedAccessDownstreamProxyPort();
        apiAssetRepositoryPort.save(enabledAsset("chat-completions", AssetType.AI_API, true));

        UnifiedAccessApplicationService service = new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                downstreamProxyPort
        );

        UnifiedAccessProxyResponseModel response = service.invoke(new ResolveUnifiedAccessInvocationCommand(
                "chat-completions",
                "ak_live_validation_key",
                "POST",
                Map.of("X-Trace-Id", List.of("trace-1")),
                Map.of("stream", List.of("true")),
                "{\"message\":\"hello\"}".getBytes(),
                "application/json",
                null
        ));

        assertEquals(1, downstreamProxyPort.invocationCount);
        assertEquals("chat-completions", downstreamProxyPort.lastInvocation.getTargetApi().getApiCode());
        assertEquals(202, response.getStatusCode());
        assertEquals(UnifiedAccessExecutionOutcomeType.SUCCESS, response.getOutcomeType());
        assertEquals("application/json", response.getContentType());
        assertEquals(1, credentialValidationUseCase.validationCount);
        assertEquals(1, apiAssetRepositoryPort.findByCodeCount);
    }

    @Test
    @DisplayName("invoke keeps upstream timeout distinct from platform pre-forward failures")
    void shouldReturnUpstreamTimeoutOutcomeWithoutWrappingItAsPlatformFailure() {
        InMemoryCredentialValidationUseCase credentialValidationUseCase = new InMemoryCredentialValidationUseCase(
                CredentialValidationResult.valid(new ConsumerContextModel(
                        "consumer-1",
                        "consumer_code_1",
                        "consumer-one",
                        "USER_ACCOUNT",
                        "credential-1",
                        "cred_code_1",
                        "ENABLED",
                        "ak_live",
                        "ak_live_****1234"
                ))
        );
        InMemoryApiAssetRepositoryPort apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        InMemoryUnifiedAccessDownstreamProxyPort downstreamProxyPort = new InMemoryUnifiedAccessDownstreamProxyPort();
        downstreamProxyPort.response = UnifiedAccessProxyResponseModel.upstreamTimeout(
                504,
                "{\"code\":\"UPSTREAM_TIMEOUT\"}".getBytes(),
                "application/json",
                "Upstream request timed out"
        );
        apiAssetRepositoryPort.save(enabledAsset("chat-completions", AssetType.AI_API, true));

        UnifiedAccessApplicationService service = new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                downstreamProxyPort
        );

        UnifiedAccessProxyResponseModel response = service.invoke(new ResolveUnifiedAccessInvocationCommand(
                "chat-completions",
                "ak_live_validation_key",
                "POST",
                Map.of(),
                Map.of("stream", List.of("true")),
                "{\"message\":\"hello\"}".getBytes(),
                "application/json",
                null
        ));

        assertEquals(UnifiedAccessExecutionOutcomeType.UPSTREAM_TIMEOUT, response.getOutcomeType());
        assertEquals(504, response.getStatusCode());
        assertEquals("application/json", response.getContentType());
        assertEquals(1, downstreamProxyPort.invocationCount);
        assertEquals(1, credentialValidationUseCase.validationCount);
        assertEquals(1, apiAssetRepositoryPort.findByCodeCount);
    }

    @Test
    @DisplayName("resolveInvocation rejects invalid credential before target lookup")
    void shouldRejectInvalidCredentialBeforeTargetLookup() {
        InMemoryCredentialValidationUseCase credentialValidationUseCase = new InMemoryCredentialValidationUseCase(
                CredentialValidationResult.invalid(
                        CredentialValidationFailureReason.CREDENTIAL_DISABLED,
                        "API credential is disabled",
                        null
                )
        );
        InMemoryApiAssetRepositoryPort apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        InMemoryUnifiedAccessDownstreamProxyPort downstreamProxyPort = new InMemoryUnifiedAccessDownstreamProxyPort();
        apiAssetRepositoryPort.save(enabledAsset("chat-completions", AssetType.STANDARD_API, false));

        UnifiedAccessApplicationService service = new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                downstreamProxyPort
        );

        UnifiedAccessPlatformFailureException ex = assertThrows(UnifiedAccessPlatformFailureException.class, () -> service.invoke(
                new ResolveUnifiedAccessInvocationCommand(
                        "chat-completions",
                        "ak_live_invalid",
                        "GET",
                        Map.of(),
                        Map.of(),
                        null,
                        null,
                        "web_console"
                )
        ));

        assertEquals("API credential is disabled", ex.getMessage());
        assertEquals("API_CREDENTIAL_INVALID", ex.getFailure().getCode());
        assertEquals(PlatformPreForwardFailureType.INVALID_CREDENTIAL, ex.getFailure().getFailureType());
        assertEquals(401, ex.getFailure().getHttpStatus());
        assertEquals(1, credentialValidationUseCase.validationCount);
        assertEquals(0, apiAssetRepositoryPort.findByCodeCount);
        assertEquals(0, downstreamProxyPort.invocationCount);
    }

    @Test
    @DisplayName("resolveInvocation rejects unknown target api")
    void shouldRejectUnknownTargetApi() {
        InMemoryCredentialValidationUseCase credentialValidationUseCase = new InMemoryCredentialValidationUseCase(
                CredentialValidationResult.valid(new ConsumerContextModel(
                        "consumer-1",
                        "consumer_code_1",
                        "consumer-one",
                        "USER_ACCOUNT",
                        "credential-1",
                        "cred_code_1",
                        "ENABLED",
                        "ak_live",
                        "ak_live_****1234"
                ))
        );
        InMemoryApiAssetRepositoryPort apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        InMemoryUnifiedAccessDownstreamProxyPort downstreamProxyPort = new InMemoryUnifiedAccessDownstreamProxyPort();
        UnifiedAccessApplicationService service = new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                downstreamProxyPort
        );

        UnifiedAccessPlatformFailureException ex = assertThrows(UnifiedAccessPlatformFailureException.class, () -> service.invoke(
                new ResolveUnifiedAccessInvocationCommand(
                        "chat-completions",
                        "ak_live_validation_key",
                        "GET",
                        Map.of(),
                        Map.of(),
                        null,
                        null,
                        null
                )
        ));

        assertEquals("Asset not found: chat-completions", ex.getMessage());
        assertEquals("ASSET_NOT_FOUND", ex.getFailure().getCode());
        assertEquals(PlatformPreForwardFailureType.TARGET_NOT_FOUND, ex.getFailure().getFailureType());
        assertEquals(404, ex.getFailure().getHttpStatus());
        assertEquals(1, credentialValidationUseCase.validationCount);
        assertEquals(1, apiAssetRepositoryPort.findByCodeCount);
        assertEquals(0, downstreamProxyPort.invocationCount);
    }

    @Test
    @DisplayName("resolveInvocation rejects disabled target api")
    void shouldRejectDisabledTargetApi() {
        InMemoryCredentialValidationUseCase credentialValidationUseCase = new InMemoryCredentialValidationUseCase(
                CredentialValidationResult.valid(new ConsumerContextModel(
                        "consumer-1",
                        "consumer_code_1",
                        "consumer-one",
                        "USER_ACCOUNT",
                        "credential-1",
                        "cred_code_1",
                        "ENABLED",
                        "ak_live",
                        "ak_live_****1234"
                ))
        );
        InMemoryApiAssetRepositoryPort apiAssetRepositoryPort = new InMemoryApiAssetRepositoryPort();
        InMemoryUnifiedAccessDownstreamProxyPort downstreamProxyPort = new InMemoryUnifiedAccessDownstreamProxyPort();
        apiAssetRepositoryPort.save(disabledAsset("chat-completions"));

        UnifiedAccessApplicationService service = new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                downstreamProxyPort
        );

        UnifiedAccessPlatformFailureException ex = assertThrows(UnifiedAccessPlatformFailureException.class, () -> service.invoke(
                new ResolveUnifiedAccessInvocationCommand(
                        "chat-completions",
                        "ak_live_validation_key",
                        "DELETE",
                        Map.of(),
                        Map.of(),
                        null,
                        null,
                        null
                )
        ));

        assertEquals("Target API is unavailable: chat-completions", ex.getMessage());
        assertEquals("TARGET_API_UNAVAILABLE", ex.getFailure().getCode());
        assertEquals(PlatformPreForwardFailureType.TARGET_UNAVAILABLE, ex.getFailure().getFailureType());
        assertEquals(503, ex.getFailure().getHttpStatus());
        assertEquals(1, credentialValidationUseCase.validationCount);
        assertEquals(1, apiAssetRepositoryPort.findByCodeCount);
        assertEquals(0, downstreamProxyPort.invocationCount);
    }

    private ApiAssetAggregate enabledAsset(String apiCode, AssetType assetType, boolean streamingSupported) {
        return ApiAssetAggregate.reconstitute(
                AssetId.generate(),
                ApiCode.of(apiCode),
                "Chat Completions",
                assetType,
                null,
                AssetStatus.ENABLED,
                UpstreamEndpointConfig.of(
                        RequestMethod.POST,
                        "https://upstream.example.com/v1/" + apiCode,
                        AuthScheme.HEADER_TOKEN,
                        "Authorization: Bearer upstream-token"
                ),
                "{\"model\":\"gpt-4.1\"}",
                null,
                assetType == AssetType.AI_API
                        ? AiCapabilityProfile.of("OpenAI", "gpt-4.1", streamingSupported, List.of("chat"))
                        : null,
                Instant.now().minusSeconds(300),
                Instant.now().minusSeconds(120),
                false,
                0L
        );
    }

    private ApiAssetAggregate disabledAsset(String apiCode) {
        return ApiAssetAggregate.reconstitute(
                AssetId.generate(),
                ApiCode.of(apiCode),
                "Chat Completions",
                AssetType.STANDARD_API,
                null,
                AssetStatus.DISABLED,
                UpstreamEndpointConfig.of(
                        RequestMethod.GET,
                        "https://upstream.example.com/v1/" + apiCode,
                        AuthScheme.NONE,
                        null
                ),
                null,
                null,
                null,
                Instant.now().minusSeconds(300),
                Instant.now().minusSeconds(120),
                false,
                0L
        );
    }

    private static final class InMemoryCredentialValidationUseCase implements CredentialValidationUseCase {

        private final CredentialValidationResult result;
        private int validationCount;
        private ValidateApiCredentialCommand lastCommand;

        private InMemoryCredentialValidationUseCase(CredentialValidationResult result) {
            this.result = result;
        }

        @Override
        public CredentialValidationResult validateApiCredential(ValidateApiCredentialCommand command) {
            this.validationCount++;
            this.lastCommand = command;
            return result;
        }
    }

    private static final class InMemoryApiAssetRepositoryPort implements ApiAssetRepositoryPort {

        private final Map<String, ApiAssetAggregate> storage = new HashMap<>();
        private int findByCodeCount;

        @Override
        public Optional<ApiAssetAggregate> findByCode(ApiCode code) {
            this.findByCodeCount++;
            return Optional.ofNullable(storage.get(code.getValue()));
        }

        @Override
        public Optional<ApiAssetAggregate> findByCodeIncludingDeleted(ApiCode code) {
            return findByCode(code);
        }

        @Override
        public boolean existsByCode(ApiCode code) {
            return storage.containsKey(code.getValue());
        }

        @Override
        public void save(ApiAssetAggregate aggregate) {
            storage.put(aggregate.getCode().getValue(), aggregate);
        }
    }

    private static final class InMemoryUnifiedAccessDownstreamProxyPort implements UnifiedAccessDownstreamProxyPort {

        private int invocationCount;
        private UnifiedAccessInvocationModel lastInvocation;
        private UnifiedAccessProxyResponseModel response = UnifiedAccessProxyResponseModel.success(
                202,
                Map.of("X-Aether-Forwarded", List.of("true")),
                "{\"accepted\":true}".getBytes(),
                "application/json",
                false
        );

        @Override
        public UnifiedAccessProxyResponseModel forward(UnifiedAccessInvocationModel invocation) {
            this.invocationCount++;
            this.lastInvocation = invocation;
            return response;
        }
    }
}
