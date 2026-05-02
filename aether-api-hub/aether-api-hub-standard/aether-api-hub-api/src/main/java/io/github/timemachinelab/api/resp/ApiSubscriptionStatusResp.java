package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current-user subscription status for one API asset.
 */
public class ApiSubscriptionStatusResp {

    @JsonProperty("apiCode")
    private String apiCode;

    @JsonProperty("accessStatus")
    private String accessStatus;

    @JsonProperty("subscriptionId")
    private String subscriptionId;

    @JsonProperty("subscriptionStatus")
    private String subscriptionStatus;

    @JsonProperty("subscribed")
    private boolean subscribed;

    @JsonProperty("ownerAccess")
    private boolean ownerAccess;

    public ApiSubscriptionStatusResp() {
    }

    public ApiSubscriptionStatusResp(
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

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(String accessStatus) {
        this.accessStatus = accessStatus;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
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
}
