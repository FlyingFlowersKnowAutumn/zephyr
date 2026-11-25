-- 把「纯 Flyway 重建出来的库」对齐到「实际生产库」。
-- 对现有生产库而言，前两条是空操作（name 本来就是 varchar），后面只补缺失的列。
--
-- 注意：不要修改已应用的 V1~V4（Flyway 会校验 checksum）。
-- 也刻意不把 uuid 主键/外键改成 varchar：tags.id 被 article_tag_map.tag_id 外键引用，
-- PostgreSQL 不允许修改被外键引用的列类型，改了会导致整个迁移失败；
-- 而 uuid 列配 String 实体本来就能工作（依赖数据源 URL 的 stringtype=unspecified）。

-- 1) 两个文本列必须是 varchar，不能是 V4 里写的 UUID
ALTER TABLE categories ALTER COLUMN name TYPE varchar(100) USING name::text;
ALTER TABLE tags       ALTER COLUMN name TYPE varchar(100) USING name::text;

-- 2) 补齐 ArticleTagMap 实体需要的 id 列（V4 里漏了）
ALTER TABLE article_tag_map ADD COLUMN IF NOT EXISTS id varchar(36);
UPDATE article_tag_map SET id = gen_random_uuid()::text WHERE id IS NULL;
ALTER TABLE article_tag_map ALTER COLUMN id SET NOT NULL;

-- 3) V4 里被注释掉的索引补回来（按 article_id 关联查询需要）
CREATE INDEX IF NOT EXISTS idx_article_tag_map_article ON article_tag_map(article_id);
