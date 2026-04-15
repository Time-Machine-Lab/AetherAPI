package io.github.aetherapihub.catalog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 分类响应 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResp {

    private String id;
    private String categoryCode;
    private String categoryName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
