package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Current-user API subscription page response.
 */
public class ApiSubscriptionPageResp {

    @JsonProperty("items")
    private List<ApiSubscriptionResp> items;

    @JsonProperty("page")
    private int page;

    @JsonProperty("size")
    private int size;

    @JsonProperty("total")
    private long total;

    public ApiSubscriptionPageResp() {
    }

    public ApiSubscriptionPageResp(List<ApiSubscriptionResp> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<ApiSubscriptionResp> getItems() {
        return items;
    }

    public void setItems(List<ApiSubscriptionResp> items) {
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
