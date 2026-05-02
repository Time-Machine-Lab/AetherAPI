package io.github.timemachinelab.service.port.out;

import io.github.timemachinelab.domain.catalog.model.ApiCode;
import io.github.timemachinelab.domain.consumerauth.model.ConsumerId;

/**
 * Entitlement read port used by Unified Access.
 */
public interface ApiSubscriptionEntitlementPort {

    boolean hasActiveSubscription(ConsumerId consumerId, ApiCode apiCode);
}
