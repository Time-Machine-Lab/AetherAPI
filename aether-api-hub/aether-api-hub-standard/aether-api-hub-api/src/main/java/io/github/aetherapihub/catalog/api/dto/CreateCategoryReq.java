package io.github.aetherapihub.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建分类请求 DTO。
 */
@Data
public class CreateCategoryReq {

    @NotBlank(message = "分类编码不能为空")
    @Size(max = 64, message = "分类编码长度不能超过 64")
    private String categoryCode;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 128, message = "分类名称长度不能超过 128")
    private String categoryName;
}
