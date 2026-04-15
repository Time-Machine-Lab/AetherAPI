package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;

/**
 * 分类响应 DTO。
 */
public class CategoryResp {

    @JsonProperty("id")
    private String id;

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("status")
    private CategoryStatus status;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public CategoryResp() {
    }

    public CategoryResp(String id, String categoryCode, String categoryName, CategoryStatus status,
                        String createdAt, String updatedAt) {
        this.id = id;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public CategoryStatus getStatus() {
        return status;
    }

    public void setStatus(CategoryStatus status) {
        this.status = status;
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
