package io.github.timemachinelab.service.model;

/**
 * 当前用户分页查询 API Key 命令。
 */
public class ListApiCredentialQuery {

    private final String currentUserId;
    private final String status;
    private final int page;
    private final int size;

    public ListApiCredentialQuery(String currentUserId, String status, int page, int size) {
        this.currentUserId = currentUserId;
        this.status = status;
        this.page = page;
        this.size = size;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getStatus() {
        return status;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
