package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * API 凭证仓储端口。
 */
public interface ApiCredentialRepositoryPort {

    Optional<ApiCredentialAggregate> findByFingerprintHash(String fingerprintHash);

    Optional<ApiCredentialAggregate> findByIdAndConsumerId(ApiCredentialId credentialId, ConsumerId consumerId);

    List<ApiCredentialAggregate> findPageByConsumerId(
            ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, int page, int size, Instant now);

    long countByConsumerId(ConsumerId consumerId, ApiCredentialStatus status, boolean expiredOnly, Instant now);

    void save(ApiCredentialAggregate aggregate);
}
