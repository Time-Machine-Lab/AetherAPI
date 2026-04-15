package io.github.timemachinelab.service.model;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;

/**
 * 修订 API 资产命令。
 */
public class ReviseApiAssetCommand {

    private final String apiCode;
    private final String assetName;
    private final boolean assetNameSet;
    private final AssetType assetType;
    private final boolean assetTypeSet;
    private final String categoryCode;
    private final boolean categoryCodeSet;
    private final RequestMethod requestMethod;
    private final boolean requestMethodSet;
    private final String upstreamUrl;
    private final boolean upstreamUrlSet;
    private final AuthScheme authScheme;
    private final boolean authSchemeSet;
    private final String authConfig;
    private final boolean authConfigSet;
    private final String requestTemplate;
    private final boolean requestTemplateSet;
    private final String requestExample;
    private final boolean requestExampleSet;
    private final String responseExample;
    private final boolean responseExampleSet;

    public ReviseApiAssetCommand(
            String apiCode,
            String assetName,
            boolean assetNameSet,
            AssetType assetType,
            boolean assetTypeSet,
            String categoryCode,
            boolean categoryCodeSet,
            RequestMethod requestMethod,
            boolean requestMethodSet,
            String upstreamUrl,
            boolean upstreamUrlSet,
            AuthScheme authScheme,
            boolean authSchemeSet,
            String authConfig,
            boolean authConfigSet,
            String requestTemplate,
            boolean requestTemplateSet,
            String requestExample,
            boolean requestExampleSet,
            String responseExample,
            boolean responseExampleSet) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetNameSet = assetNameSet;
        this.assetType = assetType;
        this.assetTypeSet = assetTypeSet;
        this.categoryCode = categoryCode;
        this.categoryCodeSet = categoryCodeSet;
        this.requestMethod = requestMethod;
        this.requestMethodSet = requestMethodSet;
        this.upstreamUrl = upstreamUrl;
        this.upstreamUrlSet = upstreamUrlSet;
        this.authScheme = authScheme;
        this.authSchemeSet = authSchemeSet;
        this.authConfig = authConfig;
        this.authConfigSet = authConfigSet;
        this.requestTemplate = requestTemplate;
        this.requestTemplateSet = requestTemplateSet;
        this.requestExample = requestExample;
        this.requestExampleSet = requestExampleSet;
        this.responseExample = responseExample;
        this.responseExampleSet = responseExampleSet;
    }

    public String getApiCode() {
        return apiCode;
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

