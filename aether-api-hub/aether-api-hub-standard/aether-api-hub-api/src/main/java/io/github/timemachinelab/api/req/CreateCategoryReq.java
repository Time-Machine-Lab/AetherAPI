package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.api.error.CatalogErrorCodes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分类请求 DTO。
 */
public class CreateCategoryReq {

    @NotBlank(message = "Category code must not be blank")
    @Size(min = 1, max = 64, message = "Category code must be 1-64 characters")
    @JsonProperty("categoryCode")
    private String categoryCode;

    @NotBlank(message = "Category name must not be blank")
    @Size(min = 1, max = 128, message = "Category name must be 1-128 characters")
    @JsonProperty("categoryName")
    private String categoryName;

    public CreateCategoryReq() {
    }

    public CreateCategoryReq(String categoryCode, String categoryName) {
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
