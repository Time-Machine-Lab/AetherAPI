package io.github.timemachinelab.infrastructure.consumerauth.persistence.converter;

import io.github.timemachinelab.domain.consumerauth.model.ConsumerAggregate;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerStatus;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerType;
import io.github.timemachinelab.infrastructure.consumerauth.persistence.entity.ConsumerIdentityDo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Consumer 转换器。
 */
public final class ConsumerIdentityConverter {

    private ConsumerIdentityConverter() {
    }

    public static ConsumerAggregate toAggregate(ConsumerIdentityDo source) {
        if (source == null) {
            return null;
        }
        return ConsumerAggregate.reconstitute(
                ConsumerId.of(source.getId()),
                ConsumerCode.of(source.getConsumerCode()),
                source.getConsumerName(),
                ConsumerType.valueOf(source.getConsumerType()),
                ConsumerStatus.valueOf(source.getStatus()),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static ConsumerIdentityDo toDo(ConsumerAggregate source) {
        ConsumerIdentityDo target = new ConsumerIdentityDo();
        updateDo(target, source);
        return target;
    }

    public static void updateDo(ConsumerIdentityDo target, ConsumerAggregate source) {
        target.setId(source.getId().getValue());
        target.setConsumerCode(source.getCode().getValue());
        target.setConsumerName(source.getName());
        target.setConsumerType(source.getType().name());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(LocalDateTime.ofInstant(source.getCreatedAt(), ZoneOffset.UTC));
        target.setUpdatedAt(LocalDateTime.ofInstant(source.getUpdatedAt(), ZoneOffset.UTC));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static java.time.Instant toInstant(LocalDateTime value) {
        return value == null ? java.time.Instant.now() : value.toInstant(ZoneOffset.UTC);
    }
}
