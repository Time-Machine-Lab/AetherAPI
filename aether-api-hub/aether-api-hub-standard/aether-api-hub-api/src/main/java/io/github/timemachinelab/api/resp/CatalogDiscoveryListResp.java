package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Discovery list response.
 */
public class CatalogDiscoveryListResp {

    @JsonProperty("items")
    private List<CatalogDiscoveryAssetSummaryResp> items;

    public CatalogDiscoveryListResp() {
    }

    public CatalogDiscoveryListResp(List<CatalogDiscoveryAssetSummaryResp> items) {
        this.items = items;
    }

    public List<CatalogDiscoveryAssetSummaryResp> getItems() {
        return items;
    }

    public void setItems(List<CatalogDiscoveryAssetSummaryResp> items) {
        this.items = items;
    }
}
