ALTER TABLE articles ADD COLUMN IF NOT EXISTS search_vector TSVECTOR;

CREATE INDEX IF NOT EXISTS idx_articles_search ON articles USING GIN(search_vector);

UPDATE articles SET search_vector = to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, ''))
WHERE search_vector IS NULL;

CREATE OR REPLACE FUNCTION articles_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('simple', coalesce(NEW.title, '') || ' ' || coalesce(NEW.content, ''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_articles_search_vector ON articles;

CREATE TRIGGER trg_articles_search_vector
  BEFORE INSERT OR UPDATE OF title, content ON articles
  FOR EACH ROW
  EXECUTE FUNCTION articles_search_vector_update();
