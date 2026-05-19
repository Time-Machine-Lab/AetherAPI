package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.AppendImportAgentTurnCommand;
import io.github.timemachinelab.service.model.ConfirmImportAgentPlanCommand;
import io.github.timemachinelab.service.model.CreateImportAgentSessionCommand;
import io.github.timemachinelab.service.model.StartImportAgentRunCommand;

import java.util.function.Consumer;

/**
 * Import agent use case.
 */
public interface ApiImportAgentUseCase {

    ApiImportAgentSessionModel createSession(CreateImportAgentSessionCommand command);

    ApiImportAgentSessionModel createSession(CreateImportAgentSessionCommand command, Consumer<String> deltaConsumer);

    ApiImportAgentSessionModel getSession(String ownerUserId, String sessionId);

    ApiImportAgentSessionModel appendTurn(AppendImportAgentTurnCommand command);

    ApiImportAgentSessionModel appendTurn(AppendImportAgentTurnCommand command, Consumer<String> deltaConsumer);

    ApiImportAgentSessionModel confirmPlan(ConfirmImportAgentPlanCommand command);

    ApiImportAgentRunModel startRun(StartImportAgentRunCommand command);

    ApiImportAgentRunModel getRun(String ownerUserId, String runId);
}