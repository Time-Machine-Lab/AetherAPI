package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Discovery example snapshot response.
 */
public class CatalogDiscoveryExampleSnapshotResp {

    @JsonProperty("requestExample")
    private String requestExample;

    @JsonProperty("responseExample")
    private String responseExample;

    public CatalogDiscoveryExampleSnapshotResp() {
    }

    public CatalogDiscoveryExampleSnapshotResp(String requestExample, String responseExample) {
        this.requestExample = requestExample;
        this.responseExample = responseExample;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public void setRequestExample(String requestExample) {
        this.requestExample = requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }
}
