package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Discovery publisher summary response.
 */
public class CatalogDiscoveryPublisherResp {

    @JsonProperty("displayName")
    private String displayName;

    public CatalogDiscoveryPublisherResp() {
    }

    public CatalogDiscoveryPublisherResp(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
