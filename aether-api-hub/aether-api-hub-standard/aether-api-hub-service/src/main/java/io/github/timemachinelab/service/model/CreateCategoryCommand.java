package io.github.timemachinelab.service.model;

/**
 * 创建分类命令。
 */
public class CreateCategoryCommand {

    private final String categoryCode;
    private final String categoryName;

    public CreateCategoryCommand(String categoryCode, String categoryName) {
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
