package io.github.timemachinelab.service.model;

/**
 * Query for current-user API subscriptions.
 */
public class ListApiSubscriptionQuery {

    private final String currentUserId;
    private final int page;
    private final int size;

    public ListApiSubscriptionQuery(String currentUserId, int page, int size) {
        this.currentUserId = currentUserId;
        this.page = page;
        this.size = size;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
