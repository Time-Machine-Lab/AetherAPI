package io.github.timemachinelab.service.model;

import java.util.List;
import java.util.Map;

/**
 * Command for querying an upstream async task through Unified Access.
 */
public class ResolveUnifiedAccessTaskQueryCommand extends ResolveUnifiedAccessInvocationCommand {

    private final String taskId;

    public ResolveUnifiedAccessTaskQueryCommand(
            String apiCode,
            String taskId,
            String plaintextApiKey,
            String httpMethod,
            Map<String, List<String>> headers,
            Map<String, List<String>> queryParameters,
            byte[] requestBody,
            String contentType,
            String accessChannel) {
        super(apiCode, plaintextApiKey, httpMethod, headers, queryParameters, requestBody, contentType, accessChannel);
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }
}
