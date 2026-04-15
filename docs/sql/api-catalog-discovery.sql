-- =============================================================
-- API Catalog Discovery 查询设计
-- 复用 api_asset / api_category 主数据表
-- 用于 API 市场浏览场景的列表与详情只读查询
-- =============================================================

-- 设计约束
-- 1. discovery 对外仅暴露 status = ENABLED 的资产
-- 2. 列表仅返回浏览摘要，不泄露写模型内部字段
-- 3. 详情不返回 upstream_url、auth_config 等内部接入配置
-- 4. AI_API 详情额外返回 provider、model、streaming 能力与 capability tags

-- 依赖表
-- - api_asset: docs/sql/api-asset-lifecycle.sql
-- - api_category: docs/sql/api-category-lifecycle.sql

-- =============================================================
-- Discovery 列表查询
-- 返回已启用资产的分类摘要与资产类型摘要，供 API 市场列表展示
-- =============================================================
SELECT
    a.api_code,
    a.asset_name,
    a.asset_type,
    a.category_code,
    c.category_name
FROM api_asset a
LEFT JOIN api_category c
    ON c.category_code = a.category_code
   AND c.is_deleted = FALSE
WHERE a.is_deleted = FALSE
  AND a.status = 'ENABLED'
ORDER BY a.created_at DESC;

-- =============================================================
-- Discovery 详情查询
-- 返回已启用资产的可浏览详情；普通 API 与 AI_API 共享公共字段，
-- AI_API 额外返回能力元数据
-- =============================================================
SELECT
    a.api_code,
    a.asset_name,
    a.asset_type,
    a.category_code,
    c.category_name,
    a.request_method,
    a.auth_scheme,
    a.request_template,
    a.request_example,
    a.response_example,
    a.ai_provider,
    a.ai_model,
    a.ai_streaming_supported,
    a.ai_capability_tags_json
FROM api_asset a
LEFT JOIN api_category c
    ON c.category_code = a.category_code
   AND c.is_deleted = FALSE
WHERE a.is_deleted = FALSE
  AND a.status = 'ENABLED'
  AND a.api_code = :apiCode
LIMIT 1;

-- =============================================================
-- 复用索引说明
-- =============================================================
-- 1. uk_api_asset_code            复用 api_code 唯一索引定位详情资产
-- 2. idx_api_asset_status         复用 status 索引筛选 ENABLED 资产
-- 3. idx_api_asset_category_code  复用 category_code 索引支撑分类关联
