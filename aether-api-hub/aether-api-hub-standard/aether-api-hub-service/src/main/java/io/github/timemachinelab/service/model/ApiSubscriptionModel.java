package io.github.timemachinelab.service.model;

/**
 * Application model for API subscription.
 */
public class ApiSubscriptionModel {

    private final String subscriptionId;
    private final String apiCode;
    private final String assetName;
    private final String assetOwnerUserId;
    private final String subscriptionStatus;
    private final boolean subscribed;
    private final boolean ownerAccess;
    private final String createdAt;
    private final String updatedAt;
    private final String cancelledAt;

    public ApiSubscriptionModel(
            String subscriptionId,
            String apiCode,
            String assetName,
            String assetOwnerUserId,
            String subscriptionStatus,
            boolean subscribed,
            boolean ownerAccess,
            String createdAt,
            String updatedAt,
            String cancelledAt) {
        this.subscriptionId = subscriptionId;
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetOwnerUserId = assetOwnerUserId;
        this.subscriptionStatus = subscriptionStatus;
        this.subscribed = subscribed;
        this.ownerAccess = ownerAccess;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledAt = cancelledAt;
    }

    public static ApiSubscriptionModel ownerAccess(String apiCode, String assetName, String assetOwnerUserId) {
        return new ApiSubscriptionModel(
                null,
                apiCode,
                assetName,
                assetOwnerUserId,
                "OWNER",
                false,
                true,
                null,
                null,
                null
        );
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getAssetOwnerUserId() {
        return assetOwnerUserId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }
}
