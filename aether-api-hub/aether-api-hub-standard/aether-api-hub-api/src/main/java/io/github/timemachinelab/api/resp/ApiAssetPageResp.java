package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Asset management page response.
 */
public class ApiAssetPageResp {

    @JsonProperty("items")
    private List<ApiAssetSummaryResp> items;

    @JsonProperty("page")
    private int page;

    @JsonProperty("size")
    private int size;

    @JsonProperty("total")
    private long total;

    public ApiAssetPageResp() {
    }

    public ApiAssetPageResp(List<ApiAssetSummaryResp> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<ApiAssetSummaryResp> getItems() {
        return items;
    }

    public void setItems(List<ApiAssetSummaryResp> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
