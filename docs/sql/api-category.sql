-- =============================================================
-- API Catalog 分类生命周期表结构
-- 用于管理 API 分类的创建、启停与有效性约束
-- 分类编码（CategoryCode）创建后不可变
-- =============================================================

CREATE TABLE IF NOT EXISTS api_category (
    id              VARCHAR(36)  PRIMARY KEY COMMENT '分类实体唯一标识（UUID）',
    category_code   VARCHAR(64)  NOT NULL                COMMENT '分类业务编码，创建后不可变更，唯一标识一个分类',
    category_name   VARCHAR(128) NOT NULL                COMMENT '分类展示名称，允许后续重命名',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ENABLED'
                                 CHECK (status IN ('ENABLED', 'DISABLED')) COMMENT '分类启用状态：ENABLED-可用，DISABLED-停用',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '记录创建时间',
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '记录更新时间',
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE              COMMENT '软删除标记',
    version         BIGINT       NOT NULL DEFAULT 0                  COMMENT '乐观锁版本号，用于并发更新控制'
);

-- 表级注释
COMMENT ON TABLE api_category IS 'API Catalog 分类主数据表';

-- 字段级注释
COMMENT ON COLUMN api_category.id             IS '分类实体唯一标识（UUID）';
COMMENT ON COLUMN api_category.category_code  IS '分类业务编码，创建后不可变更，是分类的唯一业务标识';
COMMENT ON COLUMN api_category.category_name  IS '分类展示名称，允许后续重命名';
COMMENT ON COLUMN api_category.status         IS '分类启用状态：ENABLED-可用（可被新资产引用），DISABLED-停用（不得被新资产引用）';
COMMENT ON COLUMN api_category.created_at     IS '记录创建时间';
COMMENT ON COLUMN api_category.updated_at     IS '记录最近更新时间';
COMMENT ON COLUMN api_category.is_deleted     IS '软删除标记，TRUE 表示已删除';
COMMENT ON COLUMN api_category.version        IS '乐观锁版本号，防止并发更新冲突';

-- 唯一约束：category_code 全局唯一且不可重复
CREATE UNIQUE INDEX IF NOT EXISTS uk_api_category_code ON api_category(category_code);

-- 查询过滤索引：排除已删除记录
CREATE INDEX IF NOT EXISTS idx_api_category_deleted ON api_category(is_deleted);

-- 状态查询索引：按状态快速筛选可用/停用分类
CREATE INDEX IF NOT EXISTS idx_api_category_status ON api_category(status);
