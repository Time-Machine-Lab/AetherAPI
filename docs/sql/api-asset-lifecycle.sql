-- =============================================================
-- API Catalog 资产生命周期表结构
-- 用于管理 API 资产的草稿注册、配置修订、启用/停用与 AI 能力档案绑定
-- API Code 创建后不可变更；关键配置变更后资产回退为 DRAFT 并需要重新启用校验
-- =============================================================

CREATE TABLE IF NOT EXISTS api_asset (
    id                       VARCHAR(36)  PRIMARY KEY COMMENT '资产实体唯一标识(UUID)',
    api_code                 VARCHAR(64)  NOT NULL COMMENT '资产业务编码(API Code)，创建后不可变更且全局唯一',
    asset_name               VARCHAR(128) NULL COMMENT '资产展示名称，草稿阶段允许为空',
    asset_type               VARCHAR(32)  NOT NULL COMMENT '资产类型：STANDARD_API-普通 API，AI_API-AI API',
    category_code            VARCHAR(64)  NULL COMMENT '所属分类编码，仅允许引用处于 ENABLED 状态的分类',
    status                   VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                                       CHECK (status IN ('DRAFT', 'ENABLED', 'DISABLED')) COMMENT '资产状态：DRAFT-草稿，ENABLED-已启用，DISABLED-已停用',
    request_method           VARCHAR(16)  NULL COMMENT '上游请求方法：GET、POST、PUT、PATCH、DELETE',
    upstream_url             VARCHAR(512) NULL COMMENT '上游请求地址',
    auth_scheme              VARCHAR(32)  NULL COMMENT '鉴权方案：NONE、HEADER_TOKEN、QUERY_TOKEN',
    auth_config              TEXT         NULL COMMENT '鉴权配置(JSON 字符串)，如 Header/Query 参数名与固定令牌值',
    request_template         TEXT         NULL COMMENT '请求模板描述，用于记录上游请求体或参数拼装规则',
    request_example          TEXT         NULL COMMENT '请求示例快照，可为空',
    response_example         TEXT         NULL COMMENT '响应示例快照，可为空',
    ai_provider              VARCHAR(128) NULL COMMENT 'AI 能力提供商，仅 AI_API 有效',
    ai_model                 VARCHAR(128) NULL COMMENT 'AI 模型标识，仅 AI_API 有效',
    ai_streaming_supported   BOOLEAN      NULL COMMENT '是否支持流式输出，仅 AI_API 有效',
    ai_capability_tags_json  TEXT         NULL COMMENT 'AI 能力标签(JSON 数组字符串)，仅 AI_API 有效',
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录最近更新时间',
    is_deleted               BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    version                  BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
);

COMMENT ON TABLE api_asset IS 'API Catalog 资产主数据表';

COMMENT ON COLUMN api_asset.id                      IS '资产实体唯一标识(UUID)';
COMMENT ON COLUMN api_asset.api_code                IS '资产业务编码(API Code)，创建后不可变更且全局唯一';
COMMENT ON COLUMN api_asset.asset_name              IS '资产展示名称，草稿阶段允许为空';
COMMENT ON COLUMN api_asset.asset_type              IS '资产类型：STANDARD_API-普通 API，AI_API-AI API';
COMMENT ON COLUMN api_asset.category_code           IS '所属分类编码，仅允许引用处于 ENABLED 状态的分类';
COMMENT ON COLUMN api_asset.status                  IS '资产状态：DRAFT-草稿，ENABLED-已启用，DISABLED-已停用';
COMMENT ON COLUMN api_asset.request_method          IS '上游请求方法：GET、POST、PUT、PATCH、DELETE';
COMMENT ON COLUMN api_asset.upstream_url            IS '上游请求地址';
COMMENT ON COLUMN api_asset.auth_scheme             IS '鉴权方案：NONE、HEADER_TOKEN、QUERY_TOKEN';
COMMENT ON COLUMN api_asset.auth_config             IS '鉴权配置(JSON 字符串)，如 Header/Query 参数名与固定令牌值';
COMMENT ON COLUMN api_asset.request_template        IS '请求模板描述，用于记录上游请求体或参数拼装规则';
COMMENT ON COLUMN api_asset.request_example         IS '请求示例快照，可为空';
COMMENT ON COLUMN api_asset.response_example        IS '响应示例快照，可为空';
COMMENT ON COLUMN api_asset.ai_provider             IS 'AI 能力提供商，仅 AI_API 有效';
COMMENT ON COLUMN api_asset.ai_model                IS 'AI 模型标识，仅 AI_API 有效';
COMMENT ON COLUMN api_asset.ai_streaming_supported  IS '是否支持流式输出，仅 AI_API 有效';
COMMENT ON COLUMN api_asset.ai_capability_tags_json IS 'AI 能力标签(JSON 数组字符串)，仅 AI_API 有效';
COMMENT ON COLUMN api_asset.created_at              IS '记录创建时间';
COMMENT ON COLUMN api_asset.updated_at              IS '记录最近更新时间';
COMMENT ON COLUMN api_asset.is_deleted              IS '软删除标记';
COMMENT ON COLUMN api_asset.version                 IS '乐观锁版本号';

CREATE UNIQUE INDEX IF NOT EXISTS uk_api_asset_code ON api_asset(api_code);
CREATE INDEX IF NOT EXISTS idx_api_asset_status ON api_asset(status);
CREATE INDEX IF NOT EXISTS idx_api_asset_category_code ON api_asset(category_code);
CREATE INDEX IF NOT EXISTS idx_api_asset_deleted ON api_asset(is_deleted);

