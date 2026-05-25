package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;
import java.util.List;

/**
 * 上游接入配置值对象。
 */
public final class UpstreamEndpointConfig {

    private final RequestMethod requestMethod;
    private final String upstreamUrl;
    private final AuthScheme authScheme;
    private final String authConfig;
    private final List<UpstreamRequestHeader> upstreamRequestHeaders;

    private UpstreamEndpointConfig(
            RequestMethod requestMethod,
            String upstreamUrl,
            AuthScheme authScheme,
            String authConfig,
            List<UpstreamRequestHeader> upstreamRequestHeaders) {
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
        this.upstreamRequestHeaders = upstreamRequestHeaders == null ? List.of() : List.copyOf(upstreamRequestHeaders);
    }

    public static UpstreamEndpointConfig of(
            RequestMethod requestMethod, String upstreamUrl, AuthScheme authScheme, String authConfig) {
        return of(requestMethod, upstreamUrl, authScheme, authConfig, null);
    }

    public static UpstreamEndpointConfig of(
            RequestMethod requestMethod,
            String upstreamUrl,
            AuthScheme authScheme,
            String authConfig,
            List<UpstreamRequestHeader> upstreamRequestHeaders) {
        String normalizedUrl = normalize(upstreamUrl);
        String normalizedAuthConfig = normalize(authConfig);
        List<UpstreamRequestHeader> normalizedHeaders = upstreamRequestHeaders == null
                ? List.of()
                : upstreamRequestHeaders.stream().filter(Objects::nonNull).toList();
        if (requestMethod == null
                && normalizedUrl == null
                && authScheme == null
                && normalizedAuthConfig == null
                && normalizedHeaders.isEmpty()) {
            return null;
        }
        return new UpstreamEndpointConfig(requestMethod, normalizedUrl, authScheme, normalizedAuthConfig, normalizedHeaders);
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public List<UpstreamRequestHeader> getUpstreamRequestHeaders() {
        return upstreamRequestHeaders;
    }

    public boolean isComplete() {
        if (requestMethod == null || upstreamUrl == null || authScheme == null) {
            return false;
        }
        if (authScheme == AuthScheme.NONE) {
            return true;
        }
        return authConfig != null;
    }

    public boolean hasCriticalDifference(UpstreamEndpointConfig other) {
        if (other == null) {
            return requestMethod != null
                    || upstreamUrl != null
                    || authScheme != null
                    || authConfig != null
                    || !upstreamRequestHeaders.isEmpty();
        }
        return requestMethod != other.requestMethod
                || !Objects.equals(upstreamUrl, other.upstreamUrl)
                || authScheme != other.authScheme
                || !Objects.equals(authConfig, other.authConfig)
                || !Objects.equals(upstreamRequestHeaders, other.upstreamRequestHeaders);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpstreamEndpointConfig that = (UpstreamEndpointConfig) o;
        return requestMethod == that.requestMethod
                && Objects.equals(upstreamUrl, that.upstreamUrl)
                && authScheme == that.authScheme
                && Objects.equals(authConfig, that.authConfig)
                && Objects.equals(upstreamRequestHeaders, that.upstreamRequestHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestMethod, upstreamUrl, authScheme, authConfig, upstreamRequestHeaders);
    }
}

