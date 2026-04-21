package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.api.req.ConsoleSignInReq;
import io.github.timemachinelab.api.resp.ConsoleCurrentSessionResp;
import io.github.timemachinelab.api.resp.ConsoleCurrentUserResp;
import io.github.timemachinelab.api.resp.ConsoleSignInResp;
import io.github.timemachinelab.service.model.ConsoleCurrentUserModel;
import io.github.timemachinelab.service.model.ConsoleSessionModel;
import io.github.timemachinelab.service.model.ConsoleSignInCommand;
import io.github.timemachinelab.service.port.in.ConsoleSessionAuthUseCase;
import org.springframework.stereotype.Component;

/**
 * Console auth web delegate.
 */
@Component
public class ConsoleAuthWebDelegate {

    private final ConsoleSessionAuthUseCase consoleSessionAuthUseCase;

    public ConsoleAuthWebDelegate(ConsoleSessionAuthUseCase consoleSessionAuthUseCase) {
        this.consoleSessionAuthUseCase = consoleSessionAuthUseCase;
    }

    public ConsoleSignInResp signIn(ConsoleSignInReq req) {
        ConsoleSessionModel model = consoleSessionAuthUseCase.signIn(new ConsoleSignInCommand(
                req.getLoginName(),
                req.getPassword()
        ));
        return new ConsoleSignInResp(
                model.getAccessToken(),
                model.getTokenType(),
                model.getExpiresAt(),
                model.getExpiresInSeconds(),
                toCurrentUserResp(model.getCurrentUser())
        );
    }

    public ConsoleCurrentSessionResp getCurrentSession(ConsoleSessionPrincipal principal) {
        return new ConsoleCurrentSessionResp(new ConsoleCurrentUserResp(
                principal.getUserId(),
                principal.getLoginName(),
                principal.getDisplayName(),
                principal.getEmail(),
                principal.getRole()
        ));
    }

    private ConsoleCurrentUserResp toCurrentUserResp(ConsoleCurrentUserModel model) {
        return new ConsoleCurrentUserResp(
                model.getUserId(),
                model.getLoginName(),
                model.getDisplayName(),
                model.getEmail(),
                model.getRole()
        );
    }
}
