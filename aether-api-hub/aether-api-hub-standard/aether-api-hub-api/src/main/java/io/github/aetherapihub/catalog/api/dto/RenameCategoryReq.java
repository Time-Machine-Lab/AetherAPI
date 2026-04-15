package io.github.aetherapihub.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重命名分类请求 DTO。
 */
@Data
public class RenameCategoryReq {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 128, message = "分类名称长度不能超过 128")
    private String categoryName;
}
