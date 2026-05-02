package io.github.timemachinelab.service.model;

/**
 * Command for current-user API subscription.
 */
public class SubscribeApiCommand {

    private final String currentUserId;
    private final String apiCode;

    public SubscribeApiCommand(String currentUserId, String apiCode) {
        this.currentUserId = currentUserId;
        this.apiCode = apiCode;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getApiCode() {
        return apiCode;
    }
}
