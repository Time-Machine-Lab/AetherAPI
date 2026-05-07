package io.github.timemachinelab.service.model;

/**
 * API asset proxy binding application model.
 */
public class AssetProxyBindingModel {

    private final String apiCode;
    private final String proxyProfileId;
    private final String proxyProfileCode;
    private final String proxyProfileName;

    public AssetProxyBindingModel(String apiCode, String proxyProfileId, String proxyProfileCode, String proxyProfileName) {
        this.apiCode = apiCode;
        this.proxyProfileId = proxyProfileId;
        this.proxyProfileCode = proxyProfileCode;
        this.proxyProfileName = proxyProfileName;
    }

    public String getApiCode() { return apiCode; }
    public String getProxyProfileId() { return proxyProfileId; }
    public String getProxyProfileCode() { return proxyProfileCode; }
    public String getProxyProfileName() { return proxyProfileName; }
}
