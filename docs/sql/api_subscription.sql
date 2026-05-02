-- API subscription entitlement table.
-- One row represents the current user's internal Consumer entitlement to use one API asset.
-- This table is intentionally lightweight and does not model payment, plan, quota, approval, billing, or settlement.

CREATE TABLE IF NOT EXISTS api_subscription (
    id                       VARCHAR(36) PRIMARY KEY COMMENT 'Subscription record UUID',
    subscription_code        VARCHAR(64) NOT NULL COMMENT 'Stable subscription business code',
    subscriber_user_id       VARCHAR(64) NOT NULL COMMENT 'Platform user id that owns this subscription',
    subscriber_consumer_id   VARCHAR(36) NOT NULL COMMENT 'Internal Consumer id mapped from subscriber user',
    subscriber_consumer_code VARCHAR(64) NOT NULL COMMENT 'Internal Consumer code snapshot',
    api_code                 VARCHAR(64) NOT NULL COMMENT 'Subscribed API asset code',
    asset_owner_user_id      VARCHAR(64) NOT NULL COMMENT 'API asset owner user id snapshot at subscription time',
    asset_name               VARCHAR(128) NULL COMMENT 'API asset display name snapshot at subscription time',
    status                   VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                             CHECK (status IN ('ACTIVE', 'CANCELLED')) COMMENT 'Subscription status',
    cancelled_at             TIMESTAMP NULL COMMENT 'Cancellation time',
    created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last update time',
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete flag',
    version                  BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version'
);

COMMENT ON TABLE api_subscription IS 'API subscription entitlement table';
COMMENT ON COLUMN api_subscription.id IS 'Subscription record UUID';
COMMENT ON COLUMN api_subscription.subscription_code IS 'Stable subscription business code';
COMMENT ON COLUMN api_subscription.subscriber_user_id IS 'Platform user id that owns this subscription';
COMMENT ON COLUMN api_subscription.subscriber_consumer_id IS 'Internal Consumer id mapped from subscriber user';
COMMENT ON COLUMN api_subscription.subscriber_consumer_code IS 'Internal Consumer code snapshot';
COMMENT ON COLUMN api_subscription.api_code IS 'Subscribed API asset code';
COMMENT ON COLUMN api_subscription.asset_owner_user_id IS 'API asset owner user id snapshot at subscription time';
COMMENT ON COLUMN api_subscription.asset_name IS 'API asset display name snapshot at subscription time';
COMMENT ON COLUMN api_subscription.status IS 'Subscription status: ACTIVE / CANCELLED';
COMMENT ON COLUMN api_subscription.cancelled_at IS 'Cancellation time';
COMMENT ON COLUMN api_subscription.created_at IS 'Creation time';
COMMENT ON COLUMN api_subscription.updated_at IS 'Last update time';
COMMENT ON COLUMN api_subscription.is_deleted IS 'Soft delete flag';
COMMENT ON COLUMN api_subscription.version IS 'Optimistic lock version';

CREATE UNIQUE INDEX IF NOT EXISTS uk_api_subscription_code ON api_subscription(subscription_code);
CREATE INDEX IF NOT EXISTS idx_api_subscription_consumer_api_status
    ON api_subscription(subscriber_consumer_id, api_code, status, is_deleted);
CREATE INDEX IF NOT EXISTS idx_api_subscription_user ON api_subscription(subscriber_user_id);
CREATE INDEX IF NOT EXISTS idx_api_subscription_consumer ON api_subscription(subscriber_consumer_id);
CREATE INDEX IF NOT EXISTS idx_api_subscription_api_code ON api_subscription(api_code);
CREATE INDEX IF NOT EXISTS idx_api_subscription_status ON api_subscription(status);
