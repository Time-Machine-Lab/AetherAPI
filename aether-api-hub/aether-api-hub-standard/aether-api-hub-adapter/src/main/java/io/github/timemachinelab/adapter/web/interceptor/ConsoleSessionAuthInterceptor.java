package io.github.timemachinelab.adapter.web.interceptor;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.service.model.ConsoleCurrentUserModel;
import io.github.timemachinelab.service.port.in.ConsoleSessionAuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Console session auth interceptor.
 */
@Component
public class ConsoleSessionAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ConsoleSessionAuthUseCase consoleSessionAuthUseCase;

    public ConsoleSessionAuthInterceptor(ConsoleSessionAuthUseCase consoleSessionAuthUseCase) {
        this.consoleSessionAuthUseCase = consoleSessionAuthUseCase;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String bearerToken = resolveBearerToken(authorization);
        ConsoleCurrentUserModel currentUser = consoleSessionAuthUseCase.authenticate(bearerToken);
        request.setAttribute(ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, ConsoleSessionPrincipal.from(currentUser));
        return true;
    }

    private String resolveBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
