package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;

/**
 * Downstream proxy execution boundary for unified access.
 */
public interface UnifiedAccessDownstreamProxyPort {

    UnifiedAccessProxyResponseModel forward(UnifiedAccessInvocationModel invocation);
}
