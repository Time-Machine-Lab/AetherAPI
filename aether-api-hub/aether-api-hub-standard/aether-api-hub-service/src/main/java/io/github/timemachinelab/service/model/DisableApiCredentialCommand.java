package io.github.timemachinelab.service.model;

/**
 * 当前用户停用 API Key 命令。
 */
public class DisableApiCredentialCommand {

    private final String currentUserId;
    private final String credentialId;

    public DisableApiCredentialCommand(String currentUserId, String credentialId) {
        this.currentUserId = currentUserId;
        this.credentialId = credentialId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCredentialId() {
        return credentialId;
    }
}
