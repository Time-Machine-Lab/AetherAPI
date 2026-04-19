package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * API call log page result.
 */
public class ApiCallLogPageResult {

    private final List<ApiCallLogModel> items;
    private final int page;
    private final int size;
    private final long total;

    public ApiCallLogPageResult(List<ApiCallLogModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<ApiCallLogModel> getItems() {
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
