package io.github.timemachinelab.service.model;

/**
 * 重命名分类命令。
 */
public class RenameCategoryCommand {

    private final String categoryCode;
    private final String newCategoryName;

    public RenameCategoryCommand(String categoryCode, String newCategoryName) {
        this.categoryCode = categoryCode;
        this.newCategoryName = newCategoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getNewCategoryName() {
        return newCategoryName;
    }
}
