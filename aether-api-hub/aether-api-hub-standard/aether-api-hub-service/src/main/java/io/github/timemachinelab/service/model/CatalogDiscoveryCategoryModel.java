package io.github.timemachinelab.service.model;

/**
 * Discovery category summary model.
 */
public class CatalogDiscoveryCategoryModel {

    private final String categoryCode;
    private final String categoryName;

    public CatalogDiscoveryCategoryModel(String categoryCode, String categoryName) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
