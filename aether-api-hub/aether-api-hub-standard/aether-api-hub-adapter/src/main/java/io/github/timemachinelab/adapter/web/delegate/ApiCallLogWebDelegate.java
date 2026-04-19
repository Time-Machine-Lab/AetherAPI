package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.ListApiCallLogReq;
import io.github.timemachinelab.api.resp.ApiCallLogAiExtensionResp;
import io.github.timemachinelab.api.resp.ApiCallLogDetailResp;
import io.github.timemachinelab.api.resp.ApiCallLogErrorResp;
import io.github.timemachinelab.api.resp.ApiCallLogPageResp;
import io.github.timemachinelab.api.resp.ApiCallLogResp;
import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogModel;
import io.github.timemachinelab.service.model.ApiCallLogPageResult;
import io.github.timemachinelab.service.model.GetApiCallLogDetailQuery;
import io.github.timemachinelab.service.model.ListApiCallLogQuery;
import io.github.timemachinelab.service.port.in.ApiCallLogUseCase;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * API call log web delegate.
 */
@Component
public class ApiCallLogWebDelegate {

    private final ApiCallLogUseCase apiCallLogUseCase;

    public ApiCallLogWebDelegate(ApiCallLogUseCase apiCallLogUseCase) {
        this.apiCallLogUseCase = apiCallLogUseCase;
    }

    public ApiCallLogPageResp listApiCallLogs(String currentUserId, ListApiCallLogReq req) {
        ApiCallLogPageResult result = apiCallLogUseCase.listApiCallLogs(new ListApiCallLogQuery(
                currentUserId,
                req.getTargetApiCode(),
                parseInstant(req.getInvocationStartAt(), "Invocation startAt"),
                parseInstant(req.getInvocationEndAt(), "Invocation endAt"),
                req.getPage(),
                req.getSize()
        ));
        return new ApiCallLogPageResp(
                result.getItems().stream().map(this::toResp).toList(),
                result.getPage(),
                result.getSize(),
                result.getTotal()
        );
    }

    public ApiCallLogDetailResp getApiCallLogDetail(String currentUserId, String logId) {
        return toDetailResp(apiCallLogUseCase.getApiCallLogDetail(new GetApiCallLogDetailQuery(currentUserId, logId)));
    }

    private ApiCallLogResp toResp(ApiCallLogModel model) {
        return new ApiCallLogResp(
                model.getLogId(),
                model.getTargetApiCode(),
                model.getTargetApiName(),
                model.getRequestMethod(),
                model.getInvocationTime(),
                model.getDurationMs(),
                model.getResultType(),
                model.isSuccess(),
                model.getHttpStatusCode()
        );
    }

    private ApiCallLogDetailResp toDetailResp(ApiCallLogDetailModel model) {
        return new ApiCallLogDetailResp(
                model.getLogId(),
                model.getTargetApiCode(),
                model.getTargetApiName(),
                model.getRequestMethod(),
                model.getInvocationTime(),
                model.getDurationMs(),
                model.getResultType(),
                model.isSuccess(),
                model.getHttpStatusCode(),
                model.getAccessChannel(),
                model.getCredentialCode(),
                model.getCredentialStatus(),
                model.getError() == null ? null : new ApiCallLogErrorResp(
                        model.getError().getErrorCode(),
                        model.getError().getErrorType(),
                        model.getError().getErrorSummary()
                ),
                model.getAiExtension() == null ? null : new ApiCallLogAiExtensionResp(
                        model.getAiExtension().getProvider(),
                        model.getAiExtension().getModel(),
                        model.getAiExtension().getStreaming(),
                        model.getAiExtension().getUsageSnapshot()
                ),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    private Instant parseInstant(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid ISO-8601 date-time");
        }
    }
}
