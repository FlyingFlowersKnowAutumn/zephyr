ALTER TABLE article_favorites
  ALTER COLUMN id TYPE UUID USING id::uuid,
  ALTER COLUMN article_id TYPE UUID USING article_id::uuid,
  ALTER COLUMN user_id TYPE UUID USING user_id::uuid;
