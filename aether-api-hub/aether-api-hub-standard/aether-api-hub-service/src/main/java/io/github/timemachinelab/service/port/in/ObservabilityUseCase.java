package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.RecordApiCallLogCommand;

/**
 * Observability write-side use case for platform call logs.
 */
public interface ObservabilityUseCase {

    void recordApiCallLog(RecordApiCallLogCommand command);
}
