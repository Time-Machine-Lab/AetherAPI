package io.github.aetherapihub.catalog.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.aetherapihub.catalog.domain.model.CategoryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 分类持久化对象（DO），对应 api_category 表。
 */
@Data
@NoArgsConstructor
@TableName("api_category")
public class ApiCategoryDo {

    @TableId
    private String id;

    @TableField("category_code")
    private String categoryCode;

    @TableField("category_name")
    private String categoryName;

    @TableField("status")
    private String status;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;

    @Version
    @TableField("version")
    private Long version;

    public CategoryStatus getStatusEnum() {
        return status == null ? CategoryStatus.ENABLED : CategoryStatus.valueOf(status);
    }
}
