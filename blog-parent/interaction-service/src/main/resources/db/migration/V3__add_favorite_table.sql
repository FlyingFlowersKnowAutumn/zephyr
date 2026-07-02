CREATE TABLE article_favorites (
    id VARCHAR(36) PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_favorite_article_user ON article_favorites(article_id, user_id);
CREATE INDEX idx_favorite_article_id ON article_favorites(article_id);
CREATE INDEX idx_favorite_user_id ON article_favorites(user_id);
