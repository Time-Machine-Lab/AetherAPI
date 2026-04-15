package io.github.timemachinelab.domain.catalog.model;

/**
 * 分类有效性校验依赖。
 */
public interface CategoryValidityChecker {

    boolean isValid(CategoryRef categoryRef);
}

