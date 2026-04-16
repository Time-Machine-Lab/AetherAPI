package io.github.timemachinelab.infrastructure.consumerauth.persistence.converter;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.MappingStatus;
import io.github.timemachinelab.domain.consumerauth.model.UserConsumerMapping;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.UserConsumerMappingDo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 用户-Consumer 映射转换器。
 */
public final class UserConsumerMappingConverter {

    private UserConsumerMappingConverter() {
    }

    public static UserConsumerMapping toAggregate(UserConsumerMappingDo source) {
        if (source == null) {
            return null;
        }
        return UserConsumerMapping.reconstitute(
                source.getId(),
                source.getUserId(),
                ConsumerId.of(source.getConsumerId()),
                ConsumerCode.of(source.getConsumerCode()),
                MappingStatus.valueOf(source.getMappingStatus()),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static UserConsumerMappingDo toDo(UserConsumerMapping source) {
        UserConsumerMappingDo target = new UserConsumerMappingDo();
        updateDo(target, source);
        return target;
    }

    public static void updateDo(UserConsumerMappingDo target, UserConsumerMapping source) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setConsumerId(source.getConsumerId().getValue());
        target.setConsumerCode(source.getConsumerCode().getValue());
        target.setMappingStatus(source.getStatus().name());
        target.setCreatedAt(LocalDateTime.ofInstant(source.getCreatedAt(), ZoneOffset.UTC));
        target.setUpdatedAt(LocalDateTime.ofInstant(source.getUpdatedAt(), ZoneOffset.UTC));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static java.time.Instant toInstant(LocalDateTime value) {
        return value == null ? java.time.Instant.now() : value.toInstant(ZoneOffset.UTC);
    }
}
