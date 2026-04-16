package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * API 凭证分页结果。
 */
public class ApiCredentialPageResult {

    private final List<ApiCredentialModel> items;
    private final int page;
    private final int size;
    private final long total;

    public ApiCredentialPageResult(List<ApiCredentialModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<ApiCredentialModel> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }
}
