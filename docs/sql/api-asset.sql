-- API 资产表
-- 说明：
-- 1. api_asset 是 API Catalog 领域的唯一资产主表。
-- 2. API 资产按“当前用户拥有的市场资产”建模，而不是平台代维护目录。
-- 3. 资产发布语义使用 DRAFT / PUBLISHED / UNPUBLISHED，不再使用 ENABLED / DISABLED。
-- 4. 如环境中已存在历史 api_asset 数据，迁移时必须先完成 owner_user_id 与发布者展示快照的回填，再按本结构收敛。

CREATE TABLE IF NOT EXISTS api_asset (
    id                      VARCHAR(36) PRIMARY KEY COMMENT '资产主键 UUID',
    api_code                VARCHAR(64) NOT NULL COMMENT '全局唯一 API 资产编码，创建后不可修改',
    owner_user_id           VARCHAR(64) NOT NULL COMMENT '资产所有者用户 ID',
    publisher_display_name  VARCHAR(128) NULL COMMENT '发布者展示名称快照，用于市场展示',
    asset_name              VARCHAR(128) NULL COMMENT '资产展示名称',
    asset_type              VARCHAR(32) NOT NULL COMMENT '资产类型：STANDARD_API / AI_API',
    category_code           VARCHAR(64) NULL COMMENT '分类编码',
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED')) COMMENT '资产状态：DRAFT / PUBLISHED / UNPUBLISHED',
    published_at            TIMESTAMP NULL COMMENT '最近一次发布时间',
    request_method          VARCHAR(16) NULL COMMENT '上游请求方法：GET / POST / PUT / PATCH / DELETE',
    upstream_url            VARCHAR(512) NULL COMMENT '上游请求地址',
    auth_scheme             VARCHAR(32) NULL COMMENT '上游鉴权方案：NONE / HEADER_TOKEN / QUERY_TOKEN',
    auth_config             TEXT NULL COMMENT '上游鉴权配置 JSON',
    request_template        TEXT NULL COMMENT '请求模板描述',
    request_example         TEXT NULL COMMENT '请求示例快照',
    response_example        TEXT NULL COMMENT '响应示例快照',
    ai_provider             VARCHAR(128) NULL COMMENT 'AI 提供方，仅 AI_API 使用',
    ai_model                VARCHAR(128) NULL COMMENT 'AI 模型标识，仅 AI_API 使用',
    ai_streaming_supported  BOOLEAN NULL COMMENT '是否支持流式，仅 AI_API 使用',
    ai_capability_tags_json TEXT NULL COMMENT 'AI 能力标签 JSON 数组，仅 AI_API 使用',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    version                 BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE api_asset IS 'API 资产主表';
COMMENT ON COLUMN api_asset.id IS '资产主键 UUID';
COMMENT ON COLUMN api_asset.api_code IS '全局唯一 API 资产编码，创建后不可修改';
COMMENT ON COLUMN api_asset.owner_user_id IS '资产所有者用户 ID';
COMMENT ON COLUMN api_asset.publisher_display_name IS '发布者展示名称快照，用于市场展示';
COMMENT ON COLUMN api_asset.asset_name IS '资产展示名称';
COMMENT ON COLUMN api_asset.asset_type IS '资产类型：STANDARD_API / AI_API';
COMMENT ON COLUMN api_asset.category_code IS '分类编码';
COMMENT ON COLUMN api_asset.status IS '资产状态：DRAFT / PUBLISHED / UNPUBLISHED';
COMMENT ON COLUMN api_asset.published_at IS '最近一次发布时间';
COMMENT ON COLUMN api_asset.request_method IS '上游请求方法：GET / POST / PUT / PATCH / DELETE';
COMMENT ON COLUMN api_asset.upstream_url IS '上游请求地址';
COMMENT ON COLUMN api_asset.auth_scheme IS '上游鉴权方案：NONE / HEADER_TOKEN / QUERY_TOKEN';
COMMENT ON COLUMN api_asset.auth_config IS '上游鉴权配置 JSON';
COMMENT ON COLUMN api_asset.request_template IS '请求模板描述';
COMMENT ON COLUMN api_asset.request_example IS '请求示例快照';
COMMENT ON COLUMN api_asset.response_example IS '响应示例快照';
COMMENT ON COLUMN api_asset.ai_provider IS 'AI 提供方，仅 AI_API 使用';
COMMENT ON COLUMN api_asset.ai_model IS 'AI 模型标识，仅 AI_API 使用';
COMMENT ON COLUMN api_asset.ai_streaming_supported IS '是否支持流式，仅 AI_API 使用';
COMMENT ON COLUMN api_asset.ai_capability_tags_json IS 'AI 能力标签 JSON 数组，仅 AI_API 使用';
COMMENT ON COLUMN api_asset.created_at IS '创建时间';
COMMENT ON COLUMN api_asset.updated_at IS '更新时间';
COMMENT ON COLUMN api_asset.is_deleted IS '软删除标记';
COMMENT ON COLUMN api_asset.version IS '乐观锁版本号';

CREATE UNIQUE INDEX IF NOT EXISTS uk_api_asset_code ON api_asset(api_code);
CREATE INDEX IF NOT EXISTS idx_api_asset_owner_user_id ON api_asset(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_api_asset_market_visibility ON api_asset(status, is_deleted);
CREATE INDEX IF NOT EXISTS idx_api_asset_category_code ON api_asset(category_code);
CREATE INDEX IF NOT EXISTS idx_api_asset_published_at ON api_asset(published_at);
