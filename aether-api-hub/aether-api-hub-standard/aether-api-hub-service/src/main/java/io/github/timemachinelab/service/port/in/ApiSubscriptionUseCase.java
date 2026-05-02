package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.service.model.ApiSubscriptionModel;
import io.github.timemachinelab.service.model.ApiSubscriptionPageResult;
import io.github.timemachinelab.service.model.ApiSubscriptionStatusModel;
import io.github.timemachinelab.service.model.CancelApiSubscriptionCommand;
import io.github.timemachinelab.service.model.GetApiSubscriptionStatusQuery;
import io.github.timemachinelab.service.model.ListApiSubscriptionQuery;
import io.github.timemachinelab.service.model.SubscribeApiCommand;

/**
 * API subscription use case.
 */
public interface ApiSubscriptionUseCase {

    ApiSubscriptionModel subscribe(SubscribeApiCommand command);

    ApiSubscriptionPageResult listSubscriptions(ListApiSubscriptionQuery query);

    ApiSubscriptionStatusModel getSubscriptionStatus(GetApiSubscriptionStatusQuery query);

    ApiSubscriptionModel cancelSubscription(CancelApiSubscriptionCommand command);
}
