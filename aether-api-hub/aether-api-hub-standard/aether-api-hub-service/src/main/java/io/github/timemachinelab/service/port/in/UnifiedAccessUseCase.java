package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ResolveUnifiedAccessInvocationCommand;
import io.github.timemachinelab.service.model.ResolveUnifiedAccessTaskQueryCommand;
import io.github.timemachinelab.service.model.UnifiedAccessInvocationModel;
import io.github.timemachinelab.service.model.UnifiedAccessProxyResponseModel;

/**
 * Unified access entry use case.
 */
public interface UnifiedAccessUseCase {

    UnifiedAccessInvocationModel resolveInvocation(ResolveUnifiedAccessInvocationCommand command);

    default UnifiedAccessInvocationModel resolveTaskQueryInvocation(ResolveUnifiedAccessTaskQueryCommand command) {
        throw new UnsupportedOperationException("Unified Access task query is not supported by this use case implementation");
    }

    UnifiedAccessProxyResponseModel invoke(ResolveUnifiedAccessInvocationCommand command);

    default UnifiedAccessProxyResponseModel queryTask(ResolveUnifiedAccessTaskQueryCommand command) {
        throw new UnsupportedOperationException("Unified Access task query is not supported by this use case implementation");
    }
}
