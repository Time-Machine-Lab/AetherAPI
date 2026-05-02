package io.github.timemachinelab.service.model;

/**
 * Current-user subscription status model.
 */
public class ApiSubscriptionStatusModel {

    private final String apiCode;
    private final String accessStatus;
    private final String subscriptionId;
    private final String subscriptionStatus;
    private final boolean subscribed;
    private final boolean ownerAccess;

    public ApiSubscriptionStatusModel(
            String apiCode,
            String accessStatus,
            String subscriptionId,
            String subscriptionStatus,
            boolean subscribed,
            boolean ownerAccess) {
        this.apiCode = apiCode;
        this.accessStatus = accessStatus;
        this.subscriptionId = subscriptionId;
        this.subscriptionStatus = subscriptionStatus;
        this.subscribed = subscribed;
        this.ownerAccess = ownerAccess;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getAccessStatus() {
        return accessStatus;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public boolean isOwnerAccess() {
        return ownerAccess;
    }
}
