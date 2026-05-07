-- Platform proxy profile table
-- Description:
-- 1. platform_proxy_profile is the authoritative table for platform-managed outbound proxy profiles.
-- 2. Proxy profiles are maintained by platform administrators and reused by API asset bindings.
-- 3. API assets only store proxy_profile_id references; proxy endpoint and credential secrets stay in this table.

CREATE TABLE IF NOT EXISTS platform_proxy_profile (
    id                  VARCHAR(36) PRIMARY KEY COMMENT 'Proxy profile primary key UUID',
    profile_code        VARCHAR(64) NOT NULL COMMENT 'Globally unique proxy profile code',
    profile_name        VARCHAR(128) NOT NULL COMMENT 'Operator-readable proxy profile name',
    proxy_type          VARCHAR(16) NOT NULL DEFAULT 'HTTP'
                        CHECK (proxy_type IN ('HTTP')) COMMENT 'Proxy protocol type; phase one supports HTTP',
    proxy_host          VARCHAR(255) NOT NULL COMMENT 'Proxy host or IP address',
    proxy_port          INTEGER NOT NULL COMMENT 'Proxy port, range 1-65535',
    username            VARCHAR(255) NULL COMMENT 'Optional proxy username',
    password_secret     TEXT NULL COMMENT 'Optional proxy password secret',
    enabled             BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this profile can be used for new bindings and runtime forwarding',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last update time',
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft delete marker',
    version             BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version'
);

COMMENT ON TABLE platform_proxy_profile IS 'Platform-managed outbound proxy profile table';
COMMENT ON COLUMN platform_proxy_profile.id IS 'Proxy profile primary key UUID';
COMMENT ON COLUMN platform_proxy_profile.profile_code IS 'Globally unique proxy profile code';
COMMENT ON COLUMN platform_proxy_profile.profile_name IS 'Operator-readable proxy profile name';
COMMENT ON COLUMN platform_proxy_profile.proxy_type IS 'Proxy protocol type; phase one supports HTTP';
COMMENT ON COLUMN platform_proxy_profile.proxy_host IS 'Proxy host or IP address';
COMMENT ON COLUMN platform_proxy_profile.proxy_port IS 'Proxy port, range 1-65535';
COMMENT ON COLUMN platform_proxy_profile.username IS 'Optional proxy username';
COMMENT ON COLUMN platform_proxy_profile.password_secret IS 'Optional proxy password secret';
COMMENT ON COLUMN platform_proxy_profile.enabled IS 'Whether this profile can be used for new bindings and runtime forwarding';
COMMENT ON COLUMN platform_proxy_profile.created_at IS 'Creation time';
COMMENT ON COLUMN platform_proxy_profile.updated_at IS 'Last update time';
COMMENT ON COLUMN platform_proxy_profile.is_deleted IS 'Soft delete marker';
COMMENT ON COLUMN platform_proxy_profile.version IS 'Optimistic lock version';

CREATE UNIQUE INDEX IF NOT EXISTS uk_platform_proxy_profile_code ON platform_proxy_profile(profile_code);
CREATE INDEX IF NOT EXISTS idx_platform_proxy_profile_enabled ON platform_proxy_profile(enabled, is_deleted);
