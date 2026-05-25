package io.github.timemachinelab.domain.catalog.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 发送到上游的固定非鉴权请求头。
 */
public final class UpstreamRequestHeader {

    private static final Pattern HEADER_NAME_PATTERN = Pattern.compile("[!#$%&'*+.^_`|~0-9A-Za-z-]+");
    private static final Set<String> PROTECTED_HEADER_NAMES = Set.of(
            "authorization",
            "content-type",
            "host",
            "content-length",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

    private final String name;
    private final String value;

    private UpstreamRequestHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static UpstreamRequestHeader of(String name, String value) {
        String normalizedName = normalize(name);
        String normalizedValue = normalize(value);
        if (normalizedName == null) {
            throw new AssetDomainException("上游请求头名称不能为空");
        }
        if (!HEADER_NAME_PATTERN.matcher(normalizedName).matches()) {
            throw new AssetDomainException("上游请求头名称不合法: " + normalizedName);
        }
        if (isProtectedName(normalizedName)) {
            throw new AssetDomainException("上游请求头名称为保留名称: " + normalizedName);
        }
        if (normalizedValue == null) {
            throw new AssetDomainException("上游请求头值不能为空");
        }
        if (normalizedValue.indexOf('\r') >= 0 || normalizedValue.indexOf('\n') >= 0) {
            throw new AssetDomainException("上游请求头值不能包含换行符");
        }
        return new UpstreamRequestHeader(normalizedName, normalizedValue);
    }

    public static boolean isProtectedName(String name) {
        String normalizedName = normalize(name);
        if (normalizedName == null) {
            return false;
        }
        String lowerName = normalizedName.toLowerCase(Locale.ROOT);
        return PROTECTED_HEADER_NAMES.contains(lowerName) || lowerName.startsWith("x-aether-");
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpstreamRequestHeader that = (UpstreamRequestHeader) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
