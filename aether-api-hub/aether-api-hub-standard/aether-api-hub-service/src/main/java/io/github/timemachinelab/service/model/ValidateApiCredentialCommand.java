package io.github.timemachinelab.service.model;

/**
 * Unified access credential validation command.
 */
public class ValidateApiCredentialCommand {

    private final String plaintextKey;
    private final String accessChannel;

    public ValidateApiCredentialCommand(String plaintextKey, String accessChannel) {
        this.plaintextKey = plaintextKey;
        this.accessChannel = accessChannel;
    }

    public String getPlaintextKey() {
        return plaintextKey;
    }

    public String getAccessChannel() {
        return accessChannel;
    }
}
