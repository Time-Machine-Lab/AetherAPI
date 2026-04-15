package io.github.timemachinelab.domain.catalog.model;

/**
 * 分类启用状态枚举。
 */
public enum CategoryStatus {

    /**
     * 可用状态：分类可被新的 API 资产引用。
     */
    ENABLED,

    /**
     * 停用状态：分类不得被新资产引用，但不影响已关联资产的状态。
     */
    DISABLED
}
