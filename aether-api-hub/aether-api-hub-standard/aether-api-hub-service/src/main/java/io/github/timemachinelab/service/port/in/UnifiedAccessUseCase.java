package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;

/**
 * Unified access entry use case.
 */
public interface UnifiedAccessUseCase {

    UnifiedAccessInvocationModel resolveInvocation(ResolveUnifiedAccessInvocationCommand command);

    UnifiedAccessProxyResponseModel invoke(ResolveUnifiedAccessInvocationCommand command);
}
