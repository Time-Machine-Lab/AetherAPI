package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ImportAgentExampleNormalizer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern CURL_DATA_PATTERN = Pattern.compile(
            "(?is)(?:--data(?:-raw|-binary|-urlencode)?|-d)\\s+(['\"])(.*?)\\1");

    private ImportAgentExampleNormalizer() {
    }

    static String normalizeRequestBodyExample(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (!looksLikeCurl(trimmed)) {
            return compactJsonObjectOrOriginal(trimmed);
        }
        String data = extractCurlData(trimmed);
        if (data == null || data.isBlank()) {
            return null;
        }
        return compactJsonObjectOrOriginal(data.trim());
    }

    static String normalizeJsonObjectExample(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return compactJsonObjectOrOriginal(value.trim());
    }

    private static boolean looksLikeCurl(String value) {
        return value.regionMatches(true, 0, "curl ", 0, 5)
                || value.toLowerCase(java.util.Locale.ROOT).contains("\ncurl ");
    }

    private static String extractCurlData(String value) {
        Matcher matcher = CURL_DATA_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return unescapeQuoted(matcher.group(2), matcher.group(1).charAt(0));
    }

    private static String unescapeQuoted(String value, char quote) {
        if (quote == '"') {
            return value
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return value.replace("'\\''", "'");
    }

    private static String compactJsonObjectOrOriginal(String value) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(value);
            if (node.isObject()) {
                return OBJECT_MAPPER.writeValueAsString(node);
            }
        } catch (Exception ignored) {
            return value;
        }
        return value;
    }
}
