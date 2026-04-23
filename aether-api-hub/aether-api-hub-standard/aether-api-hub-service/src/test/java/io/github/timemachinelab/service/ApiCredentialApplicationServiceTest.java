package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAuthDomainException;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.service.application.ApiCredentialApplicationService;
import io.github.timemachinelab.service.model.ApiCredentialModel;
import io.github.timemachinelab.service.model.ApiCredentialPageResult;
import io.github.timemachinelab.service.model.DisableApiCredentialCommand;
import io.github.timemachinelab.service.model.EnableApiCredentialCommand;
import io.github.timemachinelab.service.model.GetApiCredentialDetailQuery;
import io.github.timemachinelab.service.model.IssueApiCredentialCommand;
import io.github.timemachinelab.service.model.IssuedApiCredentialModel;
import io.github.timemachinelab.service.model.ListApiCredentialQuery;
import io.github.timemachinelab.service.model.RevokeApiCredentialCommand;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * API 凭证应用服务测试。
 */
class ApiCredentialApplicationServiceTest {

    private InMemoryApiCredentialRepositoryPort apiCredentialRepositoryPort;
    private InMemoryConsumerIdentityRepositoryPort consumerIdentityRepositoryPort;
    private InMemoryUserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort;
    private ApiCredentialApplicationService service;

    @BeforeEach
    void setUp() {
        apiCredentialRepositoryPort = new InMemoryApiCredentialRepositoryPort();
        consumerIdentityRepositoryPort = new InMemoryConsumerIdentityRepositoryPort();
        userConsumerMappingRepositoryPort = new InMemoryUserConsumerMappingRepositoryPort();
        service = new ApiCredentialApplicationService(
                apiCredentialRepositoryPort,
                consumerIdentityRepositoryPort,
                userConsumerMappingRepositoryPort
        );
    }

    @Test
    @DisplayName("首次签发应自动创建内部 Consumer，且明文仅在签发结果中返回一次")
    void shouldAutoCreateConsumerAndReturnPlaintextOnlyOnce() {
        IssuedApiCredentialModel issued = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "默认密钥", "本地调试", null));

        assertEquals(1, consumerIdentityRepositoryPort.size());
        assertEquals(1, userConsumerMappingRepositoryPort.size());
        assertNotNull(issued.getPlaintextKey());
        assertTrue(issued.getMaskedKey().startsWith("ak_live_****"));
        assertNotEquals(issued.getPlaintextKey(), issued.getMaskedKey());

        ApiCredentialModel detail = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", issued.getCredentialId()));

        assertEquals(issued.getMaskedKey(), detail.getMaskedKey());
        assertEquals("ENABLED", detail.getStatus());
    }

    @Test
    @DisplayName("同一用户多次签发应复用同一个内部 Consumer，并允许持有多个 Key")
    void shouldReuseSameConsumerForMultipleCredentials() {
        IssuedApiCredentialModel first = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "密钥一", null, null));
        IssuedApiCredentialModel second = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "密钥二", null, null));

        assertEquals(1, consumerIdentityRepositoryPort.size());
        assertEquals(1, userConsumerMappingRepositoryPort.size());
        assertEquals(2, apiCredentialRepositoryPort.size());

        ApiCredentialAggregate firstAggregate = apiCredentialRepositoryPort.byId(first.getCredentialId());
        ApiCredentialAggregate secondAggregate = apiCredentialRepositoryPort.byId(second.getCredentialId());
        assertEquals(firstAggregate.getConsumerId(), secondAggregate.getConsumerId());
    }

    @Test
    @DisplayName("列表与详情查询只应返回当前用户自己的掩码凭证")
    void shouldReturnOnlyCurrentUsersMaskedCredentials() {
        IssuedApiCredentialModel userOne = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "我的密钥", null, null));
        service.issueApiCredential(new IssueApiCredentialCommand("user-2", "别人的密钥", null, null));

        ApiCredentialPageResult pageResult = service.listApiCredentials(new ListApiCredentialQuery("user-1", null, 1, 20));

        assertEquals(1, pageResult.getItems().size());
        ApiCredentialModel listed = pageResult.getItems().get(0);
        assertEquals(userOne.getCredentialId(), listed.getCredentialId());
        assertTrue(listed.getMaskedKey().startsWith("ak_live_****"));
        assertNotEquals(userOne.getPlaintextKey(), listed.getMaskedKey());
    }

    @Test
    @DisplayName("多 Key 应支持独立状态切换且互不影响")
    void shouldSupportIndependentCredentialStateTransitions() {
        IssuedApiCredentialModel first = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "主密钥", null, null));
        IssuedApiCredentialModel second = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "备用密钥", null, null));

        ApiCredentialModel disabled = service.disableApiCredential(
                new DisableApiCredentialCommand("user-1", first.getCredentialId()));
        assertEquals("DISABLED", disabled.getStatus());

        ApiCredentialModel secondDetail = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", second.getCredentialId()));
        assertEquals("ENABLED", secondDetail.getStatus());

        ApiCredentialModel enabled = service.enableApiCredential(
                new EnableApiCredentialCommand("user-1", first.getCredentialId()));
        assertEquals("ENABLED", enabled.getStatus());

        ApiCredentialModel revoked = service.revokeApiCredential(
                new RevokeApiCredentialCommand("user-1", first.getCredentialId()));
        assertEquals("REVOKED", revoked.getStatus());
        assertNotNull(revoked.getRevokedAt());

        ApiCredentialModel stillEnabled = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", second.getCredentialId()));
        assertEquals("ENABLED", stillEnabled.getStatus());
    }

    @Test
    @DisplayName("状态变更后详情与列表应能重新查询到落库状态")
    void shouldPersistLifecycleTransitionsInDetailAndList() {
        IssuedApiCredentialModel issued = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "清理密钥", null, null));

        service.disableApiCredential(new DisableApiCredentialCommand("user-1", issued.getCredentialId()));
        ApiCredentialModel disabledDetail = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", issued.getCredentialId()));
        ApiCredentialPageResult disabledPage = service.listApiCredentials(
                new ListApiCredentialQuery("user-1", "DISABLED", 1, 20));
        assertEquals("DISABLED", disabledDetail.getStatus());
        assertEquals(1, disabledPage.getItems().size());
        assertEquals(issued.getCredentialId(), disabledPage.getItems().get(0).getCredentialId());

        service.enableApiCredential(new EnableApiCredentialCommand("user-1", issued.getCredentialId()));
        ApiCredentialModel enabledDetail = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", issued.getCredentialId()));
        ApiCredentialPageResult enabledPage = service.listApiCredentials(
                new ListApiCredentialQuery("user-1", "ENABLED", 1, 20));
        assertEquals("ENABLED", enabledDetail.getStatus());
        assertEquals(1, enabledPage.getItems().size());
        assertEquals(issued.getCredentialId(), enabledPage.getItems().get(0).getCredentialId());

        service.revokeApiCredential(new RevokeApiCredentialCommand("user-1", issued.getCredentialId()));
        ApiCredentialModel revokedDetail = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", issued.getCredentialId()));
        ApiCredentialPageResult revokedPage = service.listApiCredentials(
                new ListApiCredentialQuery("user-1", "REVOKED", 1, 20));
        assertEquals("REVOKED", revokedDetail.getStatus());
        assertNotNull(revokedDetail.getRevokedAt());
        assertEquals(1, revokedPage.getItems().size());
        assertEquals(issued.getCredentialId(), revokedPage.getItems().get(0).getCredentialId());
    }

    @Test
    @DisplayName("非法生命周期状态变更应返回业务异常")
    void shouldRejectLifecycleConflictsAsBusinessErrors() {
        IssuedApiCredentialModel issued = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "冲突密钥", null, null));

        service.disableApiCredential(new DisableApiCredentialCommand("user-1", issued.getCredentialId()));
        ConsumerAuthDomainException disableAgain = assertThrows(
                ConsumerAuthDomainException.class,
                () -> service.disableApiCredential(new DisableApiCredentialCommand("user-1", issued.getCredentialId()))
        );
        assertTrue(disableAgain.getMessage().contains("already disabled"));

        service.revokeApiCredential(new RevokeApiCredentialCommand("user-1", issued.getCredentialId()));
        ConsumerAuthDomainException revokeAgain = assertThrows(
                ConsumerAuthDomainException.class,
                () -> service.revokeApiCredential(new RevokeApiCredentialCommand("user-1", issued.getCredentialId()))
        );
        assertTrue(revokeAgain.getMessage().contains("already revoked"));

        ConsumerAuthDomainException enableRevoked = assertThrows(
                ConsumerAuthDomainException.class,
                () -> service.enableApiCredential(new EnableApiCredentialCommand("user-1", issued.getCredentialId()))
        );
        assertTrue(enableRevoked.getMessage().contains("Revoked API credential"));
    }

    @Test
    @DisplayName("当前用户不能操作其他用户的 API Key")
    void shouldPreserveCurrentUserBoundaryForLifecycleActions() {
        IssuedApiCredentialModel issued = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "我的密钥", null, null));
        service.issueApiCredential(new IssueApiCredentialCommand("user-2", "别人的密钥", null, null));

        ConsumerAuthDomainException exception = assertThrows(
                ConsumerAuthDomainException.class,
                () -> service.disableApiCredential(new DisableApiCredentialCommand("user-2", issued.getCredentialId()))
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("已过期凭证在查询视图中应显示为 EXPIRED")
    void shouldShowExpiredCredentialAsExpired() {
        IssuedApiCredentialModel issued = service.issueApiCredential(
                new IssueApiCredentialCommand("user-1", "过期密钥", null, Instant.now().minusSeconds(60)));

        ApiCredentialModel detail = service.getApiCredentialDetail(
                new GetApiCredentialDetailQuery("user-1", issued.getCredentialId()));
        ApiCredentialPageResult expiredPage = service.listApiCredentials(
                new ListApiCredentialQuery("user-1", "EXPIRED", 1, 20));

        assertEquals("EXPIRED", detail.getStatus());
        assertEquals(1, expiredPage.getItems().size());
        assertEquals(issued.getCredentialId(), expiredPage.getItems().get(0).getCredentialId());
    }

    private static final class InMemoryConsumerIdentityRepositoryPort implements ConsumerIdentityRepositoryPort {

        private final Map<String, ConsumerAggregate> storage = new HashMap<>();

        @Override
        public Optional<ConsumerAggregate> findById(ConsumerId id) {
            return Optional.ofNullable(storage.get(id.getValue()));
        }

        @Override
        public Optional<ConsumerAggregate> findByCode(ConsumerCode code) {
            return storage.values().stream()
                    .filter(item -> item.getCode().equals(code))
                    .findFirst();
        }

        @Override
        public void save(ConsumerAggregate aggregate) {
            storage.put(aggregate.getId().getValue(), aggregate);
        }

        int size() {
            return storage.size();
        }
    }

    private static final class InMemoryUserConsumerMappingRepositoryPort implements UserConsumerMappingRepositoryPort {

        private final Map<String, UserConsumerMapping> storage = new HashMap<>();

        @Override
        public Optional<UserConsumerMapping> findActiveByUserId(String userId) {
            return Optional.ofNullable(storage.get(userId));
        }

        @Override
        public void save(UserConsumerMapping mapping) {
            storage.put(mapping.getUserId(), mapping);
        }

        int size() {
            return storage.size();
        }
    }

    private static final class InMemoryApiCredentialRepositoryPort implements ApiCredentialRepositoryPort {

        private final Map<String, ApiCredentialAggregate> storage = new HashMap<>();

        @Override
        public Optional<ApiCredentialAggregate> findByFingerprintHash(String fingerprintHash) {
            return storage.values().stream()
                    .filter(item -> item.getKeyFingerprint().getHashValue().equals(fingerprintHash))
                    .findFirst();
        }

        @Override
        public Optional<ApiCredentialAggregate> findByIdAndConsumerId(ApiCredentialId credentialId, ConsumerId consumerId) {
            ApiCredentialAggregate aggregate = storage.get(credentialId.getValue());
            if (aggregate == null || !aggregate.getConsumerId().equals(consumerId)) {
                return Optional.empty();
            }
            return Optional.of(aggregate);
        }

        @Override
        public List<ApiCredentialAggregate> findPageByConsumerId(
                ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, int page, int size, Instant now) {
            List<ApiCredentialAggregate> filtered = storage.values().stream()
                    .filter(item -> item.getConsumerId().equals(consumerId))
                    .filter(item -> status == null || item.getStatus() == status)
                    .filter(item -> !expiredOnly || item.isExpired(now))
                    .sorted(Comparator.comparing(ApiCredentialAggregate::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            int fromIndex = Math.max(0, (page - 1) * size);
            int toIndex = Math.min(filtered.size(), fromIndex + size);
            if (fromIndex >= filtered.size()) {
                return List.of();
            }
            return new ArrayList<>(filtered.subList(fromIndex, toIndex));
        }

        @Override
        public long countByConsumerId(ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, Instant now) {
            return storage.values().stream()
                    .filter(item -> item.getConsumerId().equals(consumerId))
                    .filter(item -> status == null || item.getStatus() == status)
                    .filter(item -> !expiredOnly || item.isExpired(now))
                    .count();
        }

        @Override
        public void save(ApiCredentialAggregate aggregate) {
            storage.put(aggregate.getId().getValue(), aggregate);
        }

        int size() {
            return storage.size();
        }

        ApiCredentialAggregate byId(String credentialId) {
            return storage.get(credentialId);
        }
    }
}
