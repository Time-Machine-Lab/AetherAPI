package io.github.timemachinelab.service.model;

import java.util.List;

/**
 * Discovery list model.
 */
public class CatalogDiscoveryListModel {

    private final List<CatalogDiscoveryAssetSummaryModel> items;

    public CatalogDiscoveryListModel(List<CatalogDiscoveryAssetSummaryModel> items) {
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public List<CatalogDiscoveryAssetSummaryModel> getItems() {
        return items;
    }
}
