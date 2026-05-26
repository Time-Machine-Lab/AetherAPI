package io.github.timemachinelab.service.model;

/**
 * Application-layer async task query configuration model.
 */
public class AsyncTaskConfigModel {

    private final Boolean enabled;
    private final String queryMethod;
    private final String queryUrlTemplate;
    private final String authMode;
    private final String authScheme;
    private final String authConfig;
    private final String queryResponseJsonSchema;

    public AsyncTaskConfigModel(
            Boolean enabled,
            String queryMethod,
            String queryUrlTemplate,
            String authMode,
            String authScheme,
            String authConfig,
            String queryResponseJsonSchema) {
        this.enabled = enabled;
        this.queryMethod = queryMethod;
        this.queryUrlTemplate = queryUrlTemplate;
        this.authMode = authMode;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.queryResponseJsonSchema = queryResponseJsonSchema;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getQueryMethod() {
        return queryMethod;
    }

    public String getQueryUrlTemplate() {
        return queryUrlTemplate;
    }

    public String getAuthMode() {
        return authMode;
    }

    public String getAuthScheme() {
        return authScheme;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public String getQueryResponseJsonSchema() {
        return queryResponseJsonSchema;
    }
}
