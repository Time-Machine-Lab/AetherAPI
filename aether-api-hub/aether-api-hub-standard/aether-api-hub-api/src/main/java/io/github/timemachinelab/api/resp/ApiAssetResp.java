package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;

/**
 * API asset response.
 */
public class ApiAssetResp {

    @JsonProperty("id")
    private String id;

    @JsonProperty("apiCode")
    private String apiCode;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("status")
    private AssetStatus status;

    @JsonProperty("publisherDisplayName")
    private String publisherDisplayName;

    @JsonProperty("publishedAt")
    private String publishedAt;

    @JsonProperty("requestMethod")
    private RequestMethod requestMethod;

    @JsonProperty("upstreamUrl")
    private String upstreamUrl;

    @JsonProperty("authScheme")
    private AuthScheme authScheme;

    @JsonProperty("authConfig")
    private String authConfig;

    @JsonProperty("requestTemplate")
    private String requestTemplate;

    @JsonProperty("requestExample")
    private String requestExample;

    @JsonProperty("responseExample")
    private String responseExample;

    @JsonProperty("aiCapabilityProfile")
    private AiCapabilityProfileResp aiCapabilityProfile;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public ApiAssetResp() {
    }

    public ApiAssetResp(
            String id,
            String apiCode,
            String assetName,
            AssetType assetType,
            String categoryCode,
            AssetStatus status,
            String publisherDisplayName,
            String publishedAt,
            RequestMethod requestMethod,
            String upstreamUrl,
            AuthScheme authScheme,
            String authConfig,
            String requestTemplate,
            String requestExample,
            String responseExample,
            AiCapabilityProfileResp aiCapabilityProfile,
            boolean deleted,
            String createdAt,
            String updatedAt) {
        this.id = id;
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.status = status;
        this.publisherDisplayName = publisherDisplayName;
        this.publishedAt = publishedAt;
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.requestTemplate = requestTemplate;
        this.requestExample = requestExample;
        this.responseExample = responseExample;
        this.aiCapabilityProfile = aiCapabilityProfile;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public void setPublisherDisplayName(String publisherDisplayName) {
        this.publisherDisplayName = publisherDisplayName;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public void setUpstreamUrl(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(AuthScheme authScheme) {
        this.authScheme = authScheme;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
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

    public AiCapabilityProfileResp getAiCapabilityProfile() {
        return aiCapabilityProfile;
    }

    public void setAiCapabilityProfile(AiCapabilityProfileResp aiCapabilityProfile) {
        this.aiCapabilityProfile = aiCapabilityProfile;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
