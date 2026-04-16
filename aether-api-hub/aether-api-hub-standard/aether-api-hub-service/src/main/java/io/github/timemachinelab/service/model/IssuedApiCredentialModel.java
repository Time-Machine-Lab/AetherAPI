package io.github.timemachinelab.service.model;

/**
 * 首次签发返回的 API 凭证模型。
 */
public class IssuedApiCredentialModel extends ApiCredentialModel {

    private final String plaintextKey;

    public IssuedApiCredentialModel(
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
            LastUsedSnapshotModel lastUsedSnapshot,
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
}
