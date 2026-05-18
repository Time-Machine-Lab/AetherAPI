package io.github.timemachinelab.service.model;

/**
 * Import category plan model.
 */
public class ImportCategoryPlanModel {

    private final String categoryCode;
    private final String categoryName;
    private final ImportCategoryPlanAction action;

    public ImportCategoryPlanModel(String categoryCode, String categoryName, ImportCategoryPlanAction action) {
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

    public ImportCategoryPlanAction getAction() {
        return action;
    }
}