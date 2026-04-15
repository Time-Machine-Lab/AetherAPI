package io.github.aetherapihub.catalog.service.port.in_;

import io.github.aetherapihub.catalog.service.model.CategoryValidityResult;

/**
 * 分类有效性查询端口（in-bound，供本模块及下游模块使用）。
 */
public interface CategoryValidityQuery {

    /**
     * 校验指定分类编码是否有效。
     * @param categoryCode 分类业务编码
     * @return 有效性结果，包含无效时的具体原因
     */
    CategoryValidityResult checkValidity(String categoryCode);
}
