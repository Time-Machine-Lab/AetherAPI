package io.github.timemachinelab.domain.catalog.model;

import java.util.Locale;

/**
 * 上游鉴权方案。
 */
public enum AuthScheme {
    NONE,
    HEADER_TOKEN,
    QUERY_TOKEN;

    public static AuthScheme fromToken(String value) {
        String normalized = normalizeToken(value);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "NO_AUTH", "NOAUTH" -> NONE;
            case "BEARER", "BEARER_TOKEN", "HEADER", "HEADER_BEARER", "HEADER_AUTH" -> HEADER_TOKEN;
            case "QUERY", "QUERY_PARAM", "QUERY_PARAMS", "QUERY_PARAMETER", "QUERYSTRING_TOKEN" -> QUERY_TOKEN;
            default -> AuthScheme.valueOf(normalized);
        };
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}

