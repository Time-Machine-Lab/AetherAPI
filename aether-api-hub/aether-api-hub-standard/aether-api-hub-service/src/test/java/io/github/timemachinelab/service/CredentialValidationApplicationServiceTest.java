package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialCode;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerType;
import io.github.timemachinelab.domain.consumerauth.model.CredentialValidationFailureReason;
import io.github.timemachinelab.domain.consumerauth.model.ExpirationPolicy;
import io.github.timemachinelab.domain.consumerauth.model.KeyFingerprint;
import io.github.timemachinelab.domain.consumerauth.model.LastUsedSnapshot;
import io.github.timemachinelab.domain.consumerauth.service.CredentialValidationDomainService;
import io.github.timemachinelab.service.application.CredentialValidationApplicationService;
import io.github.timemachinelab.service.model.ConsumerContextModel;
import io.github.timemachinelab.service.model.CredentialValidationResult;
import io.github.timemachinelab.service.model.ValidateApiCredentialCommand;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialValidationApplicationServiceTest {

    private static final String PLAINTEXT_KEY = "ak_live_validation_key_1234";

    private InMemoryApiCredentialRepositoryPort apiCredentialRepositoryPort;
    private InMemoryConsumerIdentityRepositoryPort consumerIdentityRepositoryPort;
    private CredentialValidationApplicationService service;
    private ConsumerAggregate availableConsumer;

    @BeforeEach
    void setUp() {
        apiCredentialRepositoryPort = new InMemoryApiCredentialRepositoryPort();
        consumerIdentityRepositoryPort = new InMemoryConsumerIdentityRepositoryPort();
        service = new CredentialValidationApplicationService(
                apiCredentialRepositoryPort,
                consumerIdentityRepositoryPort,
                new CredentialValidationDomainService()
        );

        availableConsumer = ConsumerAggregate.reconstitute(
                ConsumerId.of("consumer-1"),
                ConsumerCode.of("consumer_code_1"),
                "consumer-one",
                ConsumerType.USER_ACCOUNT,
                ConsumerStatus.ENABLED,
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600),
                false,
                0L
        );
        consumerIdentityRepositoryPort.save(availableConsumer);
    }

    @Test
    @DisplayName("valid credential returns consumer context and updates success snapshot")
    void shouldReturnConsumerContextForValidCredential() {
        ApiCredentialAggregate credential = createCredential("cred-1", ApiCredentialStatus.ENABLED, null, availableConsumer);
        apiCredentialRepositoryPort.save(credential);

        CredentialValidationResult result = service.validateApiCredential(
                new ValidateApiCredentialCommand(PLAINTEXT_KEY, "unified_access"));

        assertTrue(result.isValid());
        assertNull(result.getFailureReason());
        ConsumerContextModel context = result.getConsumerContext();
        assertNotNull(context);
        assertEquals("consumer-1", context.getConsumerId());
        assertEquals("cred-1", context.getCredentialId());
        assertEquals("ENABLED", context.getCredentialStatus());

        ApiCredentialAggregate saved = apiCredentialRepositoryPort.findByFingerprintHash(sha256Hex(PLAINTEXT_KEY)).orElseThrow();
        assertEquals("UNIFIED_ACCESS", saved.getLastUsedSnapshot().getLastUsedChannel());
        assertEquals("SUCCESS", saved.getLastUsedSnapshot().getLastUsedResult());
        assertNotNull(saved.getLastUsedSnapshot().getLastUsedAt());
    }

    @Test
    @DisplayName("unknown credential returns not found and does not update snapshots")
    void shouldReturnNotFoundForUnknownCredential() {
        CredentialValidationResult result = service.validateApiCredential(
                new ValidateApiCredentialCommand("ak_live_unknown", "UNIFIED_ACCESS"));

        assertEquals(CredentialValidationFailureReason.CREDENTIAL_NOT_FOUND, result.getFailureReason());
        assertNull(result.getConsumerContext());
        assertEquals(0, apiCredentialRepositoryPort.size());
    }

    @Test
    @DisplayName("disabled credential returns categorized failure and updates failure snapshot")
    void shouldRejectDisabledCredential() {
        ApiCredentialAggregate credential = createCredential("cred-2", ApiCredentialStatus.DISABLED, null, availableConsumer);
        apiCredentialRepositoryPort.save(credential);

        CredentialValidationResult result = service.validateApiCredential(
                new ValidateApiCredentialCommand(PLAINTEXT_KEY, "UNIFIED_ACCESS"));

        assertEquals(CredentialValidationFailureReason.CREDENTIAL_DISABLED, result.getFailureReason());
        assertEquals("cred-2", result.getConsumerContext().getCredentialId());
        assertEquals("CREDENTIAL_DISABLED", apiCredentialRepositoryPort.findByFingerprintHash(sha256Hex(PLAINTEXT_KEY))
                .orElseThrow().getLastUsedSnapshot().getLastUsedResult());
    }

    @Test
    @DisplayName("revoked credential returns categorized failure")
    void shouldRejectRevokedCredential() {
        ApiCredentialAggregate credential = createCredential("cred-3", ApiCredentialStatus.REVOKED, null, availableConsumer);
        apiCredentialRepositoryPort.save(credential);

        CredentialValidationResult result = service.validateApiCredential(
                new ValidateApiCredentialCommand(PLAINTEXT_KEY, "UNIFIED_ACCESS"));

        assertEquals(CredentialValidationFailureReason.CREDENTIAL_REVOKED, result.getFailureReason());
    }

    @Test
    @DisplayName("expired credential returns categorized failure")
    void shouldRejectExpiredCredential() {
        ApiCredentialAggregate credential = createCredential(
                "cred-4", ApiCredentialStatus.ENABLED, Instant.now().minusSeconds(60), availableConsumer);
        apiCredentialRepositoryPort.save(credential);

        CredentialValidationResult result = service.validateApiCredential(
                new ValidateApiCredentialCommand(PLAINTEXT_KEY, "UNIFIED_ACCESS"));

        assertEquals(CredentialValidationFailureReason.CREDENTIAL_EXPIRED, result.getFailureReason());
    }

    @Test
    @DisplayName("unavailable consumer returns categorized failure and updates snapshot")
    void shouldRejectUnavailableConsumer() {
        ConsumerAggregate disabledConsumer = ConsumerAggregate.reconstitute(
                ConsumerId.of("consumer-2"),
                ConsumerCode.of("consumer_code_2"),
                "consumer-two",
                ConsumerType.USER_ACCOUNT,
                ConsumerStatus.DISABLED,
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600),
                false,
                0L
        );
        consumerIdentityRepositoryPort.save(disabledConsumer);
        ApiCredentialAggregate credential = createCredential("cred-5", ApiCredentialStatus.ENABLED, null, disabledConsumer);
        apiCredentialRepositoryPort.save(credential);

        CredentialValidationResult result = service.validateApiCredential(
                new ValidateApiCredentialCommand(PLAINTEXT_KEY, "UNIFIED_ACCESS"));

        assertEquals(CredentialValidationFailureReason.CONSUMER_UNAVAILABLE, result.getFailureReason());
        assertEquals("consumer-2", result.getConsumerContext().getConsumerId());
        assertEquals("CONSUMER_UNAVAILABLE", apiCredentialRepositoryPort.findByFingerprintHash(sha256Hex(PLAINTEXT_KEY))
                .orElseThrow().getLastUsedSnapshot().getLastUsedResult());
    }

    private ApiCredentialAggregate createCredential(
            String credentialId, ApiCredentialStatus status, Instant expireAt, ConsumerAggregate consumer) {
        return ApiCredentialAggregate.reconstitute(
                ApiCredentialId.of(credentialId),
                ApiCredentialCode.of("cred_code_" + credentialId.toLowerCase(Locale.ROOT)),
                consumer.getId(),
                consumer.getCode(),
                "test-key",
                "for validation",
                KeyFingerprint.of("ak_live", "ak_live_****1234", sha256Hex(PLAINTEXT_KEY)),
                status,
                ExpirationPolicy.of(expireAt),
                LastUsedSnapshot.empty(),
                status == ApiCredentialStatus.REVOKED ? Instant.now().minusSeconds(30) : null,
                Instant.now().minusSeconds(120),
                Instant.now().minusSeconds(120),
                false,
                0L
        );
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
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
            return List.of();
        }

        @Override
        public long countByConsumerId(ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, Instant now) {
            return 0;
        }

        @Override
        public void save(ApiCredentialAggregate aggregate) {
            storage.put(aggregate.getId().getValue(), aggregate);
        }

        int size() {
            return storage.size();
        }
    }
}
