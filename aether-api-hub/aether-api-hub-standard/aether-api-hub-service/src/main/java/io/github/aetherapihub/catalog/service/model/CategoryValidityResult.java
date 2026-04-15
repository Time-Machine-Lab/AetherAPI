package io.github.aetherapihub.catalog.service.model;

/**
 * 分类有效性校验结果。
 */
public class CategoryValidityResult {

    private final String categoryCode;
    private final boolean valid;
    private final String reason;

    private CategoryValidityResult(String categoryCode, boolean valid, String reason) {
        this.categoryCode = categoryCode;
        this.valid = valid;
        this.reason = reason;
    }

    public static CategoryValidityResult valid(String categoryCode) {
        return new CategoryValidityResult(categoryCode, true, null);
    }

    public static CategoryValidityResult invalid(String categoryCode, String reason) {
        return new CategoryValidityResult(categoryCode, false, reason);
    }

    public String getCategoryCode() { return categoryCode; }
    public boolean isValid() { return valid; }
    public String getReason() { return reason; }
}
