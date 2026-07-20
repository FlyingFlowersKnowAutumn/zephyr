ALTER TABLE articles ADD COLUMN IF NOT EXISTS slug VARCHAR(500);
CREATE UNIQUE INDEX IF NOT EXISTS idx_articles_slug ON articles(slug);

-- 为已有文章生成 slug：使用 id 的前8位作为临时 slug
UPDATE articles SET slug = 'post-' || substring(id::text, 1, 8) WHERE slug IS NULL;

ALTER TABLE articles ALTER COLUMN slug SET NOT NULL;
