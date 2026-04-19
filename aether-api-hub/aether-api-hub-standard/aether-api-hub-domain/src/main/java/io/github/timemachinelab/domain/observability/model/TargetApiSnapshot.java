package io.github.timemachinelab.domain.observability.model;

/**
 * Target API snapshot captured in a platform call log.
 */
public final class TargetApiSnapshot {

    private final String targetApiId;
    private final String targetApiCode;
    private final String targetApiName;
    private final String targetApiType;

    private TargetApiSnapshot(String targetApiId, String targetApiCode, String targetApiName, String targetApiType) {
        this.targetApiId = normalize(targetApiId, 36);
        this.targetApiCode = normalize(targetApiCode, 64);
        this.targetApiName = normalize(targetApiName, 128);
        this.targetApiType = normalize(targetApiType, 32);
    }

    public static TargetApiSnapshot of(String targetApiId, String targetApiCode, String targetApiName, String targetApiType) {
        return new TargetApiSnapshot(targetApiId, targetApiCode, targetApiName, targetApiType);
    }

    public String getTargetApiId() {
        return targetApiId;
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public String getTargetApiName() {
        return targetApiName;
    }

    public String getTargetApiType() {
        return targetApiType;
    }

    private static String normalize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException("Target API snapshot field must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }
}
