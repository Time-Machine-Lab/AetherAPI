package io.github.timemachinelab.domain.consumerauth.model;

import java.time.Instant;
import java.util.Objects;

/**
 * API 凭证聚合。
 */
public class ApiCredentialAggregate {

    private ApiCredentialId id;
    private ApiCredentialCode code;
    private ConsumerId consumerId;
    private ConsumerCode consumerCode;
    private String name;
    private String description;
    private KeyFingerprint keyFingerprint;
    private ApiCredentialStatus status;
    private ExpirationPolicy expirationPolicy;
    private LastUsedSnapshot lastUsedSnapshot;
    private Instant revokedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private long version;

    protected ApiCredentialAggregate() {
    }

    private ApiCredentialAggregate(
            ApiCredentialId id,
            ApiCredentialCode code,
            ConsumerId consumerId,
            ConsumerCode consumerCode,
            String name,
            String description,
            KeyFingerprint keyFingerprint,
            ApiCredentialStatus status,
            ExpirationPolicy expirationPolicy,
            LastUsedSnapshot lastUsedSnapshot,
            Instant revokedAt,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        this.id = Objects.requireNonNull(id, "ApiCredentialId must not be null");
        this.code = Objects.requireNonNull(code, "ApiCredentialCode must not be null");
        this.consumerId = Objects.requireNonNull(consumerId, "ConsumerId must not be null");
        this.consumerCode = Objects.requireNonNull(consumerCode, "ConsumerCode must not be null");
        this.name = normalizeName(name);
        this.description = normalizeDescription(description);
        this.keyFingerprint = Objects.requireNonNull(keyFingerprint, "KeyFingerprint must not be null");
        this.status = Objects.requireNonNull(status, "ApiCredentialStatus must not be null");
        this.expirationPolicy = Objects.requireNonNull(expirationPolicy, "ExpirationPolicy must not be null");
        this.lastUsedSnapshot = lastUsedSnapshot == null ? LastUsedSnapshot.empty() : lastUsedSnapshot;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.version = version;
    }

    public static ApiCredentialAggregate issue(
            ApiCredentialId id,
            ApiCredentialCode code,
            ConsumerId consumerId,
            ConsumerCode consumerCode,
            String name,
            String description,
            KeyFingerprint keyFingerprint,
            ExpirationPolicy expirationPolicy) {
        Instant now = Instant.now();
        return new ApiCredentialAggregate(
                id,
                code,
                consumerId,
                consumerCode,
                name,
                description,
                keyFingerprint,
                ApiCredentialStatus.ENABLED,
                expirationPolicy,
                LastUsedSnapshot.empty(),
                null,
                now,
                now,
                false,
                0L
        );
    }

    public static ApiCredentialAggregate reconstitute(
            ApiCredentialId id,
            ApiCredentialCode code,
            ConsumerId consumerId,
            ConsumerCode consumerCode,
            String name,
            String description,
            KeyFingerprint keyFingerprint,
            ApiCredentialStatus status,
            ExpirationPolicy expirationPolicy,
            LastUsedSnapshot lastUsedSnapshot,
            Instant revokedAt,
            Instant createdAt,
            Instant updatedAt,
            boolean deleted,
            long version) {
        return new ApiCredentialAggregate(
                id, code, consumerId, consumerCode, name, description, keyFingerprint, status,
                expirationPolicy, lastUsedSnapshot, revokedAt, createdAt, updatedAt, deleted, version);
    }

    public void enable() {
        ensureNotDeleted();
        if (status == ApiCredentialStatus.ENABLED) {
            throw new ConsumerAuthDomainException("API credential is already enabled");
        }
        if (status == ApiCredentialStatus.REVOKED) {
            throw new ConsumerAuthDomainException("Revoked API credential cannot be enabled");
        }
        if (expirationPolicy.isExpired(Instant.now())) {
            throw new ConsumerAuthDomainException("Expired API credential cannot be enabled");
        }
        status = ApiCredentialStatus.ENABLED;
        touch();
    }

    public void disable() {
        ensureNotDeleted();
        if (status == ApiCredentialStatus.DISABLED) {
            throw new ConsumerAuthDomainException("API credential is already disabled");
        }
        if (status == ApiCredentialStatus.REVOKED) {
            throw new ConsumerAuthDomainException("Revoked API credential cannot be disabled");
        }
        status = ApiCredentialStatus.DISABLED;
        touch();
    }

    public void revoke() {
        ensureNotDeleted();
        if (status == ApiCredentialStatus.REVOKED) {
            throw new ConsumerAuthDomainException("API credential is already revoked");
        }
        status = ApiCredentialStatus.REVOKED;
        revokedAt = Instant.now();
        touch();
    }

    public void markUsed(String channel, String result) {
        ensureNotDeleted();
        lastUsedSnapshot = LastUsedSnapshot.of(Instant.now(), channel, result);
        touch();
    }

    public CredentialValidationFailureReason validateForAccess(Instant now) {
        ensureNotDeleted();
        if (status == ApiCredentialStatus.DISABLED) {
            return CredentialValidationFailureReason.CREDENTIAL_DISABLED;
        }
        if (status == ApiCredentialStatus.REVOKED) {
            return CredentialValidationFailureReason.CREDENTIAL_REVOKED;
        }
        if (expirationPolicy.isExpired(now)) {
            return CredentialValidationFailureReason.CREDENTIAL_EXPIRED;
        }
        return null;
    }

    public boolean isExpired(Instant now) {
        return expirationPolicy.isExpired(now);
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new ConsumerAuthDomainException("API credential has been deleted");
        }
    }

    private static String normalizeName(String name) {
        Objects.requireNonNull(name, "Credential name must not be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Credential name must not be blank");
        }
        if (trimmed.length() > 128) {
            throw new IllegalArgumentException("Credential name must not exceed 128 characters");
        }
        return trimmed;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 512) {
            throw new IllegalArgumentException("Credential description must not exceed 512 characters");
        }
        return trimmed;
    }

    private void touch() {
        updatedAt = Instant.now();
        version++;
    }

    public ApiCredentialId getId() {
        return id;
    }

    public ApiCredentialCode getCode() {
        return code;
    }

    public ConsumerId getConsumerId() {
        return consumerId;
    }

    public ConsumerCode getConsumerCode() {
        return consumerCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public KeyFingerprint getKeyFingerprint() {
        return keyFingerprint;
    }

    public ApiCredentialStatus getStatus() {
        return status;
    }

    public ExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    public LastUsedSnapshot getLastUsedSnapshot() {
        return lastUsedSnapshot;
    }

    public Instant getRevokedAt() {
        return revokedAt;
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
