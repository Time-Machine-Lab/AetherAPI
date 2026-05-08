package io.github.timemachinelab.api.resp;

import java.util.List;

/**
 * Platform proxy asset binding candidate page response.
 */
public class PlatformProxyAssetCandidatePageResp {

    private final List<PlatformProxyAssetCandidateResp> items;
    private final int page;
    private final int size;
    private final long total;

    public PlatformProxyAssetCandidatePageResp(
            List<PlatformProxyAssetCandidateResp> items,
            int page,
            int size,
            long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<PlatformProxyAssetCandidateResp> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
}
