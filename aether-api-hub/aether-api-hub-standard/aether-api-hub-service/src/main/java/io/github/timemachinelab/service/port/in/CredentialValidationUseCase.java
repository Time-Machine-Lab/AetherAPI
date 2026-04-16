package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.CredentialValidationResult;
import io.github.timemachinelab.service.model.ValidateApiCredentialCommand;

/**
 * Internal unified access credential validation use case.
 */
public interface CredentialValidationUseCase {

    CredentialValidationResult validateApiCredential(ValidateApiCredentialCommand command);
}
