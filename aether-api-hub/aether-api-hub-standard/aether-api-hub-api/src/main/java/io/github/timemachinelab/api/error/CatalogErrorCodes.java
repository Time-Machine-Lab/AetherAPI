package io.github.timemachinelab.api.error;

/**
 * Catalog 模块错误码常量集中定义。
 */
public final class CatalogErrorCodes {

    private CatalogErrorCodes() {
    }

    // 分类相关错误码
    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    public static final String CATEGORY_CODE_ALREADY_EXISTS = "CATEGORY_CODE_ALREADY_EXISTS";
    public static final String CATEGORY_CODE_INVALID = "CATEGORY_CODE_INVALID";
    public static final String CATEGORY_NAME_INVALID = "CATEGORY_NAME_INVALID";
    public static final String CATEGORY_ALREADY_ENABLED = "CATEGORY_ALREADY_ENABLED";
    public static final String CATEGORY_ALREADY_DISABLED = "CATEGORY_ALREADY_DISABLED";
    public static final String CATEGORY_DELETED = "CATEGORY_DELETED";
    public static final String CATEGORY_INVALID = "CATEGORY_INVALID";
}
