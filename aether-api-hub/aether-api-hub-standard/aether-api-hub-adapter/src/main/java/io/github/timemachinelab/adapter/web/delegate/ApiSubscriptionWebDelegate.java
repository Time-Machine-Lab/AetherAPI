package io.github.timemachinelab.adapter.web.delegate;

import io.github.timemachinelab.api.req.SubscribeApiReq;
import io.github.timemachinelab.api.resp.ApiSubscriptionPageResp;
import io.github.timemachinelab.api.resp.ApiSubscriptionResp;
import io.github.timemachinelab.api.resp.ApiSubscriptionStatusResp;
import io.github.timemachinelab.service.model.ApiSubscriptionModel;
import io.github.timemachinelab.service.model.ApiSubscriptionPageResult;
import io.github.timemachinelab.service.model.ApiSubscriptionStatusModel;
import io.github.timemachinelab.service.model.CancelApiSubscriptionCommand;
import io.github.timemachinelab.service.model.GetApiSubscriptionStatusQuery;
import io.github.timemachinelab.service.model.ListApiSubscriptionQuery;
import io.github.timemachinelab.service.model.SubscribeApiCommand;
import io.github.timemachinelab.service.port.in.ApiSubscriptionUseCase;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * API subscription web delegate.
 */
@Component
public class ApiSubscriptionWebDelegate {

    private final ApiSubscriptionUseCase apiSubscriptionUseCase;

    public ApiSubscriptionWebDelegate(ApiSubscriptionUseCase apiSubscriptionUseCase) {
        this.apiSubscriptionUseCase = apiSubscriptionUseCase;
    }

    public ApiSubscriptionResp subscribe(String currentUserId, SubscribeApiReq req) {
        return toResp(apiSubscriptionUseCase.subscribe(new SubscribeApiCommand(currentUserId, req.getApiCode())));
    }

    public ApiSubscriptionPageResp listSubscriptions(String currentUserId, int page, int size) {
        ApiSubscriptionPageResult result = apiSubscriptionUseCase.listSubscriptions(
                new ListApiSubscriptionQuery(currentUserId, page, size));
        return new ApiSubscriptionPageResp(
                result.getItems().stream().map(this::toResp).collect(Collectors.toList()),
                result.getPage(),
                result.getSize(),
                result.getTotal()
        );
    }

    public ApiSubscriptionStatusResp getSubscriptionStatus(String currentUserId, String apiCode) {
        return toStatusResp(apiSubscriptionUseCase.getSubscriptionStatus(
                new GetApiSubscriptionStatusQuery(currentUserId, apiCode)));
    }

    public ApiSubscriptionResp cancelSubscription(String currentUserId, String subscriptionId) {
        return toResp(apiSubscriptionUseCase.cancelSubscription(
                new CancelApiSubscriptionCommand(currentUserId, subscriptionId)));
    }

    private ApiSubscriptionResp toResp(ApiSubscriptionModel model) {
        return new ApiSubscriptionResp(
                model.getSubscriptionId(),
                model.getApiCode(),
                model.getAssetName(),
                model.getAssetOwnerUserId(),
                model.getSubscriptionStatus(),
                model.isSubscribed(),
                model.isOwnerAccess(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                model.getCancelledAt()
        );
    }

    private ApiSubscriptionStatusResp toStatusResp(ApiSubscriptionStatusModel model) {
        return new ApiSubscriptionStatusResp(
                model.getApiCode(),
                model.getAccessStatus(),
                model.getSubscriptionId(),
                model.getSubscriptionStatus(),
                model.isSubscribed(),
                model.isOwnerAccess()
        );
    }
}
