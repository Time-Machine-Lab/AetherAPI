package io.github.timemachinelab.infrastructure.consumerauth.persistence.repository;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialCode;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerAuthDomainException;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.ExpirationPolicy;
import io.github.timemachinelab.domain.consumerauth.model.KeyFingerprint;
import io.github.timemachinelab.domain.consumerauth.model.LastUsedSnapshot;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ApiCredentialDo;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper.ApiCredentialMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MyBatis API credential repository tests.
 */
@ExtendWith(MockitoExtension.class)
class MybatisApiCredentialRepositoryTest {

    private static final String CREDENTIAL_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private ApiCredentialMapper mapper;

    private MybatisApiCredentialRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisApiCredentialRepository(mapper);
    }

    @Test
    @DisplayName("update should use persisted optimistic-lock version")
    void shouldUsePersistedVersionWhenUpdatingExistingCredential() {
        ApiCredentialDo existing = existingDo();
        when(mapper.selectById(CREDENTIAL_ID)).thenReturn(existing);
        when(mapper.updateById(existing)).thenReturn(1);

        repository.save(disabledAggregate(1L));

        ArgumentCaptor<ApiCredentialDo> captor = ArgumentCaptor.forClass(ApiCredentialDo.class);
        verify(mapper).updateById(captor.capture());
        assertEquals(0L, captor.getValue().getVersion());
        assertEquals("DISABLED", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("update conflict should be exposed as credential business error")
    void shouldThrowBusinessErrorWhenUpdateMissesRow() {
        ApiCredentialDo existing = existingDo();
        when(mapper.selectById(CREDENTIAL_ID)).thenReturn(existing);
        when(mapper.updateById(existing)).thenReturn(0);

        ConsumerAuthDomainException exception = assertThrows(
                ConsumerAuthDomainException.class,
                () -> repository.save(disabledAggregate(1L))
        );

        assertTrue(exception.getMessage().contains("update conflict"));
    }

    private ApiCredentialDo existingDo() {
        ApiCredentialDo existing = new ApiCredentialDo();
        existing.setId(CREDENTIAL_ID);
        existing.setCredentialCode("cred_test");
        existing.setConsumerId("consumer-id");
        existing.setConsumerCode("consumer_test");
        existing.setCredentialName("默认密钥");
        existing.setKeyPrefix("ak_live");
        existing.setMaskedKey("ak_live_****1234");
        existing.setFingerprintHash("hash");
        existing.setStatus("ENABLED");
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setIsDeleted(false);
        existing.setVersion(0L);
        return existing;
    }

    private ApiCredentialAggregate disabledAggregate(long version) {
        Instant now = Instant.now();
        return ApiCredentialAggregate.reconstitute(
                ApiCredentialId.of(CREDENTIAL_ID),
                ApiCredentialCode.of("cred_test"),
                ConsumerId.of("consumer-id"),
                ConsumerCode.of("consumer_test"),
                "默认密钥",
                null,
                KeyFingerprint.of("ak_live", "ak_live_****1234", "hash"),
                ApiCredentialStatus.DISABLED,
                ExpirationPolicy.of(null),
                LastUsedSnapshot.empty(),
                null,
                now,
                now,
                false,
                version
        );
    }
}
