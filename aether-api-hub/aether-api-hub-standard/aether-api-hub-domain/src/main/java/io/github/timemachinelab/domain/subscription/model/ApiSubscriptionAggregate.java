package io.github.timemachinelab.domain.subscription.model;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;

import java.time.Instant;
import java.util.Objects;

/**
 * API subscription aggregate root.
 */
public class ApiSubscriptionAggregate {

    private ApiSubscriptionId id;
    private String subscriptionCode;
    private String subscriberUserId;
    private ConsumerId subscriberConsumerId;
    private ConsumerCode subscriberConsumerCode;
    private ApiCode apiCode;
    private String assetOwnerUserId;
    private String assetName;
    private ApiSubscriptionStatus status;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected ApiSubscriptionAggregate() {
    }

    private ApiSubscriptionAggregate(
            ApiSubscriptionId id,
            String subscriptionCode,
            String subscriberUserId,
            ConsumerId subscriberConsumerId,
            ConsumerCode subscriberConsumerCode,
            ApiCode apiCode,
            String assetOwnerUserId,
            String assetName,
            ApiSubscriptionStatus status,
            Instant cancelledAt,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = Objects.requireNonNull(id, "Subscription id must not be null");
        this.subscriptionCode = requireText(subscriptionCode, "Subscription code must not be blank");
        this.subscriberUserId = requireText(subscriberUserId, "Subscriber user id must not be blank");
        this.subscriberConsumerId = Objects.requireNonNull(subscriberConsumerId, "Subscriber consumer id must not be null");
        this.subscriberConsumerCode = Objects.requireNonNull(subscriberConsumerCode, "Subscriber consumer code must not be null");
        this.apiCode = Objects.requireNonNull(apiCode, "API code must not be null");
        this.assetOwnerUserId = requireText(assetOwnerUserId, "Asset owner user id must not be blank");
        this.assetName = normalizeOptional(assetName);
        this.status = Objects.requireNonNull(status, "Subscription status must not be null");
        this.cancelledAt = cancelledAt;
        this.createdAt = Objects.requireNonNull(createdAt, "Created time must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated time must not be null");
        this.deleted = deleted;
        this.version = version;
        if (status == ApiSubscriptionStatus.ACTIVE) {
            this.cancelledAt = null;
        }
    }

    public static ApiSubscriptionAggregate createActive(
            ApiSubscriptionId id,
            String subscriptionCode,
            String subscriberUserId,
            ConsumerId subscriberConsumerId,
            ConsumerCode subscriberConsumerCode,
            ApiCode apiCode,
            String assetOwnerUserId,
            String assetName) {
        if (requireText(subscriberUserId, "Subscriber user id must not be blank")
                .equals(requireText(assetOwnerUserId, "Asset owner user id must not be blank"))) {
            throw new ApiSubscriptionDomainException("Asset owner already has owner access");
        }
        Instant now = Instant.now();
        return new ApiSubscriptionAggregate(
                id,
                subscriptionCode,
                subscriberUserId,
                subscriberConsumerId,
                subscriberConsumerCode,
                apiCode,
                assetOwnerUserId,
                assetName,
                ApiSubscriptionStatus.ACTIVE,
                null,
                now,
                now,
                false,
                0L
        );
    }

    public static ApiSubscriptionAggregate reconstitute(
            ApiSubscriptionId id,
            String subscriptionCode,
            String subscriberUserId,
            ConsumerId subscriberConsumerId,
            ConsumerCode subscriberConsumerCode,
            ApiCode apiCode,
            String assetOwnerUserId,
            String assetName,
            ApiSubscriptionStatus status,
            Instant cancelledAt,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new ApiSubscriptionAggregate(
                id,
                subscriptionCode,
                subscriberUserId,
                subscriberConsumerId,
                subscriberConsumerCode,
                apiCode,
                assetOwnerUserId,
                assetName,
                status,
                cancelledAt,
                createdAt,
                updatedAt,
                deleted,
                version
        );
    }

    public void cancel() {
        ensureNotDeleted();
        if (status == ApiSubscriptionStatus.CANCELLED) {
            throw new ApiSubscriptionDomainException("API subscription is already cancelled");
        }
        status = ApiSubscriptionStatus.CANCELLED;
        cancelledAt = Instant.now();
        touch();
    }

    public boolean isActive() {
        return !deleted && status == ApiSubscriptionStatus.ACTIVE;
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new ApiSubscriptionDomainException("API subscription has been deleted");
        }
    }

    private void touch() {
        updatedAt = Instant.now();
        version++;
    }

    private static String requireText(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public ApiSubscriptionId getId() {
        return id;
    }

    public String getSubscriptionCode() {
        return subscriptionCode;
    }

    public String getSubscriberUserId() {
        return subscriberUserId;
    }

    public ConsumerId getSubscriberConsumerId() {
        return subscriberConsumerId;
    }

    public ConsumerCode getSubscriberConsumerCode() {
        return subscriberConsumerCode;
    }

    public ApiCode getApiCode() {
        return apiCode;
    }

    public String getAssetOwnerUserId() {
        return assetOwnerUserId;
    }

    public String getAssetName() {
        return assetName;
    }

    public ApiSubscriptionStatus getStatus() {
        return status;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getVersion() {
        return version;
    }
}
