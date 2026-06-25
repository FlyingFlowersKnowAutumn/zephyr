CREATE TABLE comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id      UUID NOT NULL,
    user_id         UUID NOT NULL,
    parent_id       UUID,
    content         TEXT NOT NULL,
    status          VARCHAR(20) DEFAULT 'approved' NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_comments_article ON comments(article_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_comments_user ON comments(user_id);

CREATE TABLE reactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    article_id      UUID NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX idx_reactions_unique ON reactions(user_id, article_id);

CREATE TABLE guestbook (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    content         TEXT NOT NULL,
    status          VARCHAR(20) DEFAULT 'approved' NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_guestbook_user ON guestbook(user_id);
CREATE INDEX idx_guestbook_status ON guestbook(status);
