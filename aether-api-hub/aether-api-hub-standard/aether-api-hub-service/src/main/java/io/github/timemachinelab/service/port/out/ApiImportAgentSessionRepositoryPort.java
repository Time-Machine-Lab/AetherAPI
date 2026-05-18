package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;

import java.util.List;
import java.util.Optional;

/**
 * Import agent session repository port.
 */
public interface ApiImportAgentSessionRepositoryPort {

    void saveSession(ApiImportAgentSessionModel session);

    Optional<ApiImportAgentSessionModel> findOwnedSession(String ownerUserId, String sessionId);

    void saveTurn(ImportAgentTurnModel turn);

    List<ImportAgentTurnModel> listTurns(String sessionId);

    int countTurns(String sessionId);
}