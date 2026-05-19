package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;

import java.util.function.Consumer;

/**
 * Streams user-facing import-agent replies based on the finalized plan.
 */
public interface ApiImportAgentReplyPort {

    String streamReply(
            ImportAgentPlannerRequest request,
            ImportAgentPlanModel plan,
            Consumer<String> deltaConsumer
    );
}