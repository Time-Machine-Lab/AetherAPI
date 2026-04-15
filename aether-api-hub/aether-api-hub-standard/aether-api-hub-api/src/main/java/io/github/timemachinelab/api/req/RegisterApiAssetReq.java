package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注册 API 资产请求。
 */
public class RegisterApiAssetReq {

    @NotBlank(message = "API code must not be blank")
    @Size(min = 1, max = 64, message = "API code must be 1-64 characters")
    @JsonProperty("apiCode")
    private String apiCode;

    @NotNull(message = "Asset type must not be null")
    @JsonProperty("assetType")
    private AssetType assetType;

    @Size(min = 1, max = 128, message = "Asset name must be 1-128 characters")
    @JsonProperty("assetName")
    private String assetName;

    public RegisterApiAssetReq() {
    }

    public RegisterApiAssetReq(String apiCode, AssetType assetType, String assetName) {
        this.apiCode = apiCode;
        this.assetType = assetType;
        this.assetName = assetName;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}

