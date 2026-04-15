package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重命名分类请求 DTO。
 */
public class RenameCategoryReq {

    @NotBlank(message = "Category name must not be blank")
    @Size(min = 1, max = 128, message = "Category name must be 1-128 characters")
    @JsonProperty("categoryName")
    private String categoryName;

    public RenameCategoryReq() {
    }

    public RenameCategoryReq(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
