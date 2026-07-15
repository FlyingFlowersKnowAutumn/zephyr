CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name UUID  NOT NULL UNIQUE,
    description TEXT,
    article_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY,
    name UUID  NOT NULL UNIQUE,
    article_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_tag_map (
    article_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
-- CREATE INDEX IF NOT EXISTS idx_article_tag_map_article ON article_tag_map(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tag_map_tag ON article_tag_map(tag_id);