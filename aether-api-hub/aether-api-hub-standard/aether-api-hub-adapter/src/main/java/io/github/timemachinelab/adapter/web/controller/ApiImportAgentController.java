package io.github.timemachinelab.adapter.web.controller;

import io.github.timemachinelab.adapter.web.auth.ConsoleSessionPrincipal;
import io.github.timemachinelab.adapter.web.delegate.ApiImportAgentWebDelegate;
import io.github.timemachinelab.api.req.AppendImportAgentTurnReq;
import io.github.timemachinelab.api.req.ConfirmImportAgentPlanReq;
import io.github.timemachinelab.api.req.CreateImportAgentSessionReq;
import io.github.timemachinelab.api.req.StartImportAgentRunReq;
import io.github.timemachinelab.api.resp.ApiImportAgentRunResp;
import io.github.timemachinelab.api.resp.ApiImportAgentSessionResp;
import io.github.timemachinelab.common.annotation.AutoResp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Current-user import agent HTTP entry point.
 */
@RestController
@RequestMapping("/api/v1/current-user/import-agent")
@AutoResp
public class ApiImportAgentController {

    private final ApiImportAgentWebDelegate delegate;

    public ApiImportAgentController(ApiImportAgentWebDelegate delegate) {
        this.delegate = delegate;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiImportAgentSessionResp createSession(
            @Valid @RequestBody CreateImportAgentSessionReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.createSession(currentUserId(consoleSessionPrincipal, principal), publisherDisplayName(consoleSessionPrincipal, principal), req);
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiImportAgentSessionResp getSession(
            @PathVariable("sessionId") String sessionId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.getSession(currentUserId(consoleSessionPrincipal, principal), sessionId);
    }

    @PostMapping("/sessions/{sessionId}/turns")
    public ApiImportAgentSessionResp appendTurn(
            @PathVariable("sessionId") String sessionId,
            @Valid @RequestBody AppendImportAgentTurnReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.appendTurn(currentUserId(consoleSessionPrincipal, principal), sessionId, req);
    }

    @PatchMapping("/sessions/{sessionId}/confirm")
    public ApiImportAgentSessionResp confirmPlan(
            @PathVariable("sessionId") String sessionId,
            @Valid @RequestBody ConfirmImportAgentPlanReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.confirmPlan(currentUserId(consoleSessionPrincipal, principal), sessionId, req);
    }

    @PostMapping("/sessions/{sessionId}/runs")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiImportAgentRunResp startRun(
            @PathVariable("sessionId") String sessionId,
            @Valid @RequestBody StartImportAgentRunReq req,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.startRun(currentUserId(consoleSessionPrincipal, principal), publisherDisplayName(consoleSessionPrincipal, principal), sessionId, req);
    }

    @GetMapping("/runs/{runId}")
    public ApiImportAgentRunResp getRun(
            @PathVariable("runId") String runId,
            @RequestAttribute(name = ConsoleSessionPrincipal.REQUEST_ATTRIBUTE, required = false)
            ConsoleSessionPrincipal consoleSessionPrincipal,
            Principal principal) {
        return delegate.getRun(currentUserId(consoleSessionPrincipal, principal), runId);
    }

    private String currentUserId(ConsoleSessionPrincipal consoleSessionPrincipal, Principal principal) {
        if (consoleSessionPrincipal != null
                && consoleSessionPrincipal.getUserId() != null
                && !consoleSessionPrincipal.getUserId().isBlank()) {
            return consoleSessionPrincipal.getUserId();
        }
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Current user id must not be blank");
        }
        return principal.getName();
    }

    private String publisherDisplayName(ConsoleSessionPrincipal consoleSessionPrincipal, Principal principal) {
        if (consoleSessionPrincipal != null) {
            if (consoleSessionPrincipal.getDisplayName() != null && !consoleSessionPrincipal.getDisplayName().isBlank()) {
                return consoleSessionPrincipal.getDisplayName();
            }
            if (consoleSessionPrincipal.getLoginName() != null && !consoleSessionPrincipal.getLoginName().isBlank()) {
                return consoleSessionPrincipal.getLoginName();
            }
        }
        return currentUserId(consoleSessionPrincipal, principal);
    }
}