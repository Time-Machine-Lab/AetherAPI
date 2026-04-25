package io.github.timemachinelab.service.model;

/**
 * Discovery publisher summary model.
 */
public class CatalogDiscoveryPublisherModel {

    private final String displayName;

    public CatalogDiscoveryPublisherModel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
