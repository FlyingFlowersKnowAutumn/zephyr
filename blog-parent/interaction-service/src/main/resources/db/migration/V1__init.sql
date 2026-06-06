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

CREATE TABLE questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content         TEXT NOT NULL,
    status          VARCHAR(20) DEFAULT 'open' NOT NULL,
    is_public       BOOLEAN DEFAULT true NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_questions_status ON questions(status);

CREATE TABLE answers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id     UUID NOT NULL REFERENCES questions(id),
    user_id         UUID NOT NULL,
    content         TEXT NOT NULL,
    is_accepted     BOOLEAN DEFAULT false NOT NULL,
    status          VARCHAR(20) DEFAULT 'published' NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_answers_question ON answers(question_id);
