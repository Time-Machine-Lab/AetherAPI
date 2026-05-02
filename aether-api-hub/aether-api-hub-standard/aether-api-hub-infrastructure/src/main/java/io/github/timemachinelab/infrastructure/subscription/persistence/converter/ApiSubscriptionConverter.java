package io.github.timemachinelab.infrastructure.subscription.persistence.converter;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionAggregate;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionId;
import io.github.timemachinelab.domain.subscription.model.ApiSubscriptionStatus;
import io.github.timemachinelab.infrastructure.subscription.persistence.entity.ApiSubscriptionDo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * API subscription persistence converter.
 */
public final class ApiSubscriptionConverter {

    private ApiSubscriptionConverter() {
    }

    public static ApiSubscriptionAggregate toAggregate(ApiSubscriptionDo source) {
        if (source == null) {
            return null;
        }
        return ApiSubscriptionAggregate.reconstitute(
                ApiSubscriptionId.of(source.getId()),
                source.getSubscriptionCode(),
                source.getSubscriberUserId(),
                ConsumerId.of(source.getSubscriberConsumerId()),
                ConsumerCode.of(source.getSubscriberConsumerCode()),
                ApiCode.of(source.getApiCode()),
                source.getAssetOwnerUserId(),
                source.getAssetName(),
                ApiSubscriptionStatus.valueOf(source.getStatus()),
                toInstant(source.getCancelledAt()),
                toInstant(source.getCreatedAt()),
                toInstant(source.getUpdatedAt()),
                Boolean.TRUE.equals(source.getIsDeleted()),
                source.getVersion() == null ? 0L : source.getVersion()
        );
    }

    public static ApiSubscriptionDo toDo(ApiSubscriptionAggregate source) {
        ApiSubscriptionDo target = new ApiSubscriptionDo();
        updateDo(target, source);
        return target;
    }

    public static void updateDo(ApiSubscriptionDo target, ApiSubscriptionAggregate source) {
        target.setId(source.getId().getValue());
        target.setSubscriptionCode(source.getSubscriptionCode());
        target.setSubscriberUserId(source.getSubscriberUserId());
        target.setSubscriberConsumerId(source.getSubscriberConsumerId().getValue());
        target.setSubscriberConsumerCode(source.getSubscriberConsumerCode().getValue());
        target.setApiCode(source.getApiCode().getValue());
        target.setAssetOwnerUserId(source.getAssetOwnerUserId());
        target.setAssetName(source.getAssetName());
        target.setStatus(source.getStatus().name());
        target.setCancelledAt(toLocalDateTime(source.getCancelledAt()));
        target.setCreatedAt(toLocalDateTime(source.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(source.getUpdatedAt()));
        target.setIsDeleted(source.isDeleted());
        target.setVersion(source.getVersion());
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
