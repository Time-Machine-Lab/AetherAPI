package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ApiCallLogDetailModel;
import io.github.timemachinelab.service.model.ApiCallLogPageResult;
import io.github.timemachinelab.service.model.GetApiCallLogDetailQuery;
import io.github.timemachinelab.service.model.ListApiCallLogQuery;

/**
 * API call log query use case.
 */
public interface ApiCallLogUseCase {

    ApiCallLogPageResult listApiCallLogs(ListApiCallLogQuery query);

    ApiCallLogDetailModel getApiCallLogDetail(GetApiCallLogDetailQuery query);
}
