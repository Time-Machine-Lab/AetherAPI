package io.github.timemachinelab.service.model;

/**
 * Command for cancelling current-user API subscription.
 */
public class CancelApiSubscriptionCommand {

    private final String currentUserId;
    private final String subscriptionId;

    public CancelApiSubscriptionCommand(String currentUserId, String subscriptionId) {
        this.currentUserId = currentUserId;
        this.subscriptionId = subscriptionId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
