package io.github.timemachinelab.infrastructure.subscription.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

/**
 * API subscription persistence object.
 */
@TableName("api_subscription")
public class ApiSubscriptionDo {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String subscriptionCode;
    private String subscriberUserId;
    private String subscriberConsumerId;
    private String subscriberConsumerCode;
    private String apiCode;
    private String assetOwnerUserId;
    private String assetName;
    private String status;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Boolean isDeleted;

    @Version
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubscriptionCode() {
        return subscriptionCode;
    }

    public void setSubscriptionCode(String subscriptionCode) {
        this.subscriptionCode = subscriptionCode;
    }

    public String getSubscriberUserId() {
        return subscriberUserId;
    }

    public void setSubscriberUserId(String subscriberUserId) {
        this.subscriberUserId = subscriberUserId;
    }

    public String getSubscriberConsumerId() {
        return subscriberConsumerId;
    }

    public void setSubscriberConsumerId(String subscriberConsumerId) {
        this.subscriberConsumerId = subscriberConsumerId;
    }

    public String getSubscriberConsumerCode() {
        return subscriberConsumerCode;
    }

    public void setSubscriberConsumerCode(String subscriberConsumerCode) {
        this.subscriberConsumerCode = subscriberConsumerCode;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getAssetOwnerUserId() {
        return assetOwnerUserId;
    }

    public void setAssetOwnerUserId(String assetOwnerUserId) {
        this.assetOwnerUserId = assetOwnerUserId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
