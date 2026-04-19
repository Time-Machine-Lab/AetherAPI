package io.github.timemachinelab.service.model;

import java.time.Instant;

/**
 * Current-user API call log list query.
 */
public class ListApiCallLogQuery {

    private final String currentUserId;
    private final String targetApiCode;
    private final Instant invocationStartAt;
    private final Instant invocationEndAt;
    private final int page;
    private final int size;

    public ListApiCallLogQuery(
            String currentUserId,
            String targetApiCode,
            Instant invocationStartAt,
            Instant invocationEndAt,
            int page,
            int size) {
        this.currentUserId = currentUserId;
        this.targetApiCode = targetApiCode;
        this.invocationStartAt = invocationStartAt;
        this.invocationEndAt = invocationEndAt;
        this.page = page;
        this.size = size;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public Instant getInvocationStartAt() {
        return invocationStartAt;
    }

    public Instant getInvocationEndAt() {
        return invocationEndAt;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
