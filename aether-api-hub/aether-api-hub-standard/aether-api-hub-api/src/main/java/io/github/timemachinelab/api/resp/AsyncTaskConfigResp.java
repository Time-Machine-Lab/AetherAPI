package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskAuthMode;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;

/**
 * Async task query configuration response.
 */
public class AsyncTaskConfigResp {

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("queryMethod")
    private RequestMethod queryMethod;

    @JsonProperty("queryUrlTemplate")
    private String queryUrlTemplate;

    @JsonProperty("authMode")
    private AsyncTaskAuthMode authMode;

    @JsonProperty("authScheme")
    private AuthScheme authScheme;

    @JsonProperty("authConfig")
    private String authConfig;

    @JsonProperty("queryResponseJsonSchema")
    private String queryResponseJsonSchema;

    public AsyncTaskConfigResp() {
    }

    public AsyncTaskConfigResp(
            Boolean enabled,
            RequestMethod queryMethod,
            String queryUrlTemplate,
            AsyncTaskAuthMode authMode,
            AuthScheme authScheme,
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

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public RequestMethod getQueryMethod() {
        return queryMethod;
    }

    public void setQueryMethod(RequestMethod queryMethod) {
        this.queryMethod = queryMethod;
    }

    public String getQueryUrlTemplate() {
        return queryUrlTemplate;
    }

    public void setQueryUrlTemplate(String queryUrlTemplate) {
        this.queryUrlTemplate = queryUrlTemplate;
    }

    public AsyncTaskAuthMode getAuthMode() {
        return authMode;
    }

    public void setAuthMode(AsyncTaskAuthMode authMode) {
        this.authMode = authMode;
    }

    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(AuthScheme authScheme) {
        this.authScheme = authScheme;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public String getQueryResponseJsonSchema() {
        return queryResponseJsonSchema;
    }

    public void setQueryResponseJsonSchema(String queryResponseJsonSchema) {
        this.queryResponseJsonSchema = queryResponseJsonSchema;
    }
}
