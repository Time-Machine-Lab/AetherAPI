package io.github.timemachinelab.infrastructure.importagent.persistence.repository;

import io.github.timemachinelab.service.model.ApiImportAgentSessionModel;
import io.github.timemachinelab.service.model.ImportAgentActorType;
import io.github.timemachinelab.service.model.ImportAgentSessionStatus;
import io.github.timemachinelab.service.model.ImportAgentTurnModel;
import io.github.timemachinelab.service.port.out.ApiImportAgentSessionRepositoryPort;
import io.github.timemachinelab.infrastructure.importagent.persistence.converter.ImportAgentJsonConverter;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentSessionDo;
import io.github.timemachinelab.infrastructure.importagent.persistence.entity.ApiImportAgentTurnDo;
import io.github.timemachinelab.infrastructure.importagent.persistence.mapper.ApiImportAgentSessionMapper;
import io.github.timemachinelab.infrastructure.importagent.persistence.mapper.ApiImportAgentTurnMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis import agent session repository.
 */
@Repository
public class MybatisApiImportAgentSessionRepository implements ApiImportAgentSessionRepositoryPort {

    private final ApiImportAgentSessionMapper sessionMapper;
    private final ApiImportAgentTurnMapper turnMapper;

    public MybatisApiImportAgentSessionRepository(
            ApiImportAgentSessionMapper sessionMapper,
            ApiImportAgentTurnMapper turnMapper) {
        this.sessionMapper = sessionMapper;
        this.turnMapper = turnMapper;
    }

    @Override
    public void saveSession(ApiImportAgentSessionModel session) {
        ApiImportAgentSessionDo existing = sessionMapper.selectById(session.getSessionId());
        if (existing == null) {
            sessionMapper.insert(toDo(session));
            return;
        }
        updateDo(existing, session);
        sessionMapper.updateById(existing);
    }

    @Override
    public Optional<ApiImportAgentSessionModel> findOwnedSession(String ownerUserId, String sessionId) {
        return Optional.ofNullable(toModel(sessionMapper.selectOwnedById(ownerUserId, sessionId)));
    }

    @Override
    public void saveTurn(ImportAgentTurnModel turn) {
        turnMapper.insert(toDo(turn));
    }

    @Override
    public List<ImportAgentTurnModel> listTurns(String sessionId) {
        return turnMapper.selectBySessionId(sessionId).stream().map(this::toModel).toList();
    }

    @Override
    public int countTurns(String sessionId) {
        return turnMapper.countBySessionId(sessionId);
    }

    private ApiImportAgentSessionDo toDo(ApiImportAgentSessionModel session) {
        ApiImportAgentSessionDo target = new ApiImportAgentSessionDo();
        updateDo(target, session);
        target.setId(session.getSessionId());
        return target;
    }

    private void updateDo(ApiImportAgentSessionDo target, ApiImportAgentSessionModel session) {
        target.setOwnerUserId(session.getOwnerUserId());
        target.setStatus(session.getStatus().name());
        target.setDocumentSource(session.getDocumentSource());
        target.setDocumentSummary(session.getDocumentSummary());
        target.setImportIntent(session.getImportIntent());
        target.setPublisherDisplayName(session.getPublisherDisplayName());
        target.setCurrentPlanVersion(session.getCurrentPlanVersion());
        target.setConfirmedPlanVersion(session.getConfirmedPlanVersion());
        target.setPlanSnapshotJson(ImportAgentJsonConverter.serializePlan(session.getCurrentPlan()));
        target.setLatestRunId(session.getLatestRunId());
        target.setLatestConfirmedAt(toLocalDateTime(session.getLatestConfirmedAt()));
        target.setCreatedAt(toLocalDateTime(session.getCreatedAt()));
        target.setUpdatedAt(toLocalDateTime(session.getUpdatedAt()));
    }

    private ApiImportAgentSessionModel toModel(ApiImportAgentSessionDo source) {
        if (source == null) {
            return null;
        }
        return new ApiImportAgentSessionModel(
                source.getId(),
                source.getOwnerUserId(),
                ImportAgentSessionStatus.valueOf(source.getStatus()),
                source.getDocumentSource(),
                source.getDocumentSummary(),
                source.getImportIntent(),
                source.getPublisherDisplayName(),
                source.getCurrentPlanVersion(),
                source.getConfirmedPlanVersion(),
                source.getLatestRunId(),
                toInstantString(source.getLatestConfirmedAt()),
                ImportAgentJsonConverter.deserializePlan(source.getPlanSnapshotJson()),
                List.of(),
                toInstantString(source.getCreatedAt()),
                toInstantString(source.getUpdatedAt())
        );
    }

    private ApiImportAgentTurnDo toDo(ImportAgentTurnModel turn) {
        ApiImportAgentTurnDo target = new ApiImportAgentTurnDo();
        target.setId(turn.getTurnId());
        target.setSessionId(turn.getSessionId());
        target.setTurnIndex(turn.getTurnIndex());
        target.setActorType(turn.getActorType().name());
        target.setMessageText(turn.getMessage());
        target.setPlanVersion(turn.getPlanVersion());
        target.setCreatedAt(toLocalDateTime(turn.getCreatedAt()));
        return target;
    }

    private ImportAgentTurnModel toModel(ApiImportAgentTurnDo source) {
        return new ImportAgentTurnModel(
                source.getId(),
                source.getSessionId(),
                source.getTurnIndex(),
                ImportAgentActorType.valueOf(source.getActorType()),
                source.getMessageText(),
                source.getPlanVersion(),
                toInstantString(source.getCreatedAt())
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