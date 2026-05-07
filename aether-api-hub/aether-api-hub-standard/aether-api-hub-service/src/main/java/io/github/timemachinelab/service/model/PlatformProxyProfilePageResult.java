package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Paged platform proxy profile result.
 */
public class PlatformProxyProfilePageResult {

    private final List<PlatformProxyProfileModel> items;
    private final int page;
    private final int size;
    private final long total;

    public PlatformProxyProfilePageResult(List<PlatformProxyProfileModel> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<PlatformProxyProfileModel> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
}
