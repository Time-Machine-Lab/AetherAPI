package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Management asset list request.
 */
public class ListApiAssetReq {

    @Pattern(
            regexp = "^$|DRAFT|ENABLED|DISABLED$",
            message = "Status must be one of DRAFT, ENABLED, DISABLED")
    private String status;

    @Size(max = 64, message = "Category code must be less than or equal to 64 characters")
    private String categoryCode;

    @Size(max = 128, message = "Keyword must be less than or equal to 128 characters")
    private String keyword;

    @Min(value = 1, message = "Page must be greater than or equal to 1")
    private int page = 1;

    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 100, message = "Size must be less than or equal to 100")
    private int size = 20;

    public ListApiAssetReq() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
