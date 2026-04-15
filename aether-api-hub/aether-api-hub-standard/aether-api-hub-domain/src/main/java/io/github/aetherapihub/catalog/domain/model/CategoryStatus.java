package io.github.aetherapihub.catalog.domain.model;

/**
 * 分类启用状态枚举。
 */
public enum CategoryStatus {

    /**
     * 可用状态，可被新的 API 资产引用。
     */
    ENABLED,

    /**
     * 停用状态，不得被新资产引用。
     */
    DISABLED
}
