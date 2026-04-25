package io.github.timemachinelab.api.error;

/**
 * Catalog error code constants.
 */
public final class CatalogErrorCodes {

    private CatalogErrorCodes() {
    }

    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    public static final String CATEGORY_CODE_ALREADY_EXISTS = "CATEGORY_CODE_ALREADY_EXISTS";
    public static final String CATEGORY_CODE_INVALID = "CATEGORY_CODE_INVALID";
    public static final String CATEGORY_NAME_INVALID = "CATEGORY_NAME_INVALID";
    public static final String CATEGORY_ALREADY_ENABLED = "CATEGORY_ALREADY_ENABLED";
    public static final String CATEGORY_ALREADY_DISABLED = "CATEGORY_ALREADY_DISABLED";
    public static final String CATEGORY_DELETED = "CATEGORY_DELETED";
    public static final String CATEGORY_INVALID = "CATEGORY_INVALID";

    public static final String ASSET_NOT_FOUND = "ASSET_NOT_FOUND";
    public static final String API_CODE_ALREADY_EXISTS = "API_CODE_ALREADY_EXISTS";
    public static final String API_CODE_INVALID = "API_CODE_INVALID";
    public static final String ASSET_INVALID_QUERY = "ASSET_INVALID_QUERY";
    public static final String ASSET_ALREADY_PUBLISHED = "ASSET_ALREADY_PUBLISHED";
    public static final String ASSET_ALREADY_UNPUBLISHED = "ASSET_ALREADY_UNPUBLISHED";
    public static final String ASSET_PUBLISH_INCOMPLETE = "ASSET_PUBLISH_INCOMPLETE";
    public static final String ASSET_CATEGORY_INVALID = "ASSET_CATEGORY_INVALID";
    public static final String AI_PROFILE_REQUIRED = "AI_PROFILE_REQUIRED";
    public static final String AI_PROFILE_UNSUPPORTED = "AI_PROFILE_UNSUPPORTED";
}
