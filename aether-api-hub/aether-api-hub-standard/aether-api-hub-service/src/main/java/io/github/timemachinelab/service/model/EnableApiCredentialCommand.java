package io.github.timemachinelab.service.model;

/**
 * 当前用户启用 API Key 命令。
 */
public class EnableApiCredentialCommand {

    private final String currentUserId;
    private final String credentialId;

    public EnableApiCredentialCommand(String currentUserId, String credentialId) {
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
