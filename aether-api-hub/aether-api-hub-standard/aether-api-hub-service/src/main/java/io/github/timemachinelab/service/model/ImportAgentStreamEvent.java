package io.github.timemachinelab.service.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured event emitted by the import-agent stream.
 */
public final class ImportAgentStreamEvent {

    private final ImportAgentStreamEventType type;
    private final Object payload;

    private ImportAgentStreamEvent(ImportAgentStreamEventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public static ImportAgentStreamEvent status(String phase, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("phase", phase);
        payload.put("message", message);
        return new ImportAgentStreamEvent(ImportAgentStreamEventType.STATUS, payload);
    }

    public static ImportAgentStreamEvent thinking(
            String stage,
            String title,
            String summary,
            String detail,
            long sequence) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stage", sanitizeRequired(stage));
        payload.put("title", sanitizeRequired(title));
        payload.put("summary", sanitizeRequired(summary));
        if (detail != null && !detail.isBlank()) {
            payload.put("detail", sanitize(detail));
        }
        payload.put("sequence", sequence);
        return new ImportAgentStreamEvent(ImportAgentStreamEventType.THINKING, payload);
    }

    public static ImportAgentStreamEvent message(ImportAgentActorType actorType, String delta) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("actorType", actorType == null ? ImportAgentActorType.AGENT.name() : actorType.name());
        payload.put("delta", delta == null ? "" : delta);
        return new ImportAgentStreamEvent(ImportAgentStreamEventType.MESSAGE, payload);
    }

    public static ImportAgentStreamEvent session(Object session) {
        return new ImportAgentStreamEvent(ImportAgentStreamEventType.SESSION, session);
    }

    public static ImportAgentStreamEvent error(String code, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        return new ImportAgentStreamEvent(ImportAgentStreamEventType.ERROR, payload);
    }

    public static ImportAgentStreamEvent done(String phase) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("phase", phase);
        return new ImportAgentStreamEvent(ImportAgentStreamEventType.DONE, payload);
    }

    public ImportAgentStreamEventType getType() {
        return type;
    }

    public String getEventName() {
        return type.getEventName();
    }

    public Object getPayload() {
        return payload;
    }

    private static String sanitizeRequired(String value) {
        String sanitized = sanitize(value);
        return sanitized == null || sanitized.isBlank() ? "处理中" : sanitized;
    }

    private static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value
                .replaceAll("(?i)Bearer\\s+[A-Za-z0-9._~+/=-]+", "Bearer [hidden]")
                .replaceAll("(?i)(api[-_ ]?key|token|secret|password)\\s*[:=]\\s*[^\\s,;{}]+", "$1=[hidden]")
                .replaceAll("(?i)(Authorization\\s*:\\s*)[^\\n,;{}]+", "$1[hidden]");
        return sanitized.length() <= 500 ? sanitized : sanitized.substring(0, 500);
    }
}
