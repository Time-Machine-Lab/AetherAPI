package io.github.timemachinelab.service.model;

/**
 * 分类业务模型，服务层内部使用。
 */
public class CategoryModel {

    private final String id;
    private final String code;
    private final String name;
    private final String status;
    private final String createdAt;
    private final String updatedAt;

    public CategoryModel(String id, String code, String name, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
