package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timemachinelab.domain.catalog.model.AsyncTaskAuthMode;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import jakarta.validation.constraints.Size;

/**
 * Async task query configuration request.
 */
public class AsyncTaskConfigReq {

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("queryMethod")
    private RequestMethod queryMethod;

    @Size(max = 512, message = "Async task query URL template must not exceed 512 characters")
    @JsonProperty("queryUrlTemplate")
    private String queryUrlTemplate;

    @JsonProperty("authMode")
    private AsyncTaskAuthMode authMode;

    @JsonProperty("authScheme")
    private AuthScheme authScheme;

    @JsonProperty("authConfig")
    private String authConfig;

    @JsonProperty("statusPath")
    private String statusPath;

    @JsonProperty("resultPath")
    private String resultPath;

    @JsonProperty("errorPath")
    private String errorPath;

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

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(String statusPath) {
        this.statusPath = statusPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getErrorPath() {
        return errorPath;
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }
}
