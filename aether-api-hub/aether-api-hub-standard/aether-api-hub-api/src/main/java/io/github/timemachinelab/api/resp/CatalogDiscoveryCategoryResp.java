package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Discovery category response.
 */
public class CatalogDiscoveryCategoryResp {

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("categoryName")
    private String categoryName;

    public CatalogDiscoveryCategoryResp() {
    }

    public CatalogDiscoveryCategoryResp(String categoryCode, String categoryName) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
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
}
