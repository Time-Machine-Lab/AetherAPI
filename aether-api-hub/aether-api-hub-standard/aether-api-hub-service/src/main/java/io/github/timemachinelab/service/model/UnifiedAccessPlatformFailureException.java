package io.github.timemachinelab.service.model;

/**
 * Exception carrying a structured platform-side pre-forward failure.
 */
public class UnifiedAccessPlatformFailureException extends RuntimeException {

    private final UnifiedAccessPlatformFailureModel failure;

    public UnifiedAccessPlatformFailureException(UnifiedAccessPlatformFailureModel failure) {
        super(failure == null ? null : failure.getMessage());
        this.failure = failure;
    }

    public UnifiedAccessPlatformFailureModel getFailure() {
        return failure;
    }
}
