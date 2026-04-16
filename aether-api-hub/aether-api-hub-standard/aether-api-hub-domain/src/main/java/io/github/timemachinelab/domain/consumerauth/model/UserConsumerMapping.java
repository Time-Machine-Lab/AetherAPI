package io.github.timemachinelab.domain.consumerauth.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 用户与 Consumer 的隐式映射。
 */
public class UserConsumerMapping {

    private String id;
    private String userId;
    private ConsumerId consumerId;
    private ConsumerCode consumerCode;
    private MappingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected UserConsumerMapping() {
    }

    private UserConsumerMapping(
            String id,
            String userId,
            ConsumerId consumerId,
            ConsumerCode consumerCode,
            MappingStatus status,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = id;
        this.userId = normalizeUserId(userId);
        this.consumerId = Objects.requireNonNull(consumerId, "ConsumerId must not be null");
        this.consumerCode = Objects.requireNonNull(consumerCode, "ConsumerCode must not be null");
        this.status = Objects.requireNonNull(status, "Mapping status must not be null");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.version = version;
    }

    public static UserConsumerMapping createActive(String userId, ConsumerId consumerId, ConsumerCode consumerCode) {
        Instant now = Instant.now();
        return new UserConsumerMapping(
                UUID.randomUUID().toString(),
                userId,
                consumerId,
                consumerCode,
                MappingStatus.ACTIVE,
                now,
                now,
                false,
                0L
        );
    }

    public static UserConsumerMapping reconstitute(
            String id,
            String userId,
            ConsumerId consumerId,
            ConsumerCode consumerCode,
            MappingStatus status,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new UserConsumerMapping(id, userId, consumerId, consumerCode, status, createdAt, updatedAt, deleted, version);
    }

    private static String normalizeUserId(String userId) {
        Objects.requireNonNull(userId, "UserId must not be null");
        String trimmed = userId.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("UserId must not be blank");
        }
        return trimmed;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public ConsumerId getConsumerId() {
        return consumerId;
    }

    public ConsumerCode getConsumerCode() {
        return consumerCode;
    }

    public MappingStatus getStatus() {
        return status;
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
