package io.github.timemachinelab.infrastructure.consumerauth.persistence.converter;

import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialCode;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialId;
import io.github.timemachinelab.domain.consumerauth.model.ApiCredentialStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.ExpirationPolicy;
import io.github.timemachinelab.domain.consumerauth.model.KeyFingerprint;
import io.github.timemachinelab.domain.consumerauth.model.LastUsedSnapshot;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ApiCredentialDo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * API 凭证转换器。
 */
public final class ApiCredentialConverter {

    private ApiCredentialConverter() {
    }

    public static ApiCredentialAggregate toAggregate(ApiCredentialDo source) {
        if (source == null) {
            return null;
        }
        return ApiCredentialAggregate.reconstitute(
                ApiCredentialId.of(source.getId()),
                ApiCredentialCode.of(source.getCredentialCode()),
                ConsumerId.of(source.getConsumerId()),
                ConsumerCode.of(source.getConsumerCode()),
                source.getCredentialName(),
                source.getCredentialDescription(),
                KeyFingerprint.of(source.getKeyPrefix(), source.getMaskedKey(), source.getFingerprintHash()),
                ApiCredentialStatus.valueOf(source.getStatus()),
                ExpirationPolicy.of(toInstant(source.getExpireAt())),
                LastUsedSnapshot.of(toInstant(source.getLastUsedAt()), source.getLastUsedChannel(), source.getLastUsedResult()),
                toInstant(source.getRevokedAt()),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static ApiCredentialDo toDo(ApiCredentialAggregate source) {
        ApiCredentialDo target = new ApiCredentialDo();
        updateDo(target, source);
        return target;
    }

    public static void updateDo(ApiCredentialDo target, ApiCredentialAggregate source) {
        target.setId(source.getId().getValue());
        target.setCredentialCode(source.getCode().getValue());
        target.setConsumerId(source.getConsumerId().getValue());
        target.setConsumerCode(source.getConsumerCode().getValue());
        target.setCredentialName(source.getName());
        target.setCredentialDescription(source.getDescription());
        target.setKeyPrefix(source.getKeyFingerprint().getPrefix());
        target.setMaskedKey(source.getKeyFingerprint().getMaskedKey());
        target.setFingerprintHash(source.getKeyFingerprint().getHashValue());
        target.setStatus(source.getStatus().name());
        target.setExpireAt(toLocalDateTime(source.getExpirationPolicy().getExpireAt()));
        target.setLastUsedAt(toLocalDateTime(source.getLastUsedSnapshot().getLastUsedAt()));
        target.setLastUsedChannel(source.getLastUsedSnapshot().getLastUsedChannel());
        target.setLastUsedResult(source.getLastUsedSnapshot().getLastUsedResult());
        target.setRevokedAt(toLocalDateTime(source.getRevokedAt()));
        target.setCreatedAt(toLocalDateTime(source.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(source.getUpdatedAt()));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static java.time.Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTime(java.time.Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
