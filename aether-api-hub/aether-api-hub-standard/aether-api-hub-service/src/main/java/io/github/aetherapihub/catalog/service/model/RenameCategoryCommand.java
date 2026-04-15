package io.github.aetherapihub.catalog.service.model;

/**
 * 重命名分类命令。
 */
public class RenameCategoryCommand {

    private final String categoryCode;
    private final String newName;

    public RenameCategoryCommand(String categoryCode, String newName) {
        this.categoryCode = categoryCode;
        this.newName = newName;
    }

    public String getCategoryCode() { return categoryCode; }
    public String getNewName() { return newName; }
}
