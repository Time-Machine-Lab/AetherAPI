package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Platform proxy profile page response.
 */
public class PlatformProxyProfilePageResp {

    private final List<PlatformProxyProfileResp> items;
    private final int page;
    private final int size;
    private final long total;

    public PlatformProxyProfilePageResp(List<PlatformProxyProfileResp> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<PlatformProxyProfileResp> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
}
