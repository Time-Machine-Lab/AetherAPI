package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import jakarta.validation.constraints.Size;

/**
 * 修订 API 资产请求。
 */
public class ReviseApiAssetReq {

    @Size(min = 1, max = 128, message = "Asset name must be 1-128 characters")
    @JsonProperty("assetName")
    private String assetName;
    private boolean assetNameSet;

    @JsonProperty("assetType")
    private AssetType assetType;
    private boolean assetTypeSet;

    @Size(min = 1, max = 64, message = "Category code must be 1-64 characters")
    @JsonProperty("categoryCode")
    private String categoryCode;
    private boolean categoryCodeSet;

    @JsonProperty("requestMethod")
    private RequestMethod requestMethod;
    private boolean requestMethodSet;

    @Size(max = 512, message = "Upstream URL must not exceed 512 characters")
    @JsonProperty("upstreamUrl")
    private String upstreamUrl;
    private boolean upstreamUrlSet;

    @JsonProperty("authScheme")
    private AuthScheme authScheme;
    private boolean authSchemeSet;

    @JsonProperty("authConfig")
    private String authConfig;
    private boolean authConfigSet;

    @JsonProperty("requestTemplate")
    private String requestTemplate;
    private boolean requestTemplateSet;

    @JsonProperty("requestExample")
    private String requestExample;
    private boolean requestExampleSet;

    @JsonProperty("responseExample")
    private String responseExample;
    private boolean responseExampleSet;

    @JsonSetter("assetName")
    public void setAssetName(String assetName) {
        this.assetName = assetName;
        this.assetNameSet = true;
    }

    @JsonSetter("assetType")
    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
        this.assetTypeSet = true;
    }

    @JsonSetter("categoryCode")
    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
        this.categoryCodeSet = true;
    }

    @JsonSetter("requestMethod")
    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        this.requestMethodSet = true;
    }

    @JsonSetter("upstreamUrl")
    public void setUpstreamUrl(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
        this.upstreamUrlSet = true;
    }

    @JsonSetter("authScheme")
    public void setAuthScheme(AuthScheme authScheme) {
        this.authScheme = authScheme;
        this.authSchemeSet = true;
    }

    @JsonSetter("authConfig")
    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
        this.authConfigSet = true;
    }

    @JsonSetter("requestTemplate")
    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
        this.requestTemplateSet = true;
    }

    @JsonSetter("requestExample")
    public void setRequestExample(String requestExample) {
        this.requestExample = requestExample;
        this.requestExampleSet = true;
    }

    @JsonSetter("responseExample")
    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
        this.responseExampleSet = true;
    }

    public String getAssetName() {
        return assetName;
    }

    public boolean isAssetNameSet() {
        return assetNameSet;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public boolean isAssetTypeSet() {
        return assetTypeSet;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public boolean isCategoryCodeSet() {
        return categoryCodeSet;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public boolean isRequestMethodSet() {
        return requestMethodSet;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public boolean isUpstreamUrlSet() {
        return upstreamUrlSet;
    }

    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    public boolean isAuthSchemeSet() {
        return authSchemeSet;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public boolean isAuthConfigSet() {
        return authConfigSet;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public boolean isRequestTemplateSet() {
        return requestTemplateSet;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public boolean isRequestExampleSet() {
        return requestExampleSet;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public boolean isResponseExampleSet() {
        return responseExampleSet;
    }
}

