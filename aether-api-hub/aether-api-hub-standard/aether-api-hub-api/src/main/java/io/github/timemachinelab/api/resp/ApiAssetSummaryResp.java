package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AssetStatus;
import io.github.timemachinelab.domain.catalog.model.AssetType;

/**
 * Asset management summary response.
 */
public class ApiAssetSummaryResp {

    @JsonProperty("apiCode")
    private String apiCode;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("status")
    private AssetStatus status;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public ApiAssetSummaryResp() {
    }

    public ApiAssetSummaryResp(
            String apiCode,
            String assetName,
            AssetType assetType,
            String categoryCode,
            String categoryName,
            AssetStatus status,
            String updatedAt) {
        this.apiCode = apiCode;
        this.assetName = assetName;
        this.assetType = assetType;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.status = status;
        this.updatedAt = updatedAt;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
