package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ConsoleCurrentUserModel;
import io.github.timemachinelab.service.model.ConsoleSessionModel;
import io.github.timemachinelab.service.model.ConsoleSignInCommand;

/**
 * Console session auth use case.
 */
public interface ConsoleSessionAuthUseCase {

    ConsoleSessionModel signIn(ConsoleSignInCommand command);

    ConsoleCurrentUserModel authenticate(String bearerToken);
}
