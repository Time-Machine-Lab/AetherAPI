package io.github.aetherapihub.catalog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类分页响应 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPageResp {

    private List<CategoryResp> items;
    private Integer page;
    private Integer size;
    private Long total;
}
