package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API 凭证响应。
 */
public class ApiCredentialResp {

    @JsonProperty("credentialId")
    private String credentialId;

    @JsonProperty("credentialCode")
    private String credentialCode;

    @JsonProperty("credentialName")
    private String credentialName;

    @JsonProperty("credentialDescription")
    private String credentialDescription;

    @JsonProperty("maskedKey")
    private String maskedKey;

    @JsonProperty("keyPrefix")
    private String keyPrefix;

    @JsonProperty("status")
    private String status;

    @JsonProperty("expireAt")
    private String expireAt;

    @JsonProperty("revokedAt")
    private String revokedAt;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("lastUsedSnapshot")
    private LastUsedSnapshotResp lastUsedSnapshot;

    public ApiCredentialResp() {
    }

    public ApiCredentialResp(
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
            LastUsedSnapshotResp lastUsedSnapshot) {
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

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public void setCredentialCode(String credentialCode) {
        this.credentialCode = credentialCode;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getCredentialDescription() {
        return credentialDescription;
    }

    public void setCredentialDescription(String credentialDescription) {
        this.credentialDescription = credentialDescription;
    }

    public String getMaskedKey() {
        return maskedKey;
    }

    public void setMaskedKey(String maskedKey) {
        this.maskedKey = maskedKey;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }

    public String getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(String revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LastUsedSnapshotResp getLastUsedSnapshot() {
        return lastUsedSnapshot;
    }

    public void setLastUsedSnapshot(LastUsedSnapshotResp lastUsedSnapshot) {
        this.lastUsedSnapshot = lastUsedSnapshot;
    }
}
