package io.github.timemachinelab.infrastructure.importagent.persistence.repository;

import io.github.timemachinelab.service.model.ApiImportAgentRunModel;
import io.github.timemachinelab.service.model.ImportAgentRunStatus;
import io.github.timemachinelab.service.port.out.ApiImportAgentRunRepositoryPort;
import io.github.timemachinelab.infrastructure.importagent.persistence.converter.ImportAgentJsonConverter;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentRunDo;
import io.github.timemachinelab.infrastructure.importagent.persistence.mapper.ApiImportAgentRunMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * MyBatis import agent run repository.
 */
@Repository
public class MybatisApiImportAgentRunRepository implements ApiImportAgentRunRepositoryPort {

    private final ApiImportAgentRunMapper runMapper;

    public MybatisApiImportAgentRunRepository(ApiImportAgentRunMapper runMapper) {
        this.runMapper = runMapper;
    }

    @Override
    public void saveRun(ApiImportAgentRunModel run) {
        ApiImportAgentRunDo existing = runMapper.selectById(run.getRunId());
        if (existing == null) {
            runMapper.insert(toDo(run));
            return;
        }
        updateDo(existing, run);
        runMapper.updateById(existing);
    }

    @Override
    public Optional<ApiImportAgentRunModel> findOwnedRun(String ownerUserId, String runId) {
        return Optional.ofNullable(toModel(runMapper.selectOwnedById(ownerUserId, runId)));
    }

    private ApiImportAgentRunDo toDo(ApiImportAgentRunModel run) {
        ApiImportAgentRunDo target = new ApiImportAgentRunDo();
        target.setId(run.getRunId());
        updateDo(target, run);
        return target;
    }

    private void updateDo(ApiImportAgentRunDo target, ApiImportAgentRunModel run) {
        target.setSessionId(run.getSessionId());
        target.setOwnerUserId(run.getOwnerUserId());
        target.setPlanVersion(run.getPlanVersion());
        target.setStatus(run.getStatus().name());
        target.setSummary(run.getSummary());
        target.setFailureReason(run.getFailureReason());
        target.setAffectedApiCodes(ImportAgentJsonConverter.serializeStringList(run.getAffectedApiCodes()));
        target.setStepResultsJson(ImportAgentJsonConverter.serializeStepResults(run.getStepResults()));
        target.setCreatedAt(toLocalDateTime(run.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(run.getUpdatedAt()));
    }

    private ApiImportAgentRunModel toModel(ApiImportAgentRunDo source) {
        if (source == null) {
            return null;
        }
        return new ApiImportAgentRunModel(
                source.getId(),
                source.getSessionId(),
                source.getOwnerUserId(),
                source.getPlanVersion(),
                ImportAgentRunStatus.valueOf(source.getStatus()),
                source.getSummary(),
                source.getFailureReason(),
                ImportAgentJsonConverter.deserializeStringList(source.getAffectedApiCodes()),
                ImportAgentJsonConverter.deserializeStepResults(source.getStepResultsJson()),
                toInstantString(source.getCreatedAt()),
                toInstantString(source.getUpdatedAt())
        );
    }

    private LocalDateTime toLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(value).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String toInstantString(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC).toInstant().toString();
    }
}