package io.github.timemachinelab.domain.catalog.model;

import java.util.Objects;

/**
 * 上游接入配置值对象。
 */
public final class UpstreamEndpointConfig {

    private final RequestMethod requestMethod;
    private final String upstreamUrl;
    private final AuthScheme authScheme;
    private final String authConfig;

    private UpstreamEndpointConfig(RequestMethod requestMethod, String upstreamUrl, AuthScheme authScheme, String authConfig) {
        this.requestMethod = requestMethod;
        this.upstreamUrl = upstreamUrl;
        this.authScheme = authScheme;
        this.authConfig = authConfig;
    }

    public static UpstreamEndpointConfig of(
            RequestMethod requestMethod, String upstreamUrl, AuthScheme authScheme, String authConfig) {
        String normalizedUrl = normalize(upstreamUrl);
        String normalizedAuthConfig = normalize(authConfig);
        if (requestMethod == null && normalizedUrl == null && authScheme == null && normalizedAuthConfig == null) {
            return null;
        }
        return new UpstreamEndpointConfig(requestMethod, normalizedUrl, authScheme, normalizedAuthConfig);
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
            return requestMethod != null || upstreamUrl != null || authScheme != null || authConfig != null;
        }
        return requestMethod != other.requestMethod
                || !Objects.equals(upstreamUrl, other.upstreamUrl)
                || authScheme != other.authScheme
                || !Objects.equals(authConfig, other.authConfig);
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
                && Objects.equals(authConfig, that.authConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestMethod, upstreamUrl, authScheme, authConfig);
    }
}

