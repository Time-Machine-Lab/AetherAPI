package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 首次签发的 API 凭证响应。
 */
public class IssuedApiCredentialResp extends ApiCredentialResp {

    @JsonProperty("plaintextKey")
    private String plaintextKey;

    public IssuedApiCredentialResp() {
    }

    public IssuedApiCredentialResp(
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
            LastUsedSnapshotResp lastUsedSnapshot,
            String plaintextKey) {
        super(
                credentialId,
                credentialCode,
                credentialName,
                credentialDescription,
                maskedKey,
                keyPrefix,
                status,
                expireAt,
                revokedAt,
                createdAt,
                updatedAt,
                lastUsedSnapshot
        );
        this.plaintextKey = plaintextKey;
    }

    public String getPlaintextKey() {
        return plaintextKey;
    }

    public void setPlaintextKey(String plaintextKey) {
        this.plaintextKey = plaintextKey;
    }
}
