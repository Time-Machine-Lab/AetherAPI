package io.github.timemachinelab.service.model;

import java.time.Instant;

/**
 * 当前用户签发 API Key 命令。
 */
public class IssueApiCredentialCommand {

    private final String currentUserId;
    private final String credentialName;
    private final String credentialDescription;
    private final Instant expireAt;

    public IssueApiCredentialCommand(String currentUserId, String credentialName, String credentialDescription, Instant expireAt) {
        this.currentUserId = currentUserId;
        this.credentialName = credentialName;
        this.credentialDescription = credentialDescription;
        this.expireAt = expireAt;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public String getCredentialDescription() {
        return credentialDescription;
    }

    public Instant getExpireAt() {
        return expireAt;
    }
}
