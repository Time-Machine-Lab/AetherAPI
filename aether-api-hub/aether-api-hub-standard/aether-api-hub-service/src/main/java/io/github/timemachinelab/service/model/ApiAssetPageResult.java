package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Asset management page result.
 */
public class ApiAssetPageResult {

    private final List<ApiAssetSummaryModel> items;
    private final int page;
    private final int size;
    private final long total;

    public ApiAssetPageResult(List<ApiAssetSummaryModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<ApiAssetSummaryModel> getItems() {
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
