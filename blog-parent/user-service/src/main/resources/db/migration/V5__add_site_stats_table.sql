CREATE TABLE site_stats (
    id              BIGSERIAL PRIMARY KEY,
    visitor_count   BIGINT NOT NULL DEFAULT 0,
    view_count      BIGINT NOT NULL DEFAULT 0,
    article_count   BIGINT NOT NULL DEFAULT 0,
    last_update     TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO site_stats (visitor_count, view_count, article_count) VALUES (0, 0, 0);
