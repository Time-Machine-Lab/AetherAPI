package io.github.timemachinelab.api.resp;

/**
 * Import category plan response.
 */
public class ImportCategoryPlanResp {

    private final String categoryCode;
    private final String categoryName;
    private final String action;

    public ImportCategoryPlanResp(String categoryCode, String categoryName, String action) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.action = action;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getAction() {
        return action;
    }
}