package io.github.timemachinelab.service.model;

/**
 * API 凭证管理模型。
 */
public class ApiCredentialModel {

    private final String credentialId;
    private final String credentialCode;
    private final String credentialName;
    private final String credentialDescription;
    private final String maskedKey;
    private final String keyPrefix;
    private final String status;
    private final String expireAt;
    private final String revokedAt;
    private final String createdAt;
    private final String updatedAt;
    private final LastUsedSnapshotModel lastUsedSnapshot;

    public ApiCredentialModel(
            String credentialId,
            String credentialCode,
            String credentialName,
            String credentialDescription,
            String maskedKey,
            String keyPrefix,
            String status,
            String expireAt,
            String revokedAt,
            String createdAt,
            String updatedAt,
            LastUsedSnapshotModel lastUsedSnapshot) {
        this.credentialId = credentialId;
        this.credentialCode = credentialCode;
        this.credentialName = credentialName;
        this.credentialDescription = credentialDescription;
        this.maskedKey = maskedKey;
        this.keyPrefix = keyPrefix;
        this.status = status;
        this.expireAt = expireAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastUsedSnapshot = lastUsedSnapshot;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public String getCredentialDescription() {
        return credentialDescription;
    }

    public String getMaskedKey() {
        return maskedKey;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getStatus() {
        return status;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public String getRevokedAt() {
        return revokedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public LastUsedSnapshotModel getLastUsedSnapshot() {
        return lastUsedSnapshot;
    }
}
