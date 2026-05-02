package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.domain.observability.model.ObservabilityDomainException;
import io.github.timemachinelab.service.application.ApiCallLogApplicationService;
import io.github.timemachinelab.service.model.ApiCallLogAiExtensionModel;
import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogErrorModel;
import io.github.timemachinelab.service.model.ApiCallLogModel;
import io.github.timemachinelab.service.model.ApiCallLogPageResult;
import io.github.timemachinelab.service.model.GetApiCallLogDetailQuery;
import io.github.timemachinelab.service.model.ListApiCallLogQuery;
import io.github.timemachinelab.service.port.out.ApiCallLogQueryPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiCallLogApplicationServiceTest {

    private InMemoryApiCallLogQueryPort apiCallLogQueryPort;
    private InMemoryUserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;
    private ApiCallLogApplicationService service;

    @BeforeEach
    void setUp() {
        apiCallLogQueryPort = new InMemoryApiCallLogQueryPort();
        userConsumerMappingRepositoryPort = new InMemoryUserConsumerMappingRepositoryPort();
        service = new ApiCallLogApplicationService(apiCallLogQueryPort, userConsumerMappingRepositoryPort);

        userConsumerMappingRepositoryPort.save(UserConsumerMapping.createActive(
                "user-1",
                ConsumerId.of("11111111-1111-1111-1111-111111111111"),
                ConsumerCode.of("consumer_user_1")
        ));
        userConsumerMappingRepositoryPort.save(UserConsumerMapping.createActive(
                "user-2",
                ConsumerId.of("22222222-2222-2222-2222-222222222222"),
                ConsumerCode.of("consumer_user_2")
        ));

        apiCallLogQueryPort.add("11111111-1111-1111-1111-111111111111", detail(
                "log-1",
                "chat-completions",
                "OpenAI Chat",
                "POST",
                "2026-04-19T09:00:00Z",
                820L,
                "SUCCESS",
                true,
                200,
                "UNIFIED_ACCESS",
                "cred_alpha",
                "ENABLED",
                null,
                new ApiCallLogAiExtensionModel("OpenAI", "gpt-4.1", false, "{\"promptTokens\":12}"),
                "2026-04-19T09:00:00Z",
                "2026-04-19T09:00:01Z"
        ));
        apiCallLogQueryPort.add("11111111-1111-1111-1111-111111111111", detail(
                "log-2",
                "image-gen",
                "Image Gen",
                "POST",
                "2026-04-19T08:00:00Z",
                1200L,
                "UPSTREAM_TIMEOUT",
                false,
                504,
                "UNIFIED_ACCESS",
                "cred_alpha",
                "ENABLED",
                new ApiCallLogErrorModel("UPSTREAM_TIMEOUT", "TIMEOUT", "Upstream request timed out"),
                null,
                "2026-04-19T08:00:00Z",
                "2026-04-19T08:00:02Z"
        ));
        apiCallLogQueryPort.add("11111111-1111-1111-1111-111111111111", detail(
                "log-4",
                "unknown-api",
                null,
                "GET",
                "2026-04-19T10:00:00Z",
                12L,
                "TARGET_NOT_FOUND",
                false,
                404,
                "UNIFIED_ACCESS",
                "cred_alpha",
                "ENABLED",
                new ApiCallLogErrorModel("ASSET_NOT_FOUND", "TARGET_NOT_FOUND", "Asset not found: unknown-api"),
                null,
                "2026-04-19T10:00:00Z",
                "2026-04-19T10:00:00Z"
        ));
        apiCallLogQueryPort.add("22222222-2222-2222-2222-222222222222", detail(
                "log-3",
                "chat-completions",
                "OpenAI Chat",
                "POST",
                "2026-04-19T07:00:00Z",
                640L,
                "SUCCESS",
                true,
                200,
                "UNIFIED_ACCESS",
                "cred_beta",
                "ENABLED",
                null,
                null,
                "2026-04-19T07:00:00Z",
                "2026-04-19T07:00:01Z"
        ));
    }

    @Test
    @DisplayName("list should return only current user's logs ordered by latest invocation time")
    void shouldReturnOnlyCurrentUsersLogs() {
        ApiCallLogPageResult result = service.listApiCallLogs(
                new ListApiCallLogQuery("user-1", null, null, null, 1, 20));

        assertEquals(3, result.getItems().size());
        assertEquals(3L, result.getTotal());
        assertEquals("log-4", result.getItems().get(0).getLogId());
        assertEquals("log-1", result.getItems().get(1).getLogId());
        assertEquals("log-2", result.getItems().get(2).getLogId());
    }

    @Test
    @DisplayName("list should support filtering by target API code")
    void shouldFilterByTargetApiCode() {
        ApiCallLogPageResult result = service.listApiCallLogs(
                new ListApiCallLogQuery("user-1", "chat-completions", null, null, 1, 20));

        assertEquals(1, result.getItems().size());
        assertEquals("log-1", result.getItems().get(0).getLogId());
    }

    @Test
    @DisplayName("list should support filtering by invocation time range")
    void shouldFilterByInvocationTimeRange() {
        ApiCallLogPageResult result = service.listApiCallLogs(new ListApiCallLogQuery(
                "user-1",
                null,
                Instant.parse("2026-04-19T08:30:00Z"),
                Instant.parse("2026-04-19T09:30:00Z"),
                1,
                20
        ));

        assertEquals(1, result.getItems().size());
        assertEquals("log-1", result.getItems().get(0).getLogId());
    }

    @Test
    @DisplayName("current user can list and view valid-key unknown target failure logs")
    void shouldReturnUnknownTargetFailureLogForCurrentUser() {
        ApiCallLogPageResult result = service.listApiCallLogs(
                new ListApiCallLogQuery("user-1", "unknown-api", null, null, 1, 20));

        assertEquals(1, result.getItems().size());
        assertEquals("log-4", result.getItems().get(0).getLogId());
        assertEquals("unknown-api", result.getItems().get(0).getTargetApiCode());
        assertEquals("TARGET_NOT_FOUND", result.getItems().get(0).getResultType());
        assertEquals(404, result.getItems().get(0).getHttpStatusCode());
        assertTrue(!result.getItems().get(0).isSuccess());

        ApiCallLogDetailModel detail = service.getApiCallLogDetail(
                new GetApiCallLogDetailQuery("user-1", "log-4"));
        assertEquals("cred_alpha", detail.getCredentialCode());
        assertEquals("ENABLED", detail.getCredentialStatus());
        assertNotNull(detail.getError());
        assertEquals("ASSET_NOT_FOUND", detail.getError().getErrorCode());
        assertEquals("TARGET_NOT_FOUND", detail.getError().getErrorType());
        assertEquals("Asset not found: unknown-api", detail.getError().getErrorSummary());
    }

    @Test
    @DisplayName("detail should return owned log details")
    void shouldReturnOwnedLogDetail() {
        ApiCallLogDetailModel detail = service.getApiCallLogDetail(
                new GetApiCallLogDetailQuery("user-1", "log-2"));

        assertEquals("log-2", detail.getLogId());
        assertEquals("image-gen", detail.getTargetApiCode());
        assertNotNull(detail.getError());
        assertEquals("UPSTREAM_TIMEOUT", detail.getError().getErrorCode());
    }

    @Test
    @DisplayName("detail should reject access to logs outside the current user scope")
    void shouldRejectOutOfScopeLogDetail() {
        ObservabilityDomainException exception = assertThrows(
                ObservabilityDomainException.class,
                () -> service.getApiCallLogDetail(new GetApiCallLogDetailQuery("user-1", "log-3"))
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    private ApiCallLogDetailModel detail(
            String logId,
            String targetApiCode,
            String targetApiName,
            String requestMethod,
            String invocationTime,
            Long durationMs,
            String resultType,
            boolean success,
            Integer httpStatusCode,
            String accessChannel,
            String credentialCode,
            String credentialStatus,
            ApiCallLogErrorModel error,
            ApiCallLogAiExtensionModel aiExtension,
            String createdAt,
            String updatedAt) {
        return new ApiCallLogDetailModel(
                logId,
                targetApiCode,
                targetApiName,
                requestMethod,
                invocationTime,
                durationMs,
                resultType,
                success,
                httpStatusCode,
                accessChannel,
                credentialCode,
                credentialStatus,
                error,
                aiExtension,
                createdAt,
                updatedAt
        );
    }

    private static final class InMemoryUserConsumerMappingRepositoryPort implements UserConsumerMappingRepositoryPort {

        private final Map<String, UserConsumerMapping> storage = new HashMap<>();

        @Override
        public Optional<UserConsumerMapping> findActiveByUserId(String userId) {
            return Optional.ofNullable(storage.get(userId));
        }

        @Override
        public Optional<UserConsumerMapping> findActiveByConsumerId(ConsumerId consumerId) {
            return storage.values().stream()
                    .filter(item -> item.getConsumerId().equals(consumerId))
                    .findFirst();
        }

        @Override
        public void save(UserConsumerMapping mapping) {
            storage.put(mapping.getUserId(), mapping);
        }
    }

    private static final class InMemoryApiCallLogQueryPort implements ApiCallLogQueryPort {

        private final List<StoredLog> storage = new java.util.ArrayList<>();

        @Override
        public List<ApiCallLogModel> findPageByConsumerId(
                String consumerId,
                String targetApiCode,
                Instant invocationStartAt,
                Instant invocationEndAt,
                int page,
                int size) {
            List<ApiCallLogModel> filtered = storage.stream()
                    .filter(item -> item.consumerId.equals(consumerId))
                    .filter(item -> targetApiCode == null || targetApiCode.equals(item.detail.getTargetApiCode()))
                    .filter(item -> invocationStartAt == null
                            || !Instant.parse(item.detail.getInvocationTime()).isBefore(invocationStartAt))
                    .filter(item -> invocationEndAt == null
                            || !Instant.parse(item.detail.getInvocationTime()).isAfter(invocationEndAt))
                    .sorted(Comparator.comparing(
                            (StoredLog item) -> Instant.parse(item.detail.getInvocationTime()))
                            .reversed())
                    .map(item -> new ApiCallLogModel(
                            item.detail.getLogId(),
                            item.detail.getTargetApiCode(),
                            item.detail.getTargetApiName(),
                            item.detail.getRequestMethod(),
                            item.detail.getInvocationTime(),
                            item.detail.getDurationMs(),
                            item.detail.getResultType(),
                            item.detail.isSuccess(),
                            item.detail.getHttpStatusCode()
                    ))
                    .toList();

            int fromIndex = Math.max(0, (page - 1) * size);
            int toIndex = Math.min(filtered.size(), fromIndex + size);
            if (fromIndex >= filtered.size()) {
                return List.of();
            }
            return filtered.subList(fromIndex, toIndex);
        }

        @Override
        public long countByConsumerId(
                String consumerId,
                String targetApiCode,
                Instant invocationStartAt,
                Instant invocationEndAt) {
            return findPageByConsumerId(consumerId, targetApiCode, invocationStartAt, invocationEndAt, 1, Integer.MAX_VALUE).size();
        }

        @Override
        public Optional<ApiCallLogDetailModel> findDetailByIdAndConsumerId(String logId, String consumerId) {
            return storage.stream()
                    .filter(item -> item.consumerId.equals(consumerId))
                    .filter(item -> item.detail.getLogId().equals(logId))
                    .map(item -> item.detail)
                    .findFirst();
        }

        void add(String consumerId, ApiCallLogDetailModel detail) {
            storage.add(new StoredLog(consumerId, detail));
        }

        private record StoredLog(String consumerId, ApiCallLogDetailModel detail) {
        }
    }
}
