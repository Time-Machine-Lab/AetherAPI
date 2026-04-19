package io.github.timemachinelab.service.model;

/**
 * Current-user API call log detail query.
 */
public class GetApiCallLogDetailQuery {

    private final String currentUserId;
    private final String logId;

    public GetApiCallLogDetailQuery(String currentUserId, String logId) {
        this.currentUserId = currentUserId;
        this.logId = logId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getLogId() {
        return logId;
    }
}
