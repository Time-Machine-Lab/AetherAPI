package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current-user API subscription response.
 */
public class ApiSubscriptionResp {

    @JsonProperty("subscriptionId")
    private String subscriptionId;

    @JsonProperty("apiCode")
    private String apiCode;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetOwnerUserId")
    private String assetOwnerUserId;

    @JsonProperty("subscriptionStatus")
    private String subscriptionStatus;

    @JsonProperty("subscribed")
    private boolean subscribed;

    @JsonProperty("ownerAccess")
    private boolean ownerAccess;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("cancelledAt")
    private String cancelledAt;

    public ApiSubscriptionResp() {
    }

    public ApiSubscriptionResp(
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

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetOwnerUserId() {
        return assetOwnerUserId;
    }

    public void setAssetOwnerUserId(String assetOwnerUserId) {
        this.assetOwnerUserId = assetOwnerUserId;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public boolean isOwnerAccess() {
        return ownerAccess;
    }

    public void setOwnerAccess(boolean ownerAccess) {
        this.ownerAccess = ownerAccess;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
