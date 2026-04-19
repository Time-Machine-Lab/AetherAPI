package io.github.timemachinelab.service.application;

import io.github.timemachinelab.domain.observability.model.AiInvocationExtension;
import io.github.timemachinelab.domain.observability.model.ApiCallLogAggregate;
import io.github.timemachinelab.domain.observability.model.ApiCallLogId;
import io.github.timemachinelab.domain.observability.model.ConsumerSnapshot;
import io.github.timemachinelab.domain.observability.model.ErrorSnapshot;
import io.github.timemachinelab.domain.observability.model.TargetApiSnapshot;
import io.github.timemachinelab.service.model.RecordApiCallLogCommand;
import io.github.timemachinelab.service.port.in.ObservabilityUseCase;
import io.github.timemachinelab.service.port.out.ApiCallLogRepositoryPort;

import java.util.Objects;

/**
 * Observability application service for recording platform call logs.
 */
public class ObservabilityApplicationService implements ObservabilityUseCase {

    private final ApiCallLogRepositoryPort apiCallLogRepositoryPort;

    public ObservabilityApplicationService(ApiCallLogRepositoryPort apiCallLogRepositoryPort) {
        this.apiCallLogRepositoryPort = apiCallLogRepositoryPort;
    }

    @Override
    public void recordApiCallLog(RecordApiCallLogCommand command) {
        Objects.requireNonNull(command, "RecordApiCallLogCommand must not be null");

        ApiCallLogAggregate aggregate = ApiCallLogAggregate.record(
                ApiCallLogId.generate(),
                ConsumerSnapshot.of(
                        command.getConsumerId(),
                        command.getConsumerCode(),
                        command.getConsumerName(),
                        command.getConsumerType(),
                        command.getCredentialId(),
                        command.getCredentialCode(),
                        command.getCredentialStatus()
                ),
                TargetApiSnapshot.of(
                        command.getTargetApiId(),
                        command.getTargetApiCode(),
                        command.getTargetApiName(),
                        command.getTargetApiType()
                ),
                command.getAccessChannel(),
                command.getRequestMethod(),
                command.getInvocationTime(),
                command.getDurationMs()
        );

        if (command.isSuccess()) {
            aggregate.markSucceeded(command.getResultType(), command.getHttpStatusCode());
        } else {
            aggregate.markFailed(
                    command.getResultType(),
                    command.getHttpStatusCode(),
                    ErrorSnapshot.of(
                            command.getErrorCode(),
                            command.getErrorType(),
                            firstNonBlank(
                                    command.getErrorSummary(),
                                    command.getErrorType(),
                                    command.getErrorCode(),
                                    command.getResultType()
                            )
                    )
            );
        }

        aggregate.attachAiExtension(AiInvocationExtension.of(
                command.getAiProvider(),
                command.getAiModel(),
                command.getAiStreaming(),
                command.getAiUsageSnapshot(),
                command.getAiBillingReserved()
        ));
        apiCallLogRepositoryPort.save(aggregate);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
