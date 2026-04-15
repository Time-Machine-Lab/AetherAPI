package io.github.timemachinelab.service.model;

/**
 * Discovery example snapshot model.
 */
public class CatalogDiscoveryExampleSnapshotModel {

    private final String requestExample;
    private final String responseExample;

    public CatalogDiscoveryExampleSnapshotModel(String requestExample, String responseExample) {
        this.requestExample = requestExample;
        this.responseExample = responseExample;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }
}
