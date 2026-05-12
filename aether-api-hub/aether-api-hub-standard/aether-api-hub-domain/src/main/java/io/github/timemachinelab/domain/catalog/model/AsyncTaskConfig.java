package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;

/**
 * Optional async task query configuration owned by an API asset.
 */
public final class AsyncTaskConfig {

    public static final String TASK_ID_PLACEHOLDER = "{taskId}";

    private final boolean enabled;
    private final RequestMethod queryMethod;
    private final String queryUrlTemplate;
    private final AsyncTaskAuthMode authMode;
    private final AuthScheme authScheme;
    private final String authConfig;
    private final String statusPath;
    private final String resultPath;
    private final String errorPath;

    private AsyncTaskConfig(
            boolean enabled,
            RequestMethod queryMethod,
            String queryUrlTemplate,
            AsyncTaskAuthMode authMode,
            AuthScheme authScheme,
            String authConfig,
            String statusPath,
            String resultPath,
            String errorPath) {
        this.enabled = enabled;
        this.queryMethod = queryMethod;
        this.queryUrlTemplate = normalize(queryUrlTemplate);
        this.authMode = authMode == null ? AsyncTaskAuthMode.SAME_AS_SUBMIT : authMode;
        this.authScheme = authScheme;
        this.authConfig = normalize(authConfig);
        this.statusPath = normalize(statusPath);
        this.resultPath = normalize(resultPath);
        this.errorPath = normalize(errorPath);
        validate();
    }

    public static AsyncTaskConfig of(
            Boolean enabled,
            RequestMethod queryMethod,
            String queryUrlTemplate,
            AsyncTaskAuthMode authMode,
            AuthScheme authScheme,
            String authConfig,
            String statusPath,
            String resultPath,
            String errorPath) {
        if (enabled == null
                && queryMethod == null
                && normalize(queryUrlTemplate) == null
                && authMode == null
                && authScheme == null
                && normalize(authConfig) == null
                && normalize(statusPath) == null
                && normalize(resultPath) == null
                && normalize(errorPath) == null) {
            return null;
        }
        return new AsyncTaskConfig(
                enabled == null || enabled,
                queryMethod,
                queryUrlTemplate,
                authMode,
                authScheme,
                authConfig,
                statusPath,
                resultPath,
                errorPath
        );
    }

    public boolean isCompleteForQuery() {
        return enabled
                && queryMethod != null
                && queryUrlTemplate != null
                && queryUrlTemplate.contains(TASK_ID_PLACEHOLDER)
                && isAuthComplete();
    }

    public boolean hasCriticalDifference(AsyncTaskConfig other) {
        return !Objects.equals(this, other);
    }

    private void validate() {
        if (!enabled) {
            return;
        }
        if (queryMethod == null) {
            throw new AssetDomainException("Async task query method must be provided when async task query is enabled");
        }
        if (queryUrlTemplate == null) {
            throw new AssetDomainException("Async task query URL template must be provided when async task query is enabled");
        }
        if (!queryUrlTemplate.contains(TASK_ID_PLACEHOLDER)) {
            throw new AssetDomainException("Async task query URL template must contain {taskId}");
        }
        if (!isAuthComplete()) {
            throw new AssetDomainException("Async task query auth override is incomplete");
        }
    }

    private boolean isAuthComplete() {
        if (authMode != AsyncTaskAuthMode.OVERRIDE) {
            return true;
        }
        if (authScheme == null) {
            return false;
        }
        return authScheme == AuthScheme.NONE || authConfig != null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public RequestMethod getQueryMethod() {
        return queryMethod;
    }

    public String getQueryUrlTemplate() {
        return queryUrlTemplate;
    }

    public AsyncTaskAuthMode getAuthMode() {
        return authMode;
    }

    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public String getErrorPath() {
        return errorPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AsyncTaskConfig that = (AsyncTaskConfig) o;
        return enabled == that.enabled
                && queryMethod == that.queryMethod
                && Objects.equals(queryUrlTemplate, that.queryUrlTemplate)
                && authMode == that.authMode
                && authScheme == that.authScheme
                && Objects.equals(authConfig, that.authConfig)
                && Objects.equals(statusPath, that.statusPath)
                && Objects.equals(resultPath, that.resultPath)
                && Objects.equals(errorPath, that.errorPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, queryMethod, queryUrlTemplate, authMode, authScheme, authConfig, statusPath, resultPath, errorPath);
    }
}
