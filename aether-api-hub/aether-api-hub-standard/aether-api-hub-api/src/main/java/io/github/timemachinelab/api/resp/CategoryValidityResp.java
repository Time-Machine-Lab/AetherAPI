package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分类有效性校验响应 DTO。
 */
public class CategoryValidityResp {

    @JsonProperty("categoryCode")
    private String categoryCode;

    @JsonProperty("valid")
    private boolean valid;

    @JsonProperty("reason")
    private String reason;

    public CategoryValidityResp() {
    }

    public CategoryValidityResp(String categoryCode, boolean valid, String reason) {
        this.categoryCode = categoryCode;
        this.valid = valid;
        this.reason = reason;
    }

    public static CategoryValidityResp valid(String categoryCode) {
        return new CategoryValidityResp(categoryCode, true, null);
    }

    public static CategoryValidityResp invalid(String categoryCode, String reason) {
        return new CategoryValidityResp(categoryCode, false, reason);
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
