package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Paged platform proxy asset binding candidate result.
 */
public class PlatformProxyAssetCandidatePageResult {

    private final List<PlatformProxyAssetCandidateModel> items;
    private final int page;
    private final int size;
    private final long total;

    public PlatformProxyAssetCandidatePageResult(
            List<PlatformProxyAssetCandidateModel> items,
            int page,
            int size,
            long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<PlatformProxyAssetCandidateModel> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
}
