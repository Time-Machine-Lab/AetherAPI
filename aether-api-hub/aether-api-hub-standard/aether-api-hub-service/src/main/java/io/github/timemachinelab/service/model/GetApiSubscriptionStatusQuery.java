package io.github.timemachinelab.service.model;

/**
 * Query for current-user subscription status by apiCode.
 */
public class GetApiSubscriptionStatusQuery {

    private final String currentUserId;
    private final String apiCode;

    public GetApiSubscriptionStatusQuery(String currentUserId, String apiCode) {
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
