package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * API subscription page result.
 */
public class ApiSubscriptionPageResult {

    private final List<ApiSubscriptionModel> items;
    private final int page;
    private final int size;
    private final long total;

    public ApiSubscriptionPageResult(List<ApiSubscriptionModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<ApiSubscriptionModel> getItems() {
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
