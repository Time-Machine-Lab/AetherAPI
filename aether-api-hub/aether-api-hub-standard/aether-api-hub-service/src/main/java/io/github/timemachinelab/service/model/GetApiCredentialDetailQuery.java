package io.github.timemachinelab.service.model;

/**
 * 当前用户查询 API Key 详情命令。
 */
public class GetApiCredentialDetailQuery {

    private final String currentUserId;
    private final String credentialId;

    public GetApiCredentialDetailQuery(String currentUserId, String credentialId) {
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
