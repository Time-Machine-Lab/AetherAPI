package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ApiImportAgentRunModel;

import java.util.Optional;

/**
 * Import agent run repository port.
 */
public interface ApiImportAgentRunRepositoryPort {

    void saveRun(ApiImportAgentRunModel run);

    Optional<ApiImportAgentRunModel> findOwnedRun(String ownerUserId, String runId);
}