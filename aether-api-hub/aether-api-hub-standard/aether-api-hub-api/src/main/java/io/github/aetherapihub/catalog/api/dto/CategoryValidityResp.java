package io.github.aetherapihub.catalog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类有效性校验响应 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryValidityResp {

    private String categoryCode;
    private Boolean valid;
    private String reason;
}
