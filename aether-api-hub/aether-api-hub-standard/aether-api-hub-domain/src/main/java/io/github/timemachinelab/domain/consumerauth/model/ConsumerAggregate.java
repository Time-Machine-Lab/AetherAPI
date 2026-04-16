package io.github.timemachinelab.domain.consumerauth.model;

import java.time.Instant;
import java.util.Objects;

/**
 * 内部 Consumer 聚合。
 */
public class ConsumerAggregate {

    private ConsumerId id;
    private ConsumerCode code;
    private String name;
    private ConsumerType type;
    private ConsumerStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected ConsumerAggregate() {
    }

    private ConsumerAggregate(
            ConsumerId id,
            ConsumerCode code,
            String name,
            ConsumerType type,
            ConsumerStatus status,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = Objects.requireNonNull(id, "ConsumerId must not be null");
        this.code = Objects.requireNonNull(code, "ConsumerCode must not be null");
        this.name = normalizeName(name);
        this.type = Objects.requireNonNull(type, "ConsumerType must not be null");
        this.status = Objects.requireNonNull(status, "ConsumerStatus must not be null");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.version = version;
    }

    public static ConsumerAggregate createInternal(ConsumerId id, ConsumerCode code, String name) {
        Instant now = Instant.now();
        return new ConsumerAggregate(
                id,
                code,
                name,
                ConsumerType.USER_ACCOUNT,
                ConsumerStatus.ENABLED,
                now,
                now,
                false,
                0L
        );
    }

    public static ConsumerAggregate reconstitute(
            ConsumerId id,
            ConsumerCode code,
            String name,
            ConsumerType type,
            ConsumerStatus status,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new ConsumerAggregate(id, code, name, type, status, createdAt, updatedAt, deleted, version);
    }

    public void enable() {
        ensureNotDeleted();
        if (status == ConsumerStatus.ENABLED) {
            throw new ConsumerAuthDomainException("Consumer is already enabled");
        }
        status = ConsumerStatus.ENABLED;
        touch();
    }

    public void disable() {
        ensureNotDeleted();
        if (status == ConsumerStatus.DISABLED) {
            throw new ConsumerAuthDomainException("Consumer is already disabled");
        }
        status = ConsumerStatus.DISABLED;
        touch();
    }

    public boolean isAvailable() {
        return !deleted && status == ConsumerStatus.ENABLED;
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new ConsumerAuthDomainException("Consumer has been deleted");
        }
    }

    private static String normalizeName(String name) {
        Objects.requireNonNull(name, "Consumer name must not be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Consumer name must not be blank");
        }
        if (trimmed.length() > 128) {
            throw new IllegalArgumentException("Consumer name must not exceed 128 characters");
        }
        return trimmed;
    }

    private void touch() {
        updatedAt = Instant.now();
        version++;
    }

    public ConsumerId getId() {
        return id;
    }

    public ConsumerCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ConsumerType getType() {
        return type;
    }

    public ConsumerStatus getStatus() {
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
