#!/usr/bin/env bash
# 备份三个业务库 + 上传文件，并清理过期备份
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/var/backups/blog}"
STAMP="$(date +%Y%m%d-%H%M%S)"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

mkdir -p "$BACKUP_DIR"

echo "==> 备份数据库"
for db in user_db article_db interaction_db; do
  docker exec blog-postgres pg_dump -U "${POSTGRES_USER:-postgres}" -Fc "$db" \
    > "$BACKUP_DIR/${db}-${STAMP}.dump"
  echo "  $db -> ${db}-${STAMP}.dump"
done

echo "==> 备份上传文件"
docker run --rm \
  -v personal_uploads_data:/data:ro \
  -v "$BACKUP_DIR":/backup \
  alpine tar czf "/backup/uploads-${STAMP}.tar.gz" -C /data .

echo "==> 清理 ${RETENTION_DAYS} 天前的备份"
find "$BACKUP_DIR" -type f -mtime "+$RETENTION_DAYS" -delete

echo "备份完成：$BACKUP_DIR"
