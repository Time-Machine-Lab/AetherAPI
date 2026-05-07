package io.github.timemachinelab.api.resp;

/**
 * API asset proxy binding response.
 */
public class AssetProxyBindingResp {

    private final String apiCode;
    private final String proxyProfileId;
    private final String proxyProfileCode;
    private final String proxyProfileName;

    public AssetProxyBindingResp(String apiCode, String proxyProfileId, String proxyProfileCode, String proxyProfileName) {
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
